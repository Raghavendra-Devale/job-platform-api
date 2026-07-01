package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.ActivityLogEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {
    List<ActivityLogEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    List<ActivityLogEntity> findByUser(UserEntity user, Pageable pageable);
}
