package com.moodboard.repository;

import com.moodboard.entity.UserPersonaProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPersonaProfileRepository extends JpaRepository<UserPersonaProfile, Long> {

    Optional<UserPersonaProfile> findByUserId(Long userId);
}