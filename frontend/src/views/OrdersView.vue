<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const orders = ref([])
const filter = ref('ALL')
const detailVisible = ref(false)
const detail = ref({})
const trackNo = ref('')
const filters = [{ value: 'ALL', label: '全部订单' }, { value: 'UNPAID', label: '待支付' }, { value: 'PAID', label: '待取票' }, { value: 'USED', label: '已取票' }, { value: 'CANCELLED', label: '已取消' }]
const statusMap = { UNPAID: ['待支付', 'warning'], PAID: ['待取票', 'success'], USED: ['已取票', 'info'], CANCELLED: ['已取消', 'info'] }
const filtered = computed(() => filter.value === 'ALL' ? orders.value : orders.value.filter(item => item.status === filter.value))
const count = (status) => status === 'ALL' ? orders.value.length : orders.value.filter(item => item.status === status).length
async function load() { loading.value = true; try { const { data } = await http.get('/user/orders'); orders.value = data } finally { loading.value = false } }
async function openDetail(order) { const { data } = await http.get(`/user/orders/${order.id}`); detail.value = data; detailVisible.value = true }
async function cancel(order) {
  await ElMessageBox.confirm(`确定取消订单 ${order.orderNo} 吗？`, '取消订单', { confirmButtonText: '确认取消', cancelButtonText: '暂不取消', type: 'warning' })
  await http.post(`/user/orders/${order.id}/cancel`)
  ElMessage.success('订单已取消'); detailVisible.value = false; load()
}
async function track() {
  if (!trackNo.value.trim()) return ElMessage.warning('请输入订单号')
  try { const { data } = await http.get('/user/orders/track', { params: { orderNo: trackNo.value.trim() } }); detail.value = data; detailVisible.value = true } catch {}
}
function canCancel(order) { return ['UNPAID', 'PAID'].includes(order.status) }
onMounted(load)
</script>

