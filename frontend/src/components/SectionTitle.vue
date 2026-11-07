<script setup>
defineProps({ kicker: String, title: String, subtitle: String, more: String, to: String })
</script>
<template>
  <div class="section-title">
    <div>
      <div v-if="kicker" class="section-kicker">{{ kicker }}</div>
      <h2>{{ title }}</h2>
      <p v-if="subtitle">{{ subtitle }}</p>
    </div>
    <router-link v-if="more && to" :to="to" class="section-more">{{ more }} <el-icon><ArrowRight /></el-icon></router-link>
  </div>
</template>
