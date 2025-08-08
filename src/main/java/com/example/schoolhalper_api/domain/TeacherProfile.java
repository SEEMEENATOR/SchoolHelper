package com.example.schoolhalper_api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "teacher_profile")
public class TeacherProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private Boolean isApproved = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "certificate_image")
    private byte[] certificateImage;

    @Column(length = 1000)
    private String certificateText;

    @Builder.Default
    private Boolean isVerifiedBySystem = false;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
