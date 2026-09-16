import axios from 'axios'
import { getToken } from '../utils/authStorage'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 60000
})
http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const msg = err?.response?.data?.message || err?.response?.data?.error || err.message || '请求失败'
    alert(msg)
    return Promise.reject(err)
  }
)
export default http
