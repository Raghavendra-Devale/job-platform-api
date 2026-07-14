package com.jobrecommendation.recommendation.api;

import com.jobrecommendation.recommendation.api.dto.RecommendationCardResponse;
import com.jobrecommendation.recommendation.api.dto.RecommendationDetailResponse;
import com.jobrecommendation.recommendation.application.RecommendationOrchestrator;
import com.jobrecommendation.recommendation.domain.JobSearchCriteria;
import com.jobrecommendation.user.application.UserService;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationOrchestrator orchestrator;
    private final UserService userService;

    public RecommendationController(
            RecommendationOrchestrator orchestrator,
            UserService userService) {
        this.orchestrator = orchestrator;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<List<RecommendationCardResponse>> generateRecommendations(
            @RequestBody(required = false) JobSearchCriteria criteria) {
        
        if (criteria == null) {
            criteria = JobSearchCriteria.builder().build();
        }
        
        UserEntity user = getAuthenticatedUser();
        UUID userId = new UUID(0L, user.getId());

        List<RecommendationCardResponse> response = orchestrator.generateRecommendations(userId, criteria);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecommendationDetailResponse> getRecommendationDetail(@PathVariable("id") Long jobId) {
        UserEntity user = getAuthenticatedUser();
        UUID userId = new UUID(0L, user.getId());

        RecommendationDetailResponse response = orchestrator.getRecommendationDetail(userId, jobId);
        return ResponseEntity.ok(response);
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        return userService.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
