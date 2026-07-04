package recommendation.service;

import ai.client.AiClient;
import ai.dto.RecommendationRequest;
import ai.dto.RecommendationResponse;
import com.raghav.jobplatform.common.ai.AIException;
import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.dto.JobSearchRequest;
import com.raghav.jobplatform.jobs.dto.JobSearchResponse;
import com.raghav.jobplatform.jobs.model.Job;
import com.raghav.jobplatform.jobs.service.JobService;
import com.raghav.jobplatform.user.entity.ResumeEntity;
import com.raghav.jobplatform.user.service.ResumeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import recommendation.exception.AiClientException;
import recommendation.exception.NoJobsAvailableException;
import recommendation.exception.ResumeNotFoundException;
import recommendation.mapper.RecommendationMapper;
import recommendation.model.JobSearchCriteria;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RecommendationOrchestrator {

    private final ResumeService resumeService;
    private final JobService jobService;
    private final AiClient aiClient;
    private final RecommendationMapper recommendationMapper;

    public RecommendationOrchestrator(
            ResumeService resumeService,
            JobService jobService,
            AiClient aiClient,
            RecommendationMapper recommendationMapper) {
        this.resumeService = resumeService;
        this.jobService = jobService;
        this.aiClient = aiClient;
        this.recommendationMapper = recommendationMapper;
    }

    public RecommendationResponse generateRecommendations(UUID userId, JobSearchCriteria criteria) {
        log.info("Generating recommendations for user: {}, criteria: {}", userId, criteria);

        // 1. Load user's latest parsed resume
        ResumeEntity resume;
        try {
            resume = resumeService.getLatestParsedResume(userId);
        } catch (Exception e) {
            log.error("Failed to load parsed resume for user: {}", userId, e);
            throw new ResumeNotFoundException("Latest parsed resume not found for user: " + userId, e);
        }

        // 2. Fetch jobs using JobService
        List<Job> jobs = new ArrayList<>();
        try {
            int page = criteria.getPage() != null ? criteria.getPage() : 0;
            int size = criteria.getSize() != null ? criteria.getSize() : 20;

            JobSearchRequest searchRequest = new JobSearchRequest(
                    criteria.getKeyword(),
                    criteria.getLocation(),
                    criteria.getRemote(),
                    null, null, page, size, null, null, null, null, null, null
            );

            JobSearchResponse searchResponse = jobService.searchJobs(searchRequest, page, size);
            if (searchResponse == null || searchResponse.content() == null || searchResponse.content().isEmpty()) {
                throw new NoJobsAvailableException("No jobs found matching criteria: " + criteria);
            }

            for (JobListResponse listJob : searchResponse.content()) {
                try {
                    JobResponse detailJob = jobService.getJobById(listJob.id());
                    if (detailJob != null) {
                        jobs.add(new Job(
                                detailJob.title(),
                                detailJob.company(),
                                detailJob.location(),
                                detailJob.description(),
                                detailJob.applyUrl(),
                                detailJob.remote(),
                                null,
                                null,
                                null
                        ));
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch full details for job ID: {}", listJob.id(), e);
                }
            }

            if (jobs.isEmpty()) {
                throw new NoJobsAvailableException("No jobs available with full description content.");
            }
        } catch (NoJobsAvailableException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch jobs matching criteria: {}", criteria, e);
            throw new NoJobsAvailableException("Failed to fetch jobs for recommendations", e);
        }

        // 3. Convert resume + jobs into RecommendationRequest
        RecommendationRequest aiRequest;
        try {
            aiRequest = recommendationMapper.toRequest(resume, jobs);
        } catch (Exception e) {
            log.error("Failed to map resume and jobs to AI request", e);
            throw new AiClientException("Failed to map data to AI request", e);
        }

        // 4. Invoke AiClient
        RecommendationResponse response;
        try {
            response = aiClient.generateRecommendations(aiRequest);
        } catch (AIException e) {
            log.error("AI Client failed generating recommendations", e);
            throw new AiClientException("AI client failure: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to call AI Client", e);
            throw new AiClientException("Unexpected failure communicating with AI client", e);
        }

        // 5. Return RecommendationResponse
        return response;
    }
}
