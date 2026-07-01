package com.raghav.jobplatform.user.service;

import com.raghav.jobplatform.user.entity.ActivityLogEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import com.raghav.jobplatform.user.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
}
