package com.example.schoolhalper_api.controller;

import com.example.schoolhalper_api.domain.*;
import com.example.schoolhalper_api.dto.LessonRequest;
import com.example.schoolhalper_api.dto.TaskRequest;
import com.example.schoolhalper_api.dto.TestRequest;
import com.example.schoolhalper_api.repository.*;

import com.example.schoolhalper_api.service.OcrService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.JstlUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class TeacherController {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestOptionRepository testOptionRepository;
    @Autowired
    private CompletedTaskRepository completedTaskRepository;
    @Autowired
    private CompletedTestRepository completedTestRepository;
    @Autowired
    private OcrService ocrService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TeacherProfileRepository teacherProfileRepository;
    @Autowired
    private TestRepository testRepository;
    @Autowired
    private StatisticsRepository statisticsRepository;
    @Transactional
    @GetMapping("/teacher/lessons/add")
    public String showAddLessonForm(Model model, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent() && user.get().getTeacherProfile().getIsApproved()) {
            model.addAttribute("sections", sectionRepository.findAll());
            return "addLesson";
        } else {
            redirectAttributes.addFlashAttribute("errorApproved", "Дождитесь подтверждения вашего профиля.");
            return "redirect:/";
        }
    }

    @GetMapping("/verification")
    public String teacherVerificationPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TeacherProfile profile = user.getTeacherProfile();

        Map<String, Object> profileData = new HashMap<>();
        profileData.put("id", profile.getId());
        profileData.put("isApproved", profile.getIsApproved());
        profileData.put("isVerifiedBySystem", profile.getIsVerifiedBySystem());

        if (profile.getCertificateImage() != null) {
            profileData.put("hasCertificate", true);
            profileData.put("certificateImageBase64",
                    Base64.getEncoder().encodeToString(profile.getCertificateImage()));
        }

        if (profile.getCertificateText() != null) {
            profileData.put("certificateText", profile.getCertificateText());
        }
        model.addAttribute("user", user);

        List<Section> sections = StreamSupport
                .stream(sectionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        model.addAttribute("sections", sections);

        model.addAttribute("teacherProfile", profileData);
        return "teacher-verification";
    }

    @PostMapping("/upload-certificate")
    @Transactional
    public String uploadCertificate(@RequestParam("certificate") MultipartFile file,
                                    RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Файл не может быть пустым");
            }

            if (file.getSize() > 5_000_000) { // 5MB limit
                throw new IllegalArgumentException("Размер файла не должен превышать 5MB");
            }

            TeacherProfile profile = user.getTeacherProfile();
            if (profile == null) {
                profile = TeacherProfile.builder()
                        .user(user)
                        .build();
            }

            byte[] imageBytes = file.getBytes();
            profile.setCertificateImage(imageBytes);
            System.out.println("байтосы " + imageBytes);
            String extractedText = ocrService.extractTextFromImage(imageBytes);
            profile.setCertificateText(extractedText);
            boolean isVerified = ocrService.verifyCertificate(extractedText);
            profile.setIsVerifiedBySystem(isVerified);
            System.out.println("IMAGE BYTES LENGTH: " + imageBytes.length);
            System.out.println("IMAGE FIRST BYTE: " + imageBytes[0]);
            teacherProfileRepository.save(profile);

            redirectAttributes.addFlashAttribute("message", "Сертификат успешно загружен");
            if (isVerified) {
                redirectAttributes.addFlashAttribute("verified", "Система подтвердила валидность сертификата");
            } else {
                redirectAttributes.addFlashAttribute("warning", "Система не смогла подтвердить валидность сертификата. Администратор проверит вручную");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обработке сертификата: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/profile";
    }

    @PostMapping("/teacher/lessons/add")
    @Transactional
    public ResponseEntity<?> addLesson(@RequestBody LessonRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<User> user = userRepository.findByUsername(username);

            if (user.isEmpty() || !user.get().getTeacherProfile().getIsApproved()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Section section = sectionRepository.findById(request.getSectionId())
                    .orElseThrow(() -> new IllegalArgumentException("Раздел не найден"));

            // Создаем урок
            Lesson lesson = Lesson.builder()
                    .title(request.getTitle())
                    .content(request.getContent())
                    .teacher(user.get())
                    .section(section)
                    .build();
            lessonRepository.save(lesson);

            // Добавляем задачи
            if (request.getTasks() != null) {
                for (TaskRequest taskRequest : request.getTasks()) {
                    Task task = Task.builder()
                            .lesson(lesson)
                            .content(taskRequest.getContent())
                            .answer(taskRequest.getAnswer())
                            .build();
                    taskRepository.save(task);
                }
            }

            // Добавляем тесты
            if (request.getTests() != null) {
                for (TestRequest testRequest : request.getTests()) {
                    Test test = Test.builder()
                            .lesson(lesson)
                            .question(testRequest.getQuestion())
                            .questionType(Test.QuestionType.valueOf(testRequest.getQuestionType()))
                            .build();
                    testRepository.save(test);

                    // Добавляем варианты ответов
                    List<String> options = testRequest.getOptions();
                    List<Integer> correctOptions = testRequest.getCorrectOptions();

                    for (int i = 0; i < options.size(); i++) {
                        TestOption option = TestOption.builder()
                                .test(test)
                                .text(options.get(i))
                                .isCorrect(correctOptions.contains(i))
                                .build();
                        testOptionRepository.save(option);
                    }
                }
            }

            return ResponseEntity.ok(Map.of("redirect", "/teacher/lessons"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }




    @GetMapping("/teacher/lessons")
    public String getTeacherLessons(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User teacher = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        List<Lesson> teacherLessons = lessonRepository.findByTeacher(teacher);
        model.addAttribute("lessons",teacherLessons);
        return "teacherLessons";
    }
    @PostMapping("/teacher/lessons/delete/{id}")
    @Transactional
    public String deleteTeacherLesson(@PathVariable Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Урок не найден"));

        try {
            // Удаляем все записи в таблице statistics, которые ссылаются на этот урок
            statisticsRepository.deleteByLessonId(id);

            // Удаляем все связанные задачи и выполненные тесты
            for (Task task : lesson.getTasks()) {
                if (task != null) {
                    completedTaskRepository.deleteByTaskId(task.getId());
                }
            }
            for (Test test : lesson.getTests()) {
                if (test != null) {
                    completedTestRepository.deleteByTestId(test.getId());
                }
            }


            // Удаляем все задачи и тесты
            taskRepository.deleteAll(lesson.getTasks());
            testRepository.deleteAll(lesson.getTests());

            // Удаляем урок
            lessonRepository.delete(lesson);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка при удалении урока: " + e.getMessage());
        }

        return "redirect:/teacher/lessons";
    }



    @GetMapping("/teacher/tests/edit/{testId}")
    public String editTestForm(@PathVariable Long testId, Model model) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        model.addAttribute("isSingleChoice", test.getQuestionType() == Test.QuestionType.SINGLE_CHOICE);
        model.addAttribute("isMultipleChoice", test.getQuestionType() == Test.QuestionType.MULTIPLE_CHOICE);
        model.addAttribute("test", test);
        return "editTest";
    }

    @PostMapping("/teacher/tests/edit")
    @Transactional
    public String editTest(
            @RequestParam Long testId,
            @RequestParam String question,
            @RequestParam Test.QuestionType questionType,
            @RequestParam String optionTextsRaw,
            @RequestParam String optionCorrectRaw
    ) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        test.setQuestion(question);
        test.setQuestionType(questionType);

        // Удаляем старые опции
        testOptionRepository.deleteAll(test.getOptions());
        test.getOptions().clear();

        String[] optionTexts = optionTextsRaw != null ? optionTextsRaw.split("\\|\\|") : new String[0];
        String[] correctIndices = optionCorrectRaw != null && !optionCorrectRaw.isEmpty()
                ? optionCorrectRaw.split(",")
                : new String[0];

        List<String> correctIndexList = Arrays.asList(correctIndices);

        for (int i = 0; i < optionTexts.length; i++) {
            boolean isCorrect = correctIndexList.contains(String.valueOf(i));
            TestOption option = TestOption.builder()
                    .test(test)
                    .text(optionTexts[i])
                    .isCorrect(isCorrect)
                    .build();
            testOptionRepository.save(option);
        }

        testRepository.save(test);
        return "redirect:/lessons/" + test.getLesson().getId();
    }

    @GetMapping("/teacher/tasks/edit/{taskId}")
    public String showEditTaskForm(@PathVariable Long taskId, Model model) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        model.addAttribute("task", task);
        return "editTask";
    }

    @PostMapping("/teacher/tasks/edit")
    public String editTask(@RequestParam Long taskId, @RequestParam String content, @RequestParam String answer) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        task.setContent(content);
        task.setAnswer(answer);
        taskRepository.save(task);

        return "redirect:/lessons/" + task.getLesson().getId();
    }
    @GetMapping("/teachers")
    public String getTeachers(Model model, Authentication authentication) {
        // Проверяем авторизацию пользователя
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                userRepository.findByUsername(username).ifPresent(user -> {
                    model.addAttribute("user", user);
                    // Добавляем информацию о профиле учителя, если нужно
                    userRepository.findTeacherProfileByUserId(user.getId())
                            .ifPresent(profile -> model.addAttribute("teacherProfile", profile));
                });
            }
        }

        // Добавляем разделы в модель
        List<Section> sections = StreamSupport.stream(sectionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        model.addAttribute("sections", sections);

        List<User> teachers = userRepository.findByRole(User.Role.TEACHER);
        List<TeacherDTO> teacherDTOs = new ArrayList<>();

        for (User teacher : teachers) {
            List<Lesson> lessons = lessonRepository.findByTeacher(teacher);
            teacherDTOs.add(new TeacherDTO(
                    teacher.getFirstName(),
                    teacher.getLastName(),
                    teacher.getUsername(),
                    teacher.getEmail(),
                    teacher.getBio(),
                    teacher.getImage(),
                    lessons
            ));
        }

        model.addAttribute("teacherData", teacherDTOs);
        return "teacherList";
    }


}