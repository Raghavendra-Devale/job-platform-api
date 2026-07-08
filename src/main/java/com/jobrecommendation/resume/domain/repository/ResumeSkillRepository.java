package com.jobrecommendation.resume.domain.repository;

import com.jobrecommendation.resume.domain.ResumeSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSkillRepository extends JpaRepository<ResumeSkillEntity, Long> {
}
