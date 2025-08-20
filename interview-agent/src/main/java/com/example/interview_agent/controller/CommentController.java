package com.example.interview_agent.controller;

import com.example.interview_agent.dto.CommentDto;
import com.example.interview_agent.entity.Comment;
import com.example.interview_agent.entity.Question;
import com.example.interview_agent.entity.User;
import com.example.interview_agent.repository.CommentRepository;
import com.example.interview_agent.repository.QuestionRepository;
import com.example.interview_agent.repository.UserRepository;
import com.example.interview_agent.security.UserDetailsImpl;
import com.example.interview_agent.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private QuestionRepository questionRepository;
    @Autowired private CommentService commentService;


    @PostMapping
    public ResponseEntity<?> postComment(@RequestBody CommentDto commentDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));

        Question question = questionRepository.findById(commentDto.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        Comment newComment = new Comment();
        newComment.setContent(commentDto.getContent());
        newComment.setUser(currentUser);
        newComment.setQuestion(question);
        newComment.setCreateTime(LocalDateTime.now());

        Comment savedComment = commentRepository.save(newComment);

        return ResponseEntity.status(HttpStatus.CREATED).body(CommentDto.fromEntity(savedComment));
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<CommentDto>> getCommentsForQuestion(@PathVariable Long questionId) {
        List<CommentDto> commentDtos = commentService.getCommentsForQuestion(questionId);
        return ResponseEntity.ok(commentDtos);
    }
}