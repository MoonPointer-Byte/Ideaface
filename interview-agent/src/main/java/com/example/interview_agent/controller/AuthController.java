package com.example.interview_agent.controller;

import com.example.interview_agent.dto.AuthResponse;
import com.example.interview_agent.dto.LoginRequest;
import com.example.interview_agent.dto.RegisterRequest;
import com.example.interview_agent.entity.User;
import com.example.interview_agent.service.AuthService;
import com.example.interview_agent.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.interview_agent.dto.UserInfo;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestPart("registerRequest") RegisterRequest registerRequest,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile
    ) {
        try {
            authService.register(registerRequest, avatarFile);
            return new ResponseEntity<>(Map.of("message", "用户注册成功!"), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件处理错误"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );


            String jwtToken = jwtUtils.generateJwtToken(authentication);

            Optional<User> userOptional = authService.login(request);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserInfo userInfo = UserInfo.fromEntity(user);

                logger.info("✅ 登录成功: user={}, JWT生成成功", userInfo.getUsername());

                return ResponseEntity.ok(Map.of(
                    "token", jwtToken,
                    "user", userInfo
                ));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "User not found"));
            }
        } catch (Exception e) {
            logger.error("登录失败: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }
    }

}