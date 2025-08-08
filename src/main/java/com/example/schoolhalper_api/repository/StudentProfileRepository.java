package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.StudentProfile;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StudentProfileRepository extends CrudRepository<StudentProfile,Long> {
    Optional<StudentProfile> findByUser(User user);
}
