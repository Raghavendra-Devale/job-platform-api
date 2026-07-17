package com.jobrecommendation.recommendation.domain.repository;

import com.jobrecommendation.recommendation.domain.RecommendationRunEntity;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRunRepository extends JpaRepository<RecommendationRunEntity, Long> {
    Optional<RecommendationRunEntity> findFirstByUserOrderByGeneratedAtDesc(UserEntity user);
    List<RecommendationRunEntity> findByUserOrderByGeneratedAtDesc(UserEntity user);
}
