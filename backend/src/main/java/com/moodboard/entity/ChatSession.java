package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChatSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public String persona;
    public Long customPersonaId;
    public String chatType = "PERSONA"; // DAILY / PERSONA
    public String title;
    public Boolean ephemeral = false;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
