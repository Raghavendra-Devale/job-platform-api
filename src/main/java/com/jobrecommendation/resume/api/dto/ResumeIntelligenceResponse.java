package com.jobrecommendation.resume.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeIntelligenceResponse {
    private String status;
    private ResumeSummaryDto summary;
    private Integer resumeScore;
    private List<String> skills;
    private List<String> strengths;
    private List<String> improvementSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumeSummaryDto {
        private String candidateName;
        private String role;
        private String experience;
    }
}
