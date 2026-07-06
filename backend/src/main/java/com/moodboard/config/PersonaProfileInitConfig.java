package com.moodboard.config;

import com.moodboard.entity.PersonaProfile;
import com.moodboard.repository.PersonaProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 项目启动时初始化人格画像数据。
 * 如果表里已经有人格，则不会重复插入。
 */
@Configuration
public class PersonaProfileInitConfig {

    @Bean
    public CommandLineRunner initPersonaProfiles(PersonaProfileRepository repo) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }

            save(repo, "INFJ", "温柔洞察者",
                    "善于共情、洞察内心，适合情绪低落、迷茫、需要被理解的用户。",
                    "共情,倾听,治愈,洞察", 95);

            save(repo, "INTJ", "冷静规划师",
                    "理性、结构化、善于拆解问题，适合需要规划、决策和逻辑分析的用户。",
                    "理性,规划,分析,目标", 90);

            save(repo, "ENFP", "能量小太阳",
                    "热情、鼓励、富有想象力，适合需要被激励、寻找希望和新可能的用户。",
                    "鼓励,活力,创意,陪伴", 88);

            save(repo, "INFP", "理想倾听者",
                    "温柔、敏感、尊重真实感受，适合需要倾诉委屈和梳理价值感的用户。",
                    "理想,温柔,感受,自我", 86);

            save(repo, "ENTP", "灵感辩手",
                    "擅长换角度思考，适合陷入单一视角、想要获得新想法的用户。",
                    "灵感,辩证,创新,反转", 82);

            save(repo, "ISTJ", "稳定守序者",
                    "稳定、务实、重视秩序，适合焦虑、混乱、需要恢复节奏的用户。",
                    "稳定,执行,秩序,现实", 80);
        };
    }

    private void save(
            PersonaProfileRepository repo,
            String code,
            String name,
            String description,
            String tags,
            int popularity
    ) {
        PersonaProfile p = new PersonaProfile();

        p.code = code;
        p.name = name;
        p.description = description;
        p.tags = tags;
        p.popularity = popularity;
        p.vectorJson = "";

        repo.save(p);
    }
}