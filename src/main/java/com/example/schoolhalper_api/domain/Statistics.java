package com.example.schoolhalper_api.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    private Integer completedTasks = 0;
    private Integer totalTasks = 0;

    private Integer completedTests = 0;
    private Integer totalTests = 0;

    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
    public Statistics(User user, Lesson lesson, int completedTasks, int totalTasks, int completedTests, int totalTests) {
        this.user = user;
        this.lesson = lesson;
        this.completedTasks = completedTasks;
        this.totalTasks = totalTasks;
        this.completedTests = completedTests;
        this.totalTests = totalTests;
    }
    @Transient
    public boolean isCompleted() {
        return completedTasks != null && totalTasks != null
                && completedTests != null && totalTests != null
                && completedTasks.equals(totalTasks)
                && completedTests.equals(totalTests);
    }
}


