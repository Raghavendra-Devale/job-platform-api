package com.raghav.jobplatform.jobs.provider.remoteok;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

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