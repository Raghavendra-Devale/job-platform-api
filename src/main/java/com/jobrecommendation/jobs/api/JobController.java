package com.jobrecommendation.jobs.api;

import com.jobrecommendation.jobs.api.dto.JobListResponse;
import com.jobrecommendation.jobs.api.dto.JobResponse;
import com.jobrecommendation.jobs.api.dto.JobSearchRequest;
import com.jobrecommendation.jobs.api.dto.JobSearchResponse;
import com.jobrecommendation.jobs.api.dto.SyncSummaryResponse;
import com.jobrecommendation.jobs.application.JobService;
import com.jobrecommendation.jobs.application.JobSyncService;
import com.jobrecommendation.jobs.infrastructure.provider.JobProvider;
import com.jobrecommendation.user.application.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class JobController {

    private final JobService jobService;
    private final JobSyncService jobSyncService;
    private final List<JobProvider> jobProviders;
    private final UserService userService;

    public JobController(
            JobService jobService,
            JobSyncService jobSyncService,
            List<JobProvider> jobProviders,
            UserService userService) {
        this.jobService = jobService;
        this.jobSyncService = jobSyncService;
        this.jobProviders = jobProviders;
        this.userService = userService;
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
                userService.findUserByEmail(email).ifPresent(user -> {
                    jobService.findJobEntityById(jobId).ifPresent(job -> {
                        userService.recordRecentView(user, job);
                    });
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to automatically record recent view: " + e.getMessage());
        }
    }
}
