package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.UserProfile;
import com.moodboard.repository.UserProfileRepository;
import com.moodboard.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
public class ProfileController {
    private final AuthService authService;
    private final UserProfileRepository profileRepo;
    public ProfileController(AuthService authService, UserProfileRepository profileRepo) { this.authService = authService; this.profileRepo = profileRepo; }

    @GetMapping
    public Map<String, Object> get(@RequestHeader("Authorization") String auth) {
        Long userId = authService.currentUserId(auth);
        UserProfile profile = profileRepo.findByUserId(userId).orElseGet(() -> {
            UserProfile p = new UserProfile();
            p.userId = userId; p.nickname = "Mooder"; p.occupation = "student"; p.ageRange = "18-22"; p.gender = "未透露"; p.residentPersona = "INFJ";
            return p;
        });
        return R.ok(profile);
    }

    @PostMapping
    public Map<String, Object> save(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        UserProfile p = profileRepo.findByUserId(userId).orElseGet(UserProfile::new);
        p.userId = userId;
        p.nickname = MapUtil.str(body, "nickname", "Mooder");
        p.occupation = MapUtil.str(body, "occupation", "student");
        p.gender = MapUtil.str(body, "gender", "未透露");
        p.ageRange = MapUtil.str(body, "ageRange", "18-22");
        p.residentPersona = MapUtil.str(body, "residentPersona", "INFJ");
        p.updatedAt = LocalDateTime.now();
        return R.ok(profileRepo.save(p));
    }
}
