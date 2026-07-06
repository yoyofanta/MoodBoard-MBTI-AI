<template>
  <div class="battle-wrapper">
    <!-- 第 1 页：只选择人格 -->
    <section v-if="page === 'setup'" class="setup-page">
      <div class="setup-hero">
        <p class="sub-title">Multi-Agent Roundtable</p>
        <h2>选择参与对战的人格</h2>
        <p>
          选择 2-4 个 MBTI Agent。进入下一页后，你可以像聊天一样发起问题，
          每个 Agent 会依次发言，最后生成综合总结。
        </p>
      </div>

      <div class="selected-bar">
        <span>已选择 {{ selectedAgents.length }} / 4</span>

        <div class="selected-tags">
          <span
            v-for="agent in selectedAgents"
            :key="agent.code"
            class="selected-tag"
          >
            {{ agent.icon }} {{ agent.code }} {{ agent.name }}
          </span>
        </div>
      </div>

      <div class="setup-grid">
        <button
          v-for="agent in agentOptions"
          :key="agent.code"
          class="setup-agent-card"
          :class="{ active: isSetupSelected(agent.code) }"
          @click="toggleSetupAgent(agent)"
        >
          <span class="agent-icon">{{ agent.icon }}</span>
          <span class="agent-code">{{ agent.code }}</span>
          <span class="agent-name">{{ agent.name }}</span>
          <span class="agent-desc">{{ agent.desc }}</span>
        </button>
      </div>

      <div class="setup-actions">
        <button class="secondary-btn" @click="openHistory">
          查看历史会话
        </button>

        <button
          class="start-btn"
          :disabled="selectedAgents.length < 2"
          @click="startBattle"
        >
          开始对战
        </button>
      </div>
    </section>

    <!-- 第 2 页：微信式会话界面 -->
    <section v-else class="chat-page">
      <aside class="session-sidebar">
        <div class="sidebar-head">
          <div>
            <p class="sub-title">Battle Sessions</p>
            <h3>人格对战</h3>
          </div>

          <button class="new-btn" @click="backToSetup">
            ＋
          </button>
        </div>

        <div class="session-list">
          <button
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: session.id === activeSessionId }"
            @click="activeSessionId = session.id"
          >
            <div class="session-main">
              <p class="session-title">
                {{ session.title || '新对战' }}
              </p>

              <p class="session-preview">
                {{ session.preview || '还没有消息' }}
              </p>

              <div class="mini-agent-row">
                <span
                  v-for="agent in session.agents"
                  :key="agent.code"
                  class="mini-agent"
                >
                  {{ agent.code }}
                </span>
              </div>
            </div>

            <button class="delete-btn" @click.stop="deleteSession(session.id)">
              ×
            </button>
          </button>
        </div>
      </aside>

      <main v-if="activeSession" class="chat-panel">
        <header class="chat-head">
          <div>
            <button class="back-link" @click="backToSetup">
              ← 重新选择人格
            </button>

            <h2>{{ activeSession.title || '新对战' }}</h2>

            <div class="agent-tag-row">
              <span
                v-for="agent in activeSession.agents"
                :key="agent.code"
                class="agent-tag"
              >
                {{ agent.icon }} {{ agent.code }} {{ agent.name }}
              </span>
            </div>
          </div>

          <button class="clear-btn" @click="clearCurrentMessages">
            清空会话
          </button>
        </header>

        <section ref="messageAreaRef" class="message-area">
          <div v-if="activeSession.messages.length === 0" class="empty-chat">
            <div class="empty-icon">⚔️</div>
            <h3>开始一场多角色 Agent 圆桌</h3>
            <p>
              在下方输入你的困扰。系统会让已选人格依次发言，并给出综合总结。
            </p>
          </div>

          <div
            v-for="msg in activeSession.messages"
            :key="msg.id"
            class="message-row"
            :class="msg.role"
          >
            <div v-if="msg.role === 'user'" class="user-message">
              <div class="bubble user-bubble">
                {{ msg.content }}
              </div>
            </div>

            <div v-else-if="msg.role === 'agent'" class="agent-message">
              <div class="avatar">
                {{ getAgentIcon(msg.agentCode) }}
              </div>

              <div class="agent-body">
                <div class="agent-name-line">
                  <strong>{{ msg.agentCode }} {{ msg.agentName }}</strong>
                  <span>Agent</span>
                </div>

                <div class="bubble agent-bubble">
                  {{ msg.content }}
                </div>
              </div>
            </div>

            <div v-else-if="msg.role === 'summary'" class="summary-message">
              <div class="summary-label">
                🧩 综合总结
              </div>

              <div class="bubble summary-bubble">
                {{ msg.content }}
              </div>
            </div>

            <div v-else class="system-message">
              {{ msg.content }}
            </div>
          </div>

          <div v-if="loading" class="loading-row">
            <span></span>
            <span></span>
            <span></span>
            Agent 正在依次思考中...
          </div>
        </section>

        <footer class="composer">
          <textarea
            v-model="input"
            placeholder="例如：我最近考试很焦虑，也总是拖延，不知道今晚该怎么安排复习"
            :disabled="loading"
            @keydown.enter.exact.prevent="sendMessage"
          />

          <button
            class="send-btn"
            :disabled="loading || !input.trim()"
            @click="sendMessage"
          >
            {{ loading ? '发送中...' : '发送' }}
          </button>
        </footer>
      </main>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { api } from '../api'

