package com.example.interview_agent.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InterviewSession {

    private String sessionId;
    private String positionId;
    private int currentQuestionIndex = 0;
    private List<String> questions = new ArrayList<>();
    private List<String> userAnswers = new ArrayList<>();
    private List<String> answerEvaluations = new ArrayList<>();
    private final List<QaPair> history = new ArrayList<>();
    private String resumeText;


    public InterviewSession() {}

    public InterviewSession(String sessionId, String positionId) {
        this.sessionId = sessionId;
        this.positionId = positionId;
    }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getPositionId() { return positionId; }
    public void setPositionId(String positionId) { this.positionId = positionId; }
    public int getCurrentQuestionIndex() { return currentQuestionIndex; }
    public void setCurrentQuestionIndex(int currentQuestionIndex) { this.currentQuestionIndex = currentQuestionIndex; }
    public List<String> getQuestions() { return questions; }
    public void setQuestions(List<String> questions) { this.questions = questions; }
    public List<String> getUserAnswers() { return userAnswers; }
    public void setUserAnswers(List<String> userAnswers) { this.userAnswers = userAnswers; }
    public List<String> getAnswerEvaluations() { return answerEvaluations; }
    public void setAnswerEvaluations(List<String> answerEvaluations) { this.answerEvaluations = answerEvaluations; }

    public List<QaPair> getHistory() { return history; }
    @Override
    public String toString() {
        return "InterviewSession{" +
                "sessionId='" + sessionId + '\'' +
                ", positionId='" + positionId + '\'' +
                ", currentQuestionIndex=" + currentQuestionIndex +
                '}';
    }
    public static class QaPair {
        private String question;
        private String answer;
        private String evaluation;

        public String getQuestion() {
            return question;
        }
        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }
        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getEvaluation() {
            return evaluation;
        }
        public void setEvaluation(String evaluation) {
            this.evaluation = evaluation;
        }
    }

    public String getResumeText() {
        return resumeText;
    }

    public void setResumeText(String resumeText) {
        this.resumeText = resumeText;
    }
}