<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getFoods } from '../api/foods'
import { createDietRecord, deleteDietRecord, getDailyDietRecords } from '../api/diet-records'

const loading = ref(false)
const saving = ref(false)
const foodLoading = ref(false)
const formRef = ref()
const daily = ref(null)
const foodOptions = ref([])
const today = new Date().toISOString().slice(0, 10)

const form = reactive({
  recordDate: today,
  mealType: 'breakfast',
  foodId: null,
  grams: 100,
  remark: ''
})

const mealOptions = [
  { label: '早餐', value: 'breakfast' },
  { label: '午餐', value: 'lunch' },
  { label: '晚餐', value: 'dinner' },
  { label: '加餐', value: 'snack' }
]

const summaryCards = computed(() => [
  {
    key: 'calories',
    label: '热量',
    value: daily.value?.totalCalories,
    target: daily.value?.targetCalories,
    rate: daily.value?.caloriesRate,
    unit: 'kcal'
  },
  {
    key: 'protein',
    label: '蛋白质',
    value: daily.value?.totalProtein,
    target: daily.value?.targetProtein,
    rate: daily.value?.proteinRate,
    unit: 'g'
  },
  {
    key: 'fat',
    label: '脂肪',
    value: daily.value?.totalFat,
    target: daily.value?.targetFat,
    rate: daily.value?.fatRate,
    unit: 'g'
  },
  {
    key: 'carbs',
    label: '碳水',
    value: daily.value?.totalCarbs,
    target: daily.value?.targetCarbs,
    rate: daily.value?.carbsRate,
    unit: 'g'
  }
])

const groupedRecords = computed(() => {
  const records = daily.value?.records || []
  return mealOptions.map((meal) => ({
    ...meal,
    records: records.filter((record) => record.mealType === meal.value)
  }))
})

const rules = {
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  mealType: [{ required: true, message: '请选择餐次', trigger: 'change' }],
  foodId: [{ required: true, message: '请选择食物', trigger: 'change' }],
  grams: [{ required: true, message: '请输入克数', trigger: 'blur' }]
}

async function loadDailyRecords() {
  loading.value = true
  try {
    daily.value = await getDailyDietRecords({ date: form.recordDate })
  } catch (error) {
    ElMessage.error(error.message || '饮食记录加载失败')
  } finally {
    loading.value = false
  }
}

async function searchFoods(keyword = '') {
  foodLoading.value = true
  try {
    const data = await getFoods({
      pageNum: 1,
      pageSize: 20,
      keyword: keyword || undefined
    })
    foodOptions.value = data.records || []
  } catch (error) {
    ElMessage.error(error.message || '食物搜索失败')
  } finally {
    foodLoading.value = false
  }
}

async function submitRecord() {
  await formRef.value.validate()
  saving.value = true
  try {
    await createDietRecord({ ...form })
    ElMessage.success('饮食记录已添加')
    form.foodId = null
    form.grams = 100
    form.remark = ''
    await loadDailyRecords()
  } catch (error) {
    ElMessage.error(error.message || '添加失败')
  } finally {
    saving.value = false
  }
}

async function removeRecord(record) {
  try {
    await ElMessageBox.confirm(`删除“${record.foodName}”这条记录？`, '删除饮食记录', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      customClass: 'hd-danger-confirm',
      confirmButtonClass: 'hd-danger-confirm-button',
      cancelButtonClass: 'hd-danger-cancel-button',
      closeOnClickModal: false,
      showClose: false
    })
    await deleteDietRecord(record.id)
    ElMessage.success('已删除')
    await loadDailyRecords()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      ElMessage.error(error.message || '删除失败')
    }
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

function foodLabel(food) {
  return `${food.name} · ${food.category || '未分类'} · ${food.calories} kcal/100g`
}

onMounted(() => {
  loadDailyRecords()
  searchFoods()
})
</script>

