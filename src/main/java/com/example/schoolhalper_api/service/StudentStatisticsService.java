package com.example.schoolhalper_api.service;

import com.example.schoolhalper_api.domain.*;
import com.example.schoolhalper_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentStatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final TaskRepository taskRepository;
    private final TestRepository testRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final CompletedTestRepository completedTestRepository;
    private final CompletedTaskRepository completedTaskRepository;

    @Transactional
    public List<Statistics> getStudentStatistics(User student) {
        List<Lesson> lessons = new ArrayList<>();
        lessonRepository.findAll().forEach(lessons::add);

        List<Statistics> statisticsList = new ArrayList<>();

        for (Lesson lesson : lessons) {
            List<Task> tasks = taskRepository.findByLesson(lesson);
            List<Test> tests = testRepository.findByLesson(lesson);

            long completedTasks = completedTaskRepository.countByUserAndTaskIn(student, tasks);
            long completedTests = completedTestRepository.countByUserAndTestIn(student, tests);

            Statistics stats = statisticsRepository.findByUserAndLesson(student, lesson)
                    .orElse(new Statistics(student, lesson, 0, tasks.size(), 0, tests.size()));

            stats.setCompletedTasks((int) completedTasks);
            stats.setCompletedTests((int) completedTests);
            statisticsRepository.save(stats);

            statisticsList.add(stats);
        }

        return statisticsList;
    }

    public boolean isLessonCompleted(Statistics stats) {
        return stats.getCompletedTasks() == stats.getTotalTasks() && stats.getCompletedTests() == stats.getTotalTests();
    }
}
