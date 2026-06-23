package com.raghav.jobplatform.jobs.service;

import com.raghav.jobplatform.jobs.client.ExternalJobClient;
import com.raghav.jobplatform.jobs.dto.JobListResponse;
import com.raghav.jobplatform.jobs.dto.JobResponse;
import com.raghav.jobplatform.jobs.dto.JobSearchRequest;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import com.raghav.jobplatform.jobs.specification.JobSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class JobService {

    private final JobProvider jobProvider;
    private final JobRepository jobRepository;

    public JobService(
            JobProvider jobProvider,
            JobRepository jobRepository) {
        this.jobProvider = jobProvider;
        this.jobRepository = jobRepository;
    }

    public List<JobResponse> searchJobs(String keyword) {

        return jobProvider.searchJobs(keyword);
    }

    public Page<JobListResponse> getJobs(int page, int size) {
        return jobRepository.findAll(
                PageRequest.of(page, size)
        )
                .map(job -> new JobListResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getSource()
                ));
    }

    public Page<JobListResponse> searchJobs(
            JobSearchRequest request,
            int page,
            int size
    ) {

        var specification =
                JobSpecification.hasKeyword(
                                request.keyword()
                        )
                        .and(
                                JobSpecification.hasLocation(
                                        request.location()
                                )
                        );

        return jobRepository.findAll(
                        specification,
                        PageRequest.of(page, size)
                )
                .map(job -> new JobListResponse(
                        job.getId(),
                        job.getTitle(),
                        job.getCompany(),
                        job.getLocation(),
                        job.getSource()
                ));
    }

    public JobResponse getJobById(Long id) {

        var job = jobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Job not found with id: " + id
                ));

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

}