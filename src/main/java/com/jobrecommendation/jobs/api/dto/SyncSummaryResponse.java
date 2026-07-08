package com.jobrecommendation.jobs.api.dto;

import java.util.List;

public record SyncSummaryResponse(
        List<ProviderSyncSummary> providers
) {
}
