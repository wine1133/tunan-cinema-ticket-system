<script setup>
defineProps({ dark: Boolean, compact: Boolean })
</script>

<template>
  <router-link to="/" class="brand-logo" :class="{ dark, compact }">
    <span class="brand-mark"><img src="/favicon.svg" alt="兔南电影" /></span>
    <span v-if="!compact" class="brand-copy">
      <strong>兔南电影</strong>
      <small>TUNAN CINEMA</small>
    </span>
  </router-link>
</template>

<style scoped>
.brand-logo { display: inline-flex; align-items: center; gap: 10px; flex-shrink: 0; }
.brand-mark { width: 38px; height: 38px; display: block; border-radius: 12px; overflow: hidden; box-shadow: 0 6px 16px rgba(255,101,66,.25); }
.brand-mark img { width: 100%; height: 100%; }
.brand-copy { display: grid; line-height: 1; gap: 4px; }
.brand-copy strong { color: var(--ink); font-size: 17px; letter-spacing: -.5px; }
.brand-copy small { color: #9c948a; font-size: 8px; letter-spacing: 2.1px; font-weight: 800; }
.dark .brand-copy strong { color: white; }
.dark .brand-copy small { color: rgba(255,255,255,.48); }
.compact .brand-mark { width: 34px; height: 34px; }
</style>
