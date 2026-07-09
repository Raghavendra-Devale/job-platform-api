package com.jobrecommendation.resume.application;

import com.jobrecommendation.infrastructure.ai.dto.ResumeIntelligenceResponse;
import com.jobrecommendation.resume.domain.ResumeEducationEntity;
import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.resume.domain.ResumeExperienceEntity;
import com.jobrecommendation.resume.domain.ResumeProjectEntity;
import com.jobrecommendation.resume.domain.ResumeSkillEntity;
import com.jobrecommendation.resume.domain.ResumeSummaryEntity;
import com.jobrecommendation.resume.domain.repository.ResumeEducationRepository;
import com.jobrecommendation.resume.domain.repository.ResumeExperienceRepository;
import com.jobrecommendation.resume.domain.repository.ResumeProjectRepository;
import com.jobrecommendation.resume.domain.repository.ResumeSkillRepository;
import com.jobrecommendation.resume.domain.repository.ResumeSummaryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeIntelligencePersistenceService {

    private final ResumeSummaryRepository resumeSummaryRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final ResumeEducationRepository resumeEducationRepository;
    private final ResumeExperienceRepository resumeExperienceRepository;
    private final ResumeProjectRepository resumeProjectRepository;

    @Transactional
    public void persist(ResumeEntity resume, ResumeIntelligenceResponse response) {
        ResumeIntelligenceResponse.ResumeDetails details = response.getResume();
        if (details == null) {
            return;
        }

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
}
