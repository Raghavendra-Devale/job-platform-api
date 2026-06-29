package com.raghav.jobplatform.auth.dto;

public record LoginResponse(
        String token,
        Long id,
        String name,
        String email,
        String role
) {}
