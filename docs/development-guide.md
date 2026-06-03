# 开发指南

## 1. 环境要求

| 工具 | 建议版本 | 说明 |
|---|---|---|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.8+ | 后端依赖管理、测试和打包 |
| MySQL | 8.x | 数据库存储 |
| Node.js | 18+ | 前端运行环境 |
| npm | 9+ | 前端依赖管理 |

## 2. 数据库初始化

在 MySQL 中创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS healthy_diet_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

后端启动时会自动执行脚本：

```text
backend/src/main/resources/schema.sql
```

`schema.sql` 中包含核心建表语句和 50 条常见食物初始化数据。因为 `application.yml` 已配置 `spring.sql.init.mode=always`，所以只要数据库 `healthy_diet_system` 已经存在，启动后端时会自动创建缺失的数据表，清理同名重复食物，并补齐食物库初始化数据。

如果你已经启动过旧版本项目，但食物库页面没有显示初始化食物，或者因为旧表没有唯一索引而出现同名重复数据，重启后端会自动修正。如果仍异常，可以手动打开 `schema.sql`，执行 `tmp_food_seed` 到 `DROP TEMPORARY TABLE tmp_food_seed` 这一段。

默认数据库连接：

```yaml
url: jdbc:mysql://localhost:3306/healthy_diet_system
username: root
password: root
```

如果你的 MySQL 密码不同，请修改：

```text
backend/src/main/resources/application.yml
```

## 3. 后端启动

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

后端测试：

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn test
```

## 4. 前端启动

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd run dev
```

前端默认地址：

```text
http://localhost:3000
```

> Windows 如果 `npm run dev` 被执行策略拦截，请使用 `npm.cmd run dev`。

前端构建：

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd run build
```

## 5. 食物 Excel 一次性导入

项目根目录可放置食物数据源 Excel，当前默认文件为：

```text
D:\vscode\healthy-diet-system\中国食物成分表_四项营养版.xlsx
```

Excel 必须包含以下表头：

```text
食物名称、能量、蛋白质、脂肪、碳水化合物
```

执行一次性导入命令：

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-application-type=none --app.food-import.enabled=true --app.food-import.exit-after-import=true"
```

导入规则：

- 不创建新表。
- 不修改 `t_food` 表结构。
- 同名食物存在则更新。
- 同名食物不存在则新增。
- 如果历史数据中出现同名重复，只保留第一条有效记录，其余按逻辑删除处理。
- category、meal_tags、high_sugar、high_fat、remark 由后端规则自动生成。
- 导入结果会返回分类统计 `categoryStats`，用于快速发现分类异常。

分类规则样例：

| 食物名称 | 自动分类 |
|---|---|
| 茶叶蛋 | 蛋白质 |
| 无糖豆浆 | 饮品 |
| 鸡蛋糕 | 零食 |
| 南瓜 | 蔬菜 |
| 牛肉面 | 主食 |

普通后端启动不会自动执行 Excel 导入，因为 `app.food-import.enabled` 默认是 `false`。

也可以在前端食物库页面点击“导入 Excel”按钮上传文件。前端导入和命令行导入使用同一套后端规则，都会写入现有 `t_food` 表。

## 6. Phase 3 验证步骤

1. 启动 MySQL。
2. 确认已执行 `schema.sql`。
3. 启动后端：`mvn spring-boot:run`。
4. 启动前端：`npm.cmd run dev`。
5. 打开 `http://localhost:3000`。
6. 未登录时应自动进入 `/login`。
7. 注册一个测试账号，例如：
   - 用户名：`fit_user`
   - 密码：`123456`
8. 注册成功后进入首页。
9. 点击右上角“退出”，应回到登录页。
10. 使用同一账号再次登录，应能进入首页。

## 7. Phase 7 饮食计划验证步骤

1. 启动 MySQL。
2. 启动后端：`mvn spring-boot:run`。
3. 启动前端：`npm.cmd run dev`。
4. 登录测试账号。
5. 进入“健康档案”，确认已填写身高、体重、运动频率、饮食目标、忌口和过敏信息。
6. 进入“食物库”，确认存在主食、蛋白质、蔬菜和水果等可用食物。
7. 进入“饮食计划”，点击“生成饮食计划”。
8. 页面应展示早餐、午餐、晚餐三张卡片，每项包含食物名称、推荐克数、热量、三大营养素和推荐原因。
9. 刷新页面后，系统应能通过 `/api/diet-plans/latest` 显示最近一次计划。

本阶段验证命令：

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn.cmd test
```

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd run build
```

## 8. 常见问题

### 8.1 前端提示找不到 package.json

原因：在项目根目录执行了 npm 命令。

正确做法：

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd run dev
```

### 8.2 后端启动提示数据库连接失败

请检查：

- MySQL 是否已启动。
- 是否已创建 `healthy_diet_system` 数据库。
- `application.yml` 中的用户名和密码是否正确。
- 是否执行了 `schema.sql`。

### 8.3 登录接口返回“请先登录”

正常情况下，`/api/auth/login` 和 `/api/auth/register` 不会被拦截。

如果出现该问题，优先检查前端请求地址是否为：

```text
/api/auth/login
/api/auth/register
```

### 8.4 生成饮食计划提示请先完善健康档案

原因：当前账号还没有保存健康档案，系统无法计算推荐热量。

解决方式：进入“健康档案”页面，填写并保存基础信息后再生成计划。

### 8.5 生成饮食计划提示食物库暂无可用食物

原因：食物库为空，或者所有食物都被忌口、过敏关键词排除了。

解决方式：先在“食物库”新增食物，或导入 Excel 食物数据，再检查健康档案中的忌口和过敏内容是否过宽。
