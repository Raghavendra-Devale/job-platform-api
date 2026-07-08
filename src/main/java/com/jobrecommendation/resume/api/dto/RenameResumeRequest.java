package com.jobrecommendation.resume.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameResumeRequest(
    @NotBlank(message = "Resume name cannot be blank")
    String resumeName
) {}
