<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const submitting = ref(false)
const data = ref({ showing: {}, seats: [] })
const selected = ref([])
const paymentVisible = ref(false)
const successVisible = ref(false)
const order = ref({})
const payment = reactive({ method: '微信支付' })
const methods = [
  { name: '微信支付', icon: 'ChatDotRound', color: '#22b86a' },
  { name: '支付宝', icon: 'Wallet', color: '#1677ff' },
  { name: '云闪付', icon: 'CreditCard', color: '#e34d59' }
]
const rows = computed(() => {
  const map = new Map()
  for (const seat of data.value.seats) {
    if (!map.has(seat.row)) map.set(seat.row, [])
    map.get(seat.row).push(seat)
  }
  return [...map.entries()].map(([row, seats]) => ({ row, seats }))
})
const total = computed(() => selected.value.length * Number(data.value.showing.price || 0))
const selectedLabels = computed(() => selected.value.map(seat => `${seat.row}排${seat.col}座`))
const typeText = computed(() => data.value.showing.version?.includes('IMAX') ? 'IMAX 巨幕' : '标准厅')
function isSelected(seat) { return selected.value.some(item => item.row === seat.row && item.col === seat.col) }
function toggle(seat) {
  if (seat.occupied) return
  const index = selected.value.findIndex(item => item.row === seat.row && item.col === seat.col)
  if (index >= 0) selected.value.splice(index, 1)
  else if (selected.value.length >= 6) ElMessage.warning('单次最多选择6个座位')
  else selected.value.push(seat)
}
async function load() {
  loading.value = true
  try { const { data: res } = await http.get(`/public/showings/${route.params.showingId}/seats`); data.value = res } finally { loading.value = false }
}
async function createOrder() {
  if (!selected.value.length) return ElMessage.warning('请先选择座位')
  submitting.value = true
  try {
    const { data: res } = await http.post('/user/orders', { showingId: Number(route.params.showingId), seats: selected.value.map(({ row, col }) => ({ row, col })) })
    order.value = res
    paymentVisible.value = true
  } finally { submitting.value = false }
}
async function pay() {
  submitting.value = true
  try {
    const { data: res } = await http.post(`/user/orders/${order.value.id}/pay`, { method: payment.method })
    order.value = res
    paymentVisible.value = false
    successVisible.value = true
  } finally { submitting.value = false }
}
function goOrders() { router.push('/orders') }
onMounted(load)
</script>

