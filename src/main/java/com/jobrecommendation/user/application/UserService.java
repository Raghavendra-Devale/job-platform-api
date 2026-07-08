package com.jobrecommendation.user.application;

import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.user.domain.RecentViewEntity;
import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.repository.RecentViewRepository;
import com.jobrecommendation.user.domain.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RecentViewRepository recentViewRepository;

    public Optional<UserEntity> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UserEntity> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }

    public void recordRecentView(UserEntity user, JobEntity job) {
        RecentViewEntity recent = recentViewRepository.findByUserAndJob(user, job)
                .orElseGet(() -> RecentViewEntity.builder().user(user).job(job).build());
        recentViewRepository.save(recent);
    }
}
