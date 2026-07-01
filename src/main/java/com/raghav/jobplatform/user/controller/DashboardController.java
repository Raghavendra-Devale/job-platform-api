package com.raghav.jobplatform.user.controller;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import com.raghav.jobplatform.user.dto.*;
import com.raghav.jobplatform.user.entity.*;
import com.raghav.jobplatform.user.repository.*;
import com.raghav.jobplatform.user.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ActivityService activityService;

    private UserEntity getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        UserEntity user = getAuthenticatedUser();

        // 1. Active Resume Name
        String activeResumeName = resumeRepository.findByUserAndIsActiveTrue(user)
                .map(ResumeEntity::getResumeName)
                .orElse(null);

        // 2. Counts
        long totalResumes = resumeRepository.countByUser(user);
        long savedJobs = savedJobRepository.countByUser(user);
        long applications = jobApplicationRepository.countByUser(user);
        long interviews = jobApplicationRepository.countByUserAndStatus(user, "INTERVIEW");
        long offers = jobApplicationRepository.countByUserAndStatus(user, "OFFER");

        // 3. Recent Activities (limit to 10)
        List<ActivityLogResponse> recentActivities = activityLogRepository
                .findByUser(user, PageRequest.of(0, 10))
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
        List<JobApplicationResponse> applications = jobApplicationRepository
                .findByUserOrderByAppliedAtDesc(user)
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
        JobEntity job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        // Get the current active resume
        ResumeEntity activeResume = resumeRepository.findByUserAndIsActiveTrue(user).orElse(null);

        // If application already exists, just return it
        JobApplicationEntity application = jobApplicationRepository.findByUserAndJob(user, job)
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

                    return jobApplicationRepository.save(newApp);
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
        JobApplicationEntity application = jobApplicationRepository.findById(id)
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
        jobApplicationRepository.save(application);

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
        JobApplicationEntity application = jobApplicationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found"));

        if (!application.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Unauthorized access"));
        }

        jobApplicationRepository.delete(application);
        return ResponseEntity.ok(Map.of("message", "Application tracker deleted successfully"));
    }
}
