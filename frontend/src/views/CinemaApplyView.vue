<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { auth } from '@/stores/auth'

const router = useRouter()
const loading = ref(false)
const step = ref(0)
const form = reactive({ username: '', password: '', name: '', phone: '', email: '', cinemaName: '', brand: '', city: '南京', address: '', intro: '', facilities: '' })
async function submit() {
  if (!form.username || !form.password || !form.name || !form.cinemaName || !form.address) return ElMessage.warning('请完整填写必填信息')
  loading.value = true
  try {
    const { data } = await http.post('/auth/register-cinema', form)
    auth.state.token = data.token; auth.state.user = data.user
    localStorage.setItem('tunan_token', data.token); localStorage.setItem('tunan_user', JSON.stringify(data.user))
    ElMessage.success('入驻申请已提交，平台将在1个工作日内审核')
    router.replace('/cinema-admin/dashboard')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="apply-page">
    <header class="apply-header"><router-link to="/" class="brand"><img src="/favicon.svg" /><span><strong>兔南电影</strong><small>TUNAN CINEMA</small></span></router-link><router-link to="/login">已有账号，去登录 <el-icon><ArrowRight /></el-icon></router-link></header>
    <div class="apply-hero">
      <div class="apply-hero-inner">
        <div><span class="kicker">CINEMA PARTNER</span><h1>让好电影<br />在你的银幕相遇</h1><p>加入兔南电影影院网络，获得线上排片、选座售票、订单核销与经营数据服务。</p></div>
        <div class="apply-steps"><div :class="{ active: step===0 }"><b>01</b><span>填写资料</span></div><i></i><div :class="{ active: step===1 }"><b>02</b><span>平台审核</span></div><i></i><div :class="{ active: step===2 }"><b>03</b><span>开启经营</span></div></div>
      </div>
    </div>
    <div class="apply-content">
      <div class="apply-note"><span class="note-index">01</span><div><h3>入驻前请准备</h3><p>影院名称、经营地址、联系电话与管理员账号。提交后平台将进行认证审核，审核通过后即可创建影厅与申请排片。</p></div></div>
      <el-form class="apply-form" label-position="top" size="large">
        <div class="form-section"><div class="form-section-title"><span>01</span><h3>管理员账号</h3></div>
          <div class="form-grid">
            <el-form-item label="登录账号 *"><el-input v-model="form.username" placeholder="至少3个字符" /></el-form-item>
            <el-form-item label="登录密码 *"><el-input v-model="form.password" type="password" show-password placeholder="至少6位" /></el-form-item>
            <el-form-item label="联系人姓名 *"><el-input v-model="form.name" placeholder="影院负责人姓名" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="用于接收审核通知" /></el-form-item>
            <el-form-item class="full" label="邮箱"><el-input v-model="form.email" placeholder="选填" /></el-form-item>
          </div>
        </div>
        <div class="form-section"><div class="form-section-title"><span>02</span><h3>影院信息</h3></div>
          <div class="form-grid">
            <el-form-item label="影院名称 *"><el-input v-model="form.cinemaName" placeholder="例如：兔南国际影城·万象店" /></el-form-item>
            <el-form-item label="品牌名称"><el-input v-model="form.brand" placeholder="连锁品牌或独立影院" /></el-form-item>
            <el-form-item label="所在城市"><el-select v-model="form.city" class="w-full"><el-option label="南京" value="南京" /><el-option label="苏州" value="苏州" /><el-option label="杭州" value="杭州" /></el-select></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
            <el-form-item class="full" label="详细地址 *"><el-input v-model="form.address" placeholder="区 / 街道 / 商场楼层" /></el-form-item>
            <el-form-item class="full" label="影院简介"><el-input v-model="form.intro" type="textarea" :rows="3" placeholder="介绍影厅配置和特色服务" /></el-form-item>
            <el-form-item class="full" label="配套设施"><el-input v-model="form.facilities" placeholder="例如：IMAX,杜比全景声,免费停车,无障碍通道" /></el-form-item>
          </div>
        </div>
        <div class="submit-row"><span>提交即代表同意《影院入驻服务协议》</span><el-button type="primary" size="large" :loading="loading" @click="submit">提交入驻申请 <el-icon><ArrowRight /></el-icon></el-button></div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.apply-page { background: #f7f5f0; min-height: 100vh; }
.apply-header { height: 76px; max-width: 1180px; margin: 0 auto; display: flex; justify-content: space-between; align-items: center; padding: 0 24px; }
.brand { display: flex; gap: 10px; align-items: center; }
.brand img { width: 39px; height: 39px; border-radius: 12px; }.brand span { display:grid; gap:3px; }.brand strong { font-size:16px; }.brand small { font-size:8px; letter-spacing:2px; color:#9b9489; }
.apply-header > a:last-child { color: var(--orange); font-size: 12px; display:flex; align-items:center; gap:5px; }
.apply-hero { background: #0c1421; color: #fff; position: relative; overflow: hidden; }
.apply-hero::before { content:''; position:absolute; inset:0; background: url('/posters/original/love-myth-wide.jpg') center 25%/cover; opacity:.28; }
.apply-hero::after { content:''; position:absolute; inset:0; background:linear-gradient(90deg,rgba(8,14,23,.97),rgba(8,14,23,.45)); }
.apply-hero-inner { position:relative; z-index:1; max-width:1180px; margin:auto; padding:70px 24px; display:flex; justify-content:space-between; align-items:flex-end; gap:40px; }
.kicker { color: #ffb76a; font-size:9px; letter-spacing:2.8px; font-weight:800; }
.apply-hero h1 { margin:15px 0 18px; font-size:clamp(40px,5vw,70px); line-height:1.07; letter-spacing:-3px; }.apply-hero p { max-width:520px; color:rgba(255,255,255,.57); line-height:1.8; font-size:13px; }
.apply-steps { display:flex; align-items:center; padding-bottom:10px; }.apply-steps div { display:grid; gap:7px; min-width:70px; }.apply-steps b { font-family:Consolas,monospace; font-size:11px; color:rgba(255,255,255,.33); }.apply-steps span { font-size:11px; color:rgba(255,255,255,.45); }.apply-steps .active b,.apply-steps .active span { color:#ffb76a; }.apply-steps i { width:45px; height:1px; background:rgba(255,255,255,.16); margin:0 10px 12px; }
.apply-content { max-width:1000px; margin:-1px auto 0; padding:42px 24px 80px; }
.apply-note { display:flex; gap:18px; padding:20px 24px; background:#fff7ee; border:1px solid #f2ddc2; border-radius:14px; margin-bottom:24px; }.note-index { color:#f0b45b; font-weight:800; font-size:12px; }.apply-note h3 { margin:0 0 5px; font-size:14px; }.apply-note p { margin:0; color:var(--muted); font-size:12px; line-height:1.7; }
.apply-form { background:#fff; border:1px solid var(--line); border-radius:18px; padding:28px; box-shadow:var(--shadow-sm); }.form-section + .form-section { margin-top:28px; padding-top:28px; border-top:1px solid var(--line); }.form-section-title { display:flex; gap:11px; align-items:center; margin-bottom:20px; }.form-section-title span { color:var(--orange); font-family:Consolas,monospace; font-size:11px; }.form-section-title h3 { margin:0; font-size:17px; }
:deep(.el-form-item__label) { font-size:11px; color:#596476; }.submit-row { display:flex; justify-content:space-between; align-items:center; margin-top:28px; padding-top:24px; border-top:1px solid var(--line); }.submit-row span { color:#a29b90; font-size:10px; }
@media(max-width:760px){.apply-hero-inner{display:block;padding:50px 22px}.apply-steps{margin-top:30px}.apply-form{padding:20px}.submit-row{flex-direction:column;align-items:stretch;gap:15px}}
</style>
