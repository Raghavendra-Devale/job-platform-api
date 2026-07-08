package com.jobrecommendation.applications.api.dto;

import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String company,
        String location,
        String status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt,
        String resumeName
) {}
