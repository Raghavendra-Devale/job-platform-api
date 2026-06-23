package com.raghav.jobplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "job.api")
public record JobApiProperties(
        String baseUrl,
        String key
) {
}
