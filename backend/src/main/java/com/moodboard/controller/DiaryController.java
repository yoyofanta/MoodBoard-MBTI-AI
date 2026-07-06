package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.EmotionDiary;
import com.moodboard.repository.EmotionDiaryRepository;
import com.moodboard.service.AuthService;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/diaries")
public class DiaryController {
    private final AuthService authService;
    private final EmotionDiaryRepository diaryRepo;
    public DiaryController(AuthService authService, EmotionDiaryRepository diaryRepo) { this.authService = authService; this.diaryRepo = diaryRepo; }

    @PostMapping
    public Map<String, Object> save(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        LocalDate date = LocalDate.parse(MapUtil.str(body, "date", LocalDate.now().toString()));
        EmotionDiary d = diaryRepo.findByUserIdAndDiaryDate(userId, date).orElseGet(EmotionDiary::new);
        d.userId = userId; d.diaryDate = date;
        d.emotionEmoji = MapUtil.str(body, "emotionEmoji", "😌");
        d.emotionLabel = MapUtil.str(body, "emotionLabel", "平静");
        d.keyword = MapUtil.str(body, "keyword", "");
        d.content = MapUtil.str(body, "content", "");
        d.imageUrl = MapUtil.str(body, "imageUrl", "");
        Object lat = body.get("latitude");
        Object lng = body.get("longitude");

        if (lat != null && !String.valueOf(lat).isBlank()) {
            d.latitude = Double.parseDouble(String.valueOf(lat));
        }

        if (lng != null && !String.valueOf(lng).isBlank()) {
            d.longitude = Double.parseDouble(String.valueOf(lng));
        }

        d.locationName = MapUtil.str(body, "locationName", "");
        d.province = MapUtil.str(body, "province", "");
        d.city = MapUtil.str(body, "city", "");
        d.district = MapUtil.str(body, "district", "");
        d.sourceType = MapUtil.str(body, "sourceType", "MANUAL");
        d.sourceId = MapUtil.longValue(body, "sourceId");
        d.personaTag = MapUtil.str(body, "personaTag", "");
        d.personaPair = MapUtil.str(body, "personaPair", "");
        d.updatedAt = LocalDateTime.now();
        return R.ok(diaryRepo.save(d));
    }

    @GetMapping("/week")
    public Map<String, Object> week(@RequestHeader("Authorization") String auth, @RequestParam String startDate, @RequestParam String endDate) {
        Long userId = authService.currentUserId(auth);
        List<EmotionDiary> list = diaryRepo.findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(userId, LocalDate.parse(startDate), LocalDate.parse(endDate));
        return R.ok(list);
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestHeader("Authorization") String auth, @RequestParam(defaultValue = "") String keyword, @RequestParam(defaultValue = "") String emotionLabel) {
        Long userId = authService.currentUserId(auth);
        return R.ok(diaryRepo.findByUserIdOrderByDiaryDateDesc(userId).stream()
                .filter(d -> emotionLabel.isBlank() || (d.emotionLabel != null && d.emotionLabel.contains(emotionLabel)))
                .filter(d -> keyword.isBlank() || ((d.content != null && d.content.contains(keyword)) || (d.keyword != null && d.keyword.contains(keyword))))
                .toList());
    }

    @PostMapping("/from-battle")
    public Map<String, Object> fromBattle(@RequestHeader("Authorization") String auth, @RequestBody Map<String, Object> body) {
        body.put("sourceType", "BATTLE");
        body.putIfAbsent("emotionEmoji", "😶‍🌫️");
        body.putIfAbsent("emotionLabel", "思考中");
        return save(auth, body);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Long userId = authService.currentUserId(auth);
        EmotionDiary diary = diaryRepo.findById(id).orElseThrow();
        if (!diary.userId.equals(userId)) throw new RuntimeException("无权删除");
        diaryRepo.deleteById(id);
        return R.msg("删除成功");
    }
}
