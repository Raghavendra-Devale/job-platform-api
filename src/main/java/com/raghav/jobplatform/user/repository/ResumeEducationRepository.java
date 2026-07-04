package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.ResumeEducationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducationEntity, Long> {
}
