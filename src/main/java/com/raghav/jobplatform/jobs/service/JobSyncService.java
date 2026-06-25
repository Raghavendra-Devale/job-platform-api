package com.raghav.jobplatform.jobs.service;

import com.raghav.jobplatform.jobs.dto.ProviderSyncSummary;
import com.raghav.jobplatform.jobs.dto.SyncSummaryResponse;
import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.jobs.entity.JobSyncHistoryEntity;
import com.raghav.jobplatform.jobs.provider.JobProvider;
import com.raghav.jobplatform.jobs.repository.JobRepository;
import com.raghav.jobplatform.jobs.repository.JobSyncHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSyncService {
    private final List<JobProvider> jobProviders;
    private final JobRepository jobRepository;
    private final JobSyncHistoryRepository jobSyncHistoryRepository;

    @Transactional
    public SyncSummaryResponse syncJobs() {
        List<ProviderSyncSummary> summaries = new ArrayList<>();

        for (JobProvider provider : jobProviders) {
            String source = provider.getProviderName();
            LocalDateTime startedAt = LocalDateTime.now();
            int fetched = 0;
            int inserted = 0;
            int skipped = 0;
            String status = "SUCCESS";

            try {
                var jobs = provider.fetchAllJobs();
                fetched = jobs.size();

                for (var job : jobs) {
                    var existingOpt = jobRepository.findBySourceAndExternalJobId(source, job.slug());
                    if (existingOpt.isPresent()) {
                        JobEntity entity = existingOpt.get();
                        entity.setTitle(job.title());
                        entity.setDescription(job.description());
                        entity.setApplyUrl(job.applyUrl());
                        entity.setTags(job.tags());
                        entity.setLocation(job.location());
                        entity.setLastSeenAt(LocalDateTime.now());
                        entity.setActive(true);
                        jobRepository.save(entity);
                        skipped++;
                    } else {
                        JobEntity entity = new JobEntity();
                        entity.setTitle(job.title());
                        entity.setCompany(job.company());
                        entity.setLocation(job.location());
                        entity.setDescription(job.description());
                        entity.setExternalJobId(job.slug());
                        entity.setSource(source);
                        entity.setApplyUrl(job.applyUrl());
                        entity.setRemote(job.remote());
                        entity.setTags(job.tags());
                        
                        // Salary Support
                        entity.setSalaryMin(null);
                        entity.setSalaryMax(null);
                        entity.setCurrency(null);

                        // Job Expiration
                        entity.setCreatedAt(LocalDateTime.now());
                        entity.setLastSeenAt(LocalDateTime.now());
                        entity.setActive(true);

                        // Provider Metadata
                        entity.setProvider(source);
                        entity.setProviderUrl(job.applyUrl());
                        entity.setProviderJobId(job.slug());

                        jobRepository.save(entity);
                        inserted++;
                    }
                }

                log.info("Provider: {}", source);
                log.info("Fetched: {}", fetched);
                log.info("Inserted: {}", inserted);
                log.info("Skipped: {}", skipped);

                summaries.add(new ProviderSyncSummary(source, fetched, inserted, skipped));
            } catch (Exception ex) {
                status = "FAILED";
                log.error("Failed to sync jobs from provider: " + source, ex);
            } finally {
                // Record execution in sync history table
                try {
                    JobSyncHistoryEntity history = JobSyncHistoryEntity.builder()
                            .provider(source)
                            .startedAt(startedAt)
                            .completedAt(LocalDateTime.now())
                            .fetched(fetched)
                            .inserted(inserted)
                            .skipped(skipped)
                            .status(status)
                            .build();
                    jobSyncHistoryRepository.save(history);
                } catch (Exception historyEx) {
                    log.error("Failed to save sync history for provider: " + source, historyEx);
                }
            }
        }

        // Deactivate jobs missing for 30 days
        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(30);
            int deactivated = jobRepository.deactivateOldJobs(threshold);
            log.info("Deactivated {} jobs that haven't been seen in 30 days", deactivated);
        } catch (Exception ex) {
            log.error("Failed to deactivate old jobs", ex);
        }

        return new SyncSummaryResponse(summaries);
    }
}
