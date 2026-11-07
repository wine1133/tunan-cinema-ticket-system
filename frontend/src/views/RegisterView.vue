<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { auth } from '@/stores/auth'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '', confirm: '', name: '', phone: '', email: '' })
async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('请填写账号和密码')
  if (form.password !== form.confirm) return ElMessage.warning('两次输入的密码不一致')
  loading.value = true
  try {
    const { data } = await http.post('/auth/register', form)
    auth.state.token = data.token
    auth.state.user = data.user
    localStorage.setItem('tunan_token', data.token)
    localStorage.setItem('tunan_user', JSON.stringify(data.user))
    ElMessage.success('注册成功，开始选一部电影吧')
    router.replace('/')
  } finally { loading.value = false }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-art">
      <router-link to="/" class="back-home"><el-icon><ArrowLeft /></el-icon> 返回首页</router-link>
      <div class="art-poster"><img src="/posters/original/panda-plan.jpg" alt="熊猫计划" /></div>
      <div class="art-copy"><span>JOIN TUNAN CINEMA</span><h1>每一次相遇<br />都从一场电影开始</h1><p>收藏喜欢的电影，记录每一次评分，找到下一部让你心动的故事。</p></div>
      <div class="art-orbit"></div>
    </div>
    <div class="auth-form-wrap">
      <div class="auth-form">
        <router-link to="/"><img class="mini-logo" src="/favicon.svg" alt="兔南电影" /></router-link>
        <div class="kicker">CREATE ACCOUNT</div>
        <h2>注册影迷账号</h2>
        <p>加入兔南电影，开启你的观影清单</p>
        <el-form label-position="top" size="large">
          <div class="form-grid">
            <el-form-item label="账号"><el-input v-model="form.username" placeholder="3-20个字符" /></el-form-item>
            <el-form-item label="昵称"><el-input v-model="form.name" placeholder="你的观影昵称" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="form.password" type="password" show-password placeholder="至少6位" /></el-form-item>
            <el-form-item label="确认密码"><el-input v-model="form.confirm" type="password" show-password placeholder="再次输入密码" /></el-form-item>
            <el-form-item label="手机号"><el-input v-model="form.phone" placeholder="选填" /></el-form-item>
            <el-form-item label="邮箱"><el-input v-model="form.email" placeholder="选填" /></el-form-item>
          </div>
          <el-button type="primary" class="w-full submit" :loading="loading" @click="submit">创建账号</el-button>
        </el-form>
        <div class="switch-line">已有账号？<router-link to="/login">直接登录</router-link></div>
        <div class="cinema-link">想入驻影院？<router-link to="/cinema-apply">提交影院认证申请</router-link></div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.auth-page { min-height: 100vh; display: grid; grid-template-columns: 43% 57%; background: #fbfaf7; }
.auth-art { position: relative; overflow: hidden; background: linear-gradient(145deg,#142b46,#2f6080 55%,#d17567); padding: 34px; min-height: 100vh; color: white; display: flex; flex-direction: column; }
.auth-art::after { content:''; position:absolute; inset:0; background:linear-gradient(0deg,rgba(6,12,20,.92),transparent 65%); }
.back-home { position: relative; z-index: 2; display: inline-flex; gap: 7px; align-items: center; color: rgba(255,255,255,.7); font-size: 12px; width: fit-content; }
.art-poster { position: absolute; z-index: 1; width: 250px; right: 7%; top: 5%; transform: rotate(8deg); opacity: .85; filter: saturate(.9); }
.art-poster img { border-radius: 11px; box-shadow: 0 35px 80px rgba(0,0,0,.45); }
.art-copy { position: relative; z-index: 2; margin-top: auto; padding: 20px 0 8%; }
.art-copy span { color: #ffd193; font-size: 9px; letter-spacing: 2.6px; }
.art-copy h1 { font-size: clamp(35px,4vw,63px); line-height: 1.12; letter-spacing: -2px; margin: 16px 0 20px; }
.art-copy p { max-width: 390px; color: rgba(255,255,255,.58); font-size: 13px; line-height: 1.8; }
.art-orbit { position: absolute; width: 420px; height: 420px; border: 1px solid rgba(255,255,255,.16); border-radius: 50%; right: -200px; bottom: 10%; z-index: 1; }
.art-orbit::before { content:''; position:absolute; width: 10px; height:10px; background:#ffb864; border-radius:50%; left: 40px; top: 70px; box-shadow: 0 0 30px #ffb864; }
.auth-form-wrap { display: grid; place-items: center; padding: 55px 40px; }
.auth-form { width: min(100%, 560px); }
.mini-logo { width: 43px; height: 43px; border-radius: 13px; margin-bottom: 32px; }
.kicker { color: var(--orange); font-size: 9px; font-weight: 800; letter-spacing: 2.5px; }
.auth-form h2 { margin: 10px 0 6px; font-size: 31px; letter-spacing: -1px; }
.auth-form > p { margin: 0 0 30px; color: var(--muted); font-size: 12px; }
:deep(.el-form-item__label) { font-size: 11px; color: #4c5768; padding-bottom: 5px; }
:deep(.el-input__wrapper) { min-height: 46px; border-radius: 10px; box-shadow: 0 0 0 1px var(--line) inset; }
.submit { height: 48px; border-radius: 11px; margin-top: 8px; }
.switch-line, .cinema-link { text-align: center; color: var(--muted); font-size: 12px; margin-top: 20px; }
.switch-line a, .cinema-link a { color: var(--orange); font-weight: 700; }
.cinema-link { margin-top: 10px; font-size: 10px; }
@media (max-width: 900px) { .auth-page { grid-template-columns: 1fr; } .auth-art { display:none; } .auth-form-wrap { padding: 35px 22px 55px; } }
</style>
