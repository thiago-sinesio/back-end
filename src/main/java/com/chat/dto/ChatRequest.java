package com.chat.dto;

import java.util.List;

public record ChatRequest(
    String model,
    List<Message> messages,
    Double temperature,
    Integer maxTokens
) {
    public ChatRequest(String model, List<Message> messages) {
        this(model, messages, 0.7, 1024);
    }

    public record Message(String role, String content) {}
}
