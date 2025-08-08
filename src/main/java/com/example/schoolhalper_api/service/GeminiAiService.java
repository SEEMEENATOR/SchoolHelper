package com.example.schoolhalper_api.service;

import com.example.schoolhalper_api.dto.TaskRequest;
import com.example.schoolhalper_api.dto.TestRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiAiService {

    private static final String API_KEY = "API KEY";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SINGLE_CHOICE = "SINGLE_CHOICE";
    private static final String MULTIPLE_CHOICE = "MULTIPLE_CHOICE";

    public List<TaskRequest> generateTasks(String topic) throws IOException {
        String prompt = "Сгенерируй 1 учебную задачу по теме: " + topic +
                " в формате JSON-массива [{\"content\": \"вопрос\", \"answer\": \"ответ\"}]. " +
                "Ответ должен быть ТОЛЬКО числом, без единиц измерения и без объяснений.";
        String jsonResponse = sendRequestToGemini(prompt);
        return parseTasksFromResponse(jsonResponse);
    }

    public List<TestRequest> generateTests(String topic) throws IOException {
        String prompt = "Сгенерируй 1 тестовый вопрос по теме: " + topic +
                " с вариантами ответов и правильными ответами в формате JSON-массива " +
                "[{\"question\": \"...\", \"questionType\": \"single/multiple\", \"options\": [\"...\"], \"correctOptions\": [0,1]}]";
        String jsonResponse = sendRequestToGemini(prompt);
        return parseTestsFromResponse(jsonResponse);
    }

    private String sendRequestToGemini(String prompt) throws IOException {
        JsonNode textNode = objectMapper.createObjectNode().put("text", prompt);
        JsonNode partsNode = objectMapper.createObjectNode()
                .set("parts", objectMapper.createArrayNode().add(textNode));
        JsonNode requestJson = objectMapper.createObjectNode()
                .set("contents", objectMapper.createArrayNode().add(partsNode));

        String requestBody = objectMapper.writeValueAsString(requestJson);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "null";
                System.err.println("Gemini error response: " + response.code() + " " + errorBody);
                throw new IOException("Unexpected code " + response);
            }
            return response.body() != null ? response.body().string() : "";
        }
    }

    private List<TaskRequest> parseTasksFromResponse(String responseJson) throws IOException {
        List<TaskRequest> tasks = new ArrayList<>();

        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");

        if (textNode.isTextual()) {
            String cleanedText = textNode.asText()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)```\\s*$", "")
                    .trim();

            JsonNode tasksArray = objectMapper.readTree(cleanedText);
            for (JsonNode taskNode : tasksArray) {
                TaskRequest task = new TaskRequest();
                task.setContent(taskNode.get("content").asText());
                task.setAnswer(taskNode.get("answer").asText());
                tasks.add(task);
            }
        }

        return tasks;
    }

    private List<TestRequest> parseTestsFromResponse(String responseJson) throws IOException {
        List<TestRequest> tests = new ArrayList<>();

        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");

        if (textNode.isTextual()) {
            String cleanedText = textNode.asText()
                    .replaceAll("(?s)^```json\\s*", "")
                    .replaceAll("(?s)```\\s*$", "")
                    .trim();

            JsonNode testsArray = objectMapper.readTree(cleanedText);
            for (JsonNode testNode : testsArray) {
                TestRequest test = new TestRequest();
                test.setQuestion(testNode.get("question").asText());

                String qt = testNode.get("questionType").asText().toLowerCase();
                if ("single".equals(qt)) {
                    test.setQuestionType(SINGLE_CHOICE);
                } else if ("multiple".equals(qt)) {
                    test.setQuestionType(MULTIPLE_CHOICE);
                } else {
                    test.setQuestionType(SINGLE_CHOICE); // fallback
                }

                List<String> options = new ArrayList<>();
                for (JsonNode option : testNode.get("options")) {
                    options.add(option.asText());
                }
                test.setOptions(options);

                List<Integer> correctOptions = new ArrayList<>();
                for (JsonNode correctIdx : testNode.get("correctOptions")) {
                    correctOptions.add(correctIdx.asInt());
                }
                test.setCorrectOptions(correctOptions);

                tests.add(test);
            }
        }

        return tests;
    }

}
