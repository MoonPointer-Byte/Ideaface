package com.example.interview_agent.dto;

import java.util.Objects;


public class StartInterviewRequest {


    private String sessionId;
    private String positionId;


    public StartInterviewRequest() {
    }


    public StartInterviewRequest(String sessionId, String positionId) {
        this.sessionId = sessionId;
        this.positionId = positionId;
    }



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



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StartInterviewRequest that = (StartInterviewRequest) o;
        return Objects.equals(sessionId, that.sessionId) && Objects.equals(positionId, that.positionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, positionId);
    }

    @Override
    public String toString() {
        return "StartInterviewRequest{" +
                "sessionId='" + sessionId + '\'' +
                ", positionId='" + positionId + '\'' +
                '}';
    }
}