package com.chat.dto;

public record DocumentStatusResponse(
    String fileId,
    String originalName,
    String status,
    int totalChunks,
    int totalCharacters
) {}
