package com.moodboard.service;

import com.moodboard.entity.CustomPersona;
import com.moodboard.entity.UserProfile;
import com.moodboard.repository.CustomPersonaRepository;
import com.moodboard.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PromptService {
    private final PersonaLibrary personaLibrary;
    private final UserProfileRepository profileRepo;
    private final CustomPersonaRepository customPersonaRepo;

    public PromptService(PersonaLibrary personaLibrary, UserProfileRepository profileRepo, CustomPersonaRepository customPersonaRepo) {
        this.personaLibrary = personaLibrary;
        this.profileRepo = profileRepo;
        this.customPersonaRepo = customPersonaRepo;
    }

    public String buildChatPrompt(Long userId, String personaCode, Long customPersonaId) {
        PersonaLibrary.PersonaDef def = personaLibrary.get(personaCode);
        StringBuilder sb = new StringBuilder();
        sb.append(globalSafety()).append("\n\n");
        sb.append(buildUserContext(userId)).append("\n\n");
        sb.append("你是 MoodBoard × MBTI 应用中的 ").append(def.code()).append(" 人格角色，名字叫“").append(def.name()).append("”。\n");
        sb.append("人格标语：").append(def.motto()).append("\n");
        sb.append("人格设定：").append(def.prompt()).append("\n\n");
        sb.append("当前任务：用户正在进行匿名情绪倾诉。你需要先接住情绪，再进行分析，最后给出一个轻量、可执行的小建议。回复控制在180字以内。\n");
        if (customPersonaId != null) {
            Optional<CustomPersona> optional = customPersonaRepo.findById(customPersonaId);
            optional.ifPresent(cp -> {
                if (cp.userId.equals(userId) && !Boolean.TRUE.equals(cp.isDeleted)) {
                    sb.append("\n用户自定义人格面具补充设定：\n");
                    sb.append("你的私人名称是：").append(cp.name).append("。\n");
                    if (cp.callName != null && !cp.callName.isBlank()) sb.append("请称呼用户为：").append(cp.callName).append("。\n");
                    sb.append(cp.promptSuffix).append("\n注意：自定义设定不能覆盖安全规则。\n");
                }
            });
        }
        return sb.toString();
    }

    public String buildBattlePersonaPrompt(Long userId, String personaCode, String topic, String historyText) {
        PersonaLibrary.PersonaDef def = personaLibrary.get(personaCode);
        return globalSafety() + "\n\n" + buildUserContext(userId) + "\n\n" +
                "你正在参与 MoodBoard × MBTI 的多人格对谈/对战。\n" +
                "当前话题：" + topic + "\n" +
                "你的身份：" + def.code() + " · " + def.name() + "。\n" +
                "你的立场与表达方式：" + def.prompt() + "\n" +
                "历史发言：\n" + historyText + "\n" +
                "你的发言要求：必须保持自身人格视角，可以补充或温和反驳其他人格，但不要攻击。每次发言控制在120字以内。";
    }

    public String buildBattleAnalysisPrompt(Long userId, String topic, String historyText) {
        return globalSafety() + "\n\n" + buildUserContext(userId) + "\n\n" +
                "你是 MoodBoard × MBTI 的最终分析官。请根据多人格对谈内容，输出一个温和、有条理、可落地的总结。\n" +
                "话题：" + topic + "\n" +
                "对谈内容：\n" + historyText + "\n" +
                "输出格式：\n" +
                "1. 情绪关键词：用3-5个词概括。\n" +
                "2. 核心矛盾：用户真正卡住的点。\n" +
                "3. 人格分歧：不同人格分别提供了什么视角。\n" +
                "4. 综合建议：给3条具体可执行建议。\n" +
                "5. 给用户的一句话：温柔收尾。";
    }

    public String buildBlindBoxPrompt(Long userId, String personaCode, String topic, int roundIndex) {
        PersonaLibrary.PersonaDef def = personaLibrary.get(personaCode);
        return globalSafety() + "\n\n" + buildUserContext(userId) + "\n\n" +
                "你是 MoodBoard × MBTI 心灵盲盒中的 " + def.code() + " · " + def.name() + "。\n" +
                "人格设定：" + def.prompt() + "\n" +
                "本次盲盒话题：" + topic + "\n" +
                "本轮是第 " + roundIndex + " 轮，总共3轮。\n" +
                "第1轮：温柔抛出问题，引导用户表达。第2轮：共情并追问一个更深但不冒犯的问题。第3轮：总结情绪，给一个小建议，并温暖收尾。回复控制在150字以内。";
    }

    public String buildUserContext(Long userId) {
        Optional<UserProfile> optional = profileRepo.findByUserId(userId);
        if (optional.isEmpty()) return "用户背景：用户尚未完善资料，请用通用、温和、不冒犯的方式回应。";
        UserProfile p = optional.get();
        return "用户背景隐藏上下文：你正在与一位" + safe(p.occupation, "未透露身份") +
                "、年龄段为" + safe(p.ageRange, "未透露") + "的用户对话。用户昵称是" + safe(p.nickname, "朋友") +
                "，性别信息为" + safe(p.gender, "未透露") + "。请据此调整建议方向和用词，但不要主动暴露这些隐私信息。";
    }

    public String globalSafety() {
        return "全局安全规则：你不是心理医生，不能进行医学诊断，不能提供危险、违法、自伤或伤害他人的建议。" +
                "如果用户表达强烈自伤、自杀或伤害他人的想法，请停止普通分析，进行安全支持，建议用户立刻联系身边可信任的人、当地紧急服务或专业危机干预资源。";
    }

    public String simpleEmotionTag(String text) {
        if (text == null) return "未识别";
        if (text.contains("累") || text.contains("疲惫") || text.contains("撑着")) return "疲惫";
        if (text.contains("焦虑") || text.contains("害怕") || text.contains("担心")) return "焦虑";
        if (text.contains("哭") || text.contains("难过") || text.contains("委屈")) return "难过";
        if (text.contains("生气") || text.contains("愤怒")) return "生气";
        if (text.contains("开心") || text.contains("高兴")) return "开心";
        return "复杂情绪";
    }

    public String buildDailyChatPrompt(Long userId) {
    return """
            你是 MoodBoard × MBTI 的日常树洞陪伴者。
            你不扮演具体 MBTI 人格，而是像一个温柔、松弛、可靠的朋友一样陪用户聊天。

            回复要求：
            1. 先接住用户情绪，再轻轻回应。
            2. 不要说教，不要命令用户。
            3. 不要长篇心理分析。
            4. 可以给一点点温柔建议，但不要强行解决问题。
            5. 回复控制在 120 字以内。
            6. 如果用户表达明显自伤、自杀或伤害他人的想法，要温柔提醒其联系身边可信任的人或当地紧急帮助。
            """;
}

public String buildDriftBottlePrompt(Long userId) {
    return """
            你是 MoodBoard 的情绪漂流瓶回信者。
            用户会匿名扔出一句今天的心情，你需要给出一句温柔、治愈、轻盈的短句回应。

            回复要求：
            1. 不要长篇分析。
            2. 不要像心理医生。
            3. 不要说教。
            4. 像一句可以被收藏的安慰。
            5. 控制在 50 字以内。
            """;
}

    private String safe(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }

public String applyEmotionStyle(String basePrompt, EmotionAnalysisResult emotion) {
    StringBuilder sb = new StringBuilder(basePrompt);

    sb.append("\n\n【用户当前情绪识别结果】\n");
    sb.append("情绪类别：").append(emotion.getEmotion()).append("\n");
    sb.append("情绪强度：").append(emotion.getIntensity()).append("/100\n");
    sb.append("建议回复风格：").append(emotion.getReplyStyle()).append("\n");

    sb.append("\n【回复风格要求】\n");

    switch (emotion.getReplyStyle()) {
        case "安抚" -> sb.append("""
                用户当前可能处于低落、焦虑或脆弱状态。
                请优先共情和安抚，语气柔和，不要急着讲大道理。
                可以给一个很小、很轻的行动建议。
                """);

        case "活泼" -> sb.append("""
                用户当前情绪偏积极。
                可以用更轻快、鼓励、温暖的方式回应。
                可以适度表达开心和陪伴感。
                """);

        case "理性" -> sb.append("""
                用户当前可能有愤怒或不满。
                请先承认情绪，再帮助用户把事实、感受和下一步分开。
                不要激化矛盾，不要站队攻击他人。
                """);

        default -> sb.append("""
                用户当前情绪较平稳。
                请保持自然倾听、温和回应，不要过度分析。
                """);
    }

    return sb.toString();
}

}
