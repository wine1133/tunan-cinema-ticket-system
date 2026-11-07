<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const notices = ref([])
const visible = ref(false)
const editingId = ref(null)
const form = reactive({ title: '', content: '', type: '系统公告', status: 1 })
async function load() { loading.value = true; try { const { data } = await http.get('/admin/notices'); notices.value = data } finally { loading.value = false } }
function open(item) { editingId.value = item?.id || null; Object.assign(form, item || { title: '', content: '', type: '系统公告', status: 1 }); visible.value = true }
async function save() { if (!form.title || !form.content) return ElMessage.warning('请填写公告标题和内容'); if (editingId.value) await http.put(`/admin/notices/${editingId.value}`, form); else await http.post('/admin/notices', form); ElMessage.success('公告已保存'); visible.value = false; load() }
async function remove(item) { await ElMessageBox.confirm('确定删除这条公告吗？', '删除公告', { type: 'warning' }); await http.delete(`/admin/notices/${item.id}`); ElMessage.success('已删除'); load() }
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>PUBLIC NOTICES</span><h2>公告管理</h2><p>发布平台通知、观影指南和影院公告。</p></div><el-button type="primary" :icon="Plus" @click="open()">发布公告</el-button></div><div v-loading="loading" class="notice-admin-list"><article v-for="(item,index) in notices" :key="item.id" class="notice-admin-card surface"><div class="notice-number">{{ String(index+1).padStart(2,'0') }}</div><div class="notice-admin-main"><div><span>{{ item.type }}</span><small>{{ item.createdAt }}</small><el-tag size="small" :type="item.status ? 'success':'info'" effect="plain" round>{{ item.status?'已发布':'已隐藏' }}</el-tag></div><h3>{{ item.title }}</h3><p>{{ item.content }}</p></div><div class="notice-admin-actions"><el-button link @click="open(item)">编辑</el-button><el-button link type="danger" @click="remove(item)">删除</el-button></div></article></div><el-dialog v-model="visible" :title="editingId?'编辑公告':'发布公告'" width="min(580px,92vw)"><el-form label-position="top"><el-form-item label="公告标题"><el-input v-model="form.title" /></el-form-item><div class="form-grid"><el-form-item label="公告类型"><el-select v-model="form.type" class="w-full"><el-option label="系统公告" value="系统公告" /><el-option label="观影指南" value="观影指南" /><el-option label="影院公告" value="影院公告" /></el-select></el-form-item><el-form-item label="发布状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="发布" /></el-form-item></div><el-form-item label="公告内容"><el-input v-model="form.content" type="textarea" :rows="5" /></el-form-item></el-form><template #footer><el-button @click="visible=false">取消</el-button><el-button type="primary" @click="save">保存公告</el-button></template></el-dialog></div>
</template>
<script>
import { Plus } from '@element-plus/icons-vue'
export default { components: { Plus } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.notice-admin-list{display:grid;gap:10px}.notice-admin-card{display:grid;grid-template-columns:45px 1fr auto;gap:15px;padding:17px 20px;align-items:center}.notice-number{font-family:Consolas,monospace;font-size:22px;color:#d9d2c7}.notice-admin-main>div{display:flex;align-items:center;gap:8px}.notice-admin-main>div>span{color:var(--orange);font-size:8px;background:var(--orange-soft);padding:3px 6px;border-radius:4px}.notice-admin-main small{color:#aaa39a;font-size:8px}.notice-admin-main h3{font-size:13px;margin:7px 0 4px}.notice-admin-main p{font-size:9px;color:var(--muted);margin:0;line-height:1.6;display:-webkit-box;-webkit-line-clamp:1;-webkit-box-orient:vertical;overflow:hidden}.notice-admin-actions{display:flex}
@media(max-width:600px){.page-intro{display:block}.page-intro>.el-button{margin-top:12px}.notice-admin-card{grid-template-columns:32px 1fr;padding:14px}.notice-admin-actions{grid-column:2;justify-content:flex-end}.notice-number{font-size:16px}}
</style>
