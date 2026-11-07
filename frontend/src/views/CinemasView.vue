<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

const router = useRouter()
const loading = ref(true)
const cinemas = ref([])
const keyword = ref('')
async function load() {
  loading.value = true
  try { const { data } = await http.get('/public/cinemas', { params: { keyword: keyword.value || undefined } }); cinemas.value = data } finally { loading.value = false }
}
onMounted(load)
</script>

<template>
  <div class="cinemas-page">
    <section class="cinema-hero">
      <div class="page cinema-hero-inner"><div><span class="section-kicker">FIND YOUR SCREEN</span><h1>找到离你最近的<br /><em>一束光</em></h1><p>不同银幕，不同空间，同样的沉浸。选择一家影院，开始今晚的观影计划。</p></div><div class="cinema-search"><span>选择城市</span><div class="city-selector"><b>南京</b><el-icon><LocationFilled /></el-icon></div><el-input v-model="keyword" placeholder="搜索影院名称或地址" :prefix-icon="Search" @keyup.enter="load"><template #append><el-button :icon="Search" @click="load" /></template></el-input></div></div>
      <div class="hero-film-strip"><span v-for="i in 18" :key="i"></span></div>
    </section>
    <div class="page cinemas-content">
      <div class="list-heading"><div><span>NANJING · 南京</span><h2>精选影院</h2></div><span>共 {{ cinemas.length }} 家影院</span></div>
      <div v-loading="loading" class="cinema-grid">
        <article v-for="cinema in cinemas" :key="cinema.id" class="cinema-card surface" @click="router.push(`/cinemas/${cinema.id}`)">
          <div class="cinema-cover"><img :src="cinema.cover" :alt="cinema.name" /><div class="cover-shade"></div><span class="verified"><el-icon><CircleCheckFilled /></el-icon> 平台认证</span><span class="cinema-code">TN CINEMA · {{ String(cinema.id).padStart(3,'0') }}</span></div>
          <div class="cinema-body"><div class="cinema-title-row"><div><h3>{{ cinema.name }}</h3><p><el-icon><Location /></el-icon>{{ cinema.address }}</p></div><span class="go-circle"><el-icon><Right /></el-icon></span></div>
            <div class="cinema-data"><div><strong>{{ cinema.roomCount }}</strong><span>个影厅</span></div><div><strong>{{ cinema.movieCount }}</strong><span>部在映</span></div><div><strong>4.9</strong><span>服务评分</span></div></div>
            <div class="facility-row"><span v-for="item in cinema.facilities?.split(',').slice(0,4)" :key="item">{{ item }}</span></div>
            <div class="cinema-bottom"><span><el-icon><Clock /></el-icon> 营业中 09:00 - 24:00</span><el-button round size="small">查看排片</el-button></div>
          </div>
        </article>
      </div>
      <section class="cinema-service surface"><div><span>WHY TUNAN</span><h2>更懂你的观影选择</h2></div><div class="service-items"><div><el-icon><Select /></el-icon><strong>实时选座</strong><p>座位状态与影院同步</p></div><div><el-icon><PriceTag /></el-icon><strong>透明票价</strong><p>多影院场次自由比较</p></div><div><el-icon><Ticket /></el-icon><strong>自助取票</strong><p>订单核销简单快捷</p></div><div><el-icon><Service /></el-icon><strong>安心退改</strong><p>取票前支持取消订单</p></div></div></section>
    </div>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
export default { components: { Search } }
</script>

