package com.moodboard.repository;

import com.moodboard.entity.PersonaBattleMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonaBattleMessageRepository extends JpaRepository<PersonaBattleMessage, Long> {

    List<PersonaBattleMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}