package com.moodboard.repository;

import com.moodboard.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    void deleteBySessionId(Long sessionId);

    List<ChatMessage> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
}
