package com.jobrecommendation.resume.domain.repository;

import com.jobrecommendation.resume.domain.ResumeEntity;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    List<ResumeEntity> findByUserOrderByUpdatedAtDesc(UserEntity user);
    Optional<ResumeEntity> findByUserAndIsActiveTrue(UserEntity user);
    Optional<ResumeEntity> findByIdAndUser(Long id, UserEntity user);
    long countByUser(UserEntity user);
}
