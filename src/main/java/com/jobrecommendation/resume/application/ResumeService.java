package com.jobrecommendation.resume.application;

import com.jobrecommendation.infrastructure.ai.client.ResumeAiClient;
import com.jobrecommendation.infrastructure.ai.dto.ResumeIntelligenceResponse;
import com.jobrecommendation.infrastructure.ai.dto.ResumeProcessRequest;
import com.jobrecommendation.infrastructure.ai.exception.AIException;
import com.jobrecommendation.resume.domain.AiProcessingStatus;
import com.jobrecommendation.resume.domain.ResumeEducationEntity;
import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.resume.domain.ResumeExperienceEntity;
import com.jobrecommendation.resume.domain.ResumeProjectEntity;
import com.jobrecommendation.resume.domain.ResumeSkillEntity;
import com.jobrecommendation.resume.domain.ResumeSummaryEntity;
import com.jobrecommendation.resume.domain.repository.ResumeEducationRepository;
import com.jobrecommendation.resume.domain.repository.ResumeExperienceRepository;
import com.jobrecommendation.resume.domain.repository.ResumeProjectRepository;
import com.jobrecommendation.resume.domain.repository.ResumeRepository;
import com.jobrecommendation.resume.domain.repository.ResumeSkillRepository;
import com.jobrecommendation.resume.domain.repository.ResumeSummaryRepository;
import com.jobrecommendation.user.application.ActivityService;
import com.jobrecommendation.user.application.UserService;
import com.jobrecommendation.user.domain.UserEntity;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final com.jobrecommendation.user.application.UserService userService;
    private final ActivityService activityService;
    private final ResumeAiClient aiClient;

    private final ResumeSummaryRepository resumeSummaryRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeEducationRepository resumeEducationRepository;
    private final ResumeExperienceRepository resumeExperienceRepository;
    private final ResumeProjectRepository resumeProjectRepository;

    private ResumeService self;

    @Autowired
    public void setSelf(@Lazy ResumeService self) {
        this.self = self;
    }

    @Transactional
    public ResumeEntity uploadResume(UserEntity user, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Limit user to 4 resumes
        long resumeCount = resumeRepository.countByUser(user);
        if (resumeCount >= 4) {
            throw new IllegalArgumentException("You can have a maximum of 4 resumes. Please delete an existing resume to upload a new one.");
        }

        // Limit size to 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds limit of 5MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") &&
                !contentType.equals("application/msword") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
            throw new IllegalArgumentException("Only PDF and Word documents (.doc, .docx) are allowed");
        }

        // First resume uploaded becomes active by default
        boolean isActive = (resumeCount == 0);

        ResumeEntity resume = ResumeEntity.builder()
                .user(user)
                .resumeName(file.getOriginalFilename())
                .resumeData(file.getBytes())
                .isActive(isActive)
                .aiProcessingStatus(AiProcessingStatus.PROCESSING)
                .build();

        resume = resumeRepository.save(resume);

        activityService.log(user, "RESUME_UPLOADED", "Uploaded resume '" + resume.getResumeName() + "'");

        // Invoke AI processing synchronously, in a separate transaction so that
        // any failures inside processResumeIntelligence commit the FAILED status without rolling back the upload.
        try {
            self.processResumeIntelligence(resume);
        } catch (Exception e) {
            log.error("Sync AI processing call failed unexpectedly for resume ID: {}", resume.getId(), e);
        }

        // Reload resume entity to reflect updated status / associations
        return resumeRepository.findById(resume.getId()).orElse(resume);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processResumeIntelligence(ResumeEntity resume) {
        log.info("Starting AI intelligence extraction for resume ID: {}, user ID: {}", resume.getId(), resume.getUser().getId());

        try {
            ResumeProcessRequest request = ResumeProcessRequest.builder()
                    .fileBytes(resume.getResumeData())
                    .filename(resume.getResumeName())
                    .resumeId(resume.getId())
                    .userId(resume.getUser().getId())
                    .build();

            ResumeIntelligenceResponse response = aiClient.processResume(request);

            if (response == null || !response.isSuccess()) {
                throw new AIException("AI Engine returned failure response or empty response payload");
            }

            ResumeIntelligenceResponse.ResumeDetails details = response.getResume();
            if (details != null) {
                // 1. Map and save summary
                if (details.getSummary() != null) {
                    ResumeSummaryEntity summaryEntity = ResumeSummaryEntity.builder()
                            .resume(resume)
                            .summary(details.getSummary())
                            .build();
                    resumeSummaryRepository.save(summaryEntity);
                }

                // 2. Map and save skills
                if (details.getSkills() != null) {
                    for (ResumeIntelligenceResponse.SkillDto skill : details.getSkills()) {
                        if (skill.getName() != null) {
                            ResumeSkillEntity skillEntity = ResumeSkillEntity.builder()
                                    .resume(resume)
                                    .name(skill.getName())
                                    .confidence(skill.getConfidence())
                                    .build();
                            resumeSkillRepository.save(skillEntity);
                        }
                    }
                }

                // 3. Map and save education
                if (details.getEducation() != null) {
                    for (ResumeIntelligenceResponse.EducationDto edu : details.getEducation()) {
                        ResumeEducationEntity eduEntity = ResumeEducationEntity.builder()
                                .resume(resume)
                                .degree(edu.getDegree())
                                .institution(edu.getInstitution())
                                .startDate(edu.getStartDate())
                                .endDate(edu.getEndDate())
                                .build();
                        resumeEducationRepository.save(eduEntity);
                    }
                }

                // 4. Map and save experience
                if (details.getExperience() != null) {
                    for (ResumeIntelligenceResponse.ExperienceDto exp : details.getExperience()) {
                        ResumeExperienceEntity expEntity = ResumeExperienceEntity.builder()
                                .resume(resume)
                                .company(exp.getCompany())
                                .designation(exp.getDesignation())
                                .startDate(exp.getStartDate())
                                .endDate(exp.getEndDate())
                                .responsibilities(exp.getResponsibilities() != null ? exp.getResponsibilities() : List.of())
                                .build();
                        resumeExperienceRepository.save(expEntity);
                    }
                }

                // 5. Map and save projects
                if (details.getProjects() != null) {
                    for (ResumeIntelligenceResponse.ProjectDto proj : details.getProjects()) {
                        ResumeProjectEntity projEntity = ResumeProjectEntity.builder()
                                .resume(resume)
                                .name(proj.getName())
                                .description(proj.getDescription())
                                .technologies(proj.getTechnologies() != null ? proj.getTechnologies() : List.of())
                                .build();
                        resumeProjectRepository.save(projEntity);
                    }
                }
            }

            // Update resume status to SUCCESS
            resume.setAiProcessingStatus(AiProcessingStatus.SUCCESS);
            resumeRepository.save(resume);
            log.info("AI intelligence extraction succeeded for resume ID: {}", resume.getId());

        } catch (Exception e) {
            log.error("AI intelligence extraction failed for resume ID: {}. Marking status as FAILED.", resume.getId(), e);
            
            // Wrap failure in AIException for unified typing before updating status
            AIException aiException = (e instanceof AIException) ? (e instanceof AIException ? (AIException) e : new AIException("AI processing failed", e)) : new AIException("AI processing failed", e);

            // Update resume status to FAILED in a safe boundary
            resume.setAiProcessingStatus(AiProcessingStatus.FAILED);
            resumeRepository.save(resume);
        }
    }

    public ResumeEntity getLatestParsedResume(java.util.UUID userId) {
        Long dbUserId = userId.getLeastSignificantBits() != 0 ? userId.getLeastSignificantBits() : Math.abs(userId.getMostSignificantBits());
        UserEntity user = userService.findUserById(dbUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + dbUserId));
        
        return resumeRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new IllegalArgumentException("No active resume found for user: " + dbUserId));
    }

    public java.util.Optional<ResumeEntity> getActiveResumeForUser(UserEntity user) {
        return resumeRepository.findByUserAndIsActiveTrue(user);
    }

    public long getResumeCountForUser(UserEntity user) {
        return resumeRepository.countByUser(user);
    }
}
