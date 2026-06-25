package com.raghav.jobplatform.jobs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "jobs",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "source",
                "external_job_id"
            }
        )
    },
    indexes = {
        @Index(name = "idx_jobs_title", columnList = "title"),
        @Index(name = "idx_jobs_company", columnList = "company"),
        @Index(name = "idx_jobs_location", columnList = "location"),
        @Index(name = "idx_jobs_source", columnList = "source")
    }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "external_job_id")
    private String externalJobId;
    private String title;
    private String company;
    private String location;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String applyUrl;
    private String salary;
    private String jobType;
    private String source;
    private Boolean remote;
    @Column(columnDefinition = "TEXT")
    private String tags;
    private LocalDateTime createdAt;

    // Step 7: Salary Support
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;

    // Step 8: Job Expiration
    private LocalDateTime lastSeenAt;
    private Boolean active;

    // Step 9: Provider Metadata
    private String provider;
    private String providerUrl;
    private String providerJobId;
}
