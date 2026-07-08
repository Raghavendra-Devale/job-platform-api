package com.jobrecommendation.dashboard.api;

import com.jobrecommendation.applications.api.dto.CreateJobApplicationRequest;
import com.jobrecommendation.applications.api.dto.JobApplicationResponse;
import com.jobrecommendation.applications.api.dto.UpdateJobApplicationStatusRequest;
import com.jobrecommendation.applications.application.ApplicationService;
import com.jobrecommendation.applications.domain.JobApplicationEntity;
import com.jobrecommendation.dashboard.api.dto.DashboardSummaryResponse;
import com.jobrecommendation.jobs.application.JobService;
import com.jobrecommendation.jobs.domain.Job;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.resume.application.ResumeService;
import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.user.api.dto.ActivityLogResponse;
import com.jobrecommendation.user.application.ActivityService;
import com.jobrecommendation.user.application.UserService;
import com.jobrecommendation.user.domain.UserEntity;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ResumeService resumeService;
    private final ApplicationService applicationService;
    private final JobService jobService;
    private final ActivityService activityService;

    private UserEntity getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        UserEntity user = getAuthenticatedUser();

        // 1. Active Resume Name
        String activeResumeName = resumeService.getActiveResumeForUser(user)
                .map(ResumeEntity::getResumeName)
                .orElse(null);

        // 2. Counts
        long totalResumes = resumeService.getResumeCountForUser(user);
        long savedJobs = applicationService.getSavedJobsCountForUser(user);
        long applications = applicationService.getJobApplicationsCountForUser(user);
        long interviews = applicationService.getJobApplicationsCountForUserAndStatus(user, "INTERVIEW");
        long offers = applicationService.getJobApplicationsCountForUserAndStatus(user, "OFFER");

        // 3. Recent Activities (limit to 10)
        List<ActivityLogResponse> recentActivities = activityService
                .getRecentActivities(user, PageRequest.of(0, 10))
                .stream()
                .map(log -> new ActivityLogResponse(
                        log.getId(),
                        log.getActivityType(),
                        log.getDescription(),
                        log.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new DashboardSummaryResponse(
                activeResumeName,
                totalResumes,
                savedJobs,
                applications,
                interviews,
                offers,
                recentActivities
        ));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<JobApplicationResponse>> getApplications() {
        UserEntity user = getAuthenticatedUser();
        List<JobApplicationResponse> applications = applicationService
                .getJobApplicationsForUser(user)
                .stream()
                .map(app -> new JobApplicationResponse(
                        app.getId(),
                        app.getJob().getId(),
                        app.getJob().getTitle(),
                        app.getJob().getCompany(),
                        app.getJob().getLocation(),
                        app.getStatus(),
                        app.getAppliedAt(),
                        app.getUpdatedAt(),
                        app.getResume() != null ? app.getResume().getResumeName() : null
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/applications")
    public ResponseEntity<JobApplicationResponse> createApplication(@Valid @RequestBody CreateJobApplicationRequest request) {
        UserEntity user = getAuthenticatedUser();
        JobEntity job = jobService.findJobEntityById(request.jobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        // Get the current active resume
        ResumeEntity activeResume = resumeService.getActiveResumeForUser(user).orElse(null);

        // If application already exists, just return it
        JobApplicationEntity application = applicationService.findJobApplicationByUserAndJob(user, job)
                .orElseGet(() -> {
                    JobApplicationEntity newApp = JobApplicationEntity.builder()
                            .user(user)
                            .job(job)
                            .resume(activeResume)
                            .status("APPLIED")
                            .appliedAt(LocalDateTime.now())
                            .build();

                    // Log the activity
                    activityService.log(
                            user,
                            "APPLICATION_SUBMITTED",
                            "Applied to " + job.getTitle() + " at " + job.getCompany()
                    );

                    return applicationService.saveJobApplication(newApp);
                });

        return ResponseEntity.status(HttpStatus.CREATED).body(new JobApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getCompany(),
                application.getJob().getLocation(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt(),
                application.getResume() != null ? application.getResume().getResumeName() : null
        ));
    }

    @PutMapping("/applications/{id}/status")
    public ResponseEntity<?> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobApplicationStatusRequest request) {
        UserEntity user = getAuthenticatedUser();
        JobApplicationEntity application = applicationService.findJobApplicationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (!application.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized access"));
        }

        String newStatus = request.status().toUpperCase(); // APPLIED, SCREENING, INTERVIEW, OFFER, REJECTED
        java.util.Set<String> VALID_STATUSES = java.util.Set.of("APPLIED", "SCREENING", "INTERVIEW", "OFFER", "REJECTED");
        if (!VALID_STATUSES.contains(newStatus)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + newStatus));
        }

        application.setStatus(newStatus);
        applicationService.saveJobApplication(application);

        // Log status change activity
        activityService.log(
                user,
                "APPLICATION_UPDATED",
                "Moved " + application.getJob().getTitle() + " to " + newStatus
        );

        return ResponseEntity.ok(Map.of("message", "Application status updated successfully"));
    }

    @DeleteMapping("/applications/{id}")
    public ResponseEntity<?> deleteApplication(@PathVariable Long id) {
        UserEntity user = getAuthenticatedUser();
        JobApplicationEntity application = applicationService.findJobApplicationById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (!application.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized access"));
        }

        applicationService.deleteJobApplication(application);
        return ResponseEntity.ok(Map.of("message", "Application tracker deleted successfully"));
    }
}
