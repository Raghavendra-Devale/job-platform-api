package com.raghav.jobplatform.user.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_preferences")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "preferred_roles")
    private String preferredRoles;

    @Column(name = "preferred_locations")
    private String preferredLocations;

    @Column(name = "remote_only")
    private Boolean remoteOnly;

    @Column(name = "salary_range")
    private String salaryRange;

    @Column(name = "job_types")
    private String jobTypes;
}
