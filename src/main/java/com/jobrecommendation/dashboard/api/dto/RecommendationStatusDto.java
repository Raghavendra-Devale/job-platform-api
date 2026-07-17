package com.jobrecommendation.dashboard.api.dto;

import java.time.LocalDateTime;

public record RecommendationStatusDto(
    String status,
    String message,
    LocalDateTime generatedAt,
    boolean isOutdated
) {}
