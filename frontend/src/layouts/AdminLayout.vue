<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import BrandLogo from '@/components/BrandLogo.vue'
import { auth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const isCinema = computed(() => auth.state.user?.role === 'CINEMA')
const menu = computed(() => isCinema.value ? [
  { path: '/cinema-admin/dashboard', title: '影院首页', icon: 'DataBoard' },
  { path: '/cinema-admin/showings', title: '放映场次', icon: 'Calendar' },
  { path: '/cinema-admin/rooms', title: '影厅管理', icon: 'OfficeBuilding' },
  { path: '/cinema-admin/orders', title: '购票订单', icon: 'Tickets' },
  { path: '/cinema-admin/info', title: '影院资料', icon: 'Shop' }
] : [
  { path: '/admin/dashboard', title: '数据总览', icon: 'DataBoard' },
  { path: '/admin/movies', title: '电影管理', icon: 'Film' },
  { path: '/admin/approvals', title: '排片审批', icon: 'Calendar' },
  { path: '/admin/cinemas', title: '影院认证', icon: 'OfficeBuilding' },
  { path: '/admin/rooms', title: '全站影厅', icon: 'Monitor' },
  { path: '/admin/orders', title: '全站订单', icon: 'Tickets' },
  { path: '/admin/genres', title: '电影类型', icon: 'CollectionTag' },
  { path: '/admin/users', title: '用户与权限', icon: 'User' },
  { path: '/admin/notices', title: '公告管理', icon: 'Bell' }
])
const title = computed(() => route.meta.title || '管理后台')
function logout() {
  auth.logout(); router.push('/login'); ElMessage.success('已退出登录')
}
</script>

<template>
  <div class="admin-shell" :class="{ collapsed }">
    <aside class="admin-aside">
      <div class="admin-brand"><BrandLogo dark :compact="collapsed" /></div>
      <div v-if="!collapsed" class="portal-label">{{ isCinema ? '影院管理中心' : '平台管理控制台' }}</div>
      <nav class="admin-nav">
        <router-link v-for="item in menu" :key="item.path" :to="item.path" :title="item.title">
          <el-icon><component :is="item.icon" /></el-icon>
          <span v-if="!collapsed">{{ item.title }}</span>
        </router-link>
      </nav>
      <div class="aside-bottom">
        <router-link to="/" title="返回前台"><el-icon><Monitor /></el-icon><span v-if="!collapsed">返回购票前台</span></router-link>
      </div>
    </aside>
    <section class="admin-main">
      <header class="admin-header">
        <div class="admin-title">
          <el-button text circle @click="collapsed = !collapsed"><el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon></el-button>
          <div><h1>{{ title }}</h1><span>{{ isCinema ? 'TUNAN CINEMA · 影院工作台' : 'TUNAN CINEMA · 平台运营中心' }}</span></div>
        </div>
        <el-dropdown trigger="click">
          <div class="admin-user">
            <span class="avatar">{{ auth.state.user?.name?.slice(0, 1) }}</span>
            <div><strong>{{ auth.state.user?.name }}</strong><small>{{ isCinema ? '影院管理员' : '平台管理员' }}</small></div>
            <el-icon><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/profile')">个人资料</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <main class="admin-content"><router-view /></main>
    </section>
  </div>
</template>

<style scoped>
.admin-shell { --aside: 232px; min-height: 100vh; background: #f5f3ef; }
.admin-aside { position: fixed; z-index: 50; inset: 0 auto 0 0; width: var(--aside); padding: 22px 14px; background: #0c1421; color: #fff; display: flex; flex-direction: column; transition: width .25s ease; }
.admin-shell.collapsed { --aside: 78px; }
.admin-brand { padding: 0 10px 20px; }
.portal-label { margin: 8px 12px 18px; color: rgba(255,255,255,.36); font-size: 10px; letter-spacing: 1.4px; }
.admin-nav { display: grid; gap: 5px; }
.admin-nav a, .aside-bottom a { display: flex; align-items: center; gap: 13px; height: 44px; padding: 0 14px; border-radius: 10px; color: rgba(255,255,255,.62); font-size: 13px; white-space: nowrap; }
.admin-nav a:hover, .aside-bottom a:hover { color: white; background: rgba(255,255,255,.07); }
.admin-nav a.router-link-active { color: white; background: linear-gradient(100deg, #ff6542, #f18a43); box-shadow: 0 8px 20px rgba(255,101,66,.22); }
.admin-nav .el-icon, .aside-bottom .el-icon { font-size: 18px; }
.aside-bottom { margin-top: auto; padding-top: 18px; border-top: 1px solid rgba(255,255,255,.08); }
.admin-main { margin-left: var(--aside); min-height: 100vh; transition: margin .25s ease; }
.admin-header { height: 78px; padding: 0 28px; display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,.9); border-bottom: 1px solid var(--line); backdrop-filter: blur(15px); position: sticky; top: 0; z-index: 30; }
.admin-title { display: flex; align-items: center; gap: 12px; }
.admin-title h1 { margin: 0; font-size: 20px; }
.admin-title span { display: block; margin-top: 4px; color: #999; font-size: 9px; letter-spacing: 1.6px; }
.admin-user { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.admin-user strong { display: block; font-size: 13px; }
.admin-user small { display: block; color: var(--muted); font-size: 10px; margin-top: 3px; }
.admin-content { padding: 24px 28px 40px; }
@media(max-width: 800px) {
  .admin-shell { --aside: 78px; }
  .admin-aside { width: 78px; }
  .admin-aside .brand-copy, .portal-label, .admin-nav span, .aside-bottom span { display:none; }
  .admin-brand { padding-left: 8px; }
  .admin-header { padding: 0 15px; }
  .admin-content { padding: 16px; }
}
</style>
