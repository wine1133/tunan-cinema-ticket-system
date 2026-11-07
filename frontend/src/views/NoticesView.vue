<script setup>
import { onMounted, ref } from 'vue'
import http from '@/api/http'
const loading = ref(true)
const notices = ref([])
onMounted(async () => { try { const { data } = await http.get('/public/notices'); notices.value = data } finally { loading.value = false } })
</script>
<template>
  <div class="notices-page"><section class="notice-hero"><div class="page"><span class="section-kicker">NEWSROOM</span><h1>兔南公告</h1><p>关于放映、影院与购票服务的最新消息。</p></div></section><div v-loading="loading" class="page notice-content"><article v-for="(notice, index) in notices" :key="notice.id" class="notice-item surface"><div class="notice-index">{{ String(index + 1).padStart(2, '0') }}</div><div class="notice-main"><div class="notice-tags"><span>{{ notice.type }}</span><small>{{ notice.createdAt }}</small></div><h2>{{ notice.title }}</h2><p>{{ notice.content }}</p></div><el-icon><ArrowRight /></el-icon></article><div v-if="!loading && !notices.length" class="empty-state surface"><el-icon><Bell /></el-icon><h3>暂无公告</h3></div></div></div>
</template>
<style scoped>
.notices-page{min-height:70vh}.notice-hero{background:#0c1421;color:#fff;padding:55px 0}.notice-hero h1{font-size:56px;letter-spacing:-3px;margin:13px 0 9px}.notice-hero p{color:rgba(255,255,255,.46);font-size:12px}.notice-content{padding:35px 0 70px;display:grid;gap:12px}.notice-item{padding:22px 25px;display:grid;grid-template-columns:50px 1fr 25px;gap:20px;align-items:center}.notice-index{font-family:Consolas,monospace;font-size:22px;color:#d9d2c7}.notice-tags{display:flex;gap:13px;align-items:center}.notice-tags span{font-size:8px;color:var(--orange);background:var(--orange-soft);padding:4px 7px;border-radius:5px}.notice-tags small{color:#aba49a;font-size:8px}.notice-main h2{font-size:17px;margin:9px 0 7px}.notice-main p{font-size:11px;line-height:1.8;color:var(--muted);margin:0}.notice-item>.el-icon{color:#c4bdb2}
@media(max-width:520px){.notice-hero h1{font-size:42px}.notice-item{grid-template-columns:35px 1fr;padding:17px}.notice-item>.el-icon{display:none}.notice-index{font-size:17px}}
</style>
