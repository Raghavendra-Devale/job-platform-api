package com.jobrecommendation.user.application;

import com.jobrecommendation.user.domain.ActivityLogEntity;
import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.repository.ActivityLogRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(UserEntity user, String activityType, String description) {
        ActivityLogEntity log = ActivityLogEntity.builder()
                .user(user)
                .activityType(activityType)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        activityLogRepository.save(log);
    }

    public java.util.List<ActivityLogEntity> getRecentActivities(UserEntity user, org.springframework.data.domain.Pageable pageable) {
        return activityLogRepository.findByUser(user, pageable);
    }
}
