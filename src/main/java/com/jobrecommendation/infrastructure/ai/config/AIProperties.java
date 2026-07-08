package com.jobrecommendation.infrastructure.ai.config;

import java.time.Duration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AIProperties {
    private boolean enabled = true;
    private String baseUrl = "http://localhost:8001";
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(5);
}
