package com.moodboard.service;

import com.moodboard.entity.UserMemory;
import com.moodboard.repository.UserMemoryRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class MemoryService {

    private final UserMemoryRepository userMemoryRepository;

    public MemoryService(UserMemoryRepository userMemoryRepository) {
        this.userMemoryRepository = userMemoryRepository;
    }

    public UserMemory getOrCreate(Long userId) {
        return userMemoryRepository.findByUserId(userId)
                .orElseGet(() -> userMemoryRepository.save(new UserMemory(userId)));
    }

    public Map<String, Object> getMemory(Long userId) {
        UserMemory memory = getOrCreate(userId);
        return toMap(memory);
    }

    public Map<String, Object> updateMemory(
            Long userId,
            String recentEmotionSummary,
            String recentPersonaCode,
            String recentPersonaName,
            String chatSummary,
            String lastQuestion,
            String lastAnswer
    ) {
        UserMemory memory = getOrCreate(userId);

        if (recentEmotionSummary != null) {
            memory.setRecentEmotionSummary(recentEmotionSummary);
        }

        if (recentPersonaCode != null) {
            memory.setRecentPersonaCode(recentPersonaCode);
        }

        if (recentPersonaName != null) {
            memory.setRecentPersonaName(recentPersonaName);
        }

        if (chatSummary != null) {
            memory.setChatSummary(chatSummary);
        }

        if (lastQuestion != null) {
            memory.setLastQuestion(lastQuestion);
        }

        if (lastAnswer != null) {
            memory.setLastAnswer(lastAnswer);
        }

        userMemoryRepository.save(memory);

        return toMap(memory);
    }

    public Map<String, Object> clearMemory(Long userId) {
        UserMemory memory = getOrCreate(userId);

        memory.setRecentEmotionSummary("");
        memory.setRecentPersonaCode("");
        memory.setRecentPersonaName("");
        memory.setChatSummary("");
        memory.setLastQuestion("");
        memory.setLastAnswer("");

        userMemoryRepository.save(memory);

        return toMap(memory);
    }

    /**
     * 后面拼进 Prompt 的内容。
     */
    public String buildMemoryPrompt(Long userId) {
        UserMemory memory = getOrCreate(userId);

        StringBuilder builder = new StringBuilder();

        if (notBlank(memory.getRecentEmotionSummary())) {
            builder.append("用户最近情绪摘要：")
                    .append(memory.getRecentEmotionSummary())
                    .append("\n");
        }

        if (notBlank(memory.getRecentPersonaCode()) || notBlank(memory.getRecentPersonaName())) {
            builder.append("用户最近常聊人格：")
                    .append(memory.getRecentPersonaName() == null ? "" : memory.getRecentPersonaName())
                    .append(" ")
                    .append(memory.getRecentPersonaCode() == null ? "" : memory.getRecentPersonaCode())
                    .append("\n");
        }

        if (notBlank(memory.getChatSummary())) {
            builder.append("最近一次对话摘要：")
                    .append(memory.getChatSummary())
                    .append("\n");
        }

        if (notBlank(memory.getLastQuestion())) {
            builder.append("用户上一次提到：")
                    .append(memory.getLastQuestion())
                    .append("\n");
        }

        if (builder.isEmpty()) {
            return "暂无用户长期记忆。";
        }

        return builder.toString();
    }

    /**
     * 简易情绪关键词提取。
     * 后面可以升级成 AI 摘要。
     */
    public String inferEmotionSummary(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();

        if (text.contains("焦虑") || text.contains("紧张") || text.contains("担心")) {
            builder.append("用户近期可能存在焦虑或紧张情绪；");
        }

        if (text.contains("拖延") || text.contains("不想做") || text.contains("没动力")) {
            builder.append("用户近期可能存在拖延或行动启动困难；");
        }

        if (text.contains("睡不着") || text.contains("失眠") || text.contains("熬夜")) {
            builder.append("用户近期可能存在睡眠或作息问题；");
        }

        if (text.contains("难过") || text.contains("低落") || text.contains("崩溃")) {
            builder.append("用户近期可能存在低落情绪；");
        }

        if (text.contains("考试") || text.contains("复习") || text.contains("学习")) {
            builder.append("用户近期压力可能与学习或考试有关；");
        }

        return builder.toString();
    }

    private Map<String, Object> toMap(UserMemory memory) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("id", memory.getId());
        map.put("userId", memory.getUserId());
        map.put("recentEmotionSummary", memory.getRecentEmotionSummary());
        map.put("recentPersonaCode", memory.getRecentPersonaCode());
        map.put("recentPersonaName", memory.getRecentPersonaName());
        map.put("chatSummary", memory.getChatSummary());
        map.put("lastQuestion", memory.getLastQuestion());
        map.put("lastAnswer", memory.getLastAnswer());
        map.put("createdAt", memory.getCreatedAt());
        map.put("updatedAt", memory.getUpdatedAt());

        return map;
    }

    private boolean notBlank(String text) {
        return text != null && !text.isBlank();
    }
}