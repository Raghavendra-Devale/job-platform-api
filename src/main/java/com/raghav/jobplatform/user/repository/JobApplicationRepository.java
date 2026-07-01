package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.user.entity.JobApplicationEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {
    List<JobApplicationEntity> findByUserOrderByAppliedAtDesc(UserEntity user);
    Optional<JobApplicationEntity> findByUserAndJob(UserEntity user, JobEntity job);
    long countByUser(UserEntity user);
    long countByUserAndStatus(UserEntity user, String status);
}
