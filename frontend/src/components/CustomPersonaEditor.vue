<template>
  <div class="fixed inset-0 z-50 flex items-end bg-black/20 px-3 pb-3">
    <div class="w-full rounded-[32px] bg-[#FFFCF7] p-5 shadow-2xl">
      <div class="mb-4 flex items-center justify-between"><div><p class="text-sm text-[#A39D92]">Persona Mask</p><h2 class="text-xl font-semibold">创建我的人格面具</h2></div><button class="secondary-btn px-3 py-1" @click="$emit('close')">关闭</button></div>
      <input v-model="form.name" class="soft-input mb-3" placeholder="人格名称，如：猫猫安慰师" />
      <select v-model="form.basePersona" class="soft-input mb-3"><option v-for="p in personas" :key="p.code" :value="p.code">{{ p.code }} · {{ p.name }}</option></select>
      <input v-model="form.avatar" class="soft-input mb-3" placeholder="头像 emoji，如：🐱" />
      <input v-model="form.callName" class="soft-input mb-3" placeholder="希望 AI 怎么称呼你，如：宝贝" />
      <textarea v-model="form.promptSuffix" maxlength="500" class="h-32 w-full resize-none rounded-[24px] bg-[#F8F4ED] p-4 text-sm outline-none" placeholder="写下你的专属要求：比如多安慰我、多用猫猫语气、少讲大道理……" />
      <button class="primary-btn mt-5 w-full" @click="save">保存人格面具</button>
    </div>
  </div>
</template>
<script setup lang="ts">
import { reactive } from 'vue'
import { api } from '../api'
import { officialPersonas as personas } from '../data/personas'
const emit = defineEmits(['close','saved'])
const form = reactive({ name: '', basePersona: 'INFP', avatar: '🐱', themeColor: '#D9A9C7', callName: '', promptSuffix: '' })
async function save() { if (!form.name || !form.promptSuffix) return alert('请填写名称和自定义要求'); await api.createCustomPersona(form); emit('saved'); emit('close') }
</script>
