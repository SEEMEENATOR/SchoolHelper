package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.Lesson;
import com.example.schoolhalper_api.domain.Test;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestRepository extends CrudRepository<Test,Long> {
    Optional<Test> findByQuestionAndLesson(String question, Lesson lesson);
    List<Test> findByLesson(Lesson lesson);

}
