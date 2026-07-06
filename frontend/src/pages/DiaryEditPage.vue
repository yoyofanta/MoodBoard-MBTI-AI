<template>
  <main class="diary-edit-page">
    <div class="page-bg"></div>

    <section class="editor-shell">
      <header class="editor-header">
        <div>
          <p class="date-text">{{ diaryDate }}</p>
          <h1>记录今天的心情</h1>
        </div>

        <button class="close-btn" @click="goBack">
          关闭
        </button>
      </header>

      <!-- 情绪选择 -->
      <section class="card emotion-card">
        <div class="section-head">
          <p class="section-title">选择今天的心情</p>
          <span class="count-tip">
            已选 {{ selectedEmotions.length }}/5
          </span>
        </div>

        <div class="emotion-grid">
          <button
            v-for="item in emotionOptions"
            :key="item.label"
            class="emotion-btn"
            :class="{ active: isSelected(item) }"
            @click="toggleEmotion(item)"
          >
            <span class="emoji">{{ item.emoji }}</span>
            <span class="label">{{ item.label }}</span>
          </button>
        </div>

        <p v-if="emotionError" class="error-text">
          {{ emotionError }}
        </p>
      </section>

      <!-- 文字输入 -->
      <section class="card write-card">
        <div class="mood-preview">
          <div class="mood-icons" :class="{ empty: selectedEmotions.length === 0 }">
            <template v-if="selectedEmotions.length">
              <span
                v-for="item in selectedEmotions"
                :key="item.label"
              >
                {{ item.emoji }}
              </span>
            </template>

            <span v-else class="empty-plus">＋</span>
          </div>

          <div>
            <h3>
              {{ selectedEmotions.length ? selectedEmotionLabels : '还没有选择心情' }}
            </h3>
            <p>
              {{ selectedEmotions.length ? '写点什么吧' : '可以先选 1-5 个心情' }}
            </p>
          </div>
        </div>

        <div class="divider"></div>

        <textarea
          v-model="form.content"
          class="diary-textarea"
          placeholder="写点什么吧……"
        ></textarea>
      </section>

      <!-- 关键词 -->
      <section class="card">
        <p class="section-title">关键词</p>
        <input
          v-model="form.keyword"
          class="soft-input"
          placeholder="例如：学习、考试、开心、压力"
        />
      </section>

      <!-- 图片 -->
      <section class="card">
        <p class="section-title">图片记录</p>

        <div class="image-row">
          <input
            v-model="form.imageUrl"
            class="soft-input"
            placeholder="可以粘贴图片 URL"
          />

          <button class="sub-btn" @click="triggerFileInput">
            选择图片
          </button>
        </div>

        <input
          ref="fileInputRef"
          class="hidden-file"
          type="file"
          accept="image/*"
          @change="handleFileChange"
        />

        <div v-if="imagePreview" class="image-preview">
          <img :src="imagePreview" alt="图片预览" />
          <button class="remove-image-btn" @click="removeImage">
            移除图片
          </button>
        </div>

        <p class="hint">
          当前本地图片仅用于预览；正式保存建议接后端上传接口，数据库只保存图片 URL。
        </p>
      </section>

      <!-- 位置 -->
      <section class="card">
        <p class="section-title">位置信息</p>

        <div class="location-row">
          <input
            v-model="form.locationName"
            class="soft-input"
            placeholder="当前位置"
          />

          <button
            class="sub-btn"
            :disabled="locating"
            @click="getLocation"
          >
            {{ locating ? '获取中' : '获取位置' }}
          </button>
        </div>

        <p
          v-if="form.latitude && form.longitude"
          class="location-text"
        >
          经纬度：{{ form.latitude }}, {{ form.longitude }}
        </p>
      </section>

      <button
        class="save-btn"
        :disabled="saving"
        @click="saveDiary"
      >
        {{ saving ? '保存中...' : '保存日记' }}
      </button>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { api } from '../api'

type EmotionOption = {
  emoji: string
  label: string
}

const route = useRoute()
const router = useRouter()

