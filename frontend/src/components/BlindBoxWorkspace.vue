<template>
  <div class="blind-workspace">
    <!-- 左侧会话栏 -->
    <aside class="sidebar">
      <div class="side-head">
        <div>
          <p class="side-subtitle">Blind Box Sessions</p>
          <h3 class="side-title">心灵盲盒</h3>
        </div>
      </div>

      <button class="new-btn" :disabled="loading" @click="drawNewBlindBox">
        ＋ 抽一个心灵盲盒
      </button>

      <div class="session-list">
        <button
          v-for="session in sessions"
          :key="session.id"
          class="session-item"
          :class="{ active: activeSessionId === session.id }"
          @click="selectSession(session.id)"
        >
          <p class="session-title">
            {{ session.title }}
          </p>

          <p class="session-preview">
            {{ session.preview || '点击继续对话' }}
          </p>
        </button>
      </div>
    </aside>

    <!-- 右侧内容 -->
    <section class="content">
      <template v-if="activeSession">
        <div class="content-header">
          <div>
            <p class="side-subtitle">Soul Blind Box</p>

            <h2 class="page-title">
              {{ activeSession.personaCode }} · {{ activeSession.personaName }}
            </h2>

            <p class="topic-line">
              💌 {{ activeSession.topic }}
            </p>
          </div>

          <div class="top-actions">
            <button class="soft-btn" @click="drawNewBlindBox">
              重新抽取
            </button>

            <button class="end-btn" @click="saveSessionAsDiary">
              保存为日记
            </button>
          </div>
        </div>

        <div class="chat-card">
          <div ref="messageBoxRef" class="messages">
            <div
              v-for="msg in activeSession.messages"
              :key="msg.id"
              class="message-row"
              :class="msg.role === 'user' ? 'user-row' : 'ai-row'"
            >
              <div
                v-if="msg.role !== 'user'"
                class="avatar"
                :style="{ backgroundColor: activeSession.softTheme }"
              >
                {{ activeSession.icon }}
              </div>

              <div
                class="bubble"
                :class="msg.role === 'user' ? 'bubble-user' : 'bubble-ai'"
              >
                <p class="bubble-name">
                  {{ msg.role === 'user' ? '我' : activeSession.personaCode }}
                </p>

                <p class="bubble-content">
                  {{ msg.content }}
                </p>
              </div>

              <div v-if="msg.role === 'user'" class="avatar user-avatar">
                我
              </div>
            </div>

            <div v-if="loading" class="message-row ai-row">
              <div class="avatar">💌</div>
              <div class="bubble bubble-ai">
                <p class="bubble-content loading-text">
                  心灵盲盒正在认真回应……
                </p>
              </div>
            </div>

            <div v-if="activeSession.finished" class="finish-card">
              <p class="finish-title">这一轮心灵盲盒结束啦</p>
              <p class="finish-desc">
                你可以保存为日记，也可以重新抽取一个新的盲盒。
              </p>
            </div>
          </div>

          <div v-if="!activeSession.finished" class="input-bar">
            <input
              v-model="input"
              class="chat-input"
              placeholder="继续说说你的想法……"
              @keydown.enter="sendReply"
            />

            <button class="send-btn" :disabled="loading" @click="sendReply">
              发送
            </button>
          </div>

          <div v-else class="ended-actions">
            <button class="primary-btn flex-one" @click="saveSessionAsDiary">
              保存为日记
            </button>

            <button class="secondary-btn flex-one" @click="drawNewBlindBox">
              再抽一个
            </button>
          </div>
        </div>
      </template>

      <div v-else class="empty-panel">
        <div class="empty-icon">💌</div>
        <p class="empty-title">还没有心灵盲盒</p>
        <p class="empty-desc">
          点击左侧“抽一个心灵盲盒”，系统会随机给你一个人格和一个深度话题。
        </p>

        <button class="primary-btn" :disabled="loading" @click="drawNewBlindBox">
          抽一个心灵盲盒
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { officialPersonas } from '../data/personas'
import { api } from '../api'

type BlindMessage = {
  id: number
  role: 'user' | 'assistant'
  content: string
}

type BlindSession = {
  id: number
  blindBoxId: number | string
  personaCode: string
  personaName: string
  icon: string
  theme: string
  softTheme: string
  topic: string
  title: string
  preview: string
  roundIndex: number
  finished: boolean
  messages: BlindMessage[]
}

