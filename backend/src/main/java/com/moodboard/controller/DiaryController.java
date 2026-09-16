package com.moodboard.controller;

import com.moodboard.common.MapUtil;
import com.moodboard.common.R;
import com.moodboard.entity.EmotionDiary;
import com.moodboard.repository.EmotionDiaryRepository;
import com.moodboard.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
        LocalDate date = readDate(body, LocalDate.now());
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

    @GetMapping("/date")
    public Map<String, Object> date(@RequestHeader("Authorization") String auth, @RequestParam String date) {
        Long userId = authService.currentUserId(auth);
        return R.ok(diaryRepo.findByUserIdAndDiaryDate(userId, LocalDate.parse(date)).orElse(null));
    }

    @GetMapping("/week")
    public Map<String, Object> week(@RequestHeader("Authorization") String auth, @RequestParam String startDate, @RequestParam String endDate) {
        Long userId = authService.currentUserId(auth);
        List<EmotionDiary> list = diaryRepo.findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(userId, LocalDate.parse(startDate), LocalDate.parse(endDate));
        return R.ok(list);
    }

    @GetMapping("/month")
    public Map<String, Object> month(@RequestHeader("Authorization") String auth, @RequestParam int year, @RequestParam int month) {
        Long userId = authService.currentUserId(auth);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return R.ok(diaryRepo.findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(userId, start, end));
    }

    @GetMapping("/year")
    public Map<String, Object> year(@RequestHeader("Authorization") String auth, @RequestParam int year) {
        Long userId = authService.currentUserId(auth);
        return R.ok(diaryRepo.findByUserIdAndDiaryDateBetweenOrderByDiaryDateAsc(
                userId, LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)));
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

    @PutMapping("/{id}")
    public Map<String, Object> update(@RequestHeader("Authorization") String auth, @PathVariable Long id,
                                      @RequestBody Map<String, Object> body) {
        Long userId = authService.currentUserId(auth);
        EmotionDiary diary = getOwnedDiary(id, userId);

        diary.diaryDate = readDate(body, diary.diaryDate);
        diary.emotionEmoji = readString(body, "emotionEmoji", diary.emotionEmoji);
        diary.emotionLabel = readString(body, "emotionLabel", diary.emotionLabel);
        diary.keyword = readString(body, "keyword", diary.keyword);
        diary.content = readString(body, "content", diary.content);
        diary.imageUrl = readString(body, "imageUrl", diary.imageUrl);
        diary.latitude = readDouble(body, "latitude", diary.latitude);
        diary.longitude = readDouble(body, "longitude", diary.longitude);
        diary.locationName = readString(body, "locationName", diary.locationName);
        diary.province = readString(body, "province", diary.province);
        diary.city = readString(body, "city", diary.city);
        diary.district = readString(body, "district", diary.district);
        diary.sourceType = readString(body, "sourceType", diary.sourceType);
        if (body.containsKey("sourceId")) diary.sourceId = MapUtil.longValue(body, "sourceId");
        diary.personaTag = readString(body, "personaTag", diary.personaTag);
        diary.personaPair = readString(body, "personaPair", diary.personaPair);
        diary.updatedAt = LocalDateTime.now();

        return R.ok(diaryRepo.save(diary));
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        Long userId = authService.currentUserId(auth);
        EmotionDiary diary = getOwnedDiary(id, userId);
        diaryRepo.deleteById(id);
        return R.msg("删除成功");
    }

    private EmotionDiary getOwnedDiary(Long id, Long userId) {
        EmotionDiary diary = diaryRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "日记不存在"));
        if (!diary.userId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该日记");
        }
        return diary;
    }

    private LocalDate readDate(Map<String, Object> body, LocalDate defaultValue) {
        String value = MapUtil.str(body, "date");
        if (value == null || value.isBlank()) value = MapUtil.str(body, "diaryDate");
        return value == null || value.isBlank() ? defaultValue : LocalDate.parse(value);
    }

    private String readString(Map<String, Object> body, String key, String defaultValue) {
        return body.containsKey(key) ? MapUtil.str(body, key) : defaultValue;
    }

    private Double readDouble(Map<String, Object> body, String key, Double defaultValue) {
        if (!body.containsKey(key)) return defaultValue;
        Object value = body.get(key);
        return value == null || String.valueOf(value).isBlank() ? null : Double.parseDouble(String.valueOf(value));
    }
}
