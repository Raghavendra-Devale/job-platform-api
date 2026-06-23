package com.raghav.jobplatform.jobs.client;

import com.raghav.jobplatform.config.JobApiProperties;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

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
