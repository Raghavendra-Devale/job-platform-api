package jobs.service;

import ai.dto.JobDocument;
import jobs.dto.ExternalJob;
import jobs.mapper.JobNormalizer;
import jobs.provider.JobProvider;
import jobs.provider.JobProviderFactory;
import jobs.provider.JobSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import recommendation.model.JobSearchCriteria;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobProviderFactory providerFactory;

    @Mock
    private JobNormalizer normalizer;

    @InjectMocks
    private JobService jobService;

    private JobSearchCriteria criteria;

    @BeforeEach
    void setUp() {
        criteria = JobSearchCriteria.builder().keyword("Java").build();
    }

    @Test
    void searchJobs_OneProvider_Success() {
        // Arrange
        JobProvider provider = mock(JobProvider.class);
        when(providerFactory.getProviders()).thenReturn(List.of(provider));

        ExternalJob externalJob = ExternalJob.builder().title("Java Dev").build();
        when(provider.searchJobs(criteria)).thenReturn(JobSearchResult.success(List.of(externalJob)));

        JobDocument normalizedDoc = JobDocument.builder().title("Java Dev").build();
        when(normalizer.normalize(externalJob)).thenReturn(normalizedDoc);

        // Act
        List<JobDocument> result = jobService.searchJobs(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Dev");
        verify(provider, times(1)).searchJobs(criteria);
        verify(normalizer, times(1)).normalize(externalJob);
    }

    @Test
    void searchJobs_MultipleProviders_Success() {
        // Arrange
        JobProvider provider1 = mock(JobProvider.class);
        JobProvider provider2 = mock(JobProvider.class);
        when(providerFactory.getProviders()).thenReturn(List.of(provider1, provider2));

        ExternalJob job1 = ExternalJob.builder().title("Java Dev 1").build();
        ExternalJob job2 = ExternalJob.builder().title("Java Dev 2").build();

        when(provider1.searchJobs(criteria)).thenReturn(JobSearchResult.success(List.of(job1)));
        when(provider2.searchJobs(criteria)).thenReturn(JobSearchResult.success(List.of(job2)));

        JobDocument doc1 = JobDocument.builder().title("Java Dev 1").build();
        JobDocument doc2 = JobDocument.builder().title("Java Dev 2").build();

        when(normalizer.normalize(job1)).thenReturn(doc1);
        when(normalizer.normalize(job2)).thenReturn(doc2);

        // Act
        List<JobDocument> result = jobService.searchJobs(criteria);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Dev 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Java Dev 2");
    }

    @Test
    void searchJobs_ProviderFailure_Ignored() {
        // Arrange
        JobProvider successfulProvider = mock(JobProvider.class);
        JobProvider failedProvider = mock(JobProvider.class);
        when(providerFactory.getProviders()).thenReturn(List.of(successfulProvider, failedProvider));

        ExternalJob job = ExternalJob.builder().title("Java Dev").build();
        when(successfulProvider.searchJobs(criteria)).thenReturn(JobSearchResult.success(List.of(job)));
        when(failedProvider.searchJobs(criteria)).thenReturn(JobSearchResult.failure("Timeout error"));

        JobDocument doc = JobDocument.builder().title("Java Dev").build();
        when(normalizer.normalize(job)).thenReturn(doc);

        // Act
        List<JobDocument> result = jobService.searchJobs(criteria);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Java Dev");
    }

    @Test
    void searchJobs_EmptyResult() {
        // Arrange
        JobProvider provider = mock(JobProvider.class);
        when(providerFactory.getProviders()).thenReturn(List.of(provider));
        when(provider.searchJobs(criteria)).thenReturn(JobSearchResult.success(Collections.emptyList()));

        // Act
        List<JobDocument> result = jobService.searchJobs(criteria);

        // Assert
        assertThat(result).isEmpty();
        verify(normalizer, never()).normalize(any());
    }
}
