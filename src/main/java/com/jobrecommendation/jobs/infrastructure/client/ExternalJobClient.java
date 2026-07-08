package com.jobrecommendation.jobs.infrastructure.client;

import com.jobrecommendation.config.JobApiProperties;
import com.jobrecommendation.jobs.api.dto.JobResponse;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalJobClient {
    private final RestClient restClient;
    private final JobApiProperties properties;

    public ExternalJobClient(
            RestClient.Builder builder,
            JobApiProperties properties
    ) {
        this.restClient = builder.build();
        this.properties = properties;
    }

    public List<JobResponse> searchJobs(String keyword) {
        return List.of(
                new JobResponse(
                        "java-developer-infosys",
                        "Java Developer",
                        "Infosys",
                        "Bangalore",
                        "Spring Boot Developer",
                        null,
                        false,
                        ""
                ),
                new JobResponse(
                        "backend-engineer-tcs",
                        "Backend Engineer",
                        "TCS",
                        "Pune",
                        "Java Developer",
                        null,
                        false,
                        ""
                )
        );
    }
}
