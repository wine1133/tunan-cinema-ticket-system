import { createRouter, createWebHistory } from 'vue-router'
import { auth } from '@/stores/auth'

const routes = [
  { path: '/login', component: () => import('@/views/LoginView.vue'), meta: { guest: true } },
  { path: '/register', component: () => import('@/views/RegisterView.vue'), meta: { guest: true } },
  { path: '/cinema-apply', component: () => import('@/views/CinemaApplyView.vue'), meta: { guest: true } },
  {
    path: '/', component: () => import('@/layouts/UserLayout.vue'),
    children: [
      { path: '', component: () => import('@/views/HomeView.vue'), meta: { title: '首页' } },
      { path: 'movies', component: () => import('@/views/MoviesView.vue'), meta: { title: '电影' } },
      { path: 'movies/:id', component: () => import('@/views/MovieDetailView.vue'), meta: { title: '电影详情' } },
      { path: 'cinemas', component: () => import('@/views/CinemasView.vue'), meta: { title: '影院' } },
      { path: 'cinemas/:id', component: () => import('@/views/CinemaDetailView.vue'), meta: { title: '影院详情' } },
      { path: 'rankings', component: () => import('@/views/RankingsView.vue'), meta: { title: '排行榜' } },
      { path: 'notices', component: () => import('@/views/NoticesView.vue'), meta: { title: '公告中心' } },
      { path: 'booking/:showingId', component: () => import('@/views/SeatSelectionView.vue'), meta: { title: '在线选座', auth: true } },
      { path: 'orders', component: () => import('@/views/OrdersView.vue'), meta: { title: '购票记录', auth: true } },
      { path: 'favorites', component: () => import('@/views/FavoritesView.vue'), meta: { title: '我的收藏', auth: true } },
      { path: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { title: '个人中心', auth: true } }
    ]
  },
  {
    path: '/admin', component: () => import('@/layouts/AdminLayout.vue'), meta: { auth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', component: () => import('@/views/admin/AdminDashboard.vue'), meta: { title: '数据总览' } },
      { path: 'movies', component: () => import('@/views/admin/AdminMovies.vue'), meta: { title: '电影管理' } },
      { path: 'approvals', component: () => import('@/views/admin/AdminShowings.vue'), meta: { title: '排片审批' } },
      { path: 'cinemas', component: () => import('@/views/admin/AdminCinemas.vue'), meta: { title: '影院认证' } },
      { path: 'rooms', component: () => import('@/views/admin/AdminRooms.vue'), meta: { title: '全站影厅' } },
      { path: 'orders', component: () => import('@/views/admin/AdminOrders.vue'), meta: { title: '全站订单' } },
      { path: 'genres', component: () => import('@/views/admin/AdminGenres.vue'), meta: { title: '电影类型' } },
      { path: 'users', component: () => import('@/views/admin/AdminUsers.vue'), meta: { title: '用户与权限' } },
      { path: 'notices', component: () => import('@/views/admin/AdminNotices.vue'), meta: { title: '公告管理' } }
    ]
  },
  {
    path: '/cinema-admin', component: () => import('@/layouts/AdminLayout.vue'), meta: { auth: true, role: 'CINEMA' },
    children: [
      { path: '', redirect: '/cinema-admin/dashboard' },
      { path: 'dashboard', component: () => import('@/views/cinema/CinemaDashboard.vue'), meta: { title: '影院首页' } },
      { path: 'showings', component: () => import('@/views/cinema/CinemaShowings.vue'), meta: { title: '放映场次' } },
      { path: 'rooms', component: () => import('@/views/cinema/CinemaRooms.vue'), meta: { title: '影厅管理' } },
      { path: 'orders', component: () => import('@/views/cinema/CinemaOrders.vue'), meta: { title: '购票订单' } },
      { path: 'info', component: () => import('@/views/cinema/CinemaInfo.vue'), meta: { title: '影院资料' } }
    ]
  },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFoundView.vue') }
]

const router = createRouter({ history: createWebHistory(), routes, scrollBehavior: () => ({ top: 0 }) })
router.beforeEach((to) => {
  const user = auth.state.user
  if (to.meta.auth && !user) return { path: '/login', query: { redirect: to.fullPath } }
  if (to.meta.role && user?.role !== to.meta.role) {
    if (user?.role === 'ADMIN') return '/admin/dashboard'
    if (user?.role === 'CINEMA') return '/cinema-admin/dashboard'
    return '/'
  }
  if (to.meta.guest && user) {
    if (user.role === 'ADMIN') return '/admin/dashboard'
    if (user.role === 'CINEMA') return '/cinema-admin/dashboard'
    return '/'
  }
  return true
})
export default router
