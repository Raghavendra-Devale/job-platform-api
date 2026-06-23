package com.raghav.jobplatform.jobs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
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
}
