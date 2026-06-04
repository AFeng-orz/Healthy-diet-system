# API 设计

## 1. 统一响应格式

所有后端接口统一返回 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

鉴权失败示例：

```json
{
  "code": 401,
  "message": "请先登录",
  "data": null
}
```

## 2. 鉴权规则

- `/api/auth/register` 和 `/api/auth/login` 不需要登录。
- 其他 `/api/**` 接口默认需要登录。
- 登录后前端在请求头中携带：`Authorization: Bearer <token>`。
- 密码使用 BCrypt 加密后保存，不保存明文密码。

## 3. 当前已实现接口

### 用户认证

| 方法 | 路径 | 说明 | 是否登录 |
|---|---|---|---|
| POST | `/api/auth/register` | 用户注册 | 否 |
| POST | `/api/auth/login` | 用户登录 | 否 |
| GET | `/api/auth/me` | 当前用户 | 是 |

### 健康档案

| 方法 | 路径 | 说明 | 是否登录 |
|---|---|---|---|
| GET | `/api/profile` | 查询健康档案和指标 | 是 |
| PUT | `/api/profile` | 保存健康档案并重新计算指标 | 是 |
| GET | `/api/profile/metrics` | 查询健康指标 | 是 |

### 健康画像问卷

| 方法 | 路径 | 说明 | 是否登录 |
|---|---|---|---|
| GET | `/api/health/questions` | 获取问卷题目 | 是 |
| POST | `/api/health/assessment` | 提交问卷并生成画像 | 是 |
| GET | `/api/health/assessment/latest` | 查询最近一次画像结果 | 是 |

### 食物营养库

| 方法 | 路径 | 说明 | 是否登录 |
|---|---|---|---|
| GET | `/api/foods` | 分页查询食物 | 是 |
| POST | `/api/foods` | 新增食物 | 是 |
| POST | `/api/foods/import` | 上传 Excel 导入食物 | 是 |
| PUT | `/api/foods/{id}` | 编辑食物 | 是 |
| DELETE | `/api/foods/{id}` | 删除食物 | 是 |

#### 食物分页查询参数

| 参数 | 说明 |
|---|---|
| pageNum | 页码，默认 1 |
| pageSize | 每页条数，默认 10，最大 50 |
| keyword | 食物名称关键词 |
| category | 分类 |
| mealTag | 餐次标签 |
| highSugar | 是否高糖：true / false |
| highFat | 是否高脂：true / false |

响应中的分页对象：

```json
{
  "records": [],
  "total": 50,
  "pageNum": 1,
  "pageSize": 10
}
```

#### 食物新增/编辑请求示例

```json
{
  "name": "鸡胸肉",
  "category": "蛋白质",
  "calories": 133,
  "protein": 24.6,
  "fat": 2.8,
  "carbs": 0,
  "mealTags": "午餐,晚餐",
  "highSugar": false,
  "highFat": false,
  "remark": "低脂高蛋白"
}
```

#### 食物 Excel 导入

请求：

```text
POST /api/foods/import
Content-Type: multipart/form-data
file: Excel 文件
```

Excel 要求：

- 文件格式：`.xlsx` 或 `.xls`。
- 必须包含表头：`食物名称`、`能量`、`蛋白质`、`脂肪`、`碳水化合物`。
- 营养数据按每 100g 计算。

响应示例：

```json
{
  "totalRows": 1636,
  "insertedRows": 67,
  "updatedRows": 1569,
  "skippedRows": 0,
  "duplicateRemovedRows": 0,
  "categoryStats": {
    "主食": 320,
    "蛋白质": 260,
    "蔬菜": 180,
    "水果": 120,
    "饮品": 80,
    "其他": 40
  }
}
```

导入规则：同名食物存在则更新，不存在则新增；不创建新表，不修改 `t_food` 现有表结构。

### 饮食计划

| 方法 | 路径 | 说明 | 是否登录 |
|---|---|---|---|
| POST | `/api/diet-plans/generate` | 生成一日三餐饮食计划 | 是 |
| GET | `/api/diet-plans/latest` | 查询最近一次饮食计划 | 是 |
| GET | `/api/diet-plans/{id}` | 查询指定饮食计划详情 | 是 |

