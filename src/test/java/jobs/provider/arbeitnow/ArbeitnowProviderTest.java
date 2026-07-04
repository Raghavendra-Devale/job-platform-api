package jobs.provider.arbeitnow;

import com.raghav.jobplatform.jobs.provider.arbeitnow.ArbeitnowJob;
import com.raghav.jobplatform.jobs.provider.arbeitnow.ArbeitnowResponse;
import jobs.dto.ExternalJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import recommendation.model.JobSearchCriteria;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArbeitnowProviderTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private ArbeitnowProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ArbeitnowProvider(restClient);
    }

    @Test
    void searchJobs_Success() {
        // Arrange
        JobSearchCriteria criteria = JobSearchCriteria.builder()
                .keyword("Java")
                .build();

        ArbeitnowJob job = new ArbeitnowJob(
                "java-dev",
                "Google",
                "Java Dev Job",
                "NY",
                "Java Dev Job Description",
                "http://apply",
                true,
                Collections.emptyList()
        );
        ArbeitnowResponse mockResponse = new ArbeitnowResponse(List.of(job));

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ArbeitnowResponse.class)).thenReturn(mockResponse);

        // Act
        jobs.provider.JobSearchResult result = provider.searchJobs(criteria);

        // Assert
        assertThat(result.success()).isTrue();
        assertThat(result.jobs()).hasSize(1);
        assertThat(result.jobs().get(0).getTitle()).isEqualTo("Java Dev Job");
        assertThat(result.jobs().get(0).getCompany()).isEqualTo("Google");
        assertThat(result.jobs().get(0).getProviderJobId()).isEqualTo("java-dev");
    }

    @Test
    void searchJobs_Failure() {
        // Arrange
        JobSearchCriteria criteria = JobSearchCriteria.builder().build();

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ArbeitnowResponse.class)).thenThrow(new RuntimeException("API error"));

        // Act
        jobs.provider.JobSearchResult result = provider.searchJobs(criteria);

        // Assert
        assertThat(result.success()).isFalse();
        assertThat(result.jobs()).isEmpty();
    }
}
