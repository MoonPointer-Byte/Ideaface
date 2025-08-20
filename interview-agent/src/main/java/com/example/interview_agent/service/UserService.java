package com.example.interview_agent.service;

import com.example.interview_agent.dto.UpdatePasswordRequest;
import com.example.interview_agent.dto.UpdateUserRequest;
import com.example.interview_agent.entity.User;
import com.example.interview_agent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // 1. 【新增】导入
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value; // 新增
import org.springframework.web.multipart.MultipartFile;   // 新增
import java.io.IOException;                               // 新增
import java.nio.file.Files;                               // 新增
import java.nio.file.Path;                                // 新增
import java.nio.file.Paths;                               // 新增
import java.util.UUID;                                    // 新增

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Value("${file.access-url-prefix}")
    private String accessUrlPrefix;

    @Transactional
    public User updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("用户名已被占用");
        }

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱已被占用");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public User regenerateAvatar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        String newAvatarUrl = "https://api.multiavatar.com/" + user.getUsername() + ".svg?t=" + System.currentTimeMillis();
        user.setAvatar(newAvatarUrl);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserAvatar(Long userId, MultipartFile avatarFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        if (avatarFile.isEmpty()) {
            throw new IllegalArgumentException("上传的头像文件不能为空");
        }

        String originalFilename = avatarFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String uniqueFileName = "avatar-" + userId + "-" + UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(uniqueFileName);
        avatarFile.transferTo(filePath.toFile());


        String backendBaseUrl = "http://localhost:8080";
        String fileAccessUrl = backendBaseUrl + accessUrlPrefix + "/" + uniqueFileName;

        user.setAvatar(fileAccessUrl);

        user.setAvatarType("custom");

        return userRepository.save(user);
    }
}