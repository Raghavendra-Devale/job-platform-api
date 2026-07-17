package com.jobrecommendation.recommendation.api.dto;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record RecommendationRunSummaryResponse(
    Long id,
    LocalDateTime generatedAt,
    Double averageMatch,
    Integer recommendationCount
) {}
