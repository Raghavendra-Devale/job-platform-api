package com.jobrecommendation.jobs.application;

import com.jobrecommendation.jobs.api.dto.JobListResponse;
import com.jobrecommendation.jobs.api.dto.JobResponse;
import com.jobrecommendation.jobs.api.dto.JobSearchResponse;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.jobs.domain.repository.JobRepository;
import com.jobrecommendation.jobs.infrastructure.provider.JobProvider;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {

    private final List<JobProvider> jobProviders;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

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
                .map(jobMapper::toJobListResponse);
    }

    public JobSearchResponse searchJobs(SearchCriteria criteria) {
        Page<JobEntity> entityPage;

        if (criteria.sortBy() != null && !criteria.sortBy().isBlank()) {
            org.springframework.data.domain.Sort.Direction direction = 
                    "asc".equalsIgnoreCase(criteria.sortDirection()) ? 
                            org.springframework.data.domain.Sort.Direction.ASC : 
                            org.springframework.data.domain.Sort.Direction.DESC;

            entityPage = jobRepository.searchJobs(
                    criteria.keyword(),
                    criteria.location(),
                    criteria.provider(),
                    criteria.remote(),
                    criteria.salaryMin(),
                    criteria.salaryMax(),
                    criteria.experience(),
                    criteria.jobType(),
                    criteria.company(),
                    PageRequest.of(criteria.page(), criteria.size(), org.springframework.data.domain.Sort.by(direction, criteria.sortBy()))
            );
        } else {
            entityPage = jobRepository.searchJobsRanked(
                    criteria.keyword(),
                    criteria.location(),
                    criteria.provider(),
                    criteria.remote(),
                    criteria.salaryMin(),
                    criteria.salaryMax(),
                    criteria.experience(),
                    criteria.jobType(),
                    criteria.company(),
                    PageRequest.of(criteria.page(), criteria.size())
            );
        }

        List<JobListResponse> content = entityPage.stream()
                .map(jobMapper::toJobListResponse)
                .toList();

        List<String> distinctProviders = jobRepository.findDistinctSources();
        List<String> distinctLocations = jobRepository.findDistinctLocations();
        List<String> distinctJobTypes = jobRepository.findDistinctJobTypes();

        Map<String, List<String>> filters = Map.of(
                "providers", distinctProviders,
                "locations", distinctLocations,
                "jobTypes", distinctJobTypes
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

        return jobMapper.toJobResponse(job);
    }

    public java.util.Optional<JobEntity> findJobEntityById(Long id) {
        return jobRepository.findById(id);
    }

    public Page<JobEntity> searchJobsRanked(
            String keyword, String location, String provider, Boolean remote,
            Integer salaryMin, Integer salaryMax, String experience, String jobType,
            String company, PageRequest pageRequest) {
        return jobRepository.searchJobsRanked(
                keyword, location, provider, remote, salaryMin, salaryMax,
                experience, jobType, company, pageRequest);
    }
}
