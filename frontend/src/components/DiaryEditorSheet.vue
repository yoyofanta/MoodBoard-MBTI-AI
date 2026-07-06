<template>
  <div class="diary-sheet">
    <div class="sheet-header">
      <div>
        <p class="diary-date">{{ displayDate }}</p>
        <h1>记录今天的心情</h1>
      </div>

      <button class="close-btn" @click="emit('close')">
        关闭
      </button>
    </div>

    <!-- 情绪选择 -->
    <section class="card emotion-card">
      <p class="section-title">选择今天的主要情绪</p>

      <div class="emotion-grid">
        <button
          v-for="item in emotionOptions"
          :key="item.label"
          class="emotion-btn"
          :class="{ active: form.emotionEmoji === item.emoji }"
          @click="selectEmotion(item)"
        >
          {{ item.emoji }}
        </button>
      </div>
    </section>

    <!-- 日记内容 -->
    <section class="card write-card">
      <div class="mood-preview">
        <div
          class="mood-icon"
          :class="{ empty: !form.emotionEmoji }"
        >
          <span v-if="form.emotionEmoji">
            {{ form.emotionEmoji }}
          </span>
          <span v-else>
            ＋
          </span>
        </div>

        <div>
          <h3>
            {{ form.emotionLabel || '还没有选择心情' }}
          </h3>
          <p>
            {{ form.emotionEmoji ? '写点什么吧' : '先选择一个今天的心情吧' }}
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

    <!-- 图片信息 -->
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
        type="file"
        accept="image/*"
        class="hidden-file"
        @change="handleFileChange"
      />

      <div
        v-if="imagePreview"
        class="image-preview"
      >
        <img :src="imagePreview" alt="图片预览" />
        <button class="remove-image-btn" @click="removeImage">
          移除图片
        </button>
      </div>

      <p class="hint">
        本地图片当前仅用于预览；正式保存建议接后端上传接口，避免 base64 过长导致数据库字段报错。
      </p>
    </section>

    <!-- 位置信息 -->
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
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { api } from '../api'

type EmotionOption = {
  emoji: string
  label: string
}

const props = defineProps<{
  date?: string | Date
  entry?: any
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved'): void
}>()

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

const fileInputRef = ref<HTMLInputElement | null>(null)
const imagePreview = ref('')
const saving = ref(false)
const locating = ref(false)

const form = reactive({
  id: null as number | null,
  diaryDate: '',
  emotionEmoji: '',
  emotionLabel: '',
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

const displayDate = computed(() => {
  return form.diaryDate || formatDate(new Date())
})

watch(
  () => [props.date, props.entry],
  () => {
    resetForm()
  },
  { immediate: true, deep: true }
)

function resetForm() {
  const entry = props.entry || {}

  form.id = entry.id || null
  form.diaryDate =
    entry.diaryDate ||
    entry.date ||
    formatDate(props.date || new Date())

  /**
   * 关键修改：
   * 新建日记时默认 mood 为空。
   * 只有已有日记回显时，才读取 entry 里的情绪。
   */
  form.emotionEmoji =
    entry.emotionEmoji ||
    entry.mood ||
    entry.emoji ||
    ''

  form.emotionLabel =
    entry.emotionLabel ||
    entry.moodLabel ||
    ''

  form.content = entry.content || ''
  form.keyword = entry.keyword || entry.keywords || ''
  form.imageUrl = entry.imageUrl || entry.image_url || ''
  form.latitude = entry.latitude || null
  form.longitude = entry.longitude || null
  form.province = entry.province || ''
  form.city = entry.city || ''
  form.district = entry.district || ''
  form.locationName = entry.locationName || ''

  imagePreview.value = form.imageUrl || ''
}

function selectEmotion(item: EmotionOption) {
  form.emotionEmoji = item.emoji
  form.emotionLabel = item.label
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

  /**
   * 不直接把本地图片转 base64 存入 imageUrl。
   * 之前 image_url 字段过短的问题，就是 base64 太长导致的。
   * 后面接上传接口后，这里改成上传成功后的图片 URL。
   */
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
  if (!form.emotionEmoji) {
    alert('请先选择今天的心情')
    return
  }

  if (!form.content.trim()) {
    alert('请写一点今天的感受')
    return
  }

  saving.value = true

  const payload = {
    id: form.id,
    diaryDate: form.diaryDate,
    date: form.diaryDate,

    emotionEmoji: form.emotionEmoji,
    emotionLabel: form.emotionLabel,

    mood: form.emotionEmoji,
    moodLabel: form.emotionLabel,

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

    emit('saved')
    emit('close')
  } catch (e) {
    console.error('保存日记失败：', e)
    alert('保存失败，请检查后端接口或字段格式')
  } finally {
    saving.value = false
  }
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
.diary-sheet {
  width: min(100%, 1180px);
  margin: 0 auto;
  padding: 28px 34px 40px;
  color: #303236;
}

.sheet-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 30px;
}

.diary-date {
  margin: 0 0 4px;
  font-size: 22px;
  color: #9f968b;
}

.sheet-header h1 {
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
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 18px 46px rgba(171, 154, 130, 0.14);
  border: 1px solid rgba(235, 227, 214, 0.72);
}

.section-title {
  margin: 0 0 20px;
  color: #9b9288;
  font-size: 20px;
}

.emotion-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(100px, 1fr));
  gap: 18px;
}

.emotion-btn {
  height: 86px;
  border: none;
  border-radius: 28px;
  background: #f8f5f0;
  font-size: 38px;
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

.write-card {
  padding: 32px;
}

.mood-preview {
  display: flex;
  align-items: center;
  gap: 20px;
}

.mood-icon {
  display: grid;
  place-items: center;
  width: 76px;
  height: 76px;
  border-radius: 50%;
  background: #ead8bf;
  font-size: 36px;
}

.mood-icon.empty {
  background: #f3eee7;
  color: #b9ab9b;
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
  min-height: 170px;
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
  .diary-sheet {
    padding: 20px;
  }

  .sheet-header h1 {
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