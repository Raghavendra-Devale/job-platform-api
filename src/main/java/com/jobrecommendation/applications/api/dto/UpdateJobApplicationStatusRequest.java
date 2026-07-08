package com.jobrecommendation.applications.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateJobApplicationStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {}
