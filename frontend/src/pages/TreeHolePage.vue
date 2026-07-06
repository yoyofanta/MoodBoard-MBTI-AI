<template>
  <div
    class="min-h-screen px-4 pb-28 pt-6 transition-colors duration-500"
    :style="{ backgroundColor: pageBgColor }"
  >
    <!-- 顶部 -->
    <header class="mb-5 flex items-center justify-between">
      <div>
        <p class="text-sm text-[#9B968C]">AI Tree Hole</p>
        <h1 class="text-2xl font-semibold">{{ pageTitle }}</h1>
      </div>

      <button
        v-if="view !== 'home'"
        class="rounded-full bg-white/80 px-4 py-2 text-sm text-[#8E887F] shadow-soft"
        @click="goBack"
      >
        ← 返回
      </button>
    </header>

    <!-- 首页：只显示两个入口 -->
    <section
      v-if="view === 'home'"
      class="grid grid-cols-1 gap-4 md:grid-cols-2"
    >
      <button
        class="main-card"
        @click="view = 'dailyMenu'"
      >
        <div class="text-4xl">🌿</div>
        <p class="mt-5 text-xl font-semibold">日常闲聊</p>
        <p class="mt-2 text-sm text-[#999]">日常树洞 · 情绪漂流瓶</p>
      </button>

      <button
        class="main-card"
        @click="openPersonaMenu"
      >
        <div class="text-4xl">🧩</div>
        <p class="mt-5 text-xl font-semibold">人格对话</p>
        <p class="mt-2 text-sm text-[#999]">MBTI人格 · 面具 · 对战</p>
      </button>
    </section>

    <!-- 日常闲聊二级页 -->
    <section
      v-if="view === 'dailyMenu'"
      class="grid grid-cols-1 gap-4 md:grid-cols-2"
    >
      <button
        class="main-card"
        @click="openDailyChat"
      >
        <div class="text-4xl">🌱</div>
        <p class="mt-5 text-xl font-semibold">日常树洞</p>
        <p class="mt-2 text-sm text-[#999]">随便说说今天的事</p>
      </button>

      <button
        class="main-card"
        @click="openBottle"
      >
        <div class="text-4xl">🌊</div>
        <p class="mt-5 text-xl font-semibold">情绪漂流瓶</p>
        <p class="mt-2 text-sm text-[#999]">匿名心情 · AI 回信</p>
      </button>
    </section>

    <!-- 日常树洞聊天页 -->
    <section v-if="view === 'dailyChat'">
  <ChatWorkspace mode="daily" />
</section>

    <!-- 情绪漂流瓶页 -->
    <section
      v-if="view === 'dailyBottle'"
      class="card p-5"
    >
      <div class="mb-5">
        <p class="text-sm text-[#A39D92]">Drift Bottle</p>
        <h2 class="text-2xl font-semibold">🌊 情绪漂流瓶</h2>
        <p class="mt-2 text-sm leading-6 text-[#8E887F]">
          匿名扔出一句今天的心情，AI 会给你一句温柔回信。
        </p>
      </div>

      <div class="mb-4 flex flex-wrap gap-2">
        <button
          v-for="e in bottleMoods"
          :key="e"
          class="rounded-2xl bg-[#F8F4ED] px-4 py-3 text-2xl transition active:scale-95"
          :class="bottleMood === e ? 'bg-[#EADBC8]' : ''"
          @click="bottleMood = e"
        >
          {{ e }}
        </button>
      </div>

      <textarea
        v-model="bottleText"
        class="h-40 w-full resize-none rounded-[28px] bg-[#F8F4ED] p-4 text-sm leading-6 outline-none"
        placeholder="匿名扔出一句今天的心情……"
      />

      <button
        class="primary-btn mt-4 w-full"
        :disabled="bottleLoading"
        @click="throwBottle"
      >
        {{ bottleLoading ? '正在漂流中……' : '扔出漂流瓶' }}
      </button>

      <div
        v-if="bottleResult"
        class="mt-5 rounded-[28px] bg-[#F1E7D7] p-5 text-sm leading-7 text-[#6E5741]"
      >
        <p class="font-semibold">漂流瓶回信</p>
        <p class="mt-2">AI：{{ bottleResult.aiEcho }}</p>
      </div>
    </section>

    <!-- 人格对话二级页 -->
    <section v-if="view === 'personaMenu'">
      <section class="card mb-4 p-4">
        <PersonaSelector
          :custom="customPersonas"
          :selected="selectedPersona"
          @select="selectPersona"
        />
      </section>

      <section class="grid grid-cols-1 gap-4 md:grid-cols-3">
        <button class="main-card small" @click="openCustomPersona">
  <div class="text-3xl">🎭</div>
  <p class="mt-4 text-lg font-semibold">人格面具</p>
  <p class="mt-2 text-sm text-[#999]">创建专属 AI 人格</p>
