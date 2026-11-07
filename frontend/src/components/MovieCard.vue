<script setup>
import { useRouter } from 'vue-router'
const props = defineProps({ movie: { type: Object, required: true }, showStatus: Boolean })
const router = useRouter()
function open() { router.push(`/movies/${props.movie.id}`) }
</script>

<template>
  <article class="poster-card" @click="open">
    <div class="poster-wrap">
      <img :src="movie.poster" :alt="movie.title" loading="lazy" />
      <span v-if="movie.score > 0" class="score-badge">{{ Number(movie.score).toFixed(1) }} 分</span>
      <span v-if="showStatus && movie.status === 'SOON'" class="status-badge">即将上映</span>
      <div class="poster-overlay">
        <div class="tag" style="color:#fff;background:rgba(255,101,66,.86)">{{ movie.genreNames?.split(',')[0] || '电影' }}</div>
        <p class="overlay-title">{{ movie.title }}</p>
        <p class="overlay-sub">{{ movie.director }} 导演 · {{ movie.duration }}分钟</p>
        <el-button type="primary" round size="small" style="margin-top:12px">查看详情</el-button>
      </div>
    </div>
    <div class="poster-meta">
      <h3>{{ movie.title }}</h3>
      <p>{{ movie.genreNames?.split(',').join(' / ') }}</p>
      <div class="meta-foot">
        <span>{{ movie.status === 'SOON' ? `${movie.wantCount}人想看` : `票房 ¥${Number(movie.boxOffice || 0).toFixed(1)}万` }}</span>
        <span v-if="movie.releaseDate"> {{ movie.releaseDate?.slice(5) }}</span>
      </div>
    </div>
  </article>
</template>

<style scoped>
.poster-card { cursor: pointer; }
.overlay-title { margin: 10px 0 5px; font-size: 19px; font-weight: 800; }
.overlay-sub { margin: 0; font-size: 11px; opacity: .66; }
.meta-foot { display: flex; justify-content: space-between; margin-top: 7px; color: #a39b8f; font-size: 11px; }
</style>
