<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, saveProfile } from '../api/profile'

const loading = ref(false)
const saving = ref(false)
const formRef = ref()
const metrics = ref(null)
const hasProfile = ref(false)

const form = reactive({
  gender: 'male',
  age: 25,
  heightCm: 170,
  weightKg: 65,
  targetWeightKg: 60,
  activityLevel: 'light',
  dietGoal: 'fat_loss',
  avoidFoods: '',
  allergies: '',
  preferences: ''
})

const genderOptions = [
  { label: '男', value: 'male' },
  { label: '女', value: 'female' }
]

const activityOptions = [
  { label: '久坐少动', value: 'sedentary', desc: '几乎不运动，办公室久坐为主' },
  { label: '轻度活动', value: 'light', desc: '每周运动 1-3 次或日常步行较多' },
  { label: '中度活动', value: 'moderate', desc: '每周运动 3-5 次' },
  { label: '高强度活动', value: 'active', desc: '每周运动 6 次以上或体力劳动较多' }
]

const goalOptions = [
  { label: '减脂', value: 'fat_loss', desc: '在 TDEE 基础上减少约 400 kcal' },
  { label: '维持', value: 'maintain', desc: '接近当前日常消耗' },
  { label: '增肌', value: 'muscle_gain', desc: '在 TDEE 基础上增加约 300 kcal' }
]

const rules = {
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  age: [{ required: true, message: '请输入年龄', trigger: 'blur' }],
  heightCm: [{ required: true, message: '请输入身高', trigger: 'blur' }],
  weightKg: [{ required: true, message: '请输入当前体重', trigger: 'blur' }],
  targetWeightKg: [{ required: true, message: '请输入目标体重', trigger: 'blur' }],
  activityLevel: [{ required: true, message: '请选择运动频率', trigger: 'change' }],
  dietGoal: [{ required: true, message: '请选择饮食目标', trigger: 'change' }]
}

function assignProfile(profile) {
  if (!profile) return
  Object.assign(form, {
    gender: profile.gender || 'male',
    age: profile.age,
    heightCm: Number(profile.heightCm),
    weightKg: Number(profile.weightKg),
    targetWeightKg: Number(profile.targetWeightKg),
    activityLevel: profile.activityLevel || 'light',
    dietGoal: profile.dietGoal || 'fat_loss',
    avoidFoods: profile.avoidFoods || '',
    allergies: profile.allergies || '',
    preferences: profile.preferences || ''
  })
}

async function loadProfile() {
  loading.value = true
  try {
    const data = await getProfile()
    hasProfile.value = Boolean(data.profile)
    assignProfile(data.profile)
    metrics.value = data.metrics
  } catch (error) {
    ElMessage.error(error.message || '健康档案加载失败')
  } finally {
    loading.value = false
  }
}

