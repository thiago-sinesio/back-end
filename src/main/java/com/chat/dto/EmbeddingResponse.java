package com.chat.dto;

import java.util.List;

public record EmbeddingResponse(
    String model,
    List<EmbeddingData> data,
    Usage usage
) {
    public record EmbeddingData(Integer index, float[] embedding, String object) {}

    public record Usage(Integer promptTokens, Integer totalTokens) {}
}
