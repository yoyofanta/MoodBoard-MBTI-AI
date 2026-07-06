import axios from 'axios'


/**
 * 说明：
 * 1. 这里 baseURL 写 '/api'，是为了配合 Vite 代理。
 * 2. 前端请求 /api/auth/login，会被代理到后端 http://localhost:8888/api/auth/login。
 * 3. 如果你没有配置 Vite 代理，也可以改成：
 *    baseURL: 'http://localhost:8888/api'
 */
const http = axios.create({
  baseURL: '/api',
  timeout: 120000
})

/**
 * 请求拦截器：自动携带 token
 */
http.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')

    if (token) {
      config.headers.Authorization = token.startsWith('Bearer ')
        ? token
        : `Bearer ${token}`
    }

    return config
  },
  error => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器：
 * 后端一般返回格式：
 * {
 *   code: 200,
 *   message: '成功',
 *   data: xxx
 * }
 *
 * 这里直接 return response.data，
 * 所以前端使用时可以写：
 * const res = await api.login(...)
 * res.data 就是后端真正的数据。
 */
http.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const msg =
      error?.response?.data?.message ||
      error?.response?.data?.msg ||
      error?.message ||
      '请求失败'

    console.error('接口请求失败：', msg, error)

    return Promise.reject(error)
  }
)

export const api = {
  /**
   * =========================
   * 登录注册
   * =========================
   */

  register: (data: any) =>
    http.post('/auth/register', data),

  login: (data: any) =>
    http.post('/auth/login', data),

  /**
   * =========================
   * 用户资料 / 首次引导
   * =========================
   */

  getProfile: () =>
    http.get('/user/profile'),

  saveProfile: (data: any) =>
    http.post('/user/profile', data),

  /**
   * =========================
   * 人格相关
   * =========================
   */

  personas: () =>
    http.get('/personas'),

  recommendPersonas: () =>
    http.get('/persona-recommendations'),

  /**
   * =========================
   * 自定义人格 / 人格面具
   * =========================
   */

  customPersonas: () =>
    http.get('/custom-personas'),

  createCustomPersona: (data: any) =>
    http.post('/custom-personas', data),

  updateCustomPersona: (id: number, data: any) =>
    http.put(`/custom-personas/${id}`, data),

  deleteCustomPersona: (id: number) =>
    http.delete(`/custom-personas/${id}`),

  /**
   * =========================
   * 情绪日记
   * =========================
   */

  saveDiary: (data: any) =>
    http.post('/diaries', data),

  updateDiary: (id: number, data: any) =>
    http.put(`/diaries/${id}`, data),

  deleteDiary: (id: number) =>
    http.delete(`/diaries/${id}`),

  getDiaryByDate: (date: string) =>
    http.get('/diaries/date', {
      params: {
        date
      }
    }),

  weekDiaries: (startDate: string, endDate: string) =>
    http.get('/diaries/week', {
      params: {
        startDate,
        endDate
      }
    }),

  monthDiaries: (year: number, month: number) =>
    http.get('/diaries/month', {
      params: {
        year,
        month
      }
    }),

  yearDiaries: (year: number) =>
    http.get('/diaries/year', {
      params: {
        year
      }
    }),

  searchDiaries: (params: any) =>
    http.get('/diaries/search', {
      params
    }),

  saveDiaryFromBattle: (data: any) =>
    http.post('/diaries/from-battle', data),

  /**
   * =========================
   * 普通聊天 / 日常树洞 / 单人格聊天
   * =========================
   */

  createChatSession: (data: any) =>
    http.post('/ai/chat/sessions', data),

  sendChat: (data: any) =>
    http.post('/ai/chat/send', data),

  clearChatMessages: (sessionId: number) =>
    http.delete(`/ai/chat/sessions/${sessionId}/messages`),

  /**
   * =========================
   * 情绪漂流瓶
   * =========================
   */

  throwDriftBottle: (data: any) =>
    http.post('/drift-bottles/throw', data),

  /**
   * =========================
   * 心灵盲盒
   * =========================
   */

  drawBlindBox: () =>
    http.post('/blind-box/draw'),

  startBlindBox: (data: any) =>
    http.post('/blind-box/start', data),

  replyBlindBox: (data: any) =>
    http.post('/blind-box/reply', data),

  /**
   * =========================
   * 多人格对战：会话保存版
   * =========================
   */

  battleSessions: () =>
    http.get('/persona-battle-sessions'),

  createBattleSession: (data: any) =>
    http.post('/persona-battle-sessions', data),

  getBattleSession: (id: number) =>
    http.get(`/persona-battle-sessions/${id}`),

  addBattleMessage: (id: number, data: any) =>
    http.post(`/persona-battle-sessions/${id}/messages`, data),

  finishBattleSession: (id: number, data: any) =>
    http.post(`/persona-battle-sessions/${id}/finish`, data),

  deleteBattleSession: (id: number) =>
    http.delete(`/persona-battle-sessions/${id}`),

  /**
   * =========================
   * 旧版多人格对战接口
   * 如果你已经不用 PersonaBattlePanel.vue，可以暂时不用这个。
   * 留着是为了兼容旧代码，避免某些页面还在调用 startPersonaBattle。
   * =========================
   */

  startPersonaBattle: (data: any) =>
    http.post('/persona-battle/start', data),

  /**
   * =========================
   * 定位 / 逆地理编码
   * =========================
   */

  reverseLocation: (lat: number, lng: number) =>
    http.get('/location/reverse', {
      params: {
        lat,
        lng
      }
    }),

  /**
   * =========================
   * 广场 / 匿名发布
   * 如果你的后端没有这些接口，前端不用调用就没事。
   * =========================
   */

  plazaList: () =>
    http.get('/plaza'),

  publishToPlaza: (data: any) =>
    http.post('/plaza/publish', data),

  searchKnowledge: (q: string, topK = 3) =>
  http.get('/knowledge/search', {
    params: {
      q,
      topK
    }
  }),

  askKnowledge: (q: string, topK = 3) =>
  http.get('/knowledge/ask', {
    params: {
      q,
      topK
    }
  }),

  getMemory: () =>
  http.get('/memory/current'),

updateMemory: (data: any) =>
  http.post('/memory/update', data),

clearMemory: () =>
  http.post('/memory/clear'),

sendMemoryChat: (data: any) =>
  http.post('/ai/memory-chat/send', data),

callLocationTool: (data: { lat: number; lng: number }) =>
  http.post('/ai/tools/location/reverse', data),

runAgentRoundtable: (data: any) =>
  http.post('/ai/agent/roundtable', data),
}

export default api