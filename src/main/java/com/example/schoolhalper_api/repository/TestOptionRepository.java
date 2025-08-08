package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.TestOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestOptionRepository extends JpaRepository<TestOption, Long> {
}