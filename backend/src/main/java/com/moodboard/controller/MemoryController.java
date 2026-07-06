package com.moodboard.controller;

import com.moodboard.common.R;
import com.moodboard.service.AuthService;
import com.moodboard.service.MemoryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private final MemoryService memoryService;
    private final AuthService authService;

    public MemoryController(MemoryService memoryService, AuthService authService) {
        this.memoryService = memoryService;
        this.authService = authService;
    }

    /**
     * 获取当前用户记忆
     */
    @GetMapping("/current")
    public Map<String, Object> current(
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        Long userId = authService.currentUserId(auth);
        return R.ok(memoryService.getMemory(userId));
    }

    /**
     * 手动更新当前用户记忆
     */
    @PostMapping("/update")
    public Map<String, Object> update(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = authService.currentUserId(auth);

        String recentEmotionSummary = str(body.get("recentEmotionSummary"));
        String recentPersonaCode = str(body.get("recentPersonaCode"));
        String recentPersonaName = str(body.get("recentPersonaName"));
        String chatSummary = str(body.get("chatSummary"));
        String lastQuestion = str(body.get("lastQuestion"));
        String lastAnswer = str(body.get("lastAnswer"));

        return R.ok(memoryService.updateMemory(
                userId,
                recentEmotionSummary,
                recentPersonaCode,
                recentPersonaName,
                chatSummary,
                lastQuestion,
                lastAnswer
        ));
    }

    /**
     * 清空当前用户记忆
     */
    @PostMapping("/clear")
    public Map<String, Object> clear(
            @RequestHeader(value = "Authorization", required = false) String auth
    ) {
        Long userId = authService.currentUserId(auth);
        return R.ok(memoryService.clearMemory(userId));
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}