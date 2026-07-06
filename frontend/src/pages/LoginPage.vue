<template>
  <div class="login-page">
    <!-- 顶部欢迎卡片 -->
    <section class="hero-card">
      <p class="hero-subtitle">MoodBoard × MBTI</p>
      <h1 class="hero-title">欢迎回到树洞</h1>
      <p class="hero-desc">
        记录情绪，选择人格朋友，开启一场温柔的内心对话。
      </p>
    </section>

    <!-- 登录注册卡片 -->
    <section class="login-card">
      <!-- 登录 / 注册切换 -->
      <div class="login-tabs">
        <button
          class="tab-btn"
          :class="{ active: mode === 'login' }"
          @click="switchMode('login')"
        >
          登录
        </button>

        <button
          class="tab-btn"
          :class="{ active: mode === 'register' }"
          @click="switchMode('register')"
        >
          注册
        </button>
      </div>

      <!-- 输入框 -->
      <div class="form-area">
        <input
          v-model="form.username"
          class="soft-input"
          placeholder="请输入账号"
          autocomplete="username"
        />

        <input
          v-model="form.password"
          class="soft-input"
          type="password"
          placeholder="请输入密码"
          autocomplete="current-password"
          @keydown.enter="submit"
        />
      </div>

      <!-- 登录 / 注册按钮 -->
      <button
        class="primary-btn"
        :disabled="loading"
        @click="submit"
      >
        {{ loading ? '请稍等...' : mode === 'login' ? '登录' : '注册' }}
      </button>

      <!-- 游客登录：只放在登录按钮下面 -->
      <button
        v-if="mode === 'login'"
        class="guest-btn"
        :disabled="loading"
        @click="guestLogin"
      >
        游客登录
      </button>

      <p class="tip-text">
        支持测试环境与联调环境切换，便于功能测试、接口测试和回归验证。
      </p>
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'

type ModeType = 'login' | 'register'

const router = useRouter()

const mode = ref<ModeType>('login')
const loading = ref(false)

const form = reactive({
  username: 'demo',
  password: '123456'
})

function switchMode(target: ModeType) {
  mode.value = target
}

function getPayload(res: any) {
  if (!res) {
    return {}
  }

  if (res.data?.data) {
    return res.data.data
  }

  if (res.data) {
    return res.data
  }

  if (res.code === 200 && res.data) {
    return res.data
  }

  return res
}

function getToken(res: any) {
  const data = getPayload(res)

  return (
    data.token ||
    data.accessToken ||
    data.jwt ||
    data?.user?.token ||
    ''
  )
}


async function submit() {
  if (!form.username.trim()) {
    alert('请输入账号')
    return
  }

  if (!form.password.trim()) {
    alert('请输入密码')
    return
  }

  loading.value = true

  try {
    if (mode.value === 'login') {
      await login()
    } else {
      await register()
    }
  } catch (e) {
    console.error(e)

    if (mode.value === 'login') {
      alert('账号或密码错误')
    } else {
      alert('注册失败，请换一个账号试试')
    }
  } finally {
    loading.value = false
  }
}

async function login() {
  try {
    loading.value = true

    const res: any = await api.login({
      username: form.username,
      password: form.password
    })

    const token = getToken(res)

    if (!token) {
      alert('登录失败：未获取到 token')
      return
    }

    saveLoginState(token, form.username)
    router.push('/app/diary')
  } catch (e) {
    console.error(e)
    alert('登录失败，请检查账号或密码')
  } finally {
    loading.value = false
  }
}

async function register() {
  try {
    loading.value = true

    await api.register({
      username: form.username,
      password: form.password
    })

    const loginRes: any = await api.login({
      username: form.username,
      password: form.password
    })

    const token = getToken(loginRes)

    if (!token) {
      alert('注册成功，但自动登录失败，请手动登录')
      mode.value = 'login'
      return
    }

    saveLoginState(token, form.username)

    alert('注册成功，已自动登录')
    router.push('/app/diary')
  } catch (e: any) {
    console.error(e)

    const msg =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.message ||
      '注册失败，请稍后再试'

    alert(msg)
  } finally {
    loading.value = false
  }
}

