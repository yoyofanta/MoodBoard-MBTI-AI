package com.moodboard.common;

import java.util.HashSet;
import java.util.Set;

public class TextSimilarityUtil {

    /**
     * 简易中文文本相似度：
     * 采用字符集合 Jaccard 相似度。
     * 优点：不用引入向量库，适合学生项目第一版。
     */
    public static double similarity(String query, String text) {
        if (query == null || query.isBlank() || text == null || text.isBlank()) {
            return 0.0;
        }

        Set<String> qSet = toCharSet(query);
        Set<String> tSet = toCharSet(text);

        if (qSet.isEmpty() || tSet.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(qSet);
        intersection.retainAll(tSet);

        Set<String> union = new HashSet<>(qSet);
        union.addAll(tSet);

        return union.isEmpty() ? 0.0 : intersection.size() * 1.0 / union.size();
    }

    private static Set<String> toCharSet(String text) {
        Set<String> set = new HashSet<>();

        String cleaned = text
                .replaceAll("\\s+", "")
                .replaceAll("[，。！？、,.!?;；:：()（）【】《》\"']", "");

        for (int i = 0; i < cleaned.length(); i++) {
            set.add(String.valueOf(cleaned.charAt(i)));
        }

        return set;
    }
}