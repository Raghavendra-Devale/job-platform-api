package com.jobrecommendation.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @jakarta.validation.constraints.Min(value = 0, message = "Experience cannot be negative")
        Integer experience,

        String currentRole,
        String bio,
        String linkedin,
        String github,
        String portfolio,
        String phone,
        String location
) {}
