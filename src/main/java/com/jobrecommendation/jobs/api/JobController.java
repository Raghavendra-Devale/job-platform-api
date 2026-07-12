package com.jobrecommendation.jobs.api;

import com.jobrecommendation.jobs.api.dto.JobListResponse;
import com.jobrecommendation.jobs.api.dto.JobResponse;
import com.jobrecommendation.jobs.api.dto.JobSearchRequest;
import com.jobrecommendation.jobs.api.dto.JobSearchResponse;
import com.jobrecommendation.jobs.api.dto.SyncSummaryResponse;
import com.jobrecommendation.jobs.application.JobSearchCriteriaFactory;
import com.jobrecommendation.jobs.application.JobService;
import com.jobrecommendation.jobs.application.JobSyncService;
import com.jobrecommendation.jobs.application.SearchCriteria;
import com.jobrecommendation.jobs.infrastructure.provider.JobProvider;
import com.jobrecommendation.user.application.UserService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobSyncService jobSyncService;
    private final List<JobProvider> jobProviders;
    private final UserService userService;
    private final JobSearchCriteriaFactory jobSearchCriteriaFactory;

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
        SearchCriteria criteria = jobSearchCriteriaFactory.build(request);
        return jobService.searchJobs(criteria);
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
