package com.chat.dto;

import java.util.List;

public record LlmRequest(
    String model,
    List<Message> messages,
    Double temperature,
    Integer maxTokens
) {
    public LlmRequest(String model, List<Message> messages) {
        this(model, messages, 0.7, 1024);
    }

    public record Message(String role, String content) {}
}
