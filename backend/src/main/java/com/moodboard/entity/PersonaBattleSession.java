package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PersonaBattleSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public Long userId;

    public String title;

    @Column(columnDefinition = "TEXT")
    public String topic;

    /**
     * 参与人格，例如：INTJ,INFJ,ENFP
     */
    @Column(columnDefinition = "TEXT")
    public String personaCodes;

    /**
     * CHAT / ENDED
     */
    public String status = "CHAT";

    @Column(columnDefinition = "TEXT")
    public String summary;

    @Column(columnDefinition = "TEXT")
    public String preview;

    public LocalDateTime createdAt = LocalDateTime.now();

    public LocalDateTime updatedAt = LocalDateTime.now();
}