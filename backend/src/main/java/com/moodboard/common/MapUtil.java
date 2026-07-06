package com.moodboard.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapUtil {
    public static String str(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    public static String str(Map<String, Object> map, String key, String defaultValue) {
        String value = str(map, key);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static Long longValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || String.valueOf(value).isBlank()) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    public static int intValue(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value == null || String.valueOf(value).isBlank()) return defaultValue;
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    public static boolean bool(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    public static List<String> stringList(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return new ArrayList<>();
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) result.add(String.valueOf(item));
            return result;
        }
        return List.of(String.valueOf(value));
    }
}
