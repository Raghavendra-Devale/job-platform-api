package com.jobrecommendation.resume.application;

import com.jobrecommendation.infrastructure.ai.client.ResumeAiClient;
import com.jobrecommendation.infrastructure.ai.dto.ResumeIntelligenceResponse;
import com.jobrecommendation.infrastructure.ai.dto.ResumeProcessRequest;
import com.jobrecommendation.infrastructure.ai.exception.AIException;
import com.jobrecommendation.resume.domain.AiProcessingStatus;
import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.resume.domain.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeIntelligenceService {

    private final ResumeAiClient aiClient;
    private final ResumeIntelligencePersistenceService persistenceService;
    private final ResumeRepository resumeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processResumeIntelligence(ResumeEntity resume) {
        log.info("Starting AI intelligence extraction for resume {}", resume.getId());

        try {
            ResumeProcessRequest request = ResumeProcessRequest.builder()
                    .fileBytes(resume.getResumeData())
                    .filename(resume.getResumeName())
                    .resumeId(resume.getId())
                    .userId(resume.getUser().getId())
                    .build();

            ResumeIntelligenceResponse response = aiClient.processResume(request);

            if (response == null || !response.isSuccess()) {
                throw new AIException("AI Engine returned failure response or empty response payload");
            }

            persistenceService.persist(resume, response);

            // Update resume status to SUCCESS
            resume.setAiProcessingStatus(AiProcessingStatus.SUCCESS);
            resumeRepository.save(resume);
            log.info("AI intelligence extraction succeeded for resume {}", resume.getId());

        } catch (Exception e) {
            log.error("AI processing failed for resume {}", resume.getId(), e);
            
            // Update resume status to FAILED in a safe boundary
            resume.setAiProcessingStatus(AiProcessingStatus.FAILED);
            resumeRepository.save(resume);
        }
    }
}
