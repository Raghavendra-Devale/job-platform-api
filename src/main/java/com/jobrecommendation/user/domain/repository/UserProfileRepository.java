package com.jobrecommendation.user.domain.repository;

import com.jobrecommendation.user.domain.UserEntity;
import com.jobrecommendation.user.domain.UserProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
    Optional<UserProfileEntity> findByUser(UserEntity user);
}
