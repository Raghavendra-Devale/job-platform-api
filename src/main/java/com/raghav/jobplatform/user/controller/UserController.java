package com.raghav.jobplatform.user.controller;

import com.raghav.jobplatform.auth.dto.*;
import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import com.raghav.jobplatform.user.entity.RecentViewEntity;
import com.raghav.jobplatform.user.entity.SavedJobEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.RecentViewRepository;
import com.raghav.jobplatform.user.repository.SavedJobRepository;
import com.raghav.jobplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final RecentViewRepository recentViewRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            JobRepository jobRepository,
            SavedJobRepository savedJobRepository,
            RecentViewRepository recentViewRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.savedJobRepository = savedJobRepository;
        this.recentViewRepository = recentViewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @GetMapping("/users/profile")
    public ResponseEntity<UserResponse> getProfile() {
        UserEntity user = getAuthenticatedUser();
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getResumeFileName(),
                user.getWorkPreference(),
                user.getAlertEnabled(),
                user.getCreatedAt()
        ));
    }

    @PutMapping("/users/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserEntity user = getAuthenticatedUser();

        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is already in use"));
        }

        user.setName(request.name());
        user.setEmail(request.email());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    @PostMapping("/users/profile/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        UserEntity user = getAuthenticatedUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Incorrect current password"));
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @PutMapping("/users/profile/preferences")
    public ResponseEntity<?> updatePreferences(@Valid @RequestBody UpdatePreferencesRequest request) {
        UserEntity user = getAuthenticatedUser();
        user.setWorkPreference(request.workPreference().toUpperCase());
        user.setAlertEnabled(request.alertEnabled());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Preferences updated successfully"));
    }

    @DeleteMapping("/users/profile/resume")
    public ResponseEntity<?> deleteResume() {
        UserEntity user = getAuthenticatedUser();
        user.setResume(null);
        user.setResumeFileName(null);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
    }

    @PostMapping(value = "/users/profile/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        UserEntity user = getAuthenticatedUser();

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

        user.setResume(file.getBytes());
        user.setResumeFileName(file.getOriginalFilename());
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Resume uploaded successfully",
                "fileName", file.getOriginalFilename()
        ));
    }

    @GetMapping("/users/profile/resume/download")
    public ResponseEntity<byte[]> downloadResume() {
        UserEntity user = getAuthenticatedUser();

        if (user.getResume() == null) {
            return ResponseEntity.notFound().build();
        }

        // Detect correct mime type
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String fileName = user.getResumeFileName();
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + user.getResumeFileName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(user.getResume());
    }

    // ── Saved Jobs ───────────────────────────────────────────────────────

    @PostMapping("/jobs/{jobId}/save")
    public ResponseEntity<?> saveJob(@PathVariable Long jobId) {
        UserEntity user = getAuthenticatedUser();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        if (savedJobRepository.existsByUserAndJob(user, job)) {
            return ResponseEntity.ok(Map.of("message", "Job already saved"));
        }

        SavedJobEntity savedJob = SavedJobEntity.builder()
                .user(user)
                .job(job)
                .build();

        savedJobRepository.save(savedJob);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Job saved successfully"));
    }

    @DeleteMapping("/jobs/{jobId}/save")
    public ResponseEntity<?> unsaveJob(@PathVariable Long jobId) {
        UserEntity user = getAuthenticatedUser();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        savedJobRepository.findByUserAndJob(user, job)
                .ifPresent(savedJobRepository::delete);

        return ResponseEntity.ok(Map.of("message", "Job unsaved successfully"));
    }

    @GetMapping("/jobs/saved")
    public ResponseEntity<List<JobListResponse>> getSavedJobs() {
        UserEntity user = getAuthenticatedUser();
        List<SavedJobEntity> saved = savedJobRepository.findByUserOrderBySavedAtDesc(user);

        List<JobListResponse> responses = saved.stream()
                .map(s -> mapToJobListResponse(s.getJob()))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/jobs/saved/ids")
    public ResponseEntity<List<Long>> getSavedJobIds() {
        UserEntity user = getAuthenticatedUser();
        List<SavedJobEntity> saved = savedJobRepository.findByUserOrderBySavedAtDesc(user);
        List<Long> ids = saved.stream().map(s -> s.getJob().getId()).toList();
        return ResponseEntity.ok(ids);
    }

    // ── Recently Viewed Jobs ─────────────────────────────────────────────

    @PostMapping("/jobs/{jobId}/view")
    public ResponseEntity<?> viewJob(@PathVariable Long jobId) {
        UserEntity user = getAuthenticatedUser();

        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));

        RecentViewEntity recent = recentViewRepository.findByUserAndJob(user, job)
                .orElseGet(() -> RecentViewEntity.builder().user(user).job(job).build());

        // Save (triggers the @PreUpdate/@PrePersist)
        recentViewRepository.save(recent);

        return ResponseEntity.ok(Map.of("message", "Job view recorded"));
    }

    @GetMapping("/jobs/recent")
    public ResponseEntity<List<JobListResponse>> getRecentlyViewedJobs() {
        UserEntity user = getAuthenticatedUser();
        List<RecentViewEntity> recent = recentViewRepository.findTop10ByUserOrderByViewedAtDesc(user);

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
