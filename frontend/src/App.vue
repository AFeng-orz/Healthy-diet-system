<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCurrentUser } from './api/auth'

const route = useRoute()
const router = useRouter()
const isLoginPage = computed(() => route.path === '/login')
const showLogoutDialog = ref(false)
const currentUser = ref(readStoredUser())

const menus = [
  { path: '/home', label: '首页' },
  { path: '/profile', label: '健康档案' },
  { path: '/questionnaire', label: '健康问卷' },
  { path: '/foods', label: '食物库' },
  { path: '/diet-plans', label: '饮食计划' },
  { path: '/diet-records', label: '饮食记录' },
  { path: '/reports', label: '健康报告' }
]

function readStoredUser() {
  const userText = localStorage.getItem('healthy-diet-user')
  if (!userText) return null
  try {
    return JSON.parse(userText)
  } catch (error) {
    localStorage.removeItem('healthy-diet-user')
    return null
  }
}

function syncUserFromStorage() {
  currentUser.value = readStoredUser()
}

async function refreshCurrentUser() {
  const token = localStorage.getItem('healthy-diet-token')
  if (!token || isLoginPage.value) {
    currentUser.value = null
    return
  }

  const storedUser = readStoredUser()
  if (storedUser) {
    currentUser.value = storedUser
  }

  try {
    const user = await getCurrentUser()
    currentUser.value = user
    localStorage.setItem('healthy-diet-user', JSON.stringify(user))
  } catch (error) {
    currentUser.value = null
  }
}

function openLogoutDialog() {
  showLogoutDialog.value = true
}

function closeLogoutDialog() {
  showLogoutDialog.value = false
}

function confirmLogout() {
  localStorage.removeItem('healthy-diet-token')
  localStorage.removeItem('healthy-diet-user')
  currentUser.value = null
  showLogoutDialog.value = false
  ElMessage.success('已退出登录')
  router.replace('/login')
}

onMounted(() => {
  window.addEventListener('storage', syncUserFromStorage)
  window.addEventListener('healthy-diet-user-updated', syncUserFromStorage)
  refreshCurrentUser()
})

onBeforeUnmount(() => {
  window.removeEventListener('storage', syncUserFromStorage)
  window.removeEventListener('healthy-diet-user-updated', syncUserFromStorage)
})

watch(
  () => route.path,
  () => {
    syncUserFromStorage()
    refreshCurrentUser()
  }
)
</script>

<template>
  <router-view v-if="isLoginPage" />
  <div v-else class="app-shell">
    <header class="top-bar glass-card">
      <router-link to="/home" class="brand">
        <span class="brand-icon">H</span>
        <span>
          <strong>Healthy Diet</strong>
          <small>健康生活饮食规划</small>
        </span>
      </router-link>
      <nav class="nav-links">
        <router-link v-for="menu in menus" :key="menu.path" :to="menu.path">
          {{ menu.label }}
        </router-link>
      </nav>
      <div class="user-area">
        <span class="user-name">{{ currentUser?.nickname || currentUser?.username || '用户' }}</span>
        <button class="logout-chip" type="button" @click="openLogoutDialog">退出</button>
      </div>
    </header>

    <main class="page-container app-main">
      <router-view />
    </main>

    <teleport to="body">
      <transition name="dialog-fade">
        <div v-if="showLogoutDialog" class="logout-overlay" @click.self="closeLogoutDialog">
          <section class="logout-dialog" role="dialog" aria-modal="true" aria-labelledby="logout-title">
            <button class="dialog-close" type="button" aria-label="关闭" @click="closeLogoutDialog">×</button>
            <div class="dialog-mark">H</div>
            <h2 id="logout-title">要暂时离开吗？</h2>
            <p>
              退出后会回到登录页。你的账号信息和后续健康档案数据会继续保留，
              下次登录后可以继续使用。
            </p>
            <div class="dialog-actions">
              <button class="ghost-button" type="button" @click="closeLogoutDialog">继续使用</button>
              <button class="primary-button" type="button" @click="confirmLogout">确认退出</button>
            </div>
          </section>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  padding: 24px 0 48px;
}

