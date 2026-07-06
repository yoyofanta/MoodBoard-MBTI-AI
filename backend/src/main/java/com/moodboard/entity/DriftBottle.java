package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class DriftBottle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    public String moodEmoji;

    @Column(columnDefinition = "TEXT")
    public String content;

    @Column(columnDefinition = "TEXT")
    public String aiEcho;

    public LocalDateTime createdAt = LocalDateTime.now();
}