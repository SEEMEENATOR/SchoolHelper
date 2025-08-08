package com.example.schoolhalper_api.dto;

import lombok.Data;

import java.util.List;

@Data
public class LessonRequest {
    private String title;
    private Long sectionId;
    private String content;
    private List<TaskRequest> tasks;
    private List<TestRequest> tests;
}