// package com.raghav.jobplatform.common.ai;

// import com.raghav.jobplatform.common.ai.dto.ResumeIntelligenceResponse;
// import com.raghav.jobplatform.common.ai.dto.ResumeProcessRequest;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.reactive.function.client.WebClient;
// import
// org.springframework.web.reactive.function.client.WebClientResponseException;
// import reactor.core.publisher.Mono;

// import java.time.Duration;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class AIClientTest {

// @Mock
// private WebClient webClient;

// @Mock
// private WebClient.RequestBodyUriSpec requestBodyUriSpec;

// @Mock
// private WebClient.RequestBodySpec requestBodySpec;

// @Mock
// private WebClient.ResponseSpec responseSpec;

// @Mock
// private Mono<ResumeIntelligenceResponse> responseMono;

// private AIProperties properties;
// private AIClient aiClient;

// @BeforeEach
// void setUp() {
// properties = new AIProperties();
// properties.setEnabled(true);
// properties.setBaseUrl("http://localhost:8001");
// properties.setConnectTimeout(Duration.ofSeconds(5));
// properties.setReadTimeout(Duration.ofSeconds(5));

// aiClient = new AIClient(webClient, properties);
// }

// @Test
// void processResume_Success() {
// // Arrange
// byte[] pdfBytes = "mock pdf content".getBytes();
// ResumeProcessRequest request = new ResumeProcessRequest(pdfBytes,
// "my_resume.pdf");

// ResumeIntelligenceResponse mockResponse = new ResumeIntelligenceResponse();
// mockResponse.setSuccess(true);
// mockResponse.setProcessingTimeMs(123.45);
// ResumeIntelligenceResponse.ResumeDetails details = new
// ResumeIntelligenceResponse.ResumeDetails();
// details.setExtractedText("John Doe");
// details.setEmbeddingModel("all-MiniLM-L6-v2");
// details.setEmbeddingDimensions(384);
// mockResponse.setResume(details);

// when(webClient.post()).thenReturn(requestBodyUriSpec);
// doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
// when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
// doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
// when(requestBodySpec.retrieve()).thenReturn(responseSpec);
// when(responseSpec.bodyToMono(ResumeIntelligenceResponse.class)).thenReturn(responseMono);
// when(responseMono.block()).thenReturn(mockResponse);

// // Act
// ResumeIntelligenceResponse result = aiClient.processResume(request);

// // Assert
// assertThat(result).isNotNull();
// assertThat(result.isSuccess()).isTrue();
// assertThat(result.getProcessingTimeMs()).isEqualTo(123.45);
// assertThat(result.getResume().getExtractedText()).isEqualTo("John Doe");

// verify(webClient).post();
// verify(requestBodyUriSpec).uri("/api/v1/resume/process");
// }

// @Test
// void processResume_ThrowsExceptionWhenDisabled() {
// // Arrange
// properties.setEnabled(false);
// ResumeProcessRequest request = new ResumeProcessRequest("pdf".getBytes(),
// "resume.pdf");

// // Act & Assert
// assertThatThrownBy(() -> aiClient.processResume(request))
// .isInstanceOf(AIException.class)
// .hasMessageContaining("AI Client is disabled");

// verifyNoInteractions(webClient);
// }

// @Test
// void processResume_ThrowsExceptionOnEmptyRequest() {
// // Arrange
// ResumeProcessRequest request = new ResumeProcessRequest(null, "resume.pdf");

// // Act & Assert
// assertThatThrownBy(() -> aiClient.processResume(request))
// .isInstanceOf(AIException.class)
// .hasMessageContaining("Resume file bytes cannot be empty");

// verifyNoInteractions(webClient);
// }

// @Test
// void processResume_HandlesWebClientResponseException() {
// // Arrange
// byte[] pdfBytes = "mock pdf content".getBytes();
// ResumeProcessRequest request = new ResumeProcessRequest(pdfBytes,
// "my_resume.pdf");

// WebClientResponseException exception = new WebClientResponseException(
// HttpStatus.INTERNAL_SERVER_ERROR.value(),
// "Internal Server Error",
// null,
// "{\"error\":\"Failed to parse PDF\"}".getBytes(),
// null
// );

// when(webClient.post()).thenReturn(requestBodyUriSpec);
// doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
// when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
// doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
// when(requestBodySpec.retrieve()).thenReturn(responseSpec);
// when(responseSpec.bodyToMono(ResumeIntelligenceResponse.class)).thenReturn(responseMono);
// when(responseMono.block()).thenThrow(exception);

// // Act & Assert
// assertThatThrownBy(() -> aiClient.processResume(request))
// .isInstanceOf(AIException.class)
// .hasMessageContaining("AI Engine returned error")
// .hasCause(exception);
// }

// @Test
// void processResume_HandlesGeneralConnectionException() {
// // Arrange
// byte[] pdfBytes = "mock pdf content".getBytes();
// ResumeProcessRequest request = new ResumeProcessRequest(pdfBytes,
// "my_resume.pdf");

// RuntimeException cause = new RuntimeException("Connection timeout");

// when(webClient.post()).thenReturn(requestBodyUriSpec);
// doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
// when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
// doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
// when(requestBodySpec.retrieve()).thenReturn(responseSpec);
// when(responseSpec.bodyToMono(ResumeIntelligenceResponse.class)).thenReturn(responseMono);
// when(responseMono.block()).thenThrow(cause);

// // Act & Assert
// assertThatThrownBy(() -> aiClient.processResume(request))
// .isInstanceOf(AIException.class)
// .hasMessageContaining("Failed to communicate with AI Engine")
// .hasCause(cause);
// }
// }
