package com.jobrecommendation.user.domain.repository;

import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.UserPreferencesEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferencesEntity, Long> {
    Optional<UserPreferencesEntity> findByUser(UserEntity user);
}
