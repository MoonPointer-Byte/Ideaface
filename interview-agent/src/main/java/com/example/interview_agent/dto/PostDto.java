package com.example.interview_agent.dto;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class PostDto {
    private Long id;
    private String title;
    private String content;
    private UserInfo author;
    private int viewCount;
    private int likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private CategoryDto category;
    private Set<TagDto> tags = new HashSet<>();
    private Long categoryId;
    private Set<String> tagNames = new HashSet<>();


    public static class UserInfo {
        private Long id; // 【重要】确保这个ID字段存在
        private String username;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public UserInfo getAuthor() { return author; }
    public void setAuthor(UserInfo author) { this.author = author; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public CategoryDto getCategory() { return category; }
    public void setCategory(CategoryDto category) { this.category = category; }
    public Set<TagDto> getTags() { return tags; }
    public void setTags(Set<TagDto> tags) { this.tags = tags; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Set<String> getTagNames() { return tagNames; }
    public void setTagNames(Set<String> tagNames) { this.tagNames = tagNames; }
}