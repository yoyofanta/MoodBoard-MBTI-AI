<template>
  <section class="tool-card">
    <div class="head">
      <div>
        <p class="sub-title">Tool Calling</p>
        <h3 class="title">位置工具调用演示</h3>
        <p class="desc">
          前端获取浏览器经纬度，后端调用高德逆地理编码接口，模拟一次 AI Agent 的工具调用过程。
        </p>
      </div>

      <button class="call-btn" :disabled="loading" @click="callTool">
        {{ loading ? '调用中...' : '调用位置工具' }}
      </button>
    </div>

    <div v-if="error" class="error-box">
      {{ error }}
    </div>

    <div v-if="toolData" class="tool-flow">
      <div class="step">
        <p class="step-label">Step 1</p>
        <h4>工具名称</h4>
        <p>{{ toolData.toolName }}</p>
      </div>

      <div class="step">
        <p class="step-label">Step 2</p>
        <h4>调用参数</h4>
        <pre>{{ formatJson(toolData.input) }}</pre>
      </div>

      <div class="step">
        <p class="step-label">Step 3</p>
        <h4>工具返回结果</h4>
        <pre>{{ formatJson(toolData.output) }}</pre>
      </div>

      <div class="location-result" v-if="toolData.output">
        <p class="result-title">当前位置识别结果</p>
        <p>
          {{ toolData.output.locationName || '当前位置' }}
        </p>
        <p class="address">
          {{ toolData.output.formattedAddress || '暂无详细地址' }}
        </p>
      </div>
    </div>

    <div v-else class="empty">
      点击按钮后，会显示一次完整的工具调用参数和返回结果。
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../api'

const loading = ref(false)
const error = ref('')
const toolData = ref<any>(null)

async function callTool() {
  error.value = ''
  toolData.value = null

  if (!navigator.geolocation) {
    error.value = '当前浏览器不支持定位功能'
    return
  }

  loading.value = true

  navigator.geolocation.getCurrentPosition(
    async position => {
      try {
        const lat = position.coords.latitude
        const lng = position.coords.longitude

        const res: any = await api.callLocationTool({
          lat,
          lng
        })

        const data = res.data || res

        toolData.value = data
      } catch (e: any) {
        console.error(e)
        error.value =
          e?.response?.data?.message ||
          e?.message ||
          '位置工具调用失败'
      } finally {
        loading.value = false
      }
    },
    err => {
      console.error(err)
      error.value = '获取浏览器定位失败，请检查浏览器定位权限'
      loading.value = false
    },
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 0
    }
  )
}

function formatJson(value: any) {
  return JSON.stringify(value, null, 2)
}
</script>

<style scoped>
.tool-card {
  margin-top: 24px;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.88);
  padding: 28px;
  box-shadow: 0 18px 50px rgba(64, 52, 42, 0.06);
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.sub-title {
  font-size: 14px;
  color: #a39d92;
}

.title {
  margin-top: 6px;
  font-size: 22px;
  font-weight: 700;
  color: #2f2f2f;
}

.desc {
  margin-top: 8px;
  max-width: 760px;
  font-size: 14px;
  line-height: 1.8;
  color: #8e887f;
}

.call-btn {
  border-radius: 999px;
  background: #afc9b8;
  padding: 13px 22px;
  font-size: 14px;
  color: white;
}

.call-btn:disabled {
  opacity: 0.6;
}

.error-box {
  margin-top: 18px;
  border-radius: 18px;
  background: #f8e2df;
  padding: 14px 16px;
  color: #b65f56;
}

.empty {
  margin-top: 20px;
  border-radius: 22px;
  background: #fbf7f1;
  padding: 22px;
  color: #9b968c;
  text-align: center;
}

.tool-flow {
  margin-top: 22px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.step {
  border-radius: 22px;
  background: #fbf7f1;
  padding: 18px;
}

.step-label {
  font-size: 12px;
  color: #a39d92;
}

.step h4 {
  margin-top: 6px;
  font-size: 16px;
  color: #4a4036;
}

.step p {
  margin-top: 8px;
  color: #6b6259;
}

pre {
  margin-top: 10px;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.6;
  color: #555;
}

.location-result {
  grid-column: 1 / -1;
  border-radius: 24px;
  background: #eef7f0;
  padding: 20px;
  color: #4a4036;
}

.result-title {
  font-size: 14px;
  font-weight: 700;
  color: #5f7d67;
}

.address {
  margin-top: 6px;
  color: #7b7166;
}

@media (max-width: 900px) {
  .tool-flow {
    grid-template-columns: 1fr;
  }

  .head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>