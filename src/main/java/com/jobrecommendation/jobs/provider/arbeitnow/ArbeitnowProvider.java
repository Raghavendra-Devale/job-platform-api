package com.jobrecommendation.jobs.provider.arbeitnow;

import com.jobrecommendation.jobs.api.dto.JobResponse;
import com.jobrecommendation.jobs.infrastructure.provider.JobProvider;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ArbeitnowProvider implements JobProvider {
    private RestClient restClient;

    public ArbeitnowProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public List<JobResponse> searchJobs(String keyword) {
        var response = restClient.get()
                .uri("https://www.arbeitnow.com/api/job-board-api")
                .retrieve()
                .body(ArbeitnowResponse.class);
        if (response == null) {
            return List.of();
        }
        System.out.println("ARBEITNOW response count = " + response.data().size());
        return response.data()
                .stream()
                .filter(job ->
                        job.title().toLowerCase()
                                .contains(keyword.toLowerCase())
                )
                .map(job -> new JobResponse(
                        job.slug(),
                        job.title(),
                        job.company_name(),
                        job.location(),
                        job.description(),
                        job.url(),
                        job.remote(),
                        job.tags() == null
                                ? ""
                                : String.join(",", job.tags())
                ))
                .toList();
    }

    @Override
    public List<JobResponse> fetchAllJobs() {
        return searchJobs("");
    }

    @Override
    public String getProviderName() {
        return "ARBEITNOW";
    }

    @Override
    public boolean isHealthy() {
        try {
            restClient.get()
                    .uri("https://www.arbeitnow.com/api/job-board-api")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
