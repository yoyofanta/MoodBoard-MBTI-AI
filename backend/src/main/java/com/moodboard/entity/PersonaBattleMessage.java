package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PersonaBattleMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long sessionId;

    public Long userId;

    /**
     * user / persona
     */
    public String role;

    /**
     * 如果 role = persona，这里存 INTJ / INFJ / ENFP
     */
    public String personaCode;

    @Column(columnDefinition = "TEXT")
    public String content;

    public LocalDateTime createdAt = LocalDateTime.now();
}
