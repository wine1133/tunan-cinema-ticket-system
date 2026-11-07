<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const users = ref([])
const filter = ref('ALL')
const keyword = ref('')
const adminDialog = ref(false)
const adminForm = reactive({ username: '', password: '', name: '', phone: '', email: '' })
const roleMap = { USER: ['影迷用户','success'], CINEMA: ['影院管理员','warning'], ADMIN: ['平台管理员','danger'] }
const filtered = computed(() => users.value.filter(user => (filter.value === 'ALL' || user.role === filter.value) && (!keyword.value || user.name.includes(keyword.value) || user.username.includes(keyword.value))))
async function load() { loading.value = true; try { const { data } = await http.get('/admin/users'); users.value = data } finally { loading.value = false } }
async function toggle(user) { const status = user.status === 1 ? 0 : 1; await http.put(`/admin/users/${user.id}/status`, { status }); user.status = status; ElMessage.success(status ? '账号已启用' : '账号已停用') }
async function createAdmin() { if (!adminForm.username || !adminForm.password) return ElMessage.warning('请填写账号和密码'); await http.post('/admin/admins', adminForm); ElMessage.success('管理员已创建'); adminDialog.value = false; load() }
onMounted(load)
</script>
<template>
  <div class="admin-page"><div class="page-intro"><div><span>MEMBERS & ACCESS</span><h2>用户与权限</h2><p>管理影迷、影院管理员与平台管理员账号。</p></div><el-button type="primary" :icon="Plus" @click="adminDialog=true">新增管理员</el-button></div><div class="user-stats"><div class="surface"><strong>{{ users.length }}</strong><span>全部账号</span></div><div class="surface"><strong>{{ users.filter(i=>i.role==='USER').length }}</strong><span>影迷用户</span></div><div class="surface"><strong>{{ users.filter(i=>i.role==='CINEMA').length }}</strong><span>影院管理员</span></div><div class="surface"><strong>{{ users.filter(i=>i.role==='ADMIN').length }}</strong><span>平台管理员</span></div></div><div class="table-card"><div class="table-toolbar"><div class="filter-tabs"><button v-for="item in [{v:'ALL',l:'全部'},{v:'USER',l:'影迷'},{v:'CINEMA',l:'影院'},{v:'ADMIN',l:'管理员'}]" :key="item.v" :class="{active:filter===item.v}" @click="filter=item.v">{{ item.l }}</button></div><el-input v-model="keyword" placeholder="搜索姓名或账号" :prefix-icon="Search" clearable style="width:240px" /></div><el-table v-loading="loading" :data="filtered" stripe><el-table-column label="用户" min-width="180"><template #default="{row}"><div class="user-cell"><span class="avatar">{{ row.name?.slice(0,1) }}</span><div><strong>{{ row.name }}</strong><small>@{{ row.username }}</small></div></div></template></el-table-column><el-table-column label="角色" width="115"><template #default="{row}"><el-tag :type="roleMap[row.role]?.[1]" effect="light" round>{{ roleMap[row.role]?.[0] }}</el-tag></template></el-table-column><el-table-column prop="phone" label="手机号" width="130" /><el-table-column prop="email" label="邮箱" min-width="170" /><el-table-column label="注册时间" width="145"><template #default="{row}">{{ row.createdAt }}</template></el-table-column><el-table-column label="状态" width="85"><template #default="{row}"><el-tag :type="row.status ? 'success' : 'info'" effect="plain" round>{{ row.status ? '正常' : '停用' }}</el-tag></template></el-table-column><el-table-column label="操作" width="80"><template #default="{row}"><el-button link :type="row.status ? 'danger' : 'success'" @click="toggle(row)">{{ row.status ? '停用' : '启用' }}</el-button></template></el-table-column></el-table></div><el-dialog v-model="adminDialog" title="新增平台管理员" width="min(460px,92vw)"><el-form label-position="top"><div class="form-grid"><el-form-item label="登录账号"><el-input v-model="adminForm.username" /></el-form-item><el-form-item label="登录密码"><el-input v-model="adminForm.password" type="password" show-password /></el-form-item><el-form-item label="显示名称"><el-input v-model="adminForm.name" /></el-form-item><el-form-item label="手机号"><el-input v-model="adminForm.phone" /></el-form-item><el-form-item class="full" label="邮箱"><el-input v-model="adminForm.email" /></el-form-item></div></el-form><template #footer><el-button @click="adminDialog=false">取消</el-button><el-button type="primary" @click="createAdmin">创建管理员</el-button></template></el-dialog></div>
</template>
<script>
import { Search, Plus } from '@element-plus/icons-vue'
export default { components: { Search, Plus } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.user-stats{display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:15px}.user-stats>div{padding:13px 16px}.user-stats strong{display:block;font-size:22px}.user-stats span{color:var(--muted);font-size:8px}.user-cell{display:flex;align-items:center;gap:9px}.user-cell .avatar{width:31px;height:31px;font-size:10px}.user-cell strong{display:block;font-size:10px}.user-cell small{display:block;color:var(--muted);font-size:8px;margin-top:3px}.filter-tabs{display:flex;gap:4px}.filter-tabs button{border:0;background:none;padding:7px 9px;border-radius:6px;color:var(--muted);font-size:9px;cursor:pointer}.filter-tabs button.active{background:var(--orange-soft);color:var(--orange);font-weight:700}
@media(max-width:700px){.user-stats{grid-template-columns:repeat(2,1fr)}.page-intro{display:block}.page-intro>.el-button{margin-top:12px}.table-toolbar{display:block}.table-toolbar .el-input{width:100%!important;margin-top:10px}.filter-tabs{overflow:auto}}
</style>
