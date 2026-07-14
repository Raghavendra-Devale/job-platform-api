package com.jobrecommendation.recommendation.application;

import com.jobrecommendation.infrastructure.ai.client.RecommendationAiClient;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationMatch;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationRequest;
import com.jobrecommendation.infrastructure.ai.dto.RecommendationResponse;
import com.jobrecommendation.infrastructure.ai.exception.AIException;
import com.jobrecommendation.infrastructure.ai.exception.RecommendationAiClientException;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.jobs.domain.repository.JobRepository;
import com.jobrecommendation.recommendation.api.dto.RecommendationCardResponse;
import com.jobrecommendation.recommendation.api.dto.RecommendationDetailResponse;
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
    private final RecommendationAiClient aiClient;
    private final JobRepository jobRepository;

    public List<RecommendationCardResponse> generateRecommendations(UUID userId, JobSearchCriteria criteria) {
        log.info("Generating recommendations for user {}", userId);

        ResumeEntity resume = resumePreparationService.prepareResume(userId);
        
        RecommendationRequest request = RecommendationRequest.builder()
                .candidateProfileId(resume.getId().toString())
                .build();

        RecommendationResponse aiResponse = executeAiRecommendation(request);
        if (aiResponse == null || aiResponse.getRecommendations() == null) {
            return List.of();
        }

        return aiResponse.getRecommendations().stream()
                .map(match -> {
                    JobEntity job = jobRepository.findById(match.getJobId()).orElse(null);
                    if (job == null) {
                        log.warn("Job not found locally for ID: {}", match.getJobId());
                        return null;
                    }
                    return RecommendationCardResponse.builder()
                            .jobId(job.getId())
                            .title(job.getTitle())
                            .company(job.getCompany())
                            .location(job.getLocation())
                            .description(job.getDescription())
                            .remote(job.getRemote() != null && job.getRemote())
                            .postedAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "")
                            .similarityScore(match.getSimilarityScore() != null ? match.getSimilarityScore() : 0.0)
                            .matchingSkills(match.getMatchingSkills() != null ? match.getMatchingSkills() : List.of())
                            .missingSkills(match.getMissingSkills() != null ? match.getMissingSkills() : List.of())
                            .recommendationReason(match.getRecommendationReason())
                            .applyUrl(job.getApplyUrl())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public RecommendationDetailResponse getRecommendationDetail(UUID userId, Long jobId) {
        log.info("Fetching recommendation detail for user {} and job {}", userId, jobId);

        ResumeEntity resume = resumePreparationService.prepareResume(userId);
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Job not found with ID: " + jobId));

        RecommendationMatch match = aiClient.getJobMatchDetails(resume.getId().toString(), jobId);

        return RecommendationDetailResponse.builder()
                .jobId(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .description(job.getDescription())
                .remote(job.getRemote() != null && job.getRemote())
                .postedAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "")
                .similarityScore(match.getSimilarityScore() != null ? match.getSimilarityScore() : 0.0)
                .matchingSkills(match.getMatchingSkills() != null ? match.getMatchingSkills() : List.of())
                .missingSkills(match.getMissingSkills() != null ? match.getMissingSkills() : List.of())
                .recommendationReason(match.getRecommendationReason())
                .applyUrl(job.getApplyUrl())
                .build();
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