const emotionOptions: EmotionOption[] = [
  { emoji: '😊', label: '开心' },
  { emoji: '😢', label: '难过' },
  { emoji: '😡', label: '生气' },
  { emoji: '😰', label: '焦虑' },
  { emoji: '😴', label: '疲惫' },
  { emoji: '🥳', label: '兴奋' },
  { emoji: '😌', label: '平静' },
  { emoji: '😤', label: '烦躁' },
  { emoji: '🤯', label: '崩溃' },
  { emoji: '😶‍🌫️', label: '迷茫' },
  { emoji: '💪', label: '有力量' },
  { emoji: '❤️', label: '被爱' }
]

const selectedEmotions = ref<EmotionOption[]>([])
const emotionError = ref('')
const saving = ref(false)
const locating = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const imagePreview = ref('')

const form = reactive({
  id: null as number | null,
  content: '',
  keyword: '',
  imageUrl: '',
  latitude: null as number | null,
  longitude: null as number | null,
  province: '',
  city: '',
  district: '',
  locationName: ''
})

const diaryDate = computed(() => {
  const date = route.query.date as string | undefined
  return date || formatDate(new Date())
})

const selectedEmotionLabels = computed(() => {
  return selectedEmotions.value.map(item => item.label).join('、')
})

const selectedEmotionEmojis = computed(() => {
  return selectedEmotions.value.map(item => item.emoji).join('')
})

onMounted(() => {
  loadDiary()
})

async function loadDiary() {
  try {
    const res: any = await api.getDiaryByDate(diaryDate.value)
    const data = res?.data || res

    if (!data) {
      return
    }

    form.id = data.id || null
    form.content = data.content || ''
    form.keyword = data.keyword || data.keywords || ''
    form.imageUrl = data.imageUrl || data.image_url || ''
    form.latitude = data.latitude || null
    form.longitude = data.longitude || null
    form.province = data.province || ''
    form.city = data.city || ''
    form.district = data.district || ''
    form.locationName = data.locationName || ''

    imagePreview.value = form.imageUrl || ''

    /**
     * 回显逻辑：
     * 兼容三种情况：
     * 1. 新版本 emotions: ['😊','😢']
     * 2. 后端只存 emotionEmoji: '😊😢'
     * 3. 老版本只存 mood: '😌'
     */
    const emotionsFromArray = Array.isArray(data.emotions)
      ? data.emotions
      : []

    const emojiString =
      data.emotionEmoji ||
      data.mood ||
      ''

    const labelsString =
      data.emotionLabel ||
      data.moodLabel ||
      ''

    if (emotionsFromArray.length > 0) {
      selectedEmotions.value = emotionsFromArray
        .slice(0, 5)
        .map((emoji: string) => {
          return findEmotionByEmoji(emoji)
        })
        .filter(Boolean) as EmotionOption[]

      return
    }

    if (emojiString) {
      const emojiList = splitEmojiString(emojiString).slice(0, 5)
      const labelList = labelsString ? labelsString.split('、') : []

      selectedEmotions.value = emojiList.map((emoji, index) => {
        const found = findEmotionByEmoji(emoji)

        if (found) {
          return found
        }

        return {
          emoji,
          label: labelList[index] || '心情'
        }
      })
    }
  } catch (e) {
    console.error('读取日记失败：', e)
  }
}

function findEmotionByEmoji(emoji: string) {
  return emotionOptions.find(item => item.emoji === emoji)
}

function splitEmojiString(value: string) {
  const result: string[] = []

  for (const option of emotionOptions) {
    if (value.includes(option.emoji)) {
      result.push(option.emoji)
    }
  }

  return result
}

function isSelected(item: EmotionOption) {
  return selectedEmotions.value.some(selected => selected.emoji === item.emoji)
}

function toggleEmotion(item: EmotionOption) {
  emotionError.value = ''

  const index = selectedEmotions.value.findIndex(
    selected => selected.emoji === item.emoji
  )

  if (index !== -1) {
    selectedEmotions.value.splice(index, 1)
    return
  }

  if (selectedEmotions.value.length >= 5) {
    emotionError.value = '最多只能选择 5 个心情'
    return
  }

  selectedEmotions.value.push(item)
}

