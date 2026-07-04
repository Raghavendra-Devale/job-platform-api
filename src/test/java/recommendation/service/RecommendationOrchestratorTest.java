package recommendation.service;

import ai.client.AiClient;
import ai.dto.RecommendationRequest;
import ai.dto.RecommendationResponse;
import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.dto.JobSearchRequest;
import com.raghav.jobplatform.jobs.dto.JobSearchResponse;
import com.raghav.jobplatform.jobs.model.Job;
import com.raghav.jobplatform.jobs.service.JobService;
import com.raghav.jobplatform.user.entity.ResumeEntity;
import com.raghav.jobplatform.user.service.ResumeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import recommendation.exception.AiClientException;
import recommendation.exception.NoJobsAvailableException;
import recommendation.exception.ResumeNotFoundException;
import recommendation.mapper.RecommendationMapper;
import recommendation.model.JobSearchCriteria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationOrchestratorTest {

    @Mock
    private ResumeService resumeService;

    @Mock
    private JobService jobService;

    @Mock
    private AiClient aiClient;

    @Mock
    private RecommendationMapper recommendationMapper;

    private RecommendationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new RecommendationOrchestrator(
                resumeService,
                jobService,
                aiClient,
                recommendationMapper
        );
    }

    @Test
    void generateRecommendations_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .keyword("Java")
                .location("New York")
                .remote(true)
                .page(0)
                .size(10)
                .build();

        ResumeEntity mockResume = mock(ResumeEntity.class);
        when(resumeService.getLatestParsedResume(userId)).thenReturn(mockResume);

        JobListResponse listJob = mock(JobListResponse.class);
        when(listJob.id()).thenReturn(1L);
        JobSearchResponse searchResponse = new JobSearchResponse(
                List.of(listJob),
                Collections.emptyMap(),
                1L, 1, 10, 0
        );
        when(jobService.searchJobs(any(JobSearchRequest.class), eq(0), eq(10))).thenReturn(searchResponse);

        JobResponse detailJob = mock(JobResponse.class);
        when(detailJob.title()).thenReturn("Java Dev");
        when(detailJob.company()).thenReturn("Google");
        when(detailJob.location()).thenReturn("New York");
        when(detailJob.description()).thenReturn("Code in Java");
        when(detailJob.applyUrl()).thenReturn("http://apply");
        when(detailJob.remote()).thenReturn(true);
        when(jobService.getJobById(1L)).thenReturn(detailJob);

        Job expectedJob = new Job(
                "Java Dev",
                "Google",
                "New York",
                "Code in Java",
                "http://apply",
                true,
                null,
                null,
                null
        );

        RecommendationRequest mockRequest = mock(RecommendationRequest.class);
        when(recommendationMapper.toRequest(mockResume, List.of(expectedJob))).thenReturn(mockRequest);

        RecommendationResponse mockResponse = mock(RecommendationResponse.class);
        when(aiClient.generateRecommendations(mockRequest)).thenReturn(mockResponse);

        // Act
        RecommendationResponse result = orchestrator.generateRecommendations(userId, criteria);

        // Assert
        assertThat(result).isSameAs(mockResponse);
        verify(resumeService).getLatestParsedResume(userId);
        verify(jobService).searchJobs(any(JobSearchRequest.class), eq(0), eq(10));
        verify(jobService).getJobById(1L);
        verify(recommendationMapper).toRequest(mockResume, List.of(expectedJob));
        verify(aiClient).generateRecommendations(mockRequest);
    }

    @Test
    void generateRecommendations_ThrowsResumeNotFoundException() {
        // Arrange
        UUID userId = UUID.randomUUID();
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(resumeService.getLatestParsedResume(userId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatThrownBy(() -> orchestrator.generateRecommendations(userId, criteria))
                .isInstanceOf(ResumeNotFoundException.class)
                .hasMessageContaining("Latest parsed resume not found");

        verifyNoInteractions(jobService);
        verifyNoInteractions(recommendationMapper);
        verifyNoInteractions(aiClient);
    }

    @Test
    void generateRecommendations_ThrowsNoJobsAvailableException_WhenSearchResponseEmpty() {
        // Arrange
        UUID userId = UUID.randomUUID();
        JobSearchCriteria criteria = JobSearchCriteria.builder().page(0).size(10).build();

        ResumeEntity mockResume = mock(ResumeEntity.class);
        when(resumeService.getLatestParsedResume(userId)).thenReturn(mockResume);

        JobSearchResponse emptyResponse = new JobSearchResponse(
                Collections.emptyList(),
                Collections.emptyMap(),
                0L, 0, 10, 0
        );
        when(jobService.searchJobs(any(JobSearchRequest.class), eq(0), eq(10))).thenReturn(emptyResponse);

        // Act & Assert
        assertThatThrownBy(() -> orchestrator.generateRecommendations(userId, criteria))
                .isInstanceOf(NoJobsAvailableException.class)
                .hasMessageContaining("No jobs found matching criteria");

        verify(resumeService).getLatestParsedResume(userId);
        verify(jobService).searchJobs(any(JobSearchRequest.class), eq(0), eq(10));
        verifyNoInteractions(recommendationMapper);
        verifyNoInteractions(aiClient);
    }

    @Test
    void generateRecommendations_ThrowsAiClientException_WhenAiClientFails() {
        // Arrange
        UUID userId = UUID.randomUUID();
        JobSearchCriteria criteria = JobSearchCriteria.builder().page(0).size(10).build();

        ResumeEntity mockResume = mock(ResumeEntity.class);
        when(resumeService.getLatestParsedResume(userId)).thenReturn(mockResume);

        JobListResponse listJob = mock(JobListResponse.class);
        when(listJob.id()).thenReturn(1L);
        JobSearchResponse searchResponse = new JobSearchResponse(
                List.of(listJob),
                Collections.emptyMap(),
                1L, 1, 10, 0
        );
        when(jobService.searchJobs(any(JobSearchRequest.class), eq(0), eq(10))).thenReturn(searchResponse);

        JobResponse detailJob = mock(JobResponse.class);
        when(detailJob.title()).thenReturn("Java Dev");
        when(detailJob.company()).thenReturn("Google");
        when(detailJob.location()).thenReturn("New York");
        when(detailJob.description()).thenReturn("Code in Java");
        when(detailJob.applyUrl()).thenReturn("http://apply");
        when(detailJob.remote()).thenReturn(true);
        when(jobService.getJobById(1L)).thenReturn(detailJob);

        Job expectedJob = new Job(
                "Java Dev",
                "Google",
                "New York",
                "Code in Java",
                "http://apply",
                true,
                null,
                null,
                null
        );

        RecommendationRequest mockRequest = mock(RecommendationRequest.class);
        when(recommendationMapper.toRequest(mockResume, List.of(expectedJob))).thenReturn(mockRequest);

        when(aiClient.generateRecommendations(mockRequest)).thenThrow(new RuntimeException("Ollama down"));

        // Act & Assert
        assertThatThrownBy(() -> orchestrator.generateRecommendations(userId, criteria))
                .isInstanceOf(AiClientException.class)
                .hasMessageContaining("failure communicating with AI client");
    }
}
