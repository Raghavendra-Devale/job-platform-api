package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.ResumeEntity;
import com.raghav.jobplatform.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    List<ResumeEntity> findByUserOrderByUpdatedAtDesc(UserEntity user);
    Optional<ResumeEntity> findByUserAndIsActiveTrue(UserEntity user);
    Optional<ResumeEntity> findByIdAndUser(Long id, UserEntity user);
    long countByUser(UserEntity user);
}
