package com.jobrecommendation.recommendation.domain;

import com.jobrecommendation.jobs.domain.JobEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "recommendation_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private RecommendationRunEntity run;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @Column(nullable = false)
    private Double score;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "matching_skills", columnDefinition = "TEXT")
    private String matchingSkills;

    @Column(name = "missing_skills", columnDefinition = "TEXT")
    private String missingSkills;

    @Column(name = "rank_val", nullable = false)
    private Integer rank;
}