<template>
  <div v-loading="loading" class="booking-page">
    <div class="page">
      <div class="booking-top"><router-link :to="`/movies/${data.showing.movieId}`" class="back-link"><el-icon><ArrowLeft /></el-icon> 返回电影详情</router-link><div class="steps"><span class="done">1 选择场次</span><i></i><span class="active">2 在线选座</span><i></i><span>3 确认支付</span></div><span class="timer"><el-icon><Clock /></el-icon> 座位锁定 10 分钟</span></div>
      <div class="booking-shell">
        <aside class="movie-summary">
          <img :src="data.showing.poster" :alt="data.showing.movieTitle" />
          <div class="summary-copy"><span>TUNAN CINEMA</span><h1>{{ data.showing.movieTitle }}</h1><p>{{ typeText }} · {{ data.showing.language }}</p><div class="summary-line"></div><div class="summary-info"><span><el-icon><OfficeBuilding /></el-icon>{{ data.showing.cinemaName }}</span><span><el-icon><Location /></el-icon>{{ data.showing.cinemaAddress }}</span><span><el-icon><Calendar /></el-icon>{{ data.showing.startTime }}</span><span><el-icon><VideoCamera /></el-icon>{{ data.showing.roomName }}</span></div></div>
        </aside>
        <main class="seat-area">
          <div class="seat-heading"><div><span>CHOOSE YOUR SEAT</span><h2>选择座位</h2></div><div class="legend"><span><i class="free"></i>可选</span><span><i class="picked"></i>已选</span><span><i class="sold"></i>已售</span></div></div>
          <div class="screen-wrap"><div class="screen"></div><span>银幕中央 · SCREEN</span></div>
          <div class="seat-map">
            <div v-for="row in rows" :key="row.row" class="seat-row"><span class="row-no">{{ row.row }}</span><div class="seat-cells"><button v-for="seat in row.seats" :key="seat.col" class="seat" :class="{ occupied: seat.occupied, selected: isSelected(seat), aisle: seat.aisleAfter }" :disabled="seat.occupied" :title="seat.label" @click="toggle(seat)">{{ seat.col }}</button></div><span class="row-no">{{ row.row }}</span></div>
          </div>
          <div class="seat-note"><el-icon><InfoFilled /></el-icon> 红色座位已被购买，选座后请在10分钟内完成支付</div>
        </main>
        <aside class="checkout-panel">
          <div class="checkout-title"><span>YOUR ORDER</span><h3>订单确认</h3></div>
          <div class="selected-seats"><span>已选座位</span><div v-if="selectedLabels.length" class="chosen-tags"><b v-for="label in selectedLabels" :key="label">{{ label }}</b></div><small v-else>还未选择座位</small></div>
          <div class="price-lines"><div><span>票价</span><b>¥{{ Number(data.showing.price).toFixed(2) }} × {{ selected.length }}</b></div><div><span>服务费</span><b class="text-green">免收</b></div></div>
          <div class="total-line"><span>应付总额</span><strong><small>¥</small>{{ total.toFixed(2) }}</strong></div>
          <el-button type="primary" size="large" class="w-full pay-button" :loading="submitting" :disabled="!selected.length" @click="createOrder">确认选座</el-button>
          <p class="safe-tip"><el-icon><Lock /></el-icon> 安全支付 · 取票前可取消</p>
        </aside>
      </div>
    </div>

    <el-dialog v-model="paymentVisible" title="确认订单并支付" width="min(480px, 92vw)" :close-on-click-modal="false">
      <div class="payment-box"><div class="payment-amount"><span>订单金额</span><strong><small>¥</small>{{ Number(order.amount || total).toFixed(2) }}</strong><small>订单号 {{ order.orderNo }}</small></div><div class="payment-methods"><button v-for="method in methods" :key="method.name" :class="{ active: payment.method === method.name }" @click="payment.method = method.name"><el-icon :style="{ color: method.color }"><component :is="method.icon" /></el-icon><span>{{ method.name }}</span><i></i></button></div><el-button type="primary" size="large" class="w-full" :loading="submitting" @click="pay">立即支付 ¥{{ Number(order.amount || total).toFixed(2) }}</el-button></div>
    </el-dialog>
    <el-dialog v-model="successVisible" width="min(440px, 92vw)" :show-close="false">
      <div class="success-box"><div class="success-icon"><el-icon><Select /></el-icon></div><h2>购票成功</h2><p>祝你在兔南电影度过一段好时光</p><div class="ticket-code"><span>取票码</span><strong>{{ order.pickupCode }}</strong><small>请凭取票码到影院自助机取票</small></div><div class="success-actions"><el-button @click="router.push('/')">返回首页</el-button><el-button type="primary" @click="goOrders">查看订单</el-button></div></div>
    </el-dialog>
  </div>
</template>

