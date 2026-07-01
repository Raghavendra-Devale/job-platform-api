package com.raghav.jobplatform.jobs.controller;

import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.dto.JobSearchRequest;
import com.raghav.jobplatform.jobs.dto.JobSearchResponse;
import com.raghav.jobplatform.jobs.dto.SyncSummaryResponse;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import com.raghav.jobplatform.jobs.service.JobService;
import com.raghav.jobplatform.jobs.service.JobSyncService;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.entity.RecentViewEntity;
import com.raghav.jobplatform.user.repository.UserRepository;
import com.raghav.jobplatform.user.repository.RecentViewRepository;
import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class JobController {

    private final JobService jobService;
    private final JobSyncService jobSyncService;
    private final List<JobProvider> jobProviders;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final RecentViewRepository recentViewRepository;

    public JobController(
            JobService jobService,
            JobSyncService jobSyncService,
            List<JobProvider> jobProviders,
            UserRepository userRepository,
            JobRepository jobRepository,
            RecentViewRepository recentViewRepository) {
        this.jobService = jobService;
        this.jobSyncService = jobSyncService;
        this.jobProviders = jobProviders;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.recentViewRepository = recentViewRepository;
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
        recordRecentViewIfAuthenticated(id);
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

    private void recordRecentViewIfAuthenticated(Long jobId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String email = authentication.getName();
                userRepository.findByEmail(email).ifPresent(user -> {
                    jobRepository.findById(jobId).ifPresent(job -> {
                        RecentViewEntity recent = recentViewRepository.findByUserAndJob(user, job)
                                .orElseGet(() -> RecentViewEntity.builder().user(user).job(job).build());
                        recentViewRepository.save(recent);
                    });
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to automatically record recent view: " + e.getMessage());
        }
    }
}