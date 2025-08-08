package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.TeacherProfile;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends CrudRepository<TeacherProfile,Long> {
    Optional<TeacherProfile> findByUser(User user);
    List<TeacherProfile> findAll();
}
