package com.moodboard.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LocationToolService {

    @Value("${amap.key:}")
    private String amapKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> reverseLocation(Double lat, Double lng) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("toolName", "location_reverse_geocode");
        result.put("latitude", lat);
        result.put("longitude", lng);

        if (lat == null || lng == null) {
            result.put("success", false);
            result.put("message", "经纬度不能为空");
            return result;
        }

        if (amapKey == null || amapKey.isBlank()) {
            result.put("success", false);
            result.put("province", "");
            result.put("city", "");
            result.put("district", "");
            result.put("formattedAddress", "");
            result.put("locationName", "当前位置");
            result.put("message", "AMAP_KEY 为空，未调用高德接口");
            return result;
        }

        try {
            String url = "https://restapi.amap.com/v3/geocode/regeo"
                    + "?key=" + amapKey
                    + "&location=" + lng + "," + lat
                    + "&extensions=base"
                    + "&radius=1000";

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                result.put("success", false);
                result.put("message", "高德接口返回为空");
                return result;
            }

            String status = toText(response.get("status"));
            String info = toText(response.get("info"));
            String infocode = toText(response.get("infocode"));

            result.put("amapStatus", status);
            result.put("amapInfo", info);
            result.put("amapInfocode", infocode);

            if (!"1".equals(status)) {
                result.put("success", false);
                result.put("message", "高德接口调用失败：" + info + "，infocode=" + infocode);
                return result;
            }

            Map regeocode = (Map) response.get("regeocode");

            if (regeocode == null) {
                result.put("success", false);
                result.put("message", "高德未返回 regeocode");
                return result;
            }

            String formattedAddress = toText(regeocode.get("formatted_address"));

            Map addressComponent = (Map) regeocode.get("addressComponent");

            String province = "";
            String city = "";
            String district = "";

            if (addressComponent != null) {
                province = toText(addressComponent.get("province"));
                city = toText(addressComponent.get("city"));
                district = toText(addressComponent.get("district"));
            }

            if (city.isBlank()) {
                city = province;
            }

            String locationName = (city + " " + district).trim();

            result.put("success", true);
            result.put("province", province);
            result.put("city", city);
            result.put("district", district);
            result.put("formattedAddress", formattedAddress);
            result.put("locationName", locationName.isBlank() ? "当前位置" : locationName);
            result.put("message", "工具调用成功");

            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "后端调用高德接口异常：" + e.getMessage());
            return result;
        }
    }

    private String toText(Object value) {
        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }
}