package com.moodboard.service;

import com.moodboard.common.TextSimilarityUtil;
import com.moodboard.entity.KnowledgeChunk;
import com.moodboard.repository.KnowledgeChunkRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeBaseService {

    private final KnowledgeChunkRepository knowledgeChunkRepository;

    public KnowledgeBaseService(KnowledgeChunkRepository knowledgeChunkRepository) {
        this.knowledgeChunkRepository = knowledgeChunkRepository;
    }

    /**
     * 项目启动时初始化一批情绪知识库数据。
     * 如果表里已有数据，就不重复插入。
     */
    @PostConstruct
    public void initDefaultKnowledge() {
        if (knowledgeChunkRepository.count() > 0) {
            return;
        }

        List<KnowledgeChunk> chunks = List.of(
                new KnowledgeChunk(
                        "考试焦虑调节",
                        "考试焦虑通常来自对结果的担心、复习计划不清晰以及对失败的放大想象。可以先把任务拆成较小的复习单元，用25分钟专注学习加5分钟休息的方式降低启动难度。考试前不要反复否定自己，可以把注意力放在下一道题、下一页笔记和下一次复盘上。",
                        "焦虑,考试,学习压力,学生",
                        "默认知识库"
                ),
                new KnowledgeChunk(
                        "社交疲惫应对",
                        "社交疲惫并不一定说明你不合群，很多时候只是能量被持续消耗。可以给自己设置恢复时间，比如独处半小时、减少无效聊天、提前说明自己的边界。真正稳定的关系通常允许你有安静和休息的空间。",
                        "社交,疲惫,边界感,人际关系",
                        "默认知识库"
                ),
                new KnowledgeChunk(
                        "拖延与行动启动",
                        "拖延有时不是懒，而是任务太大、反馈太远或害怕做不好。可以使用最小行动法，例如只打开文档、只写三句话、只整理一个小标题。先完成一个很小的动作，比反复责备自己更容易进入状态。",
                        "拖延,行动力,学习,效率",
                        "默认知识库"
                ),
                new KnowledgeChunk(
                        "低落情绪自我照顾",
                        "低落时不要急着要求自己立刻变好，可以先完成基础照顾：喝水、吃饭、洗澡、睡眠和简单走动。如果持续低落很久，或者影响正常生活，建议及时向可信任的人、老师或专业人士寻求帮助。",
                        "低落,难过,自我照顾,情绪支持",
                        "默认知识库"
                ),
                new KnowledgeChunk(
                        "睡眠与情绪关系",
                        "睡眠不足会放大焦虑、烦躁和无力感。改善睡眠可以从固定入睡时间、睡前减少屏幕刺激、降低咖啡因摄入开始。如果暂时睡不着，可以先做呼吸放松或轻度拉伸，而不是强迫自己马上入睡。",
                        "睡眠,焦虑,疲惫,作息",
                        "默认知识库"
                ),
                new KnowledgeChunk(
                        "情绪记录的意义",
                        "情绪记录不是为了评判自己，而是为了看见自己的状态变化。通过记录日期、emoji、关键词和事件，可以发现哪些场景容易让自己焦虑、低落或开心，从而更好地调整生活节奏。",
                        "情绪日记,记录,自我觉察,复盘",
                        "默认知识库"
                )
        );

        knowledgeChunkRepository.saveAll(chunks);
    }

    /**
     * 根据用户问题检索最相关的知识片段。
     */
    public List<Map<String, Object>> search(String query, int topK) {
        List<KnowledgeChunk> all = knowledgeChunkRepository.findAll();

        return all.stream()
                .map(chunk -> {
                    double score = calculateScore(query, chunk);

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", chunk.getId());
                    item.put("title", chunk.getTitle());
                    item.put("content", chunk.getContent());
                    item.put("tags", chunk.getTags());
                    item.put("source", chunk.getSource());
                    item.put("score", score);

                    return item;
                })
                .filter(item -> (double) item.get("score") > 0)
                .sorted((a, b) -> Double.compare(
                        (double) b.get("score"),
                        (double) a.get("score")
                ))
                .limit(topK)
                .collect(Collectors.toList());
    }

    private double calculateScore(String query, KnowledgeChunk chunk) {
        String fullText = chunk.getTitle() + " " + chunk.getTags() + " " + chunk.getContent();

        double baseScore = TextSimilarityUtil.similarity(query, fullText);

        /**
         * 标签命中加权：
         * 如果用户问题里直接包含标签词，分数更高。
         */
        double tagBoost = 0.0;

        if (chunk.getTags() != null) {
            String[] tags = chunk.getTags().split(",");

            for (String tag : tags) {
                if (!tag.isBlank() && query.contains(tag.trim())) {
                    tagBoost += 0.15;
                }
            }
        }

        return Math.min(1.0, baseScore + tagBoost);
    }
}