type PageType = 'setup' | 'chat'

type AgentOption = {
  code: string
  name: string
  icon: string
  desc: string
}

type BattleMessage = {
  id: string
  role: 'user' | 'agent' | 'summary' | 'system'
  content: string
  agentCode?: string
  agentName?: string
  createdAt: string
}

type BattleSession = {
  id: string
  title: string
  preview: string
  agents: AgentOption[]
  messages: BattleMessage[]
  createdAt: string
  updatedAt: string
}

const STORAGE_KEY = 'moodboard_agent_battle_sessions'

const agentOptions: AgentOption[] = [
  {
    code: 'INTJ',
    name: '理性规划者',
    icon: '🧠',
    desc: '拆解问题、制定计划'
  },
  {
    code: 'INTP',
    name: '逻辑分析师',
    icon: '🔍',
    desc: '分析原因、追问本质'
  },
  {
    code: 'ENTJ',
    name: '目标指挥官',
    icon: '🚀',
    desc: '目标导向、推动执行'
  },
  {
    code: 'ENTP',
    name: '灵感辩手',
    icon: '⚡',
    desc: '提出新角度和反思'
  },

  {
    code: 'INFJ',
    name: '深度洞察者',
    icon: '🌿',
    desc: '看见深层原因'
  },
  {
    code: 'INFP',
    name: '温柔共情者',
    icon: '🌙',
    desc: '理解感受、接住情绪'
  },
  {
    code: 'ENFJ',
    name: '温暖引导者',
    icon: '🕯️',
    desc: '陪伴支持、理清方向'
  },
  {
    code: 'ENFP',
    name: '行动鼓励者',
    icon: '☀️',
    desc: '点燃动力、提供鼓励'
  },

  {
    code: 'ISTJ',
    name: '稳妥执行者',
    icon: '📘',
    desc: '重视步骤、稳定推进'
  },
  {
    code: 'ISFJ',
    name: '细心守护者',
    icon: '🧸',
    desc: '照顾细节、温和支持'
  },
  {
    code: 'ESTJ',
    name: '秩序管理者',
    icon: '📋',
    desc: '梳理任务、提高效率'
  },
  {
    code: 'ESFJ',
    name: '温暖照顾者',
    icon: '🤝',
    desc: '关注支持和关系'
  },

  {
    code: 'ISTP',
    name: '冷静解决者',
    icon: '🛠️',
    desc: '直接处理、减少内耗'
  },
  {
    code: 'ISFP',
    name: '柔和陪伴者',
    icon: '🎨',
    desc: '尊重感受、慢慢调整'
  },
  {
    code: 'ESTP',
    name: '现实行动派',
    icon: '🏃',
    desc: '马上行动、快速试错'
  },
  {
    code: 'ESFP',
    name: '快乐激励者',
    icon: '🎈',
    desc: '缓解压力、找回能量'
  }
]

const page = ref<PageType>('setup')

const selectedAgents = ref<AgentOption[]>([
  agentOptions[0],
  agentOptions[1]
])

const sessions = ref<BattleSession[]>([])
const activeSessionId = ref('')
const input = ref('')
const loading = ref(false)
const messageAreaRef = ref<HTMLElement | null>(null)

const activeSession = computed(() => {
  return sessions.value.find(item => item.id === activeSessionId.value) || null
})

onMounted(() => {
  loadSessions()
})

