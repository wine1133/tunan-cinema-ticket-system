package com.tunan.cinema.controller;

import com.tunan.cinema.auth.AuthContext;
import com.tunan.cinema.common.BizException;
import com.tunan.cinema.mapper.UserMapper;
import com.tunan.cinema.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserMapper mapper;
    private final UserService service;

    public UserController(UserMapper mapper, UserService service) {
        this.mapper = mapper;
        this.service = service;
    }

    @GetMapping("/favorites")
    public Object favorites() { return mapper.favorites(AuthContext.get().id()); }

    @GetMapping("/favorites/{movieId}")
    public Map<String, Object> favoriteStatus(@PathVariable Long movieId) {
        return Map.of("favorite", mapper.isFavorite(AuthContext.get().id(), movieId) > 0);
    }

    @PostMapping("/favorites/{movieId}")
    public Map<String, Object> favorite(@PathVariable Long movieId) {
        return service.toggleFavorite(AuthContext.get().id(), movieId, true);
    }

    @DeleteMapping("/favorites/{movieId}")
    public Map<String, Object> unfavorite(@PathVariable Long movieId) {
        return service.toggleFavorite(AuthContext.get().id(), movieId, false);
    }

    @PostMapping("/movies/{movieId}/reviews")
    public Map<String, Object> review(@PathVariable Long movieId, @RequestBody Map<String, Object> body) {
        Object raw = body.get("score");
        if (raw == null) throw new BizException("请选择评分");
        return service.review(AuthContext.get().id(), movieId, new BigDecimal(String.valueOf(raw)),
                body.get("content") == null ? "" : String.valueOf(body.get("content")));
    }

    @GetMapping("/movies/{movieId}/review")
    public Object myReview(@PathVariable Long movieId) {
        Map<String, Object> review = mapper.myReview(AuthContext.get().id(), movieId);
        return review == null ? Map.of() : review;
    }

    @GetMapping("/orders")
    public Object orders() { return mapper.userOrders(AuthContext.get().id()); }

    @GetMapping("/orders/{id}")
    public Map<String, Object> order(@PathVariable Long id) {
        return service.orderDetail(AuthContext.get().id(), id);
    }

    @GetMapping("/orders/track")
    public Map<String, Object> track(@RequestParam String orderNo) {
        return service.track(AuthContext.get().id(), orderNo);
    }

    @PostMapping("/orders")
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        Object showing = body.get("showingId");
        if (showing == null) throw new BizException("场次信息缺失");
        Object seatsRaw = body.get("seats");
        if (!(seatsRaw instanceof List<?> list)) throw new BizException("请选择座位");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seats = (List<Map<String, Object>>) list;
        return service.createOrder(AuthContext.get().id(), Long.valueOf(String.valueOf(showing)), seats);
    }

    @PostMapping("/orders/{id}/pay")
    public Map<String, Object> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String method = body == null || body.get("method") == null ? "微信支付" : String.valueOf(body.get("method"));
        return service.pay(AuthContext.get().id(), id, method);
    }

    @PostMapping("/orders/{id}/cancel")
    public Map<String, Object> cancel(@PathVariable Long id) {
        return service.cancel(AuthContext.get().id(), id);
    }
}
