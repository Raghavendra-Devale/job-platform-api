package com.jobrecommendation.recommendation.application;

import com.jobrecommendation.infrastructure.ai.dto.JobDocument;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationRequest;
import com.jobrecommendation.resume.domain.ResumeEntity;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RecommendationRequestFactory {

    public RecommendationRequest create(ResumeEntity resume, List<JobDocument> jobs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Resume of ").append(resume.getUser().getName()).append("\n\n");
        
        if (resume.getSummary() != null && resume.getSummary().getSummary() != null) {
            sb.append("Summary:\n").append(resume.getSummary().getSummary()).append("\n\n");
        }
        
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            sb.append("Skills:\n");
            String skillsStr = resume.getSkills().stream()
                    .map(s -> s.getName())
                    .collect(Collectors.joining(", "));
            sb.append(skillsStr).append("\n\n");
        }
        
        if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            sb.append("Experience:\n");
            resume.getExperiences().forEach(exp -> {
                sb.append("- ").append(exp.getDesignation()).append(" at ").append(exp.getCompany());
                if (exp.getResponsibilities() != null && !exp.getResponsibilities().isEmpty()) {
                    sb.append(" (Responsibilities: ").append(String.join(", ", exp.getResponsibilities())).append(")");
                }
                sb.append("\n");
            });
            sb.append("\n");
        }
        
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            sb.append("Projects:\n");
            resume.getProjects().forEach(p -> {
                sb.append("- ").append(p.getName()).append(": ").append(p.getDescription());
                if (p.getTechnologies() != null && !p.getTechnologies().isEmpty()) {
                    sb.append(" [Tech: ").append(String.join(", ", p.getTechnologies())).append("]");
                }
                sb.append("\n");
            });
            sb.append("\n");
        }

        return RecommendationRequest.builder()
                .resumeText(sb.toString())
                .jobs(jobs)
                .build();
    }
}