watch(
  sessions,
  () => {
    saveSessions()
  },
  {
    deep: true
  }
)

function isSetupSelected(code: string) {
  return selectedAgents.value.some(item => item.code === code)
}

function toggleSetupAgent(agent: AgentOption) {
  const exists = isSetupSelected(agent.code)

  if (exists) {
    if (selectedAgents.value.length <= 2) {
      alert('至少需要选择 2 个人格')
      return
    }

    selectedAgents.value = selectedAgents.value.filter(item => item.code !== agent.code)
    return
  }

  if (selectedAgents.value.length >= 4) {
    alert('最多只能选择 4 个人格')
    return
  }

  selectedAgents.value.push(agent)
}

function startBattle() {
  if (selectedAgents.value.length < 2) {
    alert('至少选择 2 个人格')
    return
  }

  const now = new Date().toISOString()

  const session: BattleSession = {
    id: createId(),
    title: '新对战',
    preview: '还没有消息',
    agents: [...selectedAgents.value],
    messages: [],
    createdAt: now,
    updatedAt: now
  }

  sessions.value.unshift(session)
  activeSessionId.value = session.id
  page.value = 'chat'

  saveSessions()
}

function openHistory() {
  if (sessions.value.length === 0) {
    alert('还没有历史会话，请先开始一场对战')
    return
  }

  activeSessionId.value = sessions.value[0].id
  page.value = 'chat'
}

function backToSetup() {
  page.value = 'setup'
}

function deleteSession(id: string) {
  if (!confirm('确定要删除这个人格对战会话吗？')) {
    return
  }

  sessions.value = sessions.value.filter(item => item.id !== id)

  if (activeSessionId.value === id) {
    activeSessionId.value = sessions.value[0]?.id || ''
  }

  if (sessions.value.length === 0) {
    page.value = 'setup'
  }
}

function clearCurrentMessages() {
  const session = activeSession.value

  if (!session) {
    return
  }

  if (!confirm('确定要清空当前会话吗？')) {
    return
  }

  session.messages = []
  session.preview = '会话已清空'
  session.updatedAt = new Date().toISOString()
}

async function sendMessage() {
  const session = activeSession.value
  const content = input.value.trim()

  if (!session || !content || loading.value) {
    return
  }

  const now = new Date().toISOString()

  session.messages.push({
    id: createId(),
    role: 'user',
    content,
    createdAt: now
  })

  if (!session.title || session.title === '新对战') {
    session.title = content.length > 18 ? `${content.slice(0, 18)}...` : content
  }

  session.preview = content
  session.updatedAt = now

  input.value = ''
  loading.value = true

  await scrollToBottom()

  try {
    const topic = buildRoundtableTopic(content, session)

    const res: any = await api.runAgentRoundtable({
      topic,
      agents: session.agents.map(item => ({
        code: item.code,
        name: item.name
      }))
    })

    const payload = getPayload(res)

    if (payload.message && !payload.agents) {
      session.messages.push({
        id: createId(),
        role: 'system',
        content: payload.message,
        createdAt: new Date().toISOString()
      })

      return
    }

    const replies = Array.isArray(payload.agents) ? payload.agents : []

    replies.forEach((item: any) => {
      session.messages.push({
        id: createId(),
        role: 'agent',
        agentCode: item.agentCode || item.code || '',
        agentName: item.agentName || item.name || '',
        content: item.reply || '这个 Agent 暂时没有生成有效回复。',
        createdAt: new Date().toISOString()
      })
    })

    session.messages.push({
      id: createId(),
      role: 'summary',
      content: payload.summary || '本轮暂未生成总结。',
      createdAt: new Date().toISOString()
    })

    session.preview = payload.summary || replies[0]?.reply || content
    session.updatedAt = new Date().toISOString()
  } catch (e: any) {
    console.error(e)

    session.messages.push({
      id: createId(),
      role: 'system',
      content:
        e?.response?.data?.message ||
        e?.message ||
        'Agent 圆桌会话失败，请稍后重试。',
      createdAt: new Date().toISOString()
    })
  } finally {
    loading.value = false
    saveSessions()
    await scrollToBottom()
  }
}

