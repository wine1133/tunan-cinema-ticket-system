<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const cinemas = ref([])
const filter = ref('PENDING')
const detailVisible = ref(false)
const current = ref({})
const statusMap = { PENDING: ['待认证', 'warning'], APPROVED: ['已认证', 'success'], REJECTED: ['已驳回', 'danger'] }
const filtered = () => filter.value === 'ALL' ? cinemas.value : cinemas.value.filter(item => item.status === filter.value)
async function load() { loading.value = true; try { const { data } = await http.get('/admin/cinemas'); cinemas.value = data } finally { loading.value = false } }
function view(row) { current.value = row; detailVisible.value = true }
async function audit(row, status) {
  let remark = status === 'APPROVED' ? '影院资质审核通过' : '请补充完整经营资质后重新提交'
  if (status === 'REJECTED') { const res = await ElMessageBox.prompt('请填写驳回原因', '驳回影院认证', { confirmButtonText: '确认驳回', cancelButtonText: '取消' }); remark = res.value }
  else await ElMessageBox.confirm(`确认通过“${row.name}”的影院认证吗？`, '通过认证', { type: 'success' })
  await http.post(`/admin/cinemas/${row.id}/audit`, { status, remark })
  ElMessage.success(status === 'APPROVED' ? '影院认证已通过' : '影院认证已驳回'); detailVisible.value = false; load()
}
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>CINEMA NETWORK</span><h2>影院认证管理</h2><p>管理平台影院网络，审核新影院的入驻资质。</p></div></div>
    <div class="cinema-stat-row"><div class="cinema-summary-card surface"><div class="sum-icon orange"><el-icon><OfficeBuilding /></el-icon></div><div><strong>{{ cinemas.filter(i=>i.status==='APPROVED').length }}</strong><span>已认证影院</span></div></div><div class="cinema-summary-card surface"><div class="sum-icon gold"><el-icon><Clock /></el-icon></div><div><strong>{{ cinemas.filter(i=>i.status==='PENDING').length }}</strong><span>等待认证</span></div></div><div class="cinema-summary-card surface"><div class="sum-icon blue"><el-icon><Location /></el-icon></div><div><strong>南京</strong><span>当前运营城市</span></div></div></div>
    <div class="filter-tabs"><button v-for="item in [{v:'PENDING',l:'待认证'},{v:'APPROVED',l:'已认证'},{v:'REJECTED',l:'已驳回'},{v:'ALL',l:'全部影院'}]" :key="item.v" :class="{active:filter===item.v}" @click="filter=item.v">{{ item.l }}</button></div>
    <div class="table-card"><el-table v-loading="loading" :data="filtered()" stripe><el-table-column label="影院信息" min-width="250"><template #default="{row}"><div class="cinema-cell"><img :src="row.cover" /><div><strong>{{ row.name }}</strong><small>{{ row.address }}</small><span>{{ row.ownerName || '平台录入' }}</span></div></div></template></el-table-column><el-table-column prop="city" label="城市" width="80" /><el-table-column label="影厅" width="75"><template #default="{row}">{{ row.roomCount }}个</template></el-table-column><el-table-column label="认证状态" width="100"><template #default="{row}"><el-tag :type="statusMap[row.status]?.[1]" effect="light" round>{{ statusMap[row.status]?.[0] }}</el-tag></template></el-table-column><el-table-column prop="createdAt" label="申请时间" width="145" /><el-table-column label="操作" width="175" fixed="right"><template #default="{row}"><el-button link @click="view(row)">详情</el-button><template v-if="row.status==='PENDING'"><el-button link type="success" @click="audit(row,'APPROVED')">通过</el-button><el-button link type="danger" @click="audit(row,'REJECTED')">驳回</el-button></template></template></el-table-column></el-table></div>
    <el-drawer v-model="detailVisible" title="影院认证详情" size="min(480px, 94vw)"><div v-if="current.id" class="cinema-detail-drawer"><img class="drawer-cover" :src="current.cover" /><h2>{{ current.name }}</h2><p>{{ current.address }}</p><div class="detail-info-grid"><div><span>品牌</span><b>{{ current.brand || '—' }}</b></div><div><span>城市</span><b>{{ current.city }}</b></div><div><span>联系电话</span><b>{{ current.phone || '—' }}</b></div><div><span>申请人</span><b>{{ current.ownerName || '平台录入' }}</b></div><div><span>影厅数量</span><b>{{ current.roomCount }} 个</b></div><div><span>申请时间</span><b>{{ current.createdAt }}</b></div></div><div class="detail-intro"><span>影院简介</span><p>{{ current.intro || '暂无简介' }}</p></div><div v-if="current.status==='PENDING'" class="audit-actions"><el-button type="danger" plain @click="audit(current,'REJECTED')">驳回认证</el-button><el-button type="primary" @click="audit(current,'APPROVED')">通过认证</el-button></div></div></el-drawer>
  </div>
</template>
<style scoped>
.page-intro{margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.cinema-stat-row{display:grid;grid-template-columns:repeat(3,1fr);gap:13px;margin-bottom:17px}.cinema-summary-card{display:flex;align-items:center;gap:12px;padding:14px 17px}.sum-icon{width:38px;height:38px;border-radius:10px;display:grid;place-items:center;font-size:17px}.sum-icon.orange{background:#fff0eb;color:#ff6542}.sum-icon.gold{background:#fff8e9;color:#dca53e}.sum-icon.blue{background:#edf4fc;color:#4d7fc3}.cinema-summary-card strong{display:block;font-size:19px}.cinema-summary-card span{color:var(--muted);font-size:8px}.filter-tabs{display:flex;gap:5px;margin-bottom:14px}.filter-tabs button{border:1px solid var(--line);background:#fff;border-radius:8px;padding:8px 13px;color:var(--muted);font-size:10px;cursor:pointer}.filter-tabs button.active{border-color:#ffb09c;background:var(--orange-soft);color:var(--orange)}.cinema-cell{display:flex;gap:11px;align-items:center}.cinema-cell img{width:72px;height:48px;object-fit:cover;border-radius:5px}.cinema-cell strong{display:block;font-size:10px}.cinema-cell small{display:block;color:var(--muted);font-size:7px;margin:3px 0}.cinema-cell span{display:block;color:#aaa39a;font-size:7px}.cinema-detail-drawer{display:grid;gap:17px}.drawer-cover{width:100%;height:190px;object-fit:cover;border-radius:10px}.cinema-detail-drawer h2{margin:0;font-size:22px}.cinema-detail-drawer>p{color:var(--muted);font-size:10px;margin:-10px 0 0}.detail-info-grid{display:grid;grid-template-columns:1fr 1fr;border-top:1px solid var(--line);border-bottom:1px solid var(--line);padding:8px 0}.detail-info-grid div{padding:9px}.detail-info-grid span{display:block;color:var(--muted);font-size:8px}.detail-info-grid b{display:block;font-size:10px;margin-top:4px}.detail-intro>span{color:var(--muted);font-size:8px}.detail-intro p{font-size:10px;line-height:1.8;margin:7px 0 0}.audit-actions{display:grid;grid-template-columns:1fr 1fr;gap:8px}
@media(max-width:700px){.cinema-stat-row{grid-template-columns:1fr}.filter-tabs{overflow:auto}.filter-tabs button{white-space:nowrap}}
</style>
