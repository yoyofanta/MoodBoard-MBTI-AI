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
public class RagAnswerService {

    private final KnowledgeBaseService knowledgeBaseService;
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

    public RagAnswerService(
            KnowledgeBaseService knowledgeBaseService,
            MemoryService memoryService
    ) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.memoryService = memoryService;
    }

    public Map<String, Object> ask(String query, int topK) {
        return ask(query, topK, null);
    }

    public Map<String, Object> ask(String query, int topK, Long userId) {
        List<Map<String, Object>> contexts = knowledgeBaseService.search(query, topK);

        String memoryPrompt = "暂无用户长期记忆。";

        if (userId != null) {
            memoryPrompt = memoryService.buildMemoryPrompt(userId);
        }

        String answer;

        if (!"real".equalsIgnoreCase(aiMode) || apiKey == null || apiKey.isBlank()) {
            answer = mockAnswer(query, contexts, memoryPrompt);
        } else {
            try {
                answer = callDeepSeek(query, contexts, memoryPrompt);
            } catch (Exception e) {
                e.printStackTrace();
                answer = mockAnswer(query, contexts, memoryPrompt);
            }
        }

        if (userId != null) {
            updateMemoryAfterAnswer(userId, query, answer);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("query", query);
        data.put("topK", topK);
        data.put("mode", aiMode);
        data.put("memory", memoryPrompt);
        data.put("contexts", contexts);
        data.put("answer", answer);

        return data;
    }

    private String callDeepSeek(
            String query,
            List<Map<String, Object>> contexts,
            String memoryPrompt
    ) throws Exception {
        String contextText = buildContextText(contexts);

        String systemPrompt = """
                你是 MoodBoard × MBTI 项目中的情绪知识库助手。
                你的任务是结合用户长期记忆和知识库片段，回答用户的情绪困扰。
                
                回复要求：
                1. 先温柔接住用户的情绪；
                2. 再结合知识库内容给出解释；
                3. 如果用户长期记忆中有相关信息，要自然地延续上下文；
                4. 最后给出 2-3 条具体可执行的小建议；
                5. 不要声称自己是医生或心理咨询师；
                6. 如果用户状态严重、持续很久，或出现伤害自己/他人的风险，要建议及时寻求可信任的人或专业帮助；
                7. 回答自然、简洁、像一个懂情绪的朋友。
                """;

        String userPrompt =
                "【用户长期记忆 Memory】\n" +
                memoryPrompt + "\n\n" +
                "【用户当前问题】\n" +
                query + "\n\n" +
                "【检索到的情绪知识库片段】\n" +
                contextText + "\n\n" +
                "请基于 Memory、知识库片段和用户当前问题进行回答。";

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
            return "我检索到了相关内容，但这次 AI 没有返回有效回答。你可以换一种说法再试一次。";
        }

        Map first = (Map) choices.get(0);
        Map message = (Map) first.get("message");

        if (message == null) {
            return "我检索到了相关内容，但这次 AI 回复为空。";
        }

        Object content = message.get("content");

        return content == null ? "我检索到了相关内容，但这次 AI 回复为空。" : String.valueOf(content);
    }

    private String buildContextText(List<Map<String, Object>> contexts) {
        if (contexts == null || contexts.isEmpty()) {
            return "暂无匹配知识片段。";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < contexts.size(); i++) {
            Map<String, Object> item = contexts.get(i);

            builder.append("片段").append(i + 1).append("：\n");
            builder.append("标题：").append(item.get("title")).append("\n");
            builder.append("标签：").append(item.get("tags")).append("\n");
            builder.append("内容：").append(item.get("content")).append("\n\n");
        }

        return builder.toString();
    }

    private String mockAnswer(
            String query,
            List<Map<String, Object>> contexts,
            String memoryPrompt
    ) {
        if (contexts == null || contexts.isEmpty()) {
            return "我看到了你现在的问题，也读取到了你的记忆信息：\n\n" +
                    memoryPrompt + "\n\n" +
                    "不过这次没有检索到特别匹配的知识片段。你可以先把最困扰你的部分写成一句话，我们再一起慢慢拆开。";
        }

        String title = String.valueOf(contexts.get(0).get("title"));
        String content = String.valueOf(contexts.get(0).get("content"));

        return "我先接住你现在的感受。你提到的是：「" + query + "」。\n\n" +
                "我读取到的用户记忆是：\n" +
                memoryPrompt + "\n\n" +
                "知识库中最相关的内容是「" + title + "」。\n\n" +
                "从知识库来看，" + content + "\n\n" +
                "你现在可以先做一个很小的动作：把最困扰你的部分写成一句话，然后只处理其中最容易开始的一步。";
    }

    private void updateMemoryAfterAnswer(Long userId, String query, String answer) {
        try {
            String inferredEmotion = memoryService.inferEmotionSummary(query + " " + answer);

            String chatSummary =
                    "用户本次提到：" + truncate(query, 120) +
                            "；AI 围绕该问题给出了情绪接纳和具体建议。";

            memoryService.updateMemory(
                    userId,
                    inferredEmotion == null || inferredEmotion.isBlank() ? null : inferredEmotion,
                    null,
                    null,
                    chatSummary,
                    truncate(query, 300),
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
}