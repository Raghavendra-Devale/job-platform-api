package jobs.provider.remoteok;

import jobs.dto.ExternalJob;
import jobs.provider.JobProvider;
import jobs.provider.JobSearchResult;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import recommendation.model.JobSearchCriteria;

import java.util.List;

@Component("remoteOKProvider")
public class RemoteOKProvider implements JobProvider {

    private final RestClient restClient;

    public RemoteOKProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public JobSearchResult searchJobs(JobSearchCriteria criteria) {
        String url = "https://remoteok.com/api";
        try {
            List<RemoteOKJob> response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<RemoteOKJob>>() {});

            if (response == null || response.isEmpty()) {
                return JobSearchResult.failure("Empty response from RemoteOK API");
            }

            String keyword = criteria.getKeyword();

            List<ExternalJob> jobs = response.stream()
                    .filter(job -> job.legal() == null && job.company() != null && job.position() != null)
                    .filter(job -> keyword == null || keyword.isBlank() ||
                            job.position().toLowerCase().contains(keyword.toLowerCase()))
                    .map(job -> ExternalJob.builder()
                            .title(job.position())
                            .company(job.company())
                            .location(job.location())
                            .description(job.description())
                            .applyUrl(job.url())
                            .employmentType(null)
                            .publishedAt(null)
                            .provider("RemoteOK")
                            .providerJobId(job.id())
                            .build())
                    .toList();

            return JobSearchResult.success(jobs);

        } catch (org.springframework.web.client.ResourceAccessException e) {
            return JobSearchResult.failure("RemoteOK API timeout or connection failure: " + e.getMessage());
        } catch (org.springframework.web.client.RestClientResponseException e) {
            return JobSearchResult.failure("RemoteOK API returned error status: " + e.getStatusCode());
        } catch (Exception e) {
            return JobSearchResult.failure("Failed to retrieve jobs from RemoteOK: " + e.getMessage());
        }
    }

    @Override
    public boolean health() {
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

    @Override
    public String getProviderName() {
        return "RemoteOK";
    }
}
