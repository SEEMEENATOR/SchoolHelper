package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.Lesson;
import com.example.schoolhalper_api.domain.Statistics;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StatisticsRepository extends CrudRepository<Statistics, Long> {
    Optional<Statistics> findByUserAndLesson(User user, Lesson lesson);
    void deleteByLessonId(Long lessonId);
}
