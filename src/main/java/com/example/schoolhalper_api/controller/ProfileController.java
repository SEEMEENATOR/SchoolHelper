package com.example.schoolhalper_api.controller;

import com.example.schoolhalper_api.domain.StudentProfile;
import com.example.schoolhalper_api.domain.TeacherProfile;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.repository.StudentProfileRepository;
import com.example.schoolhalper_api.repository.TeacherProfileRepository;
import com.example.schoolhalper_api.repository.UserRepository;
import com.example.schoolhalper_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
@Controller
public class ProfileController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    TeacherProfileRepository teacherProfileRepository;
    @Autowired
    StudentProfileRepository studentProfileRepository;
    @GetMapping("/profile")
    public String profilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Логируем информацию о пользователе для отладки
            System.out.println("Аутентифицированный пользователь: " + authentication.getPrincipal());
        } else {
            System.out.println("Аутентификация не прошла.");
        }
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            User user = null;

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                Optional<User> optionalUser = userRepository.findByUsername(username);

                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                    model.addAttribute("user", user);
                    model.addAttribute("base64Image", Base64.getEncoder().encodeToString(user.getImage()));

                    Optional<TeacherProfile> teacherProfile = teacherProfileRepository.findByUser(user);
                    if (teacherProfile.isPresent()) {
                        model.addAttribute("teacherProfile", teacherProfile.get());
                        return "profile";
                    }

                    Optional<StudentProfile> studentProfile = studentProfileRepository.findByUser(user);
                    if (studentProfile.isPresent()) {
                        model.addAttribute("studentProfile", studentProfile.get());
                        return "profile";
                    }
                    if(optionalUser.get().getRole()== User.Role.ADMIN){
                        model.addAttribute("admin",optionalUser);
                        return "profile";
                    }
                }
            }
        }
        return "error";
    }

    @PostMapping("/profile/edit")
    public String editProfile(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) MultipartFile image
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                String currentUsername = ((UserDetails) principal).getUsername();
                Optional<User> optionalUser = userRepository.findByUsername(currentUsername);

                if (optionalUser.isPresent()) {
                    user = optionalUser.get();
                }
            }
        }

        if (user == null) {
            return "redirect:/error";
        }

        System.out.println("Updating user: " + user.getUsername());
        System.out.println("First name: " + firstName);
        System.out.println("Last name: " + lastName);
        System.out.println("Bio: " + bio);
        System.out.println("Image provided: " + (image != null && !image.isEmpty()));

        if (firstName != null && !firstName.trim().isEmpty()) {
            user.setFirstName(firstName);
        }

        if (lastName != null && !lastName.trim().isEmpty()) {
            user.setLastName(lastName);
        }

        if (bio != null && !bio.trim().isEmpty()) {
            user.setBio(bio);
        }

        if (image != null && !image.isEmpty()) {
            try {
                user.setImage(image.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        userService.updateUser(user, image);
        return "redirect:/profile";
    }




    @GetMapping("/profile/edit")
    public String editProfilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                Optional<User> optionalUser = userRepository.findByUsername(username);

                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();
                    model.addAttribute("user", user);

                    if (user.getImage() != null) {
                        String base64Image = Base64.getEncoder().encodeToString(user.getImage());
                        model.addAttribute("base64Image", base64Image);
                    }

                    if (user.getRole() == User.Role.TEACHER) {
                        Optional<TeacherProfile> teacherProfile = teacherProfileRepository.findByUser(user);
                        teacherProfile.ifPresent(profile -> model.addAttribute("teacherProfile", profile));
                    } else if (user.getRole() == User.Role.STUDENT) {
                        Optional<StudentProfile> studentProfile = studentProfileRepository.findByUser(user);
                        studentProfile.ifPresent(profile -> model.addAttribute("studentProfile", profile));
                    }
                } else {
                    model.addAttribute("error", "Пользователь не найден");
                }
            }
        } else {
            model.addAttribute("error", "Пользователь не аутентифицирован");
        }

        return "editProfile";
    }

}
