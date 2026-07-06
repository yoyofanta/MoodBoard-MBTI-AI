<template>
  <div class="min-h-screen bg-[#F8F6F1] px-5 py-8">
    <div class="mx-auto max-w-md card p-6">
      <p class="text-sm text-[#A39D92]">首次引导</p>
      <h1 class="mt-2 text-2xl font-semibold">让 AI 更懂你的现实处境</h1>
      <p class="mt-2 text-sm leading-6 text-[#8E887F]">这些信息只作为隐藏上下文，用于调整 AI 的建议方向。</p>
      <div class="mt-6 space-y-3">
        <input v-model="form.nickname" class="soft-input" placeholder="昵称" />
        <select v-model="form.occupation" class="soft-input"><option value="student">学生</option><option value="worker">职场人</option><option value="freelancer">自由职业</option><option value="other">其他</option></select>
        <select v-model="form.ageRange" class="soft-input"><option value="under18">18岁以下</option><option value="18-22">18-22岁</option><option value="23-30">23-30岁</option><option value="31-40">31-40岁</option><option value="40+">40岁以上</option></select>
        <select v-model="form.gender" class="soft-input">
          <option value="">性别（可选）</option>
          <option value="未透露">未透露</option>
          <option value="女">女</option>
          <option value="男">男</option>
          <option value="其他">其他</option>
        </select>
        <select v-model="form.residentPersona" class="soft-input"><option v-for="p in personas" :key="p.code" :value="p.code">{{ p.code }} · {{ p.name }}</option></select>
      </div>
      <button class="primary-btn mt-6 w-full" @click="save">开始使用</button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { officialPersonas as personas } from '../data/personas'
const router = useRouter()
const form = reactive({ nickname: '小木', occupation: 'student', ageRange: '18-22', gender: '', residentPersona: 'INFJ' })
async function save() {
  await api.saveProfile({
    ...form,
    gender: form.gender || '未透露'
  })
  router.push('/app/diary')
}
</script>
