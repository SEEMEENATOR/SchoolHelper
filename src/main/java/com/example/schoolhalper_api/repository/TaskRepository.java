package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.Lesson;
import com.example.schoolhalper_api.domain.Task;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends CrudRepository<Task,Long> {
    List<Task> findByLesson(Lesson lesson);
}
