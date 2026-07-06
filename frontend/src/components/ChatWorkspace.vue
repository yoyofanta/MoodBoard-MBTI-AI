<template>
  <div class="chat-workspace">
    <!-- 左侧会话栏 -->
    <aside class="session-panel">
      <div class="session-header">
        <p class="eyebrow">{{ isDaily ? 'Daily Sessions' : 'Persona Sessions' }}</p>
        <h2>{{ isDaily ? '日常树洞' : `${personaName} 对话` }}</h2>
      </div>

      <button class="new-session-btn" @click="createNewSession">
        ＋ 开启新对话
      </button>

      <div class="session-list">
        <div
          v-for="session in sessions"
          :key="session.localId"
          class="session-swipe-wrap"
          :class="{ active: activeSessionId === session.localId }"
        >
          <button
            class="session-delete-btn"
            @click.stop="deleteSession(session.localId)"
          >
            删除
          </button>

          <button
            class="session-item"
            :class="{
              active: activeSessionId === session.localId,
              swiped: swipedSessionId === session.localId
            }"
            @click="handleSessionClick(session.localId)"
            @touchstart.passive="handleSessionTouchStart($event, session.localId)"
            @touchmove.passive="handleSessionTouchMove($event, session.localId)"
            @touchend="handleSessionTouchEnd"
          >
            <div class="session-title">
              {{ session.title }}
            </div>
            <p class="session-preview">
              {{ session.preview || '暂无内容' }}
            </p>
          </button>
        </div>
      </div>
    </aside>

    <!-- 右侧聊天区 -->
    <section class="chat-panel">
      <div class="chat-top">
        <div class="chat-title-box">
          <div class="bot-avatar">
            {{ personaAvatar }}
          </div>

          <div>
            <p class="eyebrow">
              {{ isDaily ? 'Daily Tree Hole' : personaCode }}
            </p>
            <h2>{{ isDaily ? '日常树洞' : personaName }}</h2>
            <p class="chat-subtitle">
              {{ isDaily ? '不用整理语言，慢慢说就好。' : personaDesc }}
            </p>
          </div>
        </div>

        <div class="chat-actions">
          <label class="save-check">
            <input v-model="saveRecord" type="checkbox" />
            保存聊天记录
          </label>

          <button class="small-btn" @click="clearCurrentSession">
            清空会话
          </button>
        </div>
      </div>

      <div ref="messageBoxRef" class="message-box">
        <div
          v-if="!activeSession || activeSession.messages.length === 0"
          class="empty-chat"
        >
          <div class="empty-icon">{{ personaAvatar }}</div>
          <h3>{{ isDaily ? '今天想说点什么？' : `和 ${personaName} 聊聊吧` }}</h3>
          <p>
            {{ isDaily ? '这里不会催你变好，只会认真听你说。' : '你可以直接说出最近的困扰、计划或情绪。' }}
          </p>
        </div>

        <div
          v-for="message in activeSession?.messages || []"
          :key="message.id"
          class="message-row"
          :class="message.role === 'user' ? 'message-user' : 'message-ai'"
        >
          <div
            v-if="message.role !== 'user'"
            class="message-avatar"
          >
            {{ personaAvatar }}
          </div>

          <div class="message-content">
            <div
              v-if="message.role !== 'user'"
              class="message-name"
            >
              {{ isDaily ? '日常树洞' : personaName }}
            </div>

            <div class="bubble">
              {{ message.content }}
            </div>
          </div>

          <div
            v-if="message.role === 'user'"
            class="message-avatar user-avatar"
          >
            我
          </div>
        </div>

        <div v-if="loading" class="message-row message-ai">
          <div class="message-avatar">{{ personaAvatar }}</div>
          <div class="message-content">
            <div class="message-name">
              {{ isDaily ? '日常树洞' : personaName }}
            </div>
            <div class="bubble loading-bubble">
              正在认真听你说……
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <textarea
          v-model="input"
          class="chat-input"
          rows="2"
          placeholder="写下你想说的话，按 Ctrl + Enter 发送"
          @keydown.ctrl.enter.prevent="sendMessage"
        />

        <button
          class="send-btn"
          :disabled="loading || !input.trim()"
          @click="sendMessage"
        >
          {{ loading ? '发送中' : '发送' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { api } from '../api'

type ChatMode = 'daily' | 'persona'

type ChatMessage = {
  id: number
  role: 'user' | 'assistant'
  content: string
  createdAt: string
}

type LocalChatSession = {
  localId: number
  backendSessionId: number | null
  title: string
  preview: string
  messages: ChatMessage[]
}


const sessions = ref<LocalChatSession[]>([])
const activeSessionId = ref<number | null>(null)
const input = ref('')
const loading = ref(false)
const saveRecord = ref(true)
const messageBoxRef = ref<HTMLElement | null>(null)

const swipedSessionId = ref<number | null>(null)
const touchStartX = ref(0)
const touchStartY = ref(0)

const isDaily = computed(() => props.mode === 'daily')
const text = input.value.trim()

const props = defineProps<{
  mode: 'daily' | 'persona'
  persona?: any
}>()

const personaCode = computed(() => {
  if (isDaily.value) {
    return 'DAILY'
  }

  return props.persona?.code || 'INFP'
})

const personaName = computed(() => {
  if (isDaily.value) {
    return '日常树洞'
  }

  return props.persona?.name || props.persona?.code || '人格'
})

const personaAvatar = computed(() => {
  if (isDaily.value) {
    return '🌿'
  }

  return props.persona?.avatar || props.persona?.emoji || props.persona?.icon || '🎭'
})

const personaDesc = computed(() => {
  if (isDaily.value) {
    return '不用整理语言，慢慢说就好。'
  }

  return props.persona?.description || props.persona?.slogan || '以这个人格视角陪你对话。'
})

const storageKey = computed(() => {
  if (isDaily.value) {
    return 'moodboard_daily_chat_sessions'
  }

  return `moodboard_persona_chat_sessions_${personaCode.value}`
})

const activeSession = computed(() => {
  return sessions.value.find(item => item.localId === activeSessionId.value) || null
})

onMounted(() => {
  loadLocalSessions()
})

watch(
  () => [props.mode, personaCode.value],
  () => {
    loadLocalSessions()
  }
)

function getDefaultSessionTitle() {
  if (isDaily.value) {
    return '新日常树洞'
  }

  return `${personaName.value} 对话`
}

function getDefaultSessionPreview() {
  return '暂无内容'
}

function createEmptySession(): LocalChatSession {
  const now = Date.now()

  return {
    localId: now,
    backendSessionId: null,
    title: getDefaultSessionTitle(),
    preview: getDefaultSessionPreview(),
    messages: []
  }
}

function loadLocalSessions() {
  try {
    const raw = localStorage.getItem(storageKey.value)

    if (raw) {
      const parsed = JSON.parse(raw)

      if (Array.isArray(parsed) && parsed.length > 0) {
        sessions.value = parsed
        activeSessionId.value = parsed[0].localId
        swipedSessionId.value = null
        return
      }
    }
  } catch (e) {
    console.error('读取本地会话失败：', e)
  }

  const firstSession = createEmptySession()
  sessions.value = [firstSession]
  activeSessionId.value = firstSession.localId
  saveLocalSessions()
}

function saveLocalSessions() {
  try {
    localStorage.setItem(storageKey.value, JSON.stringify(sessions.value))
  } catch (e) {
    console.error('保存本地会话失败：', e)
  }
}

function createNewSession() {
  const session = createEmptySession()
  sessions.value.unshift(session)
  activeSessionId.value = session.localId
  swipedSessionId.value = null
  saveLocalSessions()
  scrollToBottom()
}

function selectSession(sessionId: number) {
  activeSessionId.value = sessionId
  swipedSessionId.value = null
  scrollToBottom()
}

function handleSessionClick(sessionId: number) {
  if (swipedSessionId.value === sessionId) {
    swipedSessionId.value = null
    return
  }

  selectSession(sessionId)
}

function handleSessionTouchStart(e: TouchEvent, sessionId: number) {
  const touch = e.touches[0]
  touchStartX.value = touch.clientX
  touchStartY.value = touch.clientY
}

function handleSessionTouchMove(e: TouchEvent, sessionId: number) {
  const touch = e.touches[0]
  const deltaX = touch.clientX - touchStartX.value
  const deltaY = touch.clientY - touchStartY.value

  if (Math.abs(deltaX) < Math.abs(deltaY)) {
    return
  }

  if (deltaX < -40) {
    swipedSessionId.value = sessionId
  }

  if (deltaX > 40 && swipedSessionId.value === sessionId) {
    swipedSessionId.value = null
  }
}

function handleSessionTouchEnd() {
  touchStartX.value = 0
  touchStartY.value = 0
}

async function deleteSession(sessionId: number) {
  const session = sessions.value.find(item => item.localId === sessionId)

  if (!session) {
    return
  }

  const confirmed = window.confirm('确定删除这个会话吗？')

  if (!confirmed) {
    swipedSessionId.value = null
    return
  }

  try {
    if (session.backendSessionId) {
      await api.clearChatMessages(session.backendSessionId)
    }
  } catch (e) {
    console.error('删除会话时清理后端消息失败：', e)
  }

  const index = sessions.value.findIndex(item => item.localId === sessionId)
  const wasActive = activeSessionId.value === sessionId

  if (index !== -1) {
    sessions.value.splice(index, 1)
  }

  if (sessions.value.length === 0) {
    const newSession = createEmptySession()
    sessions.value = [newSession]
    activeSessionId.value = newSession.localId
    swipedSessionId.value = null
    saveLocalSessions()
    return
  }

  if (wasActive) {
    const nextSession = sessions.value[index] || sessions.value[index - 1] || sessions.value[0]
    activeSessionId.value = nextSession.localId
  }

  swipedSessionId.value = null
  saveLocalSessions()
}

async function clearCurrentSession() {
  const session = activeSession.value

  if (!session) {
    return
  }

  const confirmed = window.confirm('确定要清空当前会话吗？')

  if (!confirmed) {
    return
  }

  try {
    if (session.backendSessionId) {
      await api.clearChatMessages(session.backendSessionId)
    }
  } catch (e) {
    console.error('清空后端会话失败：', e)
  }

  session.messages = []
  session.title = getDefaultSessionTitle()
  session.preview = getDefaultSessionPreview()

  swipedSessionId.value = null
  saveLocalSessions()
}

async function sendMessage() {
  const content = input.value.trim()

  if (!content || loading.value) {
    return
  }

  let session = activeSession.value

  if (!session) {
    createNewSession()
    session = activeSession.value
  }

  if (!session) {
    return
  }

  const userMessage: ChatMessage = {
    id: Date.now(),
    role: 'user',
    content,
    createdAt: new Date().toISOString()
  }

  session.messages.push(userMessage)

  if (
    !session.title ||
    session.title === getDefaultSessionTitle() ||
    session.title === '新日常树洞' ||
    session.title.includes('对话')
  ) {
    session.title = content.length > 16 ? `${content.slice(0, 16)}...` : content
  }

  session.preview = content.length > 24 ? `${content.slice(0, 24)}...` : content

  input.value = ''
  loading.value = true
  saveLocalSessions()
  scrollToBottom()

  try {
    const currentPersona: any = props.persona || {}

const res: any = await api.sendMemoryChat({
  chatType: isDaily.value ? 'DAILY' : 'PERSONA',
  personaCode: isDaily.value ? 'DAILY' : currentPersona.code,
  personaName: isDaily.value ? '日常树洞' : currentPersona.name,
  content: content
})

    const data = getResponseData(res)

    const reply = extractReply(data, res)

    if (data && typeof data === 'object') {
      const newBackendSessionId =
        data.sessionId ||
        data.chatSessionId ||
        data.id ||
        data.backendSessionId

      if (newBackendSessionId && !session.backendSessionId) {
        session.backendSessionId = Number(newBackendSessionId)
      }
    }

    const aiMessage: ChatMessage = {
      id: Date.now() + 1,
      role: 'assistant',
      content: reply || '我在听，你可以继续说。',
      createdAt: new Date().toISOString()
    }

    session.messages.push(aiMessage)
    session.preview =
      aiMessage.content.length > 24
        ? `${aiMessage.content.slice(0, 24)}...`
        : aiMessage.content
  } catch (e) {
    console.error('聊天接口请求失败：', e)

    const errorMessage: ChatMessage = {
      id: Date.now() + 2,
      role: 'assistant',
      content: '刚刚回复失败了，请检查后端或 AI 配置。',
      createdAt: new Date().toISOString()
    }

    session.messages.push(errorMessage)
    session.preview = errorMessage.content
  } finally {
    loading.value = false
    saveLocalSessions()
    scrollToBottom()
  }
}

function getResponseData(res: any) {
  if (!res) {
    return null
  }

  if (res.data !== undefined) {
    return res.data
  }

  return res
}

function extractReply(data: any, raw: any) {
  if (typeof data === 'string') {
    return data
  }

  if (data && typeof data === 'object') {
    return (
      data.reply ||
      data.content ||
      data.answer ||
      data.message ||
      data.text ||
      data.aiReply ||
      ''
    )
  }

  if (raw && typeof raw === 'object') {
    return (
      raw.reply ||
      raw.content ||
      raw.answer ||
      raw.message ||
      raw.text ||
      raw.aiReply ||
      ''
    )
  }

  return ''
}

function scrollToBottom() {
  nextTick(() => {
    if (messageBoxRef.value) {
      messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.chat-workspace {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 28px;
  width: 100%;
  min-height: 720px;
}

.session-panel,
.chat-panel {
  background: rgba(255, 255, 255, 0.86);
  border-radius: 34px;
  box-shadow: 0 18px 48px rgba(168, 151, 130, 0.16);
  border: 1px solid rgba(236, 228, 216, 0.72);
}

.session-panel {
  padding: 28px 24px;
  overflow: hidden;
}

.session-header {
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #aaa095;
  font-size: 16px;
  letter-spacing: 0.02em;
}

.session-header h2,
.chat-title-box h2 {
  margin: 0;
  color: #2f3135;
  font-size: 30px;
  line-height: 1.2;
}

.new-session-btn {
  width: 100%;
  border: none;
  border-radius: 28px;
  padding: 18px 20px;
  background: #eadac5;
  color: #6e5944;
  font-size: 17px;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.new-session-btn:hover {
  background: #e3cfb4;
  transform: translateY(-1px);
}

.session-list {
  margin-top: 24px;
  max-height: 560px;
  overflow-y: auto;
  padding-right: 4px;
}

.session-list::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-thumb {
  background: rgba(196, 174, 143, 0.45);
  border-radius: 999px;
}

.session-swipe-wrap {
  position: relative;
  overflow: hidden;
  border-radius: 24px;
  margin-bottom: 14px;
  background: #d96c6c;
}

.session-delete-btn {
  position: absolute;
  top: 0;
  right: 0;
  width: 82px;
  height: 100%;
  border: none;
  border-radius: 0 24px 24px 0;
  background: #d96c6c;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
  z-index: 1;
}

.session-item {
  position: relative;
  z-index: 2;
  display: block;
  width: 100%;
  min-height: 86px;
  border: none;
  border-radius: 24px;
  padding: 20px;
  background: #f2e6d5;
  cursor: pointer;
  text-align: left;
  transition: transform 0.22s ease, background 0.22s ease, box-shadow 0.22s ease;
}

.session-swipe-wrap:hover .session-item {
  transform: translateX(-82px);
}

.session-item.swiped {
  transform: translateX(-82px);
}

.session-item.active {
  background: #eadac5;
  box-shadow: inset 0 0 0 1px rgba(189, 162, 123, 0.25);
}

.session-title {
  font-size: 17px;
  font-weight: 600;
  color: #2f2f2f;
  line-height: 1.4;
}

.session-preview {
  margin: 8px 0 0;
  font-size: 14px;
  color: #9b9286;
  line-height: 1.4;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

.chat-top {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 32px 22px;
  border-bottom: 1px solid rgba(230, 220, 205, 0.7);
}

.chat-title-box {
  display: flex;
  align-items: center;
  gap: 18px;
  min-width: 0;
}

.bot-avatar {
  display: grid;
  place-items: center;
  width: 64px;
  height: 64px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #e9f5ee;
  font-size: 30px;
}

.chat-subtitle {
  margin: 8px 0 0;
  color: #9b9286;
  font-size: 15px;
}

.chat-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.save-check {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #7f756a;
  font-size: 14px;
  white-space: nowrap;
}

.save-check input {
  accent-color: #bda27b;
}

.small-btn {
  border: none;
  border-radius: 999px;
  padding: 10px 16px;
  background: #f1e5d4;
  color: #7b6046;
  cursor: pointer;
}

.small-btn:hover {
  background: #e5d2b7;
}

.message-box {
  flex: 1;
  padding: 30px 32px;
  overflow-y: auto;
  background: linear-gradient(180deg, rgba(250, 248, 243, 0.6), rgba(255, 255, 255, 0.68));
}

.empty-chat {
  height: 100%;
  min-height: 360px;
  display: grid;
  place-items: center;
  text-align: center;
  align-content: center;
  color: #8f8478;
}

.empty-icon {
  display: grid;
  place-items: center;
  width: 92px;
  height: 92px;
  margin: 0 auto 18px;
  border-radius: 50%;
  background: #e9f5ee;
  font-size: 42px;
}

.empty-chat h3 {
  margin: 0 0 10px;
  font-size: 24px;
  color: #3b3b3b;
}

.empty-chat p {
  margin: 0;
  max-width: 360px;
  line-height: 1.7;
}

.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-ai {
  justify-content: flex-start;
}

.message-user {
  justify-content: flex-end;
}

.message-avatar {
  display: grid;
  place-items: center;
  width: 46px;
  height: 46px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: #e9f5ee;
  font-size: 22px;
}

.user-avatar {
  background: #eadac5;
  color: #6e5944;
  font-size: 15px;
  font-weight: 700;
}

.message-content {
  max-width: min(680px, 72%);
}

.message-user .message-content {
  order: 1;
}

.message-user .user-avatar {
  order: 2;
}

.message-name {
  margin: 0 0 6px 4px;
  color: #9f958b;
  font-size: 13px;
}

.bubble {
  padding: 16px 18px;
  border-radius: 22px;
  font-size: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 10px 24px rgba(173, 156, 132, 0.1);
}

.message-ai .bubble {
  background: #fff;
  color: #4d4d4d;
  border-top-left-radius: 8px;
}

.message-user .bubble {
  background: #eadac5;
  color: #5f4b38;
  border-top-right-radius: 8px;
}

.loading-bubble {
  color: #9b9286;
}

.input-area {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 110px;
  gap: 14px;
  padding: 20px 32px 28px;
  border-top: 1px solid rgba(230, 220, 205, 0.7);
}

.chat-input {
  width: 100%;
  resize: none;
  border: none;
  outline: none;
  border-radius: 24px;
  padding: 16px 18px;
  background: #f6efe5;
  color: #333;
  font-size: 16px;
  line-height: 1.6;
}

.chat-input::placeholder {
  color: #aaa095;
}

.send-btn {
  border: none;
  border-radius: 24px;
  background: #bda27b;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: transform 0.2s ease, background 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  background: #ab8f68;
  transform: translateY(-1px);
}

.send-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

@media (max-width: 960px) {
  .chat-workspace {
    grid-template-columns: 1fr;
  }

  .session-panel {
    min-height: auto;
  }

  .session-list {
    max-height: 260px;
  }

  .chat-top {
    flex-direction: column;
  }

  .chat-actions {
    justify-content: flex-start;
  }

  .input-area {
    grid-template-columns: 1fr;
  }

  .send-btn {
    min-height: 52px;
  }

  .message-content {
    max-width: 78%;
  }
}
</style>