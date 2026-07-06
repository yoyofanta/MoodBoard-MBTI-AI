<template>
  <section class="agent-card">
    <div class="head">
      <div>
        <p class="sub-title">Agent Roundtable</p>
        <h3 class="title">多角色 Agent 圆桌会话</h3>
        <p class="desc">
          选择 2-4 个 MBTI Agent，让它们基于用户问题、长期 Memory 和前序观点依次发言，最后生成综合建议。
        </p>
      </div>

      <button class="run-btn" :disabled="loading" @click="runRoundtable">
        {{ loading ? 'Agent 思考中...' : '开始圆桌会话' }}
      </button>
    </div>

    <div class="topic-box">
      <label>讨论主题</label>
      <textarea
        v-model="topic"
        placeholder="例如：我最近考试很焦虑，也总是拖延，不知道今晚该怎么安排复习"
      />
    </div>

    <div class="agent-select">
      <div class="select-head">
        <p>选择 Agent</p>
        <span>已选择 {{ selectedAgents.length }} / 4</span>
      </div>

      <div class="agent-grid">
        <button
          v-for="agent in agentOptions"
          :key="agent.code"
          class="agent-btn"
          :class="{ active: isSelected(agent.code) }"
          @click="toggleAgent(agent)"
        >
          <span class="emoji">{{ agent.icon }}</span>
          <span class="code">{{ agent.code }}</span>
          <span class="name">{{ agent.name }}</span>
        </button>
      </div>
    </div>

    <div v-if="error" class="error-box">
      {{ error }}
    </div>

    <div v-if="result" class="result-area">
      <div class="summary-card">
        <p class="sub-title">Final Summary</p>
        <h4>综合建议</h4>
        <p>{{ result.summary }}</p>
      </div>

      <div class="memory-card" v-if="result.memory">
        <p class="sub-title">Memory Used</p>
        <h4>本次使用的用户记忆</h4>
        <pre>{{ result.memory }}</pre>
      </div>

      <div class="reply-list">
        <article
          v-for="item in result.agents"
          :key="item.order"
          class="reply-item"
        >
          <div class="reply-head">
            <div>
              <p class="order">Agent {{ item.order }}</p>
              <h4>{{ item.agentCode }} {{ item.agentName }}</h4>
            </div>
          </div>

          <p class="reply-text">
            {{ item.reply }}
          </p>
        </article>
      </div>
    </div>

    <div v-else class="empty-state">
      先输入主题并选择至少 2 个 Agent，然后点击“开始圆桌会话”。
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../api'

type AgentOption = {
  code: string
  name: string
  icon: string
}

type AgentReply = {
  order: number
  agentCode: string
  agentName: string
  reply: string
}

type RoundtableResult = {
  topic: string
  memory: string
  agentCount: number
  agents: AgentReply[]
  summary: string
}

const loading = ref(false)
const error = ref('')
const result = ref<RoundtableResult | null>(null)

const topic = ref('我最近考试很焦虑，也总是拖延，不知道今晚该怎么安排复习')

const selectedAgents = ref<AgentOption[]>([
  {
    code: 'INTJ',
    name: '理性规划者',
    icon: '🧠'
  },
  {
    code: 'INFP',
    name: '温柔共情者',
    icon: '🌙'
  }
])

const agentOptions: AgentOption[] = [
  {
    code: 'INTJ',
    name: '理性规划者',
    icon: '🧠'
  },
  {
    code: 'INFP',
    name: '温柔共情者',
    icon: '🌙'
  },
  {
    code: 'ENFP',
    name: '行动鼓励者',
    icon: '☀️'
  },
  {
    code: 'ISTJ',
    name: '稳妥执行者',
    icon: '📘'
  },
  {
    code: 'INFJ',
    name: '深度洞察者',
    icon: '🌿'
  },
  {
    code: 'ENTP',
    name: '灵感辩手',
    icon: '⚡'
  },
  {
    code: 'ESFJ',
    name: '温暖照顾者',
    icon: '🤝'
  },
  {
    code: 'ENTJ',
    name: '目标指挥官',
    icon: '🚀'
  }
]

function isSelected(code: string) {
  return selectedAgents.value.some(item => item.code === code)
}

