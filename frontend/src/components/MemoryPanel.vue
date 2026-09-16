<template>
  <section class="memory-card">
    <div class="head">
      <div><p class="sub-title">Memory</p><h3 class="title">当前记忆</h3></div>
      <p v-if="lastRefreshTime" class="refresh-time">上次刷新：{{ lastRefreshTime }}</p>
    </div>

    <div class="memory-grid">
      <article class="memory-item">
        <p class="item-title">近期情绪</p>
        <p class="item-value">{{ display.recentEmotionSummary || '暂无近期情绪记录' }}</p>
      </article>
      <article class="memory-item">
        <p class="item-title">最近常用人格 / Agent</p>
        <div v-if="display.recentPersonaName || display.recentPersonaCode" class="persona-value"><p class="item-value">{{ display.recentPersonaName || '未命名人格' }}</p><span v-if="display.recentPersonaCode" class="code-tag">{{ display.recentPersonaCode }}</span></div>
        <p v-else class="item-value empty">暂无常用人格</p>
      </article>
      <article class="memory-item conversation-item">
        <p class="item-title">最近一次对话</p>
        <div class="dialogue"><div><span class="speaker">你</span><p :class="{ collapsed: isLongQuestion && !expandedQuestion }">{{ display.lastQuestion || '暂无最近问题' }}</p></div><div><span class="speaker ai">AI</span><p :class="{ collapsed: isLongAnswer && !expandedAnswer }">{{ display.answer || '暂无最近回答' }}</p></div></div>
        <button v-if="isLongQuestion || isLongAnswer" class="expand-btn" type="button" @click="expandedQuestion = !expandedQuestion; expandedAnswer = !expandedAnswer">{{ expandedQuestion || expandedAnswer ? '收起' : '展开' }}</button>
      </article>
      <article class="memory-item">
        <p class="item-title">最近对话摘要</p>
        <p class="item-value" :class="{ collapsed: isLongSummary && !expandedSummary }">{{ display.chatSummary || '暂无对话摘要' }}</p>
        <button v-if="isLongSummary" class="expand-btn" type="button" @click="expandedSummary = !expandedSummary">{{ expandedSummary ? '收起' : '展开' }}</button>
      </article>
    </div>

    <div class="actions"><button class="ghost-btn" :disabled="refreshLoading" @click="loadMemory">{{ refreshLoading ? '刷新中...' : '刷新记忆' }}</button><button class="danger-btn" @click="clear">清空记忆</button></div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from '../api'

const refreshLoading = ref(false)
const expandedQuestion = ref(false)
const expandedAnswer = ref(false)
const expandedSummary = ref(false)
const lastRefreshTime = ref('')
const memory = reactive({ recentEmotionSummary: '', recentPersonaCode: '', recentPersonaName: '', chatSummary: '', lastQuestion: '', lastAnswer: '' })
function cleanMemoryText(value?: string | null) {
  if (!value) return ''
  return value.replace(/【[^】]*】/g, '').replace(/\s+/g, ' ').trim()
}
const display = computed(() => {
  const answer = memory.recentPersonaCode === 'AGENT_ROUNDTABLE'
    ? (cleanMemoryText(memory.chatSummary) || cleanMemoryText(memory.lastAnswer))
    : cleanMemoryText(memory.lastAnswer)
  return { recentEmotionSummary: cleanMemoryText(memory.recentEmotionSummary), recentPersonaName: cleanMemoryText(memory.recentPersonaName), recentPersonaCode: cleanMemoryText(memory.recentPersonaCode), chatSummary: cleanMemoryText(memory.chatSummary), lastQuestion: cleanMemoryText(memory.lastQuestion), answer }
})
const isLongQuestion = computed(() => display.value.lastQuestion.length > 180)
const isLongAnswer = computed(() => display.value.answer.length > 260)
const isLongSummary = computed(() => display.value.chatSummary.length > 180)

async function loadMemory() {
  refreshLoading.value = true
  try {
    const res: any = await api.getMemory(); const data = res.data || res
    Object.keys(memory).forEach((key) => { memory[key as keyof typeof memory] = typeof data[key] === 'string' ? data[key] : '' })
    lastRefreshTime.value = new Date().toLocaleTimeString()
  } catch (e) { console.error(e); alert('读取 Memory 失败，请确认已经登录') }
  finally { refreshLoading.value = false }
}
async function clear() {
  if (!confirm('确定要清空当前用户记忆吗？')) return
  try { await api.clearMemory(); Object.keys(memory).forEach((key) => { memory[key as keyof typeof memory] = '' }); expandedQuestion.value = false; expandedAnswer.value = false; expandedSummary.value = false; alert('Memory 已清空') }
  catch (e) { console.error(e); alert('清空 Memory 失败') }
}
onMounted(loadMemory)
</script>

<style scoped>
.memory-card { margin-top: 24px; border-radius: 32px; background: rgba(255,255,255,.88); padding: 28px; box-shadow: 0 18px 50px rgba(64,52,42,.06); }
.head { display:flex; align-items:flex-end; justify-content:space-between; gap:16px; }
.sub-title,.refresh-time { font-size:14px; color:#a39d92; }
.title { margin-top:6px; font-size:22px; font-weight:700; color:#2f2f2f; }
.refresh-time { font-size:12px; }
.memory-grid { display:grid; grid-template-columns:repeat(2,minmax(0,1fr)); gap:16px; margin-top:22px; }
.memory-item { border-radius:18px; background:#fbf7f1; padding:18px 20px; min-height:112px; }
.item-title { margin-bottom:10px; font-size:14px; font-weight:700; color:#6e5741; }
.item-value { margin:0; color:#4a4036; line-height:1.7; letter-spacing:.01em; white-space:pre-wrap; overflow-wrap:anywhere; }
.item-value.empty { color:#a39d92; }
.code-tag { display:inline-block; margin-left:6px; border-radius:999px; background:#eadbc8; padding:2px 8px; color:#80664a; font-size:12px; }
.conversation-item { grid-column:span 2; }
.dialogue > div + div { margin-top:12px; }
.dialogue p { margin:0; line-height:1.7; letter-spacing:.01em; white-space:pre-wrap; overflow-wrap:anywhere; }
.speaker { display:block; margin-bottom:4px; color:#a27d53; font-weight:700; }
.speaker.ai { color:#8b789e; }
.collapsed { display:-webkit-box; -webkit-line-clamp:4; -webkit-box-orient:vertical; overflow:hidden; }
.expand-btn { margin-top:8px; border:0; background:transparent; color:#9a7955; cursor:pointer; }
.actions { display:flex; gap:10px; margin-top:20px; }
.ghost-btn,.danger-btn { border:0; border-radius:999px; padding:11px 18px; font-size:14px; cursor:pointer; }
.ghost-btn { background:#f8f4ed; color:#7b7166; }.danger-btn { background:#f8e2df; color:#b65f56; }
button:disabled { opacity:.6; cursor:not-allowed; }
@media (max-width:768px) { .memory-grid { grid-template-columns:1fr; }.conversation-item { grid-column:auto; }.head { align-items:flex-start; flex-direction:column; } }
</style>
