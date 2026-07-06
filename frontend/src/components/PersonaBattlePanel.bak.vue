<template>
  <div class="battle-page">
    <!-- 页面头部 -->
    <div class="mb-5 flex items-start justify-between gap-4">
      <div>
        <p class="text-sm text-[#A39D92]">Persona Battle</p>
        <h2 class="text-2xl font-semibold">⚔️ 多人格对战</h2>
        <p class="mt-2 text-sm leading-6 text-[#8E887F]">
          选择 2-4 个人格，让他们围绕你的问题展开讨论，最后生成总结分析。
        </p>
      </div>

      <button
        class="rounded-full bg-[#F3EEE6] px-5 py-3 text-sm text-[#8E887F]"
        @click="emit('close')"
      >
        返回人格对话
      </button>
    </div>

    <!-- 话题输入 -->
    <section class="battle-card mb-5">
      <p class="mb-3 font-semibold">1. 输入对战话题</p>

      <textarea
        v-model="topic"
        class="h-28 w-full resize-none rounded-[24px] bg-[#F8F4ED] p-4 text-sm leading-6 outline-none"
        placeholder="例如：我最近很迷茫，不知道该继续努力还是停下来。"
      />

      <p class="mt-2 text-xs text-[#A39D92]">
        建议输入一个真实困扰，AI 人格会从不同视角给出分析。
      </p>
    </section>

    <!-- 人格选择 -->
    <section class="battle-card mb-5">
      <div class="mb-3 flex items-center justify-between">
        <div>
          <p class="font-semibold">2. 选择参与人格</p>
          <p class="mt-1 text-xs text-[#A39D92]">
            已选择 {{ selectedCodes.length }} 个，最少 2 个，最多 4 个。
          </p>
        </div>

        <button
          class="text-sm text-[#B48B62]"
          @click="clearSelected"
        >
          清空选择
        </button>
      </div>

      <div class="grid grid-cols-2 gap-3 md:grid-cols-4">
        <button
          v-for="p in officialPersonas"
          :key="p.code"
          class="persona-card"
          :class="selectedCodes.includes(p.code) ? 'is-selected' : ''"
          :style="selectedCodes.includes(p.code) ? { borderColor: p.theme } : {}"
          @click="togglePersona(p.code)"
        >
          <div class="text-3xl">
            {{ p.icon || p.emoji || '🧩' }}
          </div>
          <p class="mt-2 font-semibold">{{ p.code }}</p>
          <p class="mt-1 text-xs text-[#9B968C]">{{ p.name }}</p>
        </button>
      </div>
    </section>

    <!-- 对战设置 -->
    <section class="battle-card mb-5">
      <p class="mb-3 font-semibold">3. 对战设置</p>

      <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
        <select
          v-model="roundsPerPersona"
          class="rounded-[22px] bg-[#F8F4ED] px-4 py-3 text-sm outline-none"
        >
          <option :value="1">每人说 1 句</option>
          <option :value="2">每人说 2 句</option>
        </select>

        <label class="flex items-center gap-2 rounded-[22px] bg-[#F8F4ED] px-4 py-3 text-sm text-[#8E887F]">
          <input v-model="ephemeral" type="checkbox" />
          阅后即焚，不保存对战记录
        </label>
      </div>

      <button
        class="primary-btn mt-4 w-full"
        :disabled="loading"
        @click="startBattle"
      >
        {{ loading ? '人格正在讨论中……' : '开始多人格对战' }}
      </button>
    </section>

    <!-- 对战结果 -->
    <section
      v-if="battleMessages.length || analysis"
      class="battle-card"
    >
      <div class="mb-4 flex items-center justify-between">
        <div>
          <p class="font-semibold">4. 对战结果</p>
          <p class="mt-1 text-xs text-[#A39D92]">
            不同人格会给出不同角度，最后自动生成总结。
          </p>
        </div>

        <button
          class="rounded-full bg-[#F3EEE6] px-4 py-2 text-sm text-[#8E887F]"
          @click="resetBattle"
        >
          重新开始
        </button>
      </div>

      <div class="max-h-[460px] overflow-y-auto rounded-[28px] bg-[#FBF9F5] p-4">
        <div
          v-for="(msg, index) in battleMessages"
          :key="index"
          class="mb-4 rounded-[24px] bg-white p-4 shadow-sm"
        >
          <div class="mb-2 flex items-center gap-2">
            <span class="text-lg">
              {{ getPersonaIcon(msg.persona) }}
            </span>
            <span class="font-semibold text-[#6E5741]">
              {{ msg.persona }}
            </span>
          </div>

          <p class="whitespace-pre-wrap text-sm leading-7 text-[#555]">
            {{ msg.content }}
          </p>
        </div>

        <div
          v-if="analysis"
          class="mt-5 rounded-[26px] bg-[#F1E7D7] p-5 text-sm leading-7 text-[#6E5741]"
        >
          <p class="mb-2 font-semibold">最终分析</p>
          <p class="whitespace-pre-wrap">{{ analysis }}</p>
        </div>
      </div>

      <button
        class="primary-btn mt-4 w-full"
        @click="saveAsDiary"
      >
        保存为情绪日记
      </button>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { officialPersonas } from '../data/personas'