const sessions = ref<BlindSession[]>([])
const activeSessionId = ref<number | null>(null)

const input = ref('')
const loading = ref(false)
const messageBoxRef = ref<HTMLElement | null>(null)

const activeSession = computed(() => {
  return sessions.value.find(item => item.id === activeSessionId.value) || null
})

function selectSession(id: number) {
  activeSessionId.value = id
  input.value = ''

  nextTick(scrollToBottom)
}

function getPersonaInfo(code: string) {
  const found: any = officialPersonas.find(p => p.code === code) || officialPersonas[0]

  return {
    code: found.code,
    name: found.name,
    icon: found.icon || found.emoji || found.avatar || '💌',
    theme: found.theme || '#D8C3A5',
    softTheme: found.softTheme || '#F8EAEA'
  }
}

async function drawNewBlindBox() {
  loading.value = true

  try {
    const drawRes: any = await api.drawBlindBox()
    const box = drawRes.data || {}

    const personaCode = box.persona || box.personaCode || 'INFJ'
    const topic = box.topic || '你最近最想被谁理解？'

    const persona = getPersonaInfo(personaCode)

    const startRes: any = await api.startBlindBox({
      persona: persona.code,
      topic,
      ephemeral: false
    })

    const data = startRes.data || {}

    const session: BlindSession = {
      id: Date.now(),
      blindBoxId: data.blindBoxId || Date.now(),
      personaCode: persona.code,
      personaName: persona.name,
      icon: persona.icon,
      theme: persona.theme,
      softTheme: persona.softTheme,
      topic,
      title: `${persona.code} · ${topic.slice(0, 12)}`,
      preview: topic,
      roundIndex: data.roundIndex || 1,
      finished: false,
      messages: [
        {
          id: Date.now(),
          role: 'assistant',
          content: `💌 心灵盲盒：${topic}`
        },
        {
          id: Date.now() + 1,
          role: 'assistant',
          content:
            data.reply ||
            '如果你愿意，我们就从这个问题慢慢开始。你此刻最真实的感受是什么？'
        }
      ]
    }

    sessions.value.unshift(session)
    activeSessionId.value = session.id

    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error(e)
    alert('抽取心灵盲盒失败，请检查后端接口')
  } finally {
    loading.value = false
  }
}

async function sendReply() {
  if (!activeSession.value) {
    return
  }

  if (!input.value.trim()) {
    return
  }

  if (loading.value) {
    return
  }

  const session = activeSession.value
  const userText = input.value.trim()

  input.value = ''

  session.messages.push({
    id: Date.now(),
    role: 'user',
    content: userText
  })

  session.preview = userText

  await nextTick()
  scrollToBottom()

  loading.value = true

  try {
    const res: any = await api.replyBlindBox({
      blindBoxId: session.blindBoxId,
      persona: session.personaCode,
      topic: session.topic,
      roundIndex: session.roundIndex,
      userReply: userText,
      ephemeral: false
    })

    const data = res.data || {}

    session.messages.push({
      id: Date.now() + 1,
      role: 'assistant',
      content:
        data.reply ||
        '我听见了。你说的这部分，其实已经很接近你真正想表达的东西。'
    })

    session.roundIndex = data.roundIndex || session.roundIndex + 1
    session.finished = !!data.finished

    await nextTick()
    scrollToBottom()
  } catch (e) {
    console.error(e)

    session.messages.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: '我刚刚有点没接住，但我还在这里。你可以再说一次。'
    })
  } finally {
    loading.value = false
  }
}

async function saveSessionAsDiary() {
  if (!activeSession.value) {
    return
  }

  const session = activeSession.value

  const content = [
    `心灵盲盒人格：${session.personaCode} · ${session.personaName}`,
    `话题：${session.topic}`,
    '',
    ...session.messages.map(msg => {
      if (msg.role === 'user') {
        return `我：${msg.content}`
      }

      return `${session.personaCode}：${msg.content}`
    })
  ].join('\n')

  try {
    await api.saveDiaryFromBattle({
      date: new Date().toISOString().slice(0, 10),
      emotionEmoji: '💌',
      emotionLabel: '心灵盲盒',
      keyword: '心灵盲盒',
      content,
      personaPair: session.personaCode
    })

    alert('已保存为今日情绪日记')
  } catch (e) {
    console.error(e)
    alert('保存失败，请检查日记接口')
  }
}

