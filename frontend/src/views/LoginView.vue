<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { auth } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const accounts = [
  { role: '影迷', username: 'tunan', password: '123456', color: '#ff9b64', initial: '影' },
  { role: '影院', username: 'cinema', password: '123456', color: '#56c7b7', initial: '院' },
  { role: '管理员', username: 'admin', password: '123456', color: '#7d91ef', initial: '管' }
]
function fill(account) { form.username = account.username; form.password = account.password }
async function submit() {
  if (!form.username || !form.password) return ElMessage.warning('请输入账号和密码')
  loading.value = true
  try {
    const user = await auth.login(form)
    ElMessage.success(`欢迎回来，${user.name}`)
    const fallback = user.role === 'ADMIN' ? '/admin/dashboard' : user.role === 'CINEMA' ? '/cinema-admin/dashboard' : '/'
    router.replace(route.query.redirect || fallback)
  } finally { loading.value = false }
}
</script>

<template>
  <div class="login-page">
    <section class="login-visual">
      <div class="visual-bg"></div>
      <div class="visual-noise"></div>
      <div class="visual-top">
        <router-link to="/" class="login-brand">
          <img src="/favicon.svg" alt="兔南电影" />
          <span><strong>兔南电影</strong><small>TUNAN CINEMA</small></span>
        </router-link>
        <span class="visual-chip">光影正在发生</span>
      </div>
      <div class="visual-copy">
        <div class="editorial-index">NO. 001 / 2026</div>
        <h1>好故事<br /><em>值得被看见</em></h1>
        <p>从一部电影开始，和更多人共享同一段光影记忆。在线选座、影院比价、评分收藏，都在兔南电影。</p>
        <div class="visual-stats">
          <div><strong>16</strong><span>部精选电影</span></div>
          <div><strong>5</strong><span>家精选影院</span></div>
          <div><strong>23</strong><span>个在线场次</span></div>
        </div>
      </div>
      <div class="visual-poster">
        <img src="/posters/original/love-myth.jpg" alt="爱情神话" />
        <div class="poster-caption"><span>本周编辑推荐</span><strong>爱情神话</strong><small>B FOR BUSY</small></div>
      </div>
      <div class="visual-bottom"><span>南京 · 2026</span><span>SCROLL TO DISCOVER</span></div>
    </section>

    <section class="login-panel">
      <div class="login-box">
        <div class="login-heading">
          <span class="kicker">WELCOME BACK</span>
          <h2>登录兔南电影</h2>
          <p>继续你的下一场观影旅程</p>
        </div>
        <el-form size="large" @submit.prevent="submit">
          <el-form-item>
            <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" @keyup.enter="submit" />
          </el-form-item>
          <div class="form-extra">
            <el-checkbox>记住登录状态</el-checkbox>
            <span>账号遇到问题？</span>
          </div>
          <el-button class="login-submit" type="primary" :loading="loading" @click="submit">
            登录 <el-icon><Right /></el-icon>
          </el-button>
        </el-form>
        <div class="register-line">还没有账号？<router-link to="/register">立即注册</router-link></div>
        <div class="demo-divider"><span>快速体验演示账号</span></div>
        <div class="demo-accounts">
          <button v-for="account in accounts" :key="account.role" class="demo-account" @click="fill(account)">
            <span class="demo-avatar" :style="{ background: account.color }">{{ account.initial }}</span>
            <span><strong>{{ account.role }}</strong><small>{{ account.username }} / 123456</small></span>
            <el-icon><ArrowRight /></el-icon>
          </button>
        </div>
        <router-link to="/cinema-apply" class="cinema-apply-link">我是影院，申请入驻兔南电影 <el-icon><ArrowRight /></el-icon></router-link>
      </div>
      <div class="login-footer">© 2026 兔南电影购票系统 · 隐私政策 · 用户协议</div>
    </section>
  </div>
</template>

