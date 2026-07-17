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
import com.jobrecommendation.recommendation.domain.RecommendationRunEntity;
import com.jobrecommendation.recommendation.domain.RecommendationItemEntity;
import com.jobrecommendation.recommendation.domain.repository.RecommendationRunRepository;
import com.jobrecommendation.recommendation.api.dto.RecommendationRunResponse;
import com.jobrecommendation.recommendation.api.dto.RecommendationRunSummaryResponse;
import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.resume.domain.ResumeEntity;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationOrchestrator {

    private final ResumePreparationService resumePreparationService;
    private final RecommendationAiClient aiClient;
    private final JobRepository jobRepository;
    private final RecommendationRunRepository recommendationRunRepository;

    @Transactional(readOnly = true)
    public RecommendationRunResponse getLatestRecommendationRun(UserEntity user) {
        return recommendationRunRepository.findFirstByUserOrderByGeneratedAtDesc(user)
                .map(this::mapToRunResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<RecommendationRunSummaryResponse> getRecommendationHistory(UserEntity user) {
        return recommendationRunRepository.findByUserOrderByGeneratedAtDesc(user).stream()
                .map(run -> RecommendationRunSummaryResponse.builder()
                        .id(run.getId())
                        .generatedAt(run.getGeneratedAt())
                        .averageMatch(run.getAverageMatch())
                        .recommendationCount(run.getRecommendationCount())
                        .build())
                .toList();
    }

    private RecommendationRunResponse mapToRunResponse(RecommendationRunEntity run) {
        List<RecommendationCardResponse> cards = run.getItems().stream()
                .map(item -> {
                    JobEntity job = item.getJob();
                    return RecommendationCardResponse.builder()
                            .jobId(job.getId())
                            .title(job.getTitle())
                            .company(job.getCompany())
                            .location(job.getLocation())
                            .description(job.getDescription())
                            .remote(job.getRemote() != null && job.getRemote())
                            .postedAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "")
                            .similarityScore(item.getScore())
                            .matchingSkills(item.getMatchingSkills().isEmpty() ? List.of() : List.of(item.getMatchingSkills().split(",")))
                            .missingSkills(item.getMissingSkills().isEmpty() ? List.of() : List.of(item.getMissingSkills().split(",")))
                            .recommendationReason(item.getReason())
                            .applyUrl(job.getApplyUrl())
                            .build();
                })
                .toList();

        return RecommendationRunResponse.builder()
                .id(run.getId())
                .generatedAt(run.getGeneratedAt())
                .averageMatch(run.getAverageMatch())
                .recommendationCount(run.getRecommendationCount())
                .items(cards)
                .build();
    }

    @Transactional
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

        List<RecommendationMatch> matches = aiResponse.getRecommendations();
        double sum = 0.0;
        for (RecommendationMatch m : matches) {
            sum += m.getSimilarityScore() != null ? m.getSimilarityScore() : 0.0;
        }
        double avgMatch = matches.isEmpty() ? 0.0 : (sum / matches.size());

        // Create RecommendationRunEntity
        RecommendationRunEntity run = RecommendationRunEntity.builder()
                .user(resume.getUser())
                .resume(resume)
                .generatedAt(java.time.LocalDateTime.now())
                .averageMatch(avgMatch)
                .recommendationCount(matches.size())
                .build();

        List<RecommendationItemEntity> items = new java.util.ArrayList<>();
        int rank = 1;
        for (RecommendationMatch match : matches) {
            JobEntity job = jobRepository.findById(match.getJobId()).orElse(null);
            if (job == null) {
                log.warn("Job not found locally for ID: {}", match.getJobId());
                continue;
            }
            RecommendationItemEntity item = RecommendationItemEntity.builder()
                    .run(run)
                    .job(job)
                    .score(match.getSimilarityScore() != null ? match.getSimilarityScore() : 0.0)
                    .reason(match.getRecommendationReason())
                    .matchingSkills(match.getMatchingSkills() != null ? String.join(",", match.getMatchingSkills()) : "")
                    .missingSkills(match.getMissingSkills() != null ? String.join(",", match.getMissingSkills()) : "")
                    .rank(rank++)
                    .build();
            items.add(item);
        }
        run.setItems(items);

        recommendationRunRepository.save(run);

        return items.stream()
                .map(item -> {
                    JobEntity job = item.getJob();
                    return RecommendationCardResponse.builder()
                            .jobId(job.getId())
                            .title(job.getTitle())
                            .company(job.getCompany())
                            .location(job.getLocation())
                            .description(job.getDescription())
                            .remote(job.getRemote() != null && job.getRemote())
                            .postedAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "")
                            .similarityScore(item.getScore())
                            .matchingSkills(item.getMatchingSkills().isEmpty() ? List.of() : List.of(item.getMatchingSkills().split(",")))
                            .missingSkills(item.getMissingSkills().isEmpty() ? List.of() : List.of(item.getMissingSkills().split(",")))
                            .recommendationReason(item.getReason())
                            .applyUrl(job.getApplyUrl())
                            .build();
                })
                .toList();
    }

    public RecommendationDetailResponse getRecommendationDetail(UUID userId, Long jobId) {
        log.info("Fetching recommendation detail for user {} and job {}", userId, jobId);

        ResumeEntity resume = resumePreparationService.prepareResume(userId);
        JobEntity job = jobRepository.findById(jobId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Job not found with ID: " + jobId));

        RecommendationMatch match = aiClient.getJobMatchDetails(resume.getId().toString(), jobId);

        List<String> matchingSkills = match.getMatchingSkills() != null ? match.getMatchingSkills() : List.of();
        List<String> missingSkills = match.getMissingSkills() != null ? match.getMissingSkills() : List.of();

        List<String> strengths = List.of(
            matchingSkills.isEmpty() 
                ? "Your general profile aligns with this designation."
                : "Strong match in core skills: " + String.join(", ", matchingSkills),
            "Your professional background maps to the job requirement guidelines."
        );

        List<String> suggestions = List.of(
            missingSkills.isEmpty() 
                ? "Perfect skills match! Highlight this match in your cover letter."
                : "Bridge the technical skill gap by adding: " + String.join(", ", missingSkills),
            "Optimize resume formatting to clearly outline matching technology experience."
        );

        return RecommendationDetailResponse.builder()
                .jobId(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .description(job.getDescription())
                .remote(job.getRemote() != null && job.getRemote())
                .postedAt(job.getCreatedAt() != null ? job.getCreatedAt().toString() : "")
                .similarityScore(match.getSimilarityScore() != null ? match.getSimilarityScore() : 0.0)
                .matchingSkills(matchingSkills)
                .missingSkills(missingSkills)
                .recommendationReason(match.getRecommendationReason())
                .applyUrl(job.getApplyUrl())
                .strengths(strengths)
                .suggestions(suggestions)
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