</button>

        <button class="main-card small" @click="view = 'blindBox'">
          <div class="text-3xl">💌</div>
          <p class="mt-4 text-lg font-semibold">心灵盲盒</p>
          <p class="mt-2 text-sm text-[#999]">随机人格 · 随机话题</p>
        </button>

        <button class="main-card small" @click="view = 'battle'">
          <div class="text-3xl">⚔️</div>
          <p class="mt-4 text-lg font-semibold">人格对战</p>
          <p class="mt-2 text-sm text-[#999]">2-4 人格圆桌对话</p>
        </button>
      </section>
    </section>

    

    <!-- 单人格聊天页 -->
    <section v-if="view === 'personaChat'">
  <ChatWorkspace
    mode="persona"
    :persona="selectedPersona"
  />
</section>

<section v-if="view === 'customPersona'" class="card p-5">
  <CustomPersonaEditor
    @close="view = 'personaMenu'"
    @saved="handleCustomPersonaSaved"
  />
</section>

    <!-- 心灵盲盒独立页 -->
    <section v-if="view === 'blindBox'">
  <BlindBoxWorkspace />
</section>
    <!-- 人格对战独立页 -->
    <section v-if="view === 'battle'" class="battle-page">
  <button class="back-btn" @click="view = 'personaMenu'">
    ← 返回人格对话
  </button>

  <PersonaBattleWorkspace />
</section>
      </div>
    </template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref } from 'vue'
import { officialPersonas } from '../data/personas'
import { api } from '../api'
import PersonaBattleWorkspace from '../components/PersonaBattleWorkspace.vue'
import CustomPersonaEditor from '../components/CustomPersonaEditor.vue'
import BlindBoxWorkspace from '../components/BlindBoxWorkspace.vue'
import PersonaSelector from '../components/PersonaSelector.vue'
import ChatWorkspace from '../components/ChatWorkspace.vue'
//import AgentRoundtablePanel from '../components/AgentRoundtablePanel.vue'
type ViewType =
  | 'home'
  | 'dailyMenu'
  | 'dailyChat'
  | 'dailyBottle'
  | 'personaMenu'
  | 'personaChat'
  | 'customPersona'
  | 'blindBox'
  | 'battle'

const view = ref<ViewType>('home')

const saveRecord = ref(true)
const selectedPersona = ref<any>(officialPersonas[0])
const customPersonas = ref<any[]>([])

const input = ref('')
const loading = ref(false)

const dailySessionId = ref<number | null>(null)
const personaSessionId = ref<number | null>(null)

const bottleText = ref('')
const bottleMood = ref('🫧')
const bottleMoods = ['🫧', '😢', '😌', '😊', '💪', '❤️']
const bottleResult = ref<any>(null)
const bottleLoading = ref(false)

const blindBoxState = ref<any>(null)

const messages = ref<any[]>([])

const pageTitle = computed(() => {
  const map: Record<ViewType, string> = {
    home: '今天想和谁聊聊？',
    dailyMenu: '日常闲聊',
    dailyChat: '日常树洞',
    dailyBottle: '情绪漂流瓶',
    personaMenu: '人格对话',
    personaChat: `${selectedPersona.value.code} · ${selectedPersona.value.name}`,
    customPersona: '人格面具',
    blindBox: '心灵盲盒',
    battle: '人格对战'
  }

  return map[view.value]
})

const pageBgColor = computed(() => {
  if (view.value.startsWith('daily')) return '#F3F8F4'
  if (view.value === 'home') return '#F8F6F1'
  return selectedPersona.value.softTheme || '#F8F6F1'
})

