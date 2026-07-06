package com.moodboard.service;

import java.util.HashMap;
import java.util.Map;

/**
 * 简易文本相似度工具类
 * 使用字符频率向量 + 余弦相似度实现。
 * 优点：无需外部依赖，H2/MySQL 都兼容，适合课程项目。
 */
public class TextSimilarityUtil {

    public static Map<String, Double> vectorize(String text) {
        Map<String, Double> vector = new HashMap<>();

        if (text == null) {
            return vector;
        }

        String clean = text
                .replaceAll("\\s+", "")
                .replaceAll("[，。！？、,.!?]", "");

        for (int i = 0; i < clean.length(); i++) {
            String key = String.valueOf(clean.charAt(i));
            vector.put(key, vector.getOrDefault(key, 0.0) + 1.0);
        }

        return vector;
    }

    public static double cosineSimilarity(Map<String, Double> a, Map<String, Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String key : a.keySet()) {
            dot += a.getOrDefault(key, 0.0) * b.getOrDefault(key, 0.0);
        }

        for (double value : a.values()) {
            normA += value * value;
        }

        for (double value : b.values()) {
            normB += value * value;
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}