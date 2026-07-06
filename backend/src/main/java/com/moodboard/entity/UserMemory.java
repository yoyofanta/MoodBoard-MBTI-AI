package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_memory")
public class UserMemory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID，一名用户对应一条长期记忆
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * 最近情绪摘要，例如：最近主要是考试焦虑、拖延和睡眠不足
     */
    @Column(columnDefinition = "TEXT")
    private String recentEmotionSummary;

    /**
     * 最近常聊人格编码，例如 INFJ、INTJ
     */
    @Column(length = 20)
    private String recentPersonaCode;

    /**
     * 最近常聊人格名称
     */
    @Column(length = 100)
    private String recentPersonaName;

    /**
     * 最近一次对话摘要
     */
    @Column(columnDefinition = "TEXT")
    private String chatSummary;

    /**
     * 最近一次用户问题
     */
    @Column(columnDefinition = "TEXT")
    private String lastQuestion;

    /**
     * 最近一次AI回答
     */
    @Column(columnDefinition = "TEXT")
    private String lastAnswer;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public UserMemory() {
    }

    public UserMemory(Long userId) {
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getRecentEmotionSummary() {
        return recentEmotionSummary;
    }

    public String getRecentPersonaCode() {
        return recentPersonaCode;
    }

    public String getRecentPersonaName() {
        return recentPersonaName;
    }

    public String getChatSummary() {
        return chatSummary;
    }

    public String getLastQuestion() {
        return lastQuestion;
    }

    public String getLastAnswer() {
        return lastAnswer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setRecentEmotionSummary(String recentEmotionSummary) {
        this.recentEmotionSummary = recentEmotionSummary;
    }

    public void setRecentPersonaCode(String recentPersonaCode) {
        this.recentPersonaCode = recentPersonaCode;
    }

    public void setRecentPersonaName(String recentPersonaName) {
        this.recentPersonaName = recentPersonaName;
    }

    public void setChatSummary(String chatSummary) {
        this.chatSummary = chatSummary;
    }

    public void setLastQuestion(String lastQuestion) {
        this.lastQuestion = lastQuestion;
    }

    public void setLastAnswer(String lastAnswer) {
        this.lastAnswer = lastAnswer;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}