package com.tunan.cinema.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tunan.cinema.common.BizException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long hours;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${app.jwt-secret}") String secret,
                      @Value("${app.jwt-hours:72}") long hours) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.hours = hours;
    }

    public String create(AuthContext.Principal principal) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("uid", principal.id());
            payload.put("username", principal.username());
            payload.put("name", principal.name());
            payload.put("role", principal.role());
            payload.put("exp", Instant.now().plusSeconds(hours * 3600).getEpochSecond());
            String head = encode(objectMapper.writeValueAsBytes(header));
            String body = encode(objectMapper.writeValueAsBytes(payload));
            String content = head + "." + body;
            return content + "." + sign(content);
        } catch (Exception e) {
            throw new BizException("登录凭证生成失败", 500);
        }
    }

    @SuppressWarnings("unchecked")
    public AuthContext.Principal parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) throw new IllegalArgumentException("bad token");
            String content = parts[0] + "." + parts[1];
            String expected = sign(content);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("bad signature");
            }
            Map<String, Object> payload = objectMapper.readValue(Base64.getUrlDecoder().decode(parts[1]), Map.class);
            long exp = ((Number) payload.get("exp")).longValue();
            if (exp < Instant.now().getEpochSecond()) throw new IllegalArgumentException("expired");
            return new AuthContext.Principal(
                    ((Number) payload.get("uid")).longValue(),
                    String.valueOf(payload.get("username")),
                    String.valueOf(payload.get("name")),
                    String.valueOf(payload.get("role"))
            );
        } catch (Exception e) {
            throw new BizException("登录已失效，请重新登录", 401);
        }
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return encode(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
