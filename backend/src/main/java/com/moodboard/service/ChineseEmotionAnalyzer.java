package com.moodboard.service;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 中文情感分析工具类
 * 当前版本使用关键词规则实现，优点是简单、稳定、无额外依赖，适合课程项目和简历项目。
 * 后续可以替换为 HanLP、SnowNLP 或大模型情绪识别接口。
 */
@Component
public class ChineseEmotionAnalyzer {

    private final List<String> happyWords = List.of(
            "开心", "高兴", "快乐", "惊喜", "喜欢", "顺利", "幸福", "放松", "满足", "太好了"
    );

    private final List<String> sadWords = List.of(
            "难过", "伤心", "委屈", "哭", "失落", "孤独", "崩溃", "累", "疲惫", "撑不住"
    );

    private final List<String> angryWords = List.of(
            "生气", "愤怒", "烦死", "讨厌", "气死", "不爽", "火大", "受不了", "憋屈"
    );

    private final List<String> anxiousWords = List.of(
            "焦虑", "紧张", "害怕", "担心", "压力", "恐慌", "不安", "慌", "怕", "睡不着"
    );

    public EmotionAnalysisResult analyze(String text) {
        if (text == null || text.isBlank()) {
            return new EmotionAnalysisResult("平静", 20, "倾听");
        }

        int happy = score(text, happyWords);
        int sad = score(text, sadWords);
        int angry = score(text, angryWords);
        int anxious = score(text, anxiousWords);

        int max = Math.max(Math.max(happy, sad), Math.max(angry, anxious));

        if (max == 0) {
            return new EmotionAnalysisResult("平静", 30, "倾听");
        }

        int intensity = Math.min(100, 40 + max * 15);

        if (happy == max) {
            return new EmotionAnalysisResult("开心", intensity, "活泼");
        }

        if (sad == max) {
            return new EmotionAnalysisResult("低落", intensity, "安抚");
        }

        if (angry == max) {
            return new EmotionAnalysisResult("愤怒", intensity, "理性");
        }

        return new EmotionAnalysisResult("焦虑", intensity, "安抚");
    }

    private int score(String text, List<String> words) {
        int score = 0;

        for (String word : words) {
            if (text.contains(word)) {
                score++;
            }
        }

        return score;
    }
}
