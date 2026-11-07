<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '@/api/http'

const router = useRouter()
const loading = ref(true)
const type = ref('boxoffice')
const records = ref([])
const types = [{ value: 'boxoffice', label: '总票房榜', sub: 'BOX OFFICE' }, { value: 'score', label: '观众评分榜', sub: 'AUDIENCE SCORE' }, { value: 'want', label: '期待值榜', sub: 'MOST WANTED' }]
const podium = computed(() => records.value.slice(0, 3))
const rest = computed(() => records.value.slice(3))
const unit = computed(() => type.value === 'boxoffice' ? '万' : type.value === 'score' ? '分' : '人')
const value = (movie) => type.value === 'boxoffice' ? Number(movie.boxOffice).toFixed(1) : type.value === 'score' ? Number(movie.score).toFixed(1) : movie.wantCount
async function load() {
  loading.value = true
  try { const { data } = await http.get('/public/rankings', { params: { type: type.value } }); records.value = data.records } finally { loading.value = false }
}
function setType(value) { type.value = value; load() }
onMounted(load)
</script>

<template>
  <div class="ranking-page">
    <section class="ranking-hero"><div class="page"><div class="ranking-heading"><div><span class="section-kicker">THE TUNAN CHARTS</span><h1>光影榜单</h1><p>以真实票房、观众评分与想看人数，记录每一部被看见的电影。</p></div><div class="chart-stamp"><span>2026</span><b>11 / 05</b><small>每周更新</small></div></div><div class="type-tabs"><button v-for="item in types" :key="item.value" :class="{ active: type === item.value }" @click="setType(item.value)"><small>{{ item.sub }}</small><strong>{{ item.label }}</strong></button></div></div></section>
    <div class="page ranking-content" v-loading="loading">
      <div class="podium">
        <article v-for="(movie, index) in podium" :key="movie.id" class="podium-card" :class="`place-${index + 1}`" @click="router.push(`/movies/${movie.id}`)">
          <div class="podium-number">0{{ index + 1 }}</div><img :src="movie.poster" :alt="movie.title" /><div class="podium-info"><span>{{ movie.genreNames?.split(',')[0] }}</span><h2>{{ movie.title }}</h2><p>{{ movie.englishTitle }}</p><div><strong>{{ value(movie) }}</strong><small>{{ unit }}</small></div></div><div class="podium-rank">{{ index === 0 ? '冠军' : index === 1 ? '亚军' : '季军' }}</div>
        </article>
      </div>
      <div class="ranking-list surface">
        <div class="list-head"><span>排名</span><span>电影</span><span>类型</span><span>上映日期</span><span class="text-right">{{ type === 'want' ? '想看人数' : type === 'score' ? '观众评分' : '累计票房' }}</span></div>
        <div v-for="movie in rest" :key="movie.id" class="ranking-row" @click="router.push(`/movies/${movie.id}`)"><span class="list-rank">{{ String(movie.rank).padStart(2, '0') }}</span><div class="list-movie"><img :src="movie.poster" /><div><strong>{{ movie.title }}</strong><small>{{ movie.englishTitle }}</small></div></div><span class="list-genres">{{ movie.genreNames?.split(',').join(' / ') }}</span><span class="list-date">{{ movie.releaseDate }}</span><div class="list-value"><b>{{ value(movie) }}</b><small>{{ unit }}</small></div></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ranking-page{min-height:70vh}.ranking-hero{background:linear-gradient(110deg,#0b1421,#182536 60%,#403e35);color:#fff;padding:51px 0 0}.ranking-heading{display:flex;justify-content:space-between;align-items:flex-end}.ranking-heading h1{margin:12px 0 10px;font-size:61px;letter-spacing:-3px}.ranking-heading p{color:rgba(255,255,255,.48);font-size:12px;max-width:520px;line-height:1.8}.chart-stamp{display:grid;justify-items:end;gap:2px;padding-bottom:18px}.chart-stamp span{color:#ffb76a;font-size:44px;font-weight:200;line-height:1}.chart-stamp b{font-size:11px;letter-spacing:1px}.chart-stamp small{color:rgba(255,255,255,.35);font-size:8px}.type-tabs{display:flex;margin-top:35px}.type-tabs button{background:none;border:0;border-top:2px solid transparent;color:rgba(255,255,255,.4);padding:15px 35px 18px 0;text-align:left;cursor:pointer;min-width:180px}.type-tabs button small{display:block;font-size:7px;letter-spacing:1.5px;margin-bottom:5px}.type-tabs button strong{font-size:13px}.type-tabs button.active{border-color:#ffb76a;color:#fff}.type-tabs button.active small{color:#ffb76a}.ranking-content{padding:38px 0 70px}.podium{display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:25px}.podium-card{position:relative;display:grid;grid-template-columns:100px 1fr;gap:17px;padding:18px;border-radius:15px;overflow:hidden;background:white;border:1px solid var(--line);cursor:pointer;min-height:190px}.podium-card.place-1{background:#101927;color:#fff;border-color:#101927}.podium-number{position:absolute;right:12px;top:5px;font-size:54px;font-weight:900;color:rgba(20,30,45,.04)}.place-1 .podium-number{color:rgba(255,255,255,.045)}.podium-card img{width:100px;height:150px;object-fit:cover;border-radius:8px}.podium-info{padding-top:10px;position:relative;z-index:1}.podium-info>span{color:var(--orange);font-size:8px;letter-spacing:1px}.podium-info h2{font-size:22px;margin:8px 0 4px;letter-spacing:-1px}.podium-info p{font-size:7px;color:#aaa39a;margin:0;letter-spacing:1px}.podium-info>div{margin-top:25px}.podium-info strong{font-size:32px;color:var(--orange)}.podium-info small{color:#aaa39a;font-size:9px;margin-left:4px}.podium-rank{position:absolute;right:13px;bottom:12px;font-size:8px;color:#aaa39a}.place-1 .podium-rank{color:#ffb76a}.ranking-list{overflow:hidden}.list-head{display:grid;grid-template-columns:70px 1fr 180px 120px 120px;padding:13px 20px;background:#faf8f4;color:#9d968c;font-size:8px;letter-spacing:.5px}.ranking-row{display:grid;grid-template-columns:70px 1fr 180px 120px 120px;align-items:center;padding:13px 20px;border-top:1px solid #f0ede7;cursor:pointer;transition:.2s}.ranking-row:hover{background:#fffaf7}.list-rank{font-family:Consolas,monospace;font-size:12px;color:#aaa39a}.list-movie{display:flex;align-items:center;gap:13px}.list-movie img{width:39px;height:55px;object-fit:cover;border-radius:5px}.list-movie strong{display:block;font-size:12px}.list-movie small{display:block;color:#aaa39a;font-size:7px;margin-top:4px;letter-spacing:.5px}.list-genres,.list-date{font-size:9px;color:var(--muted)}.list-value{text-align:right}.list-value b{font-size:17px;color:var(--orange)}.list-value small{font-size:8px;color:#aaa39a;margin-left:3px}
@media(max-width:900px){.podium{grid-template-columns:1fr}.podium-card{grid-template-columns:85px 1fr;min-height:auto}.podium-card img{width:85px;height:127px}.list-head{display:none}.ranking-row{grid-template-columns:45px 1fr 90px}.list-genres,.list-date{display:none}.list-value{grid-column:3}.list-movie img{width:36px;height:51px}}@media(max-width:520px){.ranking-heading h1{font-size:45px}.chart-stamp{display:none}.type-tabs{overflow:auto}.type-tabs button{min-width:145px}.ranking-row{grid-template-columns:35px 1fr 72px;padding:12px}.list-value b{font-size:14px}}
</style>
