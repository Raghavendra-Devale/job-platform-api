package com.jobrecommendation.dashboard.application;

import com.jobrecommendation.applications.application.ApplicationService;
import com.jobrecommendation.dashboard.api.dto.*;
import com.jobrecommendation.recommendation.api.dto.RecommendationCardResponse;
import com.jobrecommendation.recommendation.api.dto.RecommendationRunResponse;
import com.jobrecommendation.recommendation.application.RecommendationOrchestrator;
import com.jobrecommendation.resume.api.dto.ResumeIntelligenceResponse;
import com.jobrecommendation.resume.application.ResumeService;
import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.resume.domain.ResumeSkillEntity;
import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.UserProfileEntity;
import com.jobrecommendation.user.domain.repository.UserProfileRepository;
import com.jobrecommendation.user.domain.repository.RecentViewRepository;
import com.jobrecommendation.user.domain.RecentViewEntity;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ResumeService resumeService;
    private final RecommendationOrchestrator recommendationOrchestrator;
    private final ApplicationService applicationService;
    private final UserProfileRepository userProfileRepository;
    private final RecentViewRepository recentViewRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(UserEntity user) {
        log.info("Aggregating dashboard response for user {}", user.getId());

        // 1. User Summary
        UserSummaryDto userSummary = new UserSummaryDto(user.getName(), user.getEmail());

        // 2. Resume Intelligence
        ResumeIntelligenceResponse resumeIntel = getResumeIntelligence(user);

        // 3. Latest Recommendation Run
        RecommendationRunResponse latestRun = recommendationOrchestrator.getLatestRecommendationRun(user);

        // 4. Recommendation Status
        RecommendationStatusDto status = getRecommendationStatus(user, latestRun);

        // 5. Career Insights
        List<CareerInsightDto> insights = getCareerInsights(latestRun);

        // 6. Application Summary
        long saved = applicationService.getSavedJobsCountForUser(user);
        long applied = applicationService.getJobApplicationsCountForUser(user);
        long interview = applicationService.getJobApplicationsCountForUserAndStatus(user, "INTERVIEW");
        long offer = applicationService.getJobApplicationsCountForUserAndStatus(user, "OFFER");
        ApplicationSummaryDto appSummary = new ApplicationSummaryDto(saved, applied, interview, offer);

        // 7. Recently Viewed Jobs
        List<RecentlyViewedJobDto> recentJobs = recentViewRepository.findTop10ByUserOrderByViewedAtDesc(user).stream()
                .map(rv -> new RecentlyViewedJobDto(
                    rv.getJob().getId(),
                    rv.getJob().getTitle(),
                    rv.getJob().getCompany(),
                    rv.getJob().getLocation(),
                    rv.getJob().getRemote()
                ))
                .toList();

        return new DashboardResponse(
            userSummary,
            resumeIntel,
            status,
            latestRun,
            insights,
            appSummary,
            recentJobs
        );
    }

    private ResumeIntelligenceResponse getResumeIntelligence(UserEntity user) {
        Optional<ResumeEntity> activeResumeOpt = resumeService.getActiveResume(user);

        if (activeResumeOpt.isEmpty()) {
            return ResumeIntelligenceResponse.builder()
                    .status("NOT_UPLOADED")
                    .skills(List.of())
                    .strengths(List.of())
                    .improvementSuggestions(List.of())
                    .build();
        }

        ResumeEntity resume = activeResumeOpt.get();
        String statusStr = resume.getAiProcessingStatus().name(); // SUCCESS, FAILED, PROCESSING, PENDING

        if ("SUCCESS".equals(statusStr)) {
            statusStr = "READY";
        }

        if (!"READY".equals(statusStr)) {
            return ResumeIntelligenceResponse.builder()
                    .status(statusStr)
                    .skills(List.of())
                    .strengths(List.of())
                    .improvementSuggestions(List.of())
                    .build();
        }

        UserProfileEntity profile = userProfileRepository.findByUser(user).orElse(null);
        String experienceStr = "0 years";
        String roleStr = "Software Engineer";

        if (profile != null) {
            if (profile.getExperience() != null) {
                experienceStr = profile.getExperience() + " years";
            }
            if (profile.getCurrentRole() != null && !profile.getCurrentRole().isBlank()) {
                roleStr = profile.getCurrentRole();
            }
        } else if (resume.getExperiences() != null && !resume.getExperiences().isEmpty()) {
            roleStr = resume.getExperiences().get(0).getDesignation();
        }

        ResumeIntelligenceResponse.ResumeSummaryDto summaryDto = ResumeIntelligenceResponse.ResumeSummaryDto.builder()
                .candidateName(user.getName())
                .role(roleStr)
                .experience(experienceStr)
                .build();

        int score = 75;
        if (resume.getSkills() != null) {
            score += Math.min(resume.getSkills().size() * 3, 15);
        }
        if (resume.getExperiences() != null) {
            score += Math.min(resume.getExperiences().size() * 2, 5);
        }
        score = Math.min(score, 100);

        List<String> skillsList = resume.getSkills().stream()
                .map(ResumeSkillEntity::getName)
                .toList();

        List<String> strengths = List.of(
            "Strong alignment with modern software development standards.",
            "Demonstrated core competence in backend engineering processes."
        );

        List<String> suggestions = List.of(
            "Highlight cloud deployment experience or DevOps toolkits.",
            "Verify formatting conventions match standardized parsing formats."
        );

        return ResumeIntelligenceResponse.builder()
                .status("READY")
                .summary(summaryDto)
                .resumeScore(score)
                .skills(skillsList)
                .strengths(strengths)
                .improvementSuggestions(suggestions)
                .build();
    }

    private RecommendationStatusDto getRecommendationStatus(UserEntity user, RecommendationRunResponse latestRun) {
        if (latestRun == null) {
            return new RecommendationStatusDto("NOT_RUN", "No recommendations run yet.", null, false);
        }

        Optional<ResumeEntity> activeResumeOpt = resumeService.getActiveResume(user);
        if (activeResumeOpt.isEmpty()) {
            return new RecommendationStatusDto("NOT_RUN", "Please upload a resume first.", latestRun.generatedAt(), false);
        }

        ResumeEntity activeResume = activeResumeOpt.get();
        boolean isOutdated = activeResume.getUpdatedAt().isAfter(latestRun.generatedAt());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a");
        String formattedTime = latestRun.generatedAt().format(formatter);

        if (isOutdated) {
            return new RecommendationStatusDto(
                "OUTDATED", 
                "Your resume has changed since your last recommendation run. Generate a new run to receive updated matches.", 
                latestRun.generatedAt(), 
                true
            );
        } else {
            return new RecommendationStatusDto(
                "READY", 
                "Generated " + formattedTime, 
                latestRun.generatedAt(), 
                false
            );
        }
    }

    private List<CareerInsightDto> getCareerInsights(RecommendationRunResponse latestRun) {
        List<CareerInsightDto> insights = new java.util.ArrayList<>();
        if (latestRun == null || latestRun.items().isEmpty()) {
            insights.add(new CareerInsightDto("Strongest Skill", "Upload resume to analyze"));
            insights.add(new CareerInsightDto("Top Missing Skill", "Complete profile"));
            insights.add(new CareerInsightDto("Average Match", "0%"));
            insights.add(new CareerInsightDto("Recommended Next Step", "Generate recommendations"));
            return insights;
        }

        List<RecommendationCardResponse> items = latestRun.items();
        
        Map<String, Integer> matchSkillsCount = new HashMap<>();
        for (RecommendationCardResponse item : items) {
            if (item.getMatchingSkills() != null) {
                for (String s : item.getMatchingSkills()) {
                    matchSkillsCount.put(s, matchSkillsCount.getOrDefault(s, 0) + 1);
                }
            }
        }
        String strongestSkill = matchSkillsCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Spring Boot");

        Map<String, Integer> missingSkillsCount = new HashMap<>();
        for (RecommendationCardResponse item : items) {
            if (item.getMissingSkills() != null) {
                for (String s : item.getMissingSkills()) {
                    missingSkillsCount.put(s, missingSkillsCount.getOrDefault(s, 0) + 1);
                }
            }
        }
        String topMissingSkill = missingSkillsCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("AWS");

        insights.add(new CareerInsightDto("Strongest Skill", strongestSkill));
        insights.add(new CareerInsightDto("Top Missing Skill", topMissingSkill));
        insights.add(new CareerInsightDto("Average Match", Math.round(latestRun.averageMatch() * 100) + "%"));
        insights.add(new CareerInsightDto("Recommended Next Step", "Learn " + topMissingSkill));

        return insights;
    }
}
