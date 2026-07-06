package com.moodboard.repository;

import com.moodboard.entity.PlazaPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlazaPostRepository extends JpaRepository<PlazaPost, Long> {
    List<PlazaPost> findTop50ByOrderByCreatedAtDesc();
    List<PlazaPost> findByUserIdOrderByCreatedAtDesc(Long userId);
}
