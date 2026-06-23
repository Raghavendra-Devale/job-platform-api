package com.raghav.jobplatform.jobs.service;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import com.raghav.jobplatform.jobs.provider.arbeitnow.ArbeitnowProvider;
import com.raghav.jobplatform.jobs.provider.arbeitnow.ArbeitnowResponse;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class JobSyncService {
    private final ArbeitnowProvider arbeitnowProvider;
    private final JobRepository jobRepository;

    public void syncJobs() {
        var jobs = arbeitnowProvider.searchJobs("");
        var entities = jobs.stream()
                .filter(job -> !jobRepository.existsByExternalJobId(job.slug()))
                .map(job -> {
                    JobEntity entity = new JobEntity();
                    entity.setTitle(job.title());
                    entity.setCompany(job.company());
                    entity.setLocation(job.location());
                    entity.setDescription(job.description());
                    entity.setExternalJobId(job.slug());
                    entity.setSource("ARBEITNOW");
                    entity.setCreatedAt(LocalDateTime.now());
                    entity.setDescription(job.description());
                    entity.setApplyUrl(job.applyUrl());
                    entity.setRemote(job.remote());
                    entity.setTags(job.tags());
                    return entity;
                })
                .toList();
        System.out.println("Jobs Synced: " + entities.size());
        jobRepository.saveAll(entities);
    }

}
