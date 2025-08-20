package com.example.interview_agent.dto;

public class BehavioralData {
    private String estimatedAge;
    private String estimatedGender;
    private String gazeDirection;
    private double headTilt;

    public BehavioralData() {}

    public String getEstimatedAge() { return estimatedAge; }
    public void setEstimatedAge(String estimatedAge) { this.estimatedAge = estimatedAge; }

    public String getEstimatedGender() { return estimatedGender; }
    public void setEstimatedGender(String estimatedGender) { this.estimatedGender = estimatedGender; }

    public String getGazeDirection() { return gazeDirection; }
    public void setGazeDirection(String gazeDirection) { this.gazeDirection = gazeDirection; }

    public double getHeadTilt() { return headTilt; }
    public void setHeadTilt(double headTilt) { this.headTilt = headTilt; }

    @Override
    public String toString() {
        return "BehavioralData{" +
                "estimatedAge='" + estimatedAge + '\'' +
                ", estimatedGender='" + estimatedGender + '\'' +
                ", gazeDirection='" + gazeDirection + '\'' +
                ", headTilt=" + headTilt +
                '}';
    }
}