<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const shows = ref([])
const movies = ref([])
const rooms = ref([])
const cinema = ref({})
const filter = ref('ALL')
const visible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const empty = () => ({ movieId: null, roomId: null, startTime: '', price: 39.9, language: '国语', version: '2D' })
const form = reactive(empty())
const statusMap = { PENDING: ['待审批','warning'], APPROVED: ['已通过','success'], REJECTED: ['已驳回','danger'] }
const filtered = computed(() => filter.value === 'ALL' ? shows.value : shows.value.filter(item => item.approvalStatus === filter.value))
async function load() {
  loading.value = true
  try {
    const [c, m, r, s] = await Promise.all([http.get('/cinema/info'), http.get('/cinema/movie-options'), http.get('/cinema/rooms'), http.get('/cinema/showings')])
    cinema.value = c.data; movies.value = m.data; rooms.value = r.data; shows.value = s.data
  } finally { loading.value = false }
}
function open(item) { editingId.value = item?.id || null; Object.assign(form, item ? { movieId: item.movieId, roomId: item.roomId, startTime: item.startTime, price: Number(item.price), language: item.language, version: item.version } : empty()); visible.value = true }
async function save() {
  if (!form.movieId || !form.roomId || !form.startTime) return ElMessage.warning('请完整填写电影、影厅与放映时间')
  saving.value = true
  try { if (editingId.value) await http.put(`/cinema/showings/${editingId.value}`, form); else await http.post('/cinema/showings', form); ElMessage.success('排片申请已提交，等待平台审批'); visible.value = false; load() } finally { saving.value = false }
}
async function remove(item) { await ElMessageBox.confirm('确定删除这条排片申请吗？', '删除排片', { type: 'warning' }); await http.delete(`/cinema/showings/${item.id}`); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>SCREEN SCHEDULING</span><h2>放映场次管理</h2><p>提交排片申请，平台审核通过后自动展示给用户。</p></div><el-button type="primary" :icon="Plus" :disabled="cinema.status !== 'APPROVED'" @click="open()">申请新排片</el-button></div><el-alert v-if="cinema.status !== 'APPROVED'" title="影院通过平台认证后才能提交排片申请" type="warning" show-icon :closable="false" style="margin-bottom:15px" /><div class="summary-row"><div class="surface"><strong>{{ shows.length }}</strong><span>全部排片</span></div><div class="surface"><strong>{{ shows.filter(i=>i.approvalStatus==='PENDING').length }}</strong><span>待审批</span></div><div class="surface"><strong>{{ shows.filter(i=>i.approvalStatus==='APPROVED').length }}</strong><span>已通过</span></div><div class="surface"><strong>{{ shows.filter(i=>i.approvalStatus==='REJECTED').length }}</strong><span>已驳回</span></div></div><div class="table-card"><div class="table-toolbar"><div class="filter-tabs"><button v-for="item in [{v:'ALL',l:'全部'},{v:'PENDING',l:'待审批'},{v:'APPROVED',l:'已通过'},{v:'REJECTED',l:'已驳回'}]" :key="item.v" :class="{active:filter===item.v}" @click="filter=item.v">{{ item.l }}</button></div><span class="muted" style="font-size:9px">已售 {{ shows.reduce((sum,item)=>sum+Number(item.soldCount||0),0) }} 张</span></div><el-table v-loading="loading" :data="filtered" stripe><el-table-column label="电影" min-width="190"><template #default="{row}"><div class="movie-cell"><img :src="row.poster" /><div><strong>{{ row.movieTitle }}</strong><small>{{ row.language }} · {{ row.version }}</small></div></div></template></el-table-column><el-table-column prop="roomName" label="影厅" width="125"/><el-table-column prop="startTime" label="放映时间" width="145"/><el-table-column label="票价" width="80"><template #default="{row}"><b class="text-orange">¥{{ Number(row.price).toFixed(1) }}</b></template></el-table-column><el-table-column label="已售" width="70"><template #default="{row}">{{ row.soldCount }}张</template></el-table-column><el-table-column label="审批状态" width="95"><template #default="{row}"><el-tag :type="statusMap[row.approvalStatus]?.[1]" effect="light" round>{{ statusMap[row.approvalStatus]?.[0] }}</el-tag></template></el-table-column><el-table-column label="审核意见" min-width="130"><template #default="{row}"><span class="muted" style="font-size:9px">{{ row.auditRemark || '—' }}</span></template></el-table-column><el-table-column label="操作" width="110" fixed="right"><template #default="{row}"><template v-if="['PENDING','REJECTED'].includes(row.approvalStatus)"><el-button link @click="open(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template><span v-else class="muted" style="font-size:8px">已排片</span></template></el-table-column></el-table></div>
    <el-dialog v-model="visible" :title="editingId?'编辑排片申请':'申请新排片'" width="min(590px,92vw)"><el-form label-position="top"><div class="form-grid"><el-form-item label="选择电影" class="full"><el-select v-model="form.movieId" filterable class="w-full" placeholder="选择电影"><el-option v-for="movie in movies" :key="movie.id" :label="`${movie.title}（${movie.status==='NOW'?'热映':'待映'}）`" :value="movie.id" /></el-select></el-form-item><el-form-item label="选择影厅"><el-select v-model="form.roomId" class="w-full"><el-option v-for="room in rooms" :key="room.id" :label="room.name" :value="room.id" /></el-select></el-form-item><el-form-item label="放映时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" placeholder="选择时间" /></el-form-item><el-form-item label="票价"><el-input-number v-model="form.price" :min="1" :step="5" :precision="2" class="w-full" /></el-form-item><el-form-item label="语言版本"><el-select v-model="form.language" class="w-full"><el-option label="国语" value="国语" /><el-option label="英语" value="英语" /><el-option label="日语" value="日语" /></el-select></el-form-item><el-form-item label="放映格式"><el-select v-model="form.version" class="w-full"><el-option label="2D" value="2D" /><el-option label="3D" value="3D" /><el-option label="IMAX 3D" value="IMAX 3D" /><el-option label="杜比全景声" value="杜比全景声" /></el-select></el-form-item></div><div class="form-note"><el-icon><InfoFilled /></el-icon> 放映结束时间将根据影片片长自动计算，提交后由平台审核。</div></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">提交申请</el-button></template></el-dialog>
  </div>
</template>
<script>
import { Plus } from '@element-plus/icons-vue'
export default { components: { Plus } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.summary-row{display:grid;grid-template-columns:repeat(4,1fr);gap:11px;margin-bottom:14px}.summary-row>div{padding:12px 16px;display:flex;align-items:baseline;gap:8px}.summary-row strong{font-size:21px}.summary-row span{color:var(--muted);font-size:8px}.filter-tabs{display:flex;gap:4px}.filter-tabs button{border:0;background:none;padding:7px 9px;border-radius:6px;color:var(--muted);font-size:9px;cursor:pointer}.filter-tabs button.active{background:var(--orange-soft);color:var(--orange);font-weight:700}.movie-cell{display:flex;gap:9px;align-items:center}.movie-cell img{width:39px;height:55px;object-fit:cover;border-radius:4px}.movie-cell strong{display:block;font-size:10px}.movie-cell small{display:block;color:#aaa39a;font-size:7px;margin-top:4px}.form-note{display:flex;align-items:center;gap:6px;font-size:9px;color:#9d968c;background:#faf8f4;border-radius:7px;padding:10px}
@media(max-width:660px){.summary-row{grid-template-columns:repeat(2,1fr)}.page-intro{display:block}.page-intro>.el-button{margin-top:12px}.filter-tabs{overflow:auto}.table-toolbar{display:block}}
</style>
