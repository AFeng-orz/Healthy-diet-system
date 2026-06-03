# 数据库设计

## 1. 命名规范

- 表名使用 snake_case，统一 `t_` 前缀。
- 字段名使用 snake_case。
- 主键统一为 `id BIGINT AUTO_INCREMENT`。
- 每张业务表包含 `create_time`、`update_time`、`deleted`。
- 字符集统一 `utf8mb4`，排序规则 `utf8mb4_unicode_ci`。

## 2. 数据库脚本

当前数据库脚本位置：

```text
backend/src/main/resources/schema.sql
```

## 3. 当前已落地表

| 表名 | 阶段 | 说明 |
|---|---|---|
| `t_user` | Phase 3 | 用户账号表 |
| `t_user_profile` | Phase 4 | 用户健康档案表 |
| `t_food` | Phase 6 | 食物营养库表 |
| `t_health_question` | Phase 5 | 健康问卷题目表，当前预留，题目暂以内置配置提供 |
| `t_health_assessment` | Phase 5 | 健康画像评估结果表 |
| `t_health_answer` | Phase 5 | 健康问卷答题明细表 |
| `t_diet_plan` | Phase 7 | 饮食计划主表 |
| `t_diet_plan_item` | Phase 7 | 饮食计划明细表 |

## 4. 食物营养库表 `t_food`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| name | VARCHAR(100) | 食物名称 |
| category | VARCHAR(50) | 分类 |
| calories | DECIMAL(8,2) | 每 100g 热量 kcal |
| protein | DECIMAL(8,2) | 每 100g 蛋白质 g |
| fat | DECIMAL(8,2) | 每 100g 脂肪 g |
| carbs | DECIMAL(8,2) | 每 100g 碳水 g |
| meal_tags | VARCHAR(100) | 适合餐次 |
| high_sugar | TINYINT | 是否高糖 |
| high_fat | TINYINT | 是否高脂 |
| remark | VARCHAR(255) | 备注 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

索引：

- `uk_food_name(name)`：防止初始化数据重复插入。
- `idx_food_category(category)`：分类筛选。
- `idx_food_deleted(deleted)`：逻辑删除过滤。

Phase 6 已在 `schema.sql` 中提供 50 条常见食物数据。

食物标签标准：

- `high_sugar` 用于标记明显高糖或含添加糖较多的食物/饮品，例如可乐、奶茶；苹果、香蕉、草莓等完整水果不默认标记为高糖。
- `high_fat` 用于标记脂肪含量明显较高或需要严格控制份量的食物，例如坚果、油脂、薯片、牛油果；去皮鸡腿肉、三文鱼、低脂奶酪不默认标记为高脂，只在备注中提示脂肪相对更高。
- 当前食物库作为饮食规划基础数据，数值为常见食物估算值，不作为医疗诊断或严格营养标签使用。

Excel 导入映射：

| Excel 字段 | `t_food` 字段 | 说明 |
|---|---|---|
| 食物名称 | name | 按名称作为更新/新增依据 |
| 能量 | calories | 每 100g 热量，单位 kcal |
| 蛋白质 | protein | 每 100g 蛋白质，单位 g |
| 脂肪 | fat | 每 100g 脂肪，单位 g |
| 碳水化合物 | carbs | 每 100g 碳水，单位 g |

导入时不修改表名和现有字段。`category`、`meal_tags`、`high_sugar`、`high_fat`、`remark` 由 `FoodImportRuleEngine` 根据食物名称和营养数据生成；如果营养字段为空或无法解析，导入时按 0 处理，避免遗漏食物名称。

## 5. 饮食计划表 `t_diet_plan`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| plan_date | DATE | 计划日期 |
| target_calories | DECIMAL(8,2) | 目标热量 kcal |
| total_calories | DECIMAL(8,2) | 计划总热量 kcal |
| total_protein | DECIMAL(8,2) | 计划总蛋白质 g |
| total_fat | DECIMAL(8,2) | 计划总脂肪 g |
| total_carbs | DECIMAL(8,2) | 计划总碳水 g |
| summary | VARCHAR(500) | 计划说明 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

索引：

- `idx_diet_plan_user_time(user_id, create_time)`：按用户查询最近计划。
- `idx_diet_plan_deleted(deleted)`：逻辑删除过滤。

## 6. 饮食计划明细表 `t_diet_plan_item`

| 字段 | 类型 | 说明 |
|---|---|---|
| id | BIGINT | 主键 |
| plan_id | BIGINT | 饮食计划ID |
| food_id | BIGINT | 食物ID |
| meal_type | VARCHAR(30) | 餐次：breakfast/lunch/dinner |
| food_name | VARCHAR(100) | 食物名称快照 |
| food_category | VARCHAR(50) | 食物分类快照 |
| grams | DECIMAL(8,2) | 推荐克数 |
| calories | DECIMAL(8,2) | 本项热量 kcal |
| protein | DECIMAL(8,2) | 本项蛋白质 g |
| fat | DECIMAL(8,2) | 本项脂肪 g |
| carbs | DECIMAL(8,2) | 本项碳水 g |
| reason | VARCHAR(500) | 推荐原因 |
| sort_order | INT | 排序 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| deleted | TINYINT | 逻辑删除 |

明细表保存食物快照，避免食物库后续编辑导致历史计划展示内容被动变化。

## 7. 后续阶段待补充表

- `t_weight_record`：体重记录。
- `t_diet_record`：Phase 8 每日饮食记录。
