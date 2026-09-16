import { localDateString } from '../utils/date'
import { getCurrentUser } from '../utils/authStorage'

type DemoResponse = { data: any }

const demoUser = {
  username: 'demo',
  nickname: '答辩演示用户',
  occupation: 'student',
  ageRange: '18-22',
  gender: '未透露',
  residentPersona: 'INFJ'
}

const defaultDiary = {
  id: 1,
  diaryDate: localDateString(),
  date: localDateString(),
  emotionEmoji: '😌😊',
  emotionLabel: '平静、开心',
  mood: '😌😊',
  moodLabel: '平静、开心',
  emotions: ['😌', '😊'],
  emotionLabels: ['平静', '开心'],
  content: '今天完成了一些学习任务，也给自己留出了休息时间。',
  keyword: '学习,自我照顾',
  locationName: '杭州市'
}

const knowledge = [
  {
    title: '考试焦虑调节',
    content: '考试焦虑通常来自对结果的担心、复习计划不清晰以及对失败的放大想象。可以把任务拆成较小的复习单元，用25分钟专注学习加5分钟休息的方式降低启动难度。',
    tags: '焦虑,考试,学习压力,学生',
    score: 0.96,
    source: '演示知识库'
  },
  {
    title: '睡眠与情绪关系',
    content: '睡眠不足会放大焦虑、烦躁和无力感。改善睡眠可以从固定入睡时间、睡前减少屏幕刺激开始。',
    tags: '睡眠,焦虑,疲惫,作息',
    score: 0.72,
    source: '演示知识库'
  }
]

const demoStorageVersion = 'moodboard_demo_storage_v2'

function currentUserKey() {
  const raw = getCurrentUser() || 'guest'
  return raw.replace(/[^a-zA-Z0-9_-]/g, '_')
}

function isShowcaseUser() {
  const user = currentUserKey()
  return user === 'demo' || user.startsWith('guest_')
}

function diaryKey() {
  return `moodboard_demo_${currentUserKey()}_diaries`
}

function memoryKey() {
  return `moodboard_demo_${currentUserKey()}_memory`
}

function profileKey() {
  return `moodboard_demo_${currentUserKey()}_profile`
}
function bottleKey() { return `moodboard_demo_${currentUserKey()}_bottles` }

function migrateLegacyDemoStorage() {
  if (localStorage.getItem(demoStorageVersion) === '2') {
    return
  }

  // These keys were shared by every Demo user and contained only the old
  // built-in seed. Remove them without touching formal-mode storage keys.
  localStorage.removeItem('moodboard_demo_diaries')
  localStorage.removeItem('moodboard_demo_memory')
  localStorage.removeItem('moodboard_demo_profile')
  localStorage.setItem(demoStorageVersion, '2')
}

if (import.meta.env.VITE_DEMO_MODE === 'true') {
  migrateLegacyDemoStorage()
}

function response(data: any): DemoResponse {
  return { data }
}

function readDiaries() {
  const key = diaryKey()
  const stored = localStorage.getItem(key)
  if (!stored) {
    const initial = isShowcaseUser() ? [defaultDiary] : []
    localStorage.setItem(key, JSON.stringify(initial))
    return initial
  }
  try {
    return JSON.parse(stored)
  } catch {
    return []
  }
}

function writeDiaries(items: any[]) {
  localStorage.setItem(diaryKey(), JSON.stringify(items))
}

function dateOf(item: any) {
  return item.diaryDate || item.date
}

function chatReply(content: string, persona = '日常树洞') {
  if (persona === 'DAILY') {
    return `我听到了你说的“${content}”。先不用急着解决全部问题，可以从眼前最小的一步开始。`
  }
  return `作为 ${persona}，我会先陪你拆开这个问题：${content}。你可以先说说最困扰你的部分。`
}

function memory() {
  const stored = localStorage.getItem(memoryKey())
  if (stored) {
    return JSON.parse(stored)
  }

  if (!isShowcaseUser()) {
    return {
      recentEmotionSummary: '',
      recentPersonaCode: '',
      recentPersonaName: '',
      chatSummary: '',
      lastQuestion: '',
      lastAnswer: ''
    }
  }

  return {
    recentEmotionSummary: '近期情绪总体平稳，偶尔有学习压力。',
    recentPersonaCode: 'INFJ',
    recentPersonaName: '温柔洞察者',
    chatSummary: '这是 Netlify 答辩演示模式的示例记忆。',
    lastQuestion: '我最近有些焦虑怎么办？',
    lastAnswer: '可以先把困扰拆成一个很小的行动。'
  }
}

