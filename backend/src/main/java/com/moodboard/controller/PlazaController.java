package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.PlazaPost;
import com.moodboard.repository.PlazaPostRepository;
import com.moodboard.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/plaza/posts")
public class PlazaController {
    private final AuthService authService;
    private final PlazaPostRepository repo;
    public PlazaController(AuthService authService, PlazaPostRepository repo) { this.authService = authService; this.repo = repo; }

    @GetMapping
    public Map<String, Object> list() { return R.ok(repo.findTop50ByOrderByCreatedAtDesc()); }

    @PostMapping
    public Map<String, Object> publish(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        PlazaPost p = new PlazaPost();
        p.userId = userId; p.sourceType = MapUtil.str(body, "sourceType", "DIARY"); p.sourceId = MapUtil.longValue(body, "sourceId");
        p.emotionEmoji = MapUtil.str(body, "emotionEmoji", "🫧"); p.personaTag = MapUtil.str(body, "personaTag", ""); p.content = MapUtil.str(body, "content", "");
        return R.ok(repo.save(p));
    }
}
