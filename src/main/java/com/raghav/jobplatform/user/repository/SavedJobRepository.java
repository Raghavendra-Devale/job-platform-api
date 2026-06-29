package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.user.entity.SavedJobEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJobEntity, Long> {
    List<SavedJobEntity> findByUserOrderBySavedAtDesc(UserEntity user);
    Optional<SavedJobEntity> findByUserAndJob(UserEntity user, JobEntity job);
    boolean existsByUserAndJob(UserEntity user, JobEntity job);
}
