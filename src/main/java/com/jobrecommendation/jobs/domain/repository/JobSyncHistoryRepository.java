package com.jobrecommendation.jobs.domain.repository;

import com.jobrecommendation.jobs.domain.JobSyncHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobSyncHistoryRepository extends JpaRepository<JobSyncHistoryEntity, Long> {
}
