package com.example.schoolhalper_api.security;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Optional<User> existingAdmin = userRepository.findByUsername("admin");

        if (existingAdmin.isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@example.com")
                    .role(User.Role.ADMIN)
                    .isBlocked(false)
                    .firstName("Admin")
                    .lastName("User")
                    .build();
            userRepository.save(admin);
            System.out.println("Администратор создан: admin/admin123");
        } else {
            System.out.println("Администратор уже существует.");
        }
    }
}
