package com.moodboard.controller;

import com.moodboard.common.R;
import com.moodboard.service.LocationToolService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/tools")
public class AiToolController {

    private final LocationToolService locationToolService;

    public AiToolController(LocationToolService locationToolService) {
        this.locationToolService = locationToolService;
    }

    /**
     * Tool Calling 示例：位置逆地理编码工具
     *
     * POST /api/ai/tools/location/reverse
     *
     * body:
     * {
     *   "lat": 30.123,
     *   "lng": 120.123
     * }
     */
    @PostMapping("/location/reverse")
    public Map<String, Object> reverseLocation(@RequestBody Map<String, Object> body) {
        Double lat = toDouble(body.get("lat"));
        Double lng = toDouble(body.get("lng"));

        Map<String, Object> toolResult = locationToolService.reverseLocation(lat, lng);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("toolType", "LOCATION");
        data.put("toolName", "location_reverse_geocode");
        data.put("description", "根据浏览器经纬度调用高德逆地理编码接口，返回城市、区县和详细地址");
        data.put("input", Map.of(
                "lat", lat,
                "lng", lng
        ));
        data.put("output", toolResult);

        return R.ok(data);
    }

    private Double toDouble(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}