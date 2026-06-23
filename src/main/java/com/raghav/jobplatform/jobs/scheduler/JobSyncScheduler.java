package com.raghav.jobplatform.jobs.scheduler;

import com.raghav.jobplatform.jobs.service.JobSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JobSyncScheduler {

    private final JobSyncService jobSyncService;

    @Scheduled(cron = "0 0 2 * * *")
    public void syncJobs() {

        System.out.println("Running scheduled sync...");

        jobSyncService.syncJobs();
    }
}