#### 饮食计划响应示例

```json
{
  "id": 1,
  "planDate": "2026-06-03",
  "targetCalories": 1800,
  "totalCalories": 1726,
  "totalProtein": 98.5,
  "totalFat": 42.3,
  "totalCarbs": 236.2,
  "summary": "根据健康档案和最近健康画像生成计划，当前目标为减脂。",
  "items": [
    {
      "id": 1,
      "foodId": 8,
      "mealType": "breakfast",
      "mealName": "早餐",
      "foodName": "燕麦片",
      "foodCategory": "主食",
      "grams": 60,
      "calories": 227,
      "protein": 7.9,
      "fat": 3.9,
      "carbs": 40.6,
      "reason": "提供稳定碳水，帮助维持饱腹感；当前目标为减脂，已按推荐热量控制份量"
    }
  ],
  "createTime": "2026-06-03T14:58:00"
}
```

生成规则：

- 需要先完善健康档案。
- 需要食物库存在可用食物。
- 计划热量基于健康档案中的推荐热量。
- 自动避开用户忌口和过敏关键词。
- 当前版本生成早餐、午餐、晚餐三餐，不包含加餐。

### 饮食记录

| 方法 | 路径 | 说明 | 是否登录 |
|---|---|---|---|
| POST | `/api/diet-records` | 新增饮食记录 | 是 |
| GET | `/api/diet-records/daily` | 查询某日饮食记录和汇总 | 是 |
| DELETE | `/api/diet-records/{id}` | 删除饮食记录 | 是 |

#### 新增饮食记录请求示例

```json
{
  "foodId": 8,
  "recordDate": "2026-06-04",
  "mealType": "breakfast",
  "grams": 120,
  "remark": "少油估算"
}
```

餐次取值：

| 值 | 说明 |
|---|---|
| breakfast | 早餐 |
| lunch | 午餐 |
| dinner | 晚餐 |
| snack | 加餐 |

#### 每日记录查询

请求：

```text
GET /api/diet-records/daily?date=2026-06-04
```

如果不传 `date`，默认查询当天。

响应示例：

```json
{
  "recordDate": "2026-06-04",
  "totalCalories": 620,
  "totalProtein": 36.5,
  "totalFat": 18.2,
  "totalCarbs": 78.4,
  "targetCalories": 1817,
  "targetProtein": 104,
  "targetFat": 50.5,
  "targetCarbs": 236.7,
  "caloriesRate": 34,
  "proteinRate": 35,
  "fatRate": 36,
  "carbsRate": 33,
  "records": []
}
```

记录规则：保存食物名称、分类和营养数据快照，避免食物库后续修改影响历史记录展示。

### 健康报告

| 方法 | 路径 | 说明 | 是否登录 |
|---|---|---|---|
| GET | `/api/reports/weekly` | 查询最近 7 天健康报告 | 是 |

响应示例：

```json
{
  "startDate": "2026-05-29",
  "endDate": "2026-06-04",
  "targetCalories": 1817,
  "todayCalories": 620,
  "todayCaloriesRate": 34,
  "averageCalories": 886,
  "recordedDays": 4,
  "summary": "今日热量摄入偏低，注意不要长期低于目标过多，优先保证蛋白质和基础能量。",
  "trends": [
    {
      "date": "2026-06-04",
      "calories": 620,
      "protein": 36.5,
      "fat": 18.2,
      "carbs": 78.4,
      "caloriesRate": 34
    }
  ]
}
```

报告规则：

- 最近 7 天范围包含当天。
- 若未保存健康档案，目标值和完成率返回空，页面提示先完善档案。
- 热量趋势直接聚合 `t_diet_record` 中每日记录。

## 4. 后续接口规划

### 健康报告扩展

| 方法 | 路径 | 说明 | 阶段 |
|---|---|---|---|
| GET | `/api/reports/monthly` | 查询月度健康报告 | 后续扩展 |
