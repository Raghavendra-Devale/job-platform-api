package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.ResumeSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeSummaryRepository extends JpaRepository<ResumeSummaryEntity, Long> {
}
