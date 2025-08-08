package com.example.schoolhalper_api.controller;

import com.example.schoolhalper_api.domain.*;
import com.example.schoolhalper_api.dto.TaskRequest;
import com.example.schoolhalper_api.dto.TestRequest;
import com.example.schoolhalper_api.repository.*;
import com.example.schoolhalper_api.service.AiTaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.schoolhalper_api.service.TaskTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class LessonController {

    @Autowired
    private LessonRepository lessonRepository;
@Autowired
private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private CompletedTaskRepository completedTaskRepository;
    @Autowired
    private CompletedTestRepository completedTestRepository;
    @Autowired
    private FeedbackRepository feedbackRepository;
    @Autowired
    private StatisticsRepository statisticsRepository;
    @Autowired
    private TaskTestService taskTestService;
    @Autowired
    private SectionRepository sectionRepository;
    @PostMapping("/lessons/{lessonId}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable Long lessonId, @RequestParam String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);

        Map<String, Object> response = new HashMap<>();

        if (userOpt.isPresent() && lessonOpt.isPresent()) {
            User user = userOpt.get();
            Lesson lesson = lessonOpt.get();

            Comment comment = Comment.builder()
                    .lesson(lesson)
                    .user(user)
                    .content(content)
                    .build();

            commentRepository.save(comment);

            response.put("success", true);
            response.put("comment", Map.of(
                    "id", comment.getId(),
                    "content", comment.getContent(),
                    "createdAt", formatDate(comment.getCreatedAt()),
                    "user", Map.of(
                            "firstName", user.getFirstName(),
                            "role", user.getRole().name()
                    ),
                    "lesson", Map.of("id", lesson.getId()),
                    "currentUserIsAdmin", authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ));

            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "Ошибка при добавлении комментария");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/lessons/{lessonId}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long lessonId,
                                @PathVariable Long commentId,
                                RedirectAttributes redirectAttributes,
                                Authentication authentication) {

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Пользователь не найден");
            return "redirect:/lessons/" + lessonId;
        }

        User user = userOpt.get();

        if (user.getRole() != User.Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Недостаточно прав для удаления комментария");
            return "redirect:/lessons/" + lessonId;
        }

        Optional<Comment> commentOpt = commentRepository.findById(commentId);

        if (commentOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Комментарий не найден");
            return "redirect:/lessons/" + lessonId;
        }

        try {
            commentRepository.delete(commentOpt.get());
            redirectAttributes.addFlashAttribute("message", "Комментарий успешно удален");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Ошибка при удалении комментария");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }

        return "redirect:/lessons/" + lessonId;
    }

    @PostMapping("/lessons/{lessonId}/comment/{parentCommentId}/reply")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> replyToComment(@PathVariable Long lessonId, @PathVariable Long parentCommentId,
                                                              @RequestParam String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        Optional<Comment> parentCommentOpt = commentRepository.findById(parentCommentId);

        Map<String, Object> response = new HashMap<>();

        if (userOpt.isPresent() && lessonOpt.isPresent() && parentCommentOpt.isPresent()) {
            User user = userOpt.get();
            Lesson lesson = lessonOpt.get();
            Comment parentComment = parentCommentOpt.get();

            Comment reply = Comment.builder()
                    .lesson(lesson)
                    .user(user)
                    .content(content)
                    .parentComment(parentComment)
                    .build();

            commentRepository.save(reply);

            response.put("success", true);
            response.put("message", "Ответ успешно добавлен");
            response.put("comment", Map.of(
                    "id", reply.getId(),
                    "content", reply.getContent(),
                    "createdAt", formatDate(reply.getCreatedAt()),
                    "user", Map.of(
                            "firstName", user.getFirstName(),
                            "role", user.getRole().name()
                    ),
                    "lesson", Map.of("id", lesson.getId()),
                    "currentUserIsAdmin", authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ));

            return ResponseEntity.ok(response);
        }

        response.put("success", false);
        response.put("message", "Ошибка при добавлении ответа");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private String formatDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatter);
    }
    @GetMapping("/lessons/{id}")
    public String getLesson(@PathVariable Long id, Model model) {
        Optional<Lesson> lessonOpt = lessonRepository.findById(id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (lessonOpt.isPresent()) {
            Lesson lesson = lessonOpt.get();
            List<Comment> allComments = commentRepository.findByLessonOrderByCreatedAtAsc(lesson);

            Map<Long, List<Comment>> repliesMap = allComments.stream()
                    .filter(comment -> comment.getParentComment() != null)
                    .collect(Collectors.groupingBy(comment -> comment.getParentComment().getId()));
            boolean isAdmin = userOpt.isPresent() && userOpt.get().getRole() == User.Role.ADMIN;
            for (Comment comment : allComments) {
                comment.setReplies(repliesMap.getOrDefault(comment.getId(), new ArrayList<>()));
                User author = comment.getUser();
                if (author != null && author.getRole() != null) {
                    comment.setIsStudent(author.getRole() == User.Role.STUDENT);
                    comment.setIsTeacher(author.getRole() == User.Role.TEACHER);
                    comment.setIsAdmin(author.getRole() == User.Role.ADMIN);
                }
                comment.setCurrentUserIsAdmin(isAdmin);
            }

            List<Comment> rootComments = allComments.stream()
                    .filter(comment -> comment.getParentComment() == null)
                    .collect(Collectors.toList());

            List<Map<String, Object>> commentsWithFormattedDate = rootComments.stream().map(comment -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", comment.getId());
                map.put("content", comment.getContent());
                map.put("createdAt", formatDate(comment.getCreatedAt())); // форматируем дату
                map.put("user", comment.getUser());
                map.put("replies", comment.getReplies());
                map.put("isStudent", comment.isIsStudent());
                map.put("isTeacher", comment.isIsTeacher());
                map.put("isAdmin", comment.isIsAdmin());
                map.put("currentUserIsAdmin", comment.isCurrentUserIsAdmin());

                return map;
            }).collect(Collectors.toList());

            model.addAttribute("comments", commentsWithFormattedDate);
            if (lesson.getTasks() != null && !lesson.getTasks().isEmpty()) {
                model.addAttribute("tasks", lesson.getTasks());
            } else {
                model.addAttribute("tasksError", "В данном уроке нет задач");
            }
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("currentUser", user);
                model.addAttribute("isAdmin", user.getRole() == User.Role.ADMIN);
                Long taskIdWithMessage = (Long) model.asMap().get("taskIdWithMessage");

                List<Task> tasks = lesson.getTasks().stream()
                        .peek(task -> {
                            task.setIsCompleted(completedTaskRepository.existsByUserAndTask(user, task));
                            if (taskIdWithMessage != null && task.getId().equals(taskIdWithMessage)) {
                                task.setCurrentTask(true); // Вот здесь мы добавляем "это задача, которой показано сообщение"
                            }
                        })
                        .collect(Collectors.toList());

                model.addAttribute("tasks", tasks);
                model.addAttribute("taskMessage", model.asMap().get("taskMessage"));
                model.addAttribute("taskMessageType", model.asMap().get("taskMessageType"));


                Long testIdWithMessage = (Long) model.asMap().get("testIdWithMessage");

                List<Test> tests = lesson.getTests().stream()
                        .peek(test -> {
                            test.getOptions().size(); // Force load
                            test.setIsCompleted(completedTestRepository.existsByUserAndTest(user, test));
                            if (testIdWithMessage != null && test.getId().equals(testIdWithMessage)) {
                                test.setCurrentTest(true);
                            }
                        })
                        .collect(Collectors.toList());

                model.addAttribute("tests", tests);
                model.addAttribute("testMessage", model.asMap().get("testMessage"));
                model.addAttribute("testMessageType", model.asMap().get("testMessageType"));

            } else {
                model.addAttribute("tasksError", "В данном уроке нет задач");
            }
            lesson.getTests().forEach(test -> test.getOptions().size());

            int likes = feedbackRepository.findAll().stream()
                    .filter(feedback -> feedback.getLesson().getId().equals(id) && Boolean.TRUE.equals(feedback.getIsLiked()))
                    .mapToInt(feedback -> 1)
                    .sum();

            int dislikes = feedbackRepository.findAll().stream()
                    .filter(feedback -> feedback.getLesson().getId().equals(id) && Boolean.FALSE.equals(feedback.getIsLiked()))
                    .mapToInt(feedback -> 1)
                    .sum();

            lesson.setLikes(likes);
            lesson.setDislikes(dislikes);
            lessonRepository.save(lesson);

            model.addAttribute("lesson", lesson);
            model.addAttribute("likes", likes);
            model.addAttribute("dislikes", dislikes);

            model.addAttribute("tests", lesson.getTests());
            return "lessonDetail";
        } else {
            model.addAttribute("error", "Урок не найден");
            return "error";
        }
    }
    @Autowired
    private AiTaskService aiTaskService;
    @PostMapping("/teacher/lessons/generate-content")
    public ResponseEntity<?> generateLessonContent(@RequestBody Map<String, String> body) {
        String topic = body.get("topic");

        try {
            List<TaskRequest> tasks = aiTaskService.generateTasks(topic);
            List<TestRequest> tests = aiTaskService.generateTests(topic);

            return ResponseEntity.ok(Map.of(
                    "tasks", tasks,
                    "tests", tests
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Не удалось сгенерировать контент: " + e.getMessage()));
        }
    }



    @PostMapping("/lessons/feedback")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleFeedback(
            @RequestParam Long lessonId,
            @RequestParam boolean isLiked,
            HttpServletRequest request) {

        Map<String, Object> response = new HashMap<>();

        // Проверяем, это AJAX-запрос или нет
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty() || optionalUser.get().getRole() != User.Role.STUDENT) {
            if (isAjax) {
                response.put("success", false);
                response.put("message", "Учителя не могут ставить себе лайки");
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }

        User user = optionalUser.get();

        Optional<Lesson> optionalLesson = lessonRepository.findById(lessonId);
        if (optionalLesson.isEmpty()) {
            if (isAjax) {
                response.put("success", false);
                response.put("message", "Урок не найден");
                return ResponseEntity.badRequest().body(response);
            } else {
                return ResponseEntity.badRequest().build();
            }
        }

        Lesson lesson = optionalLesson.get();

        Optional<Feedback> feedbackOptional = feedbackRepository.findByLessonAndUser(lesson, user);
        Feedback feedback = feedbackOptional.orElse(Feedback.builder()
                .lesson(lesson)
                .user(user)
                .build());

        boolean previousLikeState = feedback.getIsLiked() != null && feedback.getIsLiked();

        feedback.setIsLiked(isLiked);
        feedbackRepository.save(feedback);

        if (isLiked != previousLikeState) {
            if (isLiked) {
                lesson.setLikes(lesson.getLikes() != null ? lesson.getLikes() + 1 : 1);
                lesson.setDislikes(lesson.getDislikes() != null && lesson.getDislikes() > 0 ? lesson.getDislikes() - 1 : 0);
            } else {
                lesson.setDislikes(lesson.getDislikes() != null ? lesson.getDislikes() + 1 : 1);
                lesson.setLikes(lesson.getLikes() != null && lesson.getLikes() > 0 ? lesson.getLikes() - 1 : 0);
            }
            lessonRepository.save(lesson);
        }

        if (isAjax) {
            response.put("success", true);
            response.put("message", isLiked ? "Спасибо за лайк!" : "Спасибо за отзыв!");
            response.put("likes", lesson.getLikes());
            response.put("dislikes", lesson.getDislikes());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.ok().build();
        }
    }
    @PostMapping("/task/{taskId}")
    public ResponseEntity<Map<String, Object>> submitTaskAnswer(
            @RequestParam String userAnswer,
            @PathVariable Long taskId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Задача с ID " + taskId + " не найдена"));
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            boolean isCorrect = task.getAnswer().equalsIgnoreCase(userAnswer);
            taskTestService.submitTaskAnswer(user, task, userAnswer);

            if (isCorrect) {
                if (!completedTaskRepository.existsByUserAndTask(user, task)) {
                    completedTaskRepository.save(CompletedTask.builder()
                            .user(user)
                            .task(task)
                            .build());
                }
            }

            response.put("success", true);
            response.put("message", isCorrect ? "✓ Ответ правильный!" : "✗ Ответ неверный");
            response.put("messageType", isCorrect ? "success" : "error");
            response.put("content", task.getContent());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @GetMapping("/sections/{id}")
    public String getSection(@PathVariable Long id, Model model) {
        Section section = sectionRepository.getSectionById(id).orElseThrow();

        if (section == null) {
            return "error/404";
        }

        model.addAttribute("section",section);

        return "section";
    }

    @PostMapping("/test/{testId}")
    public ResponseEntity<Map<String, Object>> submitTestAnswer(
            @PathVariable Long testId,
            @RequestParam(required = false) String selectedOption,
            @RequestParam(required = false) List<String> selectedOptions) {
        Map<String, Object> response = new HashMap<>();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));

            test.getOptions().size();

            boolean isCorrect;
            if (test.getQuestionType() == Test.QuestionType.SINGLE_CHOICE) {
                isCorrect = test.isAnswerCorrect(selectedOption);
            } else {
                isCorrect = test.areAnswersCorrect(selectedOptions);
            }

            if (isCorrect && !completedTestRepository.existsByUserAndTest(user, test)) {
                completedTestRepository.save(CompletedTest.builder()
                        .user(user)
                        .test(test)
                        .build());
            }

            response.put("success", true);
            response.put("message", isCorrect ? "✓ Ответ правильный!" : "✗ Ответ неверный");
            response.put("messageType", isCorrect ? "success" : "error");
            response.put("question", test.getQuestion());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




}