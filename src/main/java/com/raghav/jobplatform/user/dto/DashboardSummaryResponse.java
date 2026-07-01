package com.raghav.jobplatform.user.dto;

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
