<script setup>
import { computed, onMounted, ref } from 'vue'
import http from '@/api/http'

const loading = ref(true)
const orders = ref([])
const filter = ref('ALL')
const keyword = ref('')
const statusMap = { UNPAID: ['待支付','warning'], PAID: ['待取票','success'], USED: ['已取票','info'], CANCELLED: ['已取消','info'] }
const filtered = computed(() => orders.value.filter(order => (filter.value === 'ALL' || order.status === filter.value) && (!keyword.value || order.orderNo.includes(keyword.value) || order.movieTitle.includes(keyword.value) || order.userName.includes(keyword.value))))
async function load() { loading.value = true; try { const { data } = await http.get('/admin/orders'); orders.value = data } finally { loading.value = false } }
function exportCsv() {
  const header = ['订单号','用户','电影','影院','场次','座位','金额','状态','下单时间']
  const rows = filtered.value.map(o => [o.orderNo,o.userName,o.movieTitle,o.cinemaName,o.startTime,o.seatNames,o.amount,statusMap[o.status]?.[0],o.createdAt])
  const csv = [header, ...rows].map(r => r.map(v => `"${String(v ?? '').replaceAll('"','""')}"`).join(',')).join('\n')
  const blob = new Blob(['\ufeff'+csv], { type: 'text/csv;charset=utf-8' }); const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = '兔南电影订单数据.csv'; a.click()
}
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>ORDER OPERATIONS</span><h2>全站订单</h2><p>查看平台所有购票订单与取票状态。</p></div><el-button :icon="Download" @click="exportCsv">导出订单</el-button></div>
    <div class="table-card"><div class="table-toolbar"><div class="filter-tabs"><button v-for="item in [{v:'ALL',l:'全部'},{v:'PAID',l:'待取票'},{v:'USED',l:'已取票'},{v:'CANCELLED',l:'已取消'},{v:'UNPAID',l:'待支付'}]" :key="item.v" :class="{active:filter===item.v}" @click="filter=item.v">{{ item.l }}</button></div><el-input v-model="keyword" placeholder="订单号 / 电影 / 用户" :prefix-icon="Search" clearable style="width:250px" /></div><el-table v-loading="loading" :data="filtered" stripe><el-table-column prop="orderNo" label="订单号" width="160"><template #default="{row}"><span class="mono" style="font-size:9px">{{ row.orderNo }}</span></template></el-table-column><el-table-column label="用户" width="100"><template #default="{row}"><div><b style="font-size:10px">{{ row.userName }}</b><div class="muted" style="font-size:8px">{{ row.userPhone }}</div></div></template></el-table-column><el-table-column prop="movieTitle" label="电影" min-width="125" /><el-table-column prop="cinemaName" label="影院" min-width="150" /><el-table-column prop="startTime" label="放映时间" width="140" /><el-table-column label="座位" min-width="125"><template #default="{row}"><span class="seat-text">{{ row.seatNames }}</span></template></el-table-column><el-table-column label="金额" width="85"><template #default="{row}"><b class="text-orange">¥{{ Number(row.amount).toFixed(2) }}</b></template></el-table-column><el-table-column label="状态" width="88"><template #default="{row}"><el-tag :type="statusMap[row.status]?.[1]" effect="light" round>{{ statusMap[row.status]?.[0] }}</el-tag></template></el-table-column><el-table-column prop="createdAt" label="下单时间" width="145" /></el-table></div>
  </div>
</template>
<script>
import { Search, Download } from '@element-plus/icons-vue'
export default { components: { Search, Download } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.filter-tabs{display:flex;gap:4px}.filter-tabs button{border:0;background:none;padding:7px 9px;border-radius:6px;color:var(--muted);font-size:9px;cursor:pointer}.filter-tabs button.active{background:var(--orange-soft);color:var(--orange);font-weight:700}.seat-text{color:var(--muted);font-size:9px}
@media(max-width:660px){.page-intro{display:block}.page-intro>.el-button{margin-top:12px}.table-toolbar{display:block}.table-toolbar .el-input{margin-top:10px;width:100%!important}.filter-tabs{overflow:auto}}
</style>
