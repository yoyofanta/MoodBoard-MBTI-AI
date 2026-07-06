<template>
  <div class="space-y-4">
    <div>
      <p class="mb-2 text-xs text-[#A39D92]">官方人格</p>
      <div class="flex gap-3 overflow-x-auto pb-1">
        <button v-for="p in official" :key="p.code" class="min-w-[132px] rounded-[24px] bg-white p-4 text-left shadow-sm transition active:scale-95" :class="selected?.code===p.code && !selected?.customPersonaId ? 'ring-2' : ''" :style="{ '--tw-ring-color': p.theme }" @click="$emit('select', p)">
          <div class="mb-2 text-3xl">{{ p.avatar }}</div><p class="font-semibold">{{ p.code }}</p><p class="text-xs text-[#8E887F]">{{ p.name }}</p><p class="mt-2 line-clamp-2 text-xs text-[#AAA39A]">{{ p.motto }}</p>
        </button>
      </div>
    </div>
    <div v-if="custom.length">
      <p class="mb-2 text-xs text-[#A39D92]">我的人格</p>
      <div class="flex gap-3 overflow-x-auto pb-1">
        <button v-for="p in custom" :key="p.id" class="min-w-[132px] rounded-[24px] bg-white p-4 text-left shadow-sm transition active:scale-95" :class="selected?.customPersonaId===p.id ? 'ring-2 ring-[#D9A9C7]' : ''" @click="$emit('select', mapCustom(p))">
          <div class="mb-2 text-3xl">{{ p.avatar || '🎭' }}</div><p class="font-semibold">{{ p.name }}</p><p class="text-xs text-[#8E887F]">基于 {{ p.basePersona }}</p><p class="mt-2 line-clamp-2 text-xs text-[#AAA39A]">{{ p.promptSuffix }}</p>
        </button>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { officialPersonas } from '../data/personas'
const props = defineProps<{ custom: any[], selected: any }>()
defineEmits(['select'])
const official = officialPersonas
function mapCustom(p:any) {
  const base = officialPersonas.find(x => x.code === p.basePersona) || officialPersonas[0]
  return { ...base, code: p.basePersona, name: p.name, avatar: p.avatar || '🎭', theme: p.themeColor || base.theme, softTheme: base.softTheme, customPersonaId: p.id }
}
</script>
