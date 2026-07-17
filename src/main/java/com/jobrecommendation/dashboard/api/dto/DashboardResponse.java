package com.jobrecommendation.dashboard.api.dto;

import com.jobrecommendation.resume.api.dto.ResumeIntelligenceResponse;
import com.jobrecommendation.recommendation.api.dto.RecommendationRunResponse;
import java.util.List;

public record DashboardResponse(
    UserSummaryDto userSummary,
    ResumeIntelligenceResponse resumeIntelligence,
    RecommendationStatusDto recommendationStatus,
    RecommendationRunResponse latestRecommendationRun,
    List<CareerInsightDto> careerInsights,
    ApplicationSummaryDto applicationSummary,
    List<RecentlyViewedJobDto> recentlyViewedJobs
) {}
