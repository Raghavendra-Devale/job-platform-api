package com.raghav.jobplatform.user.controller;

import com.raghav.jobplatform.auth.dto.*;
import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import com.raghav.jobplatform.user.entity.RecentViewEntity;
import com.raghav.jobplatform.user.entity.SavedJobEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.entity.ResumeEntity;
import com.raghav.jobplatform.user.entity.UserProfileEntity;
import com.raghav.jobplatform.user.entity.UserPreferencesEntity;
import com.raghav.jobplatform.user.repository.RecentViewRepository;
import com.raghav.jobplatform.user.repository.SavedJobRepository;
import com.raghav.jobplatform.user.repository.UserRepository;
import com.raghav.jobplatform.user.repository.ResumeRepository;
import com.raghav.jobplatform.user.repository.UserProfileRepository;
import com.raghav.jobplatform.user.repository.UserPreferencesRepository;
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
    private final ResumeRepository resumeRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.raghav.jobplatform.user.service.ActivityService activityService;

    public UserController(
            UserRepository userRepository,
            JobRepository jobRepository,
            SavedJobRepository savedJobRepository,
            RecentViewRepository recentViewRepository,
            ResumeRepository resumeRepository,
            UserProfileRepository userProfileRepository,
            UserPreferencesRepository userPreferencesRepository,
            PasswordEncoder passwordEncoder,
            com.raghav.jobplatform.user.service.ActivityService activityService
    ) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.savedJobRepository = savedJobRepository;
        this.recentViewRepository = recentViewRepository;
        this.resumeRepository = resumeRepository;
        this.userProfileRepository = userProfileRepository;
        this.userPreferencesRepository = userPreferencesRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityService = activityService;
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
        String activeResumeName = resumeRepository.findByUserAndIsActiveTrue(user)
                .map(ResumeEntity::getResumeName)
                .orElse(null);

        // Fetch or lazily create User Profile Entity
        UserProfileEntity profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> userProfileRepository.save(
                        UserProfileEntity.builder()
                                .user(user)
                                .name(user.getName())
                                .experience(user.getExperience())
                                .currentRole(user.getCurrentRole())
                                .bio(user.getBio())
                                .linkedin(user.getLinkedin())
                                .github(user.getGithub())
                                .portfolio(user.getPortfolio())
                                .build()
                ));

        // Fetch or lazily create User Preferences Entity
        UserPreferencesEntity preferences = userPreferencesRepository.findByUser(user)
                .orElseGet(() -> userPreferencesRepository.save(
                        UserPreferencesEntity.builder()
                                .user(user)
                                .preferredRoles(user.getPreferredRoles())
                                .preferredLocations(user.getPreferredLocations())
                                .remoteOnly(user.getRemoteOnly())
                                .salaryRange(user.getSalaryRange())
                                .jobTypes(user.getJobTypes())
                                .build()
                ));

        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                profile.getName() != null ? profile.getName() : user.getName(),
                user.getEmail(),
                user.getRole(),
                activeResumeName,
                user.getWorkPreference(),
                user.getAlertEnabled(),
                user.getCreatedAt(),
                profile.getExperience(),
                profile.getCurrentRole(),
                profile.getBio(),
                profile.getLinkedin(),
                profile.getGithub(),
                profile.getPortfolio(),
                profile.getPhone(),
                profile.getLocation(),
                preferences.getPreferredRoles(),
                preferences.getPreferredLocations(),
                preferences.getRemoteOnly(),
                preferences.getSalaryRange(),
                preferences.getJobTypes()
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

        // Save to dedicated Profile Entity
        UserProfileEntity profile = userProfileRepository.findByUser(user)
                .orElseGet(() -> UserProfileEntity.builder().user(user).build());
        profile.setName(request.name());
        profile.setExperience(request.experience());
        profile.setCurrentRole(request.currentRole());
        profile.setBio(request.bio());
        profile.setLinkedin(request.linkedin());
        profile.setGithub(request.github());
        profile.setPortfolio(request.portfolio());
        profile.setPhone(request.phone());
        profile.setLocation(request.location());
        userProfileRepository.save(profile);

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

        // Save to dedicated Preferences Entity
        UserPreferencesEntity preferences = userPreferencesRepository.findByUser(user)
                .orElseGet(() -> UserPreferencesEntity.builder().user(user).build());
        preferences.setPreferredRoles(request.preferredRoles());
        preferences.setPreferredLocations(request.preferredLocations());
        preferences.setRemoteOnly(request.remoteOnly());
        preferences.setSalaryRange(request.salaryRange());
        preferences.setJobTypes(request.jobTypes());
        userPreferencesRepository.save(preferences);

        return ResponseEntity.ok(Map.of("message", "Preferences updated successfully"));
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
        activityService.log(user, "JOB_SAVED", "Saved job '" + job.getTitle() + " at " + job.getCompany() + "'");
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
                job.getCreatedAt(),
                job.getSalary(),
                job.getJobType(),
                job.getApplyUrl()
        );
    }
}
