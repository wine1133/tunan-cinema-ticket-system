<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const rooms = ref([])
const cinemas = ref([])
const keyword = ref('')
const visible = ref(false)
const editingId = ref(null)
const form = reactive({ cinemaId: null, name: '', seatRows: 8, seatCols: 8, roomType: '激光厅', status: 1 })
const filtered = computed(() => rooms.value.filter(room => !keyword.value || room.name.includes(keyword.value) || room.cinemaName.includes(keyword.value)))
async function load() { loading.value = true; try { const [r, c] = await Promise.all([http.get('/admin/rooms'), http.get('/admin/cinemas')]); rooms.value = r.data; cinemas.value = c.data } finally { loading.value = false } }
function open(item) { editingId.value = item?.id || null; Object.assign(form, item ? { cinemaId: item.cinemaId, name: item.name, seatRows: item.seatRows, seatCols: item.seatCols, roomType: item.roomType, status: item.status } : { cinemaId: cinemas.value[0]?.id || null, name: '', seatRows: 8, seatCols: 8, roomType: '激光厅', status: 1 }); visible.value = true }
async function save() { if (!form.cinemaId || !form.name) return ElMessage.warning('请选择影院并填写影厅名称'); if (editingId.value) await http.put(`/admin/rooms/${editingId.value}`, form); else await http.post('/admin/rooms', form); ElMessage.success('影厅信息已保存'); visible.value = false; load() }
async function remove(room) { await ElMessageBox.confirm(`确定删除“${room.cinemaName} · ${room.name}”吗？已关联排片时无法删除。`, '删除影厅', { type: 'warning' }); await http.delete(`/admin/rooms/${room.id}`); ElMessage.success('影厅已删除'); load() }
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>ALL SCREEN ROOMS</span><h2>全站影厅管理</h2><p>统一维护平台所有影院的放映厅与座位布局。</p></div><el-button type="primary" :icon="Plus" @click="open()">新增影厅</el-button></div><div class="room-stats"><div class="surface"><strong>{{ rooms.length }}</strong><span>全部影厅</span></div><div class="surface"><strong>{{ rooms.filter(i=>i.status===1).length }}</strong><span>运营中</span></div><div class="surface"><strong>{{ rooms.reduce((sum,i)=>sum+Number(i.seatRows)*Number(i.seatCols),0) }}</strong><span>总座位数</span></div></div><div class="table-card"><div class="table-toolbar"><h3>影厅列表</h3><el-input v-model="keyword" placeholder="搜索影院或影厅" :prefix-icon="Search" clearable style="width:250px" /></div><el-table v-loading="loading" :data="filtered" stripe><el-table-column prop="cinemaName" label="所属影院" min-width="180" /><el-table-column prop="name" label="影厅名称" min-width="150" /><el-table-column prop="roomType" label="类型" width="120" /><el-table-column label="座位布局" width="115"><template #default="{row}">{{ row.seatRows }} 行 × {{ row.seatCols }} 列</template></el-table-column><el-table-column label="座位数" width="90"><template #default="{row}">{{ row.seatRows * row.seatCols }}</template></el-table-column><el-table-column label="状态" width="85"><template #default="{row}"><el-tag :type="row.status ? 'success':'info'" effect="plain" round>{{ row.status?'运营中':'已停用' }}</el-tag></template></el-table-column><el-table-column prop="createdAt" label="创建时间" width="145" /><el-table-column label="操作" width="105" fixed="right"><template #default="{row}"><el-button link @click="open(row)">编辑</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column></el-table></div><el-dialog v-model="visible" :title="editingId?'编辑影厅':'新增影厅'" width="min(520px,92vw)"><el-form label-position="top"><el-form-item label="所属影院"><el-select v-model="form.cinemaId" class="w-full"><el-option v-for="cinema in cinemas" :key="cinema.id" :label="cinema.name" :value="cinema.id" /></el-select></el-form-item><el-form-item label="影厅名称"><el-input v-model="form.name" placeholder="例如：3号激光厅" /></el-form-item><el-form-item label="影厅类型"><el-select v-model="form.roomType" class="w-full"><el-option label="激光厅" value="激光厅" /><el-option label="激光IMAX" value="激光IMAX" /><el-option label="杜比全景声" value="杜比全景声" /><el-option label="情侣厅" value="情侣厅" /><el-option label="亲子厅" value="亲子厅" /><el-option label="艺术厅" value="艺术厅" /></el-select></el-form-item><div class="form-grid"><el-form-item label="座位行数"><el-input-number v-model="form.seatRows" :min="1" :max="20" class="w-full" /></el-form-item><el-form-item label="座位列数"><el-input-number v-model="form.seatCols" :min="1" :max="20" class="w-full" /></el-form-item></div><el-form-item label="运营状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" /></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存影厅</el-button></template></el-dialog></div>
</template>
<script>
import { Search, Plus } from '@element-plus/icons-vue'
export default { components: { Search, Plus } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.room-stats{display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:15px}.room-stats>div{padding:13px 16px}.room-stats strong{display:block;font-size:22px}.room-stats span{color:var(--muted);font-size:8px}
@media(max-width:600px){.page-intro{display:block}.page-intro>.el-button{margin-top:12px}.room-stats{grid-template-columns:1fr}.table-toolbar{display:block}.table-toolbar .el-input{width:100%!important;margin-top:10px}}
</style>
