<template>
  <div class="knowledge-workspace">
    <section class="hero-card">
      <p class="sub-title">RAG Knowledge Base</p>
      <h2 class="page-title">情绪知识库检索</h2>
      <p class="desc">
        输入你的问题，系统会从情绪知识库中检索相关片段。下一步会把这些片段拼进 DeepSeek，让 AI 基于知识库回答。
      </p>

      <div class="search-box">
        <input
          v-model="query"
          class="search-input"
          placeholder="例如：我最近考试很焦虑怎么办？"
          @keydown.enter="search"
        />

        <button class="search-btn" :disabled="loading" @click="search">
          {{ loading ? '检索中...' : '开始检索' }}
        </button>
        <button class="ask-btn" :disabled="loading" @click="ask">
        {{ loading ? '生成中...' : 'RAG 生成回答' }}
        </button>
      </div>
    </section>

    <section v-if="answer" class="answer-card">
  <p class="sub-title">RAG Answer</p>
  <h3 class="section-title">AI 基于知识库的回答</h3>
  <p class="answer-text">
    {{ answer }}
  </p>
</section>

    <section class="result-card">
      <div class="section-head">
        <div>
          <p class="sub-title">Search Results</p>
          <h3 class="section-title">检索结果</h3>
        </div>

        <p class="count-text">
          共 {{ results.length }} 条
        </p>
      </div>

      <div v-if="!searched" class="empty-state">
        输入问题后，系统会展示最相关的情绪知识片段。
      </div>

      <div v-else-if="results.length === 0" class="empty-state">
        暂时没有匹配到相关内容，可以换个说法试试。
      </div>

      <div v-else class="result-list">
        <article
          v-for="item in results"
          :key="item.id"
          class="result-item"
        >
          <div class="result-top">
            <h4 class="result-title">
              {{ item.title }}
            </h4>

            <span class="score">
              相似度：{{ formatScore(item.score) }}
            </span>
          </div>

          <p class="content">
            {{ item.content }}
          </p>

          <div class="tag-row">
            <span
              v-for="tag in splitTags(item.tags)"
              :key="tag"
              class="tag"
            >
              {{ tag }}
            </span>
          </div>

          <p class="source">
            来源：{{ item.source || '默认知识库' }}
          </p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../api'

type KnowledgeResult = {
  id: number
  title: string
  content: string
  tags: string
  source: string
  score: number
}

const query = ref('我最近考试很焦虑怎么办')
const results = ref<KnowledgeResult[]>([])
const answer = ref('')
const loading = ref(false)
const searched = ref(false)

async function search() {
  if (!query.value.trim()) {
    alert('请输入问题')
    return
  }

  loading.value = true
  searched.value = true

  try {
    const res: any = await api.searchKnowledge(query.value.trim(), 3)
    const data = res.data || res

    results.value = data.results || []
  } catch (e) {
    console.error(e)
    alert('知识库检索失败，请检查后端接口')
  } finally {
    loading.value = false
  }
}

async function ask() {
  if (!query.value.trim()) {
    alert('请输入问题')
    return
  }

  loading.value = true
  searched.value = true
  answer.value = ''

  try {
    const res: any = await api.askKnowledge(query.value.trim(), 3)
    const data = res.data || res

    answer.value = data.answer || ''
    results.value = data.contexts || []
  } catch (e) {
    console.error(e)
    alert('RAG 回答生成失败，请检查后端接口或 DeepSeek 配置')
  } finally {
    loading.value = false
  }
}

function splitTags(tags: string) {
  if (!tags) {
    return []
  }

  return tags
    .split(',')
    .map(item => item.trim())
    .filter(Boolean)
}

function formatScore(score: number) {
  if (score === undefined || score === null) {
    return '0%'
  }

  return `${Math.round(score * 100)}%`
}
</script>

<style scoped>
.knowledge-workspace {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.hero-card,
.result-card {
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.86);
  padding: 28px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
}

.sub-title {
  font-size: 14px;
  color: #a39d92;
}

.page-title {
  margin-top: 6px;
  font-size: 30px;
  font-weight: 700;
  color: #2f2f2f;
}

.desc {
  margin-top: 12px;
  max-width: 760px;
  font-size: 15px;
  line-height: 1.8;
  color: #8e887f;
}

.search-box {
  margin-top: 22px;
  display: flex;
  gap: 12px;
}

.search-input {
  flex: 1;
  border-radius: 999px;
  background: #f8f4ed;
  padding: 15px 20px;
  font-size: 15px;
  outline: none;
}

.search-btn {
  border-radius: 999px;
  background: #c3ab89;
  padding: 15px 24px;
  font-size: 15px;
  color: white;
}

.search-btn:disabled {
  opacity: 0.6;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 700;
  color: #2f2f2f;
}

.count-text {
  font-size: 14px;
  color: #9b968c;
}

.empty-state {
  margin-top: 20px;
  border-radius: 24px;
  background: #fbf7f1;
  padding: 28px;
  text-align: center;
  color: #9b968c;
}

.result-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  border-radius: 26px;
  background: #fbf7f1;
  padding: 22px;
}

.result-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.result-title {
  font-size: 18px;
  font-weight: 700;
  color: #4a4036;
}

.score {
  border-radius: 999px;
  background: white;
  padding: 6px 12px;
  font-size: 12px;
  color: #8e887f;
}

.content {
  margin-top: 12px;
  font-size: 15px;
  line-height: 1.8;
  color: #555;
}

.tag-row {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  border-radius: 999px;
  background: #eadbc8;
  padding: 5px 12px;
  font-size: 12px;
  color: #6e5741;
}

.source {
  margin-top: 12px;
  font-size: 12px;
  color: #a39d92;
}

.ask-btn {
  border-radius: 999px;
  background: #afc9b8;
  padding: 15px 24px;
  font-size: 15px;
  color: white;
}

.ask-btn:disabled {
  opacity: 0.6;
}

.answer-card {
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.9);
  padding: 28px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
}

.answer-text {
  margin-top: 16px;
  white-space: pre-wrap;
  font-size: 15px;
  line-height: 1.9;
  color: #555;
}
</style>