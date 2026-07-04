package jobs.provider.remoteok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoteOKJob(
    String id,
    String company,
    String position,
    String description,
    String location,
    List<String> tags,
    String date,
    String url,
    String legal
) {}
