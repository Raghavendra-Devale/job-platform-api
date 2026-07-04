package jobs.provider;

import jobs.dto.ExternalJob;
import java.util.List;

public record JobSearchResult(
    List<ExternalJob> jobs,
    boolean success,
    String errorMessage
) {
    public static JobSearchResult success(List<ExternalJob> jobs) {
        return new JobSearchResult(jobs, true, null);
    }

    public static JobSearchResult failure(String errorMessage) {
        return new JobSearchResult(List.of(), false, errorMessage);
    }
}
