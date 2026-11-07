<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { auth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const movie = ref({})
const favorite = ref(false)
const favoriteCount = ref(0)
const trailerVisible = ref(false)
const reviewVisible = ref(false)
const myReview = ref({ score: 8, content: '' })
const showGroups = computed(() => {
  const groups = new Map()
  for (const item of movie.value.showings || []) {
    if (!groups.has(item.cinemaName)) groups.set(item.cinemaName, { name: item.cinemaName, address: item.cinemaAddress, cinemaId: item.cinemaId, shows: [] })
    groups.get(item.cinemaName).shows.push(item)
  }
  return [...groups.values()]
})
async function load() {
  loading.value = true
  try {
    const { data } = await http.get(`/public/movies/${route.params.id}`)
    movie.value = data
    favoriteCount.value = data.favoriteCount || 0
    if (auth.state.token) {
      const [favRes, reviewRes] = await Promise.allSettled([http.get(`/user/favorites/${data.id}`), http.get(`/user/movies/${data.id}/review`)])
      if (favRes.status === 'fulfilled') favorite.value = favRes.value.data.favorite
      if (reviewRes.status === 'fulfilled' && reviewRes.value.data.score) myReview.value = { score: Number(reviewRes.value.data.score), content: reviewRes.value.data.content || '' }
    }
  } finally { loading.value = false }
}
function requireLogin() {
  if (auth.state.token) return true
  router.push({ path: '/login', query: { redirect: route.fullPath } })
  return false
}
async function toggleFavorite() {
  if (!requireLogin()) return
  if (favorite.value) {
    const { data } = await http.delete(`/user/favorites/${movie.value.id}`)
    favorite.value = false; favoriteCount.value = data.count
    ElMessage.success('已取消收藏')
  } else {
    const { data } = await http.post(`/user/favorites/${movie.value.id}`)
    favorite.value = true; favoriteCount.value = data.count
    ElMessage.success('已加入我的收藏')
  }
}
async function submitReview() {
  if (!requireLogin()) return
  const { data } = await http.post(`/user/movies/${movie.value.id}/reviews`, myReview.value)
  movie.value = data
  reviewVisible.value = false
  ElMessage.success('评分已发布')
}
function book(showing) {
  if (!requireLogin()) return
  router.push(`/booking/${showing.id}`)
}
function scrollToShowings() { document.querySelector('#showings')?.scrollIntoView({ behavior: 'smooth' }) }
function scoreWidth(score) { return `${Number(score || 0) * 10}%` }
watch(() => route.params.id, load)
onMounted(load)
</script>

<template>
  <div v-loading="loading" class="detail-page">
    <section class="detail-hero">
      <div class="detail-bg" :style="{ backgroundImage: `url(${movie.backdrop || movie.poster})` }"></div>
      <div class="detail-shade"></div>
      <div class="page detail-inner">
        <div class="detail-poster"><img :src="movie.poster" :alt="movie.title" /><span v-if="movie.status === 'SOON'" class="coming-label">即将上映</span></div>
        <div class="detail-copy">
          <div class="detail-breadcrumb"><router-link to="/movies">电影</router-link><el-icon><ArrowRight /></el-icon><span>{{ movie.title }}</span></div>
          <h1>{{ movie.title }}</h1>
          <div class="detail-en">{{ movie.englishTitle }}</div>
          <div class="detail-tags"><span v-for="genre in movie.genres" :key="genre.id" class="tag">{{ genre.name }}</span></div>
          <div class="detail-facts"><span>{{ movie.duration }} 分钟</span><span>{{ movie.releaseDate }}</span><span>{{ movie.director }} 导演</span><span>中国大陆</span></div>
          <p class="detail-synopsis">{{ movie.synopsis }}</p>
          <div class="detail-actions">
            <el-button v-if="movie.status === 'NOW' && movie.showings?.length" type="primary" size="large" round @click="scrollToShowings">选择场次 <el-icon><Tickets /></el-icon></el-button>
            <el-button v-else size="large" round @click="toggleFavorite">{{ favorite ? '已收藏' : '想看这部电影' }}</el-button>
            <el-button size="large" round :icon="VideoPlay" @click="trailerVisible = true">播放预告</el-button>
            <button class="icon-action" :class="{ active: favorite }" @click="toggleFavorite"><el-icon><StarFilled /></el-icon><span>{{ favoriteCount }}</span></button>
          </div>
        </div>
        <div class="detail-score-card">
          <span>兔南观众评分</span>
          <strong>{{ Number(movie.score || 0).toFixed(1) }}</strong>
          <div class="detail-stars"><i v-for="n in 10" :key="n" :class="{ on: n <= Math.round(movie.score) }"></i></div>
          <small>{{ movie.reviews?.length || 0 }} 人评分</small>
          <el-button round @click="reviewVisible = true">写短评</el-button>
        </div>
      </div>
    </section>

    <div class="page detail-body">
      <main>
        <section class="content-section">
          <div class="content-heading"><span>01</span><h2>演职人员</h2><small>CAST & CREW</small></div>
          <div class="cast-row">
            <div v-for="person in movie.cast" :key="person.id" class="cast-card"><div class="cast-avatar">{{ person.name.slice(0, 1) }}</div><strong>{{ person.name }}</strong><span>饰 {{ person.roleName }}</span></div>
            <div class="cast-card director"><div class="cast-avatar">导</div><strong>{{ movie.director }}</strong><span>导演</span></div>
          </div>
        </section>

        <section id="showings" class="content-section">
          <div class="content-heading"><span>02</span><h2>选择场次</h2><small>SELECT SHOWTIME</small><span class="heading-side">南京 · 全部影院</span></div>
          <div v-if="showGroups.length" class="cinema-showtimes">
            <div v-for="group in showGroups" :key="group.name" class="showtime-cinema surface">
              <div class="showtime-top"><div><h3>{{ group.name }}</h3><p><el-icon><Location /></el-icon>{{ group.address }}</p></div><router-link :to="`/cinemas/${group.cinemaId}`">影院详情 <el-icon><ArrowRight /></el-icon></router-link></div>
              <div class="showtime-list">
                <button v-for="show in group.shows" :key="show.id" class="showtime-item" @click="book(show)">
                  <span class="show-time">{{ show.startTime?.slice(11, 16) }}</span>
                  <span><b>{{ show.version }}</b><small>{{ show.language }} · {{ show.roomName }}</small></span>
                  <span class="show-price">¥{{ Number(show.price).toFixed(1) }}</span>
                  <span class="show-arrow"><el-icon><Right /></el-icon></span>
                </button>
              </div>
            </div>
          </div>
          <div v-else class="no-showing surface"><el-icon><Calendar /></el-icon><h3>{{ movie.status === 'SOON' ? '影片尚未上映' : '暂无可售场次' }}</h3><p>{{ movie.status === 'SOON' ? '先收藏，上映后第一时间提醒你' : '请稍后查看其他影院排片' }}</p><el-button v-if="movie.status === 'SOON'" type="primary" round @click="toggleFavorite">加入想看</el-button></div>
        </section>

        <section class="content-section">
          <div class="content-heading"><span>03</span><h2>观众短评</h2><small>REVIEWS</small><el-button class="heading-side" text @click="reviewVisible = true">写短评 <el-icon><EditPen /></el-icon></el-button></div>
          <div v-if="movie.reviews?.length" class="review-list">
            <article v-for="review in movie.reviews" :key="review.id" class="review-card surface">
              <div class="review-user"><span class="avatar">{{ review.userName?.slice(0, 1) }}</span><div><strong>{{ review.userName }}</strong><small>{{ review.createdAt }} · 已购票</small></div><b>{{ Number(review.score).toFixed(1) }}</b></div>
              <p>{{ review.content || '这部电影值得走进影院。' }}</p>
              <div class="review-foot"><span><el-icon><Pointer /></el-icon> 有帮助</span><span>来自兔南影迷</span></div>
            </article>
          </div>
          <div v-else class="empty-state surface"><el-icon><ChatLineSquare /></el-icon><h3>还没有短评</h3><p>成为第一个分享观影感受的人</p></div>
        </section>
      </main>
      <aside class="detail-aside">
        <div class="aside-card surface"><div class="aside-title">观影信息</div><div class="info-row"><span>类型</span><b>{{ movie.genreNames?.split(',').join(' / ') }}</b></div><div class="info-row"><span>片长</span><b>{{ movie.duration }} 分钟</b></div><div class="info-row"><span>上映</span><b>{{ movie.releaseDate }}</b></div><div class="info-row"><span>累计票房</span><b class="text-orange">¥{{ Number(movie.boxOffice || 0).toFixed(1) }} 万</b></div></div>
        <div class="aside-card quote-card"><span>“</span><p>电影是每秒二十四格的真理。</p><small>— 让-吕克·戈达尔</small></div>
        <div class="aside-card tip-card"><el-icon><Bell /></el-icon><div><strong>想第一时间购票？</strong><p>收藏本片，热门场次开售时不错过。</p></div></div>
      </aside>
    </div>

    <el-dialog v-model="trailerVisible" title="电影预告" width="min(900px, 92vw)" destroy-on-close>
      <video v-if="trailerVisible" :src="movie.trailerUrl" controls autoplay style="width:100%;border-radius:12px;background:#000"></video>
    </el-dialog>
    <el-dialog v-model="reviewVisible" title="写下你的观影评分" width="min(520px, 92vw)">
      <div class="review-form"><div class="review-score"><span>综合评分</span><strong>{{ Number(myReview.score).toFixed(1) }}</strong></div><el-slider v-model="myReview.score" :min="0" :max="10" :step="0.5" :show-tooltip="false" /><el-input v-model="myReview.content" type="textarea" :rows="4" maxlength="300" show-word-limit placeholder="这部电影给你留下了什么？" /><el-button type="primary" class="w-full" size="large" @click="submitReview">发布评分</el-button></div>
    </el-dialog>
  </div>
</template>

<style scoped>
.detail-page{min-height:70vh}.detail-hero{position:relative;background:#0a111b;color:#fff;overflow:hidden}.detail-bg{position:absolute;inset:0;background-size:cover;background-position:center;filter:blur(2px);transform:scale(1.03)}.detail-shade{position:absolute;inset:0;background:linear-gradient(90deg,rgba(7,12,20,.97),rgba(7,12,20,.72) 60%,rgba(7,12,20,.9)),linear-gradient(0deg,rgba(7,12,20,.9),transparent 50%)}.detail-inner{position:relative;z-index:2;min-height:510px;display:grid;grid-template-columns:220px 1fr 185px;gap:40px;align-items:center;padding-top:45px;padding-bottom:50px}.detail-poster{position:relative}.detail-poster img{width:220px;aspect-ratio:2/3;object-fit:cover;border-radius:12px;box-shadow:0 30px 65px rgba(0,0,0,.55)}.coming-label{position:absolute;left:12px;top:12px;background:var(--orange);padding:6px 10px;border-radius:6px;font-size:10px}.detail-breadcrumb{display:flex;gap:7px;align-items:center;color:rgba(255,255,255,.38);font-size:10px}.detail-copy h1{margin:22px 0 2px;font-size:clamp(48px,5.6vw,78px);letter-spacing:-4px;line-height:1}.detail-en{color:rgba(255,255,255,.33);font-size:9px;letter-spacing:3.5px}.detail-tags{display:flex;gap:7px;margin-top:22px}.detail-facts{display:flex;gap:13px;margin-top:17px;font-size:11px;color:rgba(255,255,255,.55);flex-wrap:wrap}.detail-facts span+span{border-left:1px solid rgba(255,255,255,.19);padding-left:13px}.detail-synopsis{max-width:650px;color:rgba(255,255,255,.62);font-size:12px;line-height:1.9;margin:19px 0 25px}.detail-actions{display:flex;gap:9px;align-items:center}.detail-actions .el-button+.el-button{margin-left:0}.icon-action{border:1px solid rgba(255,255,255,.2);background:rgba(255,255,255,.08);color:#fff;width:38px;height:38px;border-radius:50%;display:grid;place-items:center;cursor:pointer;position:relative}.icon-action span{position:absolute;top:41px;font-size:8px;opacity:.5}.icon-action.active{color:#ffbe6e;border-color:#ffbe6e}.detail-score-card{border-left:1px solid rgba(255,255,255,.15);display:grid;gap:6px;padding-left:28px}.detail-score-card>span{color:rgba(255,255,255,.4);font-size:9px}.detail-score-card strong{font-size:48px;color:#ffbd6b;line-height:1.1}.detail-stars{display:flex;gap:3px}.detail-stars i{width:10px;height:4px;background:rgba(255,255,255,.14);border-radius:2px}.detail-stars i.on{background:#ffbd6b}.detail-score-card small{color:rgba(255,255,255,.35);font-size:8px;margin-bottom:10px}.detail-score-card .el-button{color:#fff;background:rgba(255,255,255,.1);border-color:rgba(255,255,255,.15)}
.detail-body{display:grid;grid-template-columns:1fr 280px;gap:45px;padding-top:45px;padding-bottom:60px}.content-section{margin-bottom:48px}.content-heading{display:flex;align-items:center;gap:12px;margin-bottom:20px}.content-heading>span:first-child{color:var(--orange);font-size:10px;font-family:Consolas,monospace}.content-heading h2{font-size:21px;margin:0}.content-heading small{color:#b0a99f;font-size:8px;letter-spacing:1.5px}.heading-side{margin-left:auto;display:flex;align-items:center;gap:5px;color:var(--muted)!important;font-size:10px}.cast-row{display:grid;grid-template-columns:repeat(6,1fr);gap:13px}.cast-card{text-align:center;background:#fff;border:1px solid var(--line);border-radius:12px;padding:16px 7px}.cast-avatar{width:58px;height:58px;border-radius:50%;display:grid;place-items:center;margin:0 auto 10px;background:linear-gradient(145deg,#e8eef7,#bdcadb);color:#4c6179;font-size:20px;font-weight:800}.cast-card:nth-child(3n+2) .cast-avatar{background:linear-gradient(145deg,#f4e6d7,#ddb99e);color:#705542}.cast-card:nth-child(3n) .cast-avatar{background:linear-gradient(145deg,#e4e8d8,#bac4a0);color:#586043}.cast-card strong{display:block;font-size:11px}.cast-card span{display:block;margin-top:5px;color:var(--muted);font-size:8px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.cast-card.director .cast-avatar{background:#152238;color:#fff}
.cinema-showtimes{display:grid;gap:13px}.showtime-cinema{padding:18px}.showtime-top{display:flex;justify-content:space-between;gap:20px;align-items:flex-start}.showtime-top h3{margin:0 0 7px;font-size:15px}.showtime-top p{margin:0;color:var(--muted);font-size:9px;display:flex;align-items:center;gap:4px}.showtime-top a{font-size:9px;color:var(--muted);display:flex;align-items:center;gap:3px}.showtime-list{display:flex;flex-wrap:wrap;gap:8px;margin-top:16px;padding-top:14px;border-top:1px solid var(--line)}.showtime-item{display:grid;grid-template-columns:55px 1fr auto 18px;align-items:center;gap:10px;border:1px solid #eae6de;border-radius:9px;padding:8px 12px;background:#fff;cursor:pointer;text-align:left;min-width:245px;transition:.18s}.showtime-item:hover{border-color:#ffac99;background:#fff8f5;transform:translateY(-1px)}.show-time{font-size:17px;font-weight:800}.showtime-item b{display:block;font-size:10px}.showtime-item small{display:block;color:var(--muted);font-size:8px;margin-top:3px}.show-price{color:var(--orange);font-size:13px;font-weight:800}.show-arrow{color:#bbb}.no-showing{text-align:center;padding:55px;color:var(--muted)}.no-showing .el-icon{font-size:39px;color:#c7c0b6}.no-showing h3{color:var(--ink);margin:13px 0 6px}.no-showing p{font-size:11px;margin:0 0 18px}
.review-list{display:grid;gap:11px}.review-card{padding:17px}.review-user{display:grid;grid-template-columns:35px 1fr auto;align-items:center;gap:10px}.review-user .avatar{width:35px;height:35px;font-size:12px}.review-user strong{display:block;font-size:11px}.review-user small{display:block;color:var(--muted);font-size:8px;margin-top:4px}.review-user>b{color:var(--orange);font-size:18px}.review-card>p{font-size:11px;line-height:1.8;color:#596476;margin:14px 0}.review-foot{display:flex;justify-content:space-between;color:#aaa39a;font-size:8px}.review-foot span{display:flex;gap:4px;align-items:center}
.detail-aside{display:grid;gap:14px;align-content:start;position:sticky;top:96px}.aside-card{padding:18px}.aside-title{font-size:13px;font-weight:800;padding-bottom:12px;border-bottom:1px solid var(--line);margin-bottom:10px}.info-row{display:flex;justify-content:space-between;padding:9px 0;font-size:10px}.info-row span{color:var(--muted)}.info-row b{font-weight:600;text-align:right;max-width:170px}.quote-card{background:#152238;color:#fff;min-height:150px}.quote-card>span{font-size:38px;color:var(--orange);line-height:1}.quote-card p{font-size:13px;line-height:1.7;margin:0 0 12px}.quote-card small{color:rgba(255,255,255,.4);font-size:8px}.tip-card{display:flex;gap:11px;background:#fff9ee;border-color:#f1dfc2}.tip-card>.el-icon{color:#e7a747;font-size:20px}.tip-card strong{font-size:11px}.tip-card p{font-size:9px;line-height:1.6;color:var(--muted);margin:5px 0 0}.review-form{display:grid;gap:15px}.review-score{display:flex;justify-content:space-between;align-items:center}.review-score span{font-size:12px}.review-score strong{font-size:29px;color:var(--orange)}
@media(max-width:1050px){.detail-inner{grid-template-columns:185px 1fr;gap:28px}.detail-poster img{width:185px}.detail-score-card{display:none}.cast-row{grid-template-columns:repeat(4,1fr)}}@media(max-width:800px){.detail-body{grid-template-columns:1fr}.detail-aside{display:none}.detail-inner{grid-template-columns:130px 1fr;gap:20px;min-height:430px}.detail-poster img{width:130px}.detail-copy h1{font-size:37px;letter-spacing:-2px}.detail-facts{display:none}.detail-synopsis{-webkit-line-clamp:3;display:-webkit-box;-webkit-box-orient:vertical;overflow:hidden}.detail-actions .el-button:nth-child(2){display:none}.detail-actions .icon-action{display:none}.cast-row{grid-template-columns:repeat(3,1fr)}.showtime-item{min-width:100%}}@media(max-width:520px){.detail-inner{display:block;padding-top:30px}.detail-poster{display:none}.detail-copy h1{font-size:44px}.detail-tags{margin-top:17px}.detail-actions .el-button:first-child{width:100%}.showtime-list{display:grid}.cast-row{gap:7px}}
</style>
