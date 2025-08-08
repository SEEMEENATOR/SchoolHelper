package com.example.schoolhalper_api.controller;
import com.example.schoolhalper_api.domain.Section;
import com.example.schoolhalper_api.domain.StudentProfile;
import com.example.schoolhalper_api.domain.TeacherProfile;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.repository.SectionRepository;
import com.example.schoolhalper_api.repository.StudentProfileRepository;
import com.example.schoolhalper_api.repository.TeacherProfileRepository;
import com.example.schoolhalper_api.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    TeacherProfileRepository teacherProfileRepository;
    @Autowired
    StudentProfileRepository studentProfileRepository;
    @Autowired
    SectionRepository sectionRepository;

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        // Проверяем атрибуты сессии и добавляем их в модель
        if (session.getAttribute("error") != null) {
            model.addAttribute("error", true);
            session.removeAttribute("error");
        }

        if (session.getAttribute("blocked") != null) {
            model.addAttribute("blocked", true);
            session.removeAttribute("blocked");
        }

        return "login";
    }
    @Transactional
    @GetMapping("/")
    public String home(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            Optional<User> user = Optional.empty();

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                user = userRepository.findByUsername(username);
            }

            if (user.isPresent()) {
                model.addAttribute("user", user.get());
                userRepository.findTeacherProfileByUserId(user.get().getId()).ifPresent(profile -> {
                    if (Boolean.FALSE.equals(profile.getIsApproved())) {
                        model.addAttribute("unapprovedTeacher", true);
                    }
                });
            } else {
                model.addAttribute("error", "Пользователь не найден");
            }
        } else {
            model.addAttribute("error", "Пользователь не аутентифицирован");
        }
        List<Section> sections = StreamSupport.stream(sectionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        model.addAttribute("sections", sections);
        return "main";
    }



    @GetMapping("/registration")
    public String registration(Model model) {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String role,
                           Model model) {
        if (password.length() < 8) {
            model.addAttribute("passwordError", "Пароль должен содержать не менее 8 символов");
            return "registration";
        }
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("usernameError", "Пользователь с таким именем уже существует");
            return "registration";
        }
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("emailError", "Пользователь с таким email уже зарегистрирован");
            return "registration";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());

        try {
            user.setRole(User.Role.valueOf(role.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return "redirect:/registration?error";
        }

        userRepository.save(user);

        if (user.getRole() == User.Role.TEACHER) {
            TeacherProfile teacherProfile = new TeacherProfile();
            teacherProfile.setUser(user);
            teacherProfileRepository.save(teacherProfile);
        } else if (user.getRole() == User.Role.STUDENT) {
            StudentProfile studentProfile = new StudentProfile();
            studentProfile.setUser(user);
            studentProfileRepository.save(studentProfile);
        }

        return "redirect:/login";
    }
}