package com.chat.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
    @NotBlank String question
) {}
