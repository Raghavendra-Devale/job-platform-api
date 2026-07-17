package com.jobrecommendation.dashboard.api.dto;

public record ApplicationSummaryDto(
    long saved,
    long applied,
    long interview,
    long offer
) {}
