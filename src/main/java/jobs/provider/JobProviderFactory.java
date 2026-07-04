package jobs.provider;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobProviderFactory {

    private final List<JobProvider> allProviders;
    private final JobProviderProperties properties;

    public JobProviderFactory(List<JobProvider> allProviders, JobProviderProperties properties) {
        this.allProviders = allProviders;
        this.properties = properties;
    }

    public List<JobProvider> getProviders() {
        return allProviders.stream()
                .filter(provider -> properties.isEnabled(provider.getProviderName()))
                .toList();
    }
}
