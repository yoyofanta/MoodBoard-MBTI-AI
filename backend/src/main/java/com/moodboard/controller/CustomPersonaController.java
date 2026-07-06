package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.CustomPersona;
import com.moodboard.repository.CustomPersonaRepository;
import com.moodboard.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-personas")
public class CustomPersonaController {
    private final AuthService authService;
    private final CustomPersonaRepository repo;
    public CustomPersonaController(AuthService authService, CustomPersonaRepository repo) { this.authService = authService; this.repo = repo; }

    @GetMapping
    public Map<String, Object> list(@RequestHeader("Authorization") String auth) {
        Long userId = authService.currentUserId(auth);
        return R.ok(repo.findByUserIdAndIsDeletedFalseOrderByUpdatedAtDesc(userId));
    }

    @PostMapping
    public Map<String, Object> create(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        CustomPersona cp = new CustomPersona();
        cp.userId = userId;
        fill(cp, body);
        return R.ok(repo.save(cp));
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@RequestHeader("Authorization") String auth, @PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        CustomPersona cp = repo.findById(id).orElseThrow();
        if (!cp.userId.equals(userId)) throw new RuntimeException("无权修改");
        fill(cp, body);
        cp.updatedAt = LocalDateTime.now();
        return R.ok(repo.save(cp));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Long userId = authService.currentUserId(auth);
        CustomPersona cp = repo.findById(id).orElseThrow();
        if (!cp.userId.equals(userId)) throw new RuntimeException("无权删除");
        cp.isDeleted = true;
        cp.updatedAt = LocalDateTime.now();
        return R.ok(repo.save(cp));
    }

    private void fill(CustomPersona cp, Map<String, Object> body) {
        cp.name = MapUtil.str(body, "name", "我的人格面具");
        cp.basePersona = MapUtil.str(body, "basePersona", "INFP");
        cp.avatar = MapUtil.str(body, "avatar", "🎭");
        cp.themeColor = MapUtil.str(body, "themeColor", "#D9A9C7");
        cp.callName = MapUtil.str(body, "callName", "");
        cp.promptSuffix = MapUtil.str(body, "promptSuffix", "请更温柔地回应我。");
    }
}
