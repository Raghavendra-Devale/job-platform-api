package jobs.provider;

import jobs.dto.ExternalJob;
import recommendation.model.JobSearchCriteria;
import java.util.List;

public interface JobProvider {
    JobSearchResult searchJobs(JobSearchCriteria criteria);
    boolean health();
    String getProviderName();
}
