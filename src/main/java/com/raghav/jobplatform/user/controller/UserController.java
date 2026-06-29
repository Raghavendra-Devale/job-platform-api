package com.raghav.jobplatform.user.controller;

import com.raghav.jobplatform.auth.dto.UserResponse;
import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import com.raghav.jobplatform.user.entity.RecentViewEntity;
import com.raghav.jobplatform.user.entity.SavedJobEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.RecentViewRepository;
import com.raghav.jobplatform.user.repository.SavedJobRepository;
import com.raghav.jobplatform.user.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final RecentViewRepository recentViewRepository;

    public UserController(
            UserRepository userRepository,
            JobRepository jobRepository,
            SavedJobRepository savedJobRepository,
            RecentViewRepository recentViewRepository
    ) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.savedJobRepository = savedJobRepository;
        this.recentViewRepository = recentViewRepository;
    }

    @GetMapping("/users/profile")
    public ResponseEntity<UserResponse> getProfile(@RequestAttribute("currentUser") UserEntity user) {
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getResumeFileName(),
                user.getCreatedAt()
        ));
    }

    @PostMapping(value = "/users/profile/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResume(
            @RequestAttribute("currentUser") UserEntity user,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // Limit size to 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("error", "File size exceeds limit of 5MB"));
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") &&
                !contentType.equals("application/msword") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only PDF and Word documents are allowed"));
        }

        // Fetch fresh entity to merge changes properly
        UserEntity freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        freshUser.setResume(file.getBytes());
        freshUser.setResumeFileName(file.getOriginalFilename());
        userRepository.save(freshUser);

        return ResponseEntity.ok(Map.of(
                "message", "Resume uploaded successfully",
                "fileName", file.getOriginalFilename()
        ));
    }

    @GetMapping("/users/profile/resume/download")
    public ResponseEntity<byte[]> downloadResume(@RequestAttribute("currentUser") UserEntity user) {
        UserEntity freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (freshUser.getResume() == null) {
            return ResponseEntity.notFound().build();
        }

        // Detect correct mime type
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String fileName = freshUser.getResumeFileName();
        if (fileName != null) {
            if (fileName.toLowerCase().endsWith(".pdf")) {
                contentType = MediaType.APPLICATION_PDF_VALUE;
            } else if (fileName.toLowerCase().endsWith(".doc")) {
                contentType = "application/msword";
            } else if (fileName.toLowerCase().endsWith(".docx")) {
                contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + freshUser.getResumeFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(freshUser.getResume());
    }

    // ── Saved Jobs ───────────────────────────────────────────────────────

    @PostMapping("/jobs/{jobId}/save")
    public ResponseEntity<?> saveJob(
            @RequestAttribute("currentUser") UserEntity user,
            @PathVariable Long jobId
    ) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        UserEntity freshUser = userRepository.findById(user.getId()).orElseThrow();

        if (savedJobRepository.existsByUserAndJob(freshUser, job)) {
            return ResponseEntity.ok(Map.of("message", "Job already saved"));
        }

        SavedJobEntity savedJob = SavedJobEntity.builder()
                .user(freshUser)
                .job(job)
                .build();

        savedJobRepository.save(savedJob);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Job saved successfully"));
    }

    @DeleteMapping("/jobs/{jobId}/save")
    public ResponseEntity<?> unsaveJob(
            @RequestAttribute("currentUser") UserEntity user,
            @PathVariable Long jobId
    ) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        UserEntity freshUser = userRepository.findById(user.getId()).orElseThrow();

        savedJobRepository.findByUserAndJob(freshUser, job)
                .ifPresent(savedJobRepository::delete);

        return ResponseEntity.ok(Map.of("message", "Job unsaved successfully"));
    }

    @GetMapping("/jobs/saved")
    public ResponseEntity<List<JobListResponse>> getSavedJobs(@RequestAttribute("currentUser") UserEntity user) {
        UserEntity freshUser = userRepository.findById(user.getId()).orElseThrow();
        List<SavedJobEntity> saved = savedJobRepository.findByUserOrderBySavedAtDesc(freshUser);

        List<JobListResponse> responses = saved.stream()
                .map(s -> mapToJobListResponse(s.getJob()))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/jobs/saved/ids")
    public ResponseEntity<List<Long>> getSavedJobIds(@RequestAttribute("currentUser") UserEntity user) {
        UserEntity freshUser = userRepository.findById(user.getId()).orElseThrow();
        List<SavedJobEntity> saved = savedJobRepository.findByUserOrderBySavedAtDesc(freshUser);
        List<Long> ids = saved.stream().map(s -> s.getJob().getId()).toList();
        return ResponseEntity.ok(ids);
    }

    // ── Recently Viewed Jobs ─────────────────────────────────────────────

    @PostMapping("/jobs/{jobId}/view")
    public ResponseEntity<?> viewJob(
            @RequestAttribute("currentUser") UserEntity user,
            @PathVariable Long jobId
    ) {
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        UserEntity freshUser = userRepository.findById(user.getId()).orElseThrow();

        RecentViewEntity recent = recentViewRepository.findByUserAndJob(freshUser, job)
                .orElseGet(() -> RecentViewEntity.builder().user(freshUser).job(job).build());

        // Save (this triggers the @PrePersist / @PreUpdate to set viewedAt to current timestamp)
        recentViewRepository.save(recent);

        return ResponseEntity.ok(Map.of("message", "Job view recorded"));
    }

    @GetMapping("/jobs/recent")
    public ResponseEntity<List<JobListResponse>> getRecentlyViewedJobs(@RequestAttribute("currentUser") UserEntity user) {
        UserEntity freshUser = userRepository.findById(user.getId()).orElseThrow();
        List<RecentViewEntity> recent = recentViewRepository.findTop10ByUserOrderByViewedAtDesc(freshUser);

        List<JobListResponse> responses = recent.stream()
                .map(r -> mapToJobListResponse(r.getJob()))
                .toList();

        return ResponseEntity.ok(responses);
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private JobListResponse mapToJobListResponse(JobEntity job) {
        return new JobListResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getSource(),
                job.getRemote(),
                job.getTags(),
                job.getCreatedAt()
        );
    }
}
