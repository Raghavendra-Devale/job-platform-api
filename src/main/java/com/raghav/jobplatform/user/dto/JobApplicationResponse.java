package com.raghav.jobplatform.user.dto;

import java.time.LocalDateTime;

public record JobApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String company,
        String location,
        String status,
        LocalDateTime appliedAt,
        String resumeName
) {}
