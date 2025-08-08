package com.example.schoolhalper_api.controller;

import com.example.schoolhalper_api.domain.*;
import com.example.schoolhalper_api.repository.LessonRepository;
import com.example.schoolhalper_api.repository.TeacherStatisticsRepository;
import com.example.schoolhalper_api.repository.UserRepository;
import com.example.schoolhalper_api.service.TeacherStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.ParameterResolutionDelegate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class TeacherStatisticsController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LessonRepository lessonRepository;
@Autowired
    private TeacherStatisticsRepository teacherStatisticsRepository;
    @Autowired
    private TeacherStatisticsService teacherStatisticsService;

    @GetMapping("/teacher/statistics")
    public String getTeacherStatistics(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User teacher = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Учитель не найден"));

        List<Lesson> lessons = lessonRepository.findByTeacher(teacher);

        int totalLikes = 0;
        int totalDislikes = 0;

        List<Map<String, Object>> statistics = new ArrayList<>();
        for (Lesson lesson : lessons) {
            int lessonLikes = lesson.getLikes();
            int lessonDislikes = lesson.getDislikes();
            int lessonRating = lessonLikes - lessonDislikes;

            totalLikes += lessonLikes;
            totalDislikes += lessonDislikes;

            Map<String, Object> lessonStats = new HashMap<>();
            lessonStats.put("lesson", lesson);
            lessonStats.put("totalLikes", lessonLikes);
            lessonStats.put("totalDislikes", lessonDislikes);
            lessonStats.put("totalRating", lessonRating);

            statistics.add(lessonStats);
        }

        int totalRating = totalLikes - totalDislikes;

        model.addAttribute("statistics", statistics);
        model.addAttribute("totalLikes", totalLikes);
        model.addAttribute("totalDislikes", totalDislikes);
        model.addAttribute("totalRating", totalRating);

        return "statistics";
    }


}

