package com.example.schoolhalper_api.service;

import com.example.schoolhalper_api.domain.*;
import com.example.schoolhalper_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherStatisticsService {

    private final TeacherStatisticsRepository teacherStatisticsRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public List<TeacherStatistics> getTeacherStatistics(User teacher) {
        List<Lesson> lessons = lessonRepository.findByTeacher(teacher);
        List<TeacherStatistics> statistics = new ArrayList<>();

        for (Lesson lesson : lessons) {
            TeacherStatistics stat = teacherStatisticsRepository.findByTeacherAndLesson(teacher, lesson);
            if (stat == null) {
                stat = new TeacherStatistics();
                stat.setTeacher(teacher);
                stat.setLesson(lesson);
                stat.setTotalLikes(lesson.getLikes());
                stat.setTotalDislikes(lesson.getDislikes());
                stat.setTotalRating(lesson.getLikes()- lesson.getDislikes());
                teacherStatisticsRepository.save(stat);
            }
            statistics.add(stat);
        }

        System.out.println("Teacher Statistics: " + statistics);

        return statistics;
    }

}