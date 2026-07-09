package com.jobrecommendation.recommendation.application;

import com.jobrecommendation.infrastructure.ai.dto.JobDocument;
import com.jobrecommendation.jobs.application.JobMapper;
import com.jobrecommendation.jobs.application.JobService;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.recommendation.domain.JobSearchCriteria;
import com.jobrecommendation.recommendation.domain.exception.NoJobsAvailableException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobRecommendationService {

    private final JobService jobService;
    private final JobMapper jobMapper;

    public List<JobDocument> findJobs(JobSearchCriteria criteria) {
        Page<JobEntity> entityPage = fetchRankedJobs(criteria);
        
        List<JobDocument> jobDocs = entityPage.stream()
                .map(jobMapper::toJobDocument)
                .toList();

        if (jobDocs.isEmpty()) {
            throw new NoJobsAvailableException("No jobs found matching criteria: " + criteria);
        }
        return jobDocs;
    }

    private Page<JobEntity> fetchRankedJobs(JobSearchCriteria criteria) {
        try {
            int page = criteria.getPage() != null ? criteria.getPage() : 0;
            int size = criteria.getSize() != null ? criteria.getSize() : 20;
            String keyword = criteria.getKeyword() != null ? criteria.getKeyword() : "";
            String location = criteria.getLocation() != null ? criteria.getLocation() : "";
            
            return jobService.searchJobsRanked(
                    keyword, location, "", criteria.getRemote(), null, null, "", "", "", PageRequest.of(page, size)
            );
        } catch (Exception e) {
            log.error("Failed to fetch jobs matching criteria: {}", criteria, e);
            throw new NoJobsAvailableException("Failed to fetch jobs for recommendations", e);
        }
    }
}
