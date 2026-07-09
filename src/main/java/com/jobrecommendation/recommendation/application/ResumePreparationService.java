package com.jobrecommendation.recommendation.application;

import com.jobrecommendation.infrastructure.ai.exception.RecommendationAiClientException;
import com.jobrecommendation.recommendation.domain.exception.ResumeNotFoundException;
import com.jobrecommendation.resume.application.ResumeIntelligenceService;
import com.jobrecommendation.resume.application.ResumeQueryService;
import com.jobrecommendation.resume.domain.ResumeEntity;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumePreparationService {

    private final ResumeQueryService resumeQueryService;
    private final ResumeIntelligenceService resumeIntelligenceService;

    public ResumeEntity prepareResume(UUID userId) {
        ResumeEntity resume = getLatestResume(userId);
        if (resume.getAiProcessingStatus() != com.jobrecommendation.resume.domain.AiProcessingStatus.SUCCESS) {
            runDynamicAiAnalysis(resume);
            resume = getLatestResume(userId);
        }
        return resume;
    }

    private ResumeEntity getLatestResume(UUID userId) {
        try {
            return resumeQueryService.getLatestParsedResume(userId);
        } catch (Exception e) {
            log.error("Failed to load parsed resume for user {}", userId, e);
            throw new ResumeNotFoundException("Latest parsed resume not found for user: " + userId, e);
        }
    }

    private void runDynamicAiAnalysis(ResumeEntity resume) {
        log.info("Active resume {} has status {}. Running AI analysis dynamically...", resume.getId(), resume.getAiProcessingStatus());
        try {
            resumeIntelligenceService.processResumeIntelligence(resume);
        } catch (Exception e) {
            log.error("AI processing failed for resume {}", resume.getId(), e);
            throw new RecommendationAiClientException("Failed to analyze resume via AI: " + e.getMessage(), e);
        }
    }
}
