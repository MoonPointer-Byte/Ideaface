package com.example.interview_agent.dto;

import java.util.Objects;


public class AnswerPayload {

    private String sessionId;
    private String answerText;
    private EmotionData emotionData;
    private BehavioralData behavioralData;

    // A no-argument constructor is required by Jackson for deserialization
    public AnswerPayload() {
    }

    // --- Getters and Setters ---

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public EmotionData getEmotionData() {
        return emotionData;
    }

    public void setEmotionData(EmotionData emotionData) {
        this.emotionData = emotionData;
    }

    public BehavioralData getBehavioralData() { return behavioralData; }
    public void setBehavioralData(BehavioralData behavioralData) { this.behavioralData = behavioralData; }

    @Override
    public String toString() {
        return "AnswerPayload{" +
                "sessionId='" + sessionId + '\'' +
                ", answerText='" + answerText + '\'' +
                ", emotionData=" + emotionData +
                ", behavioralData=" + behavioralData + // 【关键修改】
                '}';
    }

    // equals() and hashCode() are good practice for DTOs
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnswerPayload that = (AnswerPayload) o;
        return Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(answerText, that.answerText) &&
                Objects.equals(emotionData, that.emotionData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, answerText, emotionData);
    }
}