import { api } from '../api'

const emit = defineEmits(['close'])

const topic = ref('我最近有点迷茫，不知道该继续努力还是停下来。')
const selectedCodes = ref<string[]>(['INTJ', 'ENFP', 'INFP'])
const roundsPerPersona = ref(1)
const ephemeral = ref(false)
const loading = ref(false)

const battleMessages = ref<any[]>([])
const analysis = ref('')

const selectedPersonas = computed(() => {
  return officialPersonas.filter(p => selectedCodes.value.includes(p.code))
})

function togglePersona(code: string) {
  if (selectedCodes.value.includes(code)) {
    selectedCodes.value = selectedCodes.value.filter(item => item !== code)
    return
  }

  if (selectedCodes.value.length >= 4) {
    alert('最多选择 4 个人格')
    return
  }

  selectedCodes.value.push(code)
}

function clearSelected() {
  selectedCodes.value = []
}

function getPersonaIcon(code: string) {
  const found = officialPersonas.find(p => p.code === code)
  return found?.icon || found?.emoji || '🧩'
}

async function startBattle() {
  if (!topic.value.trim()) {
    alert('请先输入对战话题')
    return
  }

  if (selectedCodes.value.length < 2) {
    alert('请至少选择 2 个人格')
    return
  }

  loading.value = true
  battleMessages.value = []
  analysis.value = ''

  try {
    const res: any = await api.startPersonaBattle({
      topic: topic.value,
      personas: selectedCodes.value,
      roundsPerPersona: roundsPerPersona.value,
      ephemeral: ephemeral.value
    })

    const data = res.data || {}

    const rawMessages =
      data.messages ||
      data.dialogues ||
      data.turns ||
      data.rounds ||
      []

    battleMessages.value = rawMessages.map((item: any) => {
      return {
        persona:
          item.persona ||
          item.personaCode ||
          item.speaker ||
          item.role ||
          'AI',
        content:
          item.content ||
          item.text ||
          item.message ||
          ''
      }
    })

    analysis.value =
      data.analysis ||
      data.summary ||
      data.finalAnalysis ||
      data.result ||
      ''
  } catch (e) {
    console.error(e)
    alert('多人格对战失败，请检查后端接口是否正常')
  } finally {
    loading.value = false
  }
}

function resetBattle() {
  battleMessages.value = []
  analysis.value = ''
}

async function saveAsDiary() {
  if (!battleMessages.value.length && !analysis.value) {
    alert('暂无可保存内容')
    return
  }

  const content = [
    `话题：${topic.value}`,
    '',
    ...battleMessages.value.map(msg => `${msg.persona}：${msg.content}`),
    '',
    analysis.value ? `最终分析：${analysis.value}` : ''
  ].join('\n')

  try {
    await api.saveDiaryFromBattle({
      date: new Date().toISOString().slice(0, 10),
      emotionEmoji: '🤯',
      emotionLabel: '思考',
      keyword: '人格对战',
      content,
      personaPair: selectedCodes.value.join(',')
    })

    alert('已保存为今日情绪日记')
  } catch (e) {
    console.error(e)
    alert('保存失败，请检查日记接口')
  }
}
</script>

<style scoped>
.battle-page {
  @apply rounded-[34px] bg-white/80 p-6 shadow-soft;
}

.battle-card {
  @apply rounded-[30px] bg-white/70 p-5 shadow-sm;
}

.persona-card {
  @apply rounded-[24px] border-2 border-transparent bg-[#FAF8F4] p-4 text-center transition active:scale-95;
}

.persona-card.is-selected {
  @apply bg-white shadow-md;
}
</style>