<template>
  <main class="diary-page">
    <div class="page-bg"></div>

    <section class="diary-shell">
      <!-- 顶部标题区 -->
      <header class="page-header">
        <div>
          <p class="eyebrow">Mood Diary</p>
          <h1>情绪日记</h1>
          <p class="subtitle">
            记录每天的情绪变化，把心情慢慢整理成自己的地图。
          </p>
        </div>

        <button class="write-today-btn" @click="openEditor(todayString)">
          写今天
        </button>
      </header>

      <!-- 视图切换 -->
      <section class="toolbar-card">
        <div class="view-tabs">
          <button
            class="tab-btn"
            :class="{ active: viewMode === 'week' }"
            @click="switchView('week')"
          >
            周
          </button>

          <button
            class="tab-btn"
            :class="{ active: viewMode === 'month' }"
            @click="switchView('month')"
          >
            月
          </button>

          <button
            class="tab-btn"
            :class="{ active: viewMode === 'year' }"
            @click="switchView('year')"
          >
            年
          </button>
        </div>

        <div class="date-switcher">
          <button class="circle-btn" @click="prevPeriod">
            ‹
          </button>

          <div class="period-title">
            {{ periodTitle }}
          </div>

          <button class="circle-btn" @click="nextPeriod">
            ›
          </button>
        </div>
      </section>

      <!-- 月/年柱状图 -->
      <section
        v-if="viewMode === 'month' || viewMode === 'year'"
        class="card chart-card"
      >
        <div class="section-head">
          <div>
            <p class="eyebrow">Mood Chart</p>
            <h2>
              {{ viewMode === 'month' ? '本月情绪占比柱状图' : '年度情绪占比柱状图' }}
            </h2>
          </div>

          <button class="refresh-btn" @click="loadDiaries">
            重新生成
          </button>
        </div>

        <div class="legend-row">
          <div
            v-for="item in emotionOptions"
            :key="item.label"
            class="legend-item"
          >
            <span
              class="legend-dot"
              :style="{ background: item.color }"
            ></span>
            <span>{{ item.emoji }} {{ item.label }}</span>
          </div>
        </div>

        <!-- 月视图：每天一根柱子 -->
        <div v-if="viewMode === 'month'" class="chart-scroll">
          <div class="stacked-chart month-chart">
            <div
              v-for="day in monthChartDays"
              :key="day.date"
              class="bar-unit"
              :title="day.tooltip"
            >
              <div class="bar-box">
                <template v-if="day.segments.length">
                  <div
                    v-for="segment in day.segments"
                    :key="segment.key"
                    class="bar-segment"
                    :style="{
                      height: segment.percent + '%',
                      background: segment.color
                    }"
                  ></div>
                </template>

                <div v-else class="bar-empty"></div>
              </div>

              <div
                class="bar-label"
                :class="{ today: day.date === todayString }"
              >
                {{ day.day }}
              </div>
            </div>
          </div>
        </div>

        <!-- 年视图：每个月一根柱子 -->
        <div v-if="viewMode === 'year'" class="stacked-chart year-chart">
          <div
            v-for="month in yearChartMonths"
            :key="month.month"
            class="bar-unit year-bar"
            :title="month.tooltip"
          >
            <div class="bar-box year-bar-box">
              <template v-if="month.segments.length">
                <div
                  v-for="segment in month.segments"
                  :key="segment.key"
                  class="bar-segment"
                  :style="{
                    height: segment.percent + '%',
                    background: segment.color
                  }"
                ></div>
              </template>

              <div v-else class="bar-empty"></div>
            </div>

            <div class="bar-label">
              {{ month.month }}月
            </div>
          </div>
        </div>

        <p class="chart-hint">
          月视图中每一天生成一根柱子；年视图中每个月生成一根柱子。每根柱子根据当天或当月不同心情的占比自动分层。
        </p>
      </section>

      <!-- 周视图 -->
      <section v-if="viewMode === 'week'" class="card week-card">
        <div class="week-grid">
          <button
            v-for="day in weekDays"
            :key="day.date"
            class="day-card"
            :class="{
              today: day.date === todayString,
              hasDiary: getDiary(day.date)
            }"
            @click="openEditor(day.date)"
          >
            <p class="weekday">{{ day.weekday }}</p>
            <p class="day-number">{{ day.day }}</p>

            <div class="emoji-display">
              {{ getDiaryEmojis(day.date) || '＋' }}
            </div>

            <p class="day-preview">
              {{ getDiaryLabel(day.date) || '记录一下' }}
            </p>
          </button>
        </div>
      </section>

      <!-- 月视图 -->
      <section v-if="viewMode === 'month'" class="card month-card">
        <div class="month-week-head">
          <span>一</span>
          <span>二</span>
          <span>三</span>
          <span>四</span>
          <span>五</span>
          <span>六</span>
          <span>日</span>
        </div>

        <div class="month-grid">
          <button
            v-for="day in monthDays"
            :key="day.key"
            class="month-day"
            :class="{
              muted: !day.inCurrentMonth,
              today: day.date === todayString,
              hasDiary: getDiary(day.date)
            }"
            @click="openEditor(day.date)"
          >
            <span class="month-day-num">{{ day.day }}</span>
            <span class="month-emoji">
              {{ getDiaryEmojis(day.date) || '' }}
            </span>
          </button>
        </div>
      </section>

      <!-- 年视图 -->
      <section v-if="viewMode === 'year'" class="year-grid">
        <button
          v-for="month in yearMonths"
          :key="month.month"
          class="year-month-card"
          @click="jumpToMonth(month.monthIndex)"
        >
          <div class="month-title">
            {{ month.month }}月
          </div>

          <div class="month-emotion-row">
            <span
              v-for="item in month.emojis"
              :key="item.key"
              class="small-emoji"
            >
              {{ item.emoji }}
            </span>

            <span v-if="month.emojis.length === 0" class="empty-month">
              暂无记录
            </span>
          </div>

          <p class="month-count">
            {{ month.count }} 条记录
          </p>
        </button>
      </section>

      <!-- 最近日记 -->
      <section class="card recent-card">
        <div class="section-head">
          <div>
            <p class="eyebrow">Recent</p>
            <h2>最近记录</h2>
          </div>

          <button class="refresh-btn" @click="loadDiaries">
            刷新
          </button>
        </div>

        <div v-if="recentDiaries.length === 0" class="empty-state">
          <div class="empty-icon">🌙</div>
          <h3>还没有日记</h3>
          <p>点击某一天，开始记录今天的心情。</p>
        </div>

        <div v-else class="recent-list">
          <button
            v-for="item in recentDiaries"
            :key="item.id || item.diaryDate || item.date"
            class="recent-item"
            @click="openEditor(getDiaryDate(item))"
          >
            <div class="recent-emoji">
              {{ getDiaryEmojisFromEntry(item) || '＋' }}
            </div>

            <div class="recent-content">
              <div class="recent-top">
                <h3>{{ getDiaryLabelFromEntry(item) || '未命名心情' }}</h3>
                <span>{{ getDiaryDate(item) }}</span>
              </div>

              <p>
                {{ item.content || '没有填写文字内容' }}
              </p>

              <div class="recent-tags">
                <span v-if="item.keyword || item.keywords">
                  # {{ item.keyword || item.keywords }}
                </span>

                <span v-if="item.locationName">
                  📍 {{ item.locationName }}
                </span>
              </div>
            </div>
          </button>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'

