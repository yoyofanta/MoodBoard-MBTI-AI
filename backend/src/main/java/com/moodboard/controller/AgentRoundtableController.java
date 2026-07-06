package com.moodboard.controller;

import com.moodboard.common.R;
import com.moodboard.service.AgentRoundtableService;
import com.moodboard.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/agent")
public class AgentRoundtableController {

    private final AgentRoundtableService agentRoundtableService;
    private final AuthService authService;

    public AgentRoundtableController(
            AgentRoundtableService agentRoundtableService,
            AuthService authService
    ) {
        this.agentRoundtableService = agentRoundtableService;
        this.authService = authService;
    }

    /**
     * 多角色 Agent 圆桌会话
     *
     * POST /api/ai/agent/roundtable
     */
    @PostMapping("/roundtable")
    public Map<String, Object> roundtable(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Object> body
    ) {
        try {
            Long userId = resolveUserId(auth);

            String topic = str(body.get("topic"));

            Object agentsObj = body.get("agents");

            if (!(agentsObj instanceof List<?> rawList)) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("message", "agents 必须是数组");
                return R.ok(data);
            }

            List<Map<String, Object>> agents = rawList.stream()
                    .filter(item -> item instanceof Map)
                    .map(item -> (Map<String, Object>) item)
                    .toList();

            Map<String, Object> data = agentRoundtableService.runRoundtable(
                    userId,
                    topic,
                    agents
            );

            return R.ok(data);
        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("message", "Agent 圆桌会话失败：" + e.getMessage());

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
}