package com.raghav.jobplatform.jobs.provider.remoteok;

import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RemoteOkProvider implements JobProvider {

    private final RestClient restClient;

    @Override
    public List<JobResponse> searchJobs(String keyword) {

        List<RemoteOkResponse> jobs =
                restClient.get()
                        .uri("https://remoteok.com/api")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});

        if (jobs == null) {
            return List.of();
        }


        String q = keyword.toLowerCase();
        return jobs.stream()
                .filter(job -> {
                    boolean positionMatch =
                            job.getPosition() != null &&
                            job.getPosition().toLowerCase().contains(q);

                    boolean descriptionMatch =
                            job.getDescription() != null &&
                            job.getDescription().toLowerCase().contains(q);

                    boolean tagMatch =
                            job.getTags() != null &&
                            job.getTags().stream()
                                    .anyMatch(tag ->
                                            tag.toLowerCase().contains(q));

                    return positionMatch || descriptionMatch || tagMatch;
                })
                .map(job -> new JobResponse(
                        String.valueOf(job.getId()),
                        job.getPosition(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getDescription(),
                        job.getUrl(),
                        true,
                        job.getTags() == null
                                ? ""
                                : String.join(",", job.getTags())
                ))
                .toList();
    }

    @Override
    public List<JobResponse> fetchAllJobs() {
        return searchJobs("");
    }

    @Override
    public String getProviderName() {
        return "REMOTEOK";
    }

    @Override
    public boolean isHealthy() {
        try {
            restClient.get()
                    .uri("https://remoteok.com/api")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}