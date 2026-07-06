package com.moodboard.common;

import java.util.LinkedHashMap;
import java.util.Map;

public class R {
    public static Map<String, Object> ok() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", 200);
        map.put("message", "success");
        return map;
    }

    public static Map<String, Object> ok(Object data) {
        Map<String, Object> map = ok();
        map.put("data", data);
        return map;
    }

    public static Map<String, Object> msg(String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", 200);
        map.put("message", message);
        return map;
    }

    public static Map<String, Object> error(int code, String message) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("message", message);
        return map;
    }
}
