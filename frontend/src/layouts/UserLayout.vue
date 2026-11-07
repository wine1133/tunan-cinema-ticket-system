<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BrandLogo from '@/components/BrandLogo.vue'
import { auth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const search = ref('')
const routeRoleHome = () => auth.isAdmin.value ? '/admin/dashboard' : auth.isCinema.value ? '/cinema-admin/dashboard' : ''

function doSearch() {
  if (!search.value.trim()) return
  router.push({ path: '/movies', query: { keyword: search.value.trim() } })
  search.value = ''
}
function handleCommand(command) {
  if (command === 'logout') {
    auth.logout()
    router.push('/login')
    ElMessage.success('已退出登录')
    return
  }
  router.push(command)
}
function unauthorized() {
  auth.logout()
  if (route.meta.auth && route.path !== '/login') router.push({ path: '/login', query: { redirect: route.fullPath } })
}
onMounted(() => {
  window.addEventListener('tunan:unauthorized', unauthorized)
})
</script>

<template>
  <div class="app-shell">
    <header class="site-header">
      <div class="page-wide header-inner">
        <BrandLogo />
        <nav class="site-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/movies">电影</router-link>
          <router-link to="/cinemas">影院</router-link>
          <router-link to="/rankings">排行榜</router-link>
          <router-link to="/orders">购票记录</router-link>
        </nav>
        <div class="header-actions">
          <el-input v-model="search" class="header-search" placeholder="搜索电影、导演" :prefix-icon="Search" clearable @keyup.enter="doSearch" />
          <template v-if="auth.state.user">
            <el-dropdown trigger="click" @command="handleCommand">
              <div class="user-chip">
                <span class="avatar">{{ auth.state.user.name?.slice(0, 1) }}</span>
                <span class="hide-mobile">{{ auth.state.user.name }}</span>
                <el-icon class="hide-mobile"><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="/profile" :icon="User">个人中心</el-dropdown-item>
                  <el-dropdown-item command="/favorites" :icon="Star">我的收藏</el-dropdown-item>
                  <el-dropdown-item v-if="routeRoleHome()" :command="routeRoleHome()" :icon="Setting">管理后台</el-dropdown-item>
                  <el-dropdown-item divided command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <router-link v-else to="/login">
            <el-button type="primary" round>登录 / 注册</el-button>
          </router-link>
        </div>
      </div>
    </header>

    <main><router-view /></main>

    <footer class="site-footer">
      <div class="page">
        <div class="footer-grid">
          <div>
            <BrandLogo dark />
            <p style="margin:18px 0 0;max-width:330px;line-height:1.8;font-size:13px">让每一次观影，从选座开始就值得期待。兔南电影连接好电影与热爱电影的人。</p>
          </div>
          <div>
            <div class="footer-title">快速发现</div>
            <div class="footer-links">
              <router-link to="/movies">正在热映</router-link>
              <router-link to="/movies?status=SOON">即将上映</router-link>
              <router-link to="/rankings">电影排行榜</router-link>
            </div>
          </div>
          <div>
            <div class="footer-title">观影服务</div>
            <div class="footer-links">
              <router-link to="/cinemas">影院信息</router-link>
              <router-link to="/orders">购票记录</router-link>
              <router-link to="/notices">公告中心</router-link>
            </div>
          </div>
          <div>
            <div class="footer-title">合作与管理</div>
            <div class="footer-links">
              <router-link to="/cinema-apply">影院入驻</router-link>
              <router-link to="/login">管理端登录</router-link>
              <a href="mailto:hello@tunan.cn">联系我们</a>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <span>© 2026 兔南电影购票系统 · TUNAN CINEMA</span>
          <span>本项目为全栈课程设计演示系统</span>
        </div>
      </div>
    </footer>

    <nav class="mobile-nav">
      <router-link to="/"><el-icon><HomeFilled /></el-icon><span>首页</span></router-link>
      <router-link to="/movies"><el-icon><Film /></el-icon><span>电影</span></router-link>
      <router-link to="/cinemas"><el-icon><OfficeBuilding /></el-icon><span>影院</span></router-link>
      <router-link to="/rankings"><el-icon><Trophy /></el-icon><span>榜单</span></router-link>
      <router-link to="/orders"><el-icon><Tickets /></el-icon><span>订单</span></router-link>
    </nav>
  </div>
</template>

<script>
import { Search, ArrowDown, User, Star, Setting, SwitchButton, HomeFilled, Film, OfficeBuilding, Trophy, Tickets } from '@element-plus/icons-vue'
export default { components: { Search, ArrowDown, User, Star, Setting, SwitchButton, HomeFilled, Film, OfficeBuilding, Trophy, Tickets } }
</script>
