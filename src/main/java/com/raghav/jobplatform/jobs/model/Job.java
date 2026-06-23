package com.raghav.jobplatform.jobs.model;

public record Job(
        String title,
        String company,
        String location,
        String description,
        String applyUrl,
        Boolean remote,
        String salary,
        String jobType,
        String source
) {
}