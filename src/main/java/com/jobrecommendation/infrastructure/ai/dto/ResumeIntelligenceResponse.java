package com.jobrecommendation.infrastructure.ai.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ResumeIntelligenceResponse {
    private boolean success;
    private Double processingTimeMs;
    private ResumeDetails resume;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ResumeDetails {
        private String extractedText;
        private String summary;
        private List<SkillDto> skills;
        private List<EducationDto> education;
        private List<ExperienceDto> experience;
        private List<ProjectDto> projects;
        private List<CertificationDto> certifications;
        private List<LanguageDto> languages;
        private String embeddingModel;
        private Integer embeddingDimensions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDto {
        private String name;
        private Double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class EducationDto {
        private String degree;
        private String institution;
        private String startDate;
        private String endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ExperienceDto {
        private String company;
        private String designation;
        private String startDate;
        private String endDate;
        private List<String> responsibilities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectDto {
        private String name;
        private String description;
        private List<String> technologies;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationDto {
        private String name;
        private String issuer;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LanguageDto {
        private String name;
        private String proficiency;
    }
}
