<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import MovieCard from '@/components/MovieCard.vue'

const loading = ref(true)
const favorites = ref([])
async function load() { loading.value = true; try { const { data } = await http.get('/user/favorites'); favorites.value = data } finally { loading.value = false } }
async function remove(movie) {
  await http.delete(`/user/favorites/${movie.id}`)
  favorites.value = favorites.value.filter(item => item.id !== movie.id)
  ElMessage.success('已取消收藏')
}
onMounted(load)
</script>
<template>
  <div class="favorites-page"><section class="favorites-hero"><div class="page"><div><span class="section-kicker">MY WATCHLIST</span><h1>我的收藏</h1><p>把想看的故事留在这里，等一个刚刚好的时间。</p></div><div class="collection-count"><strong>{{ favorites.length }}</strong><span>部电影<br />在我的片单</span></div></div></section><div v-loading="loading" class="page favorites-content"><div v-if="favorites.length" class="favorite-grid"><div v-for="movie in favorites" :key="movie.id" class="favorite-item"><MovieCard :movie="movie" /><button class="remove-fav" @click="remove(movie)"><el-icon><Close /></el-icon> 取消收藏</button></div></div><div v-else-if="!loading" class="empty-state surface"><el-icon><Star /></el-icon><h3>你的片单还是空的</h3><p>去电影详情页点击收藏，让好故事留在这里</p><router-link to="/movies"><el-button type="primary" round>发现电影</el-button></router-link></div></div></div>
</template>
<style scoped>
.favorites-page{min-height:70vh}.favorites-hero{background:linear-gradient(105deg,#111c2b,#332b38 70%,#49342f);color:#fff;padding:55px 0}.favorites-hero .page{display:flex;justify-content:space-between;align-items:flex-end}.favorites-hero h1{font-size:57px;letter-spacing:-3px;margin:13px 0 8px}.favorites-hero p{color:rgba(255,255,255,.44);font-size:11px}.collection-count{display:flex;align-items:center;gap:12px;padding-bottom:9px}.collection-count strong{font-size:48px;font-weight:200}.collection-count span{color:rgba(255,255,255,.42);font-size:9px;line-height:1.5}.favorites-content{padding:36px 0 70px}.favorite-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:22px 16px}.remove-fav{width:100%;margin-top:9px;border:0;background:none;color:#aaa39a;font-size:9px;display:flex;justify-content:center;align-items:center;gap:4px;cursor:pointer}.remove-fav:hover{color:var(--danger)}
@media(max-width:1050px){.favorite-grid{grid-template-columns:repeat(4,1fr)}}@media(max-width:800px){.favorite-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:540px){.favorites-hero h1{font-size:42px}.collection-count{display:none}.favorite-grid{grid-template-columns:repeat(2,1fr)}}
</style>