<template>
  <section>
    <div class="section-heading">
      <div>
        <h1 class="page-title">饮食记录</h1>
        <p class="page-subtitle">
          记录今日实际摄入，系统会按每 100g 营养数据自动换算热量和三大营养素。
        </p>
      </div>
      <span class="status-pill"><span class="status-dot"></span>{{ daily?.recordDate || form.recordDate }}</span>
    </div>

    <div class="record-layout" v-loading="loading">
      <section class="record-main">
        <div class="summary-grid">
          <article v-for="card in summaryCards" :key="card.key" class="summary-card glass-card">
            <div class="summary-head">
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
        </div>

        <section class="record-list glass-card">
          <div class="list-head">
            <div>
              <h2>今日摄入</h2>
              <p>按餐次查看已经记录的食物。</p>
            </div>
            <el-date-picker
              v-model="form.recordDate"
              type="date"
              value-format="YYYY-MM-DD"
              :clearable="false"
              @change="loadDailyRecords"
            />
          </div>

          <div class="meal-records">
            <article v-for="meal in groupedRecords" :key="meal.value" class="meal-record">
              <div class="meal-record-head">
                <strong>{{ meal.label }}</strong>
                <span>{{ meal.records.length }} 项</span>
              </div>
              <div v-if="meal.records.length" class="record-items">
                <div v-for="record in meal.records" :key="record.id" class="record-item">
                  <div class="record-food">
                    <strong>{{ record.foodName }}</strong>
                    <span>{{ record.foodCategory }} · {{ formatNumber(record.grams, ' g') }}</span>
                  </div>
                  <div class="record-nutrients">
                    <strong>{{ formatNumber(record.calories, ' kcal') }}</strong>
                    <span>蛋白 {{ formatNumber(record.protein, ' g') }}</span>
                    <span>脂肪 {{ formatNumber(record.fat, ' g') }}</span>
                    <span>碳水 {{ formatNumber(record.carbs, ' g') }}</span>
                  </div>
                  <el-button link type="danger" @click="removeRecord(record)">删除</el-button>
                </div>
              </div>
              <p v-else class="empty-meal">暂无记录</p>
            </article>
          </div>
        </section>
      </section>

      <aside class="record-form glass-card">
        <h2>添加记录</h2>
        <p>选择食物并输入实际摄入克数。</p>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="餐次" prop="mealType">
            <el-segmented v-model="form.mealType" :options="mealOptions" />
          </el-form-item>
          <el-form-item label="食物" prop="foodId">
            <el-select
              v-model="form.foodId"
              filterable
              remote
              reserve-keyword
              :remote-method="searchFoods"
              :loading="foodLoading"
              placeholder="搜索食物名称"
            >
              <el-option
                v-for="food in foodOptions"
                :key="food.id"
                :label="foodLabel(food)"
                :value="food.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="克数" prop="grams">
            <el-input-number v-model="form.grams" :min="1" :max="3000" :precision="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit placeholder="可选，例如：少油、外卖估算" />
          </el-form-item>
          <el-button class="submit-button" type="success" size="large" round :loading="saving" @click="submitRecord">
            添加到今日记录
          </el-button>
        </el-form>
      </aside>
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

.record-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 22px;
}

.record-main {
  min-width: 0;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 22px;
}

.summary-card {
  padding: 22px;
}

.summary-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--hd-muted);
}

.summary-head strong {
  color: #0b7f35;
}

.summary-card h2 {
  margin: 14px 0 6px;
  font-size: 26px;
}

.summary-card p {
  margin: 0 0 14px;
  color: var(--hd-muted);
}

.record-list,
.record-form {
  padding: 28px;
}

.list-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 22px;
}

.list-head h2,
.record-form h2 {
  margin: 0 0 8px;
}

.list-head p,
.record-form p {
  margin: 0;
  color: var(--hd-muted);
  line-height: 1.7;
}

.meal-records {
  display: grid;
  gap: 14px;
}

.meal-record {
  padding: 18px;
  border: 1px solid var(--hd-border);
  border-radius: 20px;
  background: #fbfcfb;
}

.meal-record-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}

.meal-record-head span {
  color: #0b7f35;
  font-weight: 700;
}

.record-items {
  display: grid;
  gap: 12px;
}

.record-item {
  display: grid;
  grid-template-columns: minmax(160px, 1fr) minmax(280px, 1.4fr) auto;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border-radius: 16px;
  background: #ffffff;
}

.record-food strong {
  display: block;
  margin-bottom: 6px;
}

.record-food span {
  color: var(--hd-muted);
  font-size: 13px;
}

.record-nutrients {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  color: var(--hd-muted);
  font-size: 12px;
}

.record-nutrients strong,
.record-nutrients span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 30px;
  padding: 6px 8px;
  border-radius: 999px;
  background: #f3f6f3;
  white-space: nowrap;
  font-weight: 600;
}

.record-nutrients strong {
  color: #0b7f35;
  background: var(--hd-primary-soft);
}

.empty-meal {
  margin: 0;
  color: var(--hd-muted);
}

.record-form {
  align-self: start;
  position: sticky;
  top: 96px;
}

.record-form :deep(.el-select),
.record-form :deep(.el-input-number),
.record-form :deep(.el-segmented) {
  width: 100%;
}

.record-form :deep(.el-segmented) {
  --el-segmented-item-selected-color: #0b7f35;
  --el-segmented-item-selected-bg-color: var(--hd-primary-soft);
  --el-segmented-item-hover-color: #0b7f35;
  padding: 4px;
  border-radius: 16px;
  background: #f4f7f4;
}

.record-form :deep(.el-segmented__item) {
  border-radius: 12px;
  font-weight: 650;
}

.record-form :deep(.el-segmented__item.is-selected) {
  box-shadow: 0 8px 18px rgba(52, 199, 89, 0.14);
}

.submit-button {
  width: 100%;
  height: 46px;
}

@media (max-width: 1120px) {
  .record-layout,
  .summary-grid {
    grid-template-columns: 1fr;
  }

  .record-form {
    position: static;
  }

  .record-item {
    grid-template-columns: 1fr;
  }

  .record-nutrients {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
