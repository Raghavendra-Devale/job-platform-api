package jobs.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "providers")
public class JobProviderProperties {
    private Map<String, Boolean> enabled = new HashMap<>();

    public Map<String, Boolean> getEnabled() {
        return enabled;
    }

    public void setEnabled(Map<String, Boolean> enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled(String providerName) {
        return enabled.getOrDefault(providerName.toLowerCase(), true);
    }
}
