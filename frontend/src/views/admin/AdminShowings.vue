<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const shows = ref([])
const filter = ref('PENDING')
const statusMap = { PENDING: ['待审批', 'warning'], APPROVED: ['已通过', 'success'], REJECTED: ['已驳回', 'danger'] }
const filtered = computed(() => filter.value === 'ALL' ? shows.value : shows.value.filter(item => item.approvalStatus === filter.value))
async function load() { loading.value = true; try { const { data } = await http.get('/admin/showings'); shows.value = data } finally { loading.value = false } }
async function audit(row, status) {
  let remark = status === 'APPROVED' ? '排片方案合理，审核通过' : '请调整放映时间后重新提交'
  if (status === 'REJECTED') {
    const result = await ElMessageBox.prompt('请填写驳回原因', '驳回排片申请', { confirmButtonText: '确认驳回', cancelButtonText: '取消', inputPlaceholder: '例如：影厅时间冲突' })
    remark = result.value
  } else {
    await ElMessageBox.confirm(`确认通过《${row.movieTitle}》的排片申请吗？`, '通过排片', { type: 'success' })
  }
  await http.post(`/admin/showings/${row.id}/audit`, { status, remark })
  ElMessage.success(status === 'APPROVED' ? '排片申请已通过' : '排片申请已驳回'); load()
}
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>SCHEDULE REVIEW</span><h2>排片审批中心</h2><p>审核影院提交的放映申请，保障排片合理与观影体验。</p></div><div class="approval-summary"><div><strong>{{ shows.filter(i=>i.approvalStatus==='PENDING').length }}</strong><span>待审批</span></div><div><strong>{{ shows.filter(i=>i.approvalStatus==='APPROVED').length }}</strong><span>已通过</span></div></div></div>
    <div class="filter-tabs"><button v-for="item in [{v:'PENDING',l:'待审批'},{v:'APPROVED',l:'已通过'},{v:'REJECTED',l:'已驳回'},{v:'ALL',l:'全部'}].values()" :key="item.v" :class="{active:filter===item.v}" @click="filter=item.v">{{ item.l }} <span>{{ item.v==='ALL'?shows.length:shows.filter(i=>i.approvalStatus===item.v).length }}</span></button></div>
    <div class="table-card"><el-table v-loading="loading" :data="filtered" stripe><el-table-column label="电影" min-width="200"><template #default="{row}"><div class="movie-cell"><img :src="row.poster" /><div><strong>{{ row.movieTitle }}</strong><small>{{ row.language }} · {{ row.version }}</small></div></div></template></el-table-column><el-table-column prop="cinemaName" label="影院" min-width="160" /><el-table-column prop="roomName" label="影厅" width="120" /><el-table-column prop="startTime" label="放映时间" width="150" /><el-table-column label="票价" width="80"><template #default="{row}"><b class="text-orange">¥{{ Number(row.price).toFixed(1) }}</b></template></el-table-column><el-table-column label="审批状态" width="95"><template #default="{row}"><el-tag :type="statusMap[row.approvalStatus]?.[1]" effect="light" round>{{ statusMap[row.approvalStatus]?.[0] }}</el-tag></template></el-table-column><el-table-column label="审核意见" min-width="130"><template #default="{row}"><span class="muted remark">{{ row.auditRemark || '—' }}</span></template></el-table-column><el-table-column label="操作" width="135" fixed="right"><template #default="{row}"><template v-if="row.approvalStatus==='PENDING'"><el-button link type="success" @click="audit(row,'APPROVED')">通过</el-button><el-button link type="danger" @click="audit(row,'REJECTED')">驳回</el-button></template><span v-else class="muted" style="font-size:9px">已处理</span></template></el-table-column></el-table></div>
  </div>
</template>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.approval-summary{display:flex;border:1px solid var(--line);background:#fff;border-radius:10px;overflow:hidden}.approval-summary div{padding:8px 16px;text-align:center}.approval-summary div+div{border-left:1px solid var(--line)}.approval-summary strong{display:block;font-size:18px;color:var(--orange)}.approval-summary span{color:var(--muted);font-size:8px}.filter-tabs{display:flex;gap:5px;margin-bottom:14px}.filter-tabs button{border:1px solid var(--line);background:#fff;border-radius:8px;padding:8px 13px;color:var(--muted);font-size:10px;cursor:pointer}.filter-tabs button span{font-size:8px;color:#b3aca2}.filter-tabs button.active{border-color:#ffb09c;background:var(--orange-soft);color:var(--orange)}.movie-cell{display:flex;align-items:center;gap:9px}.movie-cell img{width:38px;height:54px;object-fit:cover;border-radius:4px}.movie-cell strong{display:block;font-size:10px}.movie-cell small{display:block;color:#aaa39a;font-size:7px;margin-top:4px}.remark{font-size:9px}
@media(max-width:650px){.approval-summary{display:none}.page-intro{display:block}.filter-tabs{overflow:auto}.filter-tabs button{white-space:nowrap}}
</style>