function toggleAgent(agent: AgentOption) {
  const exists = isSelected(agent.code)

  if (exists) {
    selectedAgents.value = selectedAgents.value.filter(item => item.code !== agent.code)
    return
  }

  if (selectedAgents.value.length >= 4) {
    alert('最多只能选择 4 个 Agent')
    return
  }

  selectedAgents.value.push(agent)
}

async function runRoundtable() {
  error.value = ''
  result.value = null

  if (!topic.value.trim()) {
    alert('请输入讨论主题')
    return
  }

  if (selectedAgents.value.length < 2) {
    alert('至少选择 2 个 Agent')
    return
  }

  loading.value = true

  try {
    const res: any = await api.runAgentRoundtable({
      topic: topic.value.trim(),
      agents: selectedAgents.value.map(item => ({
        code: item.code,
        name: item.name
      }))
    })

    const payload = getPayload(res)

    if (payload.message && !payload.agents) {
      error.value = payload.message
      return
    }

    result.value = {
      topic: payload.topic || topic.value,
      memory: payload.memory || '',
      agentCount: payload.agentCount || selectedAgents.value.length,
      agents: payload.agents || [],
      summary: payload.summary || '暂未生成总结。'
    }
  } catch (e: any) {
    console.error(e)
    error.value =
      e?.response?.data?.message ||
      e?.message ||
      'Agent 圆桌会话失败'
  } finally {
    loading.value = false
  }
}

function getPayload(res: any) {
  if (!res) {
    return {}
  }

  if (res.data?.data) {
    return res.data.data
  }

  if (res.data) {
    return res.data
  }

  return res
}
</script>

<style scoped>
.agent-card {
  margin-top: 24px;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.88);
  padding: 28px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.sub-title {
  font-size: 14px;
  color: #a39d92;
}

.title {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 700;
  color: #2f2f2f;
}

.desc {
  margin-top: 8px;
  max-width: 760px;
  font-size: 14px;
  line-height: 1.8;
  color: #8e887f;
}

.run-btn {
  border-radius: 999px;
  background: #c3ab89;
  padding: 13px 22px;
  font-size: 14px;
  color: white;
  white-space: nowrap;
}

.run-btn:disabled {
  opacity: 0.6;
}

.topic-box {
  margin-top: 22px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.topic-box label {
  font-size: 14px;
  color: #8f877d;
}

.topic-box textarea {
  min-height: 96px;
  border-radius: 22px;
  background: #fbf7f1;
  padding: 16px;
  outline: none;
  resize: vertical;
  line-height: 1.7;
  color: #4a4036;
}

.agent-select {
  margin-top: 22px;
}

.select-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #8f877d;
  font-size: 14px;
}

.agent-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.agent-btn {
  border-radius: 22px;
  background: #fbf7f1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  border: 2px solid transparent;
}

.agent-btn.active {
  background: #eef7f0;
  border-color: #afc9b8;
}

.emoji {
  font-size: 24px;
}

.code {
  font-size: 15px;
  font-weight: 700;
  color: #4a4036;
}

.name {
  font-size: 12px;
  color: #8f877d;
}

.error-box {
  margin-top: 18px;
  border-radius: 18px;
  background: #f8e2df;
  padding: 14px 16px;
  color: #b65f56;
}

.empty-state {
  margin-top: 20px;
  border-radius: 22px;
  background: #fbf7f1;
  padding: 22px;
  text-align: center;
  color: #9b968c;
}

.result-area {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card,
.memory-card,
.reply-item {
  border-radius: 24px;
  background: #fbf7f1;
  padding: 20px;
}

.summary-card h4,
.memory-card h4,
.reply-item h4 {
  margin-top: 6px;
  font-size: 17px;
  color: #4a4036;
}

.summary-card p:last-child {
  margin-top: 12px;
  line-height: 1.8;
  color: #555;
}

.memory-card pre {
  margin-top: 12px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #555;
  font-size: 13px;
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.reply-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.order {
  font-size: 12px;
  color: #a39d92;
}

.reply-text {
  margin-top: 12px;
  line-height: 1.9;
  color: #555;
  white-space: pre-wrap;
}

@media (max-width: 900px) {
  .head {
    flex-direction: column;
    align-items: flex-start;
  }

  .agent-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>