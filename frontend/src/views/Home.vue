<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getWeeklyReport } from '../api/reports'


const loading = ref(false)
const report = ref(null)

const todayCards = computed(() => [
  { label: '热量', value: report.value?.todayCalories, target: report.value?.targetCalories, rate: report.value?.todayCaloriesRate, unit: 'kcal' },
  { label: '蛋白质', value: report.value?.todayProtein, target: report.value?.targetProtein, rate: report.value?.todayProteinRate, unit: 'g' },
  { label: '脂肪', value: report.value?.todayFat, target: report.value?.targetFat, rate: report.value?.todayFatRate, unit: 'g' },
  { label: '碳水', value: report.value?.todayCarbs, target: report.value?.targetCarbs, rate: report.value?.todayCarbsRate, unit: 'g' }
])

const quickLinks = [
  { title: '健康档案', desc: '基础信息与营养目标', path: '/profile', icon: '/ui/person.png' },
  { title: '饮食记录', desc: '记录今日实际摄入', path: '/diet-records', icon: '/ui/page.png' },
  { title: '饮食计划', desc: '生成一日三餐建议', path: '/diet-plans', icon: '/ui/icon-card-edit.png' },
  { title: '健康报告', desc: '查看 7 天趋势', path: '/reports', icon: '/ui/dite.png' }
]

const trendPreview = computed(() => report.value?.trends?.slice(-7) || [])

async function loadHomeData() {
  loading.value = true
  try {
    report.value = await getWeeklyReport()
  } catch (error) {
    ElMessage.error(error.message || '首页数据加载失败')
  } finally {
    loading.value = false
  }
}

function formatNumber(value, unit = '') {
  if (value === null || value === undefined || value === '') return '--'
  return `${value}${unit}`
}

function progress(rate) {
  if (rate === null || rate === undefined) return 0
  return Math.min(Number(rate), 120)
}

function progressStatus(rate) {
  if (rate === null || rate === undefined) return ''
  if (rate > 110) return 'exception'
  if (rate >= 80) return 'success'
  return ''
}

function trendWidth(rate) {
  if (rate === null || rate === undefined) return '4%'
  return `${Math.max(Math.min(Number(rate), 120), 4)}%`
}

function formatDate(value) {
  if (!value) return '--'
  return value.slice(5).replace('-', '/')
}

onMounted(loadHomeData)
</script>

<template>
  <section v-loading="loading" class="home-page">
    <section class="home-hero glass-card">
      <div class="hero-copy">
        <span class="status-pill"><span class="status-dot"></span>今日饮食概览</span>
        <h1 class="page-title">把每天吃了什么，变成看得懂的健康节奏。</h1>
        <p class="page-subtitle">{{ report?.summary || '记录饮食后，首页会展示今日完成度和最近 7 天趋势。' }}</p>
      </div>
      <div class="hero-metric">
        <span>今日热量完成</span>
        <strong>{{ report?.todayCaloriesRate === null || report?.todayCaloriesRate === undefined ? '--' : `${report.todayCaloriesRate}%` }}</strong>
        <small>{{ formatNumber(report?.todayCalories, ' kcal') }} / {{ formatNumber(report?.targetCalories, ' kcal') }}</small>
      </div>
    </section>

    <section class="metric-grid">
      <article v-for="card in todayCards" :key="card.label" class="metric-card glass-card">
        <div class="metric-head">
          <span>{{ card.label }}</span>
          <strong>{{ card.rate === null || card.rate === undefined ? '--' : `${card.rate}%` }}</strong>
        </div>
        <h2>{{ formatNumber(card.value, ` ${card.unit}`) }}</h2>
        <p>目标 {{ formatNumber(card.target, ` ${card.unit}`) }}</p>
        <el-progress
          :percentage="progress(card.rate)"
          :status="progressStatus(card.rate)"
          :show-text="false"
          :stroke-width="8"
        />
      </article>
    </section>

    <section class="home-columns">
      <article class="trend-card glass-card">
        <div class="card-head">
          <div>
            <h2>最近 7 天</h2>
            <p>{{ report?.recordedDays || 0 }} 天有记录，日均 {{ formatNumber(report?.averageCalories, ' kcal') }}</p>
          </div>
          <router-link to="/reports">查看报告</router-link>
        </div>
        <div class="trend-list">
          <div v-for="item in trendPreview" :key="item.date" class="trend-row">
            <span>{{ formatDate(item.date) }}</span>
            <div class="trend-line"><i :style="{ width: trendWidth(item.caloriesRate) }"></i></div>
            <strong>{{ formatNumber(item.calories, ' kcal') }}</strong>
          </div>
        </div>
      </article>

      <article class="quick-card glass-card">
        <div class="card-head">
          <div>
            <h2>常用入口</h2>
            <p>继续完善和记录你的饮食数据。</p>
          </div>
        </div>
        <div class="quick-grid">
          <router-link v-for="item in quickLinks" :key="item.path" :to="item.path" class="quick-link">
            <span class="quick-icon">
              <img :src="item.icon" :alt="item.title" />
            </span>
            <div>
              <strong>{{ item.title }}</strong>
              <em>{{ item.desc }}</em>
            </div>
          </router-link>
        </div>
      </article>
    </section>
  </section>
