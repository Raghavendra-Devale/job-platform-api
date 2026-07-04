package jobs.provider.remoteok;

import jobs.dto.ExternalJob;
import jobs.provider.JobSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import recommendation.model.JobSearchCriteria;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoteOKProviderTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private RemoteOKProvider provider;

    @BeforeEach
    void setUp() {
        provider = new RemoteOKProvider(restClient);
    }

    @Test
    void searchJobs_SuccessfulFetch() {
        // Arrange
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .keyword("Java")
                .build();

        RemoteOKJob legal = new RemoteOKJob(null, null, null, null, null, null, null, null, "disclaimer");
        RemoteOKJob job = new RemoteOKJob(
                "12345",
                "Google",
                "Java Software Engineer",
                "Java Dev Description",
                "Worldwide",
                Collections.emptyList(),
                "2026-07-04T12:00:00Z",
                "https://remoteok.com/apply",
                null
        );
        List<RemoteOKJob> mockResponse = List.of(legal, job);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(mockResponse);

        // Act
        JobSearchResult result = provider.searchJobs(criteria);

        // Assert
        assertThat(result.success()).isTrue();
        assertThat(result.jobs()).hasSize(1);
        assertThat(result.jobs().get(0).getTitle()).isEqualTo("Java Software Engineer");
        assertThat(result.jobs().get(0).getCompany()).isEqualTo("Google");
        assertThat(result.jobs().get(0).getProviderJobId()).isEqualTo("12345");
    }

    @Test
    void searchJobs_Timeout() {
        // Arrange
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new ResourceAccessException("Connect timeout"));

        // Act
        JobSearchResult result = provider.searchJobs(criteria);

        // Assert
        assertThat(result.success()).isFalse();
        assertThat(result.jobs()).isEmpty();
        assertThat(result.errorMessage()).contains("timeout or connection failure");
    }

    @Test
    void searchJobs_EmptyResponse() {
        // Arrange
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(Collections.emptyList());

        // Act
        JobSearchResult result = provider.searchJobs(criteria);

        // Assert
        assertThat(result.success()).isFalse();
        assertThat(result.jobs()).isEmpty();
        assertThat(result.errorMessage()).contains("Empty response");
    }

    @Test
    void searchJobs_InvalidResponse() {
        // Arrange
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(mock(RestClientResponseException.class));

        // Act
        JobSearchResult result = provider.searchJobs(criteria);

        // Assert
        assertThat(result.success()).isFalse();
        assertThat(result.jobs()).isEmpty();
        assertThat(result.errorMessage()).contains("returned error status");
    }
}
