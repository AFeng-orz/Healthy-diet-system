<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getHealthQuestions, getLatestHealthAssessment, submitHealthAssessment } from '../api/health'

const loading = ref(false)
const submitting = ref(false)
const questions = ref([])
const latest = ref(null)
const answers = reactive({})

const completedCount = computed(() => questions.value.filter((question) => answers[question.key] !== undefined).length)
const progress = computed(() => questions.value.length ? Math.round((completedCount.value / questions.value.length) * 100) : 0)
const isComplete = computed(() => questions.value.length > 0 && completedCount.value === questions.value.length)
const maxScore = computed(() => questions.value.length * 2)
const scoreLevel = computed(() => {
  if (!latest.value) return '--'
  const score = latest.value.totalScore
  if (score <= 4) return '习惯基础较好'
  if (score <= 8) return '轻度关注'
  if (score <= 12) return '需要调整'
  return '优先调整'
})

async function loadData() {
  loading.value = true
  try {
    const questionData = await getHealthQuestions()
    questions.value = questionData || []
  } catch (error) {
    ElMessage.error(error.message || '健康问卷加载失败')
  } finally {
    loading.value = false
  }

  try {
    latest.value = await getLatestHealthAssessment()
  } catch (error) {
    latest.value = null
  }
}

async function submit() {
  if (!isComplete.value) {
    ElMessage.warning('请先完成全部问卷题目')
    return
  }

  submitting.value = true
  try {
    const result = await submitHealthAssessment({
      answers: questions.value.map((question) => ({
        questionKey: question.key,
        score: answers[question.key]
      }))
    })
    latest.value = result
    ElMessage.success('健康画像已生成')
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function resetAnswers() {
  Object.keys(answers).forEach((key) => delete answers[key])
}

function formatTime(value) {
  if (!value) return '--'
  return String(value).replace('T', ' ').slice(0, 16)
}

onMounted(loadData)
</script>

<template>
  <section>
    <div class="section-heading">
      <div>
        <h1 class="page-title">健康问卷</h1>
        <p class="page-subtitle">
          通过生活方式问卷生成非医疗诊断型健康画像，用于后续饮食计划的个性化推荐说明。
        </p>
      </div>
      <span class="status-pill"><span class="status-dot"></span>已完成 {{ completedCount }} / {{ questions.length }}</span>
    </div>

    <div class="questionnaire-layout" v-loading="loading">
      <section class="question-card glass-card">
        <div class="progress-row">
          <div>
            <strong>生活方式评估</strong>
            <span>{{ progress }}% 完成</span>
          </div>
          <el-progress :percentage="progress" :stroke-width="10" color="#34c759" :show-text="false" />
        </div>

        <div class="question-list">
          <article v-for="(question, index) in questions" :key="question.key" class="question-item">
            <div class="question-title">
              <span>{{ index + 1 }}</span>
              <div>
                <h2>{{ question.title }}</h2>
                <p>{{ question.helpText }}</p>
              </div>
            </div>

            <el-radio-group v-model="answers[question.key]" class="answer-grid">
              <el-radio-button v-for="option in question.options" :key="option.score" :label="option.score">
                {{ option.label }}
              </el-radio-button>
            </el-radio-group>
          </article>
        </div>

        <div class="form-actions">
          <button class="ghost-button" type="button" @click="resetAnswers">重新填写</button>
          <button class="primary-button" type="button" :disabled="!isComplete || submitting" @click="submit">
            {{ submitting ? '生成中...' : '提交并生成画像' }}
          </button>
        </div>
      </section>

      <aside class="result-panel glass-card">
        <div class="result-header">
          <h2>最近画像</h2>
          <p>{{ latest ? `生成时间：${formatTime(latest.createTime)}` : '完成问卷后会在这里显示画像标签和建议。' }}</p>
        </div>

        <div v-if="latest" class="result-content">
          <div class="score-card">
            <span>生活方式风险分</span>
            <strong>{{ latest.totalScore }}<em>/ {{ maxScore }}</em></strong>
            <small>{{ scoreLevel }}。分数越高，说明越需要优先调整。</small>
          </div>

          <div class="score-ranges">
            <h3>分值说明</h3>
            <p><strong>0-4</strong> 习惯基础较好</p>
            <p><strong>5-8</strong> 轻度关注</p>
            <p><strong>9-12</strong> 需要调整</p>
            <p><strong>13-{{ maxScore }}</strong> 优先调整</p>
          </div>

          <div class="tag-list">
            <span v-for="tag in latest.tagNames" :key="tag">{{ tag }}</span>
          </div>

          <div class="suggestion-list">
            <h3>改善建议</h3>
            <p v-for="item in latest.suggestions" :key="item">{{ item }}</p>
          </div>
        </div>

        <div v-else class="empty-result">
          <strong>还没有画像结果</strong>
          <p>先完成左侧问卷。系统会根据饮食、运动、作息、饮水和进食习惯生成建议。</p>
        </div>
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

.questionnaire-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 22px;
  align-items: flex-start;
  margin-top: 26px;
}

.question-card,
.result-panel {
  padding: 28px;
}

.progress-row {
  display: grid;
  gap: 12px;
  margin-bottom: 22px;
}

.progress-row div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.progress-row strong,
.result-panel h2 {
  color: var(--hd-text);
  font-size: 22px;
}

.progress-row span,
.result-header p,
.empty-result p,
.score-card small {
  color: var(--hd-muted);
}

.question-list {
  display: grid;
  gap: 16px;
}

.question-item {
  padding: 20px;
  border: 1px solid var(--hd-border);
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.68);
}

