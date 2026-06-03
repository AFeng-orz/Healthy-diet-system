# 编码规范

## 1. 后端规范

- Controller 只负责接口入口、参数校验和调用 Service。
- 业务逻辑写在 Service 层。
- 所有接口统一返回 Result<T>。
- 请求参数使用 DTO，不直接接收 Entity。
- 使用构造器注入，避免字段注入。
- 使用 MyBatis-Plus LambdaQueryWrapper，避免硬编码字段名。
- 删除操作默认使用逻辑删除。
- 业务异常使用统一异常类型，返回清晰中文提示。

## 2. 前端规范

- Vue 组件使用 `<script setup>`。
- API 请求统一放在 `src/api/`。
- 页面组件放在 `src/views/`。
- 可复用组件放在 `src/components/`。
- 全局样式和主题变量放在 `src/styles/`。
- UI 使用 Element Plus，但视觉风格按 Apple Health 方向定制。

## 3. 数据库规范

- 表名使用 t_ 前缀。
- 字段使用 snake_case。
- Java 字段使用 camelCase。
- 必备字段：id、create_time、update_time、deleted。
- 热量、营养素和体重使用 DECIMAL，避免浮点误差。

## 4. 日志规范

- 每个阶段结束更新 logs/YYYY-MM-DD.md。
- 记录已完成事项、问题与解决、验收结果和待办事项。
