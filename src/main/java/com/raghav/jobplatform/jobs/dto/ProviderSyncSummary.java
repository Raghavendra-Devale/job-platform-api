package com.raghav.jobplatform.jobs.dto;

public record ProviderSyncSummary(
        String provider,
        int fetched,
        int inserted,
        int skipped
) {
}