function buildRoundtableTopic(content: string, session: BattleSession) {
  const recentMessages = session.messages
    .slice(-8)
    .map(msg => {
      if (msg.role === 'user') {
        return `用户：${msg.content}`
      }

      if (msg.role === 'agent') {
        return `${msg.agentCode} ${msg.agentName}：${msg.content}`
      }

      if (msg.role === 'summary') {
        return `综合总结：${msg.content}`
      }

      return `系统：${msg.content}`
    })
    .join('\n')

  return `【本轮用户输入】\n${content}\n\n【当前会话近期内容】\n${recentMessages}`
}

function getPayload(res: any) {
  if (!res) {
    return {}
  }

  if (res.data?.data) {
    return res.data.data
  }

  if (res.code === 200 && res.data) {
    return res.data
  }

  if (res.data?.agents || res.data?.summary) {
    return res.data
  }

  if (res.agents || res.summary || res.message) {
    return res
  }

  return res
}

function getAgentIcon(code?: string) {
  const found = agentOptions.find(item => item.code === code)

  return found?.icon || '🤖'
}

async function scrollToBottom() {
  await nextTick()

  const el = messageAreaRef.value

  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

function loadSessions() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)

    if (!raw) {
      return
    }

    const parsed = JSON.parse(raw)

    if (Array.isArray(parsed)) {
      sessions.value = parsed
    }
  } catch (e) {
    console.error('读取人格对战会话失败', e)
  }
}

function saveSessions() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(sessions.value))
}

function createId() {
  return `${Date.now()}_${Math.random().toString(16).slice(2)}`
}
</script>

<style scoped>
.battle-wrapper {
  width: 100%;
}

.setup-page {
  height: calc(100vh - 190px);
  min-height: 560px;
  max-height: calc(100vh - 190px);
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.9);
  padding: 24px 28px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sub-title {
  font-size: 14px;
  color: #a39d92;
}

.setup-hero h2 {
  margin-top: 6px;
  font-size: 28px;
  font-weight: 800;
  color: #2f2f2f;
}

.setup-hero p {
  margin-top: 8px;
  max-width: 820px;
  font-size: 14px;
  line-height: 1.6;
  color: #8e887f;
}
.selected-bar {
  margin-top: 16px;
  border-radius: 22px;
  background: #fbf7f1;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #7b7166;
  flex-shrink: 0;
}
.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selected-tag {
  border-radius: 999px;
  background: white;
  padding: 6px 11px;
  font-size: 12px;
}
.setup-grid {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 6px;
  padding-bottom: 6px;
}
.setup-agent-card {
  min-height: 118px;
  max-height: 140px;
  border-radius: 24px;
  background: #fbf7f1;
  border: 2px solid transparent;
  padding: 10px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
}

.setup-agent-card.active {
  background: #eef7f0;
  border-color: #afc9b8;
}

.agent-icon {
  font-size: 24px;
}

.agent-code {
  margin-top: 4px;
  font-size: 18px;
  font-weight: 800;
  color: #3f3830;
}

.agent-name {
  font-size: 13px;
  color: #756c63;
}

