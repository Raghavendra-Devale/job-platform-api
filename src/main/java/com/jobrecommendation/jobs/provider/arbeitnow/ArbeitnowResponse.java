package com.jobrecommendation.jobs.provider.arbeitnow;

import java.util.List;

public record ArbeitnowResponse(
        List<ArbeitnowJob> data
) {
}
