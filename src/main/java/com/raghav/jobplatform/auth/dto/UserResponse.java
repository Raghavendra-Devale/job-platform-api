package com.raghav.jobplatform.auth.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        String resumeFileName,
        String workPreference,
        Boolean alertEnabled,
        LocalDateTime createdAt,

        // Profile fields
        Integer experience,
        String currentRole,
        String bio,
        String linkedin,
        String github,
        String portfolio,
        String phone,
        String location,

        // Preference fields
        String preferredRoles,
        String preferredLocations,
        Boolean remoteOnly,
        String salaryRange,
        String jobTypes
) {}
