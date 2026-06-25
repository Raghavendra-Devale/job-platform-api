package com.raghav.jobplatform.jobs.dto;

import java.util.List;

public record SyncSummaryResponse(
        List<ProviderSyncSummary> providers
) {
}
