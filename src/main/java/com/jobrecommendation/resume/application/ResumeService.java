package com.jobrecommendation.resume.application;

import com.jobrecommendation.resume.domain.AiProcessingStatus;
import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.resume.domain.repository.ResumeRepository;
import com.jobrecommendation.user.application.ActivityService;
import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.applications.domain.repository.JobApplicationRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeValidator validator;
    private final ResumeIntelligenceService resumeIntelligenceService;
    private final ActivityService activityService;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional
    public ResumeEntity uploadResume(UserEntity user, MultipartFile file) throws IOException {
        log.info("Uploading resume for user {}", user.getId());
        validator.validateUpload(user, file);

        // Deactivate all existing active resumes
        List<ResumeEntity> existingResumes = resumeRepository.findByUserOrderByUpdatedAtDesc(user);
        for (ResumeEntity r : existingResumes) {
            if (r.isActive()) {
                r.setActive(false);
                resumeRepository.save(r);
            }
        }

        ResumeEntity resume = ResumeEntity.builder()
                .user(user)
                .resumeName(file.getOriginalFilename())
                .resumeData(file.getBytes())
                .isActive(true)
                .aiProcessingStatus(AiProcessingStatus.PROCESSING)
                .build();

        resume = resumeRepository.save(resume);

        activityService.log(user, "RESUME_UPLOADED", "Uploaded resume '" + resume.getResumeName() + "'");

        try {
            resumeIntelligenceService.processResumeIntelligence(resume);
        } catch (Exception e) {
            log.error("AI processing failed for resume {}", resume.getId(), e);
        }

        return resumeRepository.findById(resume.getId()).orElse(resume);
    }

    @Transactional
    public ResumeEntity renameResume(Long id, UserEntity user, String newName) {
        log.info("Renaming resume {} for user {}", id, user.getId());
        ResumeEntity resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        String originalName = resume.getResumeName();
        String nameToSet = newName.trim();

        if (nameToSet.isEmpty()) {
            throw new IllegalArgumentException("Resume name cannot be empty");
        }

        String originalExt = "";
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot != -1) {
            originalExt = originalName.substring(lastDot);
        }

        if (!originalExt.isEmpty() && !nameToSet.toLowerCase().endsWith(originalExt.toLowerCase())) {
            nameToSet = nameToSet + originalExt;
        }

        resume.setResumeName(nameToSet);
        return resumeRepository.save(resume);
    }

    @Transactional
    public void activateResume(Long id, UserEntity user) {
        log.info("Activating resume {} for user {}", id, user.getId());
        ResumeEntity resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (resume.isActive()) {
            return;
        }

        List<ResumeEntity> resumes = resumeRepository.findByUserOrderByUpdatedAtDesc(user);
        for (ResumeEntity r : resumes) {
            if (r.isActive()) {
                r.setActive(false);
                resumeRepository.save(r);
            }
        }

        resume.setActive(true);
        resumeRepository.save(resume);

        activityService.log(user, "RESUME_ACTIVATED", "Activated resume '" + resume.getResumeName() + "'");
    }

    @Transactional
    public void deleteResume(Long id, UserEntity user) {
        log.info("Deleting resume {} for user {}", id, user.getId());
        ResumeEntity resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        if (resume.isActive()) {
            throw new IllegalArgumentException("Active resume cannot be deleted. Please activate another resume first.");
        }

        // Nullify reference in job applications to prevent foreign key violation
        jobApplicationRepository.nullifyResumeAssociation(resume);

        resumeRepository.delete(resume);
    }

    @Transactional
    public ResumeEntity reprocessActiveResume(UserEntity user) {
        log.info("Reprocessing active resume for user {}", user.getId());
        Optional<ResumeEntity> activeResumeOpt = getActiveResume(user);

        if (activeResumeOpt.isEmpty()) {
            throw new IllegalArgumentException("No active resume found to reprocess");
        }

        ResumeEntity resume = activeResumeOpt.get();

        // Reset status to PROCESSING before retry
        resume.setAiProcessingStatus(AiProcessingStatus.PROCESSING);
        resume = resumeRepository.save(resume);
        log.info("Reset resume {} status to PROCESSING for reprocessing", resume.getId());

        try {
            resumeIntelligenceService.processResumeIntelligence(resume);
        } catch (Exception e) {
            log.error("AI reprocessing failed for resume {}", resume.getId(), e);
        }

        return resumeRepository.findById(resume.getId()).orElse(resume);
    }

    @Transactional
    public Optional<ResumeEntity> getActiveResume(UserEntity user) {
        Optional<ResumeEntity> active = resumeRepository.findByUserAndIsActiveTrue(user);
        if (active.isEmpty()) {
            List<ResumeEntity> resumes = resumeRepository.findByUserOrderByUpdatedAtDesc(user);
            if (!resumes.isEmpty()) {
                ResumeEntity first = resumes.get(0);
                first.setActive(true);
                resumeRepository.save(first);
                return Optional.of(first);
            }
        }
        return active;
    }
}
