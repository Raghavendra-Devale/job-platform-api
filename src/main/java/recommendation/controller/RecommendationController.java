package recommendation.controller;

import ai.dto.RecommendationResponse;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import recommendation.model.JobSearchCriteria;
import recommendation.service.RecommendationOrchestrator;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationOrchestrator orchestrator;
    private final UserRepository userRepository;

    public RecommendationController(
            RecommendationOrchestrator orchestrator,
            UserRepository userRepository) {
        this.orchestrator = orchestrator;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<RecommendationResponse> generateRecommendations(
            @Valid @RequestBody JobSearchCriteria criteria) {
        
        UserEntity user = getAuthenticatedUser();
        UUID userId = new UUID(0L, user.getId());

        RecommendationResponse response = orchestrator.generateRecommendations(userId, criteria);
        return ResponseEntity.ok(response);
    }

    private UserEntity getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
