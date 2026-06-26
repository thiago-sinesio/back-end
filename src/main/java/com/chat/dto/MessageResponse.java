package com.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageResponse(
    UUID id,
    UUID sessionId,
    String role,
    String content,
    UUID fileId,
    LocalDateTime timestamp
) {}
