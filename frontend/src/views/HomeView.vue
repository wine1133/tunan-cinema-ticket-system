<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'
import MovieCard from '@/components/MovieCard.vue'
import SectionTitle from '@/components/SectionTitle.vue'

const router = useRouter()
const loading = ref(true)
const data = ref({ now: [], soon: [], boxOffice: [], scoreRank: [], notices: [], stats: {} })
const heroIndex = ref(0)
const trailerVisible = ref(false)
const trailerUrl = ref('')
const videoMuted = ref(false)
const videoProgress = ref(0)
const heroVideo = ref(null)
let timer

const videoSlide = {
  id: 'winter-video',
  type: 'video',
  title: '你是我今生最美的相遇',
  englishTitle: 'TUNAN CINEMA · A BEAUTIFUL ENCOUNTER',
  genreNames: '冬日特别企划',
  duration: 76,
  clipDuration: 76,
  director: '兔南电影',
  synopsis: '人海茫茫，相遇本身就是一场礼物。兔南电影冬日特别企划，用一段光影，陪你重温那些温柔而珍贵的瞬间。',
  poster: '/videos/tunan-special-poster.jpg',
  backdrop: '/videos/tunan-special-poster.jpg',
  video: '/videos/tunan-special-76s.mp4'
}
const featured = computed(() => data.value.now.slice(0, 3))
const slides = computed(() => [videoSlide, ...featured.value])
const hero = computed(() => slides.value[heroIndex.value] || videoSlide)
const isVideoHero = computed(() => hero.value.type === 'video')
const ticker = computed(() => {
  const stats = data.value.stats || {}
  return [
    `今日票房 ¥${Number(stats.todayBoxOffice || 0).toFixed(2)}`,
    `今日已有 ${stats.todayOrders || 0} 笔订单`,
    `全城 ${stats.showingCount || 0} 个场次正在售票`,
    `${stats.userCount || 0} 位影迷已加入兔南电影`
  ]
})
async function load() {
  loading.value = true
  try {
    const res = await http.get('/public/home')
    data.value = res.data
  } finally { loading.value = false }
}
function selectHero(index) { heroIndex.value = index }
function nextHero() { heroIndex.value = (heroIndex.value + 1) % slides.value.length }
function openTrailer() {
  if (!hero.value.trailerUrl) return
  trailerUrl.value = hero.value.trailerUrl
  trailerVisible.value = true
}
function updateVideoProgress(event) {
  const video = event.target
  const limit = Number(hero.value.clipDuration || video.duration || 0)
  if (limit) videoProgress.value = Math.min(100, (video.currentTime / limit) * 100)
  if (hero.value.clipDuration && video.currentTime >= hero.value.clipDuration) {
    video.pause()
    nextHero()
  }
}
function toggleVideoMute() {
  window.removeEventListener('pointerdown', unlockVideoSound, true)
  window.removeEventListener('keydown', unlockVideoSound, true)
  videoMuted.value = !videoMuted.value
  if (heroVideo.value) {
    heroVideo.value.muted = videoMuted.value
    heroVideo.value.volume = 1
  }
}
async function playHeroVideo() {
  const video = heroVideo.value
  if (!video) return
  video.volume = 1
  video.muted = videoMuted.value
  try {
    await video.play()
  } catch {
    videoMuted.value = true
    video.muted = true
    await video.play().catch(() => {})
  }
}
function unlockVideoSound(event) {
  if (event?.target?.closest?.('.video-sound')) return
  window.removeEventListener('pointerdown', unlockVideoSound, true)
  window.removeEventListener('keydown', unlockVideoSound, true)
  if (!isVideoHero.value) return
  videoMuted.value = false
  const video = heroVideo.value
  if (!video) return
  video.muted = false
  video.volume = 1
  video.play().catch(() => {})
}
onMounted(() => {
  load()
  nextTick(playHeroVideo)
  window.addEventListener('pointerdown', unlockVideoSound, true)
  window.addEventListener('keydown', unlockVideoSound, true)
  timer = setInterval(() => {
    if (!isVideoHero.value && slides.value.length) nextHero()
  }, 7000)
})
onBeforeUnmount(() => {
  clearInterval(timer)
  window.removeEventListener('pointerdown', unlockVideoSound, true)
  window.removeEventListener('keydown', unlockVideoSound, true)
})
</script>

