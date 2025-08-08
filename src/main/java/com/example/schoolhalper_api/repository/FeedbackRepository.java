package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.Feedback;
import com.example.schoolhalper_api.domain.Lesson;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByLessonAndUser(Lesson lesson, User user);
    long countByLessonAndIsLiked(Lesson lesson, boolean isLiked);
}