<style scoped>
.cinemas-page{min-height:70vh}.cinema-hero{position:relative;overflow:hidden;background:linear-gradient(112deg,#0b1421,#1c3043 65%,#3d4b47);color:#fff;padding:58px 0 0}.cinema-hero::after{content:'';position:absolute;inset:0;background:radial-gradient(circle at 78% 45%,rgba(255,182,107,.2),transparent 32%)}.cinema-hero-inner{position:relative;z-index:2;display:flex;justify-content:space-between;align-items:flex-end;gap:50px;padding-bottom:70px}.cinema-hero h1{margin:14px 0 15px;font-size:clamp(45px,5.3vw,73px);line-height:1.05;letter-spacing:-3px}.cinema-hero h1 em{color:#ffb76a;font-style:normal}.cinema-hero p{color:rgba(255,255,255,.52);font-size:12px;line-height:1.8;max-width:480px}.cinema-search{width:390px;background:rgba(255,255,255,.09);border:1px solid rgba(255,255,255,.15);border-radius:15px;padding:16px;backdrop-filter:blur(10px)}.cinema-search>span{color:rgba(255,255,255,.43);font-size:8px;letter-spacing:1px}.city-selector{display:flex;justify-content:space-between;align-items:center;padding:10px 0 13px}.city-selector b{font-size:18px}.city-selector .el-icon{color:#ffb76a}.hero-film-strip{height:32px;display:flex;justify-content:space-around;align-items:center;background:rgba(0,0,0,.28);position:relative;z-index:2}.hero-film-strip span{width:26px;height:14px;border:2px solid rgba(255,255,255,.16);border-radius:3px}.cinemas-content{padding:36px 0 70px}.list-heading{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:24px}.list-heading>div>span{color:var(--orange);font-size:8px;letter-spacing:1.8px}.list-heading h2{margin:7px 0 0;font-size:28px}.list-heading>span{color:var(--muted);font-size:10px}.cinema-grid{display:grid;grid-template-columns:repeat(2,1fr);gap:20px}.cinema-card{overflow:hidden;cursor:pointer;transition:.25s}.cinema-card:hover{transform:translateY(-4px);box-shadow:var(--shadow-md)}.cinema-cover{height:190px;position:relative;overflow:hidden}.cinema-cover img{width:100%;height:100%;object-fit:cover;transition:.4s}.cinema-card:hover .cinema-cover img{transform:scale(1.04)}.cover-shade{position:absolute;inset:0;background:linear-gradient(0deg,rgba(5,10,17,.65),transparent)}.verified{position:absolute;left:14px;top:14px;display:flex;align-items:center;gap:4px;padding:6px 9px;border-radius:7px;color:#fff;background:rgba(9,16,26,.55);backdrop-filter:blur(8px);font-size:8px}.cinema-code{position:absolute;right:14px;bottom:12px;color:rgba(255,255,255,.55);font-family:Consolas,monospace;font-size:8px;letter-spacing:1px}.cinema-body{padding:18px}.cinema-title-row{display:flex;justify-content:space-between;gap:15px}.cinema-title-row h3{margin:0 0 7px;font-size:17px}.cinema-title-row p{margin:0;color:var(--muted);font-size:9px;display:flex;align-items:center;gap:4px}.go-circle{width:32px;height:32px;border-radius:50%;display:grid;place-items:center;background:var(--orange-soft);color:var(--orange);flex-shrink:0}.cinema-data{display:flex;gap:25px;margin:16px 0;padding:13px 0;border-top:1px solid var(--line);border-bottom:1px solid var(--line)}.cinema-data div{display:flex;align-items:baseline;gap:5px}.cinema-data strong{font-size:17px}.cinema-data span{color:var(--muted);font-size:8px}.facility-row{display:flex;gap:6px;flex-wrap:wrap}.facility-row span{padding:4px 7px;background:#f6f3ed;border-radius:5px;color:#777064;font-size:8px}.cinema-bottom{display:flex;justify-content:space-between;align-items:center;margin-top:15px}.cinema-bottom>span{display:flex;align-items:center;gap:4px;color:var(--muted);font-size:8px}.cinema-service{margin-top:38px;padding:28px 30px;display:grid;grid-template-columns:240px 1fr;gap:25px;align-items:center}.cinema-service>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.cinema-service h2{margin:7px 0 0;font-size:21px}.service-items{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}.service-items>div{border-left:1px solid var(--line);padding-left:16px}.service-items .el-icon{color:var(--orange);font-size:18px}.service-items strong{display:block;font-size:11px;margin-top:8px}.service-items p{color:var(--muted);font-size:8px;margin:5px 0 0}
@media(max-width:850px){.cinema-hero-inner{display:block}.cinema-search{width:100%;margin-top:25px}.cinema-grid{grid-template-columns:1fr}.cinema-service{grid-template-columns:1fr}.service-items{grid-template-columns:repeat(2,1fr)}.service-items>div{padding:10px 0 10px 12px}}@media(max-width:480px){.cinema-cover{height:170px}.cinema-data{gap:17px}.facility-row span:nth-child(n+4){display:none}}
</style>
