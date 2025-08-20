//package com.example.interview_agent.dto;
//
//import jakarta.persistence.*; // Spring Boot 3+ 使用 jakarta.persistence
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "comments")
//public class Comment {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, columnDefinition = "TEXT")
//    private String content;
//
//    @Column(name = "create_time", nullable = false)
//    private LocalDateTime createTime;
//
//    @Column(name = "user_id", nullable = false)
//    private Long userId;
//
//    @Column(name = "question_id", nullable = false)
//    private Long questionId;
//
//
//    public Comment() {
//    }
//
//
//    public Comment(String content, Long userId, Long questionId) {
//        this.content = content;
//        this.userId = userId;
//        this.questionId = questionId;
//        this.createTime = LocalDateTime.now(); // 在创建时设置当前时间
//    }
//
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getContent() {
//        return content;
//    }
//
//    public void setContent(String content) {
//        this.content = content;
//    }
//
//    public LocalDateTime getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(LocalDateTime createTime) {
//        this.createTime = createTime;
//    }
//
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public Long getQuestionId() {
//        return questionId;
//    }
//
//    public void setQuestionId(Long questionId) {
//        this.questionId = questionId;
//    }
//
//    @Override
//    public String toString() {
//        return "Comment{" +
//                "id=" + id +
//                ", content='" + content + '\'' +
//                ", createTime=" + createTime +
//                ", userId=" + userId +
//                ", questionId=" + questionId +
//                '}';
//    }
//}