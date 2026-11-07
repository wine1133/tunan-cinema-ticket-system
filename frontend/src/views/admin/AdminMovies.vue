<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const movies = ref([])
const genres = ref([])
const keyword = ref('')
const dialog = ref(false)
const castDrawer = ref(false)
const saving = ref(false)
const editingId = ref(null)
const currentMovie = ref({})
const cast = ref([])
const castForm = reactive({ name: '', roleName: '', sortNo: 99 })
const emptyForm = () => ({ title: '', englishTitle: '', director: '', duration: 120, releaseDate: '', status: 'NOW', score: 0, boxOffice: 0, wantCount: 0, poster: '/posters/original/love-myth.jpg', backdrop: '/posters/original/love-myth-wide.jpg', trailerUrl: 'https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4', synopsis: '', isFeatured: 0, genreIds: [] })
const form = reactive(emptyForm())
const filtered = () => movies.value.filter(movie => !keyword.value || movie.title.includes(keyword.value) || movie.director?.includes(keyword.value))
async function load() { loading.value = true; try { const [m, g] = await Promise.all([http.get('/admin/movies'), http.get('/admin/genres')]); movies.value = m.data; genres.value = g.data } finally { loading.value = false } }
function openCreate() { Object.assign(form, emptyForm()); editingId.value = null; dialog.value = true }
function openEdit(movie) {
  editingId.value = movie.id
  Object.assign(form, { ...movie, genreIds: movie.genreNames ? movie.genreNames.split(',').map(name => genres.value.find(g => g.name === name)?.id).filter(Boolean) : [] })
  dialog.value = true
}
async function save() {
  if (!form.title) return ElMessage.warning('请填写电影名称')
  saving.value = true
  try {
    const payload = { ...form, isFeatured: form.isFeatured ? 1 : 0 }
    if (editingId.value) await http.put(`/admin/movies/${editingId.value}`, payload)
    else await http.post('/admin/movies', payload)
    ElMessage.success(editingId.value ? '电影信息已更新' : '电影已创建')
    dialog.value = false; load()
  } finally { saving.value = false }
}
async function remove(movie) {
  await ElMessageBox.confirm(`确定删除《${movie.title}》吗？该操作不可撤销。`, '删除电影', { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' })
  await http.delete(`/admin/movies/${movie.id}`); ElMessage.success('电影已删除'); load()
}
async function openCast(movie) { currentMovie.value = movie; castDrawer.value = true; const { data } = await http.get(`/admin/movies/${movie.id}/cast`); cast.value = data }
async function addCast() {
  if (!castForm.name) return ElMessage.warning('请填写演职人员姓名')
  await http.post(`/admin/movies/${currentMovie.value.id}/cast`, castForm)
  ElMessage.success('已添加'); const { data } = await http.get(`/admin/movies/${currentMovie.value.id}/cast`); cast.value = data; castForm.name = ''; castForm.roleName = ''
}
async function removeCast(item) { await http.delete(`/admin/cast/${item.id}`); cast.value = cast.value.filter(i => i.id !== item.id); ElMessage.success('已删除') }
function statusText(status) { return { NOW: '正在热映', SOON: '即将上映', OFFLINE: '已下映' }[status] || status }
onMounted(load)
</script>

<template>
  <div class="admin-page">
    <div class="page-intro"><div><span>CONTENT LIBRARY</span><h2>电影内容管理</h2><p>维护电影资料、类型、状态与演职人员信息。</p></div><el-button type="primary" size="large" :icon="Plus" @click="openCreate">新增电影</el-button></div>
    <div class="table-card"><div class="table-toolbar"><h3>全部电影 <span class="count-badge">{{ movies.length }}</span></h3><el-input v-model="keyword" placeholder="搜索电影 / 导演" :prefix-icon="Search" clearable style="width:260px" /></div>
      <el-table v-loading="loading" :data="filtered()" stripe>
        <el-table-column label="电影信息" min-width="240"><template #default="{row}"><div class="movie-cell"><img :src="row.poster" /><div><strong>{{ row.title }}</strong><small>{{ row.englishTitle }}</small><span>{{ row.genreNames?.split(',').join(' / ') }}</span></div></div></template></el-table-column>
        <el-table-column prop="director" label="导演" width="100" />
        <el-table-column prop="duration" label="片长" width="80"><template #default="{row}">{{ row.duration }}分钟</template></el-table-column>
        <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="row.status === 'NOW' ? 'danger' : row.status === 'SOON' ? 'warning' : 'info'" effect="light" round>{{ statusText(row.status) }}</el-tag></template></el-table-column>
        <el-table-column label="评分" width="80"><template #default="{row}"><b class="score-text">{{ Number(row.score).toFixed(1) }}</b></template></el-table-column>
        <el-table-column label="累计票房" width="110"><template #default="{row}">¥{{ Number(row.boxOffice).toFixed(1) }}万</template></el-table-column>
        <el-table-column label="推荐" width="70"><template #default="{row}"><el-icon v-if="row.isFeatured" color="#ff6542"><StarFilled /></el-icon><span v-else class="muted">—</span></template></el-table-column>
        <el-table-column label="操作" width="205" fixed="right"><template #default="{row}"><el-button link type="primary" @click="openEdit(row)">编辑</el-button><el-button link @click="openCast(row)">演职人员</el-button><el-button link type="danger" @click="remove(row)">删除</el-button></template></el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialog" :title="editingId ? '编辑电影' : '新增电影'" width="min(820px, 94vw)" destroy-on-close>
      <el-form label-position="top" class="movie-form"><div class="dialog-grid">
        <div class="poster-field"><img :src="form.poster" /><el-input v-model="form.poster" placeholder="海报路径" /></div>
        <div class="form-grid">
          <el-form-item label="电影名称 *"><el-input v-model="form.title" /></el-form-item><el-form-item label="英文片名"><el-input v-model="form.englishTitle" /></el-form-item>
          <el-form-item label="导演"><el-input v-model="form.director" /></el-form-item><el-form-item label="片长（分钟）"><el-input-number v-model="form.duration" :min="1" class="w-full" /></el-form-item>
          <el-form-item label="上映日期"><el-date-picker v-model="form.releaseDate" type="date" value-format="YYYY-MM-DD" class="w-full" /></el-form-item><el-form-item label="状态"><el-select v-model="form.status" class="w-full"><el-option label="正在热映" value="NOW" /><el-option label="即将上映" value="SOON" /><el-option label="已下映" value="OFFLINE" /></el-select></el-form-item>
          <el-form-item label="评分"><el-input-number v-model="form.score" :min="0" :max="10" :step="0.1" class="w-full" /></el-form-item><el-form-item label="累计票房（万）"><el-input-number v-model="form.boxOffice" :min="0" :step="10" class="w-full" /></el-form-item>
          <el-form-item label="电影类型" class="full"><el-select v-model="form.genreIds" multiple class="w-full" placeholder="最多3个类型"><el-option v-for="genre in genres" :key="genre.id" :label="genre.name" :value="genre.id" /></el-select></el-form-item>
          <el-form-item label="剧情简介" class="full"><el-input v-model="form.synopsis" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="首页推荐"><el-switch v-model="form.isFeatured" :active-value="1" :inactive-value="0" /></el-form-item>
        </div>
      </div></el-form>
      <template #footer><el-button @click="dialog=false">取消</el-button><el-button type="primary" :loading="saving" @click="save">保存电影</el-button></template>
    </el-dialog>

    <el-drawer v-model="castDrawer" :title="`《${currentMovie.title}》演职人员`" size="min(460px, 94vw)">
      <div class="cast-add"><el-input v-model="castForm.name" placeholder="姓名" /><el-input v-model="castForm.roleName" placeholder="饰演角色" /><el-button type="primary" @click="addCast">添加</el-button></div>
      <div class="cast-manage-list"><div v-for="item in cast" :key="item.id"><span class="avatar">{{ item.name.slice(0,1) }}</span><div><strong>{{ item.name }}</strong><small>饰 {{ item.roleName }}</small></div><el-button link type="danger" @click="removeCast(item)">移除</el-button></div><div v-if="!cast.length" class="empty-state"><p>暂未添加演职人员</p></div></div>
    </el-drawer>
  </div>
</template>

<script>
import { Search, Plus } from '@element-plus/icons-vue'
export default { components: { Search, Plus } }
</script>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.count-badge{margin-left:5px;padding:2px 6px;background:var(--orange-soft);color:var(--orange);border-radius:5px;font-size:9px}.movie-cell{display:flex;gap:10px;align-items:center}.movie-cell img{width:45px;height:64px;object-fit:cover;border-radius:5px}.movie-cell strong{display:block;font-size:11px}.movie-cell small{display:block;color:#aaa39a;font-size:7px;margin:3px 0}.movie-cell span{display:block;color:var(--muted);font-size:8px}.score-text{color:var(--orange);font-size:14px}.dialog-grid{display:grid;grid-template-columns:150px 1fr;gap:22px}.poster-field{display:grid;gap:10px;align-content:start}.poster-field img{width:150px;height:225px;object-fit:cover;border-radius:9px;background:#eee}.cast-add{display:grid;grid-template-columns:1fr 1fr auto;gap:7px;padding-bottom:18px;border-bottom:1px solid var(--line)}.cast-manage-list{padding-top:10px}.cast-manage-list>div{display:grid;grid-template-columns:38px 1fr auto;align-items:center;gap:10px;padding:10px 0;border-bottom:1px solid var(--line)}.cast-manage-list .avatar{width:36px;height:36px;font-size:11px}.cast-manage-list strong{display:block;font-size:11px}.cast-manage-list small{display:block;color:var(--muted);font-size:8px;margin-top:3px}
@media(max-width:600px){.dialog-grid{grid-template-columns:1fr}.poster-field{grid-template-columns:85px 1fr;align-items:end}.poster-field img{width:85px;height:127px}.page-intro{display:block}.page-intro>.el-button{margin-top:14px}.cast-add{grid-template-columns:1fr}.table-toolbar{display:block}.table-toolbar .el-input{margin-top:10px;width:100%!important}}
</style>
