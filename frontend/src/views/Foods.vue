<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createFood, deleteFood, getFoods, importFoods, updateFood } from '../api/foods'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const importDialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const foods = ref([])
const total = ref(0)
const importFile = ref(null)
const importResult = ref(null)
const importing = ref(false)
let keywordTimer = null

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  category: '',
  mealTag: '',
  highSugar: '',
  highFat: ''
})

const form = reactive({
  name: '',
  category: '',
  calories: 0,
  protein: 0,
  fat: 0,
  carbs: 0,
  mealTags: '',
  highSugar: false,
  highFat: false,
  remark: ''
})

const categoryOptions = ['主食', '蛋白质', '蔬菜', '水果', '乳制品', '坚果', '油脂', '饮品', '零食', '补充品', '其他']
const mealOptions = ['早餐', '午餐', '晚餐', '加餐']

const rules = {
  name: [{ required: true, message: '请输入食物名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  calories: [{ required: true, message: '请输入热量', trigger: 'blur' }],
  protein: [{ required: true, message: '请输入蛋白质', trigger: 'blur' }],
  fat: [{ required: true, message: '请输入脂肪', trigger: 'blur' }],
  carbs: [{ required: true, message: '请输入碳水', trigger: 'blur' }]
}

async function loadFoods() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      category: query.category || undefined,
      mealTag: query.mealTag || undefined,
      highSugar: query.highSugar === '' ? undefined : query.highSugar,
      highFat: query.highFat === '' ? undefined : query.highFat
    }
    const data = await getFoods(params)
    foods.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error(error.message || '食物列表加载失败')
  } finally {
    loading.value = false
  }
}

function search() {
  if (keywordTimer) {
    clearTimeout(keywordTimer)
    keywordTimer = null
  }
  query.pageNum = 1
  loadFoods()
}

function debounceSearch() {
  if (keywordTimer) {
    clearTimeout(keywordTimer)
  }
  keywordTimer = window.setTimeout(() => {
    query.pageNum = 1
    loadFoods()
  }, 400)
}

function resetQuery() {
  if (keywordTimer) {
    clearTimeout(keywordTimer)
    keywordTimer = null
  }
  query.pageNum = 1
  query.keyword = ''
  query.category = ''
  query.mealTag = ''
  query.highSugar = ''
  query.highFat = ''
  loadFoods()
}

function resetForm() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    category: '',
    calories: 0,
    protein: 0,
    fat: 0,
    carbs: 0,
    mealTags: '',
    highSugar: false,
    highFat: false,
    remark: ''
  })
  formRef.value?.clearValidate()
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openImportDialog() {
  importFile.value = null
  importResult.value = null
  importDialogVisible.value = true
}

function handleImportFileChange(uploadFile) {
  const file = uploadFile.raw
  if (!file) {
    importFile.value = null
    return
  }
  const filename = file.name.toLowerCase()
  if (!filename.endsWith('.xlsx') && !filename.endsWith('.xls')) {
    ElMessage.error('仅支持 .xlsx 或 .xls 格式的 Excel 文件')
    importFile.value = null
    return
  }
  importFile.value = file
}

function handleImportFileRemove() {
  importFile.value = null
}

function formatCategoryStats(categoryStats = {}) {
  const entries = Object.entries(categoryStats)
  return entries.map(([category, count]) => ({ category, count }))
}

async function submitImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择需要导入的 Excel 文件')
    return
  }
  importing.value = true
  try {
    const result = await importFoods(importFile.value)
    importResult.value = result
    ElMessage.success(`导入完成：新增 ${result.insertedRows} 条，更新 ${result.updatedRows} 条，跳过 ${result.skippedRows} 条`)
    resetQuery()
  } catch (error) {
    ElMessage.error(error.message || '导入失败')
  } finally {
    importing.value = false
  }
}

