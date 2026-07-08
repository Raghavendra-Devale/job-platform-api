package com.jobrecommendation.resume.domain.repository;

import com.jobrecommendation.resume.domain.ResumeExperienceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperienceEntity, Long> {
}
