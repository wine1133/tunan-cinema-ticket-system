import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 15000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('tunan_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message || error.message || '网络连接失败'
    if (error.response?.status === 401) {
      localStorage.removeItem('tunan_token')
      localStorage.removeItem('tunan_user')
      window.dispatchEvent(new CustomEvent('tunan:unauthorized'))
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default http
