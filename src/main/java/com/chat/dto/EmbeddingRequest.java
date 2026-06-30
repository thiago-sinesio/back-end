package com.chat.dto;

public record EmbeddingRequest(
    String model,
    String input
) {}
