package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.PersonaBattleMessage;
import com.moodboard.entity.PersonaBattleSession;
import com.moodboard.repository.PersonaBattleMessageRepository;
import com.moodboard.repository.PersonaBattleSessionRepository;
import com.moodboard.service.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/persona-battle-sessions")
public class PersonaBattleSessionController {

    private final AuthService authService;
    private final PersonaBattleSessionRepository sessionRepo;
    private final PersonaBattleMessageRepository messageRepo;

    public PersonaBattleSessionController(
            AuthService authService,
            PersonaBattleSessionRepository sessionRepo,
            PersonaBattleMessageRepository messageRepo
    ) {
        this.authService = authService;
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
    }

    /**
     * 查询当前用户的多人格对战会话列表
     */
    @GetMapping
    public Map<String, Object> list(
            @RequestHeader("Authorization") String auth
    ) {
        Long userId = authService.currentUserId(auth);

        List<Map<String, Object>> list = sessionRepo
                .findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::toSessionMap)
                .collect(Collectors.toList());

        return R.ok(list);
    }

    /**
     * 新建多人格对战会话
     */
    @PostMapping
    public Map<String, Object> create(
            @RequestHeader("Authorization") String auth,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = authService.currentUserId(auth);

        String topic = MapUtil.str(body, "topic", "");
        List<String> personas = readStringList(body.get("personas"));

        if (topic.isBlank()) {
            throw new RuntimeException("请先输入对战话题");
        }

        if (personas.size() < 2) {
            throw new RuntimeException("请至少选择 2 个人格");
        }

        if (personas.size() > 4) {
            throw new RuntimeException("最多选择 4 个人格");
        }

        PersonaBattleSession session = new PersonaBattleSession();
        session.userId = userId;
        session.topic = topic;
        session.title = topic.length() > 16 ? topic.substring(0, 16) : topic;
        session.personaCodes = String.join(",", personas);
        session.status = "CHAT";
        session.preview = "对话已开始";
        session.createdAt = LocalDateTime.now();
        session.updatedAt = LocalDateTime.now();

        sessionRepo.save(session);

        return R.ok(toDetailMap(session));
    }

    /**
     * 查询某个会话详情，包括消息
     */
    @GetMapping("/{id}")
    public Map<String, Object> detail(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id
    ) {
        Long userId = authService.currentUserId(auth);

        PersonaBattleSession session = getOwnedSession(id, userId);

        return R.ok(toDetailMap(session));
    }

    /**
     * 给会话追加一条消息
     */
    @PostMapping("/{id}/messages")
    public Map<String, Object> addMessage(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = authService.currentUserId(auth);

        PersonaBattleSession session = getOwnedSession(id, userId);

        String role = MapUtil.str(body, "role", "user");
        String personaCode = MapUtil.str(body, "personaCode", "");
        String content = MapUtil.str(body, "content", "");

        if (content.isBlank()) {
            throw new RuntimeException("消息内容不能为空");
        }

        PersonaBattleMessage message = new PersonaBattleMessage();
        message.sessionId = session.id;
        message.userId = userId;
        message.role = role;
        message.personaCode = personaCode;
        message.content = content;
        message.createdAt = LocalDateTime.now();

        messageRepo.save(message);

        session.preview = content.length() > 30 ? content.substring(0, 30) + "..." : content;
        session.updatedAt = LocalDateTime.now();
        sessionRepo.save(session);

        return R.ok(toMessageMap(message));
    }

    /**
     * 结束会话并保存总结
     */
    @PostMapping("/{id}/finish")
    public Map<String, Object> finish(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body
    ) {
        Long userId = authService.currentUserId(auth);

        PersonaBattleSession session = getOwnedSession(id, userId);

        String summary = MapUtil.str(body, "summary", "");

        session.summary = summary;
        session.status = "ENDED";
        session.preview = "已生成总结";
        session.updatedAt = LocalDateTime.now();

        sessionRepo.save(session);

        return R.ok(toDetailMap(session));
    }

    /**
     * 删除会话
     */
    @Transactional
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(
            @RequestHeader("Authorization") String auth,
            @PathVariable Long id
    ) {
        Long userId = authService.currentUserId(auth);

        PersonaBattleSession session = getOwnedSession(id, userId);

        messageRepo.deleteBySessionId(session.id);
        sessionRepo.delete(session);

        return R.msg("删除成功");
    }

    private PersonaBattleSession getOwnedSession(Long id, Long userId) {
        PersonaBattleSession session = sessionRepo.findById(id).orElseThrow();

        if (!session.userId.equals(userId)) {
            throw new RuntimeException("无权访问该会话");
        }

        return session;
    }

    private Map<String, Object> toDetailMap(PersonaBattleSession session) {
        Map<String, Object> map = toSessionMap(session);

        List<Map<String, Object>> messages = messageRepo
                .findBySessionIdOrderByCreatedAtAsc(session.id)
                .stream()
                .map(this::toMessageMap)
                .collect(Collectors.toList());

        map.put("messages", messages);

        return map;
    }

    private Map<String, Object> toSessionMap(PersonaBattleSession session) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", session.id);
        map.put("title", session.title);
        map.put("topic", session.topic);
        map.put("personaCodes", splitPersonaCodes(session.personaCodes));
        map.put("status", session.status);
        map.put("summary", session.summary);
        map.put("preview", session.preview);
        map.put("createdAt", session.createdAt);
        map.put("updatedAt", session.updatedAt);

        return map;
    }

    private Map<String, Object> toMessageMap(PersonaBattleMessage message) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", message.id);
        map.put("sessionId", message.sessionId);
        map.put("role", message.role);
        map.put("personaCode", message.personaCode);
        map.put("content", message.content);
        map.put("createdAt", message.createdAt);

        return map;
    }

    private List<String> splitPersonaCodes(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return Arrays.stream(text.split(","))
                .filter(item -> !item.isBlank())
                .collect(Collectors.toList());
    }

    private List<String> readStringList(Object raw) {
        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .filter(item -> !item.isBlank())
                    .collect(Collectors.toList());
        }

        if (raw instanceof String text) {
            if (text.isBlank()) {
                return List.of();
            }

            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(item -> !item.isBlank())
                    .collect(Collectors.toList());
        }

        return List.of();
    }
}