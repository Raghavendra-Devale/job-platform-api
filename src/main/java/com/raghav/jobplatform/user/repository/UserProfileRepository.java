package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.UserProfileEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
    Optional<UserProfileEntity> findByUser(UserEntity user);
}
