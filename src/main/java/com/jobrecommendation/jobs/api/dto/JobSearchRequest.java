package com.jobrecommendation.jobs.api.dto;

public record JobSearchRequest(
        String keyword,
        String location,
        Boolean remote,
        String salaryMin,
        String salaryMax,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection,
        String provider,
        String experience,
        String jobType,
        String company
) {
}
