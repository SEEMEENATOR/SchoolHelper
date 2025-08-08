package com.example.schoolhalper_api.repository;

import com.example.schoolhalper_api.domain.Comment;
import com.example.schoolhalper_api.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByLessonOrderByCreatedAtAsc(Lesson lesson);
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);
}
