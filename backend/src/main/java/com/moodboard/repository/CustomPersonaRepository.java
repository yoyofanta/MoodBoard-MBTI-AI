package com.moodboard.repository;

import com.moodboard.entity.CustomPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomPersonaRepository extends JpaRepository<CustomPersona, Long> {
    List<CustomPersona> findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc(Long userId);
}
