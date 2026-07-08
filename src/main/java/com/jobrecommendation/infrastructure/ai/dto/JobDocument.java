package com.jobrecommendation.infrastructure.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.jobrecommendation.jobs.domain.Job;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JobDocument {
    @NotBlank(message = "Job title is required")
    private String title;

    @NotBlank(message = "Company is required")
    private String company;

    private String location;

    @NotBlank(message = "Job description is required")
    private String description;

    private String employmentType;

    @NotBlank(message = "Apply URL is required")
    private String applyUrl;

    private LocalDateTime publishedAt;
}