export const demoApi: any = {
  register: async (data: any) => response({ username: data.username }),
  login: async (data: any) => response({ token: `demo-token-${data.username || 'demo'}`, username: data.username || 'demo' }),
  getProfile: async () => response(memoryProfile()),
  saveProfile: async (data: any) => {
    localStorage.setItem(profileKey(), JSON.stringify({ ...demoUser, ...data }))
    return response(memoryProfile())
  },
  personas: async () => response([]),
  recommendPersonas: async () => response([]),
  customPersonas: async () => response([]),
  createCustomPersona: async () => response({ id: Date.now() }),
  updateCustomPersona: async () => response({}),
  deleteCustomPersona: async () => response({}),
  saveDiary: async (data: any) => {
    const item = { ...data, id: Date.now(), diaryDate: data.diaryDate || data.date }
    const items = readDiaries().filter((entry: any) => dateOf(entry) !== dateOf(item))
    writeDiaries([...items, item])
    return response(item)
  },
  updateDiary: async (id: number, data: any) => {
    const items = readDiaries().map((item: any) => item.id === id ? { ...item, ...data, id } : item)
    writeDiaries(items)
    return response(items.find((item: any) => item.id === id) || data)
  },
  deleteDiary: async (id: number) => {
    writeDiaries(readDiaries().filter((item: any) => item.id !== id))
    return response({ deleted: true, id })
  },
  getDiaryByDate: async (date: string) => response(readDiaries().find((item: any) => dateOf(item) === date) || null),
  weekDiaries: async (start: string, end: string) => response(readDiaries().filter((item: any) => dateOf(item) >= start && dateOf(item) <= end)),
  monthDiaries: async (year: number, month: number) => response(readDiaries().filter((item: any) => {
    const date = dateOf(item) || ''
    return date.startsWith(`${year}-${String(month).padStart(2, '0')}`)
  })),
  yearDiaries: async (year: number) => response(readDiaries().filter((item: any) => (dateOf(item) || '').startsWith(String(year)))),
  searchDiaries: async () => response(readDiaries()),
  saveDiaryFromBattle: async (data: any) => demoApi.saveDiary(data),
  createChatSession: async () => response({ sessionId: Date.now() }),
  sendChat: async (data: any) => response({ sessionId: data.sessionId || Date.now(), reply: chatReply(data.content, data.persona) }),
  clearChatMessages: async () => response({ cleared: true }),
  throwDriftBottle: async (data: any) => {
    const item = { id: Date.now(), content: data.content, moodEmoji: data.moodEmoji, moodLabel: data.moodLabel, aiEcho: '我在听。你可以慢慢说，不需要组织得很完整。', createdAt: new Date().toISOString() }
    const items = JSON.parse(localStorage.getItem(bottleKey()) || '[]'); items.push(item); localStorage.setItem(bottleKey(), JSON.stringify(items))
    return response(item)
  },
  drawBlindBox: async () => response({ persona: 'INFJ', topic: '今天你希望被怎样安慰？' }),
  startBlindBox: async (data: any) => response({ blindBoxId: Date.now(), roundIndex: 1, reply: `我是 ${data.persona}，我们可以从“${data.topic}”开始聊。` }),
  replyBlindBox: async (data: any) => response({ roundIndex: (data.roundIndex || 1) + 1, finished: false, reply: '我在听。你可以继续说说刚才那份感受。' }),
  searchKnowledge: async () => response({ results: knowledge }),
  askKnowledge: async () => response({ answer: '你可以先把最困扰的部分写成一句话，再只处理其中最容易开始的一步。', contexts: knowledge }),
  getMemory: async () => response(memory()),
  updateMemory: async (data: any) => { localStorage.setItem(memoryKey(), JSON.stringify(data)); return response(data) },
  clearMemory: async () => { localStorage.removeItem(memoryKey()); return response({}) },
  sendMemoryChat: async (data: any) => response({ reply: chatReply(data.content || data.message || '', 'DAILY') }),
  reverseLocation: async () => response({ province: '浙江省', city: '杭州市', district: '演示区', locationName: '杭州市 演示区' }),
  callLocationTool: async () => response({ province: '浙江省', city: '杭州市', district: '演示区', locationName: '杭州市 演示区' }),
  runAgentRoundtable: async (data: any) => response({
    agents: (data.agents || []).map((agent: any, index: number) => ({
      code: agent.code,
      name: agent.name,
      reply: `第 ${index + 1} 位 Agent 建议：先把“${data.topic || '当前问题'}”拆成一个今天能完成的小步骤。`
    })),
    summary: '综合建议：先接纳当前感受，再选择一个最小、明确、可执行的行动。'
  }),
  battleSessions: async () => response([]),
  createBattleSession: async () => response({ id: Date.now() }),
  getBattleSession: async () => response({}),
  addBattleMessage: async () => response({}),
  finishBattleSession: async () => response({}),
  deleteBattleSession: async () => response({})
}

function memoryProfile() {
  const stored = localStorage.getItem(profileKey())
  return stored ? JSON.parse(stored) : (isShowcaseUser() ? demoUser : {
    nickname: '',
    occupation: '',
    ageRange: '',
    gender: '',
    residentPersona: ''
  })
}
