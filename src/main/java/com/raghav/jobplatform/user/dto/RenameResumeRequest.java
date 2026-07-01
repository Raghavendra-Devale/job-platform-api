package com.raghav.jobplatform.user.dto;

import jakarta.validation.constraints.NotBlank;

public record RenameResumeRequest(
    @NotBlank(message = "Resume name cannot be blank")
    String resumeName
) {}
