package com.tunan.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
        @NotBlank(message = "请输入你想咨询的问题")
        @Size(max = 500, message = "问题请控制在 500 字以内")
        String message,
        @Size(max = 64, message = "会话编号格式不正确")
        String sessionId
) {
}