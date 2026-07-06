package com.moodboard.repository;

import com.moodboard.entity.PersonaBattleSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonaBattleSessionRepository extends JpaRepository<PersonaBattleSession, Long> {

    List<PersonaBattleSession> findByUserIdOrderByUpdatedAtDesc(Long userId);
}