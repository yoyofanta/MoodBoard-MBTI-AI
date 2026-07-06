package com.moodboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class MemoryChatService {

    private final MemoryService memoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.mode:mock}")
    private String aiMode;

    @Value("${ai.chat-url:https://api.deepseek.com/chat/completions}")
    private String chatUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.temperature:0.7}")
    private Double temperature;

    @Value("${ai.timeout-seconds:90}")
    private Integer timeoutSeconds;

    public MemoryChatService(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    public Map<String, Object> chat(
            Long userId,
            String content,
            String chatType,
            String personaCode,
            String personaName
    ) {
        String memoryPrompt = memoryService.buildMemoryPrompt(userId);

        String answer;

        if (!"real".equalsIgnoreCase(aiMode) || apiKey == null || apiKey.isBlank()) {
            answer = mockAnswer(content, chatType, personaCode, personaName, memoryPrompt);
        } else {
            try {
                answer = callDeepSeek(content, chatType, personaCode, personaName, memoryPrompt);
            } catch (Exception e) {
                e.printStackTrace();
                answer = mockAnswer(content, chatType, personaCode, personaName, memoryPrompt);
            }
        }

        updateMemory(userId, content, answer, personaCode, personaName);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reply", answer);
        data.put("answer", answer);
        data.put("content", answer);
        data.put("mode", aiMode);
        data.put("memory", memoryPrompt);
        data.put("chatType", chatType);
        data.put("personaCode", personaCode);
        data.put("personaName", personaName);

        return data;
    }

    private String callDeepSeek(
            String content,
            String chatType,
            String personaCode,
            String personaName,
            String memoryPrompt
    ) throws Exception {

        String systemPrompt = buildSystemPrompt(chatType, personaCode, personaName);

        String userPrompt =
                "【用户长期记忆 Memory】\n" +
                memoryPrompt + "\n\n" +
                "【用户当前输入】\n" +
                content + "\n\n" +
                "请结合用户长期记忆和当前输入进行回复。";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("temperature", temperature);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", systemPrompt
        ));
        messages.add(Map.of(
                "role", "user",
                "content", userPrompt
        ));

        body.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(chatUrl))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("DeepSeek 调用失败：" + response.statusCode() + "，" + response.body());
        }

        Map result = objectMapper.readValue(response.body(), Map.class);
        List choices = (List) result.get("choices");

        if (choices == null || choices.isEmpty()) {
            return "我看到了你的消息，但这次 AI 没有返回有效内容。你可以再说得具体一点。";
        }

        Map first = (Map) choices.get(0);
        Map message = (Map) first.get("message");

        if (message == null) {
            return "我看到了你的消息，但这次 AI 回复为空。";
        }

        Object reply = message.get("content");

        return reply == null ? "我看到了你的消息，但这次 AI 回复为空。" : String.valueOf(reply);
    }

    private String buildSystemPrompt(String chatType, String personaCode, String personaName) {
        boolean isPersona = personaCode != null
                && !personaCode.isBlank()
                && !"DAILY".equalsIgnoreCase(personaCode);

        if (!isPersona) {
            return """
                    你是 MoodBoard × MBTI 项目中的 AI 情绪树洞助手。
                    
                    回复要求：
                    1. 先温柔接住用户情绪；
                    2. 不要批评、说教或否定用户；
                    3. 结合用户长期记忆自然延续上下文；
                    4. 回复像朋友聊天，不要像报告；
                    5. 可以给出 1-3 条具体可执行的小建议；
                    6. 如果用户表达持续低落、崩溃、伤害自己或他人的风险，要建议及时联系可信任的人或专业帮助。
                    """;
        }

        return """
                你是 MoodBoard × MBTI 项目中的人格对话助手。
                
                当前人格：
                %s %s
                
                回复要求：
                1. 严格以当前人格视角回答；
                2. 体现该人格的语气、关注点和思考方式；
                3. 结合用户长期记忆自然延续上下文；
                4. 不要替其他人格发言；
                5. 回复像真实聊天，不要像心理报告；
                6. 控制在 2-5 句话。
                """.formatted(
                personaCode == null ? "" : personaCode,
                personaName == null ? "" : personaName
        );
    }

    private String mockAnswer(
            String content,
            String chatType,
            String personaCode,
            String personaName,
            String memoryPrompt
    ) {
        boolean isPersona = personaCode != null
                && !personaCode.isBlank()
                && !"DAILY".equalsIgnoreCase(personaCode);

        if (!isPersona) {
            return "我看到你说：「" + content + "」。\n\n" +
                    "我也读取到了你的记忆：\n" +
                    memoryPrompt + "\n\n" +
                    "先别急着否定自己。你可以先把现在最困扰你的事情拆成一个很小的动作，从最容易开始的那一步做起。";
        }

        return "【" + personaCode + " " + safe(personaName) + "】我看到你说：「" + content + "」。\n\n" +
                "结合我读取到的记忆：\n" +
                memoryPrompt + "\n\n" +
                "我会建议你先不要急着追求一次性解决，而是先把问题拆开，找到今天最能完成的一小步。";
    }

    private void updateMemory(
            Long userId,
            String question,
            String answer,
            String personaCode,
            String personaName
    ) {
        try {
            String emotionSummary = memoryService.inferEmotionSummary(question + " " + answer);

            String chatSummary =
                    "用户本次提到：" + truncate(question, 120) +
                            "；AI 已结合长期记忆进行回应。";

            memoryService.updateMemory(
                    userId,
                    emotionSummary == null || emotionSummary.isBlank() ? null : emotionSummary,
                    personaCode,
                    personaName,
                    chatSummary,
                    truncate(question, 300),
                    truncate(answer, 500)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private String safe(String text) {
        return text == null ? "" : text;
    }
}