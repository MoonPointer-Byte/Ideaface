package com.example.interview_agent.dto;

import com.example.interview_agent.entity.User;
import java.io.Serializable;

public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String email;

    private String avatar;

    public UserInfo() {}

    public UserInfo(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }


    public static UserInfo fromEntity(User user) {
        if (user == null) return null;
        UserInfo userInfo = new UserInfo(user.getId(), user.getUsername(), user.getEmail());
        userInfo.setAvatar(user.getAvatar()); // 设置头像
        return userInfo;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}