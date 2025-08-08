package com.example.schoolhalper_api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Size(max = 500)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;

    @OneToMany(mappedBy = "test", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestOption> options = new ArrayList<>();

    @Column(nullable = false)
    private boolean isCompleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Transient
    private boolean isCurrentTest;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
  public void setIsCompleted(boolean isCompleted){
        this.isCompleted=isCompleted;
  }
    public enum QuestionType {
        SINGLE_CHOICE,
        MULTIPLE_CHOICE
    }
    // Helper method to check if answer is correct
    public boolean isAnswerCorrect(String selectedOptionId) {
        try {
            Long id = Long.parseLong(selectedOptionId);
            return options.stream()
                    .anyMatch(option -> option.getId().equals(id) && option.isCorrect());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Helper method for multiple choice answers
    public boolean areAnswersCorrect(List<String> selectedOptionIds) {
        try {
            List<Long> selectedIds = selectedOptionIds.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            List<Long> correctIds = options.stream()
                    .filter(TestOption::isCorrect)
                    .map(TestOption::getId)
                    .collect(Collectors.toList());

            return selectedIds.containsAll(correctIds) && correctIds.containsAll(selectedIds);
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public boolean isSingleChoice() {
        return questionType == QuestionType.SINGLE_CHOICE;
    }
    public List<String> getOptionTexts() {
        return this.options.stream()
                .map(TestOption::getText)
                .collect(Collectors.toList());
    }
}