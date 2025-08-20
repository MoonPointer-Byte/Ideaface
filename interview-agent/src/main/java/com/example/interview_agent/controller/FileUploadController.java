package com.example.interview_agent.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-url-prefix}")
    private String accessUrlPrefix;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "上传的文件不能为空"));
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf('.') != -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }

            String uniqueFileName = UUID.randomUUID().toString() + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            String backendBaseUrl = "http://localhost:8080";
            String fileAccessUrl = backendBaseUrl + accessUrlPrefix + "/" + uniqueFileName;

            Map<String, Object> data = new HashMap<>();
            Map<String, String> succMap = new HashMap<>();
            succMap.put(originalFilename, fileAccessUrl);
            data.put("succMap", succMap);

            Map<String, Object> response = new HashMap<>();
            response.put("msg", "上传成功");
            response.put("code", 0);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "文件上传失败"));
        }
    }
}