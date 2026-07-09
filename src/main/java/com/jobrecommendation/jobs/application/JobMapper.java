package com.jobrecommendation.jobs.application;

import com.jobrecommendation.infrastructure.ai.dto.JobDocument;
import com.jobrecommendation.jobs.api.dto.JobListResponse;
import com.jobrecommendation.jobs.api.dto.JobResponse;
import com.jobrecommendation.jobs.domain.JobEntity;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobResponse toJobResponse(JobEntity job) {
        return new JobResponse(
                job.getExternalJobId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getDescription(),
                job.getApplyUrl(),
                job.getRemote(),
                job.getTags()
        );
    }

    public JobListResponse toJobListResponse(JobEntity job) {
        return new JobListResponse(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getSource(),
                job.getRemote(),
                job.getTags(),
                job.getCreatedAt(),
                job.getSalary(),
                job.getJobType(),
                job.getApplyUrl()
        );
    }

    public JobDocument toJobDocument(JobEntity job) {
        return JobDocument.builder()
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .description(job.getDescription())
                .applyUrl(job.getApplyUrl())
                .employmentType(job.getJobType())
                .build();
    }
}
