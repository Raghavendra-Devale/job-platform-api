package com.raghav.jobplatform.jobs.service;

import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.dto.JobSearchRequest;
import com.raghav.jobplatform.jobs.dto.JobSearchResponse;
import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JobService {

    private final List<JobProvider> jobProviders;
    private final JobRepository jobRepository;

    public JobService(
            List<JobProvider> jobProviders,
            JobRepository jobRepository) {
        this.jobProviders = jobProviders;
        this.jobRepository = jobRepository;
    }

    public List<JobResponse> searchJobs(String keyword) {
        jobProviders.forEach(provider ->
                log.info("Searching jobs from {}", provider.getProviderName()));

        return jobProviders.stream()
                .flatMap(provider ->
                        provider.searchJobs(keyword).stream())
                .toList();
    }

    public Page<JobListResponse> getJobs(int page, int size) {
        return jobRepository.findAll(PageRequest.of(page, size))
                .map(job -> new JobListResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getSource(),
                        job.getRemote(),
                        job.getTags(),
                        job.getCreatedAt()
                ));
    }

    public JobSearchResponse searchJobs(
            JobSearchRequest request,
            int page,
            int size
    ) {
        String keyword = request.keyword() != null ? request.keyword().trim() : "";
        String location = request.location() != null ? request.location().trim() : "";
        String provider = request.provider() != null ? request.provider().trim() : "";
        Boolean remote = request.remote();

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

        Page<JobEntity> entityPage;

        // Check if custom sorting is requested
        if (request.sortBy() != null && !request.sortBy().isBlank()) {
            // Whitelist sort fields to prevent SQL injection/errors
            String sortBy = "createdAt"; // default fallback
            String requestedSort = request.sortBy().trim();
            if (List.of("createdAt", "salaryMin", "salaryMax", "title", "company", "location", "source").contains(requestedSort)) {
                sortBy = requestedSort;
            }

            org.springframework.data.domain.Sort.Direction direction = org.springframework.data.domain.Sort.Direction.DESC; // default
            if (request.sortDirection() != null && "asc".equalsIgnoreCase(request.sortDirection().trim())) {
                direction = org.springframework.data.domain.Sort.Direction.ASC;
            }

            entityPage = jobRepository.searchJobs(
                    keyword,
                    location,
                    provider,
                    remote,
                    salaryMin,
                    salaryMax,
                    PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortBy))
            );
        } else {
            // Default: keyword match weighted ranking
            entityPage = jobRepository.searchJobsRanked(
                    keyword,
                    location,
                    provider,
                    remote,
                    salaryMin,
                    salaryMax,
                    PageRequest.of(page, size)
            );
        }

        List<JobListResponse> content = entityPage.stream()
                .map(job -> new JobListResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getSource(),
                        job.getRemote(),
                        job.getTags(),
                        job.getCreatedAt()
                ))
                .toList();

        List<String> distinctProviders = jobRepository.findDistinctSources();
        List<String> distinctLocations = jobRepository.findDistinctLocations();

        Map<String, List<String>> filters = Map.of(
                "providers", distinctProviders,
                "locations", distinctLocations
        );

        return new JobSearchResponse(
                content,
                filters,
                entityPage.getTotalElements(),
                entityPage.getTotalPages(),
                entityPage.getSize(),
                entityPage.getNumber()
        );
    }

    public JobResponse getJobById(Long id) {
        var job = jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Job not found with id: " + id
                ));

        return new JobResponse(
                job.getExternalJobId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription(),
                job.getApplyUrl(),
                job.getRemote(),
                job.getTags()
        );
    }
}