type ViewMode = 'week' | 'month' | 'year'

type EmotionOption = {
  emoji: string
  label: string
  color: string
}

type ChartSegment = {
  key: string
  emoji: string
  label: string
  color: string
  count: number
  percent: number
}

type DiaryEntry = {
  id?: number
  diaryDate?: string
  date?: string
  mood?: string
  moodLabel?: string
  emotionEmoji?: string
  emotionLabel?: string
  emotions?: string[]
  emotionLabels?: string[]
  content?: string
  keyword?: string
  keywords?: string
  locationName?: string
  [key: string]: any
}

const router = useRouter()
const route = useRoute()

const viewMode = ref<ViewMode>('week')
const currentDate = ref(new Date())
const diaries = ref<DiaryEntry[]>([])
const loading = ref(false)

const todayString = formatDate(new Date())

const emotionOptions: EmotionOption[] = [
  { emoji: '😊', label: '开心', color: '#F6C76A' },
  { emoji: '😢', label: '难过', color: '#82B7E8' },
  { emoji: '😡', label: '生气', color: '#E76F61' },
  { emoji: '😰', label: '焦虑', color: '#9E89D8' },
  { emoji: '😴', label: '疲惫', color: '#B8B2A6' },
  { emoji: '🥳', label: '兴奋', color: '#F29E7D' },
  { emoji: '😌', label: '平静', color: '#90C9A7' },
  { emoji: '😤', label: '烦躁', color: '#D9956A' },
  { emoji: '🤯', label: '崩溃', color: '#D86C8D' },
  { emoji: '😶‍🌫️', label: '迷茫', color: '#A7B6C8' },
  { emoji: '💪', label: '有力量', color: '#E5B84D' },
  { emoji: '❤️', label: '被爱', color: '#E88DB2' }
]

