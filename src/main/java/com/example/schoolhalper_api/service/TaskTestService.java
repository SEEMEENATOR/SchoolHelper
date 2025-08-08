package com.example.schoolhalper_api.service;

import com.example.schoolhalper_api.domain.*;
import com.example.schoolhalper_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskTestService {

    private final TaskRepository taskRepository;
    private final TestRepository testRepository;
    private final StatisticsRepository statisticsRepository;

    public boolean submitTaskAnswer(User user, Task task, String userAnswer) {
        boolean isCorrect = task.getAnswer().equals(userAnswer);

        updateStatistics(user, task.getLesson(), isCorrect, false);

        return isCorrect;
    }

    public boolean submitTestAnswer(User user, Test test, List<String> userAnswers) {
        try {
            List<Long> selectedOptionIds = userAnswers.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            List<Long> correctOptionIds = test.getOptions().stream()
                    .filter(TestOption::isCorrect)
                    .map(TestOption::getId)
                    .collect(Collectors.toList());

            boolean isCorrect = selectedOptionIds.containsAll(correctOptionIds)
                    && correctOptionIds.containsAll(selectedOptionIds);

            updateStatistics(user, test.getLesson(), false, isCorrect);
            return isCorrect;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void updateStatistics(User user, Lesson lesson, boolean taskCompleted, boolean testCompleted) {
        Statistics statistics = statisticsRepository.findByUserAndLesson(user, lesson)
                .orElse(new Statistics(user, lesson, 0, lesson.getTasks().size(), 0, lesson.getTests().size()));

        if (taskCompleted) {
            statistics.setCompletedTasks(statistics.getCompletedTasks() + 1);
        }
        if (testCompleted) {
            statistics.setCompletedTests(statistics.getCompletedTests() + 1);
        }

        statisticsRepository.save(statistics);
    }
}
