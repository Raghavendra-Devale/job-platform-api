package com.raghav.jobplatform.jobs.dto;

import java.util.List;
import java.util.Map;

public record JobSearchResponse(
        List<JobListResponse> content,
        Map<String, List<String>> filters,
        long totalElements,
        int totalPages,
        int size,
        int number
) {
}
