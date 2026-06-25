package com.raghav.jobplatform.jobs.controller;

import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.dto.JobSearchRequest;
import com.raghav.jobplatform.jobs.dto.JobSearchResponse;
import com.raghav.jobplatform.jobs.dto.SyncSummaryResponse;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import com.raghav.jobplatform.jobs.service.JobService;
import com.raghav.jobplatform.jobs.service.JobSyncService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class JobController {

    private final JobService jobService;
    private final JobSyncService jobSyncService;
    private final List<JobProvider> jobProviders;

    public JobController(
            JobService jobService,
            JobSyncService jobSyncService,
            List<JobProvider> jobProviders) {
        this.jobService = jobService;
        this.jobSyncService = jobSyncService;
        this.jobProviders = jobProviders;
    }

    @GetMapping("/api/jobs")
    public Page<JobListResponse> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return jobService.getJobs(page, size);
    }

    @GetMapping("/api/jobs/search")
    public JobSearchResponse searchJobs(
            JobSearchRequest request
    ) {
        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 10;
        return jobService.searchJobs(request, page, size);
    }

    @GetMapping("/api/jobs/{id}")
    public JobResponse getJobById(@PathVariable Long id) {
        return jobService.getJobById(id);
    }

    @PostMapping("/api/jobs/sync")
    public SyncSummaryResponse syncJobs() {
        return jobSyncService.syncJobs();
    }

    @GetMapping("/api/providers/health")
    public Map<String, String> getProvidersHealth() {
        Map<String, String> healthMap = new HashMap<>();
        for (JobProvider provider : jobProviders) {
            healthMap.put(provider.getProviderName(), provider.isHealthy() ? "UP" : "DOWN");
        }
        return healthMap;
    }

    @GetMapping("/api/providers/search")
    public List<JobResponse> searchExternalJobs(
            @RequestParam String keyword
    ) {
        return jobService.searchJobs(keyword);
    }
}