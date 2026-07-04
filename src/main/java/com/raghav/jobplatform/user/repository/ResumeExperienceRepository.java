package com.raghav.jobplatform.user.repository;

import com.raghav.jobplatform.user.entity.ResumeExperienceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeExperienceRepository extends JpaRepository<ResumeExperienceEntity, Long> {
}
