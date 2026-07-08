package com.jobrecommendation.resume.domain;

import com.jobrecommendation.user.domain.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "resumes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"resumeData", "summary", "skills", "educations", "experiences", "projects"})
public class ResumeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "resume_name", nullable = false)
    private String resumeName;

    @Column(name = "resume_data", columnDefinition = "bytea", nullable = false)
    private byte[] resumeData;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_processing_status", nullable = false)
    @Builder.Default
    private AiProcessingStatus aiProcessingStatus = AiProcessingStatus.PENDING;

    @OneToOne(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ResumeSummaryEntity summary;

    @Builder.Default
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSkillEntity> skills = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeEducationEntity> educations = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeExperienceEntity> experiences = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeProjectEntity> projects = new ArrayList<>();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
