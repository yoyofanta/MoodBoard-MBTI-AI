package com.moodboard.repository;

import com.moodboard.entity.BlindBoxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BlindBoxMessageRepository extends JpaRepository<BlindBoxMessage, Long> {
    List<BlindBoxMessage> findByBlindBoxIdOrderByCreatedAtAsc(Long blindBoxId);
}
