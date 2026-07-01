package com.raghav.jobplatform.user.controller;

import com.raghav.jobplatform.user.dto.RenameResumeRequest;
import com.raghav.jobplatform.user.dto.ResumeResponse;
import com.raghav.jobplatform.user.entity.ResumeEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.ResumeRepository;
import com.raghav.jobplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/profile/resumes")
public class ResumeController {

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;

    public ResumeController(UserRepository userRepository, ResumeRepository resumeRepository) {
        this.userRepository = userRepository;
        this.resumeRepository = resumeRepository;
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
    @Transactional
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) throws IOException {
        UserEntity user = getAuthenticatedUser();

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }

        // Limit user to 4 resumes
        long resumeCount = resumeRepository.countByUser(user);
        if (resumeCount >= 4) {
            return ResponseEntity.badRequest().body(Map.of("error", "You can have a maximum of 4 resumes. Please delete an existing resume to upload a new one."));
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
            return ResponseEntity.badRequest().body(Map.of("error", "Only PDF and Word documents (.doc, .docx) are allowed"));
        }

        // First resume uploaded becomes active by default
        boolean isActive = (resumeCount == 0);

        ResumeEntity resume = ResumeEntity.builder()
                .user(user)
                .resumeName(file.getOriginalFilename())
                .resumeData(file.getBytes())
                .isActive(isActive)
                .build();

        resumeRepository.save(resume);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResumeResponse(
                resume.getId(),
                resume.getResumeName(),
                resume.isActive(),
                resume.getUpdatedAt()
        ));
    }

    @PutMapping("/{id}/activate")
    @Transactional
    public ResponseEntity<?> activateResume(@PathVariable Long id) {
        UserEntity user = getAuthenticatedUser();
        ResumeEntity resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        if (resume.isActive()) {
            return ResponseEntity.ok(Map.of("message", "Resume is already active"));
        }

        // Set all other resumes of this user to inactive
        List<ResumeEntity> resumes = resumeRepository.findByUserOrderByUpdatedAtDesc(user);
        for (ResumeEntity r : resumes) {
            if (r.isActive()) {
                r.setActive(false);
                resumeRepository.save(r);
            }
        }

        // Activate target resume
        resume.setActive(true);
        resumeRepository.save(resume);

        return ResponseEntity.ok(Map.of("message", "Resume activated successfully"));
    }

    @PutMapping("/{id}/rename")
    @Transactional
    public ResponseEntity<?> renameResume(@PathVariable Long id, @Valid @RequestBody RenameResumeRequest request) {
        UserEntity user = getAuthenticatedUser();
        ResumeEntity resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        String originalName = resume.getResumeName();
        String newName = request.resumeName().trim();

        if (newName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Resume name cannot be empty"));
        }

        // Preserve file extension if new name does not specify it
        String originalExt = "";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot != -1) {
            originalExt = originalName.substring(lastDot);
        }

        if (!originalExt.isEmpty() && !newName.toLowerCase().endsWith(originalExt.toLowerCase())) {
            newName = newName + originalExt;
        }

        resume.setResumeName(newName);
        resumeRepository.save(resume);

        return ResponseEntity.ok(new ResumeResponse(
                resume.getId(),
                resume.getResumeName(),
                resume.isActive(),
                resume.getUpdatedAt()
        ));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteResume(@PathVariable Long id) {
        UserEntity user = getAuthenticatedUser();
        ResumeEntity resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));

        boolean wasActive = resume.isActive();
        resumeRepository.delete(resume);

        // If we deleted the active resume, auto-activate the next most recently updated remaining resume
        if (wasActive) {
            List<ResumeEntity> remaining = resumeRepository.findByUserOrderByUpdatedAtDesc(user);
            if (!remaining.isEmpty()) {
                ResumeEntity nextActive = remaining.get(0);
                nextActive.setActive(true);
                resumeRepository.save(nextActive);
            }
        }

        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
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
