package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.CompletedTask;
import com.example.schoolhalper_api.domain.Task;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompletedTaskRepository extends JpaRepository<CompletedTask, Long> {
    boolean existsByUserAndTask(User user, Task task);
    long countByUserAndTaskIn(User user, List<Task> tasks);
    void deleteByTaskId(Long taskId);
}
