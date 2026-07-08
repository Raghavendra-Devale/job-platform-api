package com.jobrecommendation.jobs.provider.remoteok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteOkResponse {
    private Long id;
    private String position;
    private String company;
    private String location;
    private String description;
    private String url;
    private List<String> tags;
}
