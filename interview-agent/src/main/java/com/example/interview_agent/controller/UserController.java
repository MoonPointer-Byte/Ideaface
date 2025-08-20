package com.example.interview_agent.controller;

import com.example.interview_agent.entity.InterviewReport;
import com.example.interview_agent.repository.InterviewReportRepository;
import com.example.interview_agent.repository.UserRepository;
import com.example.interview_agent.security.UserDetailsImpl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.interview_agent.dto.UpdateUserRequest;
import com.example.interview_agent.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.example.interview_agent.dto.UpdatePasswordRequest;
import com.example.interview_agent.dto.UserInfo;
import com.example.interview_agent.entity.User;
import org.springframework.web.multipart.MultipartFile;
import com.example.interview_agent.dto.PostDto;
import com.example.interview_agent.service.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewReportRepository interviewReportRepository;

    @Autowired private UserService userService; // 【新增】

    @Autowired private PostService postService; // 【新增】

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        logger.info("Fetching profile for user ID: {}", userId);

        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "email", user.getEmail()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/history")
    public ResponseEntity<?> getInterviewHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String userIdStr = String.valueOf(userDetails.getId());
        logger.info("Fetching interview history for user ID: {}", userIdStr);
        List<InterviewReport> history = interviewReportRepository.findByUserIdOrderByCreateTimeDesc(userIdStr);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/avatar/{username}.svg")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String username) {
        String url = "https://api.multiavatar.com/" + username + ".svg";
        logger.info("开始代理请求头像, URL: {}", url);


        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            logger.info("收到来自 multiavatar 的响应, 状态码: {}", response.code());
            if (!response.isSuccessful()) {
                logger.error("代理请求失败, 响应体: {}", response.body() != null ? response.body().string() : "null");
                return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
            }

            ResponseBody body = response.body();
            if (body == null) {
                logger.warn("响应体为空");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("image/svg+xml"));
            headers.setCacheControl("public, max-age=86400");

            logger.info("成功获取头像并返回给前端");
            return new ResponseEntity<>(body.bytes(), headers, HttpStatus.OK);

        } catch (IOException e) {
            logger.error("代理头像请求时发生IO异常", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UpdateUserRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        try {
            userService.updateUserProfile(currentUserId, request);
            return ResponseEntity.ok(Map.of("message", "个人资料更新成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        try {
            userService.updatePassword(userDetails.getId(), request);
            return ResponseEntity.ok(Map.of("message", "密码修改成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/avatar/regenerate")
    public ResponseEntity<UserInfo> regenerateAvatar() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User updatedUser = userService.regenerateAvatar(userDetails.getId());
        return ResponseEntity.ok(UserInfo.fromEntity(updatedUser));
    }

    @PostMapping("/avatar/upload")
    public ResponseEntity<?> uploadUserAvatar(@RequestParam("avatarFile") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        try {
            User updatedUser = userService.updateUserAvatar(userDetails.getId(), file);
            return ResponseEntity.ok(UserInfo.fromEntity(updatedUser));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "文件上传失败"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{userId}/posts")
    public ResponseEntity<Page<PostDto>> getPostsByUserId(
            @PathVariable Long userId,
            Pageable pageable
    ) {

        return ResponseEntity.ok(postService.getPostsByUserId(userId, pageable));
    }


    @GetMapping("/{userId}/public-profile")
    public ResponseEntity<?> getPublicUserProfile(@PathVariable Long userId) {
        return userRepository.findById(userId)
                .map(user -> ResponseEntity.ok(Map.of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "avatar", user.getAvatar()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}