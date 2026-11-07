<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const genres = ref([])
const visible = ref(false)
const editingId = ref(null)
const form = reactive({ name: '', color: '#ff6542', sortNo: 99 })
async function load() { loading.value = true; try { const { data } = await http.get('/admin/genres'); genres.value = data } finally { loading.value = false } }
function open(item) { editingId.value = item?.id || null; Object.assign(form, item || { name: '', color: '#ff6542', sortNo: 99 }); visible.value = true }
async function save() {
  if (!form.name) return ElMessage.warning('请填写类型名称')
  if (editingId.value) await http.put(`/admin/genres/${editingId.value}`, form); else await http.post('/admin/genres', form)
  ElMessage.success('类型已保存'); visible.value = false; load()
}
async function remove(item) { await ElMessageBox.confirm(`确定删除“${item.name}”类型吗？`, '删除类型', { type: 'warning' }); await http.delete(`/admin/genres/${item.id}`); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>GENRE TAXONOMY</span><h2>电影类型管理</h2><p>维护电影分类体系，用于筛选与数据分析。</p></div><el-button type="primary" :icon="Plus" @click="open()">新增类型</el-button></div><div v-loading="loading" class="genre-admin-grid"><div v-for="(item,index) in genres" :key="item.id" class="genre-admin-card surface"><div class="genre-color" :style="{ background: item.color }"></div><span class="genre-index">{{ String(index+1).padStart(2,'0') }}</span><h3>{{ item.name }}</h3><p>排序 {{ item.sortNo }}</p><div class="genre-actions"><el-button link @click="open(item)">编辑</el-button><el-button link type="danger" @click="remove(item)">删除</el-button></div></div></div>
    <el-dialog v-model="visible" :title="editingId?'编辑类型':'新增类型'" width="min(430px,92vw)"><el-form label-position="top"><el-form-item label="类型名称"><el-input v-model="form.name" /></el-form-item><div class="form-grid"><el-form-item label="标记颜色"><el-color-picker v-model="form.color" /></el-form-item><el-form-item label="排序"><el-input-number v-model="form.sortNo" :min="1" /></el-form-item></div></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template></el-dialog>
  </div>
</template>
<script>
import { Plus } from '@element-plus/icons-vue'
export default { components: { Plus } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.genre-admin-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:13px}.genre-admin-card{position:relative;padding:18px;overflow:hidden}.genre-color{position:absolute;left:0;top:0;bottom:0;width:4px}.genre-index{position:absolute;right:13px;top:8px;font-family:Consolas,monospace;font-size:25px;font-weight:800;color:#f1ede6}.genre-admin-card h3{margin:12px 0 6px;font-size:17px}.genre-admin-card p{color:var(--muted);font-size:8px;margin:0}.genre-actions{margin-top:15px;padding-top:10px;border-top:1px solid var(--line)}
@media(max-width:900px){.genre-admin-grid{grid-template-columns:repeat(3,1fr)}}@media(max-width:600px){.genre-admin-grid{grid-template-columns:repeat(2,1fr)}.page-intro{display:block}.page-intro>.el-button{margin-top:12px}}
</style>
