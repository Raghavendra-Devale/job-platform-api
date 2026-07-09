package com.jobrecommendation.recommendation.application;

import com.jobrecommendation.infrastructure.ai.client.RecommendationAiClient;
import com.jobrecommendation.infrastructure.ai.dto.JobDocument;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationRequest;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationResponse;
import com.jobrecommendation.infrastructure.ai.exception.AIException;
import com.jobrecommendation.infrastructure.ai.exception.RecommendationAiClientException;
import com.jobrecommendation.recommendation.domain.JobSearchCriteria;
import com.jobrecommendation.resume.domain.ResumeEntity;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationOrchestrator {

    private final ResumePreparationService resumePreparationService;
    private final JobRecommendationService jobRecommendationService;
    private final RecommendationRequestFactory recommendationRequestFactory;
    private final RecommendationAiClient aiClient;

    public RecommendationResponse generateRecommendations(UUID userId, JobSearchCriteria criteria) {
        log.info("Generating recommendations for user {}", userId);

        ResumeEntity resume = resumePreparationService.prepareResume(userId);
        List<JobDocument> jobs = jobRecommendationService.findJobs(criteria);
        RecommendationRequest request = recommendationRequestFactory.create(resume, jobs);

        return executeAiRecommendation(request);
    }

    private RecommendationResponse executeAiRecommendation(RecommendationRequest request) {
        try {
            return aiClient.generateRecommendations(request);
        } catch (AIException e) {
            log.error("AI Client failed generating recommendations", e);
            throw new RecommendationAiClientException("AI client failure: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to call AI Client", e);
            throw new RecommendationAiClientException("Unexpected failure communicating with AI client", e);
        }
    }
}
