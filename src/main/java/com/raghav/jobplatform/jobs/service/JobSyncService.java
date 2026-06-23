package com.raghav.jobplatform.jobs.service;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import com.raghav.jobplatform.jobs.provider.arbeitnow.ArbeitnowProvider;
import com.raghav.jobplatform.jobs.provider.arbeitnow.ArbeitnowResponse;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobSyncService {
    private final List<JobProvider> jobProviders;
    private final JobRepository jobRepository;

    public void syncJobs() {

        for (JobProvider provider : jobProviders) {

            var jobs = provider.searchJobs("");

            var entities = jobs.stream()
                    .filter(job ->
                            !jobRepository.existsByExternalJobId(
                                    job.slug()
                            )
                    )
                    .map(job -> JobEntity.builder()
                            .externalJobId(job.slug())
                            .title(job.title())
                            .company(job.company())
                            .location(job.location())
                            .description(job.description())
                            .applyUrl(job.applyUrl())
                            .remote(job.remote())
                            .tags(job.tags())
                            .source(provider.getProviderName())
                            .createdAt(LocalDateTime.now())
                            .build())
                    .toList();

            jobRepository.saveAll(entities);

            System.out.println(
                    provider.getProviderName()
                            + " Jobs Synced: "
                            + entities.size()
            );
        }
    }

}
