<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import http from '@/api/http'

const router = useRouter()
const loading = ref(true)
const dashboard = ref({ stats: {}, sales: [], days: [], genreDistribution: [], genreBoxOffice: [] })
const pendingShowings = ref([])
const pendingCinemas = ref([])
const salesEl = ref(null)
const genreEl = ref(null)
const boxEl = ref(null)
let charts = []
async function load() {
  loading.value = true
  try {
    const [dash, shows, cinemas] = await Promise.all([http.get('/admin/dashboard'), http.get('/admin/showings'), http.get('/admin/cinemas')])
    dashboard.value = dash.data
    pendingShowings.value = shows.data.filter(item => item.approvalStatus === 'PENDING').slice(0, 5)
    pendingCinemas.value = cinemas.data.filter(item => item.status === 'PENDING').slice(0, 5)
    await nextTick(); renderCharts()
  } finally { loading.value = false }
}
function renderCharts() {
  charts.forEach(chart => chart.dispose()); charts = []
  const axisStyle = { axisLine: { lineStyle: { color: '#e9e4dc' } }, axisTick: { show: false }, axisLabel: { color: '#9d968c', fontSize: 10 } }
  if (salesEl.value) {
    const chart = echarts.init(salesEl.value)
    chart.setOption({ tooltip: { trigger: 'axis' }, grid: { left: 42, right: 18, top: 28, bottom: 30 }, xAxis: { type: 'category', data: dashboard.value.days, boundaryGap: false, ...axisStyle }, yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0ece5' } }, axisLabel: { color: '#9d968c', fontSize: 9 } }, series: [{ type: 'line', smooth: true, data: dashboard.value.sales, symbolSize: 7, lineStyle: { color: '#ff6542', width: 3 }, itemStyle: { color: '#ff6542' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(255,101,66,.28)' }, { offset: 1, color: 'rgba(255,101,66,0)' }]) } }] })
    charts.push(chart)
  }
  if (genreEl.value) {
    const chart = echarts.init(genreEl.value)
    chart.setOption({ tooltip: { trigger: 'item' }, legend: { bottom: 0, textStyle: { fontSize: 9, color: '#778092' }, icon: 'circle' }, series: [{ type: 'pie', radius: ['43%', '67%'], center: ['50%', '44%'], padAngle: 3, itemStyle: { borderRadius: 6 }, label: { show: false }, data: dashboard.value.genreDistribution.slice(0, 8).map((item, i) => ({ name: item.name, value: item.value, itemStyle: { color: ['#ff6542','#f2b04b','#4aa99a','#6f87d9','#b681c9','#ef8d8d','#83b76b','#94a3b1'][i] } })) }] })
    charts.push(chart)
  }
  if (boxEl.value) {
    const chart = echarts.init(boxEl.value)
    chart.setOption({ tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } }, grid: { left: 45, right: 15, top: 25, bottom: 45 }, xAxis: { type: 'category', data: dashboard.value.genreBoxOffice.map(i => i.name), axisLabel: { color: '#9d968c', fontSize: 9, rotate: 25 }, axisLine: { lineStyle: { color: '#e9e4dc' } }, axisTick: { show: false } }, yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0ece5' } }, axisLabel: { color: '#9d968c', fontSize: 9 } }, series: [{ type: 'bar', data: dashboard.value.genreBoxOffice.map((i, index) => ({ value: i.amount, itemStyle: { color: index === 0 ? '#ff6542' : '#f3b764', borderRadius: [6, 6, 0, 0] } })), barMaxWidth: 27 }] })
    charts.push(chart)
  }
}
function resize() { charts.forEach(chart => chart.resize()) }
onMounted(() => { load(); window.addEventListener('resize', resize) })
onBeforeUnmount(() => { window.removeEventListener('resize', resize); charts.forEach(chart => chart.dispose()) })
</script>

<template>
  <div v-loading="loading" class="dashboard">
    <section class="welcome-banner">
      <div><span>GOOD EVENING · 平台运营中心</span><h2>今天，也为好电影<br />找到它的观众。</h2><p>{{ new Date().toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' }) }} · 兔南电影数据概览</p></div>
      <div class="banner-ring"><strong>TN</strong><span>2026</span></div>
      <div class="banner-marquee"><span>DATA DRIVES CINEMA</span><span>DATA DRIVES CINEMA</span><span>DATA DRIVES CINEMA</span></div>
    </section>
    <div class="admin-grid-stats mt-24">
      <div class="stat-card"><div class="flex-between"><span class="label">今日票房</span><span class="stat-icon"><el-icon><TrendCharts /></el-icon></span></div><div class="value">¥{{ Number(dashboard.stats.todayRevenue || 0).toFixed(2) }}</div><small>实时支付口径</small></div>
      <div class="stat-card"><div class="flex-between"><span class="label">在映电影</span><span class="stat-icon"><el-icon><Film /></el-icon></span></div><div class="value">{{ dashboard.stats.movies || 0 }}</div><small>平台电影总数</small></div>
      <div class="stat-card"><div class="flex-between"><span class="label">认证影院</span><span class="stat-icon"><el-icon><OfficeBuilding /></el-icon></span></div><div class="value">{{ dashboard.stats.cinemas || 0 }}</div><small>覆盖南京核心商圈</small></div>
      <div class="stat-card"><div class="flex-between"><span class="label">注册影迷</span><span class="stat-icon"><el-icon><User /></el-icon></span></div><div class="value">{{ dashboard.stats.users || 0 }}</div><small>平台累计用户</small></div>
    </div>
    <div class="chart-grid mt-24">
      <div class="chart-card surface"><div class="chart-head"><div><span>BOX OFFICE TREND</span><h3>近七日票房趋势</h3></div><b>¥</b></div><div ref="salesEl" class="chart-line"></div></div>
      <div class="chart-card surface"><div class="chart-head"><div><span>GENRE MIX</span><h3>电影类型分布</h3></div></div><div ref="genreEl" class="chart-pie"></div></div>
      <div class="chart-card surface"><div class="chart-head"><div><span>GENRE PERFORMANCE</span><h3>类型累计票房</h3></div></div><div ref="boxEl" class="chart-bar"></div></div>
      <div class="todo-card surface"><div class="chart-head"><div><span>TO-DO LIST</span><h3>待处理事项</h3></div><router-link to="/admin/approvals">全部 <el-icon><ArrowRight /></el-icon></router-link></div><div class="todo-items"><button @click="router.push('/admin/approvals')"><span class="todo-icon orange"><el-icon><Calendar /></el-icon></span><div><strong>排片审批</strong><small>等待审核的影院排片申请</small></div><b>{{ dashboard.stats.pendingShowings || 0 }}</b></button><button @click="router.push('/admin/cinemas')"><span class="todo-icon teal"><el-icon><OfficeBuilding /></el-icon></span><div><strong>影院认证</strong><small>新影院入驻审核</small></div><b>{{ dashboard.stats.pendingCinemas || 0 }}</b></button><button @click="router.push('/admin/movies')"><span class="todo-icon blue"><el-icon><Film /></el-icon></span><div><strong>电影管理</strong><small>维护电影与演职人员</small></div><el-icon><ArrowRight /></el-icon></button><button @click="router.push('/admin/notices')"><span class="todo-icon gold"><el-icon><Bell /></el-icon></span><div><strong>公告发布</strong><small>向用户发送系统消息</small></div><el-icon><ArrowRight /></el-icon></button></div></div>
    </div>
    <div class="pending-grid mt-24">
      <div class="table-card"><div class="table-toolbar"><h3>待审批排片</h3><router-link to="/admin/approvals" class="section-more">查看全部 <el-icon><ArrowRight /></el-icon></router-link></div><el-table :data="pendingShowings" size="small"><el-table-column label="电影" min-width="150"><template #default="{row}"><div class="mini-movie"><img :src="row.poster" /><div><b>{{ row.movieTitle }}</b><small>{{ row.roomName }}</small></div></div></template></el-table-column><el-table-column prop="cinemaName" label="影院" min-width="150" /><el-table-column prop="startTime" label="放映时间" width="135" /><el-table-column label="操作" width="75"><template #default><el-button link type="primary" @click="router.push('/admin/approvals')">审批</el-button></template></el-table-column><template #empty><div class="table-empty">暂无待审批排片</div></template></el-table></div>
      <div class="table-card"><div class="table-toolbar"><h3>待认证影院</h3><router-link to="/admin/cinemas" class="section-more">查看全部 <el-icon><ArrowRight /></el-icon></router-link></div><el-table :data="pendingCinemas" size="small"><el-table-column label="影院" min-width="180"><template #default="{row}"><div><b>{{ row.name }}</b><div class="muted" style="font-size:9px">{{ row.address }}</div></div></template></el-table-column><el-table-column prop="ownerName" label="申请人" width="100" /><el-table-column label="状态" width="80"><template #default><el-tag size="small" type="warning" round>待审核</el-tag></template></el-table-column><template #empty><div class="table-empty">暂无待认证影院</div></template></el-table></div>
    </div>
  </div>
</template>

<style scoped>
.welcome-banner{position:relative;min-height:190px;padding:30px 34px;border-radius:18px;overflow:hidden;background:linear-gradient(110deg,#0c1421,#172c3b 60%,#5a4230);color:#fff;display:flex;align-items:center}.welcome-banner>div:first-child{position:relative;z-index:2}.welcome-banner>div>span{color:#ffb76a;font-size:8px;letter-spacing:2.3px}.welcome-banner h2{font-size:33px;line-height:1.15;letter-spacing:-1.3px;margin:10px 0 8px}.welcome-banner p{color:rgba(255,255,255,.36);font-size:9px}.banner-ring{position:absolute;right:55px;width:150px;height:150px;border:1px solid rgba(255,255,255,.15);border-radius:50%;display:grid;place-items:center;color:rgba(255,255,255,.2)}.banner-ring::before,.banner-ring::after{content:'';position:absolute;border:1px solid rgba(255,255,255,.08);border-radius:50%}.banner-ring::before{inset:16px}.banner-ring::after{inset:35px}.banner-ring strong{font-size:44px}.banner-ring span{position:absolute;bottom:24px;font-size:8px;letter-spacing:2px}.banner-marquee{position:absolute;right:-20px;bottom:10px;font-size:42px;font-weight:900;color:rgba(255,255,255,.03);white-space:nowrap;letter-spacing:5px}.stat-card small{color:#b1aa9f;font-size:8px}.chart-grid{display:grid;grid-template-columns:1.4fr 1fr;gap:15px}.chart-card,.todo-card{padding:18px;min-width:0}.chart-head{display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:9px}.chart-head span{color:var(--orange);font-size:7px;letter-spacing:1.5px}.chart-head h3{font-size:14px;margin:5px 0 0}.chart-head>b{color:#e0dbd1;font-size:24px}.chart-head a{display:flex;gap:3px;align-items:center;color:var(--muted);font-size:8px}.chart-line,.chart-pie{height:255px}.chart-bar{height:215px}.todo-items{display:grid;gap:4px;margin-top:3px}.todo-items button{display:grid;grid-template-columns:34px 1fr auto;align-items:center;gap:10px;padding:9px;border:0;background:none;text-align:left;border-radius:8px;cursor:pointer}.todo-items button:hover{background:#faf8f4}.todo-icon{width:32px;height:32px;display:grid;place-items:center;border-radius:9px;font-size:15px}.todo-icon.orange{background:#fff0eb;color:#ff6542}.todo-icon.teal{background:#eaf8f4;color:#32a88f}.todo-icon.blue{background:#eef3fd;color:#567ccc}.todo-icon.gold{background:#fff8e5;color:#d69e35}.todo-items strong{display:block;font-size:10px}.todo-items small{display:block;color:#aaa39a;font-size:7px;margin-top:3px}.todo-items b{color:var(--orange);font-size:17px}.todo-items button>.el-icon{color:#bbb4a9}.pending-grid{display:grid;grid-template-columns:1fr 1fr;gap:15px}.mini-movie{display:flex;align-items:center;gap:8px}.mini-movie img{width:26px;height:36px;object-fit:cover;border-radius:4px}.mini-movie b{display:block;font-size:10px}.mini-movie small{color:var(--muted);font-size:7px}.table-empty{text-align:center;color:#aaa39a;font-size:9px;padding:18px}
@media(max-width:1100px){.chart-grid{grid-template-columns:1fr 1fr}.chart-line{grid-column:1/-1}.pending-grid{grid-template-columns:1fr}}@media(max-width:700px){.welcome-banner{padding:22px}.welcome-banner h2{font-size:25px}.banner-ring{display:none}.chart-grid{grid-template-columns:1fr}.chart-line{grid-column:auto}.chart-line,.chart-pie{height:220px}}
</style>
