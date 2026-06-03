<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generateDietPlan, getLatestDietPlan } from '../api/diet-plans'

const loading = ref(false)
const generating = ref(false)
const plan = ref(null)

const mealOrder = [
  { type: 'breakfast', name: '早餐', desc: '轻盈开始，保证稳定能量' },
  { type: 'lunch', name: '午餐', desc: '承担全天主要营养摄入' },
  { type: 'dinner', name: '晚餐', desc: '适度饱腹，避免过重负担' }
]

const meals = computed(() => {
  const items = plan.value?.items || []
  return mealOrder.map((meal) => ({
    ...meal,
    items: items.filter((item) => item.mealType === meal.type)
  }))
})

const completionRate = computed(() => {
  if (!plan.value?.targetCalories || !plan.value?.totalCalories) return '--'
  const value = Number(plan.value.totalCalories) / Number(plan.value.targetCalories) * 100
  return `${Math.round(value)}%`
})

async function loadLatestPlan() {
  loading.value = true
  try {
    plan.value = await getLatestDietPlan()
  } catch (error) {
    ElMessage.error(error.message || '饮食计划加载失败')
  } finally {
    loading.value = false
  }
}

async function handleGenerate() {
  generating.value = true
  try {
    plan.value = await generateDietPlan()
    ElMessage.success('饮食计划已生成')
  } catch (error) {
    ElMessage.error(error.message || '生成失败，请先确认健康档案和食物库数据')
  } finally {
    generating.value = false
  }
}

function formatNumber(value, unit = '') {
  if (value === null || value === undefined || value === '') return '--'
  return `${value}${unit}`
}

onMounted(loadLatestPlan)
</script>

<template>
  <section>
    <div class="section-heading">
      <div>
        <h1 class="page-title">饮食计划</h1>
        <p class="page-subtitle">
          基于健康档案、健康画像和食物库生成一日三餐建议，所有推荐仅作为生活方式参考。
        </p>
      </div>
      <el-button class="generate-button" type="success" size="large" round :loading="generating" @click="handleGenerate">
        {{ plan ? '重新生成计划' : '生成饮食计划' }}
      </el-button>
    </div>

    <div v-loading="loading" class="diet-plan-layout">
      <template v-if="plan">
        <section class="plan-hero glass-card">
          <div>
            <span class="status-pill"><span class="status-dot"></span>已生成 {{ plan.planDate }}</span>
            <h2>今日一日三餐建议</h2>
            <p>{{ plan.summary }}</p>
          </div>
          <div class="calorie-orb">
            <span>目标达成</span>
            <strong>{{ completionRate }}</strong>
            <em>{{ formatNumber(plan.totalCalories, ' kcal') }} / {{ formatNumber(plan.targetCalories, ' kcal') }}</em>
          </div>
        </section>

        <section class="macro-strip">
          <article class="macro-card">
            <span>总热量</span>
            <strong>{{ formatNumber(plan.totalCalories, ' kcal') }}</strong>
          </article>
          <article class="macro-card">
            <span>蛋白质</span>
            <strong>{{ formatNumber(plan.totalProtein, ' g') }}</strong>
          </article>
          <article class="macro-card">
            <span>脂肪</span>
            <strong>{{ formatNumber(plan.totalFat, ' g') }}</strong>
          </article>
          <article class="macro-card">
            <span>碳水</span>
            <strong>{{ formatNumber(plan.totalCarbs, ' g') }}</strong>
          </article>
        </section>

        <section class="meal-grid">
          <article v-for="meal in meals" :key="meal.type" class="meal-card glass-card">
            <div class="meal-head">
              <div>
                <span>{{ meal.desc }}</span>
                <h2>{{ meal.name }}</h2>
              </div>
              <strong>{{ meal.items.length }} 项</strong>
            </div>

            <div class="food-list">
              <div v-for="item in meal.items" :key="item.id" class="food-row">
                <div class="food-main">
                  <strong>{{ item.foodName }}</strong>
                  <span>{{ item.foodCategory }} · {{ formatNumber(item.grams, ' g') }}</span>
                </div>
                <div class="food-nutrients">
                  <span>{{ formatNumber(item.calories, ' kcal') }}</span>
                  <em>蛋白 {{ formatNumber(item.protein, ' g') }}</em>
                  <em>脂肪 {{ formatNumber(item.fat, ' g') }}</em>
                  <em>碳水 {{ formatNumber(item.carbs, ' g') }}</em>
                </div>
                <p>{{ item.reason }}</p>
              </div>
            </div>
          </article>
        </section>
      </template>

      <section v-else class="empty-plan glass-card">
        <span class="empty-icon">H</span>
        <h2>还没有饮食计划</h2>
        <p>先完善健康档案并准备好食物库数据，然后生成第一版一日三餐建议。</p>
        <el-button type="success" round size="large" :loading="generating" @click="handleGenerate">
          生成饮食计划
        </el-button>
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