function openEditDialog(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    category: row.category,
    calories: Number(row.calories),
    protein: Number(row.protein),
    fat: Number(row.fat),
    carbs: Number(row.carbs),
    mealTags: row.mealTags || '',
    highSugar: row.highSugar === 1,
    highFat: row.highFat === 1,
    remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function submitForm() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form }
    if (editingId.value) {
      await updateFood(editingId.value, payload)
      ElMessage.success('食物已更新')
    } else {
      await createFood(payload)
      ElMessage.success('食物已新增')
    }
    dialogVisible.value = false
    loadFoods()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function removeFood(row) {
  try {
    await ElMessageBox.confirm(`删除后，“${row.name}”将不再出现在食物库列表中。`, '确认删除食物', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      customClass: 'food-delete-confirm',
      confirmButtonClass: 'food-delete-confirm-button',
      cancelButtonClass: 'food-delete-cancel-button',
      closeOnClickModal: false,
      showClose: false
    })
    await deleteFood(row.id)
    ElMessage.success('食物已删除')
    if (foods.value.length === 1 && query.pageNum > 1) {
      query.pageNum -= 1
    }
    loadFoods()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function flagText(value) {
  return value === 1 ? '是' : '否'
}

onMounted(loadFoods)

onBeforeUnmount(() => {
  if (keywordTimer) {
    clearTimeout(keywordTimer)
  }
})
</script>

<template>
  <section>
    <div class="section-heading">
      <div>
        <h1 class="page-title">食物库</h1>
        <p class="page-subtitle">
          维护常见食物的热量、蛋白质、脂肪和碳水数据，为后续饮食计划和饮食记录提供基础。
        </p>
      </div>
      <div class="heading-actions">
        <button class="ghost-action" type="button" @click="openImportDialog">导入 Excel</button>
        <button class="primary-action" type="button" @click="openCreateDialog">新增食物</button>
      </div>
    </div>

    <section class="filter-panel glass-card">
      <el-input v-model="query.keyword" placeholder="搜索食物名称" clearable @input="debounceSearch" @keyup.enter="search" />
      <el-select v-model="query.category" placeholder="全部分类" clearable @change="search">
        <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="query.mealTag" placeholder="全部餐次" clearable @change="search">
        <el-option v-for="item in mealOptions" :key="item" :label="item" :value="item" />
      </el-select>
      <el-select v-model="query.highSugar" placeholder="是否高糖" clearable @change="search">
        <el-option label="高糖" :value="true" />
        <el-option label="非高糖" :value="false" />
      </el-select>
      <el-select v-model="query.highFat" placeholder="是否高脂" clearable @change="search">
        <el-option label="高脂" :value="true" />
        <el-option label="非高脂" :value="false" />
      </el-select>
      <button class="ghost-action" type="button" @click="resetQuery">重置</button>
      <button class="search-action" type="button" @click="search">筛选</button>
    </section>

    <section class="table-card glass-card">
      <p class="data-note">数据来源：中国食物成分表；热量、蛋白质、脂肪和碳水均按每 100g 计算。</p>
      <el-table v-loading="loading" :data="foods" row-key="id" class="food-table">
        <el-table-column prop="name" label="食物名称" min-width="130" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="calories" label="热量" width="110">
          <template #default="{ row }">{{ row.calories }} kcal</template>
        </el-table-column>
        <el-table-column prop="protein" label="蛋白质" width="120">
          <template #default="{ row }">{{ row.protein }} g</template>
        </el-table-column>
        <el-table-column prop="fat" label="脂肪" width="110">
          <template #default="{ row }">{{ row.fat }} g</template>
        </el-table-column>
        <el-table-column prop="carbs" label="碳水" width="110">
          <template #default="{ row }">{{ row.carbs }} g</template>
        </el-table-column>
        <el-table-column prop="mealTags" label="餐次" min-width="120" />
        <el-table-column label="标签" width="130">
          <template #default="{ row }">
            <div class="tag-cell">
              <el-tag v-if="row.highSugar === 1" type="warning" effect="plain">高糖</el-tag>
              <el-tag v-if="row.highFat === 1" type="danger" effect="plain">高脂</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <button class="link-button" type="button" @click="openEditDialog(row)">编辑</button>
            <button class="link-button danger" type="button" @click="removeFood(row)">删除</button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <span>共 {{ total }} 条</span>
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 30, 50]"
          layout="sizes, prev, pager, next, jumper"
          @size-change="search"
          @current-change="loadFoods"
        />
      </div>
    </section>

    <el-dialog v-model="importDialogVisible" title="导入食物 Excel" width="560px" destroy-on-close>
      <template v-if="!importResult">
        <div class="import-tip">
          <strong>导入要求</strong>
          <p>仅支持 .xlsx 或 .xls 文件；必须包含“食物名称、能量、蛋白质、脂肪、碳水化合物”表头。</p>
          <p>系统会按食物名称自动更新或新增到现有食物库，分类、餐次、高糖/高脂标签和备注由规则自动生成。</p>
        </div>

        <el-upload
          class="import-upload"
          drag
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-change="handleImportFileChange"
          :on-remove="handleImportFileRemove"
        >
          <div class="upload-title">选择或拖拽 Excel 文件</div>
          <div class="upload-subtitle">数据会导入现有 t_food 表，不会创建新表或修改表结构。</div>
        </el-upload>
      </template>

      <div v-else class="import-result-panel">
        <div class="result-hero">
          <span class="result-dot"></span>
          <div>
            <p>导入完成</p>
            <strong>食物库已更新</strong>
          </div>
        </div>

        <div class="result-metrics">
          <div class="result-metric">
            <span>总行数</span>
            <strong>{{ importResult.totalRows }}</strong>
          </div>
          <div class="result-metric">
            <span>新增</span>
            <strong>{{ importResult.insertedRows }}</strong>
          </div>
          <div class="result-metric">
            <span>更新</span>
            <strong>{{ importResult.updatedRows }}</strong>
          </div>
          <div class="result-metric">
            <span>跳过</span>
            <strong>{{ importResult.skippedRows }}</strong>
          </div>
        </div>

        <div class="result-section">
          <div class="result-section-title">
            <span>分类统计</span>
            <small>用于快速检查分类是否异常</small>
          </div>
          <div class="category-stat-grid">
            <div v-for="item in formatCategoryStats(importResult.categoryStats)" :key="item.category" class="category-stat">
              <span>{{ item.category }}</span>
              <strong>{{ item.count }}</strong>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <button class="ghost-action" type="button" @click="importDialogVisible = false">
          {{ importResult ? '关闭' : '取消' }}
        </button>
        <button v-if="!importResult" class="search-action" type="button" :disabled="importing" @click="submitImport">
          {{ importing ? '导入中...' : '开始导入' }}
        </button>
        <button v-else class="search-action" type="button" @click="openImportDialog">继续导入</button>
      </template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑食物' : '新增食物'" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <div class="dialog-grid two">
          <el-form-item label="食物名称" prop="name">
            <el-input v-model="form.name" maxlength="100" clearable />
          </el-form-item>
          <el-form-item label="分类" prop="category">
            <el-select v-model="form.category" placeholder="请选择分类" filterable>
              <el-option v-for="item in categoryOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </div>

        <div class="dialog-grid four">
          <el-form-item label="热量 kcal/100g" prop="calories">
            <el-input-number v-model="form.calories" :min="0" :max="1000" :precision="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="蛋白质 g/100g" prop="protein">
            <el-input-number v-model="form.protein" :min="0" :max="100" :precision="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="脂肪 g/100g" prop="fat">
            <el-input-number v-model="form.fat" :min="0" :max="100" :precision="1" controls-position="right" />
          </el-form-item>
          <el-form-item label="碳水 g/100g" prop="carbs">
            <el-input-number v-model="form.carbs" :min="0" :max="100" :precision="1" controls-position="right" />
          </el-form-item>
        </div>

        <el-form-item label="适合餐次">
          <el-input v-model="form.mealTags" maxlength="100" placeholder="例如：早餐,午餐,加餐" clearable />
        </el-form-item>

        <div class="switch-row">
          <el-switch v-model="form.highSugar" active-text="高糖" inactive-text="非高糖" />
          <el-switch v-model="form.highFat" active-text="高脂" inactive-text="非高脂" />
        </div>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" maxlength="255" show-word-limit />
        </el-form-item>
      </el-form>

      <template #footer>
        <button class="ghost-action" type="button" @click="dialogVisible = false">取消</button>
        <button class="search-action" type="button" :disabled="saving" @click="submitForm">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.section-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.heading-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.primary-action,