async function guestLogin() {
  const guestUsername = `guest_${Date.now()}`
  const guestPassword = 'guest123456'

  try {
    loading.value = true

    await api.register({
      username: guestUsername,
      password: guestPassword
    })

    const loginRes: any = await api.login({
      username: guestUsername,
      password: guestPassword
    })

    const token = getToken(loginRes)

    if (!token) {
      alert('游客登录失败：未获取到 token')
      return
    }

    saveLoginState(token, guestUsername)

    try {
      await api.saveProfile({
        nickname: '游客',
        occupation: 'student',
        ageRange: '18-22',
        gender: '未透露',
        residentPersona: 'INFJ'
      })
    } catch (e) {
      console.warn('游客默认资料保存失败，不影响登录', e)
    }

    router.push('/app/diary')
  } catch (e: any) {
    console.error(e)

    const msg =
      e?.response?.data?.message ||
      e?.response?.data?.error ||
      e?.message ||
      '游客登录失败，请稍后再试'

    alert(msg)
  } finally {
    loading.value = false
  }
}

function saveAuth(token: string, userKey: string) {
  localStorage.setItem('token', token)
  localStorage.setItem('moodboard_current_user', userKey || 'user')
}

function saveLoginState(token: string, userKey: string) {
  localStorage.setItem('token', token)
  localStorage.setItem('moodboard_current_user', userKey || 'user')
}

</script>

<style scoped>
.login-page {
  min-height: 100vh;
  padding: 40px 16px;
  background:
    radial-gradient(circle at 20% 10%, rgba(234, 219, 200, 0.35), transparent 30%),
    radial-gradient(circle at 80% 30%, rgba(175, 201, 184, 0.25), transparent 28%),
    #f8f6f1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hero-card {
  width: min(720px, 100%);
  border-radius: 48px;
  background: rgba(255, 255, 255, 0.86);
  padding: 56px 54px;
  box-shadow: 0 24px 70px rgba(64, 52, 42, 0.08);
}

.hero-subtitle {
  font-size: 20px;
  color: #9b968c;
}

.hero-title {
  margin-top: 18px;
  font-size: 44px;
  line-height: 1.2;
  font-weight: 800;
  color: #2f2f2f;
}

.hero-desc {
  margin-top: 24px;
  font-size: 22px;
  line-height: 1.7;
  color: #8e887f;
}

.login-card {
  width: min(720px, 100%);
  margin-top: 52px;
  border-radius: 44px;
  background: rgba(255, 255, 255, 0.88);
  padding: 34px;
  box-shadow: 0 24px 70px rgba(64, 52, 42, 0.08);
}

.login-tabs {
  display: flex;
  width: 100%;
  border-radius: 999px;
  background: #f3eee6;
  padding: 6px;
  margin-bottom: 24px;
}

.tab-btn {
  flex: 1;
  border: none;
  border-radius: 999px;
  padding: 13px 0;
  background: transparent;
  font-size: 18px;
  color: #6e5741;
  cursor: pointer;
  transition: 0.2s;
}

.tab-btn.active {
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(64, 52, 42, 0.08);
  color: #2f2f2f;
}

.form-area {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.soft-input {
  width: 100%;
  border: none;
  border-radius: 999px;
  background: #f8f4ed;
  padding: 18px 26px;
  font-size: 20px;
  color: #2f2f2f;
  outline: none;
}

.soft-input::placeholder {
  color: #b6aea3;
}

.primary-btn {
  width: 100%;
  margin-top: 28px;
  border: none;
  border-radius: 999px;
  background: #c3ab89;
  padding: 18px 0;
  font-size: 20px;
  font-weight: 600;
  color: #ffffff;
  cursor: pointer;
  transition: 0.2s;
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.primary-btn:active {
  transform: scale(0.98);
}

.guest-btn {
  width: 100%;
  margin-top: 14px;
  border: none;
  border-radius: 999px;
  background: #f3eee6;
  padding: 16px 0;
  font-size: 18px;
  color: #6e5741;
  cursor: pointer;
  transition: 0.2s;
}

.guest-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.guest-btn:active {
  transform: scale(0.98);
}

.tip-text {
  margin-top: 28px;
  text-align: center;
  font-size: 16px;
  line-height: 1.7;
  color: #a39d92;
}

@media (max-width: 640px) {
  .login-page {
    padding: 24px 14px;
  }

  .hero-card {
    padding: 38px 30px;
    border-radius: 38px;
  }

  .hero-title {
    font-size: 34px;
  }

  .hero-desc {
    font-size: 18px;
  }

  .login-card {
    margin-top: 34px;
    padding: 24px;
    border-radius: 34px;
  }

  .soft-input {
    font-size: 17px;
    padding: 15px 22px;
  }

  .primary-btn,
  .guest-btn {
    font-size: 17px;
  }
}
</style>