</template>

<style scoped>
.home-page {
  display: grid;
  gap: 22px;
}

.home-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 46px;
  align-items: center;
  padding: 44px;
}

.hero-copy .page-title {
  max-width: 760px;
  margin-top: 24px;
  font-size: 40px;
  line-height: 1.2;
}

.hero-copy .page-subtitle {
  max-width: 720px;
}

.hero-metric {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 220px;
  padding: 30px;
  border-radius: 32px;
  background: linear-gradient(145deg, #e8f8ee, #ffffff);
  box-shadow: 0 18px 45px rgba(52, 199, 89, 0.12);
}

.hero-metric span,
.hero-metric small {
  color: var(--hd-muted);
}

.hero-metric strong {
  margin: 16px 0 10px;
  font-size: 58px;
  line-height: 1;
  letter-spacing: -0.04em;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.metric-card {
  padding: 24px;
}

.metric-head,
.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.metric-head span,
.metric-card p,
.card-head p {
  color: var(--hd-muted);
}

.metric-head strong,
.card-head a {
  color: #0b7f35;
  font-weight: 750;
}

.metric-card h2 {
  margin: 16px 0 8px;
  font-size: 28px;
}

.metric-card p {
  margin: 0 0 14px;
}

.home-columns {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(340px, 0.65fr);
  gap: 22px;
}

.trend-card,
.quick-card {
  padding: 30px;
}

.card-head h2 {
  margin: 0 0 8px;
  font-size: 24px;
}

.card-head p {
  margin: 0;
}

.trend-list {
  display: grid;
  gap: 14px;
  margin-top: 24px;
}

.trend-row {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr) 84px;
  gap: 12px;
  align-items: center;
}

.trend-row span,
.trend-row strong {
  font-size: 13px;
}

.trend-row span {
  color: var(--hd-muted);
}

.trend-row strong {
  text-align: right;
}

.trend-line {
  height: 12px;
  border-radius: 999px;
  background: #edf2ee;
  overflow: hidden;
}

.trend-line i {
  display: block;
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #8ee6a5, #22a84d);
}

.quick-grid {
  display: grid;
  gap: 12px;
  margin-top: 24px;
}

.quick-link {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  border-radius: 20px;
  background: #fbfcfb;
  border: 1px solid rgba(229, 229, 234, 0.7);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.quick-link:hover {
  transform: translateY(-2px);
  box-shadow: var(--hd-shadow-soft);
}

.quick-link > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 16px;
  color: #0b7f35;
  background: var(--hd-primary-soft);
  font-weight: 800;
}

.quick-icon img {
  display: block;
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.quick-link strong,
.quick-link em {
  display: block;
}

.quick-link em {
  margin-top: 4px;
  color: var(--hd-muted);
  font-size: 13px;
  font-style: normal;
}

@media (max-width: 980px) {
  .home-hero,
  .home-columns,
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