<template>
  <div class="orders-page">
    <section class="orders-hero"><div class="page"><div><span class="section-kicker">MY TICKETS</span><h1>购票记录</h1><p>每一张票，都是一次值得回忆的出发。</p></div><div class="order-search"><span>按订单号查询</span><el-input v-model="trackNo" placeholder="输入订单号" :prefix-icon="Search" @keyup.enter="track"><template #append><el-button @click="track">查询</el-button></template></el-input></div></div></section>
    <div class="page orders-content">
      <div class="order-tabs"><button v-for="item in filters" :key="item.value" :class="{ active: filter === item.value }" @click="filter = item.value">{{ item.label }}<span>{{ count(item.value) }}</span></button></div>
      <div v-loading="loading" class="orders-list">
        <article v-for="order in filtered" :key="order.id" class="order-card surface">
          <div class="order-card-head"><span class="order-no">订单号 {{ order.orderNo }}</span><span class="order-date">{{ order.createdAt }}</span><el-tag :type="statusMap[order.status]?.[1]" effect="light" round>{{ statusMap[order.status]?.[0] || order.status }}</el-tag></div>
          <div class="order-card-body"><img :src="order.poster" :alt="order.movieTitle" /><div class="order-movie"><h2>{{ order.movieTitle }}</h2><p><el-icon><OfficeBuilding /></el-icon>{{ order.cinemaName }}</p><p><el-icon><Calendar /></el-icon>{{ order.startTime }} · {{ order.roomName }}</p></div><div class="order-amount"><span>实付金额</span><strong><small>¥</small>{{ Number(order.amount).toFixed(2) }}</strong><small>{{ order.payMethod || '尚未支付' }}</small></div></div>
          <div v-if="order.status === 'PAID'" class="pickup-strip"><el-icon><Ticket /></el-icon><span>取票码</span><strong>{{ order.pickupCode }}</strong><small>请在自助取票机输入取票码</small></div>
          <div class="order-actions"><el-button text @click="openDetail(order)">查看详情 <el-icon><ArrowRight /></el-icon></el-button><div><el-button v-if="canCancel(order)" text type="danger" @click="cancel(order)">取消订单</el-button><el-button type="primary" plain round @click="openDetail(order)">{{ order.status === 'UNPAID' ? '去支付' : '订单详情' }}</el-button></div></div>
        </article>
        <div v-if="!loading && !filtered.length" class="empty-state surface"><el-icon><Tickets /></el-icon><h3>这里还没有订单</h3><p>去挑一部电影，开启你的观影旅程吧</p><router-link to="/movies"><el-button type="primary" round>去选电影</el-button></router-link></div>
      </div>
    </div>
    <el-drawer v-model="detailVisible" title="订单详情" size="min(500px, 94vw)">
      <div v-if="detail.id" class="drawer-content">
        <div class="drawer-status"><el-tag :type="statusMap[detail.status]?.[1]" effect="dark" round>{{ statusMap[detail.status]?.[0] }}</el-tag><span>{{ detail.orderNo }}</span></div>
        <div class="drawer-movie"><img :src="detail.poster" /><div><h2>{{ detail.movieTitle }}</h2><p>{{ detail.cinemaName }}</p><small>{{ detail.address }}</small></div></div>
        <div class="detail-list"><div><span>放映时间</span><b>{{ detail.startTime }}</b></div><div><span>影厅</span><b>{{ detail.roomName }}</b></div><div><span>座位</span><b>{{ detail.seats?.map(s => `${s.rowNo}排${s.colNo}座`).join('、') }}</b></div><div><span>订单金额</span><b class="text-orange">¥{{ Number(detail.amount).toFixed(2) }}</b></div><div><span>支付方式</span><b>{{ detail.payMethod || '未支付' }}</b></div><div><span>下单时间</span><b>{{ detail.createdAt }}</b></div></div>
        <div v-if="detail.status === 'PAID'" class="drawer-code"><span>电子取票码</span><strong>{{ detail.pickupCode }}</strong><div class="fake-qr"><i v-for="n in 64" :key="n" :class="{ dark: [1,2,3,5,7,8,9,11,13,14,15,17,20,21,23,24,26,28,29,31,32,33,35,37,39,40,42,44,45,47,49,50,51,53,55,57,58,60,62,63].includes(n) }"></i></div><small>演示二维码 · 到店请使用取票码</small></div>
        <el-button v-if="detail.status === 'UNPAID'" type="primary" size="large" class="w-full" @click="detailVisible=false; ElMessage.info('请在选座页完成支付')">去完成支付</el-button>
        <el-button v-if="canCancel(detail)" text type="danger" class="w-full" @click="cancel(detail)">取消此订单</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { Search } from '@element-plus/icons-vue'
export default { components: { Search } }
</script>

