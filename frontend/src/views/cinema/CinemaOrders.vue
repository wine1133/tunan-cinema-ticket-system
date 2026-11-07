<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const orders = ref([])
const filter = ref('PAID')
const keyword = ref('')
const statusMap = { UNPAID: ['待支付','warning'], PAID: ['待取票','success'], USED: ['已取票','info'], CANCELLED: ['已取消','info'] }
const filtered = computed(() => orders.value.filter(o => (filter.value === 'ALL' || o.status === filter.value) && (!keyword.value || o.orderNo.includes(keyword.value) || o.userName.includes(keyword.value) || o.movieTitle.includes(keyword.value))))
async function load() { loading.value = true; try { const { data } = await http.get('/cinema/orders'); orders.value = data } finally { loading.value = false } }
async function pickup(order) { await ElMessageBox.confirm(`确认为订单 ${order.orderNo} 核销取票吗？`, '取票核销', { type: 'success', confirmButtonText: '确认核销' }); await http.post(`/cinema/orders/${order.id}/pickup`); ElMessage.success('取票核销成功'); load() }
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>TICKETING DESK</span><h2>购票订单</h2><p>查询本影院订单并为到店观众完成取票核销。</p></div><div class="pickup-count"><strong>{{ orders.filter(i=>i.status==='PAID').length }}</strong><span>笔待取票</span></div></div><div class="table-card"><div class="table-toolbar"><div class="filter-tabs"><button v-for="item in [{v:'PAID',l:'待取票'},{v:'ALL',l:'全部'},{v:'USED',l:'已取票'},{v:'CANCELLED',l:'已取消'},{v:'UNPAID',l:'待支付'}]" :key="item.v" :class="{active:filter===item.v}" @click="filter=item.v">{{ item.l }}</button></div><el-input v-model="keyword" placeholder="订单号 / 用户 / 电影" :prefix-icon="Search" clearable style="width:255px" /></div><el-table v-loading="loading" :data="filtered" stripe><el-table-column prop="orderNo" label="订单号" width="160"><template #default="{row}"><span class="mono" style="font-size:9px">{{ row.orderNo }}</span></template></el-table-column><el-table-column label="观众" width="105"><template #default="{row}"><div><b style="font-size:10px">{{ row.userName }}</b><div class="muted" style="font-size:8px">{{ row.userPhone }}</div></div></template></el-table-column><el-table-column prop="movieTitle" label="电影" min-width="115" /><el-table-column prop="startTime" label="放映时间" width="140" /><el-table-column prop="roomName" label="影厅" width="105" /><el-table-column prop="seatNames" label="座位" min-width="130"><template #default="{row}"><span style="font-size:9px;color:#6a7484">{{ row.seatNames }}</span></template></el-table-column><el-table-column label="金额" width="80"><template #default="{row}"><b class="text-orange">¥{{ Number(row.amount).toFixed(1) }}</b></template></el-table-column><el-table-column label="取票码" width="90"><template #default="{row}"><span class="mono pickup-code">{{ row.pickupCode }}</span></template></el-table-column><el-table-column label="状态" width="85"><template #default="{row}"><el-tag :type="statusMap[row.status]?.[1]" effect="light" round>{{ statusMap[row.status]?.[0] }}</el-tag></template></el-table-column><el-table-column label="操作" width="95" fixed="right"><template #default="{row}"><el-button v-if="row.status==='PAID'" size="small" type="success" round @click="pickup(row)">核销取票</el-button><span v-else class="muted" style="font-size:8px">—</span></template></el-table-column></el-table></div></div>
</template>
<script>
import { Search } from '@element-plus/icons-vue'
export default { components: { Search } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.pickup-count{display:flex;align-items:baseline;gap:6px;background:#eaf8f4;color:#249f84;padding:9px 14px;border-radius:9px}.pickup-count strong{font-size:21px}.pickup-count span{font-size:8px}.filter-tabs{display:flex;gap:4px}.filter-tabs button{border:0;background:none;padding:7px 9px;border-radius:6px;color:var(--muted);font-size:9px;cursor:pointer}.filter-tabs button.active{background:var(--orange-soft);color:var(--orange);font-weight:700}.pickup-code{color:#279c83;font-weight:700;letter-spacing:1px}
@media(max-width:650px){.page-intro{display:block}.pickup-count{margin-top:12px;width:fit-content}.table-toolbar{display:block}.table-toolbar .el-input{width:100%!important;margin-top:10px}.filter-tabs{overflow:auto}}
</style>
