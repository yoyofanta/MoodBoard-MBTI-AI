package com.moodboard.repository;

import com.moodboard.entity.EmotionDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmotionDiaryRepository extends JpaRepository<EmotionDiary, Long> {
    List<EmotionDiary> findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(Long userId, LocalDate start, LocalDate end);
    Optional<EmotionDiary> findByUserIdAndDiaryDate(Long userId, LocalDate diaryDate);
    List<EmotionDiary> findByUserIdOrderByDiaryDateDesc(Long userId);
}