.agent-desc {
  margin-top: 4px;
  font-size: 12px;
  color: #a39d92;
}
.setup-actions {
  flex-shrink: 0;
  margin-top: 16px;
  padding-top: 0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.secondary-btn,
.start-btn {
  border-radius: 999px;
  padding: 11px 22px;
  font-size: 14px;
}
.secondary-btn {
  background: #fbf7f1;
  color: #7b7166;
}

.start-btn {
  background: #c3ab89;
  color: white;
}

.start-btn:disabled {
  opacity: 0.55;
}

.chat-page {
  height: calc(100vh - 170px);
  min-height: 650px;
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 20px;
  overflow: hidden;
}

.session-sidebar {
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.sidebar-head h3 {
  margin-top: 5px;
  font-size: 22px;
  font-weight: 800;
  color: #2f2f2f;
}

.new-btn {
  width: 42px;
  height: 42px;
  border-radius: 999px;
  background: #c3ab89;
  color: white;
  font-size: 24px;
}

.session-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  padding-right: 4px;
}

.session-item {
  position: relative;
  border-radius: 22px;
  background: #fbf7f1;
  padding: 14px 34px 14px 14px;
  text-align: left;
  border: 2px solid transparent;
}

.session-item.active {
  background: #eef7f0;
  border-color: #afc9b8;
}

.session-title {
  font-size: 15px;
  font-weight: 800;
  color: #4a4036;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.session-preview {
  margin-top: 6px;
  font-size: 12px;
  color: #9b968c;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.mini-agent-row {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.mini-agent {
  border-radius: 999px;
  background: white;
  padding: 3px 7px;
  font-size: 11px;
  color: #7b7166;
}

.delete-btn {
  position: absolute;
  top: 9px;
  right: 9px;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  color: #b65f56;
  background: rgba(255, 255, 255, 0.85);
}

.chat-panel {
  height: 100%;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.chat-head {
  padding: 22px 26px;
  border-bottom: 1px solid rgba(195, 171, 137, 0.18);
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
}

.back-link {
  margin-bottom: 8px;
  color: #8f877d;
  font-size: 14px;
}

.chat-head h2 {
  font-size: 25px;
  font-weight: 800;
  color: #2f2f2f;
}

.agent-tag-row {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.agent-tag {
  border-radius: 999px;
  background: #fbf7f1;
  padding: 6px 11px;
  font-size: 12px;
  color: #7b7166;
}

.clear-btn {
  border-radius: 999px;
  background: #fbf7f1;
  padding: 11px 18px;
  color: #7b7166;
  white-space: nowrap;
}

.message-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px 26px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-chat {
  margin: auto;
  text-align: center;
  max-width: 460px;
  color: #8f877d;
}

.empty-icon {
  font-size: 48px;
}

.empty-chat h3 {
  margin-top: 14px;
  font-size: 22px;
  font-weight: 800;
  color: #4a4036;
}

.empty-chat p {
  margin-top: 10px;
  line-height: 1.8;
}

.message-row.user {
  display: flex;
  justify-content: flex-end;
}

.bubble {
  max-width: 760px;
  border-radius: 22px;
  padding: 14px 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 15px;
}

.user-bubble {
  background: #c3ab89;
  color: white;
  border-bottom-right-radius: 8px;
}

.agent-message {
  display: flex;
  gap: 12px;
  max-width: 850px;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 16px;
  background: #fbf7f1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agent-name-line {
  display: flex;
  align-items: center;
  gap: 8px;
}

.agent-name-line strong {
  color: #4a4036;
}

.agent-name-line span {
  font-size: 12px;
  color: #a39d92;
}

.agent-bubble {
  margin-top: 7px;
  background: #fbf7f1;
  color: #555;
  border-bottom-left-radius: 8px;
}

.summary-message {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-label {
  border-radius: 999px;
  background: #eef7f0;
  color: #5f7d67;
  padding: 7px 14px;
  font-size: 13px;
  font-weight: 800;
}

.summary-bubble {
  margin-top: 10px;
  width: min(840px, 100%);
  background: #eef7f0;
  color: #4f5f53;
}

.system-message {
  align-self: center;
  border-radius: 999px;
  background: #f8e2df;
  color: #b65f56;
  padding: 8px 14px;
  font-size: 13px;
}

.loading-row {
  align-self: flex-start;
  border-radius: 999px;
  background: #fbf7f1;
  padding: 10px 16px;
  color: #8f877d;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.loading-row span {
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #c3ab89;
  animation: bounce 1s infinite ease-in-out;
}

.loading-row span:nth-child(2) {
  animation-delay: 0.15s;
}

.loading-row span:nth-child(3) {
  animation-delay: 0.3s;
}

.composer {
  flex-shrink: 0;
  padding: 16px 26px 20px;
  border-top: 1px solid rgba(195, 171, 137, 0.18);
  display: flex;
  gap: 12px;
  background: rgba(255, 255, 255, 0.96);
}

.composer textarea {
  flex: 1;
  min-height: 52px;
  max-height: 100px;
  border-radius: 22px;
  background: #fbf7f1;
  padding: 14px 18px;
  outline: none;
  resize: none;
  line-height: 1.6;
  color: #4a4036;
}

.send-btn {
  align-self: flex-end;
  border-radius: 999px;
  background: #c3ab89;
  padding: 14px 24px;
  color: white;
}

.send-btn:disabled {
  opacity: 0.6;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }

  40% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

@media (max-width: 960px) {
  .setup-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chat-page {
    height: auto;
    min-height: auto;
    grid-template-columns: 1fr;
  }

  .session-sidebar {
    max-height: 260px;
  }

  .chat-panel {
    min-height: 700px;
  }

  .setup-grid::-webkit-scrollbar {
  width: 6px;
}

.setup-grid::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(195, 171, 137, 0.45);
}

.setup-grid::-webkit-scrollbar-track {
  background: transparent;
}
}
</style>