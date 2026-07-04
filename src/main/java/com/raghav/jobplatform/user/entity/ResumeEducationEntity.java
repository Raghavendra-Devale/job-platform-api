package com.raghav.jobplatform.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_educations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeEducationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private ResumeEntity resume;

    @Column(name = "degree")
    private String degree;

    @Column(name = "institution")
    private String institution;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;
}
