# 技术规范

## 1. 技术栈

| 层次 | 技术 | 说明 |
|---|---|---|
| 后端框架 | Spring Boot 3.4.1 | Java Web 服务框架 |
| 后端语言 | Java 17 | 长期支持版本，适合企业项目 |
| 构建工具 | Maven | 依赖管理、测试、打包 |
| 持久层 | MyBatis-Plus 3.5.9 | 简化 CRUD、分页、逻辑删除 |
| 数据库 | MySQL 8.x | 存储用户、档案、食物和记录 |
| 参数校验 | Spring Validation | 请求参数校验 |
| 前端框架 | Vue 3 | 组合式 API，适合中小型应用 |
| 构建工具 | Vite | 快速启动和打包前端项目 |
| UI 组件 | Element Plus | 表单、表格、弹窗等基础组件 |
| HTTP 客户端 | Axios | 前后端请求封装 |
| 路由 | Vue Router | 前端页面导航 |
| 图表 | ECharts | 后续用于趋势和营养结构图 |

## 2. 后端工程结构

当前 Phase 1 已创建后端 Maven 工程：`backend/`。

```text
backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/com/healthydiet/system/
    │   │   ├── HealthyDietSystemApplication.java
    │   │   ├── common/
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   ├── entity/
    │   │   ├── mapper/
    │   │   └── service/
    │   └── resources/
    │       ├── application.yml
    │       └── schema.sql
    └── test/
```

后端分层：

```text
controller -> service -> service.impl -> mapper -> entity
```

职责：

- `controller`：接口入口、参数校验、调用服务。
- `service`：业务接口。
- `service.impl`：业务规则实现。
- `mapper`：数据库访问。
- `entity`：数据库实体。
- `dto`：请求和响应对象。
- `common`：统一响应、异常、通用对象。
- `config`：框架配置。

## 3. Phase 1 已完成后端基础能力

- 统一响应对象 `Result<T>`。
- 业务异常 `BusinessException`。
- 全局异常处理 `GlobalExceptionHandler`。
- MyBatis-Plus 分页插件配置。
- Mapper 扫描配置。
- 自动填充创建时间、更新时间和逻辑删除默认值。
- 初始 `application.yml`。
- 初始数据库脚本 `schema.sql`。
- 基础单元测试 `ResultTests`。

## 4. 统一响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 5. 当前阶段说明

Phase 1 后端工程骨架已完成。下一阶段是 Phase 2：前端工程骨架。

## 5. Phase 2 前端落地情况

- 已创建 Vite + Vue 3 前端工程。
- 已配置 Element Plus 中文化、Vue Router 和 Axios 实例。
- 已创建登录、首页、健康档案、健康问卷、食物库、饮食计划、饮食记录、健康报告占位页面。
- 已建立 Apple Health 风格基础 CSS 变量和通用卡片样式。
