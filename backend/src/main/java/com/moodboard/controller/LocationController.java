package com.moodboard.controller;

import com.moodboard.common.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Value("${amap.key:}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/reverse")
    public Map<String, Object> reverse(
            @RequestParam Double lat,
            @RequestParam Double lng
    ) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("latitude", lat);
        result.put("longitude", lng);

        if (amapKey == null || amapKey.isBlank()) {
            result.put("province", "");
            result.put("city", "");
            result.put("district", "");
            result.put("formattedAddress", "");
            result.put("locationName", "当前位置");
            result.put("debug", "AMAP_KEY 为空，未调用高德接口");
            return R.ok(result);
        }

        try {
            // 高德要求 location=经度,纬度，注意 lng 在前，lat 在后
            String url =
                    "https://restapi.amap.com/v3/geocode/regeo" +
                            "?key=" + amapKey +
                            "&location=" + lng + "," + lat +
                            "&extensions=base" +
                            "&radius=1000";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                result.put("province", "");
                result.put("city", "");
                result.put("district", "");
                result.put("formattedAddress", "");
                result.put("locationName", "当前位置");
                result.put("debug", "高德接口返回为空");
                return R.ok(result);
            }

            String status = toText(response.get("status"));
            String info = toText(response.get("info"));
            String infocode = toText(response.get("infocode"));

            if (!"1".equals(status)) {
                result.put("province", "");
                result.put("city", "");
                result.put("district", "");
                result.put("formattedAddress", "");
                result.put("locationName", "当前位置");
                result.put("debug", "高德接口失败：" + info + "，infocode=" + infocode);
                return R.ok(result);
            }

            Map regeocode = (Map) response.get("regeocode");

            if (regeocode == null) {
                result.put("province", "");
                result.put("city", "");
                result.put("district", "");
                result.put("formattedAddress", "");
                result.put("locationName", "当前位置");
                result.put("debug", "高德未返回 regeocode");
                return R.ok(result);
            }

            String formattedAddress = toText(regeocode.get("formatted_address"));

            Map addressComponent = (Map) regeocode.get("addressComponent");

            if (addressComponent == null) {
                result.put("province", "");
                result.put("city", "");
                result.put("district", "");
                result.put("formattedAddress", formattedAddress);
                result.put("locationName", formattedAddress.isBlank() ? "当前位置" : formattedAddress);
                result.put("debug", "高德未返回 addressComponent");
                return R.ok(result);
            }

            String province = toText(addressComponent.get("province"));
            String city = toText(addressComponent.get("city"));
            String district = toText(addressComponent.get("district"));

            // 有些直辖市 city 可能为空或数组，这里兜底
            if (city.isBlank()) {
                city = province;
            }

            String locationName = (city + " " + district).trim();

            result.put("province", province);
            result.put("city", city);
            result.put("district", district);
            result.put("formattedAddress", formattedAddress);
            result.put("locationName", locationName.isBlank() ? "当前位置" : locationName);
            result.put("debug", "success");

            return R.ok(result);
        } catch (Exception e) {
            e.printStackTrace();

            result.put("province", "");
            result.put("city", "");
            result.put("district", "");
            result.put("formattedAddress", "");
            result.put("locationName", "当前位置");
            result.put("debug", "后端调用高德异常：" + e.getMessage());

            return R.ok(result);
        }
    }

    private String toText(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String text) {
            return text;
        }

        return String.valueOf(value);
    }
}