package com.raghav.jobplatform.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateJobApplicationStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {}
