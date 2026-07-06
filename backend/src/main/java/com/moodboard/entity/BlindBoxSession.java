package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BlindBoxSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Long userId;
    public String persona;
    public String topic;
    public Integer maxRounds = 3;
    public Integer currentRound = 0;
    public Boolean ephemeral = false;
    public LocalDateTime createdAt = LocalDateTime.now();
}
