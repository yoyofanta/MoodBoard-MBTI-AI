<template>
  <div class="min-h-screen px-4 pb-28 pt-6">
    <header class="mb-5"><p class="text-sm text-[#9B968C]">Profile</p><h1 class="text-2xl font-semibold">我的居民卡</h1></header>
    <section class="card p-5">
      <div class="flex items-center gap-4">
        <div class="flex h-16 w-16 items-center justify-center rounded-[24px] bg-[#F1E7D7] text-3xl">{{ persona?.avatar || '🌙' }}</div>
        <div><h2 class="text-xl font-semibold">{{ form.nickname }}</h2><p class="text-sm text-[#8E887F]">{{ form.occupation }} · {{ form.ageRange }} · 常驻 {{ form.residentPersona }}</p></div>
      </div>
      <div class="mt-5 grid grid-cols-2 gap-3">
        <input v-model="form.nickname" class="soft-input" placeholder="昵称" />
        <select v-model="form.occupation" class="soft-input"><option value="student">学生</option><option value="worker">职场人</option><option value="freelancer">自由职业</option><option value="other">其他</option></select>
        <select v-model="form.ageRange" class="soft-input"><option value="under18">18岁以下</option><option value="18-22">18-22岁</option><option value="23-30">23-30岁</option><option value="31-40">31-40岁</option><option value="40+">40岁以上</option></select>
        <select v-model="form.residentPersona" class="soft-input"><option v-for="p in personas" :key="p.code" :value="p.code">{{ p.code }} · {{ p.name }}</option></select>
      </div>
      <input v-model="form.gender" class="soft-input mt-3" placeholder="性别，可填未透露" />
      <button class="primary-btn mt-4 w-full" @click="save">保存资料</button>
    </section>
    <section class="card mt-5 p-5">
      <h2 class="font-semibold">我的人格面具</h2>
      <div v-if="custom.length" class="mt-3 space-y-3"><div v-for="c in custom" :key="c.id" class="rounded-[24px] bg-[#FBF7F0] p-4"><p class="font-medium">{{ c.avatar }} {{ c.name }} <span class="text-xs text-[#999]">基于 {{ c.basePersona }}</span></p><p class="mt-1 line-clamp-2 text-sm text-[#8E887F]">{{ c.promptSuffix }}</p></div></div>
      <p v-else class="mt-3 text-sm text-[#999]">还没有创建人格面具，可以在树洞页创建。</p>
    </section>
    <button class="secondary-btn mt-5 w-full" @click="logout">退出登录</button>
    <KnowledgeWorkspace />
    <MemoryPanel />
    <ToolCallingPanel />
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { officialPersonas as personas } from '../data/personas'
import KnowledgeWorkspace from '../components/KnowledgeWorkspace.vue'
import MemoryPanel from '../components/MemoryPanel.vue'
import ToolCallingPanel from '../components/ToolCallingPanel.vue'
//import AgentRoundtablePanel from '../components/AgentRoundtablePanel.vue'

const router = useRouter()
const form = reactive({ nickname:'', occupation:'student', ageRange:'18-22', gender:'未透露', residentPersona:'INFJ' })
const custom = ref<any[]>([])
const persona = computed(() => personas.find(p => p.code === form.residentPersona))
onMounted(load)
async function load(){ const res:any = await api.getProfile(); Object.assign(form, res.data); const cp:any = await api.customPersonas(); custom.value = cp.data }
async function save(){ await api.saveProfile(form); alert('保存成功') }
function logout(){ localStorage.removeItem('token'); router.push('/login') }
</script>
