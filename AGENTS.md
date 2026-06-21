# 健康生活饮食规划系统

## 项目简介

本系统是一个前后端分离的健康生活饮食规划系统，面向减脂健身和日常健康饮食管理场景，提供账号体系、健康档案、健康画像问卷、食物营养库、饮食计划生成、每日饮食记录、首页仪表盘和健康报告等能力。

系统定位为生活方式管理工具，只提供健康生活和饮食建议，不提供医疗诊断、治疗建议或处方推荐。

## 当前阶段

> **阶段：核心功能已完成到 Phase 9，进入 Phase 10 项目收尾。**
>
> - ✅ 独立项目目录与文档体系
> - ✅ Spring Boot 后端工程骨架
> - ✅ Vue 3 + Vite 前端工程骨架
> - ✅ 用户注册、登录、JWT 登录状态
> - ✅ 健康档案保存与营养目标计算
> - ✅ 健康画像问卷题目获取、提交评估、最近结果查询
> - ✅ 食物库分页、筛选、新增、编辑、删除
> - ✅ 食物 Excel 导入和同名更新
> - ✅ 饮食计划规则生成、最近计划查询和忌口过滤
> - ✅ 每日饮食记录、摄入换算和目标对比
> - ✅ 健康报告和首页数据摘要
> - ✅ Apple Health 风格页面优化

## 项目文档索引

| 文档 | 路径 | 说明 |
|---|---|---|
| README | [README.md](README.md) | 项目展示说明
| 工作说明 | [docs/project-workflow.md](docs/project-workflow.md) | 阶段迭代规则、日志规则、验收标准 |
| 需求文档 | [docs/requirements.md](docs/requirements.md) | 用户、功能、业务边界和核心规则 |
| 技术规范 | [docs/tech-spec.md](docs/tech-spec.md) | 技术栈、架构分层、模块规划 |
| 数据库设计 | [docs/database-design.md](docs/database-design.md) | 表结构、字段说明、关系设计 |
| API 设计 | [docs/api-design.md](docs/api-design.md) | 接口规划、统一响应、鉴权规则 |
| UI 设计 | [docs/ui-design.md](docs/ui-design.md) | Apple Health 风格视觉规范 |
| 编码规范 | [docs/coding-standards.md](docs/coding-standards.md) | 前后端、数据库、日志规范 |
| 开发指南 | [docs/development-guide.md](docs/development-guide.md) | 环境要求、启动方式、验证命令 |
| 数据库脚本 | [backend/src/main/resources/schema.sql](backend/src/main/resources/schema.sql) | 当前数据库初始化脚本 |
| 开发日志 | [logs/](logs/) | 每日开发记录、问题和待办 |

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3.x |
| 后端语言 | Java 17 |
| 构建工具 | Maven |
| 持久层 | MyBatis-Plus |
| 数据库 | MySQL 8.x |
| 鉴权 | JWT、BCrypt |
| 参数校验 | Spring Validation |
| 前端框架 | Vue 3 + Vite |
| 路由 | Vue Router |
| HTTP | Axios |
| UI | Element Plus + 自定义 Apple Health 风格样式 |
| Excel | Apache POI |

## 核心开发原则

1. 不一次性完成所有功能。
2. 严格按照阶段小步迭代。
3. 每次只完成一个可验证闭环。
4. 每个阶段完成后必须更新 docs/ 和 logs/。
5. 每个阶段都要运行对应验证命令。
6. 健康建议相关文案必须避免医疗诊断表达。
7. 页面风格参考 Apple 官网 / Apple Health：简洁、高级、留白充足、圆角卡片、柔和阴影、浅色背景、健康绿色点缀。
8. 不随意修改非本日日志；如需修复历史日志，必须先确认。

## 快速启动

后端：

```powershell
cd D:\vscode\healthy-diet-system\backend
mvn.cmd spring-boot:run
```

前端：

```powershell
cd D:\vscode\healthy-diet-system\frontend
npm.cmd run dev
```
