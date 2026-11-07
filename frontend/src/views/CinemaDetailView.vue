<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const cinema = ref({})
const selectedDate = ref('')
const dates = computed(() => [...new Set((cinema.value.allShowings || []).map(item => item.startTime.slice(0, 10)))])
const shows = computed(() => (cinema.value.allShowings || []).filter(item => item.startTime.startsWith(selectedDate.value)))
const grouped = computed(() => {
  const map = new Map()
  for (const show of shows.value) {
    if (!map.has(show.movieId)) map.set(show.movieId, { movieId: show.movieId, movieTitle: show.movieTitle, poster: show.poster, duration: show.duration, shows: [] })
    map.get(show.movieId).shows.push(show)
  }
  return [...map.values()]
})
const weekday = (date) => ['周日','周一','周二','周三','周四','周五','周六'][new Date(date).getDay()]
async function load() {
  loading.value = true
  try { const { data } = await http.get(`/public/cinemas/${route.params.id}`); cinema.value = data; selectedDate.value = dates.value[0] || '' } finally { loading.value = false }
}
function book(show) { router.push(`/booking/${show.id}`) }
onMounted(load)
</script>

<template>
  <div v-loading="loading" class="cinema-detail">
    <section class="cinema-profile">
      <div class="profile-bg" :style="{ backgroundImage: `url(${cinema.cover})` }"></div><div class="profile-shade"></div>
      <div class="page profile-inner"><div class="profile-main"><div class="profile-label"><el-icon><CircleCheckFilled /></el-icon> 平台认证影院 · TUNAN CINEMA</div><h1>{{ cinema.name }}</h1><p class="profile-address"><el-icon><Location /></el-icon>{{ cinema.address }}</p><div class="profile-tags"><span v-for="item in cinema.facilities?.split(',')" :key="item">{{ item }}</span></div></div><div class="profile-stats"><div><strong>{{ cinema.roomCount }}</strong><span>影厅</span></div><div><strong>{{ cinema.movieCount }}</strong><span>在映电影</span></div><div><strong>4.9</strong><span>观众评分</span></div></div></div>
    </section>
    <div class="page detail-content">
      <div class="cinema-info-strip surface"><div><el-icon><Phone /></el-icon><span>联系电话</span><strong>{{ cinema.phone || '025-88886666' }}</strong></div><div><el-icon><Clock /></el-icon><span>营业时间</span><strong>09:00 - 24:00</strong></div><div><el-icon><Van /></el-icon><span>停车服务</span><strong>商场地下停车场</strong></div><div><el-icon><Service /></el-icon><span>特色服务</span><strong>{{ cinema.facilities?.split(',').slice(0,2).join(' · ') }}</strong></div></div>
      <section class="schedule-section">
        <div class="schedule-head"><div><span>NOW SHOWING</span><h2>今日排片</h2></div><div class="date-switch"><button v-for="date in dates" :key="date" :class="{ active: selectedDate === date }" @click="selectedDate = date"><small>{{ weekday(date) }}</small><b>{{ date.slice(5).replace('-', '/') }}</b></button></div></div>
        <div v-if="grouped.length" class="movie-schedules">
          <article v-for="group in grouped" :key="group.movieId" class="movie-schedule surface"><router-link :to="`/movies/${group.movieId}`" class="schedule-poster"><img :src="group.poster" /><div><strong>{{ group.movieTitle }}</strong><span>{{ group.duration }} 分钟</span></div></router-link><div class="schedule-times"><button v-for="show in group.shows" :key="show.id" @click="book(show)"><span>{{ show.startTime.slice(11,16) }}</span><b>{{ show.version }}</b><small>{{ show.roomName }} · ¥{{ Number(show.price).toFixed(1) }}</small></button></div></article>
        </div>
        <div v-else class="empty-state surface"><el-icon><Calendar /></el-icon><h3>这一天暂无排片</h3><p>请切换其他日期查看</p></div>
      </section>
      <section class="cinema-about surface"><div class="about-label"><span>ABOUT THE SPACE</span><h2>关于这家影院</h2></div><div><p>{{ cinema.intro }}</p><div class="about-facilities"><span v-for="item in cinema.facilities?.split(',')" :key="item"><el-icon><Check /></el-icon>{{ item }}</span></div></div></section>
    </div>
  </div>
</template>

