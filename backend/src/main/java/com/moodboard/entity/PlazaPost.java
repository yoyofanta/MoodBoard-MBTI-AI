package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PlazaPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public String sourceType;
    public Long sourceId;
    public String emotionEmoji;
    public String personaTag;
    @Column(columnDefinition = "TEXT")
    public String content;
    public LocalDateTime createdAt = LocalDateTime.now();
}
