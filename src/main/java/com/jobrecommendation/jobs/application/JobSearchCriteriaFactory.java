package com.jobrecommendation.jobs.application;

import com.jobrecommendation.jobs.api.dto.JobSearchRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobSearchCriteriaFactory {

    public SearchCriteria build(JobSearchRequest request) {
        String keyword = request.keyword() != null ? request.keyword().trim() : "";
        String location = request.location() != null ? request.location().trim() : "";
        String provider = request.provider() != null ? request.provider().trim() : "";
        Boolean remote = request.remote();
        String experience = request.experience() != null ? request.experience().trim() : "";
        String jobType = request.jobType() != null ? request.jobType().trim() : "";
        String company = request.company() != null ? request.company().trim() : "";

        Integer salaryMin = null;
        if (request.salaryMin() != null && !request.salaryMin().isBlank()) {
            try {
                salaryMin = Integer.parseInt(request.salaryMin().trim().replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                log.warn("Invalid salaryMin format: {}", request.salaryMin());
            }
        }

        Integer salaryMax = null;
        if (request.salaryMax() != null && !request.salaryMax().isBlank()) {
            try {
                salaryMax = Integer.parseInt(request.salaryMax().trim().replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                log.warn("Invalid salaryMax format: {}", request.salaryMax());
            }
        }

        String sortBy = null;
        String sortDirection = null;
        if (request.sortBy() != null && !request.sortBy().isBlank()) {
            String requestedSort = request.sortBy().trim();
            if (List.of("createdAt", "salaryMin", "salaryMax", "title", "company", "location", "source").contains(requestedSort)) {
                sortBy = requestedSort;
            } else {
                sortBy = "createdAt";
            }
            sortDirection = "desc";
            if (request.sortDirection() != null && "asc".equalsIgnoreCase(request.sortDirection().trim())) {
                sortDirection = "asc";
            }
        }

        int page = request.page() != null ? request.page() : 0;
        int size = request.size() != null ? request.size() : 10;

        return new SearchCriteria(
                keyword,
                location,
                provider,
                remote,
                salaryMin,
                salaryMax,
                experience,
                jobType,
                company,
                page,
                size,
                sortBy,
                sortDirection
        );
    }
}
