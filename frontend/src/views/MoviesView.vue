<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import MovieCard from '@/components/MovieCard.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const movies = ref([])
const genres = ref([])
const total = ref(0)
const page = ref(1)
const size = 16
const filters = ref({ status: route.query.status || 'NOW', genreId: route.query.genreId ? Number(route.query.genreId) : null, keyword: route.query.keyword || '' })
const sort = ref('default')
const search = ref(filters.value.keyword)
const displayed = computed(() => {
  const rows = [...movies.value]
  if (sort.value === 'score') rows.sort((a, b) => b.score - a.score)
  if (sort.value === 'box') rows.sort((a, b) => b.boxOffice - a.boxOffice)
  if (sort.value === 'want') rows.sort((a, b) => b.wantCount - a.wantCount)
  return rows
})
async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size, status: filters.value.status || undefined, genreId: filters.value.genreId || undefined, keyword: filters.value.keyword || undefined }
    const { data } = await http.get('/public/movies', { params })
    movies.value = data.records
    total.value = data.total
    router.replace({ query: { ...(filters.value.status ? { status: filters.value.status } : {}), ...(filters.value.genreId ? { genreId: filters.value.genreId } : {}), ...(filters.value.keyword ? { keyword: filters.value.keyword } : {}) } })
  } finally { loading.value = false }
}
function setStatus(status) { filters.value.status = status; page.value = 1; load() }
function setGenre(id) { filters.value.genreId = filters.value.genreId === id ? null : id; page.value = 1; load() }
function doSearch() { filters.value.keyword = search.value.trim(); page.value = 1; load() }
onMounted(async () => {
  const { data } = await http.get('/public/genres')
  genres.value = data
  load()
})
watch(() => route.query.keyword, (value) => { if (value !== undefined && value !== filters.value.keyword) { filters.value.keyword = value; search.value = value; page.value = 1; load() } })
</script>

<template>
  <div class="movies-page">
    <section class="movies-hero">
      <div class="page">
        <div class="movies-hero-copy"><div><span class="section-kicker">DISCOVER YOUR NEXT STORY</span><h1>电影，不止一种<br /><em>打开世界的方式</em></h1><p>在热映与待映之间，找到此刻最想看的故事。按类型、评分与期待值自由筛选。</p></div><div class="count-mark"><strong>{{ total }}</strong><span>部电影<br />正在展示</span></div></div>
      </div>
    </section>
    <div class="page movie-browser">
      <aside class="filter-panel surface">
        <div class="filter-title"><span>筛选电影</span><el-button text @click="filters = { status: 'NOW', genreId: null, keyword: '' }; search=''; load()">重置</el-button></div>
        <div class="filter-group"><label>放映状态</label><div class="status-tabs"><button :class="{ active: filters.status === 'NOW' }" @click="setStatus('NOW')">正在热映</button><button :class="{ active: filters.status === 'SOON' }" @click="setStatus('SOON')">即将上映</button><button :class="{ active: filters.status === '' }" @click="setStatus('')">全部电影</button></div></div>
        <div class="filter-group"><label>电影类型</label><div class="genre-tags"><button v-for="genre in genres" :key="genre.id" :class="{ active: filters.genreId === genre.id }" @click="setGenre(genre.id)"><i :style="{ background: genre.color }"></i>{{ genre.name }}</button></div></div>
        <div class="filter-promo"><span>EDITOR'S PICK</span><img src="/posters/original/love-myth.jpg" /><strong>爱情神话</strong><small>本周最值得大银幕体验</small><router-link to="/movies/1">查看详情 <el-icon><Right /></el-icon></router-link></div>
      </aside>
      <main class="movie-content">
        <div class="browser-toolbar"><el-input v-model="search" placeholder="搜索电影名称、导演" clearable :prefix-icon="Search" @keyup.enter="doSearch" @clear="doSearch"><template #append><el-button :icon="Search" @click="doSearch" /></template></el-input><div class="toolbar-right"><span class="muted">共 {{ total }} 部</span><el-select v-model="sort" style="width:130px"><el-option label="默认排序" value="default" /><el-option label="评分最高" value="score" /><el-option label="票房最高" value="box" /><el-option label="期待值最高" value="want" /></el-select></div></div>
        <div v-loading="loading" class="movie-grid browser-grid"><MovieCard v-for="movie in displayed" :key="movie.id" :movie="movie" show-status /></div>
        <div v-if="!loading && !movies.length" class="empty-state surface"><el-icon><Film /></el-icon><h3>没有找到匹配的电影</h3><p>换一个关键词或筛选条件试试</p></div>
        <div v-if="total > size" class="pagination"><el-pagination background layout="prev, pager, next" :total="total" :page-size="size" v-model:current-page="page" @current-change="load" /></div>
      </main>
    </div>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
