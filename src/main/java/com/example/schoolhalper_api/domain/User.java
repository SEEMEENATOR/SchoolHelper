package com.example.schoolhalper_api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "usr")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder.Default
    private Boolean isBlocked = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private StudentProfile studentProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TeacherProfile teacherProfile;

    @Size(max = 50)
    @Builder.Default
    private String firstName = ""; // Значение по умолчанию

    @Size(max = 50)
    @Builder.Default
    private String lastName = ""; // Значение по умолчанию

    @Size(max = 500) // Задайте максимальную длину, если это необходимо
    @Column(nullable = true)
    private String bio = ""; // Значение по умолчанию

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "profile_photo", columnDefinition = "bytea")
    private byte[] image;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.image = loadDefaultImage();
    }
    private byte[] loadDefaultImage() {
        try (InputStream is = getClass().getResourceAsStream("/images.png")) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public enum Role {
        ADMIN, TEACHER, STUDENT, GUEST
    }

}
