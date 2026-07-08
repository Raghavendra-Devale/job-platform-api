package com.jobrecommendation.applications.domain.repository;

import com.jobrecommendation.applications.domain.SavedJobEntity;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJobEntity, Long> {
    List<SavedJobEntity> findByUserOrderBySavedAtDesc(UserEntity user);
    Optional<SavedJobEntity> findByUserAndJob(UserEntity user, JobEntity job);
    boolean existsByUserAndJob(UserEntity user, JobEntity job);
    long countByUser(UserEntity user);
}
