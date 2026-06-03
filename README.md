# 健康生活饮食规划系统

一个面向减脂健身人群的前后端分离 Web 项目，用于管理健康档案、健康画像问卷、食物营养数据、饮食计划和每日饮食记录。

> 说明：系统只提供健康生活方式和饮食建议，不提供医疗诊断或治疗建议。

## 当前进度

当前已完成 **Phase 6：食物营养库闭环**。

已具备能力：

- 用户注册、登录、退出和 JWT 登录状态
- 健康档案保存与查询
- BMI、BMR、TDEE、推荐热量和三大营养素目标计算
- 健康画像问卷答题、标签生成和改善建议展示
- 食物营养库分页查询、分类筛选、餐次筛选、高糖/高脂筛选
- 食物新增、编辑、删除
- `schema.sql` 提供 50 条常见食物初始化数据
- 支持通过命令行或食物库页面上传 Excel 导入食物数据库，按食物名称更新或新增到现有 `t_food`

下一阶段：**Phase 7：饮食计划生成**。

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Spring Boot 3、Java 17、Maven |
| 持久层 | MyBatis-Plus |
| 数据库 | MySQL 8 |
| 前端 | Vue 3、Vite、Vue Router、Axios |
| UI | Element Plus |
| 登录状态 | JWT |
| 密码 | BCrypt |

## 启动方式

### 后端

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn spring-boot:run
```

默认地址：`http://localhost:8080`

### 前端

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd run dev
```

默认地址：`http://localhost:3000`

## 数据库

先创建数据库并执行脚本：

```text
backend/src/main/resources/schema.sql
```

Phase 6 已在 `schema.sql` 中加入食物库初始化数据，并在后端 `application.yml` 中配置了启动时自动执行 SQL。只要先创建好 `healthy_diet_system` 数据库，后端启动时会自动创建缺失的数据表，清理同名重复食物，并补齐 50 条常见食物数据。

对于已经存在的旧数据库，`CREATE TABLE IF NOT EXISTS` 不会自动改变旧表结构；如果本地 `t_food` 已存在但初始化食物没有出现，重启后端会自动补齐缺失数据。如果仍异常，可以手动执行 `schema.sql` 中 `tmp_food_seed` 到 `DROP TEMPORARY TABLE tmp_food_seed` 这一段。

### 食物 Excel 导入

当前默认 Excel 文件：

```text
中国食物成分表_四项营养版.xlsx
```

执行一次性导入：

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.main.web-application-type=none --app.food-import.enabled=true --app.food-import.exit-after-import=true"
```

导入后数据仍写入现有 `t_food` 表，食物库查询页面无需额外修改即可使用。

默认连接配置在：

```text
backend/src/main/resources/application.yml
```

如本地 MySQL 密码不同，请修改 `username` 和 `password`。
