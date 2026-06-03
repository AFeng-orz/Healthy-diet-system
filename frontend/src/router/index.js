import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Profile from '../views/Profile.vue'
import HealthQuestionnaire from '../views/HealthQuestionnaire.vue'
import Foods from '../views/Foods.vue'
import DietPlans from '../views/DietPlans.vue'
import DietRecords from '../views/DietRecords.vue'
import Reports from '../views/Reports.vue'

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: Login, meta: { title: '登录注册', public: true } },
  { path: '/home', name: 'Home', component: Home, meta: { title: '首页' } },
  { path: '/profile', name: 'Profile', component: Profile, meta: { title: '健康档案' } },
  { path: '/questionnaire', name: 'HealthQuestionnaire', component: HealthQuestionnaire, meta: { title: '健康问卷' } },
  { path: '/foods', name: 'Foods', component: Foods, meta: { title: '食物库' } },
  { path: '/diet-plans', name: 'DietPlans', component: DietPlans, meta: { title: '饮食计划' } },
  { path: '/diet-records', name: 'DietRecords', component: DietRecords, meta: { title: '饮食记录' } },
  { path: '/reports', name: 'Reports', component: Reports, meta: { title: '健康报告' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const token = localStorage.getItem('healthy-diet-token')
  if (!to.meta.public && !token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && token) {
    return { path: '/home' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 健康生活饮食规划系统` : '健康生活饮食规划系统'
})

export default router