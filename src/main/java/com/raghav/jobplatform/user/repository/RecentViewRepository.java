package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import com.raghav.jobplatform.user.entity.RecentViewEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentViewRepository extends JpaRepository<RecentViewEntity, Long> {
    List<RecentViewEntity> findTop10ByUserOrderByViewedAtDesc(UserEntity user);
    Optional<RecentViewEntity> findByUserAndJob(UserEntity user, JobEntity job);
}
