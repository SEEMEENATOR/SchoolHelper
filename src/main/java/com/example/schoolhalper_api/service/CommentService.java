package com.example.schoolhalper_api.service;

import com.example.schoolhalper_api.domain.Comment;
import com.example.schoolhalper_api.domain.Lesson;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.repository.CommentRepository;
import com.example.schoolhalper_api.repository.LessonRepository;
import com.example.schoolhalper_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;

    public List<Comment> getCommentsForLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new RuntimeException("Lesson not found"));
        return commentRepository.findByLessonOrderByCreatedAtAsc(lesson);
    }

    @Transactional
    public Comment addComment(Long lessonId, Long userId, String content, Long parentCommentId) {
        Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(() -> new RuntimeException("Lesson not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .lesson(lesson)
                .user(user)
                .content(content)
                .build();

        if (parentCommentId != null) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }

        return commentRepository.save(comment);
    }
}
