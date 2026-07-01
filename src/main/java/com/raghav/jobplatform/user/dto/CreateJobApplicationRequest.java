package com.raghav.jobplatform.user.dto;

import jakarta.validation.constraints.NotNull;

public record CreateJobApplicationRequest(
        @NotNull(message = "Job ID is required")
        Long jobId
) {}