const diaryMap = computed(() => {
  const map = new Map<string, DiaryEntry>()

  diaries.value.forEach(item => {
    const date = getDiaryDate(item)

    if (date) {
      map.set(date, item)
    }
  })

  return map
})

const periodTitle = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth() + 1

  if (viewMode.value === 'week') {
    const days = getWeekDays(currentDate.value)
    return `${days[0].date} 至 ${days[6].date}`
  }

  if (viewMode.value === 'month') {
    return `${year}年 ${month}月`
  }

  return `${year}年`
})

const weekDays = computed(() => {
  return getWeekDays(currentDate.value)
})

const monthDays = computed(() => {
  return getMonthDays(currentDate.value)
})

const monthChartDays = computed(() => {
  return monthDays.value
    .filter(day => day.inCurrentMonth)
    .map(day => {
      const entry = getDiary(day.date)
      const emotions = entry ? getEntryEmotionList(entry) : []
      const segments = buildSegments(emotions)

      return {
        date: day.date,
        day: day.day,
        segments,
        tooltip: buildDayTooltip(day.date, segments)
      }
    })
})

const yearChartMonths = computed(() => {
  const year = currentDate.value.getFullYear()

  return Array.from({ length: 12 }).map((_, index) => {
    const month = index + 1
    const monthText = `${year}-${String(month).padStart(2, '0')}`

    const monthEntries = diaries.value.filter(item => {
      return getDiaryDate(item).startsWith(monthText)
    })

    const emotions = monthEntries.flatMap(item => getEntryEmotionList(item))
    const segments = buildSegments(emotions)

    return {
      month,
      segments,
      tooltip: buildMonthTooltip(month, segments, monthEntries.length)
    }
  })
})

const yearMonths = computed(() => {
  const year = currentDate.value.getFullYear()

  return Array.from({ length: 12 }).map((_, index) => {
    const month = index + 1
    const monthText = `${year}-${String(month).padStart(2, '0')}`

    const items = diaries.value.filter(item => {
      return getDiaryDate(item).startsWith(monthText)
    })

    const emojis = items
      .flatMap(item => getEntryEmotionList(item).map(emotion => emotion.emoji))
      .slice(0, 8)
      .map((emoji, emojiIndex) => ({
        key: `${month}-${emojiIndex}-${emoji}`,
        emoji
      }))

    return {
      month,
      monthIndex: index,
      count: items.length,
      emojis
    }
  })
})

const recentDiaries = computed(() => {
  return [...diaries.value]
    .filter(item => getDiaryDate(item))
    .sort((a, b) => {
      return getDiaryDate(b).localeCompare(getDiaryDate(a))
    })
    .slice(0, 8)
})

onMounted(() => {
  loadDiaries()
})

watch(
  () => route.fullPath,
  () => {
    if (route.path === '/app/diary') {
      loadDiaries()
    }
  }
)

function switchView(mode: ViewMode) {
  viewMode.value = mode
  loadDiaries()
}

function openEditor(date: string) {
  router.push({
    path: '/app/diary/edit',
    query: {
      date
    }
  })
}

function jumpToMonth(monthIndex: number) {
  const next = new Date(currentDate.value)
  next.setMonth(monthIndex)
  currentDate.value = next
  viewMode.value = 'month'
  loadDiaries()
}

function prevPeriod() {
  const next = new Date(currentDate.value)

  if (viewMode.value === 'week') {
    next.setDate(next.getDate() - 7)
  } else if (viewMode.value === 'month') {
    next.setMonth(next.getMonth() - 1)
  } else {
    next.setFullYear(next.getFullYear() - 1)
  }

  currentDate.value = next
  loadDiaries()
}

