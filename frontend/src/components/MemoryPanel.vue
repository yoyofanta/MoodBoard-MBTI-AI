<template>
  <section class="memory-card">
    <div class="head">
      <div>
        <p class="sub-title">Memory</p>
        <h3 class="title">用户会话记忆</h3>
      </div>

      <div class="actions">
        <button class="ghost-btn" :disabled="refreshLoading" @click="loadMemory">
  {{ refreshLoading ? '刷新中...' : '刷新' }}
</button>
        <button class="danger-btn" @click="clear">
          清空记忆
        </button>
      </div>
      <p v-if="lastRefreshTime" class="refresh-time">
  上次刷新：{{ lastRefreshTime }}
</p>
    </div>

    <div class="form-grid">
      <label>
        <span>最近情绪摘要</span>
        <textarea
          v-model="form.recentEmotionSummary"
          placeholder="例如：用户最近主要存在考试焦虑、拖延和睡眠不足。"
        />
      </label>

      <label>
        <span>最近常聊人格编码</span>
        <input
          v-model="form.recentPersonaCode"
          placeholder="例如：INFJ"
        />
      </label>

      <label>
        <span>最近常聊人格名称</span>
        <input
          v-model="form.recentPersonaName"
          placeholder="例如：温柔陪伴者"
        />
      </label>

      <label>
        <span>最近一次对话摘要</span>
        <textarea
          v-model="form.chatSummary"
          placeholder="例如：用户提到最近复习压力较大，担心考试结果。"
        />
      </label>

      <label>
        <span>上一次用户问题</span>
        <textarea
          v-model="form.lastQuestion"
          placeholder="用户最近一次提问内容"
        />
      </label>

      <label>
        <span>上一次 AI 回答</span>
        <textarea
          v-model="form.lastAnswer"
          placeholder="AI 最近一次回答内容"
        />
      </label>
    </div>

    <button class="save-btn" :disabled="loading" @click="save">
      {{ loading ? '保存中...' : '保存 Memory' }}
    </button>

    <div v-if="memoryText" class="preview">
      <p class="preview-title">当前记忆预览</p>
      <pre>{{ memoryText }}</pre>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from '../api'

const loading = ref(false)
const lastRefreshTime = ref('')
const refreshLoading = ref(false)

const form = reactive({
  recentEmotionSummary: '',
  recentPersonaCode: '',
  recentPersonaName: '',
  chatSummary: '',
  lastQuestion: '',
  lastAnswer: ''
})

const memoryText = computed(() => {
  const lines: string[] = []

  if (form.recentEmotionSummary) {
    lines.push(`最近情绪摘要：${form.recentEmotionSummary}`)
  }

  if (form.recentPersonaCode || form.recentPersonaName) {
    lines.push(`最近常聊人格：${form.recentPersonaName} ${form.recentPersonaCode}`)
  }

  if (form.chatSummary) {
    lines.push(`最近一次对话摘要：${form.chatSummary}`)
  }

  if (form.lastQuestion) {
    lines.push(`上一次用户问题：${form.lastQuestion}`)
  }

  if (form.lastAnswer) {
    lines.push(`上一次 AI 回答：${form.lastAnswer}`)
  }

  return lines.join('\n')
})

async function loadMemory() {
  refreshLoading.value = true

  try {
    const res: any = await api.getMemory()
    const data = res.data || res

    form.recentEmotionSummary = data.recentEmotionSummary || ''
    form.recentPersonaCode = data.recentPersonaCode || ''
    form.recentPersonaName = data.recentPersonaName || ''
    form.chatSummary = data.chatSummary || ''
    form.lastQuestion = data.lastQuestion || ''
    form.lastAnswer = data.lastAnswer || ''

    lastRefreshTime.value = new Date().toLocaleTimeString()
  } catch (e) {
    console.error(e)
    alert('读取 Memory 失败，请确认已经登录')
  } finally {
    refreshLoading.value = false
  }
}

async function save() {
  loading.value = true

  try {
    await api.updateMemory({
      recentEmotionSummary: form.recentEmotionSummary,
      recentPersonaCode: form.recentPersonaCode,
      recentPersonaName: form.recentPersonaName,
      chatSummary: form.chatSummary,
      lastQuestion: form.lastQuestion,
      lastAnswer: form.lastAnswer
    })

    alert('Memory 保存成功')
    await loadMemory()
  } catch (e) {
    console.error(e)
    alert('保存 Memory 失败')
  } finally {
    loading.value = false
  }
}

async function clear() {
  if (!confirm('确定要清空当前用户记忆吗？')) {
    return
  }

  try {
    await api.clearMemory()
    await loadMemory()
    alert('Memory 已清空')
  } catch (e) {
    console.error(e)
    alert('清空 Memory 失败')
  }
}

onMounted(() => {
  loadMemory()
})
</script>

<style scoped>
.memory-card {
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

.actions {
  display: flex;
  gap: 10px;
}

.ghost-btn,
.danger-btn,
.save-btn {
  border-radius: 999px;
  padding: 11px 18px;
  font-size: 14px;
}

.ghost-btn {
  background: #f8f4ed;
  color: #7b7166;
}

.danger-btn {
  background: #f8e2df;
  color: #b65f56;
}

.save-btn {
  margin-top: 20px;
  background: #c3ab89;
  color: white;
}

.save-btn:disabled {
  opacity: 0.6;
}

.form-grid {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

label span {
  font-size: 13px;
  color: #8f877d;
}

input,
textarea {
  border-radius: 18px;
  background: #fbf7f1;
  padding: 13px 15px;
  font-size: 14px;
  outline: none;
  color: #4a4036;
}

textarea {
  min-height: 92px;
  resize: vertical;
  line-height: 1.7;
}

.preview {
  margin-top: 22px;
  border-radius: 22px;
  background: #fbf7f1;
  padding: 18px;
}

.preview-title {
  font-size: 14px;
  font-weight: 700;
  color: #6e5741;
}

pre {
  margin-top: 10px;
  white-space: pre-wrap;
  line-height: 1.8;
  color: #555;
  font-size: 14px;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .head {
    align-items: flex-start;
    flex-direction: column;
  }
}

.refresh-time {
  margin-top: 8px;
  font-size: 12px;
  color: #a39d92;
}
</style>