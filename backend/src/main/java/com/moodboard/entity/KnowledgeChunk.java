package com.moodboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_chunk")
public class KnowledgeChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 知识标题，例如：考试焦虑调节
     */
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 知识正文
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 标签，例如：焦虑,考试,睡眠
     */
    @Column(length = 255)
    private String tags;

    /**
     * 来源说明
     */
    @Column(length = 100)
    private String source;

    private LocalDateTime createdAt;

    public KnowledgeChunk() {
    }

    public KnowledgeChunk(String title, String content, String tags, String source) {
        this.title = title;
        this.content = content;
        this.tags = tags;
        this.source = source;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getTags() {
        return tags;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}