package com.example.schoolhalper_api.controller;

import com.example.schoolhalper_api.domain.Section;
import com.example.schoolhalper_api.domain.Statistics;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.repository.SectionRepository;
import com.example.schoolhalper_api.repository.UserRepository;
import com.example.schoolhalper_api.service.StudentStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class StudentStatisticsController {

    @Autowired
    private StudentStatisticsService studentStatisticsService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SectionRepository sectionRepository;

    @GetMapping("/student/statistics")
    public String getStudentStatistics(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User student = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Ученика не найдено"));
        model.addAttribute("user", student);

        List<Section> sections = StreamSupport.stream(sectionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        model.addAttribute("sections", sections);

        List<Statistics> statisticsList = studentStatisticsService.getStudentStatistics(student);
        model.addAttribute("statistics", statisticsList);

        return "studentStatistics";
    }



}
