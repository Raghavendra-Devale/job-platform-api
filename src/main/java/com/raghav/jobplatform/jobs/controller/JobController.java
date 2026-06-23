package com.raghav.jobplatform.jobs.controller;

import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.dto.JobSearchRequest;
import com.raghav.jobplatform.jobs.service.JobService;
import com.raghav.jobplatform.jobs.service.JobSyncService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {

    private final JobService jobService;
    private final JobSyncService jobSyncService;

    public JobController(
            JobService jobService,
            JobSyncService jobSyncService) {
        this.jobService = jobService;
        this.jobSyncService = jobSyncService;
    }

    @GetMapping("/api/jobs")
    public Page<JobListResponse> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return jobService.getJobs(page, size);
    }

    @GetMapping("/api/jobs/search")
    public Page<JobListResponse> searchJobs(
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
    public String syncJobs() {
        jobSyncService.syncJobs();
        System.out.println("syncJobs Synced");
        return "Jobs Synced";
    }
}