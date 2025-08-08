package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.CompletedTest;
import com.example.schoolhalper_api.domain.Task;
import com.example.schoolhalper_api.domain.Test;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompletedTestRepository extends JpaRepository<CompletedTest, Long> {
    boolean existsByUserAndTest(User user, Test test);
    long countByUserAndTestIn(User user, List<Test> tests);
    void deleteByTestId(Long testId);
}