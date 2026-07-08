package com.jobrecommendation.applications.api.dto;

import com.jobrecommendation.jobs.domain.Job;
import jakarta.validation.constraints.NotNull;

public record CreateJobApplicationRequest(
        @NotNull(message = "Job ID is required")
        Long jobId
) {}
