package com.moodboard.controller;

import com.moodboard.common.R;
import com.moodboard.service.AuthService;
import com.moodboard.service.MemoryChatService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/memory-chat")
public class MemoryChatController {

    private final MemoryChatService memoryChatService;
    private final AuthService authService;

    public MemoryChatController(
            MemoryChatService memoryChatService,
            AuthService authService
    ) {
        this.memoryChatService = memoryChatService;
        this.authService = authService;
    }

    @PostMapping("/send")
    public Map<String, Object> send(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body
    ) {
        try {
            Long userId = resolveUserId(auth);

            String content = str(body.get("content"));
            String chatType = str(body.get("chatType"));
            String personaCode = firstNotBlank(
                    str(body.get("personaCode")),
                    str(body.get("persona"))
            );
            String personaName = str(body.get("personaName"));

            if (content == null || content.isBlank()) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("reply", "请输入你想说的话。");
                return R.ok(data);
            }

            Map<String, Object> data = memoryChatService.chat(
                    userId,
                    content,
                    chatType,
                    personaCode,
                    personaName
            );

            return R.ok(data);
        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("reply", "Memory Chat 接口已捕获异常：" + e.getMessage());
            data.put("answer", "Memory Chat 接口已捕获异常：" + e.getMessage());

            return R.ok(data);
        }
    }

    private Long resolveUserId(String auth) {
        try {
            if (auth == null || auth.isBlank()) {
                return 1L;
            }

            return authService.currentUserId(auth);
        } catch (Exception e) {
            return 1L;
        }
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNotBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }

        return b == null ? "" : b;
    }
}