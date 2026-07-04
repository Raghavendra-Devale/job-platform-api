package recommendation.mapper;

import ai.dto.JobDocument;
import ai.dto.RecommendationRequest;
import com.raghav.jobplatform.jobs.model.Job;
import com.raghav.jobplatform.user.entity.ResumeEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecommendationMapper {

    public RecommendationRequest toRequest(ResumeEntity resume, List<Job> jobs) {
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

        List<JobDocument> jobDocuments = jobs.stream()
                .map(this::toJobDocument)
                .toList();

        return RecommendationRequest.builder()
                .resumeText(sb.toString())
                .jobs(jobDocuments)
                .build();
    }

    public RecommendationRequest toRequestFromDocuments(ResumeEntity resume, List<JobDocument> jobs) {
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

    public JobDocument toJobDocument(Job job) {
        return JobDocument.builder()
                .title(job.title())
                .company(job.company())
                .location(job.location())
                .description(job.description())
                .applyUrl(job.applyUrl())
                .employmentType(job.jobType())
                .publishedAt(null)
                .build();
    }
}
