package com.jobrecommendation.jobs.provider.arbeitnow;

import java.util.List;

public record ArbeitnowJob(
        String slug,
        String company_name,
        String title,
        String location,
        String description,
        String url,
        Boolean remote,
        List<String> tags
) {
}
