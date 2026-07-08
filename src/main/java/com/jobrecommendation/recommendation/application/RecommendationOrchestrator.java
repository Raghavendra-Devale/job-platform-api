package com.jobrecommendation.recommendation.application;

import com.jobrecommendation.infrastructure.ai.client.RecommendationAiClient;
import com.jobrecommendation.infrastructure.ai.dto.JobDocument;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationRequest;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationResponse;
import com.jobrecommendation.infrastructure.ai.exception.AIException;
import com.jobrecommendation.infrastructure.ai.exception.RecommendationAiClientException;
import com.jobrecommendation.jobs.application.JobService;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.recommendation.domain.JobSearchCriteria;
import com.jobrecommendation.recommendation.domain.exception.NoJobsAvailableException;
import com.jobrecommendation.recommendation.domain.exception.ResumeNotFoundException;
import com.jobrecommendation.recommendation.mapper.RecommendationMapper;
import com.jobrecommendation.resume.application.ResumeService;
import com.jobrecommendation.resume.domain.AiProcessingStatus;
import com.jobrecommendation.resume.domain.ResumeEntity;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecommendationOrchestrator {

    private final ResumeService resumeService;
    private final JobService jobService;
    private final RecommendationAiClient aiClient;
    private final RecommendationMapper recommendationMapper;

    public RecommendationOrchestrator(
            ResumeService resumeService,
            JobService jobService,
            RecommendationAiClient aiClient,
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
            if (resume.getAiProcessingStatus() != com.jobrecommendation.resume.domain.AiProcessingStatus.SUCCESS) {
                log.info("Active resume ID {} has status {}. Running AI analysis dynamically...", resume.getId(), resume.getAiProcessingStatus());
                try {
                    resumeService.processResumeIntelligence(resume);
                } catch (Exception e) {
                    log.error("Dynamic AI resume processing failed for resume ID: {}", resume.getId(), e);
                    throw new RecommendationAiClientException("Failed to analyze resume via AI: " + e.getMessage(), e);
                }
                // Re-fetch to load all associations populated by AI processing
                resume = resumeService.getLatestParsedResume(userId);
            }
        } catch (RecommendationAiClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to load parsed resume for user: {}", userId, e);
            throw new ResumeNotFoundException("Latest parsed resume not found for user: " + userId, e);
        }

        // 2. Fetch jobs using JobService (live data)
        List<JobDocument> jobDocs;
        try {
            int page = criteria.getPage() != null ? criteria.getPage() : 0;
            int size = criteria.getSize() != null ? criteria.getSize() : 20;
            String keyword = criteria.getKeyword() != null ? criteria.getKeyword() : "";
            String location = criteria.getLocation() != null ? criteria.getLocation() : "";
            
            Page<JobEntity> entityPage = jobService.searchJobsRanked(
                    keyword, location, "", criteria.getRemote(), null, null, "", "", "", PageRequest.of(page, size)
            );
            
            jobDocs = entityPage.stream()
                    .map(job -> JobDocument.builder()
                            .title(job.getTitle())
                            .company(job.getCompany())
                            .location(job.getLocation())
                            .description(job.getDescription())
                            .applyUrl(job.getApplyUrl())
                            .employmentType(job.getJobType())
                            .build())
                    .toList();

            if (jobDocs == null || jobDocs.isEmpty()) {
                throw new NoJobsAvailableException("No jobs found matching criteria: " + criteria);
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
            aiRequest = recommendationMapper.toRequestFromDocuments(resume, jobDocs);
        } catch (Exception e) {
            log.error("Failed to map resume and jobs to AI request", e);
            throw new RecommendationAiClientException("Failed to map data to AI request", e);
        }

        // 4. Invoke AiClient
        RecommendationResponse response;
        try {
            response = aiClient.generateRecommendations(aiRequest);
        } catch (AIException e) {
            log.error("AI Client failed generating recommendations", e);
            throw new RecommendationAiClientException("AI client failure: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to call AI Client", e);
            throw new RecommendationAiClientException("Unexpected failure communicating with AI client", e);
        }

        // 5. Return RecommendationResponse
        return response;
    }
}
