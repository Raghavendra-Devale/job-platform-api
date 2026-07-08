package com.jobrecommendation.jobs.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "job_sync_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSyncHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer fetched;
    private Integer inserted;
    private Integer skipped;
    private String status; // "SUCCESS" or "FAILED"
}