.generate-button {
  min-width: 150px;
  height: 48px;
  box-shadow: 0 14px 30px rgba(52, 199, 89, 0.22);
}

.diet-plan-layout {
  min-height: 420px;
}

.plan-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 260px;
  gap: 36px;
  align-items: center;
  padding: 42px;
  margin-bottom: 28px;
}

.plan-hero h2 {
  margin: 24px 0 12px;
  font-size: 34px;
  line-height: 1.2;
  color: var(--hd-text);
}

.plan-hero p {
  margin: 0;
  color: var(--hd-muted);
  line-height: 1.9;
}

.calorie-orb {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 210px;
  padding: 32px;
  border-radius: 32px;
  background: linear-gradient(145deg, #ecfbf1, #ffffff);
  box-shadow: 0 18px 45px rgba(52, 199, 89, 0.12);
}

.calorie-orb span,
.calorie-orb em {
  color: var(--hd-muted);
  font-style: normal;
}

.calorie-orb strong {
  margin: 12px 0;
  font-size: 54px;
  line-height: 1;
  color: var(--hd-text);
}

.macro-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 28px;
}

.macro-card {
  padding: 24px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: var(--hd-shadow-soft);
}

.macro-card span {
  display: block;
  margin-bottom: 10px;
  color: var(--hd-muted);
}

.macro-card strong {
  font-size: 24px;
  color: var(--hd-text);
}

.meal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.meal-card {
  padding: 28px;
}

.meal-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--hd-border);
}

.meal-head span {
  color: var(--hd-muted);
  font-size: 14px;
}

.meal-head h2 {
  margin: 6px 0 0;
  color: var(--hd-text);
}

.meal-head strong {
  flex: 0 0 auto;
  padding: 7px 12px;
  border-radius: 999px;
  background: var(--hd-primary-soft);
  color: #0b7f35;
  font-size: 13px;
}

.food-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-top: 22px;
}

.food-row {
  padding: 18px;
  border-radius: 20px;
  background: #fbfcfb;
  border: 1px solid rgba(229, 229, 234, 0.76);
}

.food-main {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.food-main strong {
  color: var(--hd-text);
  font-size: 17px;
}

.food-main span {
  color: var(--hd-muted);
  font-size: 13px;
  white-space: nowrap;
}

.food-nutrients {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 14px;
  color: var(--hd-muted);
  font-size: 13px;
}

.food-nutrients span {
  color: #0b7f35;
  font-weight: 700;
}

.food-nutrients em {
  font-style: normal;
}

.food-row p {
  margin: 14px 0 0;
  color: var(--hd-muted);
  font-size: 14px;
  line-height: 1.7;
}

.empty-plan {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 420px;
  padding: 48px;
  text-align: center;
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 22px;
  background: var(--hd-primary-soft);
  color: #0b7f35;
  font-weight: 800;
  font-size: 28px;
}

.empty-plan h2 {
  margin: 20px 0 10px;
  font-size: 30px;
}

.empty-plan p {
  max-width: 480px;
  margin: 0 0 26px;
  color: var(--hd-muted);
  line-height: 1.8;
}

@media (max-width: 980px) {
  .section-heading,
  .plan-hero {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .macro-strip,
  .meal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
