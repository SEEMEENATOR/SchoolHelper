package com.example.schoolhalper_api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeacherDTO {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String bio;
    private byte[] image;
    private List<Lesson> lessons;
}
