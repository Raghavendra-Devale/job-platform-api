package com.raghav.jobplatform.jobs.dto;

import java.time.LocalDateTime;

public record JobListResponse(
        Long id,
        String title,
        String company,
        String location,
        String source,
        Boolean remote,
        String tags,
        LocalDateTime createdAt,
        String salary,
        String jobType,
        String applyUrl
) {
}