package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BlindBoxMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long blindBoxId;
    public Long userId;
    public String role;
    public String persona;
    public Integer roundIndex;
    @Column(columnDefinition = "TEXT")
    public String content;
    public LocalDateTime createdAt = LocalDateTime.now();
}
