package com.example.interview_agent.service;

import com.example.interview_agent.client.AsrClient;
import com.example.interview_agent.client.SparkClient;
import com.example.interview_agent.entity.InterviewReport;
import com.example.interview_agent.repository.InterviewReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InterviewAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(InterviewAnalysisService.class);

    private final AsrClient asrClient;
    private final SparkClient sparkClient;
    private final InterviewReportRepository reportRepository;

    @Autowired
    public InterviewAnalysisService(AsrClient asrClient, SparkClient sparkClient, InterviewReportRepository reportRepository) {
        this.asrClient = asrClient;
        this.sparkClient = sparkClient;
        this.reportRepository = reportRepository;
    }

    @Async
    @Transactional
    public void analyzeInterviewAsync(Path videoPath, String sessionId, String positionId) {
        logger.info("[{}] ▶️ 开始分析面试视频: {}", sessionId, videoPath.toString());
        long startTime = System.currentTimeMillis();

        InterviewReport report = reportRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("报告实体未找到: " + sessionId));

        try {

            logger.info("[{}] 步骤 1/4: 提取音频...", sessionId);
            File audioFile = extractAudio(videoPath, sessionId);
            report.setAudioFilePath(audioFile.getAbsolutePath());
            logger.info("[{}] ✅ 音频提取成功: {}", sessionId, audioFile.getAbsolutePath());

            logger.info("[{}] 步骤 2/4: 进行语音转文字 (讯飞)...", sessionId);
            String transcript = asrClient.transcribe(audioFile);
            if (transcript == null || transcript.isBlank()) {
                throw new IllegalStateException("语音转文字结果为空或空白");
            }
            logger.info("[{}] ✅ 语音转文字成功, 内容长度: {} 字符", sessionId, transcript.length());

            logger.info("[{}] 步骤 3/4: 调用大模型进行评估 (讯飞星火)...", sessionId);
            String analysisReportJson = sparkClient.evaluateTranscript(transcript, positionId);
            // 这里不再保存llmResponseJson，因为实体字段已更新
            logger.info("[{}] ✅ 大模型评估报告生成成功。", sessionId);

            report.setStatus("COMPLETED");

        } catch (Throwable t) {
            logger.error("[{}] ❌ 分析流程中发生严重错误", sessionId, t);
            report.setStatus("FAILED");
        } finally {
            reportRepository.save(report);
            long endTime = System.currentTimeMillis();
            logger.info("[{}] ⏹️ 分析流程结束，最终状态: {}，总耗时: {} ms", sessionId, report.getStatus(), (endTime - startTime));
        }
    }

    private File extractAudio(Path videoPath, String sessionId) throws IOException, InterruptedException {
        File sourceVideo = videoPath.toFile();
        File targetAudio = new File(videoPath.getParent().toString(), sessionId + "_audio.pcm");


        String ffmpegCommand = "ffmpeg";
        List<String> command = List.of(
                ffmpegCommand,
                "-i", sourceVideo.getAbsolutePath(),
                "-y", // 覆盖输出文件
                "-vn", // 无视频
                "-f", "s16le",
                "-ar", "16000",
                "-ac", "1",
                targetAudio.getAbsolutePath()
        );

        logger.info("准备执行FFmpeg命令以提取PCM裸流: {}", String.join(" ", command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("FFMPEG_LOG: {}", line);
            }
        }

        boolean finishedInTime = process.waitFor(3, TimeUnit.MINUTES);
        if (!finishedInTime) {
            process.destroyForcibly();
            throw new IOException("FFmpeg process timed out after 3 minutes.");
        }

        int exitCode = process.exitValue();
        logger.info("FFmpeg命令执行完毕，退出码: {}", exitCode);
        if (exitCode != 0) {
            throw new IOException("FFmpeg process exited with error code: " + exitCode);
        }

        return targetAudio;
    }

    @Async
    @Transactional
    public void generateReportFromSessionAsync(InterviewSession session) {
        String sessionId = session.getSessionId();
        logger.info("[{}] ▶️ 开始根据会话数据生成深度分析报告...", sessionId);
        long startTime = System.currentTimeMillis();

        InterviewReport report = reportRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalStateException("报告实体未找到: " + sessionId));
        report.setStatus("ANALYZING");
        reportRepository.save(report);

        try {
            String dialogueTranscript = buildTranscriptFromHistory(session.getHistory());
            String behaviorSummary = buildBehaviorSummary(session);
            String prompt = buildComprehensivePrompt(dialogueTranscript, behaviorSummary, report.getPositionTitle());

            logger.info("[{}] 步骤 1/2: 调用大模型进行最终评估...", sessionId);
            String rawLlmResponse = sparkClient.askQuestion(prompt);

            // 【【【核心修复】】】: 清洗并提取JSON字符串
            String cleanJson = extractJson(rawLlmResponse);
            if (cleanJson == null) {
                throw new com.google.gson.JsonSyntaxException("无法从大模型响应中提取有效的JSON对象。原始响应: " + rawLlmResponse);
            }

            logger.info("[{}] 步骤 2/2: 解析并保存评估结果...", sessionId);
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject result = gson.fromJson(cleanJson, com.google.gson.JsonObject.class);

            report.setOverallComment(result.get("overallComment").getAsString());
            report.setCapabilityAnalysisJson(result.get("capabilityAnalysis").toString());
            report.setImprovementSuggestionsJson(result.get("improvementSuggestions").toString());
            report.setDialogueHistoryJson(gson.toJson(session.getHistory()));
            report.setStatus("COMPLETED");

        } catch (Throwable t) {
            logger.error("[{}] ❌ 生成深度报告过程中发生严重错误", sessionId, t);
            report.setStatus("FAILED");
        } finally {
            reportRepository.save(report);
            long endTime = System.currentTimeMillis();
            logger.info("[{}] ⏹️ 深度报告分析流程结束，最终状态: {}，总耗时: {} ms", sessionId, report.getStatus(), (endTime - startTime));
        }
    }

    /**
     * 【【【新增辅助方法】】】
     * 使用正则表达式从一段可能混杂的文本中提取出最外层的JSON对象。
     * @param text 包含JSON的原始字符串
     * @return 纯净的JSON字符串，如果找不到则返回null
     */
    private String extractJson(String text) {
        // 这个正则表达式会寻找第一个 '{' 和最后一个 '}' 之间的所有内容
        final Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String buildTranscriptFromHistory(List<InterviewSession.QaPair> history) {
        StringBuilder sb = new StringBuilder();
        int questionNum = 1;
        for (InterviewSession.QaPair qa : history) {
            sb.append("【问题 ").append(questionNum++).append("】: ").append(qa.getQuestion()).append("\n");
            sb.append("【候选人回答】: ").append(qa.getAnswer()).append("\n");
            sb.append("【AI 当轮简评】: ").append(qa.getEvaluation()).append("\n\n");
        }
        return sb.toString();
    }

    private String buildBehaviorSummary(InterviewSession session) {
        return "行为数据未记录。";
    }

    // 【【【核心修改】】】: 升级Prompt以生成更丰富的内容
    private String buildComprehensivePrompt(String dialogueTranscript, String behaviorSummary, String positionTitle) {
        return String.format("""
        你是一位顶级的HR专家和资深技术面试官，以深刻、全面、建设性的反馈而闻名。请根据下面提供的候选人与AI面试官的完整对话记录，对候选人应聘【%s】岗位的表现进行一次全面、深入、专业的评估。

        【完整对话记录】
        %s
        
        【全程行为总结】
        %s

        【你的评估任务】
        请严格按照以下JSON格式输出你的评估结果，不要有任何多余的解释或说明。
        {
          "overallComment": "在这里对候选人的整体表现给出一个【200字左右】的详细综合评价。评价需要结合技术基础、项目经验、逻辑思维和沟通表达能力，指出其核心优势，并点明主要的潜力方向。评价应客观、专业且富有洞察力。",
          "capabilityAnalysis": {
            "专业知识": <在1.0到5.0之间打分>,
            "求职动机": <在1.0到5.0之间打分>,
            "项目经验": <在1.0到5.0之间打分>,
            "沟通表达": <在1.0到5.0之间打分>,
            "逻辑思维": <在1.0到5.0之间打分>
          },
          "improvementSuggestions": [
            { "dimension": "专业知识", "suggestion": "在这里填写针对'专业知识'维度的【第一条】具体、可执行的改进建议。" },
            { "dimension": "项目经验", "suggestion": "在这里填写针对'项目经验'维度的【第二条】具体、可执行的改进建议。" },
            { "dimension": "沟通表达", "suggestion": "在这里填写针对'沟通表达'维度的【第三条】具体、可执行的改进建议。" }
          ],
          "highlights": [
            { "highlight": "【必须填写】在这里总结出候选人的【第一个】最突出的亮点表现。即使表现平平，也要从中提炼出一个最值得肯定的地方。" },
            { "highlight": "在这里总结出候选人的【第二个】具体亮点表现，例如：在数据库架构和多版本维护方面展现出系统性的解决方案和实战经验，能有效保障系统的稳定性。" },
            { "highlight": "在这里总结出候选人的【第三个】具体亮点表现，例如：在回答复杂业务场景题时，能够快速拆解问题，展现了优秀的逻辑思维和问题解决能力。" }
          ]
        }
        """, positionTitle, dialogueTranscript, behaviorSummary);
    }
}