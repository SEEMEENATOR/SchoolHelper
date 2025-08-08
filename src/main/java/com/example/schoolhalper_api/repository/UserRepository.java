package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.TeacherProfile;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(User.Role role);
    @Query("SELECT t FROM TeacherProfile t WHERE t.user.id = :userId")
    Optional<TeacherProfile> findTeacherProfileByUserId(@Param("userId") Long userId);
}
