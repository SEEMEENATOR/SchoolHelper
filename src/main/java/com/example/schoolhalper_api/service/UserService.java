package com.example.schoolhalper_api.service;

import com.example.schoolhalper_api.domain.Lesson;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.repository.LessonRepository;
import com.example.schoolhalper_api.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    public UserService(UserRepository userRepository, LessonRepository lessonRepository) {
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }
    @Transactional
    public void blockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        user.setIsBlocked(true);
        userRepository.save(user);

        updateLessonVisibility(user, false);
    }

    @Transactional
    public void unblockUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        user.setIsBlocked(false);
        userRepository.save(user);

        updateLessonVisibility(user, true);
    }

    public void updateLessonVisibility(User user, boolean isVisible) {
        List<Lesson> lessons = lessonRepository.findByTeacher(user);
        for (Lesson lesson : lessons) {
            lesson.setIsVisible(isVisible);
            lessonRepository.save(lesson);
        }
    }
    @Transactional
    public void updateUser(User user, MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            try {
                user.setImage(image.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (user.getIsBlocked()) {
            throw new BlockedUserException("User is blocked");
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}
