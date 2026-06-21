# 健康生活饮食规划系统

一个前后端分离的健康生活饮食规划 Web 项目，面向希望管理体重、改善饮食结构和追踪每日摄入的用户。系统围绕“健康档案 -> 健康画像 -> 食物营养库 -> 饮食计划 -> 饮食记录 -> 健康报告”形成完整闭环。

> 说明：本系统只提供生活方式和饮食管理建议，不提供医疗诊断、治疗建议或处方推荐。

## 当前进度

当前核心功能已完成到 **Phase 9：首页仪表盘与健康报告闭环**，项目已具备较完整的简历展示价值。后续进入 **Phase 10：简历增强与项目收尾**，重点是补充截图、测试说明、README 展示细节和部署说明。

已具备能力：

- 用户注册、登录、退出和 JWT 登录状态保护
- 健康档案保存与查询
- BMI、BMR、TDEE、推荐热量和三大营养素目标计算
- 生活方式健康问卷、画像标签和改善建议
- 食物营养库分页查询、条件筛选、新增、编辑、删除
- Excel 导入食物数据，同名食物自动更新，不存在则新增
- 基于健康档案、健康画像、食物库和忌口信息生成一日三餐饮食计划
- 每日饮食记录，按 100g 营养数据自动换算实际摄入
- 今日热量、蛋白质、脂肪、碳水摄入与目标对比
- 最近 7 天健康报告和首页数据摘要
- Apple Health 风格 UI：浅色背景、圆角卡片、柔和阴影、健康绿色点缀

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Spring Boot 3、Java 17、Maven |
| 持久层 | MyBatis-Plus |
| 数据库 | MySQL 8 |
| 鉴权 | JWT、BCrypt |
| 参数校验 | Spring Validation |
| 前端 | Vue 3、Vite、Vue Router、Axios |
| UI | Element Plus、自定义 Apple Health 风格样式 |
| 数据导入 | Apache POI 读取 Excel |

## 核心模块

| 模块 | 说明 |
|---|---|
| 账号体系 | 注册、登录、当前用户查询、登录态保护 |
| 健康档案 | 记录性别、年龄、身高、体重、运动频率、饮食目标、忌口、过敏和偏好 |
| 营养计算 | 根据 Mifflin-St Jeor 公式和活动系数计算 BMI、BMR、TDEE 和营养目标 |
| 健康问卷 | 通过生活方式问卷生成非医疗诊断型健康画像 |
| 食物库 | 维护食物热量、蛋白质、脂肪、碳水、餐次、高糖和高脂标签 |
| 饮食计划 | 基于规则引擎生成早餐、午餐、晚餐建议，并避开忌口和过敏关键词 |
| 饮食记录 | 记录每日实际摄入，自动换算营养数据并与目标值对比 |
| 健康报告 | 聚合最近 7 天摄入趋势、今日完成率和总结建议 |

## 项目结构

```text
healthy-diet-system/
├── AGENTS.md
├── README.md
├── docs/
│   ├── project-workflow.md
│   ├── requirements.md
│   ├── tech-spec.md
│   ├── database-design.md
│   ├── api-design.md
│   ├── ui-design.md
│   ├── coding-standards.md
│   └── development-guide.md
├── logs/
├── backend/
│   ├── pom.xml
│   └── src/main/
└── frontend/
    ├── package.json
    └── src/
```

## 启动方式

### 1. 数据库

先在 MySQL 中创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS healthy_diet_system
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

数据库脚本位置：

```text
backend/src/main/resources/schema.sql
```

默认连接配置：

```text
backend/src/main/resources/application.yml
```

如果本地 MySQL 账号或密码不同，请修改 `username` 和 `password`。

### 2. 后端

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn.cmd spring-boot:run
```

默认地址：`http://localhost:8080`

### 3. 前端

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd install
npm.cmd run dev
```

默认地址：`http://localhost:3000`

## 食物 Excel 导入

食物库页面支持上传 `.xlsx` 或 `.xls` 文件，表头需要包含：

```text
食物名称、能量、蛋白质、脂肪、碳水化合物
```

导入规则：

- 食物名称映射到 `name`
- 能量映射到 `calories`
- 蛋白质映射到 `protein`
- 脂肪映射到 `fat`
- 碳水化合物映射到 `carbs`
- 同名食物自动更新
- 不存在的食物自动新增
- 分类、餐次、高糖、高脂和备注按系统规则自动生成

## 验证命令

后端测试：

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn.cmd test
```

前端构建：

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd run build
```

当前前端构建可能出现 Vite chunk size warning 和依赖注释提示，属于构建优化提示，不影响当前功能运行。

## 项目亮点

- 从个人健康档案到每日饮食记录形成完整业务闭环
- 规则引擎生成饮食计划，可解释、可扩展，便于面试讲解
- 支持 Excel 食物数据导入，贴近真实业务的数据维护场景
- 使用 JWT、DTO、参数校验、统一响应和全局异常处理，结构接近规范后台系统
- 前端不是传统后台风格，而是参考 Apple Health 的轻量健康产品界面
- 每日开发日志和 docs 文档同步，便于展示工程管理意识

## 后续优化方向

- 补充项目截图和演示流程
- 增加更多服务层测试和接口测试
- 优化首页图表和健康报告可视化
- 增加运动消耗记录和周期性饮食计划
- 支持家庭成员档案或小程序端
- 后续可接入 AI 生成更自然的饮食建议，但仍需保留生活方式建议边界
