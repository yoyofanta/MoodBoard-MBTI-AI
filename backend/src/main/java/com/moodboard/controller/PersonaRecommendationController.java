package com.moodboard.controller;

import com.moodboard.common.R;
import com.moodboard.service.AuthService;
import com.moodboard.service.PersonaRecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 人格智能推荐接口
 * 只给前端人格对话页面使用。
 */
@RestController
@RequestMapping("/api/persona-recommendations")
public class PersonaRecommendationController {

    private final AuthService authService;
    private final PersonaRecommendationService recommendationService;

    public PersonaRecommendationController(
            AuthService authService,
            PersonaRecommendationService recommendationService
    ) {
        this.authService = authService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public Map<String, Object> recommend(
            @RequestHeader("Authorization") String auth
    ) {
        Long userId = authService.currentUserId(auth);

        return R.ok(recommendationService.recommend(userId));
    }
}
