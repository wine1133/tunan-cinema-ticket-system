package com.tunan.cinema.controller;

import com.tunan.cinema.common.PublicApi;
import com.tunan.cinema.dto.AiChatRequest;
import com.tunan.cinema.service.AiCustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@PublicApi
@RestController
@RequestMapping("/api/public/ai-chat")
public class AiCustomerServiceController {
    private final AiCustomerService service;

    public AiCustomerServiceController(AiCustomerService service) {
        this.service = service;
    }

    @PostMapping
    public Map<String, Object> chat(@Valid @RequestBody AiChatRequest request,
                                    HttpServletRequest httpRequest) {
        return service.chat(request.sessionId(), request.message(), clientIp(httpRequest));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        String realIp = request.getHeader("X-Real-IP");
        return realIp == null || realIp.isBlank() ? request.getRemoteAddr() : realIp;
    }
}