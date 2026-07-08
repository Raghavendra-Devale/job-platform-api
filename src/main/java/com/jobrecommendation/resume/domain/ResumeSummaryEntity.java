package com.jobrecommendation.resume.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_summaries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private ResumeEntity resume;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
}
