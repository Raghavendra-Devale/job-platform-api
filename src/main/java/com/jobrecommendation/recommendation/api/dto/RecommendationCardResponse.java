package com.jobrecommendation.recommendation.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationCardResponse {
    private Long jobId;
    private String title;
    private String company;
    private String location;
    private String description;
    private boolean remote;
    private String postedAt;
    private double similarityScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private String recommendationReason;
    private String applyUrl;
}
