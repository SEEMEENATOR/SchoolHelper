package com.example.schoolhalper_api.service;

import com.example.schoolhalper_api.dto.TaskRequest;
import com.example.schoolhalper_api.dto.TestRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiTaskService {

    private final GeminiAiService geminiAiService;

    public AiTaskService(GeminiAiService geminiAiService) {
        this.geminiAiService = geminiAiService;
    }

    public List<TaskRequest> generateTasks(String topic) {
        try {
            return geminiAiService.generateTasks(topic);
        } catch (Exception e) {
            e.printStackTrace();
            TaskRequest fallback = new TaskRequest();
            fallback.setContent("Рассчитайте силу тока в цепи при напряжении 12 В и сопротивлении 6 Ом.");
            fallback.setAnswer("2");
            return List.of(fallback);
        }
    }

    public List<TestRequest> generateTests(String topic) {
        try {
            return geminiAiService.generateTests(topic);
        } catch (Exception e) {
            e.printStackTrace();
            TestRequest fallback = new TestRequest();
            fallback.setQuestion("Что измеряет амперметр?");
            fallback.setQuestionType("SINGLE_CHOICE");
            fallback.setOptions(List.of("Напряжение", "Сопротивление", "Силу тока", "Температуру"));
            fallback.setCorrectOptions(List.of(2));
            return List.of(fallback);
        }
    }
}
