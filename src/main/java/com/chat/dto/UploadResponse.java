package com.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UploadResponse(
    UUID fileId,
    String originalName,
    String mimeType,
    Long size,
    String extractedText,
    LocalDateTime uploadedAt
) {}