<style scoped>
.cinema-detail{min-height:70vh}.cinema-profile{position:relative;background:#0a121d;color:#fff;overflow:hidden}.profile-bg{position:absolute;inset:0;background-size:cover;background-position:center;filter:blur(1px)}.profile-shade{position:absolute;inset:0;background:linear-gradient(90deg,rgba(6,12,20,.95),rgba(6,12,20,.45)),linear-gradient(0deg,#0a121d,transparent 60%)}.profile-inner{position:relative;z-index:2;min-height:340px;display:flex;justify-content:space-between;align-items:flex-end;gap:30px;padding-top:50px;padding-bottom:42px}.profile-label{display:flex;align-items:center;gap:6px;color:#ffb86b;font-size:9px;letter-spacing:1.5px}.profile-main h1{margin:16px 0 10px;font-size:clamp(38px,5vw,65px);letter-spacing:-3px}.profile-address{display:flex;align-items:center;gap:5px;color:rgba(255,255,255,.55);font-size:11px;margin:0}.profile-tags{display:flex;gap:7px;margin-top:20px;flex-wrap:wrap}.profile-tags span{font-size:8px;padding:5px 8px;border:1px solid rgba(255,255,255,.18);border-radius:5px;color:rgba(255,255,255,.56)}.profile-stats{display:flex;border:1px solid rgba(255,255,255,.14);background:rgba(255,255,255,.06);border-radius:12px;backdrop-filter:blur(9px)}.profile-stats div{display:grid;gap:4px;padding:16px 25px;text-align:center}.profile-stats div+div{border-left:1px solid rgba(255,255,255,.12)}.profile-stats strong{font-size:24px}.profile-stats span{color:rgba(255,255,255,.4);font-size:8px}.detail-content{padding:24px 0 65px}.cinema-info-strip{display:grid;grid-template-columns:repeat(4,1fr);padding:18px 20px;margin-top:-1px}.cinema-info-strip>div{display:grid;grid-template-columns:28px 1fr;column-gap:10px;align-items:center;padding:0 18px}.cinema-info-strip>div+div{border-left:1px solid var(--line)}.cinema-info-strip .el-icon{grid-row:1/3;color:var(--orange);font-size:19px}.cinema-info-strip span{color:var(--muted);font-size:8px}.cinema-info-strip strong{font-size:10px;margin-top:2px}.schedule-section{margin-top:38px}.schedule-head{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.schedule-head>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.schedule-head h2{margin:7px 0 0;font-size:27px}.date-switch{display:flex;gap:6px}.date-switch button{border:1px solid var(--line);background:#fff;border-radius:8px;padding:7px 10px;min-width:57px;cursor:pointer;display:grid;gap:3px}.date-switch small{color:var(--muted);font-size:8px}.date-switch b{font-size:11px}.date-switch button.active{border-color:var(--orange);background:var(--orange-soft);color:var(--orange)}.movie-schedules{display:grid;gap:12px}.movie-schedule{display:grid;grid-template-columns:260px 1fr;gap:24px;padding:15px;align-items:center}.schedule-poster{display:flex;gap:13px;align-items:center}.schedule-poster img{width:57px;height:81px;object-fit:cover;border-radius:6px}.schedule-poster strong{display:block;font-size:13px}.schedule-poster span{display:block;color:var(--muted);font-size:8px;margin-top:5px}.schedule-times{display:flex;gap:8px;flex-wrap:wrap}.schedule-times button{display:grid;gap:2px;border:1px solid var(--line);background:#fff;border-radius:8px;padding:7px 12px;text-align:left;cursor:pointer;min-width:118px}.schedule-times button:hover{border-color:#ffb19f;background:#fff8f5}.schedule-times span{font-size:15px;font-weight:800}.schedule-times b{font-size:8px;color:#5e6878}.schedule-times small{color:#a19a8f;font-size:7px}.cinema-about{margin-top:36px;padding:26px 30px;display:grid;grid-template-columns:250px 1fr;gap:25px}.about-label>span{color:var(--orange);font-size:8px;letter-spacing:2px}.about-label h2{margin:7px 0 0;font-size:21px}.cinema-about p{color:var(--muted);font-size:11px;line-height:1.9;margin:0}.about-facilities{display:flex;gap:8px;flex-wrap:wrap;margin-top:15px}.about-facilities span{display:flex;align-items:center;gap:4px;font-size:9px;background:#f7f4ee;padding:5px 8px;border-radius:5px;color:#6c746f}.about-facilities .el-icon{color:var(--green)}
@media(max-width:850px){.profile-stats{display:none}.cinema-info-strip{grid-template-columns:repeat(2,1fr);gap:15px 0}.cinema-info-strip>div:nth-child(3){border:0}.movie-schedule{grid-template-columns:1fr;gap:15px}.cinema-about{grid-template-columns:1fr}.schedule-head{display:block}.date-switch{margin-top:15px;overflow:auto;padding-bottom:5px}}@media(max-width:520px){.cinema-info-strip{padding:15px 6px}.cinema-info-strip>div{padding:0 8px}.profile-inner{min-height:290px}.profile-main h1{font-size:37px}.movie-schedule{padding:12px}.schedule-times{display:grid;grid-template-columns:1fr 1fr}.schedule-times button{min-width:auto}}
</style>
