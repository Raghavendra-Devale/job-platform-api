package com.jobrecommendation.jobs.infrastructure.provider;

import com.jobrecommendation.jobs.api.dto.JobResponse;
import java.util.List;

public interface JobProvider {
    List<JobResponse> searchJobs(String keyword);

    List<JobResponse> fetchAllJobs();

    String getProviderName();

    boolean isHealthy();
}
