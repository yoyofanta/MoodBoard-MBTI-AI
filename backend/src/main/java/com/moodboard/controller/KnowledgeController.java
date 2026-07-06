package com.moodboard.controller;

import com.moodboard.common.R;
import com.moodboard.service.AuthService;
import com.moodboard.service.KnowledgeBaseService;
import com.moodboard.service.RagAnswerService;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeBaseService knowledgeBaseService;
    private final RagAnswerService ragAnswerService;
    private final AuthService authService;

    public KnowledgeController(
            KnowledgeBaseService knowledgeBaseService,
            RagAnswerService ragAnswerService,
            AuthService authService
    ) {
        this.knowledgeBaseService = knowledgeBaseService;
        this.ragAnswerService = ragAnswerService;
        this.authService = authService;
    }

    /**
     * 简易 RAG 检索接口
     * GET /api/knowledge/search?q=我最近考试很焦虑&topK=3
     */
    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "3") Integer topK
    ) {
        List<Map<String, Object>> results = knowledgeBaseService.search(q, topK);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("query", q);
        data.put("topK", topK);
        data.put("results", results);

        return R.ok(data);
    }

    /**
     * RAG + DeepSeek + Memory 回答接口
     * GET /api/knowledge/ask?q=我最近考试很焦虑&topK=3
     */
    @GetMapping("/ask")
    public Map<String, Object> ask(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam String q,
            @RequestParam(defaultValue = "3") Integer topK
    ) {
        try {
            Long userId = resolveUserId(auth);

            Map<String, Object> data = ragAnswerService.ask(q, topK, userId);

            return R.ok(data);
        } catch (Exception e) {
            e.printStackTrace();

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("query", q);
            data.put("topK", topK);
            data.put("answer", "RAG 回答生成失败，但接口已捕获异常。错误原因：" + e.getMessage());
            data.put("contexts", List.of());

            return R.ok(data);
        }
    }

    /**
     * 本地演示时，浏览器直接访问接口没有 Authorization。
     * 为了方便测试，没有 token 时默认写入 userId=1 的记忆。
     */
    private Long resolveUserId(String auth) {
        try {
            if (auth == null || auth.isBlank()) {
                return 1L;
            }

            return authService.currentUserId(auth);
        } catch (Exception e) {
            return 1L;
        }
    }
}