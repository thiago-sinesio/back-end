package com.chat.dto;

import java.util.List;

public record ChatResponse(
    String id,
    String model,
    List<Choice> choices,
    Usage usage
) {
    public record Choice(Message message, Integer index, String finishReason) {
        public record Message(String role, String content) {}
    }

    public record Usage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {}
}