function scrollToBottom() {
  if (!messageBoxRef.value) {
    return
  }

  messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight
}
</script>

<style scoped>
.blind-workspace {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  min-height: 760px;
}

.sidebar {
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.8);
  padding: 20px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
}

.side-head {
  margin-bottom: 16px;
}

.side-subtitle {
  font-size: 14px;
  color: #a39d92;
}

.side-title {
  font-size: 20px;
  font-weight: 600;
}

.new-btn {
  width: 100%;
  border-radius: 999px;
  background: #eadbc8;
  padding: 12px 16px;
  font-size: 14px;
  font-weight: 500;
  color: #6e5741;
}

.new-btn:disabled {
  opacity: 0.5;
}

.session-list {
  margin-top: 20px;
  max-height: 640px;
  overflow-y: auto;
}

.session-item {
  width: 100%;
  margin-bottom: 8px;
  border-radius: 20px;
  background: #faf8f4;
  padding: 12px 16px;
  text-align: left;
  transition: 0.2s;
}

.session-item.active {
  background: #f1e7d7;
}

.session-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
  color: #4a4036;
}

.session-preview {
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 12px;
  color: #9b968c;
}

.content {
  border-radius: 30px;
  background: rgba(255, 255, 255, 0.8);
  padding: 20px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
}

.content-header {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
}

.topic-line {
  margin-top: 8px;
  font-size: 14px;
  color: #8e887f;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.soft-btn {
  border-radius: 999px;
  background: #f3eee6;
  padding: 8px 16px;
  font-size: 14px;
  color: #8e887f;
}

.end-btn {
  border-radius: 999px;
  background: #eadbc8;
  padding: 8px 16px;
  font-size: 14px;
  color: #6e5741;
}

.chat-card {
  display: flex;
  height: 680px;
  flex-direction: column;
  border-radius: 26px;
  background: #fbf9f5;
  padding: 20px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding-right: 8px;
}

.message-row {
  margin-bottom: 16px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.ai-row {
  justify-content: flex-start;
}

.user-row {
  justify-content: flex-end;
}

.avatar {
  display: flex;
  height: 44px;
  width: 44px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  font-size: 18px;
}

.user-avatar {
  background: #d8c3a5;
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.bubble {
  max-width: 72%;
  border-radius: 24px;
  padding: 12px 16px;
}

.bubble-user {
  background: #d8c3a5;
  color: white;
}

.bubble-ai {
  background: white;
  color: #444;
  box-shadow: 0 8px 20px rgba(64, 52, 42, 0.06);
}

.bubble-name {
  margin-bottom: 4px;
  font-size: 12px;
  opacity: 0.7;
}

.bubble-content {
  white-space: pre-wrap;
  font-size: 14px;
  line-height: 1.7;
}

.loading-text {
  color: #9b968c;
}

.input-bar {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.chat-input {
  flex: 1;
  border-radius: 999px;
  background: #f8f4ed;
  padding: 12px 16px;
  font-size: 14px;
  outline: none;
}

.send-btn {
  border-radius: 999px;
  background: #d8c3a5;
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.send-btn:disabled {
  opacity: 0.5;
}

.finish-card {
  margin-top: 16px;
  border-radius: 24px;
  background: #f1e7d7;
  padding: 20px;
  color: #6e5741;
}

.finish-title {
  margin-bottom: 8px;
  font-weight: 600;
}

.finish-desc {
  font-size: 14px;
  line-height: 1.7;
}

.ended-actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}

.empty-panel {
  display: flex;
  height: 680px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #8e887f;
}

.empty-icon {
  font-size: 44px;
}

.empty-title {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 600;
}

.empty-desc {
  margin-top: 8px;
  margin-bottom: 20px;
  max-width: 420px;
  font-size: 14px;
  line-height: 1.7;
  color: #9b968c;
}

.primary-btn {
  border-radius: 999px;
  background: #d8c3a5;
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  color: white;
}

.secondary-btn {
  border-radius: 999px;
  background: #f3eee6;
  padding: 12px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #6e5741;
}

.flex-one {
  flex: 1;
}
</style>