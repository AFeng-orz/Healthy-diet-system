<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getWeeklyReport } from '../api/reports'

const loading = ref(false)
const report = ref(null)

const todayCards = computed(() => [
  {
    label: '热量',
    value: report.value?.todayCalories,
    target: report.value?.targetCalories,
    rate: report.value?.todayCaloriesRate,
    unit: 'kcal'
  },
  {
    label: '蛋白质',
    value: report.value?.todayProtein,
    target: report.value?.targetProtein,
    rate: report.value?.todayProteinRate,
    unit: 'g'
  },
  {
    label: '脂肪',
    value: report.value?.todayFat,
    target: report.value?.targetFat,
    rate: report.value?.todayFatRate,
    unit: 'g'
  },
  {
    label: '碳水',
    value: report.value?.todayCarbs,
    target: report.value?.targetCarbs,
    rate: report.value?.todayCarbsRate,
    unit: 'g'
  }
])

const maxTrendCalories = computed(() => {
  const values = report.value?.trends?.map((item) => Number(item.calories || 0)) || []
  return Math.max(...values, Number(report.value?.targetCalories || 0), 1)
})

async function loadReport() {
  loading.value = true
  try {
    report.value = await getWeeklyReport()
  } catch (error) {
    ElMessage.error(error.message || '健康报告加载失败')
  } finally {
    loading.value = false
  }
}

function formatNumber(value, unit = '') {
  if (value === null || value === undefined || value === '') return '--'
  return `${value}${unit}`
}

function formatDate(value) {
  if (!value) return '--'
  return value.slice(5).replace('-', '/')
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

function trendHeight(calories) {
  return `${Math.max(Number(calories || 0) / maxTrendCalories.value * 100, 4)}%`
}

onMounted(loadReport)
</script>

<template>
  <section>
    <div class="section-heading">
      <div>
        <h1 class="page-title">健康报告</h1>
        <p class="page-subtitle">
          汇总今日摄入和最近 7 天热量趋势，帮助你观察饮食执行节奏。
        </p>
      </div>
      <span class="status-pill"><span class="status-dot"></span>{{ report?.startDate }} 至 {{ report?.endDate }}</span>
    </div>

    <div v-loading="loading" class="report-layout">
      <section class="report-hero glass-card">
        <div>
          <span class="status-pill"><span class="status-dot"></span>最近 7 天记录 {{ report?.recordedDays || 0 }} 天</span>
          <h2>本周饮食概览</h2>
          <p>{{ report?.summary || '记录饮食后，这里会生成本周总结。' }}</p>
        </div>
        <div class="hero-number">
          <span>日均热量</span>
          <strong>{{ formatNumber(report?.averageCalories, ' kcal') }}</strong>
          <em>目标 {{ formatNumber(report?.targetCalories, ' kcal') }}</em>
        </div>
      </section>

      <section class="today-grid">
        <article v-for="card in todayCards" :key="card.label" class="today-card glass-card">
          <div class="today-head">
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

      <section class="trend-card glass-card">
        <div class="trend-head">
          <div>
            <h2>最近 7 天热量趋势</h2>
            <p>柱形高度按本周最高摄入或目标热量自动缩放。</p>
          </div>
          <span>目标 {{ formatNumber(report?.targetCalories, ' kcal') }}</span>
        </div>
        <div class="trend-chart">
          <div v-for="item in report?.trends || []" :key="item.date" class="trend-day">
            <div class="trend-track">
              <span class="trend-bar" :style="{ height: trendHeight(item.calories) }"></span>
            </div>
            <strong>{{ formatNumber(item.calories, ' kcal') }}</strong>
            <em>{{ formatDate(item.date) }}</em>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<style scoped>
.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 28px;
}

.report-layout {
  display: grid;
  gap: 22px;
}

.report-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  align-items: center;
  gap: 34px;
  padding: 42px;
}

.report-hero h2 {
  margin: 24px 0 12px;
  font-size: 34px;
}

.report-hero p {
  max-width: 720px;
  margin: 0;
  color: var(--hd-muted);
  line-height: 1.9;
}

.hero-number {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 190px;
  padding: 30px;
  border-radius: 30px;
  background: linear-gradient(145deg, #e8f8ee, #ffffff);
  box-shadow: var(--hd-shadow-soft);
}

.hero-number span,
.hero-number em {
  color: var(--hd-muted);
  font-style: normal;
}

.hero-number strong {
  margin: 14px 0 8px;
  font-size: 42px;
  line-height: 1;
}

.today-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.today-card {
  padding: 24px;
}

.today-head {
  display: flex;
  justify-content: space-between;
  color: var(--hd-muted);
}

.today-head strong {
  color: #0b7f35;
}

.today-card h2 {
  margin: 16px 0 8px;
  font-size: 28px;
}

.today-card p {
  margin: 0 0 14px;
  color: var(--hd-muted);
}

.trend-card {
  padding: 32px;
}

.trend-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 26px;
}

.trend-head h2 {
  margin: 0 0 8px;
}

.trend-head p,
.trend-head span {
  margin: 0;
  color: var(--hd-muted);
}

.trend-chart {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 14px;
  min-height: 260px;
}

.trend-day {
  display: grid;
  grid-template-rows: 1fr auto auto;
  gap: 10px;
  min-width: 0;
  text-align: center;
}

.trend-track {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-height: 180px;
  border-radius: 999px;
  background: #f1f4f1;
  overflow: hidden;
}

.trend-bar {
  display: block;
  width: 100%;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(180deg, #6ee08b, #22a84d);
  box-shadow: 0 -8px 20px rgba(52, 199, 89, 0.18);
}

.trend-day strong {
  font-size: 13px;
  color: var(--hd-text);
}

.trend-day em {
  color: var(--hd-muted);
  font-size: 13px;
  font-style: normal;
}

@media (max-width: 980px) {
  .section-heading,
  .report-hero,
  .trend-head {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .today-grid,
  .trend-chart {
    grid-template-columns: 1fr;
  }

  .trend-day {
    grid-template-columns: 1fr 80px 54px;
    grid-template-rows: auto;
    align-items: center;
    text-align: left;
  }

  .trend-track {
    min-height: 18px;
    height: 18px;
  }

  .trend-bar {
    height: 100% !important;
  }
}
</style>
