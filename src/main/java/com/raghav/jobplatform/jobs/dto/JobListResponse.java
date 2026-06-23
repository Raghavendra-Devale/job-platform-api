package com.raghav.jobplatform.jobs.dto;

public record JobListResponse(
        Long id,
        String title,
        String company,
        String location,
        String source
) {
}