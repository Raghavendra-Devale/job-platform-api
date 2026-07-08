package com.jobrecommendation.resume.domain.repository;

import com.jobrecommendation.resume.domain.ResumeSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSummaryRepository extends JpaRepository<ResumeSummaryEntity, Long> {
}