async function submit() {
  await formRef.value.validate()
  saving.value = true
  try {
    const data = await saveProfile({ ...form })
    hasProfile.value = true
    assignProfile(data.profile)
    metrics.value = data.metrics
    ElMessage.success('健康档案已保存')
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function metricValue(key, unit = '') {
  if (!metrics.value || metrics.value[key] === null || metrics.value[key] === undefined) return '--'
  return `${metrics.value[key]}${unit}`
}

onMounted(loadProfile)
</script>

<template>
  <section>
    <div class="section-heading">
      <div>
        <h1 class="page-title">健康档案</h1>
        <p class="page-subtitle">
          记录身体基础信息后，系统会自动计算 BMI、BMR、TDEE、推荐热量和三大营养素目标。
        </p>
      </div>
      <span class="status-pill"><span class="status-dot"></span>{{ hasProfile ? '档案已建立' : '待完善档案' }}</span>
    </div>

    <div class="profile-layout" v-loading="loading">
      <section class="profile-form glass-card">
        <h2>基础信息</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid two">
            <el-form-item label="性别" prop="gender">
              <el-segmented v-model="form.gender" :options="genderOptions" />
            </el-form-item>
            <el-form-item label="年龄" prop="age">
              <el-input-number v-model="form.age" :min="12" :max="100" :precision="0" controls-position="right" />
            </el-form-item>
          </div>

          <div class="form-grid three">
            <el-form-item label="身高（cm）" prop="heightCm">
              <el-input-number v-model="form.heightCm" :min="80" :max="230" :precision="1" controls-position="right" />
            </el-form-item>
            <el-form-item label="当前体重（kg）" prop="weightKg">
              <el-input-number v-model="form.weightKg" :min="25" :max="250" :precision="1" controls-position="right" />
            </el-form-item>
            <el-form-item label="目标体重（kg）" prop="targetWeightKg">
              <el-input-number v-model="form.targetWeightKg" :min="25" :max="250" :precision="1" controls-position="right" />
            </el-form-item>
          </div>

          <el-form-item label="运动频率" prop="activityLevel">
            <el-radio-group v-model="form.activityLevel" class="option-grid activity-grid">
              <el-radio-button v-for="item in activityOptions" :key="item.value" :label="item.value">
                <strong>{{ item.label }}</strong>
                <small>{{ item.desc }}</small>
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="饮食目标" prop="dietGoal">
            <el-radio-group v-model="form.dietGoal" class="option-grid goal-grid">
              <el-radio-button v-for="item in goalOptions" :key="item.value" :label="item.value">
                <strong>{{ item.label }}</strong>
                <small>{{ item.desc }}</small>
              </el-radio-button>
            </el-radio-group>
          </el-form-item>

          <div class="form-grid three">
            <el-form-item label="忌口">
              <el-input v-model="form.avoidFoods" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="例如：不吃香菜、少吃辣" />
            </el-form-item>
            <el-form-item label="过敏">
              <el-input v-model="form.allergies" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="例如：花生、海鲜、乳糖不耐" />
            </el-form-item>
            <el-form-item label="饮食偏好">
              <el-input v-model="form.preferences" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="例如：偏好中餐、喜欢高蛋白早餐" />
            </el-form-item>
          </div>

          <el-button class="save-button" type="success" size="large" round :loading="saving" @click="submit">
            保存并计算营养目标
          </el-button>
        </el-form>
      </section>

      <aside class="metrics-panel glass-card">
        <div class="metrics-header">
          <h2>营养目标</h2>
          <p>基于 Mifflin-St Jeor 公式和活动系数估算，仅作生活方式参考。</p>
        </div>

        <div class="metric-hero">
          <span>BMI</span>
          <strong>{{ metricValue('bmi') }}</strong>
          <em>{{ metrics?.bmiLevel || '保存档案后生成' }}</em>
        </div>

        <div class="metric-list">
          <div class="metric-item">
            <span>基础代谢 BMR</span>
            <strong>{{ metricValue('bmr', ' kcal') }}</strong>
          </div>
          <div class="metric-item">
            <span>日常消耗 TDEE</span>
            <strong>{{ metricValue('tdee', ' kcal') }}</strong>
          </div>
          <div class="metric-item highlight">
            <span>推荐热量</span>
            <strong>{{ metricValue('recommendedCalories', ' kcal') }}</strong>
          </div>
        </div>

        <div class="macro-grid">
          <div>
            <span>蛋白质</span>
            <strong>{{ metricValue('proteinTarget', ' g') }}</strong>
          </div>
          <div>
            <span>脂肪</span>
            <strong>{{ metricValue('fatTarget', ' g') }}</strong>
          </div>
          <div>
            <span>碳水</span>
            <strong>{{ metricValue('carbsTarget', ' g') }}</strong>
          </div>
        </div>

        <p class="metrics-tip">
          后续饮食计划会优先使用这里的推荐热量和三大营养素目标，再结合忌口、过敏和偏好生成餐单。
        </p>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.profile-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 22px;
  align-items: flex-start;
  margin-top: 26px;
}

.profile-form,
.metrics-panel {
  padding: 28px;
}

.profile-form h2,
.metrics-panel h2 {
  margin: 0 0 18px;
  font-size: 22px;
}

.form-grid {
  display: grid;
  gap: 16px;
}

.form-grid.two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-grid.three {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-segmented) {
  width: 100%;
  --el-segmented-item-selected-color: #147a32;
  --el-segmented-item-selected-bg-color: #ffffff;
  background: #f1f4f1;
}

.option-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}

.goal-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

:deep(.activity-grid .el-radio-button) {
  display: block;
  min-width: 0;
  height: 100%;
}

:deep(.activity-grid .el-radio-button__inner) {
  min-height: 86px;
}

:deep(.option-grid .el-radio-button__inner) {
  display: block;
  height: 100%;
  padding: 14px;
  border: 1px solid var(--hd-border) !important;
  border-radius: 18px !important;
  box-shadow: none !important;
  white-space: normal;
  text-align: left;
}

:deep(.option-grid .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  color: #147a32;
  border-color: rgba(52, 199, 89, 0.36) !important;
  background: var(--hd-primary-soft);
}

:deep(.option-grid strong),
:deep(.option-grid small) {
  display: block;
}

:deep(.option-grid small) {
  margin-top: 6px;
  color: var(--hd-muted);
  font-size: 12px;
  line-height: 1.5;
}

.save-button {
  width: 100%;
  margin-top: 8px;
}

.metrics-header p,
.metrics-tip {
  margin: 0;
  color: var(--hd-muted);
  font-size: 14px;
  line-height: 1.8;
}

.metric-hero {
  margin: 22px 0;
  padding: 24px;
  border-radius: 28px;
  color: #ffffff;
  background: linear-gradient(135deg, #34c759, #147a32);
  box-shadow: 0 18px 36px rgba(52, 199, 89, 0.24);
}

.metric-hero span,
.metric-hero em {
  display: block;
}

.metric-hero span {
  opacity: 0.84;
  font-size: 13px;
}

.metric-hero strong {
  display: block;
  margin: 10px 0 6px;
  font-size: 48px;
  line-height: 1;
}

.metric-hero em {
  font-style: normal;
  opacity: 0.9;
}

.metric-list {
  display: grid;
  gap: 10px;
}

.metric-item,
.macro-grid div {
  padding: 16px;
  border: 1px solid var(--hd-border);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
}

.metric-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.metric-item span,
.macro-grid span {
  color: var(--hd-muted);
  font-size: 13px;
}

.metric-item strong,
.macro-grid strong {
  color: var(--hd-text);
  font-size: 18px;
}

.metric-item.highlight {
  border-color: rgba(52, 199, 89, 0.32);
  background: var(--hd-primary-soft);
}

.macro-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin: 18px 0;
}

.macro-grid div {
  text-align: center;
}

.macro-grid strong {
  display: block;
  margin-top: 6px;
}

@media (max-width: 1120px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .section-heading,
  .metric-item {
    align-items: flex-start;
    flex-direction: column;
  }

  .form-grid.two,
  .form-grid.three,
  .option-grid,
  .goal-grid,
  .macro-grid {
    grid-template-columns: 1fr;
  }
}
</style>