function nextPeriod() {
  const next = new Date(currentDate.value)

  if (viewMode.value === 'week') {
    next.setDate(next.getDate() + 7)
  } else if (viewMode.value === 'month') {
    next.setMonth(next.getMonth() + 1)
  } else {
    next.setFullYear(next.getFullYear() + 1)
  }

  currentDate.value = next
  loadDiaries()
}

/**
 * 关键修复：
 * 周/月/年共用同一个 diaries 数据源。
 * 优先读取全年数据；如果后端没有 yearDiaries，则按 12 个月请求后合并。
 * 这样周视图写入的数据，月视图和年视图都能同步展示。
 */
async function loadDiaries() {
  loading.value = true

  try {
    const apiAny = api as any
    const year = currentDate.value.getFullYear()

    let list: DiaryEntry[] = []

    if (apiAny.yearDiaries) {
      try {
        const res = await apiAny.yearDiaries(year)
        list = extractList(res)
      } catch (e) {
        console.warn('yearDiaries 获取失败，尝试按月加载：', e)
      }
    }

    if (list.length === 0 && apiAny.monthDiaries) {
      const monthResults: DiaryEntry[] = []

      for (let month = 1; month <= 12; month++) {
        try {
          const res = await apiAny.monthDiaries(year, month)
          const monthList = extractList(res)
          monthResults.push(...monthList)
        } catch (e) {
          console.warn(`${year}-${month} 月日记加载失败：`, e)
        }
      }

      list = monthResults
    }

    if (list.length === 0 && apiAny.weekDiaries) {
      const days = getWeekDays(currentDate.value)
      const res = await apiAny.weekDiaries(days[0].date, days[6].date)
      list = extractList(res)
    }

    diaries.value = dedupeDiaries(list)
  } catch (e) {
    console.error('加载日记失败：', e)
    diaries.value = []
  } finally {
    loading.value = false
  }
}

function extractList(res: any): DiaryEntry[] {
  const data = normalizeResponse(res)

  if (Array.isArray(data)) {
    return data
  }

  if (Array.isArray(data?.records)) {
    return data.records
  }

  if (Array.isArray(data?.list)) {
    return data.list
  }

  if (Array.isArray(data?.diaries)) {
    return data.diaries
  }

  if (data && typeof data === 'object' && (data.diaryDate || data.date)) {
    return [data]
  }

  return []
}

function dedupeDiaries(list: DiaryEntry[]) {
  const map = new Map<string, DiaryEntry>()

  list.forEach(item => {
    const date = getDiaryDate(item)

    if (!date) {
      return
    }

    map.set(date, item)
  })

  return Array.from(map.values())
}

function normalizeResponse(res: any) {
  if (!res) {
    return null
  }

  if (res.data !== undefined) {
    return res.data
  }

  return res
}

function getDiary(date: string) {
  return diaryMap.value.get(date)
}

function getDiaryDate(item: DiaryEntry) {
  return item.diaryDate || item.date || ''
}

function getDiaryEmojis(date: string) {
  const item = getDiary(date)

  if (!item) {
    return ''
  }

  return getDiaryEmojisFromEntry(item)
}

function getDiaryEmojisFromEntry(item: DiaryEntry) {
  return getEntryEmotionList(item)
    .map(emotion => emotion.emoji)
    .join('')
}

function getDiaryLabel(date: string) {
  const item = getDiary(date)

  if (!item) {
    return ''
  }

  return getDiaryLabelFromEntry(item)
}

function getDiaryLabelFromEntry(item: DiaryEntry) {
  return getEntryEmotionList(item)
    .map(emotion => emotion.label)
    .join('、')
}

function getEntryEmotionList(item: DiaryEntry): EmotionOption[] {
  if (!item) {
    return []
  }

  if (Array.isArray(item.emotions) && item.emotions.length > 0) {
    return item.emotions
      .slice(0, 5)
      .map((emoji: string, index: number) => {
        const found = findEmotionByEmoji(emoji)

        if (found) {
          return found
        }

        const label = Array.isArray(item.emotionLabels)
          ? item.emotionLabels[index]
          : '心情'

        return {
          emoji,
          label: label || '心情',
          color: '#C9B9A4'
        }
      })
  }

  const emojiString = item.emotionEmoji || item.mood || ''
  const labelString = item.emotionLabel || item.moodLabel || ''

  if (!emojiString) {
    return []
  }

  const matched = emotionOptions.filter(option => {
    return emojiString.includes(option.emoji)
  })

  if (matched.length > 0) {
    return matched.slice(0, 5)
  }

  return [
    {
      emoji: emojiString,
      label: labelString || '心情',
      color: '#C9B9A4'
    }
  ]
}

