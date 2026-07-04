package jobs.provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobProviderFactoryTest {

    @Mock
    private JobProviderProperties properties;

    private JobProvider provider1;
    private JobProvider provider2;

    private JobProviderFactory factory;

    @BeforeEach
    void setUp() {
        provider1 = mock(JobProvider.class);
        when(provider1.getProviderName()).thenReturn("Arbeitnow");

        provider2 = mock(JobProvider.class);
        when(provider2.getProviderName()).thenReturn("RemoteOK");

        factory = new JobProviderFactory(List.of(provider1, provider2), properties);
    }

    @Test
    void getProviders_ReturnsEnabledProviders() {
        // Arrange
        when(properties.isEnabled("Arbeitnow")).thenReturn(true);
        when(properties.isEnabled("RemoteOK")).thenReturn(true);

        // Act
        List<JobProvider> result = factory.getProviders();

        // Assert
        assertThat(result).containsExactly(provider1, provider2);
    }

    @Test
    void getProviders_ExcludesDisabledProviders() {
        // Arrange
        when(properties.isEnabled("Arbeitnow")).thenReturn(true);
        when(properties.isEnabled("RemoteOK")).thenReturn(false);

        // Act
        List<JobProvider> result = factory.getProviders();

        // Assert
        assertThat(result).containsExactly(provider1);
    }
}
