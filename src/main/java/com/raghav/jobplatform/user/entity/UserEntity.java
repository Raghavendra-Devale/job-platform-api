package com.raghav.jobplatform.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
