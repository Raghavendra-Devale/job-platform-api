package com.jobrecommendation.user.domain.repository;

import com.jobrecommendation.user.domain.ActivityLogEntity;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, Long> {
    List<ActivityLogEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    List<ActivityLogEntity> findByUser(UserEntity user, Pageable pageable);
}