<style scoped>
.login-page { min-height: 100vh; display: grid; grid-template-columns: minmax(0, 1.18fr) minmax(440px, .82fr); background: #0c111b; }
.login-visual { position: relative; min-height: 100vh; overflow: hidden; padding: 38px 48px; color: #fff; display: flex; flex-direction: column; }
.visual-bg { position: absolute; inset: 0; background: url('/posters/original/love-myth-wide.jpg') center/cover; transform: scale(1.04); animation: slowZoom 18s ease-in-out infinite alternate; }
.visual-noise { position: absolute; inset: 0; background: linear-gradient(90deg, rgba(5,9,15,.93) 0%, rgba(5,9,15,.55) 52%, rgba(5,9,15,.16) 100%), linear-gradient(0deg, rgba(5,9,15,.9), transparent 45%); }
.login-brand { position: relative; z-index: 2; display: inline-flex; align-items: center; gap: 12px; }
.login-brand img { width: 45px; height: 45px; border-radius: 14px; box-shadow: 0 8px 25px rgba(255,101,66,.3); }
.login-brand span { display: grid; gap: 3px; }
.login-brand strong { font-size: 20px; }
.login-brand small { color: rgba(255,255,255,.45); font-size: 8px; letter-spacing: 2.5px; }
.visual-top { position: relative; z-index: 2; display: flex; justify-content: space-between; align-items: flex-start; }
.visual-chip { padding: 8px 14px; border: 1px solid rgba(255,255,255,.22); border-radius: 999px; font-size: 11px; color: rgba(255,255,255,.7); backdrop-filter: blur(8px); }
.visual-copy { position: relative; z-index: 2; margin: auto 0; max-width: 620px; }
.editorial-index { color: #ffb864; font-family: Consolas, monospace; font-size: 11px; letter-spacing: 3px; margin-bottom: 23px; }
.visual-copy h1 { margin: 0; font-size: clamp(58px, 6.6vw, 104px); line-height: .98; letter-spacing: -6px; font-weight: 900; }
.visual-copy h1 em { color: #ff6542; font-style: normal; }
.visual-copy > p { max-width: 500px; margin: 30px 0; line-height: 1.9; color: rgba(255,255,255,.63); font-size: 14px; }
.visual-stats { display: flex; gap: 42px; margin-top: 40px; }
.visual-stats div { display: grid; gap: 5px; }
.visual-stats strong { font-size: 25px; }
.visual-stats span { color: rgba(255,255,255,.42); font-size: 11px; }
.visual-poster { position: absolute; right: 5%; bottom: 8%; width: 170px; z-index: 2; transform: rotate(5deg); }
.visual-poster img { border-radius: 8px; box-shadow: 0 30px 60px rgba(0,0,0,.55); }
.poster-caption { padding: 11px 2px; display: grid; gap: 3px; }
.poster-caption span { color: #ffb864; font-size: 9px; }
.poster-caption strong { font-size: 14px; }
.poster-caption small { color: rgba(255,255,255,.4); font-size: 7px; letter-spacing: 1.4px; }
.visual-bottom { position: relative; z-index: 2; display: flex; justify-content: space-between; color: rgba(255,255,255,.32); font-size: 9px; letter-spacing: 2px; }
.login-panel { min-height: 100vh; position: relative; display: flex; align-items: center; justify-content: center; padding: 50px 55px; background: #fbfaf7; }
.login-box { width: min(100%, 420px); }
.kicker { color: var(--orange); font-size: 10px; font-weight: 800; letter-spacing: 2.8px; }
.login-heading h2 { margin: 12px 0 7px; font-size: 34px; letter-spacing: -1.4px; }
.login-heading p { margin: 0 0 34px; color: var(--muted); font-size: 13px; }
:deep(.el-input__wrapper) { min-height: 52px; border-radius: 12px; background: white; box-shadow: 0 0 0 1px var(--line) inset; }
:deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 1.5px var(--orange) inset; }
.form-extra { display: flex; justify-content: space-between; margin: -3px 0 20px; color: var(--muted); font-size: 11px; }
.login-submit { width: 100%; height: 51px; border-radius: 12px; font-size: 15px; box-shadow: 0 12px 26px rgba(255,101,66,.24); }
.register-line { text-align: center; margin: 20px 0 25px; color: var(--muted); font-size: 12px; }
.register-line a { color: var(--orange); font-weight: 700; }
.demo-divider { position: relative; text-align: center; color: #aaa49a; font-size: 10px; margin-bottom: 15px; }
.demo-divider::before { content:''; position:absolute; left:0; right:0; top:50%; border-top:1px solid var(--line); }
.demo-divider span { background: #fbfaf7; padding: 0 12px; position: relative; }
.demo-accounts { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
.demo-account { border: 1px solid var(--line); background: #fff; border-radius: 12px; padding: 10px 8px; text-align: left; cursor: pointer; display: grid; grid-template-columns: 30px 1fr; gap: 7px; align-items: center; transition: .2s; }
.demo-account:hover { transform: translateY(-2px); border-color: #ffb6a5; box-shadow: var(--shadow-sm); }
.demo-avatar { width: 30px; height: 30px; border-radius: 9px; color: white; display: grid; place-items: center; font-size: 11px; font-weight: 800; }
.demo-account strong { display: block; font-size: 11px; }
.demo-account small { display: block; margin-top: 3px; color: #a39b8f; font-size: 7px; white-space: nowrap; }
.demo-account > .el-icon { display: none; }
.cinema-apply-link { margin-top: 18px; display: flex; justify-content: center; gap: 6px; color: var(--muted); font-size: 11px; }
.login-footer { position: absolute; bottom: 18px; color: #b2aba0; font-size: 9px; }
@keyframes slowZoom { to { transform: scale(1.1); } }
@media (max-width: 980px) {
  .login-page { grid-template-columns: 1fr; }
  .login-visual { display: none; }
  .login-panel { padding: 36px 22px 65px; }
}
@media (max-width: 460px) { .demo-accounts { grid-template-columns: 1fr; } .demo-account { grid-template-columns: 34px 1fr 16px; } .demo-account > .el-icon { display: block; } }
</style>
