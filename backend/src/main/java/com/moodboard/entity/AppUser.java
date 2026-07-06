package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(unique = true, nullable = false)
    public String username;
    @Column(nullable = false)
    public String password;
    public LocalDateTime createdAt = LocalDateTime.now();
}
