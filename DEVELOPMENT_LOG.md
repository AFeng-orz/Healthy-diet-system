
## 2025-02-15 顶部用户名显示修复
- 修复登录后顶部仍显示默认“用户”的问题：将 App 顶部用户信息从非响应式 localStorage 读取改为响应式 ref。
- 登录/注册成功后派发 `healthy-diet-user-updated` 事件，App 立即同步最新用户。
- App 在存在 Token 时会调用 `/api/auth/me` 补拉当前用户，避免本地用户缓存缺失导致无法显示用户名。
- 顺手恢复 App 与 Login 页中文文案为正常 UTF-8 显示。
- 验证：`npm.cmd run build` 通过。