<style scoped>
.orders-page{min-height:70vh}.orders-hero{background:#0d1623;color:#fff;padding:48px 0}.orders-hero .page{display:flex;justify-content:space-between;align-items:flex-end;gap:30px}.orders-hero h1{font-size:54px;letter-spacing:-3px;margin:12px 0 7px}.orders-hero p{color:rgba(255,255,255,.42);font-size:11px}.order-search{width:370px;padding-bottom:5px}.order-search>span{display:block;color:rgba(255,255,255,.38);font-size:8px;margin-bottom:8px}.order-search :deep(.el-input__wrapper){background:rgba(255,255,255,.09);box-shadow:0 0 0 1px rgba(255,255,255,.14) inset}.order-search :deep(.el-input__inner){color:#fff}.order-search :deep(.el-input-group__append){background:#ff6542;color:#fff;border:0}.orders-content{padding:28px 0 70px}.order-tabs{display:flex;gap:5px;border-bottom:1px solid var(--line);margin-bottom:20px;overflow:auto}.order-tabs button{border:0;background:none;padding:12px 16px;color:var(--muted);font-size:11px;cursor:pointer;border-bottom:2px solid transparent;white-space:nowrap}.order-tabs button span{margin-left:5px;color:#b2aba0;font-size:8px}.order-tabs button.active{color:var(--orange);border-color:var(--orange);font-weight:700}.orders-list{display:grid;gap:12px}.order-card{padding:0;overflow:hidden}.order-card-head{display:grid;grid-template-columns:1fr 1fr auto;align-items:center;padding:13px 18px;background:#faf8f4;border-bottom:1px solid var(--line)}.order-no{font-family:Consolas,monospace;color:#596476;font-size:9px}.order-date{text-align:center;color:#aaa39a;font-size:8px}.order-card-body{display:grid;grid-template-columns:78px 1fr 170px;gap:17px;align-items:center;padding:17px 18px}.order-card-body>img{width:78px;height:110px;object-fit:cover;border-radius:7px}.order-movie h2{font-size:17px;margin:0 0 11px}.order-movie p{display:flex;align-items:center;gap:5px;color:var(--muted);font-size:9px;margin:6px 0}.order-amount{text-align:right;border-left:1px solid var(--line);padding-left:20px}.order-amount>span{display:block;color:var(--muted);font-size:8px}.order-amount strong{display:block;font-size:24px;color:var(--orange);margin:5px 0}.order-amount strong small{font-size:12px}.order-amount>small{color:#aaa39a;font-size:8px}.pickup-strip{display:flex;align-items:center;gap:8px;background:#f2fbf8;border-top:1px solid #dff3ed;border-bottom:1px solid #dff3ed;padding:9px 18px;color:#53a892;font-size:8px}.pickup-strip strong{font-size:13px;color:#269d84;letter-spacing:2px}.pickup-strip small{margin-left:auto;color:#a5b9b3}.order-actions{display:flex;justify-content:space-between;padding:9px 18px}.order-actions>div{display:flex;gap:5px}.drawer-content{display:grid;gap:20px}.drawer-status{display:flex;justify-content:space-between;align-items:center}.drawer-status span{color:#aaa39a;font-family:Consolas,monospace;font-size:9px}.drawer-movie{display:flex;gap:15px;padding:15px;background:#faf8f4;border-radius:12px}.drawer-movie img{width:70px;height:100px;object-fit:cover;border-radius:6px}.drawer-movie h2{font-size:17px;margin:4px 0 12px}.drawer-movie p{font-size:10px;margin:0 0 6px}.drawer-movie small{color:var(--muted);font-size:8px}.detail-list{display:grid}.detail-list div{display:flex;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--line);font-size:10px}.detail-list span{color:var(--muted)}.detail-list b{text-align:right;max-width:65%;font-weight:600}.drawer-code{text-align:center;background:#0f1825;color:#fff;border-radius:12px;padding:17px}.drawer-code>span{display:block;color:rgba(255,255,255,.4);font-size:8px}.drawer-code>strong{display:block;font-size:28px;letter-spacing:5px;color:#ffb86b;margin:5px 0 12px}.fake-qr{width:120px;height:120px;margin:auto;padding:7px;background:#fff;display:grid;grid-template-columns:repeat(8,1fr);gap:2px;border-radius:4px}.fake-qr i{background:#fff}.fake-qr i.dark{background:#111}.drawer-code small{display:block;color:rgba(255,255,255,.3);font-size:7px;margin-top:8px}
@media(max-width:760px){.orders-hero .page{display:block}.orders-hero h1{font-size:42px}.order-search{width:100%;margin-top:23px}.order-card-head{grid-template-columns:1fr auto}.order-date{display:none}.order-card-body{grid-template-columns:65px 1fr}.order-card-body>img{width:65px;height:92px}.order-amount{grid-column:2;border:0;padding:10px 0 0;text-align:left;border-top:1px solid var(--line)}.order-amount strong{font-size:18px;display:inline;margin-left:5px}.order-amount>span,.order-amount>small{display:inline;margin-right:5px}.pickup-strip small{display:none}.order-actions{padding:8px 12px}.order-actions>div .el-button:first-child{display:none}}
</style>
