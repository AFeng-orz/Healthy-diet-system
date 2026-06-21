# 技术规范

## 1. 技术栈

| 层次 | 技术 | 说明 |
|---|---|---|
| 后端框架 | Spring Boot 3.4.1 | Java Web 服务框架 |
| 后端语言 | Java 17 | 长期支持版本，适合企业项目 |
| 构建工具 | Maven | 依赖管理、测试、打包 |
| 持久层 | MyBatis-Plus 3.5.9 | 简化 CRUD、分页和条件查询 |
| 数据库 | MySQL 8.x | 存储用户、档案、问卷、食物、计划和记录 |
| 参数校验 | Spring Validation | 请求参数校验 |
| 鉴权 | JWT、BCrypt | 登录状态和密码加密 |
| Excel | Apache POI | 食物数据导入 |
| 前端框架 | Vue 3 | 组合式 API，适合中小型应用 |
| 构建工具 | Vite | 快速启动和打包前端项目 |
| UI 组件 | Element Plus | 表单、表格、弹窗、分页等基础组件 |
| HTTP 客户端 | Axios | 前后端请求封装 |
| 路由 | Vue Router | 前端页面导航和登录保护 |

## 2. 当前系统结构

```text
healthy-diet-system/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/healthydiet/system/
│       │   │   ├── HealthyDietSystemApplication.java
│       │   │   ├── common/
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── dto/
│       │   │   ├── entity/
│       │   │   ├── mapper/
│       │   │   └── service/
│       │   └── resources/
│       │       ├── application.yml
│       │       └── schema.sql
│       └── test/
└── frontend/
    ├── package.json
    └── src/
        ├── api/
        ├── assets/
        ├── router/
        ├── styles/
        └── views/
```

## 3. 后端分层

```text
controller -> service -> service.impl -> mapper -> entity
```

职责：

- `controller`：接口入口、参数校验、当前用户识别、调用服务。
- `service`：业务接口。
- `service.impl`：业务规则实现，例如营养计算、问卷评分、饮食计划生成和记录聚合。
- `mapper`：数据库访问。
- `entity`：数据库实体。
- `dto`：请求和响应对象，避免 Controller 直接暴露实体。
- `common`：统一响应、业务异常、全局异常处理和分页对象。
- `config`：框架配置、JWT 拦截器、MyBatis-Plus 配置、CORS 配置。

## 4. 已完成核心后端能力

- 统一响应对象 `Result<T>`。
- 业务异常 `BusinessException`。
- 全局异常处理 `GlobalExceptionHandler`。
- MyBatis-Plus 分页插件配置。
- JWT 登录拦截和当前用户上下文。
- BCrypt 密码加密。
- DTO 参数校验。
- 食物 Excel 导入。
- 健康档案营养目标计算。
- 健康画像问卷评分。
- 饮食计划规则生成。
- 每日饮食记录营养换算。
- 最近 7 天健康报告聚合。

## 5. 前端模块

| 页面 | 路径 | 说明 |
|---|---|---|
| 登录页 | `/login` | 登录、注册 |
| 首页 | `/home` | 今日摘要、目标完成度、最近趋势、常用入口 |
| 健康档案 | `/profile` | 身体基础信息、饮食目标、营养目标卡片 |
| 健康问卷 | `/questionnaire` | 生活方式问卷、健康画像和改善建议 |
| 食物库 | `/foods` | 食物分页筛选、维护、Excel 导入 |
| 饮食计划 | `/diet-plans` | 生成和查看一日三餐建议 |
| 饮食记录 | `/diet-records` | 新增、查看、删除每日摄入记录 |
| 健康报告 | `/reports` | 最近 7 天摄入趋势和总结 |

## 6. 统一响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

## 7. 业务边界

- 健康画像只做生活方式评估，不做疾病判断。
- 饮食计划只做推荐建议，不作为医疗或治疗方案。
- 营养计算使用通用公式，仅作为生活方式参考。
- 忌口、过敏和偏好在饮食计划生成时作为过滤和说明依据。

## 8. 当前阶段说明

当前核心功能已完成到 Phase 9。后续 Phase 10 重点是简历展示收尾，包括 README 完善、截图补充、测试补充、项目演示说明和可扩展方向整理。
