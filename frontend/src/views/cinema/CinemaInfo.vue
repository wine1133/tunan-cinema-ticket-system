<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'

const loading = ref(true)
const saving = ref(false)
const status = ref('APPROVED')
const auditRemark = ref('')
const form = reactive({ name: '', brand: '', city: '', address: '', phone: '', intro: '', facilities: '' })
const statusMap = { APPROVED: ['已认证','success'], PENDING: ['审核中','warning'], REJECTED: ['未通过','danger'] }
async function load() { loading.value = true; try { const { data } = await http.get('/cinema/info'); Object.assign(form, data); status.value = data.status; auditRemark.value = data.auditRemark } finally { loading.value = false } }
async function save() {
  if (!form.name || !form.address) return ElMessage.warning('影院名称和地址不能为空')
  saving.value = true
  try { await http.put('/cinema/info', form); ElMessage.success('影院资料已更新，等待平台重新认证'); load() } finally { saving.value = false }
}
onMounted(load)
</script>
<template>
  <div v-loading="loading" class="cinema-info-page"><div class="page-intro"><div><span>CINEMA PROFILE</span><h2>影院资料</h2><p>维护影院公开展示信息，修改后需要平台重新审核。</p></div><el-tag :type="statusMap[status]?.[1]" effect="dark" round>{{ statusMap[status]?.[0] }}</el-tag></div><el-alert v-if="status==='REJECTED'" :title="`认证未通过：${auditRemark || '请完善资料后重新提交'}`" type="error" show-icon :closable="false" style="margin-bottom:15px" /><el-alert v-else-if="status==='PENDING'" title="资料正在审核中，审核通过后即可恢复排片权限" type="warning" show-icon :closable="false" style="margin-bottom:15px" /><div class="info-layout"><section class="surface info-form"><div class="section-head"><div><span>01</span><h3>公开信息</h3></div><small>用户将在影院详情页看到这些内容</small></div><el-form label-position="top" size="large"><div class="form-grid"><el-form-item label="影院名称"><el-input v-model="form.name" /></el-form-item><el-form-item label="品牌名称"><el-input v-model="form.brand" /></el-form-item><el-form-item label="所在城市"><el-select v-model="form.city" class="w-full"><el-option label="南京" value="南京" /><el-option label="苏州" value="苏州" /><el-option label="杭州" value="杭州" /></el-select></el-form-item><el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item><el-form-item label="详细地址" class="full"><el-input v-model="form.address" /></el-form-item><el-form-item label="影院简介" class="full"><el-input v-model="form.intro" type="textarea" :rows="4" /></el-form-item><el-form-item label="配套设施" class="full"><el-input v-model="form.facilities" placeholder="用英文逗号分隔，例如：IMAX,免费停车" /></el-form-item></div><el-button type="primary" :loading="saving" @click="save">保存并提交审核</el-button></el-form></section><aside class="surface info-preview"><div class="section-head"><div><span>PREVIEW</span><h3>展示预览</h3></div></div><img :src="`/cinemas/cinema-1.svg`" /><span class="preview-label">平台认证影院</span><h3>{{ form.name || '影院名称' }}</h3><p><el-icon><Location /></el-icon>{{ form.address || '影院详细地址' }}</p><div class="preview-tags"><span v-for="item in form.facilities?.split(',')" :key="item">{{ item }}</span></div><div class="preview-note"><el-icon><InfoFilled /></el-icon><span>修改认证信息会重新进入平台审核流程，审核期间已有排片与订单不受影响。</span></div></aside></div></div>
</template>
<style scoped>
.page-intro{display:flex;justify-content:space-between;align-items:flex-end;margin-bottom:20px}.page-intro>div>span{color:var(--orange);font-size:8px;letter-spacing:2px}.page-intro h2{font-size:25px;margin:7px 0 5px}.page-intro p{color:var(--muted);font-size:10px}.info-layout{display:grid;grid-template-columns:1fr 310px;gap:15px;align-items:start}.info-form,.info-preview{padding:22px}.section-head{display:flex;justify-content:space-between;align-items:center;padding-bottom:15px;margin-bottom:18px;border-bottom:1px solid var(--line)}.section-head>div{display:flex;align-items:center;gap:9px}.section-head span{color:var(--orange);font-family:Consolas,monospace;font-size:8px}.section-head h3{margin:0;font-size:16px}.section-head small{color:#aaa39a;font-size:8px}.info-preview>img{width:100%;height:150px;object-fit:cover;border-radius:9px;margin-bottom:14px}.preview-label{display:inline-block;color:#2fa88e;background:#eaf8f4;padding:4px 7px;border-radius:5px;font-size:7px}.info-preview h3{font-size:18px;margin:9px 0 7px}.info-preview>p{display:flex;align-items:center;gap:4px;color:var(--muted);font-size:8px;margin:0}.preview-tags{display:flex;flex-wrap:wrap;gap:5px;margin-top:13px}.preview-tags span{background:#f5f2ec;color:#777064;padding:4px 7px;font-size:7px;border-radius:4px}.preview-note{display:flex;gap:7px;margin-top:18px;padding:11px;background:#faf8f4;border-radius:7px;color:#9d968c;font-size:8px;line-height:1.6}.preview-note .el-icon{flex-shrink:0;margin-top:1px}
@media(max-width:900px){.info-layout{grid-template-columns:1fr}.info-preview{display:none}}@media(max-width:600px){.page-intro{display:block}.page-intro>.el-tag{margin-top:12px}.info-form{padding:17px}}
</style>
