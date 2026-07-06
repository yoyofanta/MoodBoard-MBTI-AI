package com.moodboard.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AgentRoundtableService {

    private final MemoryChatService memoryChatService;
    private final MemoryService memoryService;

    public AgentRoundtableService(
            MemoryChatService memoryChatService,
            MemoryService memoryService
    ) {
        this.memoryChatService = memoryChatService;
        this.memoryService = memoryService;
    }

    public Map<String, Object> runRoundtable(
            Long userId,
            String topic,
            List<Map<String, Object>> agents
    ) {
        if (topic == null || topic.isBlank()) {
            topic = "用户想和多个 Agent 一起讨论当前困扰。";
        }

        if (agents == null || agents.size() < 2) {
            throw new RuntimeException("至少需要选择 2 个 Agent");
        }

        if (agents.size() > 4) {
            throw new RuntimeException("最多只能选择 4 个 Agent");
        }

        String memoryPrompt = memoryService.buildMemoryPrompt(userId);

        List<Map<String, Object>> agentReplies = new ArrayList<>();

        StringBuilder conversationContext = new StringBuilder();
        conversationContext.append("用户问题：").append(topic).append("\n\n");
        conversationContext.append("用户长期记忆：\n").append(memoryPrompt).append("\n\n");

        for (int i = 0; i < agents.size(); i++) {
            Map<String, Object> agent = agents.get(i);

            String code = str(agent.get("code"));
            String name = str(agent.get("name"));

            if (code.isBlank()) {
                code = "AGENT_" + (i + 1);
            }

            if (name.isBlank()) {
                name = code + " Agent";
            }

            String agentInput =
                    "现在正在进行一场多角色 Agent 圆桌会话。\n\n" +
                    "用户问题：\n" + topic + "\n\n" +
                    "前面其他 Agent 的观点：\n" +
                    (conversationContext.isEmpty() ? "暂无。" : conversationContext.toString()) + "\n\n" +
                    "请你作为 " + code + " " + name + "，从自己的视角给出观点。\n" +
                    "要求：\n" +
                    "1. 不要重复前面 Agent 的内容；\n" +
                    "2. 必须体现你的人格视角；\n" +
                    "3. 回复 2-4 句话；\n" +
                    "4. 像真实讨论，不要像报告。";

            Map<String, Object> result = memoryChatService.chat(
                    userId,
                    agentInput,
                    "AGENT_ROUNDTABLE",
                    code,
                    name
            );

            String reply = extractReply(result);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("order", i + 1);
            item.put("agentCode", code);
            item.put("agentName", name);
            item.put("reply", reply);

            agentReplies.add(item);

            conversationContext.append(code)
                    .append(" ")
                    .append(name)
                    .append("：")
                    .append(reply)
                    .append("\n\n");
        }

        String summary = buildSummary(topic, agentReplies);

        memoryService.updateMemory(
                userId,
                memoryService.inferEmotionSummary(topic + " " + summary),
                "AGENT_ROUNDTABLE",
                "多角色 Agent 圆桌",
                "用户发起了一次多角色 Agent 圆桌会话，主题为：" + truncate(topic, 120),
                truncate(topic, 300),
                truncate(summary, 500)
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("topic", topic);
        data.put("memory", memoryPrompt);
        data.put("agentCount", agents.size());
        data.put("agents", agentReplies);
        data.put("summary", summary);

        return data;
    }

    private String buildSummary(String topic, List<Map<String, Object>> replies) {
        StringBuilder builder = new StringBuilder();

        builder.append("围绕「")
                .append(topic)
                .append("」，不同 Agent 给出了几个角度：\n\n");

        for (Map<String, Object> reply : replies) {
            builder.append("・")
                    .append(reply.get("agentCode"))
                    .append(" ")
                    .append(reply.get("agentName"))
                    .append("：")
                    .append(shortText(str(reply.get("reply")), 80))
                    .append("\n");
        }

        builder.append("\n综合来看，你可以先不用急着一次性解决所有问题。")
                .append("更适合的做法是：先确认当前最困扰你的一个点，")
                .append("把它拆成今天能完成的一小步，再根据结果慢慢调整。");

        return builder.toString();
    }

    private String extractReply(Map<String, Object> result) {
        if (result == null) {
            return "这个 Agent 暂时没有生成有效回复。";
        }

        Object reply = result.get("reply");

        if (reply == null) {
            reply = result.get("answer");
        }

        if (reply == null) {
            reply = result.get("content");
        }

        return reply == null ? "这个 Agent 暂时没有生成有效回复。" : String.valueOf(reply);
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }

    private String shortText(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "暂无观点";
        }

        text = text.replaceAll("\\s+", " ");

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "...";
    }
}
