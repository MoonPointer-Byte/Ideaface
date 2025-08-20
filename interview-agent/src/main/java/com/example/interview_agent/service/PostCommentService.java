package com.example.interview_agent.service;

import com.example.interview_agent.dto.PostCommentDto;
import com.example.interview_agent.dto.PostDto;
import com.example.interview_agent.entity.Post;
import com.example.interview_agent.entity.PostComment;
import com.example.interview_agent.entity.User;
import com.example.interview_agent.repository.PostCommentRepository;
import com.example.interview_agent.repository.PostRepository;
import com.example.interview_agent.repository.UserRepository;
import com.example.interview_agent.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostCommentService {

    @Autowired private PostCommentRepository postCommentRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public PostCommentDto createComment(Long postId, PostCommentDto commentDto) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("当前用户不存在"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章未找到: " + postId));

        PostComment comment = new PostComment();
        comment.setContent(commentDto.getContent());
        comment.setUser(currentUser);
        comment.setPost(post);

        // 处理回复逻辑
        if (commentDto.getParentId() != null) {
            PostComment parentComment = postCommentRepository.findById(commentDto.getParentId())
                    .orElseThrow(() -> new RuntimeException("父评论未找到: " + commentDto.getParentId()));
            comment.setParent(parentComment);
        }

        PostComment savedComment = postCommentRepository.save(comment);
        return convertToDto(savedComment);
    }

    @Transactional(readOnly = true)
    public List<PostCommentDto> getCommentsByPostId(Long postId) {
        // Service 只负责查询顶级评论，转换逻辑在 convertToDto 中递归处理
        List<PostComment> topLevelComments = postCommentRepository.findByPostIdAndParentIsNullOrderByCreateTimeDesc(postId);
        return topLevelComments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // DTO转换器，现在支持递归转换子评论
    private PostCommentDto convertToDto(PostComment comment) {
        PostCommentDto dto = new PostCommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setPostId(comment.getPost().getId());

        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }

        PostDto.UserInfo authorInfo = new PostDto.UserInfo();
        authorInfo.setId(comment.getUser().getId());
        authorInfo.setUsername(comment.getUser().getUsername());
        dto.setAuthor(authorInfo);

        dto.setCreateTime(comment.getCreateTime());

        // 递归转换子评论
        List<PostComment> replies = comment.getReplies();
        if (replies != null && !replies.isEmpty()) {
            dto.setReplies(
                    replies.stream()
                            .map(this::convertToDto) // 递归调用
                            .collect(Collectors.toList())
            );
        } else {
            dto.setReplies(Collections.emptyList()); // 确保 replies 字段不为 null
        }

        return dto;
    }
}