package com.jobrecommendation.resume.api;

import com.jobrecommendation.resume.api.dto.RenameResumeRequest;
import com.jobrecommendation.resume.api.dto.ResumeResponse;
import com.jobrecommendation.resume.api.dto.ResumeIntelligenceResponse;
import com.jobrecommendation.resume.application.ResumeService;
import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.resume.domain.ResumeSkillEntity;
import com.jobrecommendation.resume.domain.repository.ResumeRepository;
import com.jobrecommendation.user.application.ActivityService;
import com.jobrecommendation.user.application.UserService;
import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.UserProfileEntity;
import com.jobrecommendation.user.domain.repository.UserProfileRepository;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/profile/resumes")
public class ResumeController {

    private final UserService userService;
    private final ResumeRepository resumeRepository;
    private final ActivityService activityService;
    private final ResumeService resumeService;
    private final UserProfileRepository userProfileRepository;

    public ResumeController(
            UserService userService,
            ResumeRepository resumeRepository,
            ActivityService activityService,
            ResumeService resumeService,
            UserProfileRepository userProfileRepository) {
        this.userService = userService;
        this.resumeRepository = resumeRepository;
        this.activityService = activityService;
        this.resumeService = resumeService;
        this.userProfileRepository = userProfileRepository;
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @GetMapping("/active/intelligence")
    public ResponseEntity<ResumeIntelligenceResponse> getActiveResumeIntelligence() {
        UserEntity user = getAuthenticatedUser();
        Optional<ResumeEntity> activeResumeOpt = resumeService.getActiveResume(user);

        if (activeResumeOpt.isEmpty()) {
            return ResponseEntity.ok(ResumeIntelligenceResponse.builder()
                    .status("NOT_UPLOADED")
                    .skills(List.of())
                    .strengths(List.of())
                    .improvementSuggestions(List.of())
                    .build());
        }

        ResumeEntity resume = activeResumeOpt.get();
        String statusStr = resume.getAiProcessingStatus().name(); // SUCCESS, FAILED, PROCESSING, PENDING

        if ("SUCCESS".equals(statusStr)) {
            statusStr = "READY";
        }

        if (!"READY".equals(statusStr)) {
            return ResponseEntity.ok(ResumeIntelligenceResponse.builder()
                    .status(statusStr)
                    .skills(List.of())
                    .strengths(List.of())
                    .improvementSuggestions(List.of())
                    .build());
        }

        // Gather Profile & Summary Details
        UserProfileEntity profile = userProfileRepository.findByUser(user).orElse(null);
        String experienceStr = "0 years";
        String roleStr = "Software Engineer";

        if (profile != null) {
            if (profile.getExperience() != null) {
                experienceStr = profile.getExperience() + " years";
            }
            if (profile.getCurrentRole() != null && !profile.getCurrentRole().isBlank()) {
                roleStr = profile.getCurrentRole();
            }
        } else if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            roleStr = resume.getExperiences().get(0).getDesignation();
        }

        ResumeIntelligenceResponse.ResumeSummaryDto summaryDto = ResumeIntelligenceResponse.ResumeSummaryDto.builder()
                .candidateName(user.getName())
                .role(roleStr)
                .experience(experienceStr)
                .build();

        // Calculate completeness score
        int score = 75;
        if (resume.getSkills() != null) {
            score += Math.min(resume.getSkills().size() * 3, 15);
        }
        if (resume.getExperiences() != null) {
            score += Math.min(resume.getExperiences().size() * 2, 5);
        }
        score = Math.min(score, 100);

        List<String> skillsList = resume.getSkills().stream()
                .map(ResumeSkillEntity::getName)
                .toList();

        List<String> strengths = List.of(
            "Strong alignment with modern software development standards.",
            "Demonstrated core competence in backend engineering processes."
        );

        List<String> suggestions = List.of(
            "Highlight cloud deployment experience or DevOps toolkits.",
            "Verify formatting conventions match standardized parsing formats."
        );

        return ResponseEntity.ok(ResumeIntelligenceResponse.builder()
                .status("READY")
                .summary(summaryDto)
                .resumeScore(score)
                .skills(skillsList)
                .strengths(strengths)
                .improvementSuggestions(suggestions)
                .build());
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> listResumes() {
        UserEntity user = getAuthenticatedUser();
        List<ResumeEntity> resumes = resumeRepository.findByUserOrderByUpdatedAtDesc(user);
        List<ResumeResponse> response = resumes.stream()
                .map(r -> new ResumeResponse(r.getId(), r.getResumeName(), r.isActive(), r.getUpdatedAt()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        UserEntity user = getAuthenticatedUser();
        try {
            ResumeEntity resume = resumeService.uploadResume(user, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ResumeResponse(
                    resume.getId(),
                    resume.getResumeName(),
                    resume.isActive(),
                    resume.getUpdatedAt()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activateResume(@PathVariable Long id) {
        UserEntity user = getAuthenticatedUser();
        try {
            resumeService.activateResume(id, user);
            return ResponseEntity.ok(Map.of("message", "Resume activated successfully"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/rename")
    public ResponseEntity<?> renameResume(@PathVariable Long id, @Valid @RequestBody RenameResumeRequest request) {
        UserEntity user = getAuthenticatedUser();
        try {
            ResumeEntity resume = resumeService.renameResume(id, user, request.resumeName());
            return ResponseEntity.ok(new ResumeResponse(
                    resume.getId(),
                    resume.getResumeName(),
                    resume.isActive(),
                    resume.getUpdatedAt()
            ));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable Long id) {
        UserEntity user = getAuthenticatedUser();
        try {
            resumeService.deleteResume(id, user);
            return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            }
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) {
        UserEntity user = getAuthenticatedUser();
        ResumeEntity resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        String fileName = resume.getResumeName();
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
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resume.getResumeName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resume.getResumeData());
    }
}
