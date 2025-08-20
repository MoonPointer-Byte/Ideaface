package com.example.interview_agent.service;

import com.example.interview_agent.dto.LoginRequest;
import com.example.interview_agent.dto.RegisterRequest;
import com.example.interview_agent.entity.User;
import com.example.interview_agent.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // 新增
import java.io.IOException;                             // 新增
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired private UserService userService; // 【新增】注入UserService来复用上传逻辑


    @Transactional
    public void register(RegisterRequest request, MultipartFile avatarFile) throws IOException {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("错误: 用户名已被占用!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("错误: 邮箱已被使用!");
        }
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        // 保存用户以获取ID
        User savedUser = userRepository.saveAndFlush(user);

        // 判断用户是否上传了自定义头像
        if (avatarFile != null && !avatarFile.isEmpty()) {
            // 如果上传了，调用 UserService 中的方法来处理文件并更新用户
            userService.updateUserAvatar(savedUser.getId(), avatarFile);
        } else {
            // 如果没上传，生成一个默认的 multiavatar 头像
            String defaultAvatar = "https://api.multiavatar.com/" + request.getUsername() + ".svg";
            savedUser.setAvatar(defaultAvatar);
            userRepository.save(savedUser);
        }
    }

    public Optional<User> login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }
}