function findEmotionByEmoji(emoji: string) {
  return emotionOptions.find(item => item.emoji === emoji)
}

function buildSegments(emotions: EmotionOption[]): ChartSegment[] {
  if (!emotions.length) {
    return []
  }

  const counter = new Map<string, ChartSegment>()

  emotions.forEach(emotion => {
    const key = emotion.emoji

    if (!counter.has(key)) {
      counter.set(key, {
        key,
        emoji: emotion.emoji,
        label: emotion.label,
        color: emotion.color,
        count: 0,
        percent: 0
      })
    }

    const current = counter.get(key)

    if (current) {
      current.count += 1
    }
  })

  const total = Array.from(counter.values()).reduce((sum, item) => {
    return sum + item.count
  }, 0)

  return Array.from(counter.values()).map(item => {
    return {
      ...item,
      percent: total ? Math.round((item.count / total) * 10000) / 100 : 0
    }
  })
}

function buildDayTooltip(date: string, segments: ChartSegment[]) {
  if (!segments.length) {
    return `${date}：暂无心情记录`
  }

  const text = segments
    .map(item => `${item.emoji}${item.label} ${item.percent}%`)
    .join('，')

  return `${date}：${text}`
}

function buildMonthTooltip(month: number, segments: ChartSegment[], diaryCount: number) {
  if (!segments.length) {
    return `${month}月：暂无心情记录`
  }

  const text = segments
    .map(item => `${item.emoji}${item.label} ${item.percent}%`)
    .join('，')

  return `${month}月：${diaryCount}条记录，${text}`
}

function getWeekDays(baseDate: Date) {
  const date = new Date(baseDate)
  const day = date.getDay() || 7
  const monday = new Date(date)

  monday.setDate(date.getDate() - day + 1)

  const weekdays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

  return Array.from({ length: 7 }).map((_, index) => {
    const item = new Date(monday)
    item.setDate(monday.getDate() + index)

    return {
      date: formatDate(item),
      day: item.getDate(),
      weekday: weekdays[index]
    }
  })
}

function getMonthDays(baseDate: Date) {
  const year = baseDate.getFullYear()
  const month = baseDate.getMonth()

  const firstDay = new Date(year, month, 1)
  const firstWeekday = firstDay.getDay() || 7

  const start = new Date(firstDay)
  start.setDate(firstDay.getDate() - firstWeekday + 1)

  return Array.from({ length: 42 }).map((_, index) => {
    const item = new Date(start)
    item.setDate(start.getDate() + index)

    return {
      key: `${formatDate(item)}-${index}`,
      date: formatDate(item),
      day: item.getDate(),
      inCurrentMonth: item.getMonth() === month
    }
  })
}

function formatDate(value: Date | string) {
  const date = value instanceof Date ? value : new Date(value)

  if (Number.isNaN(date.getTime())) {
    return new Date().toISOString().slice(0, 10)
  }

  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')

  return `${year}-${month}-${day}`
}
</script>

<style scoped>
.diary-page {
  position: relative;
  min-height: 100vh;
  padding: 34px 24px 60px;
  overflow: hidden;
  background: #faf8f3;
  color: #303236;
}

.page-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(circle at 12% 10%, rgba(221, 239, 226, 0.88), transparent 30%),
    radial-gradient(circle at 92% 4%, rgba(242, 229, 209, 0.9), transparent 30%),
    linear-gradient(180deg, #fbfaf6 0%, #f7f2eb 100%);
}

.diary-shell {
  position: relative;
  z-index: 1;
  width: min(100%, 1180px);
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 28px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #aaa095;
  font-size: 16px;
  letter-spacing: 0.03em;
}

.page-header h1 {
  margin: 0;
  font-size: 46px;
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.subtitle {
  margin: 12px 0 0;
  color: #8f8478;
  font-size: 17px;
}

.write-today-btn {
  border: none;
  border-radius: 999px;
  padding: 18px 32px;
  background: #bda27b;
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  box-shadow: 0 14px 32px rgba(168, 139, 101, 0.24);
}

.write-today-btn:hover {
  background: #aa8f68;
}

.toolbar-card,
.card,
.year-month-card {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(235, 227, 214, 0.72);
  box-shadow: 0 18px 46px rgba(171, 154, 130, 0.14);
}

.toolbar-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-bottom: 24px;
  padding: 18px;
  border-radius: 32px;
}

