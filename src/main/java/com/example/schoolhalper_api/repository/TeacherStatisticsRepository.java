package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.TeacherStatistics;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherStatisticsRepository extends JpaRepository<TeacherStatistics, Long> {

    List<TeacherStatistics> findByTeacher(User teacher);

    TeacherStatistics findByTeacherAndLesson(User teacher, Lesson lesson);
}
