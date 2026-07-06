package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.ChatMessage;
import com.moodboard.entity.ChatSession;
import com.moodboard.repository.ChatMessageRepository;
import com.moodboard.repository.ChatSessionRepository;
import com.moodboard.service.AiService;
import com.moodboard.service.AuthService;
import com.moodboard.service.ChineseEmotionAnalyzer;
import com.moodboard.service.EmotionAnalysisResult;
import com.moodboard.service.PromptService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/ai/chat")
public class ChatController {
    private final AuthService authService;
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final PromptService promptService;
    private final AiService aiService;
    private final ChineseEmotionAnalyzer emotionAnalyzer;
    public ChatController(
        AuthService authService,
        ChatSessionRepository sessionRepo,
        ChatMessageRepository messageRepo,
        PromptService promptService,
        AiService aiService,
        ChineseEmotionAnalyzer emotionAnalyzer
) {
    this.authService = authService;
    this.sessionRepo = sessionRepo;
    this.messageRepo = messageRepo;
    this.promptService = promptService;
    this.aiService = aiService;
    this.emotionAnalyzer = emotionAnalyzer;
}

    @PostMapping("/sessions")
    public Map<String, Object> createSession(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        ChatSession s = new ChatSession();
        s.userId = userId; s.persona = MapUtil.str(body, "persona", "INFJ"); s.customPersonaId = MapUtil.longValue(body, "customPersonaId");
        s.title = MapUtil.str(body, "title", "新的树洞"); s.ephemeral = MapUtil.bool(body, "ephemeral", false);
        return R.ok(sessionRepo.save(s));
    }

    @GetMapping("/sessions")
    public Map<String, Object> sessions(@RequestHeader("Authorization") String auth) {
        Long userId = authService.currentUserId(auth);
        return R.ok(sessionRepo.findByUserIdOrderByUpdatedAtDesc(userId));
    }

    @GetMapping("/sessions/{id}")
    public Map<String, Object> messages(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        authService.currentUserId(auth);
        return R.ok(messageRepo.findBySessionIdOrderByCreatedAtAsc(id));
    }

    @PostMapping("/send")
    public Map<String, Object> send(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        String content = MapUtil.str(body, "content", "");
        EmotionAnalysisResult emotionResult = emotionAnalyzer.analyze(content);

        System.out.println(
                "[情感分析] userText=" + content
                        + ", emotion=" + emotionResult.getEmotion()
                        + ", intensity=" + emotionResult.getIntensity()
                        + ", style=" + emotionResult.getReplyStyle()
        );
        String persona = MapUtil.str(body, "persona", "INFJ");
        String chatType = MapUtil.str(body, "chatType", "PERSONA");
        Long customPersonaId = MapUtil.longValue(body, "customPersonaId");
        boolean ephemeral = MapUtil.bool(body, "ephemeral", false);
        Long sessionId = MapUtil.longValue(body, "sessionId");

        ChatSession session = null;
        if (!ephemeral) {
            if (sessionId == null || sessionId == 0) {
                session = new ChatSession();
                session.userId = userId;
                session.persona = persona;
                session.chatType = chatType;
                session.customPersonaId = customPersonaId;
                session.title = content.length() > 12 ? content.substring(0, 12) + "..." : content;
                session.ephemeral = false;
                session = sessionRepo.save(session);
            } else {
                session = sessionRepo.findById(sessionId).orElseThrow(); session.updatedAt = LocalDateTime.now(); sessionRepo.save(session);
            }
            ChatMessage userMsg = new ChatMessage();
            
            userMsg.sessionId = session.id; userMsg.userId = userId; userMsg.role = "user"; userMsg.persona = persona; userMsg.customPersonaId = customPersonaId;
            userMsg.content = content; userMsg.emotionTag = promptService.simpleEmotionTag(content); messageRepo.save(userMsg);
            userMsg.chatType = chatType;
        }

        String prompt;

        if ("DAILY".equalsIgnoreCase(chatType)) {
            prompt = promptService.buildDailyChatPrompt(userId);
        } else {
            prompt = promptService.buildChatPrompt(userId, persona, customPersonaId);
        }
        prompt = promptService.applyEmotionStyle(prompt, emotionResult);
        String reply = aiService.generate(prompt, List.of(Map.of("role", "user", "content", content)));
        String emotionTag = promptService.simpleEmotionTag(content);

        if (!ephemeral && session != null) {
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.sessionId = session.id; aiMsg.userId = userId; aiMsg.role = "assistant"; aiMsg.persona = persona; aiMsg.customPersonaId = customPersonaId;
            aiMsg.content = reply; aiMsg.emotionTag = emotionTag; messageRepo.save(aiMsg);
            aiMsg.chatType = chatType;
        }
        return R.ok(Map.of("sessionId", session == null ? 0 : session.id, "reply", reply, "emotionTag", emotionTag, "persona", persona, "createdAt", LocalDateTime.now().toString()));
    }

    @DeleteMapping("/sessions/{id}")
    public Map<String, Object> delete(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Long userId = authService.currentUserId(auth);
        ChatSession s = sessionRepo.findById(id).orElseThrow();
        if (!s.userId.equals(userId)) throw new RuntimeException("无权删除");
        sessionRepo.deleteById(id);
        return R.msg("删除成功");
    }
    @Transactional
    @DeleteMapping("/sessions/{id}/messages")
    public Map<String, Object> clearMessages(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id
    ) {
        Long userId = authService.currentUserId(auth);

        ChatSession session = sessionRepo.findById(id).orElseThrow();

        if (!session.userId.equals(userId)) {
            throw new RuntimeException("无权清空该会话");
        }

        messageRepo.deleteBySessionId(id);

        return R.msg("会话已清空");
    }

}
