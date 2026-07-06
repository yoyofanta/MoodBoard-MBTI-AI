<template>
  <div class="fixed inset-0 z-50 flex items-end bg-black/20 px-3 pb-3">
    <div class="w-full rounded-[32px] bg-[#FFFCF7] p-5 shadow-2xl">
      <div class="mb-4 flex items-center justify-between"><div><p class="text-sm text-[#A39D92]">Soul Blind Box</p><h2 class="text-xl font-semibold">抽一个灵魂对谈</h2></div><button class="secondary-btn px-3 py-1" @click="$emit('close')">关闭</button></div>
      <div class="rounded-[28px] bg-[#F8F4ED] p-5 text-center"><div class="mb-3 text-5xl">{{ box.avatar }}</div><p class="text-sm text-[#A39D92]">你抽到了</p><h3 class="text-xl font-semibold">{{ box.persona }} · {{ box.personaName }}</h3></div>
      <div class="mt-4 rounded-[28px] bg-white p-5"><p class="mb-2 text-sm text-[#A39D92]">今日话题</p><p class="text-lg font-medium leading-7">{{ box.topic }}</p></div>
      <div class="mt-4 flex gap-3"><button class="secondary-btn flex-1" @click="draw">再抽一次</button><button class="primary-btn flex-1" @click="$emit('start', box)">开始 3 轮轻对话</button></div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../api'
defineEmits(['close','start'])
const box = ref<any>({ persona:'INFJ', personaName:'温柔洞察者', avatar:'🌙', topic:'今天你想被怎样安慰？' })
onMounted(draw)
async function draw(){ const res:any = await api.drawBlindBox(); box.value = res.data }
</script>
