package com.example.interview_agent.controller;

import com.example.interview_agent.service.ResumeAssessmentService;
import com.example.interview_agent.service.ResumeOptimizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private static final Logger log = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeAssessmentService resumeAssessmentService;

    @Autowired
    private ResumeOptimizationService resumeOptimizationService;

    @PostMapping("/assess-file")
    public ResponseEntity<?> assessResumeByFile(
            @RequestParam("positionId") String positionId,
            @RequestParam("resumeFile") MultipartFile resumeFile) {

        if (resumeFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "上传的文件不能为空。"));
        }

        log.info("API层接收到文件评估请求，岗位ID: {}, 文件名: {}", positionId, resumeFile.getOriginalFilename());

        try {
            String assessmentReport = resumeAssessmentService.assessResumeFromFile(positionId, resumeFile);
            return ResponseEntity.ok(Map.of("assessment", assessmentReport));
        } catch (IllegalArgumentException e) {
            log.warn("处理简历失败，参数无效: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("处理简历时发生内部服务器错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "服务器在解析或评估简历时发生意外错误，请稍后重试。"));
        }
    }

    @PostMapping("/optimize")
    public ResponseEntity<?> optimizeResume(
            @RequestParam("targetPositionId") String targetPositionId,
            @RequestParam("resumeFile") MultipartFile resumeFile) {

        if (resumeFile.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "The uploaded file cannot be empty."));
        }

        log.info("API layer received request to optimize resume for position: {}", targetPositionId);

        try {
            String suggestions = resumeOptimizationService.optimizeResume(targetPositionId, resumeFile);
            return ResponseEntity.ok(Map.of("suggestions", suggestions));
        } catch (IllegalArgumentException e) {
            log.warn("Bad request for resume optimization: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("An internal server error occurred during resume optimization", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred on the server while optimizing the resume."));
        }
    }
}