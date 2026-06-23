package com.raghav.jobplatform.jobs.dto;

public record JobSearchRequest(
        String keyword,
        String location,
        Boolean remote,
        String salaryMin,
        String salaryMax,
        Integer page,
        Integer size,
        String sortBy,
        String sortDirection
) {
}