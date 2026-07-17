package com.jobrecommendation.recommendation.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record RecommendationRunResponse(
    Long id,
    LocalDateTime generatedAt,
    Double averageMatch,
    Integer recommendationCount,
    List<RecommendationCardResponse> items
) {}
