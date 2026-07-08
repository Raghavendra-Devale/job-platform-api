package com.jobrecommendation.dashboard.api.dto;

import com.jobrecommendation.user.api.dto.ActivityLogResponse;
import java.util.List;

public record DashboardSummaryResponse(
        String activeResumeName,
        long totalResumesCount,
        long savedJobsCount,
        long applicationsCount,
        long interviewsCount,
        long offersCount,
        List<ActivityLogResponse> recentActivities
) {}