<style scoped>
.booking-page{padding:22px 0 70px;min-height:75vh}.booking-top{display:flex;align-items:center;justify-content:space-between;margin-bottom:18px}.back-link{display:flex;align-items:center;gap:5px;color:var(--muted);font-size:10px}.steps{display:flex;align-items:center;gap:9px;font-size:9px;color:#aaa39a}.steps span{display:flex;align-items:center;gap:5px}.steps span::before{content:'';width:16px;height:16px;border:1px solid #d2ccc2;border-radius:50%;display:grid;place-items:center}.steps .done{color:var(--green)}.steps .done::before{content:'✓';border-color:var(--green);background:#e8f7f4}.steps .active{color:var(--orange);font-weight:700}.steps .active::before{content:'2';border-color:var(--orange);background:var(--orange-soft);display:grid;place-items:center}.steps i{width:24px;height:1px;background:#ded8cd}.timer{display:flex;align-items:center;gap:5px;color:#a29b90;font-size:9px}.booking-shell{display:grid;grid-template-columns:225px 1fr 265px;gap:18px;align-items:stretch}.movie-summary{background:#0e1724;border-radius:14px;padding:14px;color:#fff;overflow:hidden;display:flex;flex-direction:column}.movie-summary>img{width:100%;aspect-ratio:2/3;object-fit:cover;border-radius:9px}.summary-copy{padding:16px 3px 3px}.summary-copy>span{color:#ffb76a;font-size:7px;letter-spacing:1.8px}.summary-copy h1{font-size:23px;margin:8px 0 5px;letter-spacing:-1px}.summary-copy>p{color:rgba(255,255,255,.42);font-size:9px;margin:0}.summary-line{height:1px;background:rgba(255,255,255,.1);margin:14px 0}.summary-info{display:grid;gap:11px}.summary-info span{display:flex;align-items:flex-start;gap:7px;color:rgba(255,255,255,.56);font-size:9px;line-height:1.45}.summary-info .el-icon{color:#ffb76a;flex-shrink:0;margin-top:1px}.seat-area{background:#fff;border:1px solid var(--line);border-radius:14px;padding:20px 25px;box-shadow:var(--shadow-sm)}.seat-heading{display:flex;justify-content:space-between;align-items:flex-start}.seat-heading>div:first-child>span{color:var(--orange);font-size:7px;letter-spacing:1.8px}.seat-heading h2{font-size:22px;margin:5px 0 0}.legend{display:flex;gap:13px;color:var(--muted);font-size:8px}.legend span{display:flex;align-items:center;gap:4px}.legend i{width:9px;height:9px;border-radius:3px;background:#2cbf80}.legend i.picked{background:#ffbd54}.legend i.sold{background:#ffd5d2}.screen-wrap{text-align:center;margin:30px 0 23px}.screen{height:7px;max-width:450px;margin:0 auto 9px;background:linear-gradient(90deg,transparent,#c2c8d1 12%,#e5e8ec 50%,#c2c8d1 88%,transparent);border-radius:50%;box-shadow:0 6px 18px rgba(54,72,95,.12)}.screen-wrap span{color:#c0b9ae;font-size:7px;letter-spacing:1.4px}.seat-map{display:grid;gap:6px;justify-content:center}.seat-row{display:flex;align-items:center;gap:8px}.row-no{width:12px;text-align:center;color:#c4bdb2;font-size:8px;font-family:Consolas,monospace}.seat-cells{display:flex;gap:5px}.seat{width:25px;height:24px;border:0;border-radius:5px;background:#35c486;color:#fff;font-size:8px;cursor:pointer;transition:.14s;box-shadow:inset 0 -3px 0 rgba(0,0,0,.08)}.seat:hover:not(.occupied){transform:translateY(-2px);box-shadow:0 4px 10px rgba(43,180,122,.22)}.seat.selected{background:#ffbc50;box-shadow:0 0 0 2px #fff,0 0 0 3.5px #ffbc50}.seat.occupied{background:#ffd8d5;color:#f0ada7;cursor:not-allowed}.seat.aisle{margin-right:15px}.seat-note{display:flex;justify-content:center;align-items:center;gap:5px;margin-top:22px;color:#aaa39a;font-size:8px}.checkout-panel{background:#fff;border:1px solid var(--line);border-radius:14px;padding:20px;box-shadow:var(--shadow-sm);display:flex;flex-direction:column}.checkout-title span{color:var(--orange);font-size:7px;letter-spacing:1.6px}.checkout-title h3{font-size:18px;margin:5px 0 20px}.selected-seats>span{color:var(--muted);font-size:9px}.chosen-tags{display:flex;flex-wrap:wrap;gap:5px;margin-top:10px}.chosen-tags b{background:var(--orange-soft);color:var(--orange);padding:5px 7px;border-radius:5px;font-size:8px}.selected-seats>small{display:block;color:#b5aea4;font-size:9px;margin-top:11px}.price-lines{border-top:1px solid var(--line);border-bottom:1px solid var(--line);padding:15px 0;margin-top:20px;display:grid;gap:10px}.price-lines div{display:flex;justify-content:space-between;font-size:9px}.price-lines span{color:var(--muted)}.price-lines b{font-weight:600}.text-green{color:var(--green)}.total-line{display:flex;justify-content:space-between;align-items:flex-end;margin:18px 0}.total-line>span{font-size:10px;font-weight:700}.total-line strong{font-size:28px;color:var(--orange);line-height:1}.total-line strong small{font-size:13px}.pay-button{height:45px}.safe-tip{display:flex;justify-content:center;align-items:center;gap:4px;color:#aaa39a;font-size:8px;margin:12px 0 0}.payment-box{display:grid;gap:18px}.payment-amount{text-align:center;padding:15px;background:#faf8f4;border-radius:11px}.payment-amount>span{display:block;color:var(--muted);font-size:9px}.payment-amount strong{display:block;font-size:36px;margin:7px 0}.payment-amount strong small{font-size:16px}.payment-amount>small{color:#aaa39a;font-size:8px}.payment-methods{display:grid;gap:8px}.payment-methods button{display:grid;grid-template-columns:25px 1fr 16px;align-items:center;gap:8px;height:45px;padding:0 13px;border:1px solid var(--line);border-radius:9px;background:#fff;cursor:pointer;text-align:left}.payment-methods button.active{border-color:var(--orange);background:#fffbf9}.payment-methods .el-icon{font-size:19px}.payment-methods span{font-size:11px}.payment-methods i{width:13px;height:13px;border:1px solid #d1cbc1;border-radius:50%}.payment-methods button.active i{border:4px solid var(--orange)}.success-box{text-align:center;padding:12px 15px 20px}.success-icon{width:64px;height:64px;border-radius:50%;display:grid;place-items:center;margin:0 auto 15px;background:#eaf8f4;color:var(--green);font-size:29px}.success-box h2{margin:0 0 7px}.success-box>p{color:var(--muted);font-size:10px;margin:0}.ticket-code{margin:22px 0;padding:15px;border:1px dashed #d9d2c7;border-radius:10px;background:#faf8f4}.ticket-code span{display:block;color:var(--muted);font-size:8px}.ticket-code strong{display:block;font-size:29px;letter-spacing:5px;margin:5px 0;color:var(--orange)}.ticket-code small{color:#aaa39a;font-size:8px}.success-actions{display:grid;grid-template-columns:1fr 1fr;gap:9px}
@media(max-width:1100px){.booking-shell{grid-template-columns:190px 1fr}.checkout-panel{grid-column:1/-1;display:grid;grid-template-columns:1fr 1fr 1fr;gap:18px;align-items:center}.checkout-title h3{margin-bottom:0}.price-lines{margin:0;border:0;padding:0}.total-line{margin:0;justify-content:flex-end;gap:10px}.pay-button{grid-column:3}.safe-tip{display:none}.selected-seats{border-left:1px solid var(--line);padding-left:18px}}@media(max-width:780px){.booking-top .steps,.timer{display:none}.booking-shell{grid-template-columns:1fr}.movie-summary{display:grid;grid-template-columns:85px 1fr;padding:10px}.movie-summary>img{width:85px;height:127px}.summary-info{grid-template-columns:1fr 1fr}.summary-copy h1{font-size:19px}.summary-copy>span,.summary-line{display:none}.seat-area{padding:16px 10px;overflow:auto}.seat{width:23px;height:23px}.seat-cells{gap:4px}.seat.aisle{margin-right:9px}.checkout-panel{display:block}.selected-seats{border:0;padding:0}.price-lines{border-top:1px solid var(--line);border-bottom:1px solid var(--line);padding:13px 0;margin-top:15px}.total-line{margin:15px 0;justify-content:space-between}.pay-button{margin-top:0}}
</style>
