package com.example.interview_agent.repository;

import com.example.interview_agent.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByQuestionIdOrderByCreateTimeDesc(Long questionId);

    @EntityGraph(value = "Comment.withUser")
    List<Comment> findByQuestionId(Long questionId);
}