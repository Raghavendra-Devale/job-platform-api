package com.raghav.jobplatform.jobs.repository;

import com.raghav.jobplatform.jobs.entity.JobEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<JobEntity, Long>, JpaSpecificationExecutor<JobEntity> {
        boolean existsBySourceAndExternalJobId(String source, String externalJobId);

        Optional<JobEntity> findBySourceAndExternalJobId(String source, String externalJobId);

        Page<JobEntity> findAll(Pageable pageable);

        Page<JobEntity> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

        @Query("SELECT j FROM JobEntity j WHERE " +
                        "(j.active IS NULL OR j.active = true) " +
                        "AND (:keyword = '' OR " +
                        " LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        " LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        " LOWER(j.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:location = '' OR " +
                        " LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
                        "AND (:provider = '' OR " +
                        " LOWER(j.source) = LOWER(:provider)) " +
                        "AND (:remote IS NULL OR j.remote = :remote) " +
                        "AND (:salaryMin IS NULL OR j.salaryMin >= :salaryMin) " +
                        "AND (:salaryMax IS NULL OR j.salaryMax <= :salaryMax) " +
                        "AND (:jobType = '' OR LOWER(j.jobType) LIKE LOWER(CONCAT('%', :jobType, '%'))) " +
                        "AND (:company = '' OR LOWER(j.company) LIKE LOWER(CONCAT('%', :company, '%'))) " +
                        "AND (:experience = '' OR " +
                        "  (:experience = 'JUNIOR' AND (LOWER(j.title) LIKE '%junior%' OR LOWER(j.title) LIKE '%entry%' OR LOWER(j.title) LIKE '%intern%' OR LOWER(j.title) LIKE '%associate%' OR LOWER(j.description) LIKE '%junior%' OR LOWER(j.description) LIKE '%entry%' OR LOWER(j.description) LIKE '%intern%')) OR " +
                        "  (:experience = 'SENIOR' AND (LOWER(j.title) LIKE '%senior%' OR LOWER(j.title) LIKE '%sr.%' OR LOWER(j.title) LIKE '%sr %' OR LOWER(j.description) LIKE '%senior%' OR LOWER(j.description) LIKE '%5+ years%')) OR " +
                        "  (:experience = 'LEAD' AND (LOWER(j.title) LIKE '%lead%' OR LOWER(j.title) LIKE '%principal%' OR LOWER(j.title) LIKE '%director%' OR LOWER(j.title) LIKE '%manager%' OR LOWER(j.description) LIKE '%lead%' OR LOWER(j.description) LIKE '%principal%')) OR " +
                        "  (:experience = 'MID' AND NOT (LOWER(j.title) LIKE '%junior%' OR LOWER(j.title) LIKE '%entry%' OR LOWER(j.title) LIKE '%intern%' OR LOWER(j.title) LIKE '%senior%' OR LOWER(j.title) LIKE '%sr.%' OR LOWER(j.title) LIKE '%lead%' OR LOWER(j.title) LIKE '%principal%' OR LOWER(j.title) LIKE '%director%'))" +
                        ") " +
                        "ORDER BY " +
                        "  (CASE WHEN :keyword = '' THEN 0 " +
                        "        WHEN LOWER(j.title) = LOWER(:keyword) THEN 100 " +
                        "        WHEN LOWER(j.title) LIKE LOWER(CONCAT(:keyword, '%')) THEN 50 " +
                        "        WHEN LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 25 " +
                        "        WHEN LOWER(j.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 10 " +
                        "        ELSE 0 END) DESC, " +
                        "  j.createdAt DESC")
        Page<JobEntity> searchJobsRanked(
                        @Param("keyword") String keyword,
                        @Param("location") String location,
                        @Param("provider") String provider,
                        @Param("remote") Boolean remote,
                        @Param("salaryMin") Integer salaryMin,
                        @Param("salaryMax") Integer salaryMax,
                        @Param("experience") String experience,
                        @Param("jobType") String jobType,
                        @Param("company") String company,
                        Pageable pageable);

        @Query("SELECT j FROM JobEntity j WHERE " +
                        "(j.active IS NULL OR j.active = true) " +
                        "AND (:keyword = '' OR " +
                        " LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        " LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        " LOWER(j.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
                        "AND (:location = '' OR " +
                        " LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
                        "AND (:provider = '' OR " +
                        " LOWER(j.source) = LOWER(:provider)) " +
                        "AND (:remote IS NULL OR j.remote = :remote) " +
                        "AND (:salaryMin IS NULL OR j.salaryMin >= :salaryMin) " +
                        "AND (:salaryMax IS NULL OR j.salaryMax <= :salaryMax) " +
                        "AND (:jobType = '' OR LOWER(j.jobType) LIKE LOWER(CONCAT('%', :jobType, '%'))) " +
                        "AND (:company = '' OR LOWER(j.company) LIKE LOWER(CONCAT('%', :company, '%'))) " +
                        "AND (:experience = '' OR " +
                        "  (:experience = 'JUNIOR' AND (LOWER(j.title) LIKE '%junior%' OR LOWER(j.title) LIKE '%entry%' OR LOWER(j.title) LIKE '%intern%' OR LOWER(j.title) LIKE '%associate%' OR LOWER(j.description) LIKE '%junior%' OR LOWER(j.description) LIKE '%entry%' OR LOWER(j.description) LIKE '%intern%')) OR " +
                        "  (:experience = 'SENIOR' AND (LOWER(j.title) LIKE '%senior%' OR LOWER(j.title) LIKE '%sr.%' OR LOWER(j.title) LIKE '%sr %' OR LOWER(j.description) LIKE '%senior%' OR LOWER(j.description) LIKE '%5+ years%')) OR " +
                        "  (:experience = 'LEAD' AND (LOWER(j.title) LIKE '%lead%' OR LOWER(j.title) LIKE '%principal%' OR LOWER(j.title) LIKE '%director%' OR LOWER(j.title) LIKE '%manager%' OR LOWER(j.description) LIKE '%lead%' OR LOWER(j.description) LIKE '%principal%')) OR " +
                        "  (:experience = 'MID' AND NOT (LOWER(j.title) LIKE '%junior%' OR LOWER(j.title) LIKE '%entry%' OR LOWER(j.title) LIKE '%intern%' OR LOWER(j.title) LIKE '%senior%' OR LOWER(j.title) LIKE '%sr.%' OR LOWER(j.title) LIKE '%lead%' OR LOWER(j.title) LIKE '%principal%' OR LOWER(j.title) LIKE '%director%'))" +
                        ") ")
        Page<JobEntity> searchJobs(
                        @Param("keyword") String keyword,
                        @Param("location") String location,
                        @Param("provider") String provider,
                        @Param("remote") Boolean remote,
                        @Param("salaryMin") Integer salaryMin,
                        @Param("salaryMax") Integer salaryMax,
                        @Param("experience") String experience,
                        @Param("jobType") String jobType,
                        @Param("company") String company,
                        Pageable pageable);

        @Query("SELECT DISTINCT j.source FROM JobEntity j WHERE j.source IS NOT NULL AND j.source != ''")
        List<String> findDistinctSources();

        @Query("SELECT DISTINCT j.location FROM JobEntity j WHERE j.location IS NOT NULL AND j.location != ''")
        List<String> findDistinctLocations();

        @Query("SELECT DISTINCT j.jobType FROM JobEntity j WHERE j.jobType IS NOT NULL AND j.jobType != ''")
        List<String> findDistinctJobTypes();

        @Modifying
        @Query("UPDATE JobEntity j SET j.active = false WHERE j.lastSeenAt < :threshold AND j.active = true")
        int deactivateOldJobs(@Param("threshold") LocalDateTime threshold);
}
