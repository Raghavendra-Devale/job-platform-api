package com.raghav.jobplatform.jobs.provider;

import com.raghav.jobplatform.jobs.dto.JobResponse;

import java.util.List;

public interface JobProvider {
    List<JobResponse> searchJobs(String keyword);

    String getProviderName();
}
