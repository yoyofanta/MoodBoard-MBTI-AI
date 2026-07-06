package com.moodboard.service;

public class EmotionAnalysisResult {

    public String emotion;
    public int intensity;
    public String replyStyle;

    public EmotionAnalysisResult(String emotion, int intensity, String replyStyle) {
        this.emotion = emotion;
        this.intensity = intensity;
        this.replyStyle = replyStyle;
    }

    public String getEmotion() {
        return emotion;
    }

    public int getIntensity() {
        return intensity;
    }

    public String getReplyStyle() {
        return replyStyle;
    }
}