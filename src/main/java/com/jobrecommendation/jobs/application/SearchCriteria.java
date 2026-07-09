package com.jobrecommendation.jobs.application;

public record SearchCriteria(
        String keyword,
        String location,
        String provider,
        Boolean remote,
        Integer salaryMin,
        Integer salaryMax,
        String experience,
        String jobType,
        String company,
        int page,
        int size,
        String sortBy,
        String sortDirection
) {}
