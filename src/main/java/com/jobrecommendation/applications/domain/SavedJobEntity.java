package com.jobrecommendation.applications.domain;

import com.jobrecommendation.jobs.domain.JobEntity;
import com.jobrecommendation.user.domain.UserEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "saved_jobs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "job_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedJobEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @Column(name = "saved_at", nullable = false)
    private LocalDateTime savedAt;

    @PrePersist
    protected void onCreate() {
        if (savedAt == null) {
            savedAt = LocalDateTime.now();
        }
    }
}