.top-bar {
  position: sticky;
  top: 18px;
  z-index: 10;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  width: min(100% - 48px, var(--hd-page-width));
  margin: 0 auto;
  padding: 14px 18px;
  backdrop-filter: blur(20px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
}

.brand-icon,
.dialog-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #147a32;
  background: linear-gradient(145deg, #e8f8ee, #ffffff);
  font-weight: 800;
  box-shadow: inset 0 0 0 1px rgba(52, 199, 89, 0.14);
}

.brand-icon {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  font-size: 17px;
}

.brand strong,
.brand small {
  display: block;
}

.brand strong {
  font-size: 17px;
  letter-spacing: 0;
}

.brand small {
  margin-top: 2px;
  color: var(--hd-muted);
  font-size: 12px;
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.nav-links a {
  padding: 10px 13px;
  border-radius: 999px;
  color: var(--hd-muted);
  font-size: 14px;
  transition: all 0.2s ease;
}

.nav-links a:hover,
.nav-links a.router-link-active {
  color: #147a32;
  background: var(--hd-primary-soft);
}

.user-area {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding-left: 12px;
  border-left: 1px solid var(--hd-border);
}

.user-name {
  max-width: 88px;
  overflow: hidden;
  color: var(--hd-muted);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-chip {
  height: 32px;
  padding: 0 14px;
  border: 0;
  border-radius: 999px;
  color: #147a32;
  background: var(--hd-primary-soft);
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  transition: transform 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.logout-chip:hover {
  background: #dcf4e5;
  box-shadow: 0 6px 16px rgba(52, 199, 89, 0.16);
  transform: translateY(-1px);
}

.app-main {
  padding-top: 46px;
}

.logout-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(247, 248, 246, 0.72);
  backdrop-filter: blur(18px) saturate(140%);
}

.logout-dialog {
  position: relative;
  width: min(420px, 100%);
  padding: 34px;
  border: 1px solid rgba(255, 255, 255, 0.78);
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 70px rgba(31, 35, 31, 0.14);
  text-align: center;
}

.dialog-close {
  position: absolute;
  top: 16px;
  right: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: 50%;
  color: var(--hd-muted);
  background: #f3f5f2;
  cursor: pointer;
  font-size: 22px;
  line-height: 1;
}

.dialog-close:hover {
  color: var(--hd-text);
  background: #e9ece8;
}

.dialog-mark {
  width: 58px;
  height: 58px;
  margin: 0 auto 20px;
  border-radius: 20px;
  font-size: 22px;
}

.logout-dialog h2 {
  margin: 0 0 12px;
  color: var(--hd-text);
  font-size: 26px;
  font-weight: 760;
  letter-spacing: 0;
}

.logout-dialog p {
  margin: 0;
  color: var(--hd-muted);
  font-size: 15px;
  line-height: 1.8;
}

.dialog-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 28px;
}

.ghost-button,
.primary-button {
  height: 46px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 750;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.ghost-button {
  color: var(--hd-text);
  background: #f1f4f1;
}

.primary-button {
  color: #ffffff;
  background: linear-gradient(135deg, #34c759, #27a844);
  box-shadow: 0 12px 24px rgba(52, 199, 89, 0.24);
}

.ghost-button:hover,
.primary-button:hover {
  transform: translateY(-1px);
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.18s ease;
}

.dialog-fade-enter-active .logout-dialog,
.dialog-fade-leave-active .logout-dialog {
  transition: transform 0.18s ease, opacity 0.18s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

.dialog-fade-enter-from .logout-dialog,
.dialog-fade-leave-to .logout-dialog {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}

@media (max-width: 980px) {
  .top-bar {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .nav-links {
    justify-content: flex-start;
  }

  .user-area {
    padding-left: 0;
    border-left: 0;
  }
}

@media (max-width: 520px) {
  .logout-dialog {
    padding: 30px 24px;
    border-radius: 28px;
  }

  .dialog-actions {
    grid-template-columns: 1fr;
  }
}
</style>