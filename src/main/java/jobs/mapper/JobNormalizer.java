package jobs.mapper;

import ai.dto.JobDocument;
import jobs.dto.ExternalJob;
import org.springframework.stereotype.Component;

@Component
public class JobNormalizer {

    public JobDocument normalize(ExternalJob externalJob) {
        if (externalJob == null) {
            return null;
        }

        String title = externalJob.getTitle();
        if (title == null || title.isBlank()) {
            title = "Unknown Position";
        } else {
            title = title.trim();
        }

        String company = externalJob.getCompany();
        if (company == null || company.isBlank()) {
            company = "Unknown Company";
        } else {
            company = company.trim();
        }

        String location = externalJob.getLocation();
        if (location == null || location.isBlank()) {
            location = "Remote";
        } else {
            location = location.trim();
        }

        String description = externalJob.getDescription();
        if (description == null) {
            description = "";
        } else {
            // Strip HTML tags for clean description text
            description = description.replaceAll("<[^>]*>", "").trim();
        }

        String employmentType = externalJob.getEmploymentType();
        if (employmentType == null || employmentType.isBlank()) {
            employmentType = "Full-time";
        } else {
            employmentType = employmentType.trim();
        }

        String applyUrl = externalJob.getApplyUrl();
        if (applyUrl == null || applyUrl.isBlank()) {
            applyUrl = "";
        } else {
            applyUrl = applyUrl.trim();
        }

        return JobDocument.builder()
                .title(title)
                .company(company)
                .location(location)
                .description(description)
                .employmentType(employmentType)
                .applyUrl(applyUrl)
                .publishedAt(externalJob.getPublishedAt())
                .build();
    }
}