.view-tabs {
  display: flex;
  gap: 10px;
  padding: 6px;
  border-radius: 999px;
  background: #f4eee5;
}

.tab-btn {
  border: none;
  border-radius: 999px;
  padding: 12px 24px;
  background: transparent;
  color: #7f756a;
  font-size: 16px;
  cursor: pointer;
}

.tab-btn.active {
  background: #bda27b;
  color: #fff;
}

.date-switcher {
  display: flex;
  align-items: center;
  gap: 14px;
}

.circle-btn {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border: none;
  border-radius: 50%;
  background: #eee3d4;
  color: #7b6046;
  font-size: 28px;
  cursor: pointer;
}

.period-title {
  min-width: 240px;
  text-align: center;
  color: #6d6257;
  font-size: 18px;
  font-weight: 700;
}

.card {
  margin-bottom: 26px;
  padding: 26px;
  border-radius: 34px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-bottom: 22px;
}

.section-head h2 {
  margin: 0;
  color: #303236;
  font-size: 30px;
}

.refresh-btn {
  border: none;
  border-radius: 999px;
  padding: 12px 20px;
  background: #f0e6d8;
  color: #7b6046;
  cursor: pointer;
}

.chart-card {
  overflow: hidden;
}

.legend-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 16px;
  margin-bottom: 24px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 7px 12px;
  border-radius: 999px;
  background: #f8f5f0;
  color: #756d65;
  font-size: 13px;
}

.legend-dot {
  width: 11px;
  height: 11px;
  border-radius: 50%;
}

.chart-scroll {
  overflow-x: auto;
  padding-bottom: 8px;
}

.stacked-chart {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  min-height: 230px;
  padding: 18px 8px 4px;
  border-radius: 28px;
  background: #fbf8f3;
}

.month-chart {
  min-width: 980px;
}

.year-chart {
  justify-content: space-between;
  gap: 18px;
}

.bar-unit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-width: 26px;
}

.year-bar {
  flex: 1;
  min-width: 48px;
}

.bar-box {
  display: flex;
  flex-direction: column-reverse;
  justify-content: flex-start;
  width: 22px;
  height: 170px;
  overflow: hidden;
  border-radius: 999px;
  background: #eee7dd;
  box-shadow: inset 0 0 0 1px rgba(200, 184, 160, 0.28);
}

.year-bar-box {
  width: 34px;
  height: 190px;
}

.bar-segment {
  width: 100%;
  min-height: 4px;
}

.bar-empty {
  width: 100%;
  height: 100%;
  background: repeating-linear-gradient(
    135deg,
    #eee7dd,
    #eee7dd 6px,
    #f7f1e8 6px,
    #f7f1e8 12px
  );
}

.bar-label {
  color: #8f8478;
  font-size: 12px;
  white-space: nowrap;
}

.bar-label.today {
  padding: 2px 7px;
  border-radius: 999px;
  background: #bda27b;
  color: #fff;
}

.chart-hint {
  margin: 18px 0 0;
  color: #9f958b;
  font-size: 14px;
  line-height: 1.7;
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16px;
}

