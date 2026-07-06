package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.BlindBoxMessage;
import com.moodboard.entity.BlindBoxSession;
import com.moodboard.repository.BlindBoxMessageRepository;
import com.moodboard.repository.BlindBoxSessionRepository;
import com.moodboard.service.AiService;
import com.moodboard.service.AuthService;
import com.moodboard.service.PersonaLibrary;
import com.moodboard.service.PromptService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/blind-box")
public class BlindBoxController {
    private final AuthService authService;
    private final PersonaLibrary personaLibrary;
    private final PromptService promptService;
    private final AiService aiService;
    private final BlindBoxSessionRepository sessionRepo;
    private final BlindBoxMessageRepository messageRepo;
    private final Random random = new Random();
    private final List<String> topics = List.of("你最近一次大哭是什么时候？", "最近有没有一件事让你觉得‘我真的好累’？", "你有没有一个一直不敢说出口的委屈？", "你现在最需要被谁理解？", "你觉得自己最近是在生活，还是在撑着？", "你最近最想逃离的是什么？", "如果情绪有颜色，你今天是什么颜色？", "今天你想被怎样安慰？", "你有没有假装不在意，其实很在意的事？", "最近哪个关系让你感觉消耗？");

    public BlindBoxController(AuthService authService, PersonaLibrary personaLibrary, PromptService promptService, AiService aiService, BlindBoxSessionRepository sessionRepo, BlindBoxMessageRepository messageRepo) {
        this.authService = authService; this.personaLibrary = personaLibrary; this.promptService = promptService; this.aiService = aiService; this.sessionRepo = sessionRepo; this.messageRepo = messageRepo;
    }

    @PostMapping("/draw")
    public Map<String, Object> draw(@RequestHeader("Authorization") String auth) {
        authService.currentUserId(auth);
        List<PersonaLibrary.PersonaDef> all = personaLibrary.all();
        PersonaLibrary.PersonaDef p = all.get(random.nextInt(all.size()));
        String topic = topics.get(random.nextInt(topics.size()));
        return R.ok(Map.of("persona", p.code(), "personaName", p.name(), "avatar", p.avatar(), "theme", p.theme(), "topic", topic, "maxRounds", 3));
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        String persona = MapUtil.str(body, "persona", "INFJ");
        String topic = MapUtil.str(body, "topic", "今天你想被怎样安慰？");
        boolean ephemeral = MapUtil.bool(body, "ephemeral", false);
        BlindBoxSession s = new BlindBoxSession();
        s.userId = userId; s.persona = persona; s.topic = topic; s.maxRounds = 3; s.currentRound = 1; s.ephemeral = ephemeral;
        if (!ephemeral) s = sessionRepo.save(s);
        String prompt = promptService.buildBlindBoxPrompt(userId, persona, topic, 1);
        String reply = aiService.generate(prompt, List.of(Map.of("role", "user", "content", "请开启心灵盲盒第一轮")));
        if (!ephemeral) saveMsg(s.id, userId, "assistant", persona, 1, reply);
        return R.ok(Map.of("blindBoxId", s.id == null ? 0 : s.id, "reply", reply, "roundIndex", 1, "finished", false));
    }

    @PostMapping("/reply")
    public Map<String, Object> reply(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        Long id = MapUtil.longValue(body, "blindBoxId");
        String userReply = MapUtil.str(body, "userReply", "");
        String persona = MapUtil.str(body, "persona", "INFJ");
        String topic = MapUtil.str(body, "topic", "今天你想被怎样安慰？");
        boolean ephemeral = MapUtil.bool(body, "ephemeral", false);
        int nextRound = Math.min(3, MapUtil.intValue(body, "roundIndex", 1) + 1);
        if (id != null && id > 0) {
            BlindBoxSession s = sessionRepo.findById(id).orElse(null);
            if (s != null) { nextRound = Math.min(3, s.currentRound + 1); s.currentRound = nextRound; sessionRepo.save(s); }
        }
        if (!ephemeral && id != null && id > 0) saveMsg(id, userId, "user", persona, nextRound - 1, userReply);
        String prompt = promptService.buildBlindBoxPrompt(userId, persona, topic, nextRound);
        String reply = aiService.generate(prompt, List.of(Map.of("role", "user", "content", userReply)));
        if (!ephemeral && id != null && id > 0) saveMsg(id, userId, "assistant", persona, nextRound, reply);
        return R.ok(Map.of("blindBoxId", id == null ? 0 : id, "reply", reply, "roundIndex", nextRound, "finished", nextRound >= 3));
    }

    private void saveMsg(Long boxId, Long userId, String role, String persona, int round, String content) {
        BlindBoxMessage m = new BlindBoxMessage();
        m.blindBoxId = boxId; m.userId = userId; m.role = role; m.persona = persona; m.roundIndex = round; m.content = content; messageRepo.save(m);
    }
}
