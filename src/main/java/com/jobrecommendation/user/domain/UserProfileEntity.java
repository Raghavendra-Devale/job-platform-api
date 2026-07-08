package com.jobrecommendation.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String name;
    private String phone;
    private String location;
    
    @Column(name = "current_role_title")
    private String currentRole;
    
    private Integer experience;
    
    @Column(columnDefinition = "TEXT")
    private String bio;
    
    private String linkedin;
    private String github;
    private String portfolio;
}
