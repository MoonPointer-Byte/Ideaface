package com.example.interview_agent.controller;

import com.example.interview_agent.dto.PostCommentDto;
import com.example.interview_agent.service.PostCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@RequestMapping("/api/posts/{postId}/comments")
public class PostCommentController {

    @Autowired
    private PostCommentService postCommentService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostCommentDto> createPostComment(@PathVariable Long postId, @RequestBody PostCommentDto commentDto) {
        commentDto.setPostId(postId);
        return new ResponseEntity<>(postCommentService.createComment(postId, commentDto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PostCommentDto>> getCommentsForPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postCommentService.getCommentsByPostId(postId));
    }
}