.day-card {
  min-height: 168px;
  border: none;
  border-radius: 28px;
  padding: 18px 12px;
  background: #f8f5f0;
  cursor: pointer;
  text-align: center;
  transition: transform 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.day-card:hover {
  transform: translateY(-3px);
  background: #f0e7db;
}

.day-card.today {
  box-shadow: inset 0 0 0 2px rgba(189, 162, 123, 0.45);
}

.day-card.hasDiary {
  background: #f1e3cf;
}

.weekday {
  margin: 0;
  color: #9f958b;
  font-size: 14px;
}

.day-number {
  margin: 8px 0 10px;
  color: #2f3135;
  font-size: 26px;
  font-weight: 800;
}

.emoji-display {
  min-height: 38px;
  margin-bottom: 10px;
  font-size: 30px;
  line-height: 1.2;
}

.day-preview {
  margin: 0;
  color: #8f8478;
  font-size: 14px;
  line-height: 1.4;
}

.month-week-head {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 12px;
  color: #9f958b;
  text-align: center;
  font-size: 15px;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 12px;
}

.month-day {
  position: relative;
  min-height: 100px;
  border: none;
  border-radius: 22px;
  background: #f8f5f0;
  cursor: pointer;
  padding: 12px;
  text-align: left;
}

.month-day:hover {
  background: #f0e7db;
}

.month-day.muted {
  opacity: 0.38;
}

.month-day.today {
  box-shadow: inset 0 0 0 2px rgba(189, 162, 123, 0.45);
}

.month-day.hasDiary {
  background: #f1e3cf;
}

.month-day-num {
  color: #5f5a54;
  font-size: 16px;
  font-weight: 700;
}

.month-emoji {
  position: absolute;
  left: 12px;
  right: 12px;
  bottom: 12px;
  font-size: 24px;
  line-height: 1.2;
}

.year-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
  margin-bottom: 26px;
}

.year-month-card {
  min-height: 160px;
  border: none;
  border-radius: 30px;
  padding: 24px;
  text-align: left;
  cursor: pointer;
}

.year-month-card:hover {
  background: #f7efe4;
}

.month-title {
  color: #2f3135;
  font-size: 24px;
  font-weight: 800;
}

.month-emotion-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  min-height: 40px;
  margin: 22px 0 12px;
}

.small-emoji {
  font-size: 24px;
}

.empty-month {
  color: #aaa095;
  font-size: 15px;
}

.month-count {
  margin: 0;
  color: #9b9288;
  font-size: 15px;
}

.empty-state {
  display: grid;
  place-items: center;
  align-content: center;
  min-height: 240px;
  text-align: center;
  color: #8f8478;
}

.empty-icon {
  display: grid;
  place-items: center;
  width: 78px;
  height: 78px;
  margin-bottom: 14px;
  border-radius: 50%;
  background: #e9f5ee;
  font-size: 36px;
}

.empty-state h3 {
  margin: 0 0 8px;
  color: #303236;
  font-size: 24px;
}

.empty-state p {
  margin: 0;
}

.recent-list {
  display: grid;
  gap: 14px;
}

.recent-item {
  display: flex;
  gap: 18px;
  width: 100%;
  border: none;
  border-radius: 28px;
  padding: 18px;
  background: #f8f5f0;
  cursor: pointer;
  text-align: left;
}

.recent-item:hover {
  background: #f0e7db;
}

.recent-emoji {
  display: grid;
  place-items: center;
  min-width: 66px;
  height: 66px;
  padding: 0 10px;
  border-radius: 24px;
  background: #ead8bf;
  font-size: 28px;
}

.recent-content {
  flex: 1;
  min-width: 0;
}

.recent-top {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.recent-top h3 {
  margin: 0;
  color: #303236;
  font-size: 20px;
}

.recent-top span {
  color: #9f958b;
  font-size: 14px;
  white-space: nowrap;
}

.recent-content p {
  margin: 0;
  color: #756d65;
  line-height: 1.6;
}

.recent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 12px;
}

.recent-tags span {
  border-radius: 999px;
  padding: 6px 12px;
  background: #eee3d4;
  color: #80664d;
  font-size: 13px;
}

@media (max-width: 980px) {
  .page-header,
  .toolbar-card {
    flex-direction: column;
    align-items: stretch;
  }

  .period-title {
    min-width: 0;
    flex: 1;
  }

  .week-grid,
  .month-grid,
  .month-week-head {
    grid-template-columns: repeat(7, minmax(42px, 1fr));
  }

  .day-card {
    min-height: 132px;
    padding: 12px 6px;
  }

  .year-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .month-chart {
    min-width: 880px;
  }
}

@media (max-width: 640px) {
  .diary-page {
    padding: 22px 14px 44px;
  }

  .page-header h1 {
    font-size: 36px;
  }

  .week-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .month-grid,
  .month-week-head {
    grid-template-columns: repeat(7, minmax(32px, 1fr));
  }

  .month-day {
    min-height: 72px;
    border-radius: 16px;
    padding: 8px;
  }

  .month-emoji {
    left: 8px;
    bottom: 8px;
    font-size: 18px;
  }

  .year-grid {
    grid-template-columns: 1fr;
  }

  .recent-item {
    flex-direction: column;
  }
}
</style>