.question-title {
  display: flex;
  gap: 14px;
  margin-bottom: 16px;
}

.question-title > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 32px;
  width: 32px;
  height: 32px;
  border-radius: 12px;
  color: #147a32;
  background: var(--hd-primary-soft);
  font-weight: 800;
}

.question-title h2 {
  margin: 0;
  font-size: 17px;
  line-height: 1.5;
}

.question-title p {
  margin: 6px 0 0;
  color: var(--hd-muted);
  font-size: 13px;
  line-height: 1.6;
}

.answer-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}

:deep(.answer-grid .el-radio-button) {
  display: block;
  min-width: 0;
}

:deep(.answer-grid .el-radio-button__inner) {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 58px;
  width: 100%;
  padding: 12px;
  border: 1px solid var(--hd-border) !important;
  border-radius: 18px !important;
  box-shadow: none !important;
  white-space: normal;
  line-height: 1.5;
}

:deep(.answer-grid .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  color: #147a32;
  border-color: rgba(52, 199, 89, 0.36) !important;
  background: var(--hd-primary-soft);
}

.form-actions {
  display: grid;
  grid-template-columns: 160px minmax(0, 1fr);
  gap: 12px;
  margin-top: 22px;
}

.ghost-button,
.primary-button {
  height: 48px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 760;
}

.ghost-button {
  color: var(--hd-text);
  background: #f1f4f1;
}

.primary-button {
  color: #ffffff;
  background: linear-gradient(135deg, #34c759, #27a844);
  box-shadow: 0 12px 24px rgba(52, 199, 89, 0.22);
}

.primary-button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
  box-shadow: none;
}

.result-header h2,
.suggestion-list h3 {
  margin: 0 0 8px;
}

.score-card {
  margin: 22px 0 18px;
  padding: 24px;
  border-radius: 28px;
  color: #ffffff;
  background: linear-gradient(135deg, #34c759, #147a32);
  box-shadow: 0 18px 36px rgba(52, 199, 89, 0.22);
}

.score-card span,
.score-card small {
  display: block;
}

.score-card small {
  color: rgba(255, 255, 255, 0.84);
}

.score-card strong {
  display: block;
  margin: 10px 0 6px;
  font-size: 46px;
  line-height: 1;
}

.score-card em {
  margin-left: 8px;
  font-size: 22px;
  font-style: normal;
  opacity: 0.78;
}

.score-ranges {
  display: grid;
  gap: 8px;
  margin-bottom: 20px;
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.72);
}

.score-ranges h3 {
  margin: 0 0 4px;
  font-size: 16px;
}

.score-ranges p {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0;
  color: var(--hd-muted);
  font-size: 13px;
  line-height: 1.6;
}

.score-ranges strong {
  color: #147a32;
  font-size: 13px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.tag-list span {
  padding: 9px 13px;
  border-radius: 999px;
  color: #147a32;
  background: var(--hd-primary-soft);
  font-size: 13px;
  font-weight: 700;
}

.suggestion-list {
  display: grid;
  gap: 10px;
}

.suggestion-list p,
.empty-result {
  margin: 0;
  padding: 16px;
  border-radius: 18px;
  color: var(--hd-muted);
  background: rgba(255, 255, 255, 0.72);
  line-height: 1.7;
}

.empty-result strong {
  display: block;
  margin-bottom: 8px;
  color: var(--hd-text);
}

@media (max-width: 1120px) {
  .questionnaire-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .section-heading,
  .progress-row div {
    align-items: flex-start;
    flex-direction: column;
  }

  .answer-grid,
  .form-actions {
    grid-template-columns: 1fr;
  }
}
</style>