.search-action,
.ghost-action {
  height: 42px;
  padding: 0 18px;
  border: 0;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 760;
}

.primary-action,
.search-action {
  color: #ffffff;
  background: linear-gradient(135deg, #34c759, #27a844);
  box-shadow: 0 10px 22px rgba(52, 199, 89, 0.2);
}

.ghost-action {
  color: var(--hd-text);
  background: #f1f4f1;
}

.filter-panel {
  display: grid;
  grid-template-columns: minmax(180px, 1.2fr) repeat(4, minmax(120px, 1fr)) auto auto;
  gap: 12px;
  margin-top: 26px;
  padding: 18px;
}

.table-card {
  margin-top: 18px;
  padding: 18px;
}

.data-note {
  margin: 0 0 12px;
  color: var(--hd-muted);
  font-size: 13px;
  line-height: 1.7;
}

.food-table {
  width: 100%;
}

:deep(.food-table .el-table__cell .cell) {
  white-space: nowrap;
}

.tag-cell {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.muted-text {
  color: var(--hd-muted);
  font-size: 13px;
}

.link-button {
  border: 0;
  color: #147a32;
  background: transparent;
  cursor: pointer;
  font-weight: 700;
}

.link-button.danger {
  color: var(--hd-danger);
}

.pagination-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 18px;
  margin-top: 18px;
  color: var(--hd-muted);
}

.dialog-grid {
  display: grid;
  gap: 14px;
}

.dialog-grid.two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.dialog-grid.four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

:deep(.el-input-number),
:deep(.el-select) {
  width: 100%;
}

:deep(.filter-panel .el-input__wrapper),
:deep(.filter-panel .el-select__wrapper) {
  height: 42px;
  min-height: 42px;
  border-radius: 10px;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 18px;
}

.import-tip {
  padding: 16px 18px;
  border-radius: 18px;
  background: #f4fbf6;
  color: var(--hd-text);
  line-height: 1.7;
}

.import-tip strong {
  display: block;
  margin-bottom: 6px;
  font-size: 16px;
}

.import-tip p {
  margin: 4px 0;
  color: var(--hd-muted);
}

.import-upload {
  margin-top: 16px;
}

.upload-title {
  color: var(--hd-text);
  font-size: 16px;
  font-weight: 760;
}

.upload-subtitle {
  margin-top: 8px;
  color: var(--hd-muted);
  font-size: 13px;
}

.import-result-panel {
  display: grid;
  gap: 16px;
}

.result-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(135deg, #ecfff3, #f7fbf8);
  box-shadow: inset 0 0 0 1px rgba(52, 199, 89, 0.12);
}

.result-dot {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  background: #34c759;
  box-shadow: 0 0 0 8px rgba(52, 199, 89, 0.12);
}

.result-hero p {
  margin: 0 0 4px;
  color: #12833a;
  font-size: 13px;
  font-weight: 760;
}

.result-hero strong {
  color: var(--hd-text);
  font-size: 22px;
}

.result-metrics,
.category-stat-grid {
  display: grid;
  gap: 10px;
}

.result-metrics {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.result-metric,
.category-stat {
  padding: 14px;
  border-radius: 18px;
  background: #f8faf8;
  box-shadow: inset 0 0 0 1px rgba(20, 40, 26, 0.05);
}

.result-metric span,
.category-stat span {
  display: block;
  color: var(--hd-muted);
  font-size: 12px;
}

.result-metric strong,
.category-stat strong {
  display: block;
  margin-top: 6px;
  color: var(--hd-text);
  font-size: 20px;
}

.result-section {
  padding: 16px;
  border-radius: 22px;
  background: #ffffff;
  box-shadow: inset 0 0 0 1px rgba(20, 40, 26, 0.06);
}

.result-section-title {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.result-section-title span {
  color: var(--hd-text);
  font-size: 16px;
  font-weight: 780;
}

.result-section-title small {
  color: var(--hd-muted);
}

.category-stat-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

:global(.food-delete-confirm.el-message-box) {
  width: min(420px, calc(100vw - 40px));
  padding: 22px;
  border: 0;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 24px 80px rgba(20, 40, 26, 0.18);
}

:global(.food-delete-confirm .el-message-box__header) {
  padding: 0;
}

:global(.food-delete-confirm .el-message-box__title) {
  color: var(--hd-text);
  font-size: 20px;
  font-weight: 820;
}

:global(.food-delete-confirm .el-message-box__content) {
  position: relative;
  margin-top: 16px;
  padding: 18px 18px 18px 52px;
  border-radius: 18px;
  background: #fff7f7;
  color: var(--hd-muted);
  line-height: 1.7;
}

:global(.food-delete-confirm .el-message-box__content::before) {
  content: "!";
  position: absolute;
  left: 18px;
  top: 20px;
  width: 22px;
  height: 22px;
  border-radius: 999px;
  color: #ffffff;
  background: #ff6b6b;
  font-size: 14px;
  font-weight: 800;
  line-height: 22px;
  text-align: center;
}

:global(.food-delete-confirm .el-message-box__container) {
  align-items: flex-start;
}

:global(.food-delete-confirm .el-message-box__message) {
  padding: 0;
}

:global(.food-delete-confirm .el-message-box__btns) {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 18px 0 0;
}

:global(.food-delete-confirm .el-button) {
  min-width: 82px;
  height: 40px;
  border-radius: 999px;
  font-weight: 760;
}

:global(.food-delete-cancel-button) {
  border: 0 !important;
  color: var(--hd-text) !important;
  background: #f1f4f1 !important;
}

:global(.food-delete-confirm-button) {
  border: 0 !important;
  color: #ffffff !important;
  background: linear-gradient(135deg, #ff6b6b, #e5484d) !important;
  box-shadow: 0 12px 26px rgba(229, 72, 77, 0.22);
}

@media (max-width: 1180px) {
  .filter-panel {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .section-heading,
  .heading-actions,
  .pagination-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .filter-panel,
  .dialog-grid.two,
  .dialog-grid.four,
  .result-metrics,
  .category-stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
