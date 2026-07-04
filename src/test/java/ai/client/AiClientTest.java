package ai.client;

import ai.dto.JobDocument;
import ai.dto.RecommendationRequest;
import ai.dto.RecommendationResponse;
import ai.dto.RecommendationItem;
import com.raghav.jobplatform.common.ai.AIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private Mono<RecommendationResponse> responseMono;

    private AiClient aiClient;

    @BeforeEach
    void setUp() {
        aiClient = new AiClient(webClient);
    }

    @Test
    void generateRecommendations_Success() {
        // Arrange
        RecommendationRequest request = RecommendationRequest.builder()
                .resumeText("Experienced software developer.")
                .jobs(List.of(JobDocument.builder()
                        .title("Java Dev")
                        .company("Google")
                        .description("Code in Java")
                        .applyUrl("http://apply")
                        .build()))
                .build();

        RecommendationResponse mockResponse = RecommendationResponse.builder()
                .recommendations(List.of(RecommendationItem.builder()
                        .title("Java Dev")
                        .company("Google")
                        .similarityScore(0.9)
                        .build()))
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RecommendationResponse.class)).thenReturn(responseMono);
        when(responseMono.block()).thenReturn(mockResponse);

        // Act
        RecommendationResponse result = aiClient.generateRecommendations(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRecommendations()).hasSize(1);
        assertThat(result.getRecommendations().get(0).getTitle()).isEqualTo("Java Dev");
        
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("/api/v1/recommendations/generate");
    }

    @Test
    void generateRecommendations_ThrowsExceptionOnNullRequest() {
        // Act & Assert
        assertThatThrownBy(() -> aiClient.generateRecommendations(null))
                .isInstanceOf(AIException.class)
                .hasMessageContaining("Request cannot be null");
        
        verifyNoInteractions(webClient);
    }

    @Test
    void generateRecommendations_HandlesTimeoutOrConnectionFailure() {
        // Arrange
        RecommendationRequest request = RecommendationRequest.builder()
                .resumeText("text")
                .jobs(List.of(JobDocument.builder().title("title").company("company").description("desc").applyUrl("url").build()))
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RecommendationResponse.class)).thenReturn(responseMono);
        
        // Mock a connection timeout / request exception
        WebClientRequestException mockRequestException = mock(WebClientRequestException.class);
        when(responseMono.block()).thenThrow(mockRequestException);

        // Act & Assert
        assertThatThrownBy(() -> aiClient.generateRecommendations(request))
                .isInstanceOf(AIException.class)
                .hasMessageContaining("AI Engine is unreachable");
    }

    @Test
    void generateRecommendations_Handles500Error() {
        // Arrange
        RecommendationRequest request = RecommendationRequest.builder()
                .resumeText("text")
                .jobs(List.of(JobDocument.builder().title("title").company("company").description("desc").applyUrl("url").build()))
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RecommendationResponse.class)).thenReturn(responseMono);
        
        // Mock a 500 status exception
        WebClientResponseException mockResponseException = WebClientResponseException.create(
                500, "Internal Server Error", null, null, null);
        when(responseMono.block()).thenThrow(mockResponseException);

        // Act & Assert
        assertThatThrownBy(() -> aiClient.generateRecommendations(request))
                .isInstanceOf(AIException.class)
                .hasMessageContaining("AI Engine returned error");
    }

    @Test
    void generateRecommendations_HandlesMalformedOrNullResponse() {
        // Arrange
        RecommendationRequest request = RecommendationRequest.builder()
                .resumeText("text")
                .jobs(List.of(JobDocument.builder().title("title").company("company").description("desc").applyUrl("url").build()))
                .build();

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(RecommendationResponse.class)).thenReturn(responseMono);
        
        // Return null
        when(responseMono.block()).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> aiClient.generateRecommendations(request))
                .isInstanceOf(AIException.class)
                .hasMessageContaining("Received null response");
    }
}
