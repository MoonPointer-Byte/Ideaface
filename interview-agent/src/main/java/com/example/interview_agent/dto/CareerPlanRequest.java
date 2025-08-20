package com.example.interview_agent.dto;

import java.util.Objects;


public class CareerPlanRequest {


    private String currentSkills;
    private String targetPositionId;


    public CareerPlanRequest() {
    }


    public CareerPlanRequest(String currentSkills, String targetPositionId) {
        this.currentSkills = currentSkills;
        this.targetPositionId = targetPositionId;
    }


    public String getCurrentSkills() {
        return currentSkills;
    }

    public void setCurrentSkills(String currentSkills) {
        this.currentSkills = currentSkills;
    }

    public String getTargetPositionId() {
        return targetPositionId;
    }

    public void setTargetPositionId(String targetPositionId) {
        this.targetPositionId = targetPositionId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CareerPlanRequest that = (CareerPlanRequest) o;
        return Objects.equals(currentSkills, that.currentSkills) &&
                Objects.equals(targetPositionId, that.targetPositionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentSkills, targetPositionId);
    }

    @Override
    public String toString() {
        return "CareerPlanRequest{" +
                "currentSkills='" + currentSkills + '\'' +
                ", targetPositionId='" + targetPositionId + '\'' +
                '}';
    }
}