<template>
  <div class="home">
    <section class="hero-shell">
      <div class="hero-backdrop" :style="{ backgroundImage: `url(${hero.backdrop})` }"></div>
      <div class="hero-vignette"></div>
      <div class="hero-orbit orbit-a"></div><div class="hero-orbit orbit-b"></div>
      <div class="page-wide hero-inner" :class="{ 'video-mode': isVideoHero }">
        <div class="hero-copy">
          <div class="hero-eyebrow"><span class="live-dot"></span> {{ isVideoHero ? 'TUNAN ORIGINAL · 冬日特别企划' : 'NOW SHOWING · 正在热映' }}</div>
          <div :key="hero.id" class="hero-content">
              <h1 :class="{ 'video-title': isVideoHero }">{{ hero.title }}</h1>
              <div class="hero-en">{{ hero.englishTitle }}</div>
              <div class="hero-meta">
                <template v-if="isVideoHero">
                  <span class="hero-score">1:16</span>
                  <span>冬日先导片</span>
                  <span>前1分16秒 · 有声音</span>
                </template>
                <template v-else>
                  <span class="hero-score">{{ hero.score }}</span>
                  <span>{{ hero.genreNames?.split(',').join(' / ') }}</span>
                  <span>{{ hero.duration }} 分钟</span>
                  <span>{{ hero.director }} 导演</span>
                </template>
              </div>
              <p>{{ hero.synopsis }}</p>
              <div class="hero-actions">
                <el-button type="primary" size="large" round @click="isVideoHero ? router.push('/movies') : router.push(`/movies/${hero.id}`)">{{ isVideoHero ? '立即选片' : '立即购票' }} <el-icon><Tickets /></el-icon></el-button>
                <el-button class="trailer-btn" size="large" round @click="isVideoHero ? nextHero() : openTrailer()"><el-icon><ArrowRight v-if="isVideoHero" /><VideoPlay v-else /></el-icon> {{ isVideoHero ? '跳过视频' : '播放预告' }}</el-button>
              </div>
            </div>
        </div>
        <div v-if="isVideoHero" :key="hero.id" class="hero-poster-card video-card">
          <img class="video-ambient" :src="hero.poster" alt="" aria-hidden="true" />
          <video ref="heroVideo" :src="hero.video" :poster="hero.poster" :muted="videoMuted" autoplay preload="auto" playsinline @loadedmetadata="playHeroVideo" @canplay="playHeroVideo" @ended="nextHero" @timeupdate="updateVideoProgress"></video>
          <span class="video-label"><i></i> 1分16秒冬日先导片</span>
          <button class="video-sound" @click="toggleVideoMute">{{ videoMuted ? '开启声音' : '静音' }}</button>
          <div class="video-progress"><i :style="{ width: `${videoProgress}%` }"></i></div>
        </div>
        <router-link v-else :to="`/movies/${hero.id}`" class="hero-poster-card">
          <img :src="hero.poster" :alt="hero.title" />
          <span>本周推荐 · 正在热映</span>
        </router-link>
        <div class="hero-side">
          <div class="hero-index"><span>FEATURED</span><strong>{{ String(heroIndex + 1).padStart(2, '0') }}</strong><small>/ {{ String(slides.length).padStart(2, '0') }}</small></div>
          <div class="hero-thumbs">
            <button v-for="(slide, index) in slides" :key="slide.id" :class="{ active: heroIndex === index }" @click="selectHero(index)">
              <span class="thumb-media"><img :src="slide.poster" :alt="slide.title" /><i v-if="slide.type === 'video'">▶</i></span>
              <span>{{ slide.title }}</span>
            </button>
          </div>
        </div>
      </div>
      <div class="page-wide ticker">
        <span class="ticker-tag">TUNAN LIVE</span>
        <div class="ticker-track"><span v-for="item in ticker" :key="item">{{ item }} <i>✦</i></span></div>
        <span class="ticker-date">南京 · 11.05</span>
      </div>
    </section>

    <div class="page section-space">
      <div class="discover-head">
        <SectionTitle kicker="CURATED FOR YOU" title="正在热映" subtitle="此刻值得走进影院的 8 部电影" more="查看全部" to="/movies" />
        <div class="head-stat"><strong>{{ data.stats.showingCount || 0 }}</strong><span>个场次<br />正在售票</span></div>
      </div>
      <div v-loading="loading" class="home-movies">
        <div class="movie-grid main-grid"><MovieCard v-for="movie in data.now.slice(0, 8)" :key="movie.id" :movie="movie" /></div>
        <aside class="home-rank surface">
          <div class="rank-head"><span>总票房 TOP 10</span><router-link to="/rankings">完整榜单 <el-icon><ArrowRight /></el-icon></router-link></div>
          <div class="rank-list">
            <router-link v-for="(movie, index) in data.boxOffice?.slice(0, 10)" :key="movie.id" :to="`/movies/${movie.id}`" class="rank-row">
              <span class="rank-no" :class="{ top: index < 3 }">{{ String(index + 1).padStart(2, '0') }}</span>
              <img :src="movie.poster" :alt="movie.title" />
              <div><strong>{{ movie.title }}</strong><small>{{ movie.genreNames?.split(',')[0] }}</small></div>
              <span class="rank-value">¥{{ Number(movie.boxOffice).toFixed(1) }}万</span>
            </router-link>
          </div>
          <router-link to="/rankings"><el-button class="rank-button" round>查看完整电影榜单</el-button></router-link>
        </aside>
      </div>
    </div>

    <section class="editorial-banner">
      <div class="page editorial-inner">
        <div class="editorial-copy"><div class="editorial-label">TUNAN SELECTED · 本周策划</div><h2>不只是看电影<br />是进入另一个世界</h2><p>从爱情神话的市井浪漫，到熊猫计划的合家欢笑，再到大银幕上的热血与冒险。兔南编辑部为你整理本周最值得体验的故事。</p><el-button round size="large" @click="router.push('/movies')">探索本周片单 <el-icon><Right /></el-icon></el-button></div>
        <div class="editorial-posters">
          <div class="ep ep-1"><img src="/posters/original/panda-plan.jpg" /></div>
          <div class="ep ep-2"><img src="/posters/original/love-myth.jpg" /></div>
          <div class="ep ep-3"><img src="/posters/original/venom-last-dance.jpg" /></div>
        </div>
        <div class="editorial-number">07</div>
      </div>
    </section>

    <div class="page section-space">
      <SectionTitle kicker="COMING SOON" title="即将上映" subtitle="提前收藏，不错过与好故事的相遇" more="全部待映" to="/movies?status=SOON" />
      <div class="movie-grid"><MovieCard v-for="movie in data.soon" :key="movie.id" :movie="movie" show-status /></div>
    </div>

    <div class="page home-bottom section-space">
      <section>
        <SectionTitle kicker="CRITICS & AUDIENCE" title="观众评分榜" more="完整榜单" to="/rankings" />
        <div class="score-list surface">
          <router-link v-for="(movie, index) in data.scoreRank?.slice(0, 6)" :key="movie.id" :to="`/movies/${movie.id}`" class="score-row">
            <span class="score-no">{{ index + 1 }}</span>
            <img :src="movie.poster" />
            <div class="score-info"><strong>{{ movie.title }}</strong><span>{{ movie.genreNames?.split(',').join(' / ') }}</span></div>
            <div class="score-bar"><i :style="{ width: `${movie.score * 10}%` }"></i></div>
            <b>{{ Number(movie.score).toFixed(1) }}</b>
          </router-link>
        </div>
      </section>
      <section>
        <SectionTitle kicker="WHAT'S NEW" title="兔南公告" more="查看全部" to="/notices" />
        <div class="notice-stack">
          <router-link v-for="(notice, index) in data.notices" :key="notice.id" :to="`/notices`" class="notice-card surface">
            <div class="notice-icon"><el-icon><BellFilled /></el-icon></div>
            <div><span>{{ notice.type }} · {{ notice.createdAt?.slice(0, 10) }}</span><h3>{{ notice.title }}</h3><p>{{ notice.content }}</p></div>
            <span class="notice-index">0{{ index + 1 }}</span>
          </router-link>
        </div>
      </section>
    </div>

    <el-dialog v-model="trailerVisible" title="电影预告片" width="min(900px, 92vw)" destroy-on-close>
      <video v-if="trailerVisible" :src="trailerUrl" controls autoplay style="width:100%;border-radius:12px;background:#000"></video>
    </el-dialog>
  </div>
