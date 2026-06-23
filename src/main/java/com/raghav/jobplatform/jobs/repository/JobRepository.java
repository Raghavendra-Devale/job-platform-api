package com.raghav.jobplatform.jobs.repository;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long>, JpaSpecificationExecutor<JobEntity> {
    boolean existsByExternalJobId(String externalJobId);

    Page<JobEntity> findAll(Pageable pageable);

    Page<JobEntity> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}
