package com.example.interview_agent.service;

import com.example.interview_agent.dto.CommentDto;
import com.example.interview_agent.entity.Comment;
import com.example.interview_agent.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    /**
     * 根据问题ID获取所有评论
     * @param questionId 问题ID
     * @return 评论列表
     */
    public List<Comment> getCommentsByQuestionId(Long questionId) {
        return commentRepository.findByQuestionIdOrderByCreateTimeDesc(questionId);
    }


    public Comment addComment(Comment comment) {

        comment.setCreateTime(LocalDateTime.now());

        return commentRepository.save(comment);
    }
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsForQuestion(Long questionId) {
        List<Comment> comments = commentRepository.findByQuestionId(questionId);


        return comments.stream()
                .map(CommentDto::fromEntity)
                .collect(Collectors.toList());
    }
}