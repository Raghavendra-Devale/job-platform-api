package jobs.provider.arbeitnow;

import com.raghav.jobplatform.jobs.provider.arbeitnow.ArbeitnowResponse;
import jobs.dto.ExternalJob;
import jobs.provider.JobProvider;
import jobs.provider.JobSearchResult;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import recommendation.model.JobSearchCriteria;

import java.util.List;

@Component("newArbeitnowProvider")
public class ArbeitnowProvider implements JobProvider {

    private final RestClient restClient;

    public ArbeitnowProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public JobSearchResult searchJobs(JobSearchCriteria criteria) {
        String url = "https://www.arbeitnow.com/api/job-board-api";
        ArbeitnowResponse response;
        try {
            response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(ArbeitnowResponse.class);
        } catch (Exception e) {
            return JobSearchResult.failure(e.getMessage());
        }

        if (response == null || response.data() == null) {
            return JobSearchResult.failure("Null response from API");
        }

        String keyword = criteria.getKeyword();
        List<ExternalJob> jobsList = response.data().stream()
                .filter(job -> keyword == null || keyword.isBlank() ||
                        job.title().toLowerCase().contains(keyword.toLowerCase()))
                .map(job -> ExternalJob.builder()
                        .title(job.title())
                        .company(job.company_name())
                        .location(job.location())
                        .description(job.description())
                        .applyUrl(job.url())
                        .employmentType(null)
                        .publishedAt(null)
                        .provider("Arbeitnow")
                        .providerJobId(job.slug())
                        .build())
                .toList();
        return JobSearchResult.success(jobsList);
    }

    @Override
    public boolean health() {
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

    @Override
    public String getProviderName() {
        return "Arbeitnow";
    }
}
