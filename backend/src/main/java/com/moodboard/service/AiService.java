package com.moodboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${ai.mode:mock}")
    private String mode;

    @Value("${ai.chat-url:https://api.deepseek.com/chat/completions}")
    private String chatUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:deepseek-v4-flash}")
    private String model;

    @Value("${ai.thinking-type:disabled}")
    private String thinkingType;

    @Value("${ai.reasoning-effort:high}")
    private String reasoningEffort;

    @Value("${ai.temperature:0.7}")
    private Double temperature;

    @Value("${ai.timeout-seconds:90}")
    private Integer timeoutSeconds;

    private final ObjectMapper mapper = new ObjectMapper();

    public String generate(String systemPrompt, List<Map<String, String>> messages) {
        if (!"real".equalsIgnoreCase(mode)
                || chatUrl == null || chatUrl.isBlank()
                || apiKey == null || apiKey.isBlank()) {
            return mock(systemPrompt, messages);
        }

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Math.min(timeoutSeconds, 30)))
                    .build();

            List<Map<String, String>> allMessages = buildMessages(systemPrompt, messages);

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", allMessages);
            body.put("temperature", temperature);

            /*
             * DeepSeek V4 的 thinking 参数是一个对象：
             * "thinking": {
             *   "type": "disabled",
             *   "reasoning_effort": "high"
             * }
             *
             * 注意：不要把 reasoning_effort 单独放到顶层。
             */
            if (isDeepSeekV4Model()
                    && thinkingType != null
                    && !thinkingType.isBlank()
                    && !"omit".equalsIgnoreCase(thinkingType)) {

                Map<String, Object> thinking = new LinkedHashMap<>();
                thinking.put("type", thinkingType);

                if (reasoningEffort != null && !reasoningEffort.isBlank()) {
                    thinking.put("reasoning_effort", reasoningEffort);
                }

                body.put("thinking", thinking);
            }

            String requestJson = mapper.writeValueAsString(body);

            printRequestLog(requestJson);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(chatUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            int statusCode = response.statusCode();
            String responseBody = response.body();

            System.out.println("========== DeepSeek 响应状态码 ==========");
            System.out.println(statusCode);
            System.out.println("========== DeepSeek 响应体 ==========");
            System.out.println(responseBody);

            if (statusCode < 200 || statusCode >= 300) {
                return "DeepSeek 接口调用失败，状态码：" + statusCode
                        + "。错误信息：" + simplifyError(responseBody);
            }

            return parseReply(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return "DeepSeek 接口调用异常：" + e.getMessage();
        }
    }

    private List<Map<String, String>> buildMessages(
            String systemPrompt,
            List<Map<String, String>> messages
    ) {
        List<Map<String, String>> allMessages = new ArrayList<>();

        String safeSystemPrompt = systemPrompt == null || systemPrompt.isBlank()
                ? "你是一个温柔、真诚、不会说教的情绪树洞助手。请用自然中文回复用户。"
                : systemPrompt;

        Map<String, String> systemMessage = new LinkedHashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", safeSystemPrompt);
        allMessages.add(systemMessage);

        if (messages != null) {
            for (Map<String, String> item : messages) {
                if (item == null) {
                    continue;
                }

                String role = item.getOrDefault("role", "").trim();
                String content = item.getOrDefault("content", "").trim();

                if (role.isBlank() || content.isBlank()) {
                    continue;
                }

                if (!isValidRole(role)) {
                    role = "user";
                }

                Map<String, String> msg = new LinkedHashMap<>();
                msg.put("role", role);
                msg.put("content", content);
                allMessages.add(msg);
            }
        }

        if (allMessages.size() == 1) {
            Map<String, String> userMessage = new LinkedHashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", "你好，我想和你聊一聊。");
            allMessages.add(userMessage);
        }

        return allMessages;
    }

    private boolean isValidRole(String role) {
        return "system".equals(role)
                || "user".equals(role)
                || "assistant".equals(role)
                || "tool".equals(role);
    }

    private boolean isDeepSeekV4Model() {
        return model != null && model.toLowerCase().startsWith("deepseek-v4");
    }

    private void printRequestLog(String requestJson) {
        try {
            System.out.println("========== DeepSeek 请求地址 ==========");
            System.out.println(chatUrl);

            System.out.println("========== DeepSeek API Key 是否存在 ==========");
            System.out.println(apiKey != null && !apiKey.isBlank());

            System.out.println("========== DeepSeek API Key 掩码 ==========");
            System.out.println(maskKey(apiKey));

            System.out.println("========== DeepSeek 模型 ==========");
            System.out.println(model);

            System.out.println("========== DeepSeek thinking-type ==========");
            System.out.println(thinkingType);

            System.out.println("========== DeepSeek 请求体 JSON ==========");
            Object json = mapper.readValue(requestJson, Object.class);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
        } catch (Exception e) {
            System.out.println("打印 DeepSeek 请求日志失败：" + e.getMessage());
        }
    }

    private String maskKey(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }

        if (key.length() <= 8) {
            return "****";
        }

        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    private String parseReply(String responseBody) {
        try {
            JsonNode root = mapper.readTree(responseBody);

            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).path("message");

                String content = message.path("content").asText("");
                if (content != null && !content.isBlank()) {
                    return content.trim();
                }

                String reasoningContent = message.path("reasoning_content").asText("");
                if (reasoningContent != null && !reasoningContent.isBlank()) {
                    return reasoningContent.trim();
                }
            }

            return "AI 已返回结果，但未解析到有效回复内容。";

        } catch (Exception e) {
            e.printStackTrace();
            return "AI 回复解析失败：" + e.getMessage();
        }
    }

    private String simplifyError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "无错误响应体";
        }

        try {
            JsonNode root = mapper.readTree(responseBody);

            String message = root.path("error").path("message").asText("");
            if (!message.isBlank()) {
                return message;
            }

            String msg = root.path("message").asText("");
            if (!msg.isBlank()) {
                return msg;
            }

            String info = root.path("info").asText("");
            if (!info.isBlank()) {
                return info;
            }

            return responseBody.length() > 300
                    ? responseBody.substring(0, 300)
                    : responseBody;

        } catch (Exception e) {
            return responseBody.length() > 300
                    ? responseBody.substring(0, 300)
                    : responseBody;
        }
    }

    private String mock(String systemPrompt, List<Map<String, String>> messages) {
        String lastUserText = getLastUserText(messages);
        String prompt = systemPrompt == null ? "" : systemPrompt;

        if (prompt.contains("INTJ")) {
            return "我先帮你把这件事拆开看。你现在的不舒服，可能不是单一情绪，而是压力、目标感和自我要求叠在一起。先不用急着解决全部问题，今天可以先选一个最小动作完成。";
        }

        if (prompt.contains("INFP")) {
            return "我能感觉到你其实不是不努力，而是心里有些东西还没有被好好接住。你可以先不用逼自己马上变好，我们先把这份迷茫说清楚，好吗？";
        }

        if (prompt.contains("ENFP")) {
            return "我懂你这种感觉！但这不代表你不行，只是你现在的能量有点被消耗掉了。我们可以先找一个很小很轻的切入口，让你重新有一点点掌控感。";
        }

        if (prompt.contains("ESFJ")) {
            return "你已经撑了一段时间了吧。先别一个人硬扛，可以把今天最困扰你的事情说出来，我会陪你一起慢慢理清楚。";

        }

        if (lastUserText.contains("累") || lastUserText.contains("难受") || lastUserText.contains("迷茫")) {
            return "听起来你今天真的有点辛苦。先不用急着证明自己，也不用马上把状态调整好。你愿意说说，是哪一件事让你最累吗？";
        }

        return "我在听。你可以慢慢说，不用组织得很完整，我会陪你一起把情绪理清楚。";
    }

    private String getLastUserText(List<Map<String, String>> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }

        for (int i = messages.size() - 1; i >= 0; i--) {
            Map<String, String> item = messages.get(i);
            if (item == null) {
                continue;
            }

            String role = item.getOrDefault("role", "");
            String content = item.getOrDefault("content", "");

            if ("user".equalsIgnoreCase(role)) {
                return content == null ? "" : content;
            }
        }

        return "";
    }
}