function triggerFileInput() {
  fileInputRef.value?.click()
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]

  if (!file) {
    return
  }

  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    alert('图片不能超过 5MB')
    return
  }

  const objectUrl = URL.createObjectURL(file)
  imagePreview.value = objectUrl
  form.imageUrl = ''
}

function removeImage() {
  imagePreview.value = ''
  form.imageUrl = ''

  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

async function getLocation() {
  if (!navigator.geolocation) {
    alert('当前浏览器不支持定位')
    return
  }

  locating.value = true

  navigator.geolocation.getCurrentPosition(
    async position => {
      form.latitude = Number(position.coords.latitude.toFixed(6))
      form.longitude = Number(position.coords.longitude.toFixed(6))

      try {
        const res: any = await api.reverseLocation(
          form.latitude,
          form.longitude
        )

        const data = res?.data || res || {}

        form.province = data.province || ''
        form.city = data.city || ''
        form.district = data.district || ''

        form.locationName =
          data.locationName ||
          `${form.city} ${form.district}`.trim() ||
          '当前位置'
      } catch (e) {
        console.error('逆地理编码失败：', e)
        form.locationName = '当前位置'
      } finally {
        locating.value = false
      }
    },
    error => {
      console.error('浏览器定位失败：', error)
      locating.value = false
      alert('获取位置失败，请检查浏览器定位权限')
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

async function saveDiary() {
  if (selectedEmotions.value.length === 0) {
    alert('请至少选择一个心情')
    return
  }

  if (selectedEmotions.value.length > 5) {
    alert('最多只能选择 5 个心情')
    return
  }

  if (!form.content.trim()) {
    alert('请写一点今天的感受')
    return
  }

  saving.value = true

  const payload = {
    id: form.id,
    diaryDate: diaryDate.value,
    date: diaryDate.value,

    /**
     * 兼容后端旧字段：
     * mood / emotionEmoji 仍然传，但现在传的是多个 emoji 拼接。
     */
    mood: selectedEmotionEmojis.value,
    emotionEmoji: selectedEmotionEmojis.value,

    moodLabel: selectedEmotionLabels.value,
    emotionLabel: selectedEmotionLabels.value,

    /**
     * 新字段：
     * 如果后端支持数组，就可以直接用。
     * 如果后端暂时不支持，通常会被忽略，不影响旧字段保存。
     */
    emotions: selectedEmotions.value.map(item => item.emoji),
    emotionLabels: selectedEmotions.value.map(item => item.label),

    content: form.content.trim(),
    keyword: form.keyword.trim(),
    keywords: form.keyword.trim(),

    imageUrl: form.imageUrl,

    latitude: form.latitude,
    longitude: form.longitude,
    province: form.province,
    city: form.city,
    district: form.district,
    locationName: form.locationName
  }

  try {
    if (form.id && api.updateDiary) {
      await api.updateDiary(form.id, payload)
    } else {
      await api.saveDiary(payload)
    }

    router.push('/app/diary')
  } catch (e) {
    console.error('保存日记失败：', e)
    alert('保存失败，请检查后端接口或字段格式')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/app/diary')
}

function formatDate(value: string | Date) {
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
.diary-edit-page {
  position: relative;
  min-height: 100vh;
  padding: 34px 24px 54px;
  overflow: hidden;
  background: #faf8f3;
  color: #303236;
}

.page-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(circle at 12% 12%, rgba(221, 239, 226, 0.85), transparent 30%),
    radial-gradient(circle at 92% 8%, rgba(242, 229, 209, 0.9), transparent 28%),
    linear-gradient(180deg, #fbfaf6 0%, #f6f2eb 100%);
}

.editor-shell {
  position: relative;
  z-index: 1;
  width: min(100%, 1180px);
  margin: 0 auto;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 30px;
}

.date-text {
  margin: 0 0 4px;
  font-size: 22px;
  color: #9f968b;
}

.editor-header h1 {
  margin: 0;
  font-size: 44px;
  line-height: 1.1;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.close-btn {
  border: none;
  border-radius: 999px;
  padding: 20px 34px;
  background: #f0e6d8;
  color: #7b654e;
  font-size: 18px;
  cursor: pointer;
}

.close-btn:hover {
  background: #e6d6c1;
}

.card {
  margin-bottom: 26px;
  padding: 26px;
  border-radius: 34px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 46px rgba(171, 154, 130, 0.14);
  border: 1px solid rgba(235, 227, 214, 0.72);
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
  margin-bottom: 20px;
}

.section-title {
  margin: 0;
  color: #9b9288;
  font-size: 20px;
}

.count-tip {
  color: #b29d82;
  font-size: 16px;
}

.emotion-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(100px, 1fr));
  gap: 18px;
}

.emotion-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 98px;
  border: none;
  border-radius: 28px;
  background: #f8f5f0;
  cursor: pointer;
  transition: transform 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.emotion-btn:hover {
  transform: translateY(-2px);
  background: #f0e7db;
}

.emotion-btn.active {
  background: #ead8bf;
  box-shadow: inset 0 0 0 2px rgba(184, 151, 108, 0.36);
}

.emoji {
  font-size: 36px;
  line-height: 1;
}

.label {
  margin-top: 8px;
  font-size: 14px;
  color: #8c8176;
}

.error-text {
  margin: 16px 0 0;
  color: #c96b6b;
  font-size: 15px;
}

.write-card {
  padding: 32px;
}

.mood-preview {
  display: flex;
  align-items: center;
  gap: 20px;
}

.mood-icons {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 76px;
  height: 76px;
  padding: 0 16px;
  border-radius: 999px;
  background: #ead8bf;
  font-size: 34px;
  gap: 4px;
}

.mood-icons.empty {
  width: 76px;
  padding: 0;
  background: #f3eee7;
  color: #b9ab9b;
}

.empty-plus {
  font-size: 34px;
}

.mood-preview h3 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #343434;
}

.mood-preview p {
  margin: 0;
  font-size: 17px;
  color: #aaa096;
}

.divider {
  height: 1px;
  margin: 26px 0;
  background: #eadfd0;
}

.diary-textarea {
  width: 100%;
  min-height: 180px;
  border: none;
  outline: none;
  resize: vertical;
  background: transparent;
  color: #333;
  font-size: 24px;
  line-height: 1.8;
}

.diary-textarea::placeholder {
  color: #bcb4aa;
}

.soft-input {
  width: 100%;
  border: none;
  outline: none;
  border-radius: 28px;
  padding: 20px 24px;
  background: #f7f2eb;
  color: #333;
  font-size: 18px;
}

.soft-input::placeholder {
  color: #aaa096;
}

.image-row,
.location-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 160px;
  gap: 18px;
  align-items: center;
}

.sub-btn {
  height: 62px;
  border: none;
  border-radius: 28px;
  background: #eee3d4;
  color: #80664d;
  font-size: 17px;
  cursor: pointer;
}

.sub-btn:hover:not(:disabled) {
  background: #e4d3bd;
}

.sub-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.hidden-file {
  display: none;
}

.image-preview {
  margin-top: 20px;
  position: relative;
  width: 240px;
  border-radius: 24px;
  overflow: hidden;
  background: #f5efe7;
}

.image-preview img {
  display: block;
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.remove-image-btn {
  width: 100%;
  border: none;
  padding: 12px;
  background: rgba(217, 108, 108, 0.92);
  color: #fff;
  cursor: pointer;
}

.hint {
  margin: 14px 0 0;
  color: #aaa096;
  font-size: 14px;
  line-height: 1.6;
}

.location-text {
  margin: 16px 0 0;
  color: #a29a91;
  font-size: 17px;
}

.save-btn {
  width: 100%;
  height: 72px;
  border: none;
  border-radius: 999px;
  background: #bda27b;
  color: #fff;
  font-size: 22px;
  cursor: pointer;
  box-shadow: 0 16px 34px rgba(168, 139, 101, 0.24);
}

.save-btn:hover:not(:disabled) {
  background: #aa8f68;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 900px) {
  .diary-edit-page {
    padding: 24px 16px 40px;
  }

  .editor-header h1 {
    font-size: 34px;
  }

  .emotion-grid {
    grid-template-columns: repeat(3, 1fr);
  }

  .image-row,
  .location-row {
    grid-template-columns: 1fr;
  }

  .sub-btn {
    width: 100%;
  }
}
</style>