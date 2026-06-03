<script setup>
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '../api/auth'

const router = useRouter()
const route = useRoute()
const activeTab = ref('login')
const loading = ref(false)
const formRef = ref()

const form = reactive({
  username: '',
  password: '',
  nickname: ''
})

const isRegister = computed(() => activeTab.value === 'register')
const submitText = computed(() => (isRegister.value ? '创建账号并进入' : '登录并进入'))

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '用户名长度应为3到30个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度应为6到30个字符', trigger: 'blur' }
  ],
  nickname: [
    { max: 30, message: '昵称不能超过30个字符', trigger: 'blur' }
  ]
}

function resetForm() {
  form.username = ''
  form.password = ''
  form.nickname = ''
  formRef.value?.clearValidate()
}

function saveLoginState(data) {
  localStorage.setItem('healthy-diet-token', data.token)
  localStorage.setItem('healthy-diet-user', JSON.stringify(data.user))
  window.dispatchEvent(new Event('healthy-diet-user-updated'))
}

async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = isRegister.value
      ? await register({ username: form.username, password: form.password, nickname: form.nickname })
      : await login({ username: form.username, password: form.password })
    saveLoginState(data)
    ElMessage.success(isRegister.value ? '注册成功，欢迎加入' : '登录成功')
    const redirect = route.query.redirect || '/home'
    router.replace(String(redirect))
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="intro-card glass-card">
      <span class="status-pill"><span class="status-dot"></span>Phase 3 账号体系</span>
      <h1>用一个轻量账号，保存你的健康饮食规划。</h1>
      <p>
        当前阶段已接入真实注册、登录和 Token 鉴权。健康建议仅作为生活方式参考，
        不替代医生诊断或治疗建议。
      </p>
      <div class="intro-list">
        <span>密码 BCrypt 加密</span>
        <span>JWT 登录状态</span>
        <span>前端路由保护</span>
      </div>
    </section>

    <section class="login-card glass-card">
      <div class="tab-switch">
        <button :class="{ active: activeTab === 'login' }" @click="activeTab = 'login'; resetForm()">登录</button>
        <button :class="{ active: activeTab === 'register' }" @click="activeTab = 'register'; resetForm()">注册</button>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" size="large" placeholder="例如：fit_user" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" size="large" type="password" placeholder="至少 6 位" show-password />
        </el-form-item>
        <el-form-item v-if="isRegister" label="昵称（可选）" prop="nickname">
          <el-input v-model="form.nickname" size="large" placeholder="例如：晨跑的阿青" clearable />
        </el-form-item>
        <el-button class="submit-button" type="success" size="large" round :loading="loading" @click="submit">
          {{ submitText }}
        </el-button>
      </el-form>

      <p class="form-tip">
        初学测试建议：先注册一个账号，再用同样用户名和密码登录。
      </p>
    </section>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  gap: 26px;
  align-items: center;
  min-height: 100vh;
  width: min(100% - 48px, 1080px);
  margin: 0 auto;
  padding: 36px 0;
}

.intro-card,
.login-card {
  padding: 42px;
}

.intro-card h1 {
  margin: 24px 0 16px;
  font-size: clamp(38px, 6vw, 64px);
  line-height: 1.05;
  letter-spacing: 0;
}

.intro-card p {
  max-width: 620px;
  margin: 0;
  color: var(--hd-muted);
  font-size: 17px;
  line-height: 1.9;
}

.intro-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 28px;
}

.intro-list span {
  padding: 10px 14px;
  border-radius: 999px;
  color: #147a32;
  background: var(--hd-primary-soft);
  font-size: 14px;
}

.login-card {
  align-self: stretch;
}

.tab-switch {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 28px;
  padding: 6px;
  border-radius: 999px;
  background: #f1f4f1;
}

.tab-switch button {
  height: 44px;
  border: 0;
  border-radius: 999px;
  color: var(--hd-muted);
  background: transparent;
  cursor: pointer;
  font-size: 15px;
  font-weight: 700;
}

.tab-switch button.active {
  color: #147a32;
  background: #ffffff;
  box-shadow: var(--hd-shadow-soft);
}

.submit-button {
  width: 100%;
  margin-top: 6px;
}

.form-tip {
  margin: 18px 0 0;
  color: var(--hd-muted);
  font-size: 13px;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }
}
</style>