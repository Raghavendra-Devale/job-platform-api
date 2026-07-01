package com.raghav.jobplatform.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdatePreferencesRequest(
        @NotBlank(message = "Work preference is required")
        String workPreference,

        @NotNull(message = "Alert preference status is required")
        Boolean alertEnabled,

        String preferredRoles,
        String preferredLocations,
        Boolean remoteOnly,
        String salaryRange,
        String jobTypes
) {}
