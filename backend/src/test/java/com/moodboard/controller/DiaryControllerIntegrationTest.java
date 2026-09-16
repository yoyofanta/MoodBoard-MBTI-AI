package com.moodboard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:diary-test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "ai.mode=mock",
        "amap.key="
})
@AutoConfigureMockMvc
class DiaryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void diaryCrudAndDateRangesFormACompleteLifecycle() throws Exception {
        String token = registerAndLogin();
        String auth = "Bearer " + token;
        String date = "2026-03-15";

        String createResponse = mockMvc.perform(post("/api/diaries")
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "date", date,
                                "emotionEmoji", "😊",
                                "emotionLabel", "开心",
                                "keyword", "课程",
                                "content", "初始内容"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.diaryDate").value(date))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(createResponse).path("data").path("id").asLong();

        mockMvc.perform(get("/api/diaries/date")
                        .header("Authorization", auth)
                        .param("date", date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.content").value("初始内容"));

        mockMvc.perform(put("/api/diaries/{id}", id)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "diaryDate", date,
                                "emotionEmoji", "💪",
                                "emotionLabel", "有力量",
                                "keyword", "答辩",
                                "content", "修改后的内容"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.content").value("修改后的内容"));

        mockMvc.perform(get("/api/diaries/week")
                        .header("Authorization", auth)
                        .param("startDate", "2026-03-09")
                        .param("endDate", "2026-03-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(id));

        mockMvc.perform(get("/api/diaries/month")
                        .header("Authorization", auth)
                        .param("year", "2026")
                        .param("month", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(id));

        mockMvc.perform(get("/api/diaries/year")
                        .header("Authorization", auth)
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(id));

        mockMvc.perform(delete("/api/diaries/{id}", id)
                        .header("Authorization", auth))
                .andExpect(status().isOk());

        String missingByDate = mockMvc.perform(get("/api/diaries/date")
                        .header("Authorization", auth)
                        .param("date", date))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode missingData = objectMapper.readTree(missingByDate).path("data");
        org.junit.jupiter.api.Assertions.assertTrue(missingData.isNull());

        mockMvc.perform(put("/api/diaries/{id}", id)
                        .header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"不存在\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("日记不存在"));
    }

    private String registerAndLogin() throws Exception {
        String username = "diary_test_" + UUID.randomUUID().toString().replace("-", "");
        String credentials = objectMapper.writeValueAsString(Map.of(
                "username", username,
                "password", "123456"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(loginResponse).path("data").path("token").asText();
    }
}
