package com.raghav.jobplatform.auth.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String role,
        String resumeFileName,
        LocalDateTime createdAt
) {}
