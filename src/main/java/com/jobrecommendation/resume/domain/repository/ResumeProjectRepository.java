package com.jobrecommendation.resume.domain.repository;

import com.jobrecommendation.resume.domain.ResumeProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeProjectRepository extends JpaRepository<ResumeProjectEntity, Long> {
}
