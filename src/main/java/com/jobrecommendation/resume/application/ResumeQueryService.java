package com.jobrecommendation.resume.application;

import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.resume.domain.repository.ResumeRepository;
import com.jobrecommendation.user.application.UserService;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeQueryService {

    private final ResumeRepository resumeRepository;
    private final UserService userService;

    public ResumeEntity getLatestParsedResume(UUID userId) {
        Long dbUserId = userId.getLeastSignificantBits() != 0 ? userId.getLeastSignificantBits() : Math.abs(userId.getMostSignificantBits());
        UserEntity user = userService.findUserById(dbUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + dbUserId));
        
        return resumeRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new IllegalArgumentException("No active resume found for user: " + dbUserId));
    }

    public Optional<ResumeEntity> getActiveResumeForUser(UserEntity user) {
        return resumeRepository.findByUserAndIsActiveTrue(user);
    }

    public long getResumeCountForUser(UserEntity user) {
        return resumeRepository.countByUser(user);
    }
}
