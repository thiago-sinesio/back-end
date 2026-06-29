package com.chat.dto;

import java.util.List;
import java.util.UUID;

public record RagQueryResponse(
    String answer,
    List<ChunkContext> context,
    Integer totalTokensUsed
) {
    public record ChunkContext(
        UUID chunkId,
        String content,
        Double similarity,
        UUID fileId
    ) {}
}
