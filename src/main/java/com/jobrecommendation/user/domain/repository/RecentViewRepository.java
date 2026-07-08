package com.jobrecommendation.user.domain.repository;

import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.user.domain.RecentViewEntity;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecentViewRepository extends JpaRepository<RecentViewEntity, Long> {
    List<RecentViewEntity> findTop10ByUserOrderByViewedAtDesc(UserEntity user);
    Optional<RecentViewEntity> findByUserAndJob(UserEntity user, JobEntity job);
}
