package com.jobrecommendation.jobs.api.dto;

public record ProviderSyncSummary(
        String provider,
        int fetched,
        int inserted,
        int skipped
) {
}
