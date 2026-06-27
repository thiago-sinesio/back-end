package com.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MessageRequest(
    @NotNull UUID sessionId,
    @NotBlank String role,
    String content,
    UUID fileId
) {}
