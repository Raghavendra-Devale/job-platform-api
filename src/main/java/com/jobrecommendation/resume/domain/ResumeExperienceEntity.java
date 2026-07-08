package com.jobrecommendation.resume.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "resume_experiences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeExperienceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private ResumeEntity resume;

    @Column(name = "company")
    private String company;

    @Column(name = "designation")
    private String designation;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    @ElementCollection
    @CollectionTable(name = "resume_experience_responsibilities", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "responsibility", columnDefinition = "TEXT")
    @Builder.Default
    private List<String> responsibilities = new ArrayList<>();
}