export default { components: { Search } }
</script>

<style scoped>
.movies-page { min-height:70vh; }
.movies-hero { padding:52px 0 38px; background:linear-gradient(120deg,#f7f3ea,#fff 65%); border-bottom:1px solid var(--line); }
.movies-hero-copy { display:flex; justify-content:space-between; align-items:flex-end; gap:30px; }.movies-hero h1 { margin:12px 0 14px; font-size:clamp(38px,5vw,67px); line-height:1.08; letter-spacing:-3px; }.movies-hero h1 em { color:var(--orange); font-style:normal; }.movies-hero p { max-width:520px; color:var(--muted); line-height:1.8; font-size:13px; }.count-mark { display:flex; align-items:center; gap:13px; padding-bottom:7px; }.count-mark strong { font-size:47px; font-weight:200; }.count-mark span { color:var(--muted); font-size:10px; line-height:1.5; }
.movie-browser { display:grid; grid-template-columns:235px 1fr; gap:30px; padding:34px 0 70px; align-items:start; }.filter-panel { padding:20px; position:sticky; top:96px; }.filter-title { display:flex; justify-content:space-between; align-items:center; font-size:14px; font-weight:800; padding-bottom:15px; border-bottom:1px solid var(--line); }.filter-title .el-button { font-size:10px; color:var(--muted); }.filter-group { padding:20px 0; border-bottom:1px solid var(--line); }.filter-group label { display:block; color:#8c857a; font-size:9px; letter-spacing:1px; margin-bottom:12px; }.status-tabs { display:grid; gap:3px; }.status-tabs button { border:0; text-align:left; padding:9px 11px; border-radius:8px; background:none; color:#5c6677; font-size:11px; cursor:pointer; }.status-tabs button.active { background:var(--orange-soft); color:var(--orange); font-weight:700; }.genre-tags { display:flex; flex-wrap:wrap; gap:7px; }.genre-tags button { border:1px solid var(--line); background:#fff; padding:5px 8px; border-radius:6px; font-size:10px; cursor:pointer; color:#687284; display:flex; align-items:center; gap:5px; }.genre-tags i { width:5px; height:5px; border-radius:50%; }.genre-tags button.active { border-color:#ffad9b; color:var(--orange); background:var(--orange-soft); }.filter-promo { margin-top:20px; background:#101927; border-radius:12px; padding:15px; color:#fff; display:grid; justify-items:start; }.filter-promo span { color:#ffb86b; font-size:7px; letter-spacing:1.6px; }.filter-promo img { width:72px; border-radius:5px; margin:14px 0 10px; }.filter-promo strong { font-size:13px; }.filter-promo small { color:rgba(255,255,255,.42); font-size:8px; margin-top:4px; }.filter-promo a { margin-top:12px; color:#ffb86b; font-size:9px; display:flex; align-items:center; gap:4px; }
.browser-toolbar { display:flex; justify-content:space-between; gap:15px; margin-bottom:26px; }.browser-toolbar .el-input { max-width:410px; }.toolbar-right { display:flex; align-items:center; gap:16px; font-size:11px; }.browser-grid { grid-template-columns:repeat(4,1fr); }.pagination { display:flex; justify-content:center; margin-top:45px; }.empty-state h3 { margin:0 0 7px; color:var(--ink); }.empty-state p { margin:0; font-size:12px; }
@media(max-width:1080px){.movie-browser{grid-template-columns:200px 1fr}.browser-grid{grid-template-columns:repeat(3,1fr)}}
@media(max-width:780px){.movie-browser{grid-template-columns:1fr}.filter-panel{position:static}.filter-promo{display:none}.filter-group{padding:14px 0}.status-tabs{grid-template-columns:repeat(3,1fr)}.status-tabs button{text-align:center}.browser-grid{grid-template-columns:repeat(2,1fr)}.movies-hero-copy{display:block}.count-mark{display:none}.toolbar-right>span{display:none}.browser-toolbar .el-input{max-width:none}}
</style>
