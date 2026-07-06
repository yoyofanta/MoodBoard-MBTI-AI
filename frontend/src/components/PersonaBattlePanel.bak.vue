<template>
  <div class="fixed inset-0 z-50 bg-black/20 px-3 py-6">
    <div class="mx-auto flex h-full max-w-xl flex-col rounded-[32px] bg-[#FFFCF7] p-5 shadow-2xl">
      <div class="mb-4 flex items-center justify-between">
        <div><p class="text-sm text-[#A39D92]">Persona Battle</p><h2 class="text-xl font-semibold">多人格对战</h2><p class="text-xs text-[#9B968C]">选择 2-4 个人格，每个被选中人格至少发言一次，最后自动总结。</p></div>
        <button class="secondary-btn px-3 py-1" @click="$emit('close')">关闭</button>
      </div>
      <textarea v-model="topic" class="mb-3 h-20 w-full resize-none rounded-[24px] bg-[#F8F4ED] p-4 text-sm outline-none" placeholder="输入你的烦恼或话题：比如纠结要不要换专业、社交很累怎么办……" />
      <div class="mb-3 grid grid-cols-4 gap-2">
        <button v-for="p in personas" :key="p.code" class="rounded-[20px] p-3 text-center text-xs transition active:scale-95" :class="selected.includes(p.code) ? 'bg-white ring-2' : 'bg-[#F8F4ED]'" :style="{ '--tw-ring-color': p.theme }" @click="toggle(p.code)">
          <div class="text-2xl">{{ p.avatar }}</div><p class="mt-1 font-semibold">{{ p.code }}</p>
        </button>
      </div>
      <div class="mb-3 flex items-center gap-3">
        <select v-model.number="roundsPerPersona" class="soft-input flex-1"><option :value="1">每人说 1 句</option><option :value="2">每人说 2 句</option></select>
        <label class="flex items-center gap-2 text-xs text-[#8E887F]"><input type="checkbox" v-model="ephemeral" /> 阅后即焚</label>
      </div>
      <button class="primary-btn mb-3 w-full" :disabled="loading" @click="startBattle">{{ loading ? '人格正在对谈中……' : '开始多人格对战' }}</button>
      <div class="flex-1 overflow-y-auto rounded-[28px] bg-[#FBF9F5] p-4">
        <div v-for="m in messages" :key="m.roundIndex" class="mb-3 rounded-[24px] bg-white p-4 shadow-sm" :style="{ borderLeft: `6px solid ${m.theme}` }">
          <div class="mb-2 flex items-center gap-2"><span class="text-xl">{{ m.avatar }}</span><span class="font-semibold" :style="{ color: m.theme }">{{ m.persona }} · {{ m.personaName }}</span><span class="ml-auto text-xs text-[#AAA39A]">#{{ m.roundIndex }}</span></div>
          <p class="whitespace-pre-wrap text-sm leading-6 text-[#555]">{{ m.content }}</p>
        </div>
        <div v-if="analysis" class="mt-4 rounded-[28px] bg-[#F1E7D7] p-4">
          <p class="mb-2 font-semibold text-[#6E5741]">最终分析结果</p>
          <p class="whitespace-pre-wrap text-sm leading-7 text-[#6E5741]">{{ analysis }}</p>
        </div>
      </div>
      <div class="mt-4 grid grid-cols-2 gap-3"><button class="secondary-btn" @click="reset">重新开始</button><button class="primary-btn" :disabled="!analysis" @click="saveDiary">保存为日记</button></div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../api'
import { officialPersonas as personas } from '../data/personas'
const emit = defineEmits(['close', 'save-to-diary'])
const topic = ref('我最近有点迷茫，不知道该继续努力还是停下来。')
const selected = ref<string[]>(['INTJ', 'INFP', 'ENFP'])
const roundsPerPersona = ref(1)
const ephemeral = ref(false)
const loading = ref(false)
const messages = ref<any[]>([])
const analysis = ref('')
const lastResult = ref<any>(null)
function toggle(code: string) { if (selected.value.includes(code)) selected.value = selected.value.filter(x => x !== code); else { if (selected.value.length >= 4) return alert('最多选择4个人格'); selected.value.push(code) } }
async function startBattle() { if (selected.value.length < 2) return alert('至少选择2个人格'); if (!topic.value.trim()) return alert('请填写话题'); loading.value = true; messages.value = []; analysis.value = ''; try { const res: any = await api.startPersonaBattle({ topic: topic.value, personas: selected.value, roundsPerPersona: roundsPerPersona.value, ephemeral: ephemeral.value }); lastResult.value = res.data; messages.value = res.data.messages; analysis.value = res.data.analysis } finally { loading.value = false } }
function reset() { messages.value=[]; analysis.value=''; lastResult.value=null }
async function saveDiary() { const payload = { date: new Date().toISOString().slice(0,10), emotionEmoji: '😶‍🌫️', emotionLabel: '思考中', keyword: '人格对战', content: lastResult.value?.diaryContent || analysis.value, personaPair: selected.value.join('-'), sourceId: lastResult.value?.battleId }; await api.saveDiaryFromBattle(payload); emit('save-to-diary', payload); alert('已保存为情绪日记') }
</script>
