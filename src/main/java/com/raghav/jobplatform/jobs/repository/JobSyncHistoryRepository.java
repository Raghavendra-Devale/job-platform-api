package com.raghav.jobplatform.jobs.repository;

import com.raghav.jobplatform.jobs.entity.JobSyncHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobSyncHistoryRepository extends JpaRepository<JobSyncHistoryEntity, Long> {
}
