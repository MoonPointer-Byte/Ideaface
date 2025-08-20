package com.example.interview_agent.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_report")
public class InterviewReport {

    @Id
    @Column(name = "session_id", nullable = false, unique = true)
    private String sessionId;

    @Column(name = "position_id", nullable = false)
    private String positionId;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "user_id")
    private String userId;

    @Lob
    @Column(name = "dialogue_history_json", columnDefinition = "JSON")
    private String dialogueHistoryJson;

    @Lob
    @Column(name = "capability_analysis_json", columnDefinition = "JSON")
    private String capabilityAnalysisJson;

    @Lob
    @Column(name = "improvement_suggestions_json", columnDefinition = "JSON")
    private String improvementSuggestionsJson;

    // 【【【新增字段】】】
    @Lob
    @Column(name = "highlights_json", columnDefinition = "JSON")
    private String highlightsJson;

    @Lob
    @Column(name = "overall_comment", columnDefinition = "TEXT")
    private String overallComment;

    @Column(name = "video_file_path")
    private String videoFilePath;

    @Column(name = "audio_file_path")
    private String audioFilePath;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "status")
    private String status;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getPositionTitle() {
        return positionTitle;
    }

    public void setPositionTitle(String positionTitle) {
        this.positionTitle = positionTitle;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDialogueHistoryJson() {
        return dialogueHistoryJson;
    }

    public void setDialogueHistoryJson(String dialogueHistoryJson) {
        this.dialogueHistoryJson = dialogueHistoryJson;
    }

    public String getCapabilityAnalysisJson() {
        return capabilityAnalysisJson;
    }

    public void setCapabilityAnalysisJson(String capabilityAnalysisJson) {
        this.capabilityAnalysisJson = capabilityAnalysisJson;
    }

    public String getImprovementSuggestionsJson() {
        return improvementSuggestionsJson;
    }

    public void setImprovementSuggestionsJson(String improvementSuggestionsJson) {
        this.improvementSuggestionsJson = improvementSuggestionsJson;
    }

    public String getHighlightsJson() {
        return highlightsJson;
    }

    public void setHighlightsJson(String highlightsJson) {
        this.highlightsJson = highlightsJson;
    }

    public String getOverallComment() {
        return overallComment;
    }

    public void setOverallComment(String overallComment) {
        this.overallComment = overallComment;
    }

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}