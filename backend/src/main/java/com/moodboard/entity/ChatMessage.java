package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long sessionId;
    public Long userId;
    public String role;
    public String persona;
    public String chatType = "PERSONA"; // DAILY / PERSONA
    public Long customPersonaId;
    @Column(columnDefinition = "TEXT")
    public String content;
    public String emotionTag;
    public LocalDateTime createdAt = LocalDateTime.now();
}
