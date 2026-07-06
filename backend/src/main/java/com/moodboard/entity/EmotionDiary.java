package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class EmotionDiary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false)
    public Long userId;
    public LocalDate diaryDate;
    public String emotionEmoji;
    public String emotionLabel;
    public String keyword;
    @Column(columnDefinition = "TEXT")
    public String content;
    @Column(columnDefinition = "TEXT")
    public String imageUrl;
    public Double latitude;
    public Double longitude;
    public String province;
    public String city;
    public String district;
    public String locationName;
    public Boolean isShared = false;
    public String sourceType;
    public Long sourceId;
    public String personaTag;
    public String personaPair;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
