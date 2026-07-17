package com.jobrecommendation.dashboard.api.dto;

public record RecentlyViewedJobDto(
    Long id,
    String title,
    String company,
    String location,
    Boolean remote
) {}
