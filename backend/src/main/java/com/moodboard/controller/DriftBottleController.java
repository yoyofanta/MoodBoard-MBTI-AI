package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.DriftBottle;
import com.moodboard.repository.DriftBottleRepository;
import com.moodboard.service.AiService;
import com.moodboard.service.AuthService;
import com.moodboard.service.PromptService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drift-bottles")
public class DriftBottleController {

    private final AuthService authService;
    private final DriftBottleRepository bottleRepo;
    private final AiService aiService;
    private final PromptService promptService;

    public DriftBottleController(
            AuthService authService,
            DriftBottleRepository bottleRepo,
            AiService aiService,
            PromptService promptService
    ) {
        this.authService = authService;
        this.bottleRepo = bottleRepo;
        this.aiService = aiService;
        this.promptService = promptService;
    }

    @PostMapping("/throw")
    public Map<String, Object> throwBottle(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = authService.currentUserId(auth);

        String content = MapUtil.str(body, "content", "");
        String moodEmoji = MapUtil.str(body, "moodEmoji", "🫧");

        if (content.isBlank()) {
            throw new RuntimeException("请先写下你的心情");
        }

        String prompt = promptService.buildDriftBottlePrompt(userId);

        String aiEcho = aiService.generate(
                prompt,
                List.of(Map.of(
                        "role", "user",
                        "content", "我的匿名心情漂流瓶是：" + content
                ))
        );

        DriftBottle bottle = new DriftBottle();
        bottle.userId = userId;
        bottle.content = content;
        bottle.moodEmoji = moodEmoji;
        bottle.aiEcho = aiEcho;

        bottleRepo.save(bottle);

        return R.ok(Map.of(
                "bottle", bottle,
                "aiEcho", aiEcho
        ));
    }
}