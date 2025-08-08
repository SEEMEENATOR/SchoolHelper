package com.example.schoolhalper_api.dto;

import lombok.Data;

import java.util.List;

@Data
public class TestRequest {
    private String question;
    private String questionType;
    private List<String> options;
    private List<Integer> correctOptions;
}