package com.moodboard.service;

import com.moodboard.entity.ChatMessage;
import com.moodboard.entity.PersonaProfile;
import com.moodboard.entity.UserPersonaProfile;
import com.moodboard.repository.ChatMessageRepository;
import com.moodboard.repository.PersonaProfileRepository;
import com.moodboard.repository.UserPersonaProfileRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 人格智能推荐服务
 * 根据用户画像文本和人格描述做相似度计算。
 */
@Service
public class PersonaRecommendationService {

    private final PersonaProfileRepository personaRepo;
    private final UserPersonaProfileRepository userProfileRepo;
    private final ChatMessageRepository chatMessageRepo;

    public PersonaRecommendationService(
            PersonaProfileRepository personaRepo,
            UserPersonaProfileRepository userProfileRepo,
            ChatMessageRepository chatMessageRepo
    ) {
        this.personaRepo = personaRepo;
        this.userProfileRepo = userProfileRepo;
        this.chatMessageRepo = chatMessageRepo;
    }

    public List<Map<String, Object>> recommend(Long userId) {
        List<PersonaProfile> personas = personaRepo.findAllByOrderByPopularityDesc();

        if (personas.isEmpty()) {
            return List.of();
        }

        Optional<UserPersonaProfile> userProfileOptional = userProfileRepo.findByUserId(userId);
        List<ChatMessage> history = chatMessageRepo.findTop20ByUserIdOrderByCreatedAtDesc(userId);

        // 冷启动：没有画像，也没有历史对话，直接推荐热门人格
        if (userProfileOptional.isEmpty() && history.isEmpty()) {
            return personas.stream()
                    .limit(5)
                    .map(p -> toResult(p, 0.6, "新用户冷启动，优先推荐热门人格"))
                    .collect(Collectors.toList());
        }

        StringBuilder userFeatureText = new StringBuilder();

        userProfileOptional.ifPresent(profile -> {
            if (profile.mbti != null) {
                userFeatureText.append("用户MBTI是").append(profile.mbti).append("。");
            }

            if (profile.featureText != null) {
                userFeatureText.append(profile.featureText).append("。");
            }

            if (profile.historySummary != null) {
                userFeatureText.append(profile.historySummary).append("。");
            }
        });

        for (ChatMessage message : history) {
            if ("user".equalsIgnoreCase(message.role) && message.content != null) {
                userFeatureText.append(message.content).append("。");
            }
        }

        Map<String, Double> userVector = TextSimilarityUtil.vectorize(userFeatureText.toString());

        return personas.stream()
                .map(persona -> {
                    String personaText = persona.code + " "
                            + persona.name + " "
                            + persona.description + " "
                            + persona.tags;

                    Map<String, Double> personaVector = TextSimilarityUtil.vectorize(personaText);

                    double score = TextSimilarityUtil.cosineSimilarity(userVector, personaVector);

                    // 热度轻微加权，避免新用户结果过于随机
                    double finalScore = score + Math.min(0.1, persona.popularity / 1000.0);

                    return toResult(persona, finalScore, "根据用户画像与历史对话相似度推荐");
                })
                .sorted((a, b) -> Double.compare(
                        (Double) b.get("score"),
                        (Double) a.get("score")
                ))
                .limit(5)
                .collect(Collectors.toList());
    }

    private Map<String, Object> toResult(PersonaProfile p, double score, String reason) {
        Map<String, Object> map = new LinkedHashMap<>();

        map.put("code", p.code);
        map.put("name", p.name);
        map.put("description", p.description);
        map.put("tags", p.tags);
        map.put("popularity", p.popularity);
        map.put("score", Math.round(score * 10000.0) / 100.0);
        map.put("reason", reason);

        return map;
    }
}