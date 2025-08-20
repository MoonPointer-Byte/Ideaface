package com.example.interview_agent.dto;

import java.util.Objects;

/**
 * DTO for Facial Emotion Data.
 * This is a standard POJO without Lombok annotations.
 */
public class EmotionData {

    private double neutral;
    private double happy;
    private double sad;
    private double angry;
    private double fearful;
    private double disgusted;
    private double surprised;

    public EmotionData() {
    }



    public double getNeutral() {
        return neutral;
    }

    public void setNeutral(double neutral) {
        this.neutral = neutral;
    }

    public double getHappy() {
        return happy;
    }

    public void setHappy(double happy) {
        this.happy = happy;
    }

    public double getSad() {
        return sad;
    }

    public void setSad(double sad) {
        this.sad = sad;
    }

    public double getAngry() {
        return angry;
    }

    public void setAngry(double angry) {
        this.angry = angry;
    }

    public double getFearful() {
        return fearful;
    }

    public void setFearful(double fearful) {
        this.fearful = fearful;
    }

    public double getDisgusted() {
        return disgusted;
    }

    public void setDisgusted(double disgusted) {
        this.disgusted = disgusted;
    }

    public double getSurprised() {
        return surprised;
    }

    public void setSurprised(double surprised) {
        this.surprised = surprised;
    }

    @Override
    public String toString() {
        return "EmotionData{" +
                "neutral=" + neutral +
                ", happy=" + happy +
                ", sad=" + sad +
                ", angry=" + angry +
                ", fearful=" + fearful +
                ", disgusted=" + disgusted +
                ", surprised=" + surprised +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmotionData that = (EmotionData) o;
        return Double.compare(that.neutral, neutral) == 0 &&
                Double.compare(that.happy, happy) == 0 &&
                Double.compare(that.sad, sad) == 0 &&
                Double.compare(that.angry, angry) == 0 &&
                Double.compare(that.fearful, fearful) == 0 &&
                Double.compare(that.disgusted, disgusted) == 0 &&
                Double.compare(that.surprised, surprised) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(neutral, happy, sad, angry, fearful, disgusted, surprised);
    }
}