const mainColor = computed(() => {
  if (view.value.startsWith('daily')) return '#AFC9B8'
  return selectedPersona.value.theme || '#B9A7D8'
})

onMounted(loadCustomPersonas)

async function loadCustomPersonas() {
  const res: any = await api.customPersonas()
  customPersonas.value = res.data
}

function resetDailyMessages() {
  messages.value = [
    {
      id: Date.now(),
      role: 'assistant',
      content: '这里是日常树洞。你可以随便说说，不需要组织好语言。'
    }
  ]
}

function resetPersonaMessages() {
  messages.value = [
    {
      id: Date.now(),
      role: 'assistant',
      content: `我现在会以 ${selectedPersona.value.name} 的方式陪你聊天。`
    }
  ]
}

function openDailyChat() {
  view.value = 'dailyChat'
  blindBoxState.value = null
  resetDailyMessages()
}

function openBottle() {
  view.value = 'dailyBottle'
  bottleResult.value = null
}

function openBattle() {
  view.value = 'battle'
}

function openPersonaMenu() {
  view.value = 'personaMenu'
  blindBoxState.value = null
}

function selectPersona(p: any) {
  selectedPersona.value = p
  view.value = 'personaChat'
  blindBoxState.value = null
  resetPersonaMessages()
}

function goBack() {
  if (view.value === 'dailyChat' || view.value === 'dailyBottle') {
    view.value = 'dailyMenu'
    return
  }

  if (
    view.value === 'personaChat' ||
    view.value === 'customPersona' ||
    view.value === 'blindBox' ||
    view.value === 'battle'
  ) {
    view.value = 'personaMenu'
    return
  }

  view.value = 'home'
}

function openCustomPersona() {
  view.value = 'customPersona'
}

async function sendMessage() {
  if (!input.value.trim()) return

  const content = input.value.trim()
  input.value = ''

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content
  })

  loading.value = true

  try {
    if (blindBoxState.value) {
      const res: any = await api.replyBlindBox({
        ...blindBoxState.value,
        userReply: content,
        roundIndex: blindBoxState.value.roundIndex || 1,
        ephemeral: !saveRecord.value
      })

      blindBoxState.value.roundIndex = res.data.roundIndex

      messages.value.push({
        id: Date.now() + 1,
        role: 'assistant',
        content: res.data.reply
      })

      if (res.data.finished) {
        blindBoxState.value = null
      }

      return
    }

    const isDaily = view.value === 'dailyChat'

    const currentSessionId = isDaily
      ? dailySessionId.value
      : personaSessionId.value

    const res: any = await api.sendChat({
      sessionId: currentSessionId,
      chatType: isDaily ? 'DAILY' : 'PERSONA',
      persona: isDaily ? 'DAILY' : selectedPersona.value.code,
      customPersonaId: isDaily ? null : selectedPersona.value.customPersonaId,
      content,
      ephemeral: !saveRecord.value
    })

    if (isDaily) {
      dailySessionId.value = res.data.sessionId || dailySessionId.value
    } else {
      personaSessionId.value = res.data.sessionId || personaSessionId.value
    }

    messages.value.push({
      id: Date.now() + 1,
      role: 'assistant',
      content: res.data.reply
    })
  } finally {
    loading.value = false
  }
}

async function clearCurrentSession() {
  const isDaily = view.value === 'dailyChat'

  const id = isDaily
    ? dailySessionId.value
    : personaSessionId.value

  if (id) {
    await api.clearChatMessages(id)
  }

  blindBoxState.value = null

  if (isDaily) {
    resetDailyMessages()
  } else {
    resetPersonaMessages()
  }
}

async function throwBottle() {
  if (!bottleText.value.trim()) {
    alert('请先写下你的匿名心情')
    return
  }

  bottleLoading.value = true

  try {
    const res: any = await api.throwDriftBottle({
      content: bottleText.value,
      moodEmoji: bottleMood.value
    })

    bottleResult.value = res.data
    bottleText.value = ''
  } finally {
    bottleLoading.value = false
  }
}

function handleCustomPersonaSaved() {
  loadCustomPersonas()
  view.value = 'personaMenu'
}

