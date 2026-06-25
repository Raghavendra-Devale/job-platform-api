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
           "(:keyword = '' OR " +
           " LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(j.tags) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:location = '' OR " +
           " LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:provider = '' OR " +
           " LOWER(j.source) = LOWER(:provider)) " +
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
            Pageable pageable
    );

    @Query("SELECT DISTINCT j.source FROM JobEntity j WHERE j.source IS NOT NULL AND j.source != ''")
    List<String> findDistinctSources();

    @Query("SELECT DISTINCT j.location FROM JobEntity j WHERE j.location IS NOT NULL AND j.location != ''")
    List<String> findDistinctLocations();

    @Modifying
    @Query("UPDATE JobEntity j SET j.active = false WHERE j.lastSeenAt < :threshold AND j.active = true")
    int deactivateOldJobs(@Param("threshold") LocalDateTime threshold);
}
