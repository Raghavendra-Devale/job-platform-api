package com.jobrecommendation.user.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password"})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // e.g. "USER", "ADMIN"

    @Column(name = "work_preference")
    private String workPreference; // e.g. "REMOTE", "ONSITE", "HYBRID", "ANY"

    @Column(name = "alert_enabled")
    private Boolean alertEnabled;

    // Profile fields
    private Integer experience;

    @Column(name = "current_role_title")
    private String currentRole;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String linkedin;
    private String github;
    private String portfolio;

    // Preference fields
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

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (role == null) {
            role = "USER";
        }
        if (workPreference == null) {
            workPreference = "ANY";
        }
        if (alertEnabled == null) {
            alertEnabled = true;
        }
    }
}
