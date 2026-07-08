package com.jobrecommendation.resume.api.dto;

import java.time.LocalDateTime;

public record ResumeResponse(
    Long id,
    String resumeName,
    boolean isActive,
    LocalDateTime updatedAt
) {}
