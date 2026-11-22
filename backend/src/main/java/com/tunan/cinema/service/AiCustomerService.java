package com.tunan.cinema.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunan.cinema.common.BizException;
import com.tunan.cinema.mapper.AiCustomerServiceMapper;
import com.tunan.cinema.mapper.PublicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiCustomerService {
    private static final Logger log = LoggerFactory.getLogger(AiCustomerService.class);
    private static final int WINDOW_SECONDS = 60;
    private static final int MAX_REQUESTS_PER_WINDOW = 20;
    private static final String FALLBACK_REPLY = "我暂时没找到这个问题的准确答案 🐰\n你可以试试问我：怎么购票、如何选座、取票码在哪、订单能否取消、影院地址或影片推荐。复杂问题可发送邮件到 hello@tunan.cn。";
    private static final List<String> SUGGESTIONS = List.of(
            "怎么在线选座购票？",
            "取票码在哪里看？",
            "订单可以取消吗？",
            "推荐一部正在热映的电影"
    );

    private final AiCustomerServiceMapper chatMapper;
    private final PublicMapper publicMapper;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Map<String, RateWindow> rateWindows = new ConcurrentHashMap<>();

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${app.ai.model:gpt-4o-mini}")
    private String model;

    public AiCustomerService(AiCustomerServiceMapper chatMapper,
                             PublicMapper publicMapper,
                             ObjectMapper objectMapper) {
        this.chatMapper = chatMapper;
        this.publicMapper = publicMapper;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    public Map<String, Object> chat(String requestedSessionId, String rawMessage, String clientIp) {
        String message = rawMessage == null ? "" : rawMessage.trim();
        if (message.isEmpty()) throw new BizException("请输入你想咨询的问题");
        if (message.length() > 500) throw new BizException("问题请控制在 500 字以内");
        String sessionId = safeSessionId(requestedSessionId);
        checkRate(clientIp == null || clientIp.isBlank() ? "unknown" : clientIp);

        List<Map<String, Object>> history = loadHistory(sessionId);
        persist(sessionId, "USER", message, "USER");

        String local = localReply(message);
        String reply;
        String source;
        if (!FALLBACK_REPLY.equals(local)) {
            reply = local;
            source = "KNOWLEDGE_BASE";
        } else if (apiKey != null && !apiKey.isBlank()) {
            try {
                reply = callModel(history, message);
                source = "AI_MODEL";
            } catch (Exception ex) {
                log.warn("AI model call failed, using knowledge-base fallback: {}", ex.getMessage());
                reply = local;
                source = "KNOWLEDGE_BASE";
            }
        } else {
            reply = local;
            source = "KNOWLEDGE_BASE";
        }

        reply = truncate(reply == null ? "我暂时没有理解这个问题，可以换个说法再问一次。" : reply.trim(), 1800);
        persist(sessionId, "ASSISTANT", reply, source);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("sessionId", sessionId);
        result.put("reply", reply);
        result.put("source", source);
        result.put("suggestions", suggestionsFor(message));
        result.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        return result;
    }

    private String callModel(List<Map<String, Object>> history, String message) throws Exception {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt()));
        for (Map<String, Object> item : history) {
            String role = "USER".equals(String.valueOf(item.get("role"))) ? "user" : "assistant";
            String content = String.valueOf(item.getOrDefault("content", ""));
            if (!content.isBlank()) messages.add(Map.of("role", role, "content", content));
        }
        messages.add(Map.of("role", "user", "content", message));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.35);
        body.put("max_tokens", 500);

        String endpoint = baseUrl.replaceAll("/+$", "");
        if (!endpoint.endsWith("/chat/completions")) endpoint += "/chat/completions";
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("AI provider returned HTTP " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode content = root.path("choices").path(0).path("message").path("content");
        if (!content.isTextual() || content.asText().isBlank()) {
            throw new IllegalStateException("AI provider returned an empty answer");
        }
        return content.asText();
    }

    private String systemPrompt() {
        return """
            你是“兔南电影购票系统”的智能客服，名字叫“小兔客服”。请始终使用简体中文，语气亲切、简短、准确。
            你只能依据下面的系统知识回答；不知道时建议用户查看对应页面或联系影院，不要编造订单状态、退款结果或影院政策。
            本系统是课程设计演示项目，支付、退款和取票均为演示流程。不要索取密码、银行卡、验证码等敏感信息。
            回答一般控制在 80 到 220 个汉字；用户需要操作步骤时使用简短编号。

            【系统知识】
            %s
            """.formatted(knowledgeContext());
    }

    private String knowledgeContext() {
        StringBuilder text = new StringBuilder();
        text.append("购票流程：进入“电影”页选择影片，打开详情后选择影院和场次，进入选座页选座并提交订单，然后在订单页完成模拟支付。\n");
        text.append("支付说明：系统仅演示微信/支付宝支付结果，不会产生真实扣款。支付成功后可在“购票记录”或订单详情查看6位取票码。\n");
        text.append("取消说明：未支付订单可取消；已支付但未取票的订单也可在订单页取消，取消后状态变为“已取消”。演示系统不处理真实退款。\n");
        text.append("取票说明：已支付订单显示6位取票码，请到影院自助取票机输入；实际影院的取票规则以影院现场为准。\n");
        text.append("账号说明：可注册普通影迷账号；影院管理员和平台管理员从“管理端登录”进入。用户可在个人中心修改资料和密码。\n");
        text.append("其他功能：支持电影收藏、评分短评、排行榜、公告、影院查询；影院可提交入驻申请，审核后由平台管理。\n");
        try {
            List<Map<String, Object>> movies = publicMapper.nowMovies(8);
            if (!movies.isEmpty()) {
                text.append("正在热映：");
                List<String> movieTexts = new ArrayList<>();
                for (Map<String, Object> movie : movies) {
                    movieTexts.add(movie.get("title") + "（" + movie.get("score") + "分）");
                }
                text.append(String.join("、", movieTexts)).append("。\n");
            }
        } catch (Exception ignored) {
        }
        try {
            List<Map<String, Object>> cinemas = publicMapper.cinemas(null);
            if (!cinemas.isEmpty()) {
                text.append("影院信息：");
                List<String> cinemaTexts = new ArrayList<>();
                for (Map<String, Object> cinema : cinemas) {
                    cinemaTexts.add(cinema.get("name") + "，地址" + cinema.get("address")
                            + "，电话" + cinema.get("phone"));
                }
                text.append(String.join("；", cinemaTexts)).append("。\n");
            }
        } catch (Exception ignored) {
        }
        text.append("联系邮箱：hello@tunan.cn。\n");
        return text.toString();
    }

    private String localReply(String message) {
        String text = message.toLowerCase(Locale.ROOT).replace(" ", "");
        if (containsAny(text, "你好", "您好", "hi", "hello", "在吗")) {
            return "你好，我是小兔客服 🐰 我可以帮你查询购票、选座、支付、取票、取消订单、影院地址和影片推荐。你想先了解哪一项？";
        }
        if (containsAny(text, "退票", "退款", "取消订单", "取消", "改签")) {
            return "可以这样处理：\n1. 打开“购票记录”；\n2. 找到对应订单，点击“取消订单”；\n3. 未支付和已支付未取票的订单都可取消。\n\n这是课程演示系统，取消后只更新订单状态，不会产生真实扣款或退款。改签暂未开放，可取消后重新购票。";
        }
        if (containsAny(text, "取票码", "取票", "怎么拿票", "兑票")) {
            return "支付成功后，在“购票记录”或订单详情里能看到 6 位取票码。到影院自助取票机输入取票码即可取票。建议提前 30 分钟到店。";
        }
        if (containsAny(text, "支付", "付款", "微信", "支付宝", "扣款")) {
            return "选座并提交订单后，进入“购票记录”，打开待支付订单并选择微信或支付宝完成演示支付。系统不会产生真实扣款。";
        }
        if (containsAny(text, "选座", "买票", "购票", "订票", "怎么买")) {
            return "购票步骤：\n1. 在“电影”页选择影片；\n2. 打开电影详情，选择影院和场次；\n3. 选择座位并提交订单；\n4. 在“购票记录”完成演示支付；\n5. 支付后查看 6 位取票码。";
        }
        if (containsAny(text, "订单", "订单号", "记录", "查询订单")) {
            return "登录后点击顶部“购票记录”，可以查看全部订单、待支付、待取票、已取票和已取消订单，也能按订单号查询。";
        }
        if (containsAny(text, "登录", "注册", "密码", "账号", "登录不了")) {
            return "普通影迷可在右上角“登录 / 注册”创建账号；忘记密码或无法登录时，请检查账号状态。管理端请从页面底部的“管理端登录”进入。为了安全，请不要把密码发给任何人。";
        }
        if (containsAny(text, "影院", "地址", "在哪", "电话", "门店")) {
            return cinemaAnswer();
        }
        if (containsAny(text, "推荐", "看什么", "电影", "热映", "好看")) {
            return movieAnswer();
        }
        if (containsAny(text, "收藏", "评分", "评论", "短评", "榜单")) {
            return "电影详情页支持收藏和评分短评；“排行榜”页面可查看票房榜与观众评分榜。登录后才能保存收藏和评论。";
        }
        if (containsAny(text, "入驻", "商家", "合作", "开影院")) {
            return "从页脚进入“影院入驻”，填写影院资料并提交申请。平台审核通过后，影院管理员即可维护影厅、申请排片并核销取票。";
        }
        if (containsAny(text, "人工", "联系", "邮箱", "投诉", "建议")) {
            return "我可以先帮你处理常见问题。如需人工帮助，可发送邮件到 hello@tunan.cn，或在“影院信息”页面查看对应门店电话。";
        }
        if (containsAny(text, "ai", "机器人", "你是谁")) {
            return "我是兔南电影购票系统的“小兔客服”。有 AI 服务可用时由大模型回答，网络或密钥不可用时会自动切换到内置知识库，所以仍能继续回复。";
        }
        return FALLBACK_REPLY;
    }

    private String cinemaAnswer() {
        try {
            List<Map<String, Object>> cinemas = publicMapper.cinemas(null);
            if (!cinemas.isEmpty()) {
                StringBuilder answer = new StringBuilder("目前可查询的影院：\n");
                for (int i = 0; i < Math.min(cinemas.size(), 5); i++) {
                    Map<String, Object> cinema = cinemas.get(i);
                    answer.append(i + 1).append(". ").append(cinema.get("name"))
                            .append("：").append(cinema.get("address"))
                            .append("，电话 ").append(cinema.get("phone")).append("\n");
                }
                return answer.append("\n更多场次和路线可在顶部“影院”页查看。").toString();
            }
        } catch (Exception ignored) {
        }
        return "可在顶部“影院”页查看门店名称、地址、电话和近期场次。";
    }

    private String movieAnswer() {
        try {
            List<Map<String, Object>> movies = publicMapper.nowMovies(5);
            if (!movies.isEmpty()) {
                StringBuilder answer = new StringBuilder("正在热映中，可以看看：\n");
                for (int i = 0; i < movies.size(); i++) {
                    Map<String, Object> movie = movies.get(i);
                    answer.append(i + 1).append(". 《").append(movie.get("title"))
                            .append("》").append(movie.get("score")).append("分");
                    if (i < movies.size() - 1) answer.append("\n");
                }
                return answer.append("\n\n想看哪一部？进入“电影”页即可查看场次和选座。").toString();
            }
        } catch (Exception ignored) {
        }
        return "可以进入“电影”页查看正在热映、即将上映和排行榜，再按类型筛选题材。";
    }

    private List<String> suggestionsFor(String message) {
        String text = message.toLowerCase(Locale.ROOT);
        if (containsAny(text, "取消", "退票", "退款", "改签")) {
            return List.of("取票码在哪里看？", "支付遇到问题怎么办？", "怎么重新购票？");
        }
        if (containsAny(text, "影院", "地址", "电话")) {
            return List.of("还有哪些热映电影？", "怎么在线选座？", "影院入驻怎么申请？");
        }
        return SUGGESTIONS;
    }

    private List<Map<String, Object>> loadHistory(String sessionId) {
        try {
            List<Map<String, Object>> rows = new ArrayList<>(chatMapper.recentMessages(sessionId, 6));
            Collections.reverse(rows);
            return rows;
        } catch (Exception ex) {
            log.warn("Unable to load AI chat history: {}", ex.getMessage());
            return List.of();
        }
    }

    private void persist(String sessionId, String role, String content, String source) {
        try {
            chatMapper.insertMessage(sessionId, null, role, truncate(content, 2000), source);
        } catch (Exception ex) {
            log.warn("Unable to save AI chat message: {}", ex.getMessage());
        }
    }

    private String safeSessionId(String requested) {
        if (requested == null || requested.isBlank()) return UUID.randomUUID().toString();
        String cleaned = requested.trim().replaceAll("[^a-zA-Z0-9_-]", "");
        return cleaned.isBlank() ? UUID.randomUUID().toString() : truncate(cleaned, 64);
    }

    private void checkRate(String clientIp) {
        long now = System.currentTimeMillis();
        RateWindow window = rateWindows.computeIfAbsent(clientIp, key -> new RateWindow(now));
        synchronized (window) {
            if (now - window.startedAt >= WINDOW_SECONDS * 1000L) {
                window.startedAt = now;
                window.count.set(0);
            }
            if (window.count.incrementAndGet() > MAX_REQUESTS_PER_WINDOW) {
                throw new BizException("消息发得太快了，请稍等一分钟再试", 429);
            }
        }
        if (rateWindows.size() > 500) {
            rateWindows.entrySet().removeIf(entry -> now - entry.getValue().startedAt > 10 * 60 * 1000L);
        }
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) if (text.contains(word)) return true;
        return false;
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private static final class RateWindow {
        private long startedAt;
        private final AtomicInteger count = new AtomicInteger();

        private RateWindow(long startedAt) {
            this.startedAt = startedAt;
        }
    }
}