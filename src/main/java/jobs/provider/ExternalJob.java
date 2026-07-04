package jobs.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalJob {
    private String title;
    private String company;
    private String location;
    private String description;
    private String applyUrl;
    private Boolean remote;
    private String salary;
    private String jobType;
    private String source;
}
