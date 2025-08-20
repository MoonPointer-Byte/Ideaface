package com.example.interview_agent.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PostCommentDto {
    private Long id;
    private String content;
    private PostDto.UserInfo author;
    private Long postId;
    private Long parentId;
    private List<PostCommentDto> replies;
    private LocalDateTime createTime;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public PostDto.UserInfo getAuthor() {
        return author;
    }

    public void setAuthor(PostDto.UserInfo author) {
        this.author = author;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public List<PostCommentDto> getReplies() {
        return replies;
    }

    public void setReplies(List<PostCommentDto> replies) {
        this.replies = replies;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}