async function startBlindBox(box: any) {
  selectedPersona.value =
    officialPersonas.find(p => p.code === box.persona) || officialPersonas[0]

  const res: any = await api.startBlindBox({
    persona: box.persona,
    topic: box.topic,
    ephemeral: !saveRecord.value
  })

  blindBoxState.value = {
    blindBoxId: res.data.blindBoxId,
    persona: box.persona,
    topic: box.topic,
    roundIndex: res.data.roundIndex,
    ephemeral: !saveRecord.value
  }

  view.value = 'personaChat'

  messages.value = [
    {
      id: Date.now(),
      role: 'assistant',
      content: `💌 心灵盲盒：${box.topic}`
    },
    {
      id: Date.now() + 1,
      role: 'assistant',
      content: res.data.reply
    }
  ]
}

/**
 * 内联聊天内容组件
 * 先放在当前页面里，避免你现在新增太多文件。
 */
const ChatContent = defineComponent({
  props: {
    messages: {
      type: Array,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    },
    label: {
      type: String,
      default: 'AI'
    },
    mainColor: {
      type: String,
      default: '#AFC9B8'
    },
    assistantBg: {
      type: String,
      default: '#EAF4ED'
    }
  },
  setup(props) {
    return () =>
      h('div', { class: 'h-[520px] overflow-y-auto rounded-[28px] bg-[#FBF9F5] p-4' }, [
        ...(props.messages as any[]).map((msg: any) =>
          h(
            'div',
            {
              key: msg.id,
              class: `mb-3 flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`
            },
            [
              h(
                'div',
                {
                  class: 'max-w-[78%] whitespace-pre-wrap rounded-[24px] px-4 py-3 text-sm leading-6',
                  style: {
                    backgroundColor: msg.role === 'user' ? props.mainColor : props.assistantBg,
                    color: msg.role === 'user' ? '#fff' : '#383838'
                  }
                },
                [
                  msg.role === 'assistant'
                    ? h('p', { class: 'mb-1 text-xs opacity-70' }, props.label)
                    : null,
                  msg.content
                ]
              )
            ]
          )
        ),
        props.loading
          ? h('p', { class: 'text-center text-sm text-[#AAA39A]' }, 'AI 正在认真想……')
          : null
      ])
  }
})

/**
 * 内联输入框组件
 */
const ChatInput = defineComponent({
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '说点什么……'
    },
    mainColor: {
      type: String,
      default: '#AFC9B8'
    }
  },
  emits: ['update:modelValue', 'send'],
  setup(props, { emit }) {
    return () =>
      h('div', { class: 'mt-4 flex gap-2' }, [
        h('input', {
          value: props.modelValue,
          placeholder: props.placeholder,
          class: 'flex-1 rounded-full bg-[#F7F3EC] px-4 py-3 text-sm outline-none',
          onInput: (e: Event) => emit('update:modelValue', (e.target as HTMLInputElement).value),
          onKeydown: (e: KeyboardEvent) => {
            if (e.key === 'Enter') {
              emit('send')
            }
          }
        }),
        h(
          'button',
          {
            class: 'rounded-full px-5 py-3 text-sm font-medium text-white',
            style: { backgroundColor: props.mainColor },
            onClick: () => emit('send')
          },
          '发送'
        )
      ])
  }
})
</script>

<style scoped>
.main-card {
  @apply min-h-[190px] rounded-[34px] bg-white/80 p-8 text-left shadow-soft transition active:scale-95;
}

.main-card.small {
  @apply min-h-[160px];
}

.card {
  @apply rounded-[34px] bg-white/80 shadow-soft;
}

.chat-page {
  @apply rounded-[34px] bg-white/80 p-5 shadow-soft;
}

.chat-header {
  @apply mb-4 flex items-center justify-between gap-3;
}
.battle-page {
  height: calc(100vh - 96px);
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: hidden;
}

.back-btn {
  align-self: flex-start;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.88);
  padding: 8px 16px;
  font-size: 13px;
  color: #7b7166;
  box-shadow: 0 10px 30px rgba(64, 52, 42, 0.06);
}
.back-btn:hover {
  background: #fbf7f1;
}
</style>