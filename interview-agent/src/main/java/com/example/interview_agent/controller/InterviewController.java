package com.example.interview_agent.controller;

import com.example.interview_agent.dto.AnswerPayload;
import com.example.interview_agent.dto.StartInterviewRequest;
import com.example.interview_agent.entity.InterviewReport;
import com.example.interview_agent.repository.InterviewReportRepository;
import com.example.interview_agent.security.UserDetailsImpl;
import com.example.interview_agent.service.InterviewAnalysisService;
import com.example.interview_agent.service.InteractiveInterviewService;
import com.example.interview_agent.service.InterviewSession;
import com.example.interview_agent.service.ResumeParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class InterviewController {

    private static final Logger logger = LoggerFactory.getLogger(InterviewController.class);

    @Autowired
    private InteractiveInterviewService interactiveService;

    @Autowired
    private InterviewAnalysisService analysisService;

    @Autowired
    private InterviewReportRepository reportRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private ResumeParserService resumeParserService;

    // startInterview 方法保持不变
    @PostMapping("/interactive-interview/start")
    public ResponseEntity<?> startInterview(@RequestBody StartInterviewRequest request, Authentication authentication) {
        String sessionId = request.getSessionId();
        String positionId = request.getPositionId();
        logger.info("[{}] 收到 /start 请求, 岗位: {}", sessionId, positionId);

        if (sessionId == null || sessionId.isBlank() || positionId == null || positionId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "sessionId and positionId are required."));
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // 创建初始报告记录
        InterviewReport report = new InterviewReport();
        report.setSessionId(sessionId);
        report.setPositionId(positionId);
        report.setUserId(String.valueOf(userId));
        report.setCreateTime(LocalDateTime.now());
        report.setStatus("ONGOING"); // 初始状态为进行中
        reportRepository.save(report);
        logger.info("[{}] 已为用户ID {} 创建初始面试报告记录。", sessionId, userId);

        try {
            InterviewSession session = interactiveService.startInterview(sessionId, positionId);
            String firstQuestion = session.getQuestions().get(0);
            return ResponseEntity.ok(Map.of("question", firstQuestion));
        } catch (Exception e) {
            logger.error("[{}] 开始面试时发生内部错误", sessionId, e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to start interview: " + e.getMessage()));
        }
    }

    // submitAnswer 方法保持不变
    @PostMapping("/interactive-interview/answer")
    public ResponseEntity<?> submitAnswer(@RequestBody AnswerPayload payload) {
        String sessionId = payload.getSessionId();
        logger.info("[{}] 收到回答数据: {}", sessionId, payload.toString());
        try {
            InterviewSession session = interactiveService.processAnswer(
                    payload.getSessionId(),
                    payload.getAnswerText(),
                    payload.getEmotionData(),
                    payload.getBehavioralData()
            );
            String nextQuestion = interactiveService.getNextQuestion(sessionId);
            if (nextQuestion != null) {
                String lastEvaluation = session.getAnswerEvaluations().get(session.getAnswerEvaluations().size() - 1);
                return ResponseEntity.ok(Map.of("question", nextQuestion, "evaluation", lastEvaluation));
            } else {

                String finalEvaluation = session.getAnswerEvaluations().get(session.getAnswerEvaluations().size() - 1);

                return ResponseEntity.ok(Map.of("evaluation", finalEvaluation, "message", "面试所有问题已回答完毕，请点击'结束面试'生成最终报告。"));
            }
        } catch (Exception e) {
            logger.error("[{}] 处理回答时发生内部错误。Payload: {}", sessionId, payload.toString(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "处理回答时服务器发生内部错误，请稍后重试。"));
        }
    }

    @PostMapping("/interactive-interview/upload-resume")
    public ResponseEntity<Map<String, String>> uploadResume(@RequestParam("file") MultipartFile file,
                                                            @RequestParam("sessionId") String sessionId,
                                                            @RequestParam("positionId") String positionId) {
        logger.info("[{}] 接收到简历上传请求，文件名: {}", sessionId, file.getOriginalFilename());
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "上传的文件不能为空"));
        }
        try {
            String resumeText = resumeParserService.parse(file);
            interactiveService.saveResumeTextToSession(sessionId, positionId, resumeText);
            return ResponseEntity.ok(Map.of("message", "简历上传并解析成功"));
        } catch (IllegalArgumentException e) {
            logger.warn("[{}] 简历上传失败: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("[{}] 处理上传的简历时发生内部错误", sessionId, e);
            return ResponseEntity.status(500).body(Map.of("error", "服务器处理简历失败，请稍后重试"));
        }
    }


    @PostMapping("/interviews/{sessionId}/end")
    public ResponseEntity<?> endInterview(@PathVariable String sessionId) {
        interactiveService.finish(sessionId);
        return ResponseEntity.accepted().body(Map.of("message", "Request accepted. Report generation is in progress."));
    }


    @GetMapping("/reports/{sessionId}")
    public ResponseEntity<InterviewReport> getReport(@PathVariable String sessionId) {
        return reportRepository.findById(sessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


}