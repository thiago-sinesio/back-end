package com.chat.dto;

import java.time.Instant;

public record HealthResponse(
    String status,
    Instant timestamp,
    String version
) {}
