package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户画像表
 * 用于人格推荐，存储用户 MBTI、兴趣文本、历史对话摘要等。
 */
@Entity
public class UserPersonaProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public Long userId;

    public String mbti;

    @Column(columnDefinition = "TEXT")
    public String featureText;

    @Column(columnDefinition = "TEXT")
    public String historySummary;

    public LocalDateTime updatedAt = LocalDateTime.now();
}
