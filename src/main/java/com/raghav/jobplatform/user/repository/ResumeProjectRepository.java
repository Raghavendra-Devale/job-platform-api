package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.ResumeProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeProjectRepository extends JpaRepository<ResumeProjectEntity, Long> {
}
