package com.jobrecommendation.jobs.api.dto;

public record JobResponse(
        String slug,
        String title,
        String company,
        String location,
        String description,
        String applyUrl,
        Boolean remote,
        String tags
) {
}
