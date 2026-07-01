package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.UserPreferencesEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferencesEntity, Long> {
    Optional<UserPreferencesEntity> findByUser(UserEntity user);
}
