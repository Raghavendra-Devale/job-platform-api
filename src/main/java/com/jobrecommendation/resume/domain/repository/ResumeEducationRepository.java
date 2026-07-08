package com.jobrecommendation.resume.domain.repository;

import com.jobrecommendation.resume.domain.ResumeEducationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeEducationRepository extends JpaRepository<ResumeEducationEntity, Long> {
}
