package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 人格画像表
 * 用于存储人格简介、标签、向量特征和热度。
 */
@Entity
public class PersonaProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true, nullable = false)
    public String code;

    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    public String tags;

    @Column(columnDefinition = "TEXT")
    public String vectorJson;

    public Integer popularity = 0;

    public LocalDateTime createdAt = LocalDateTime.now();
}
