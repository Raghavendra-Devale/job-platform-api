package com.raghav.jobplatform.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_skills")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSkillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private ResumeEntity resume;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "confidence")
    private Double confidence;
}
