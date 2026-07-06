package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class CustomPersona {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public String name;
    public String basePersona;
    public String avatar;
    public String themeColor;
    public String callName;
    @Column(columnDefinition = "TEXT")
    public String promptSuffix;
    public Boolean isDeleted = false;
    public LocalDateTime createdAt = LocalDateTime.now();
    public LocalDateTime updatedAt = LocalDateTime.now();
}
