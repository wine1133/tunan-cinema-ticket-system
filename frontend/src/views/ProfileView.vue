<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { auth } from '@/stores/auth'

const saving = ref(false)
const passwordSaving = ref(false)
const orderCount = ref(0)
const favoriteCount = ref(0)
const profile = reactive({ name: '', phone: '', email: '', avatar: '' })
const password = reactive({ currentPassword: '', newPassword: '', confirm: '' })
async function load() {
  const [me, orders, favorites] = await Promise.allSettled([http.get('/auth/me'), http.get('/user/orders'), http.get('/user/favorites')])
  if (me.status === 'fulfilled') { Object.assign(profile, me.value.data); auth.setUser(me.value.data) }
  if (orders.status === 'fulfilled') orderCount.value = orders.value.data.length
  if (favorites.status === 'fulfilled') favoriteCount.value = favorites.value.data.length
}
async function saveProfile() {
  saving.value = true
  try { const { data } = await http.put('/auth/profile', profile); auth.setUser(data); ElMessage.success('个人资料已保存') } finally { saving.value = false }
}
async function savePassword() {
  if (password.newPassword !== password.confirm) return ElMessage.warning('两次输入的新密码不一致')
  passwordSaving.value = true
  try { await http.put('/auth/password', password); ElMessage.success('密码修改成功'); password.currentPassword = password.newPassword = password.confirm = '' } finally { passwordSaving.value = false }
}
onMounted(load)
</script>
<template>
  <div class="profile-page"><section class="profile-hero"><div class="page profile-hero-inner"><div class="big-avatar">{{ auth.state.user?.name?.slice(0,1) }}</div><div><span>MEMBER PROFILE</span><h1>{{ auth.state.user?.name }}</h1><p>@{{ auth.state.user?.username }} · {{ auth.state.user?.role === 'USER' ? '兔南影迷' : '平台管理员' }}</p></div><div class="profile-metrics"><div><strong>{{ orderCount }}</strong><span>观影订单</span></div><div><strong>{{ favoriteCount }}</strong><span>收藏电影</span></div></div></div></section><div class="page profile-content"><aside class="profile-nav surface"><router-link to="/profile" class="active"><el-icon><User /></el-icon>个人资料</router-link><router-link to="/favorites"><el-icon><Star /></el-icon>我的收藏</router-link><router-link to="/orders"><el-icon><Tickets /></el-icon>购票记录</router-link><router-link to="/notices"><el-icon><Bell /></el-icon>公告中心</router-link></aside><main class="profile-main"><section class="surface profile-section"><div class="section-head"><div><span>01</span><h2>基本资料</h2></div><small>修改后将同步到你的兔南账号</small></div><el-form label-position="top" size="large"><div class="form-grid"><el-form-item label="账号"><el-input :model-value="auth.state.user?.username" disabled /></el-form-item><el-form-item label="昵称"><el-input v-model="profile.name" /></el-form-item><el-form-item label="手机号"><el-input v-model="profile.phone" /></el-form-item><el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item></div><el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button></el-form></section><section class="surface profile-section"><div class="section-head"><div><span>02</span><h2>修改密码</h2></div><small>建议定期更换登录密码</small></div><el-form label-position="top" size="large"><div class="form-grid"><el-form-item label="当前密码"><el-input v-model="password.currentPassword" type="password" show-password /></el-form-item><el-form-item></el-form-item><el-form-item label="新密码"><el-input v-model="password.newPassword" type="password" show-password /></el-form-item><el-form-item label="确认新密码"><el-input v-model="password.confirm" type="password" show-password /></el-form-item></div><el-button type="primary" plain :loading="passwordSaving" @click="savePassword">更新密码</el-button></el-form></section></main></div></div>
</template>
<style scoped>
.profile-page{min-height:70vh}.profile-hero{background:linear-gradient(110deg,#0f1927,#263a4d 60%,#4a4640);color:#fff;padding:45px 0}.profile-hero-inner{display:flex;align-items:center;gap:19px}.big-avatar{width:82px;height:82px;border-radius:50%;display:grid;place-items:center;background:linear-gradient(145deg,#ffb185,#ff6442);font-size:30px;font-weight:900;box-shadow:0 10px 30px rgba(255,101,66,.25)}.profile-hero-inner>div:nth-child(2)>span{color:#ffb76a;font-size:8px;letter-spacing:2px}.profile-hero h1{font-size:34px;margin:7px 0 5px}.profile-hero p{color:rgba(255,255,255,.42);font-size:10px;margin:0}.profile-metrics{margin-left:auto;display:flex;border:1px solid rgba(255,255,255,.13);border-radius:12px;background:rgba(255,255,255,.05)}.profile-metrics div{padding:14px 28px;text-align:center}.profile-metrics div+div{border-left:1px solid rgba(255,255,255,.1)}.profile-metrics strong{display:block;font-size:22px}.profile-metrics span{color:rgba(255,255,255,.38);font-size:8px}.profile-content{display:grid;grid-template-columns:200px 1fr;gap:20px;padding:28px 0 70px;align-items:start}.profile-nav{padding:10px;display:grid;gap:3px;position:sticky;top:95px}.profile-nav a{display:flex;align-items:center;gap:9px;padding:11px 12px;border-radius:8px;color:#677183;font-size:11px}.profile-nav a.active{background:var(--orange-soft);color:var(--orange);font-weight:700}.profile-main{display:grid;gap:15px}.profile-section{padding:24px}.section-head{display:flex;justify-content:space-between;align-items:center;padding-bottom:18px;margin-bottom:18px;border-bottom:1px solid var(--line)}.section-head>div{display:flex;align-items:center;gap:10px}.section-head>div>span{color:var(--orange);font-family:Consolas,monospace;font-size:9px}.section-head h2{font-size:17px;margin:0}.section-head>small{color:#aaa39a;font-size:8px}
@media(max-width:720px){.profile-content{grid-template-columns:1fr}.profile-nav{position:static;display:flex;overflow:auto}.profile-nav a{white-space:nowrap}.profile-metrics{display:none}.profile-hero h1{font-size:28px}.profile-section{padding:17px}.section-head>small{display:none}}
</style>
