package com.jobrecommendation.user.api.dto;

import java.time.LocalDateTime;

public record ActivityLogResponse(
        Long id,
        String activityType,
        String description,
        LocalDateTime createdAt
) {}