</template>

<style scoped>
.hero-shell { position: relative; min-height: 620px; overflow: hidden; background: #0a111c; color: #fff; }
.hero-backdrop { position:absolute; inset:0; background-position:center; background-size:cover; transition:background-image .6s ease; transform:scale(1.02); animation:heroZoom 20s ease-in-out infinite alternate; }
.hero-vignette { position:absolute; inset:0; background: linear-gradient(90deg,rgba(5,10,17,.97) 0%,rgba(5,10,17,.8) 38%,rgba(5,10,17,.26) 75%,rgba(5,10,17,.45) 100%),linear-gradient(0deg,rgba(5,10,17,.92),transparent 35%); }
.hero-inner { min-height: 564px; position:relative; z-index:2; display:grid; grid-template-columns:minmax(0,1fr) 280px 155px; align-items:center; gap:32px; }
.hero-inner.video-mode { min-height:820px; grid-template-columns:minmax(0,1fr) 155px; }
.hero-inner.video-mode .hero-copy { position:relative; z-index:3; max-width:650px; }
.hero-inner.video-mode .hero-side { position:relative; z-index:3; }
.hero-copy { width:auto; padding:65px 0 45px; }
.hero-poster-card { display:block; width:175px; position:relative; z-index:2; transform:rotate(2deg); transition:.3s ease; }
.hero-poster-card:hover { transform:rotate(0) translateY(-5px); }
.hero-poster-card img, .hero-poster-card video { width:100%; aspect-ratio:2/3; object-fit:cover; border-radius:9px; box-shadow:0 28px 58px rgba(0,0,0,.5); border:1px solid rgba(255,255,255,.16); }
.hero-poster-card.video-card { width:280px; aspect-ratio:9/16; overflow:hidden; transform:rotate(0); background:#080d15; }
.hero-inner.video-mode .hero-poster-card.video-card { position:absolute; z-index:1; top:50%; right:0; width:860px; height:760px; aspect-ratio:auto; border-radius:16px; transform:translateY(-50%); border:1px solid rgba(255,255,255,.08); box-shadow:0 35px 80px rgba(0,0,0,.42); }
.hero-inner.video-mode .video-card::after { content:''; position:absolute; inset:0; z-index:1; pointer-events:none; background:linear-gradient(0deg,rgba(5,10,17,.58),transparent 24%); }
.hero-inner.video-mode .video-card video { position:absolute; top:0; bottom:0; left:50%; width:auto; height:100%; transform:translateX(-50%); object-fit:contain; object-position:center; border:0; border-radius:0; box-shadow:none; }
.video-ambient { position:absolute; inset:-26px; z-index:0; width:calc(100% + 52px); height:calc(100% + 52px); object-fit:cover; filter:blur(28px) brightness(.38) saturate(.8); transform:scale(1.08); opacity:.9; aspect-ratio:auto; border:0; border-radius:0; box-shadow:none; }
.hero-inner.video-mode .video-card video { z-index:1; background:#05070c; }
.hero-inner.video-mode .video-label { z-index:3; left:22px; top:18px; }
.hero-inner.video-mode .video-sound { z-index:3; right:175px; bottom:30px; }
.hero-inner.video-mode .video-progress { z-index:3; left:22px; right:190px; bottom:22px; }
.hero-poster-card.video-card video { height:100%; aspect-ratio:auto; image-rendering:auto; backface-visibility:hidden; }
.video-label { position:absolute; top:10px; left:10px; display:flex!important; align-items:center; gap:5px; margin:0!important; padding:5px 7px; border-radius:5px; background:rgba(8,13,21,.72); backdrop-filter:blur(8px); color:#fff!important; font-size:7px!important; letter-spacing:.5px!important; }
.video-label i { width:5px; height:5px; border-radius:50%; background:#ff6542; box-shadow:0 0 8px #ff6542; }
.video-sound { position:absolute; right:8px; bottom:34px; border:1px solid rgba(255,255,255,.22); background:rgba(8,13,21,.7); color:#fff; border-radius:999px; padding:5px 8px; font-size:7px; cursor:pointer; backdrop-filter:blur(7px); }
.video-progress { position:absolute; left:8px; right:8px; bottom:24px; height:2px; border-radius:2px; background:rgba(255,255,255,.22); overflow:hidden; }
.video-progress i { display:block; height:100%; width:0; background:#ffb86b; transition:width .2s linear; }
.video-title { font-size:clamp(48px,6vw,82px)!important; }
.hero-poster-card span { display:block; margin-top:10px; color:rgba(255,255,255,.42); font-size:8px; letter-spacing:1px; }
.hero-eyebrow { display:flex; align-items:center; gap:9px; color:#ffb86b; font-size:10px; letter-spacing:2.5px; font-weight:700; }
.live-dot { width:7px; height:7px; background:#ff6542; border-radius:50%; box-shadow:0 0 0 5px rgba(255,101,66,.14),0 0 15px #ff6542; animation:pulse 1.8s infinite; }
.hero-content h1 { margin:23px 0 3px; font-size:clamp(56px,7vw,96px); line-height:1; letter-spacing:-5px; }
.hero-en { color:rgba(255,255,255,.36); font-size:11px; letter-spacing:4px; }
.hero-meta { display:flex; align-items:center; gap:15px; flex-wrap:wrap; margin:24px 0 17px; color:rgba(255,255,255,.62); font-size:12px; }
.hero-meta > span + span { border-left:1px solid rgba(255,255,255,.22); padding-left:15px; }
.hero-score { color:#ffbe6e; font-size:23px!important; font-weight:900; border:0!important; padding:0!important; }
.hero-content p { margin:0; max-width:570px; color:rgba(255,255,255,.56); font-size:13px; line-height:1.9; }
.hero-actions { display:flex; gap:10px; margin-top:30px; }
.hero-actions .el-button { min-width:130px; }
.trailer-btn { color:#fff!important; background:rgba(255,255,255,.1)!important; border-color:rgba(255,255,255,.18)!important; backdrop-filter:blur(8px); }
.hero-side { position:relative; z-index:2; align-self:stretch; width:auto; display:flex; flex-direction:column; justify-content:center; align-items:flex-end; gap:30px; }
.hero-index { display:grid; grid-template-columns:auto auto auto; align-items:end; gap:4px; width:100%; justify-content:end; }
.hero-index span { grid-column:1/-1; text-align:right; color:rgba(255,255,255,.35); font-size:8px; letter-spacing:2px; }
.hero-index strong { font-size:47px; font-weight:200; line-height:1; }.hero-index small { color:rgba(255,255,255,.35); font-size:12px; margin-bottom:6px; }
.hero-thumbs { display:grid; grid-template-columns:repeat(2, 58px); gap:10px 8px; justify-content:end; }
.hero-thumbs button { border:0; padding:0; background:none; cursor:pointer; color:rgba(255,255,255,.45); width:58px; text-align:left; }
.hero-thumbs .thumb-media { position:relative; display:block; }
.hero-thumbs img { width:100%; aspect-ratio:2/3; object-fit:contain; background:#101824; border-radius:5px; filter:grayscale(.4); opacity:.55; transition:.3s; }
.hero-thumbs .thumb-media i { position:absolute; inset:0; display:grid; place-items:center; font-style:normal; color:#fff; font-size:12px; text-shadow:0 2px 8px #000; background:rgba(4,8,14,.24); }
.hero-thumbs span { display:block; margin-top:7px; font-size:9px; overflow:hidden; white-space:nowrap; text-overflow:ellipsis; }
.hero-thumbs button.active img { opacity:1; filter:none; box-shadow:0 10px 25px rgba(0,0,0,.4); transform:translateY(-5px); }
.hero-thumbs button.active span { color:#fff; }
.hero-orbit { position:absolute; border:1px solid rgba(255,255,255,.07); border-radius:50%; z-index:1; }.orbit-a{width:560px;height:560px;right:-210px;top:-180px}.orbit-b{width:310px;height:310px;right:75px;bottom:-180px}
.ticker { height:56px; position:relative; z-index:3; display:flex; align-items:center; gap:20px; border-top:1px solid rgba(255,255,255,.12); color:rgba(255,255,255,.55); font-size:11px; overflow:hidden; }
.ticker-tag { color:#ffb86b; font-size:8px; letter-spacing:2px; border-right:1px solid rgba(255,255,255,.15); padding-right:18px; white-space:nowrap; }
.ticker-track { flex:1; display:flex; gap:32px; white-space:nowrap; overflow:hidden; }.ticker-track i { color:var(--orange); font-style:normal; margin-left:32px; }.ticker-date { white-space:nowrap; color:rgba(255,255,255,.35); }
.discover-head { display:flex; justify-content:space-between; align-items:flex-end; }.head-stat { display:flex; gap:12px; align-items:center; padding-bottom:23px; }.head-stat strong { font-size:31px; }.head-stat span { color:var(--muted); font-size:10px; line-height:1.5; }
.home-movies { display:grid; grid-template-columns:1fr 330px; gap:28px; align-items:start; }.main-grid { grid-template-columns:repeat(4,1fr); }
.home-rank { padding:18px; position:sticky; top:96px; }.rank-head { display:flex; justify-content:space-between; align-items:center; padding-bottom:14px; border-bottom:1px solid var(--line); font-size:12px; font-weight:700; }.rank-head a { display:flex; align-items:center; gap:3px; color:var(--muted); font-size:9px; font-weight:400; }
.rank-row { display:grid; grid-template-columns:24px 35px 1fr auto; gap:10px; align-items:center; padding:11px 0; border-bottom:1px solid #f0ede7; }.rank-row:last-child { border-bottom:0; }.rank-no { font-family:Consolas,monospace; font-size:11px; color:#aaa39a; }.rank-no.top { color:var(--orange); font-weight:800; }.rank-row img { width:35px; height:49px; object-fit:cover; border-radius:5px; }.rank-row strong { display:block; font-size:11px; margin-bottom:4px; }.rank-row small { color:#a39c91; font-size:8px; }.rank-value { color:var(--orange); font-size:9px; font-weight:700; }.rank-button { width:100%; margin-top:12px; }
.editorial-banner { position:relative; background:#0c1421; color:#fff; overflow:hidden; }.editorial-inner { min-height:430px; padding:55px 0; display:flex; align-items:center; position:relative; }.editorial-copy { position:relative; z-index:3; width:46%; }.editorial-label { color:#ffb76a; font-size:9px; letter-spacing:2.5px; }.editorial-copy h2 { margin:17px 0; font-size:clamp(35px,4.2vw,60px); line-height:1.1; letter-spacing:-2px; }.editorial-copy p { color:rgba(255,255,255,.5); max-width:440px; font-size:12px; line-height:1.8; margin-bottom:25px; }.editorial-copy .el-button { background:#fff; color:#152238; border:0; }
.editorial-posters { position:absolute; right:3%; width:48%; height:100%; }.ep { position:absolute; width:165px; border-radius:8px; overflow:hidden; box-shadow:0 25px 50px rgba(0,0,0,.4); }.ep img { width:100%; }.ep-1 { left:8%; top:9%; transform:rotate(-7deg); }.ep-2 { left:39%; top:20%; transform:rotate(3deg); z-index:2; }.ep-3 { right:8%; top:5%; transform:rotate(9deg); opacity:.75; }.editorial-number { position:absolute; right:0; bottom:-45px; font-size:190px; font-weight:900; color:rgba(255,255,255,.025); }
.home-bottom { display:grid; grid-template-columns:1.15fr .85fr; gap:30px; }.score-list { padding:5px 18px; }.score-row { display:grid; grid-template-columns:25px 38px 1fr 120px 38px; gap:12px; align-items:center; padding:12px 0; border-bottom:1px solid #f0ede7; }.score-row:last-child { border:0; }.score-no { color:#aaa39a; font-size:11px; font-family:Consolas,monospace; }.score-row img { width:38px; height:53px; border-radius:5px; object-fit:cover; }.score-info strong { display:block; font-size:12px; }.score-info span { display:block; color:#aaa39a; font-size:8px; margin-top:4px; }.score-bar { height:3px; background:#eeeae2; border-radius:3px; overflow:hidden; }.score-bar i { display:block; height:100%; background:linear-gradient(90deg,#ffb86a,var(--orange)); border-radius:3px; }.score-row b { color:var(--orange); font-size:14px; text-align:right; }.notice-stack { display:grid; gap:12px; }.notice-card { display:grid; grid-template-columns:45px 1fr auto; gap:14px; padding:18px; position:relative; }.notice-icon { width:43px; height:43px; border-radius:12px; display:grid; place-items:center; background:var(--orange-soft); color:var(--orange); }.notice-card span { color:var(--orange); font-size:8px; letter-spacing:.5px; }.notice-card h3 { margin:5px 0; font-size:13px; }.notice-card p { margin:0; color:var(--muted); font-size:10px; line-height:1.6; display:-webkit-box; -webkit-line-clamp:2; -webkit-box-orient:vertical; overflow:hidden; }.notice-index { color:#dfd9cf!important; font-size:13px!important; }
.fade-enter-active,.fade-leave-active{transition:opacity .25s,transform .25s}.fade-enter-from{opacity:0;transform:translateY(12px)}.fade-leave-to{opacity:0;transform:translateY(-8px)}
@keyframes heroZoom{to{transform:scale(1.08)}}@keyframes pulse{50%{opacity:.45}}
@media(max-width:1100px){.home-movies{grid-template-columns:1fr}.home-rank{position:static;display:none}.main-grid{grid-template-columns:repeat(4,1fr)}.hero-inner{grid-template-columns:minmax(0,1fr) 280px;gap:35px}.hero-poster-card.video-card{width:280px}.hero-side{display:none}.hero-copy{width:auto}}
@media(max-width:850px){.hero-shell{min-height:570px}.hero-copy{position:relative;z-index:3;width:100%;padding:55px 0 35px}.hero-inner{position:relative;min-height:510px;display:block}.hero-poster-card{display:none}.hero-inner.video-mode{min-height:510px}.hero-inner.video-mode .hero-poster-card.video-card{position:absolute;inset:0;width:100%;height:100%;border-radius:0;transform:none;box-shadow:none;border:0}.hero-poster-card.video-card{display:block;position:absolute;inset:0;width:100%;height:100%;z-index:1;border-radius:0}.video-card video{position:absolute;inset:0;left:0;right:0;width:100%;height:100%;transform:none;object-fit:cover;object-position:center 24%;border:0;border-radius:0;box-shadow:none;filter:brightness(.57)}.video-card::after{content:'';position:absolute;inset:0;z-index:1;background:linear-gradient(90deg,rgba(5,10,17,.92),rgba(5,10,17,.22)),linear-gradient(0deg,rgba(5,10,17,.78),transparent 55%)}.video-label,.video-sound,.video-progress{position:relative;z-index:2}.video-label{top:auto;left:auto;position:absolute;right:12px;top:12px}.video-sound{right:12px;bottom:30px}.video-progress{left:12px;right:12px;bottom:18px}.hero-content h1{letter-spacing:-3px}.main-grid{grid-template-columns:repeat(3,1fr)}.home-bottom{grid-template-columns:1fr}.editorial-posters{opacity:.35;right:-10%}.editorial-copy{width:75%}.ticker-date{display:none}}
@media(max-width:620px){.hero-shell{min-height:560px}.hero-inner{min-height:500px}.hero-copy{padding:40px 0 20px}.hero-content h1{font-size:54px}.hero-meta span:nth-last-child(-n+2){display:none}.hero-actions{flex-direction:column;align-items:stretch;max-width:220px}.hero-actions .el-button{margin:0}.main-grid{grid-template-columns:repeat(2,1fr)}.ticker-track span:nth-child(n+3){display:none}.editorial-copy{width:100%}.editorial-posters{display:none}.score-row{grid-template-columns:22px 34px 1fr 38px}.score-bar{display:none}.notice-card{grid-template-columns:40px 1fr}.notice-index{display:none}.discover-head .head-stat{display:none}}
</style>
