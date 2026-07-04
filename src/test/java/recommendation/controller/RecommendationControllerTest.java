package recommendation.controller;

import ai.dto.RecommendationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.raghav.jobplatform.common.GlobalExceptionHandler;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import recommendation.exception.AiClientException;
import recommendation.exception.NoJobsAvailableException;
import recommendation.exception.ResumeNotFoundException;
import recommendation.model.JobSearchCriteria;
import recommendation.service.RecommendationOrchestrator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecommendationOrchestrator orchestrator;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RecommendationController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Setup security context
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user@example.com");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void generateRecommendations_Success() throws Exception {
        // Arrange
        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .keyword("Java")
                .location("NY")
                .build();

        RecommendationResponse mockResponse = new RecommendationResponse(Collections.emptyList());
        when(orchestrator.generateRecommendations(eq(new UUID(0L, 1L)), any(JobSearchCriteria.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isOk());
    }

    @Test
    void generateRecommendations_ResumeNotFound_Returns404() throws Exception {
        // Arrange
        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(orchestrator.generateRecommendations(eq(new UUID(0L, 1L)), any(JobSearchCriteria.class)))
                .thenThrow(new ResumeNotFoundException("Resume not found"));

        // Act & Assert
        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Resume not found"));
    }

    @Test
    void generateRecommendations_NoJobsAvailable_Returns400() throws Exception {
        // Arrange
        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(orchestrator.generateRecommendations(eq(new UUID(0L, 1L)), any(JobSearchCriteria.class)))
                .thenThrow(new NoJobsAvailableException("No jobs matching"));

        // Act & Assert
        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No jobs matching"));
    }

    @Test
    void generateRecommendations_AiClientError_Returns502() throws Exception {
        // Arrange
        UserEntity mockUser = mock(UserEntity.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(mockUser));

        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(orchestrator.generateRecommendations(eq(new UUID(0L, 1L)), any(JobSearchCriteria.class)))
                .thenThrow(new AiClientException("Ollama error"));

        // Act & Assert
        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criteria)))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.error").value("Ollama error"));
    }
}
