<template>
  <div class="min-h-screen bg-[#F8F6F1]">
    <router-view />
    <nav class="fixed bottom-4 left-1/2 z-30 grid w-[92%] max-w-md -translate-x-1/2 grid-cols-3 rounded-full bg-white/90 p-2 shadow-soft backdrop-blur">
      <router-link class="nav-item" to="/app/diary">📅 日记</router-link>
      <router-link class="nav-item" to="/app/tree-hole">💬 树洞</router-link>
      <router-link class="nav-item" to="/app/profile">👤 我的</router-link>
    </nav>
  </div>
</template>
<style scoped>
.nav-item { @apply rounded-full py-3 text-center text-sm text-[#8E887F]; }
.router-link-active { @apply bg-[#F1E7D7] text-[#6E5741]; }
</style>
