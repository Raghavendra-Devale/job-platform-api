package com.jobrecommendation.applications.domain.repository;

import com.jobrecommendation.applications.domain.JobApplicationEntity;
import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.user.domain.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.jobrecommendation.resume.domain.ResumeEntity;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplicationEntity, Long> {
    List<JobApplicationEntity> findByUserOrderByAppliedAtDesc(UserEntity user);
    Optional<JobApplicationEntity> findByUserAndJob(UserEntity user, JobEntity job);
    long countByUser(UserEntity user);
    long countByUserAndStatus(UserEntity user, String status);

    @Modifying
    @Query("update JobApplicationEntity j set j.resume = null where j.resume = :resume")
    void nullifyResumeAssociation(@Param("resume") ResumeEntity resume);
}
