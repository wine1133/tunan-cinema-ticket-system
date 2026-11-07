import { computed, reactive } from 'vue'
import http from '@/api/http'

const saved = (() => {
  try { return JSON.parse(localStorage.getItem('tunan_user') || 'null') } catch { return null }
})()

const state = reactive({
  token: localStorage.getItem('tunan_token') || '',
  user: saved
})

function persist() {
  if (state.token) localStorage.setItem('tunan_token', state.token)
  else localStorage.removeItem('tunan_token')
  if (state.user) localStorage.setItem('tunan_user', JSON.stringify(state.user))
  else localStorage.removeItem('tunan_user')
}

export const auth = {
  state,
  isLoggedIn: computed(() => !!state.token),
  isUser: computed(() => state.user?.role === 'USER'),
  isAdmin: computed(() => state.user?.role === 'ADMIN'),
  isCinema: computed(() => state.user?.role === 'CINEMA'),
  async login(payload) {
    const { data } = await http.post('/auth/login', payload)
    state.token = data.token
    state.user = data.user
    persist()
    return data.user
  },
  async refresh() {
    if (!state.token) return null
    const { data } = await http.get('/auth/me')
    state.user = data
    persist()
    return data
  },
  setUser(user) { state.user = user; persist() },
  logout() { state.token = ''; state.user = null; persist() }
}
