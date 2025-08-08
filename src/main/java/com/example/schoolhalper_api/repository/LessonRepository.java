package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.Lesson;
import com.example.schoolhalper_api.domain.Section;
import com.example.schoolhalper_api.domain.TeacherProfile;
import com.example.schoolhalper_api.domain.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends CrudRepository<Lesson,Long> {
    List<Lesson> findByTeacher(User teacher);
    List<Lesson> findBySection(Section section);
}
