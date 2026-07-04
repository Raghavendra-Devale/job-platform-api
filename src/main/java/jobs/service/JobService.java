package jobs.service;

import ai.dto.JobDocument;
import jobs.dto.ExternalJob;
import jobs.mapper.JobNormalizer;
import jobs.provider.JobProvider;
import jobs.provider.JobProviderFactory;
import jobs.provider.JobSearchResult;
import org.springframework.stereotype.Service;
import recommendation.model.JobSearchCriteria;

import java.util.ArrayList;
import java.util.List;

@Service("newJobService")
public class JobService {

    private final JobProviderFactory providerFactory;
    private final JobNormalizer normalizer;

    public JobService(JobProviderFactory providerFactory, JobNormalizer normalizer) {
        this.providerFactory = providerFactory;
        this.normalizer = normalizer;
    }

    public List<JobDocument> searchJobs(JobSearchCriteria criteria) {
        List<JobProvider> providers = providerFactory.getProviders();
        List<ExternalJob> aggregatedJobs = new ArrayList<>();

        for (JobProvider provider : providers) {
            try {
                JobSearchResult result = provider.searchJobs(criteria);
                if (result != null && result.success() && result.jobs() != null) {
                    aggregatedJobs.addAll(result.jobs());
                }
            } catch (Exception e) {
                // Ignore failed providers as per workflow requirements
            }
        }

        return aggregatedJobs.stream()
                .map(normalizer::normalize)
                .toList();
    }
}
