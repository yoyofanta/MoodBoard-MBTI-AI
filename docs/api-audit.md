# MoodBoard 前后端 API 对照审计

审计范围：`frontend/src/api/index.ts`、`frontend/src/api/http.ts`、前端页面及组件中的 `api.*` 调用，以及 `backend/src/main/java/com/moodboard/controller` 下的 Spring MVC 映射。

统一响应约定：后端使用 `{ code, message, data? }`；前端 Axios 响应拦截器返回该响应体，因此业务代码通过 `res.data` 读取真正数据。

| 功能 | 前端调用 | 后端接口 | 最终状态 |
| --- | --- | --- | --- |
| 注册 | `POST /api/auth/register` | `POST /api/auth/register` | 已对齐，页面使用中 |
| 登录 | `POST /api/auth/login` | `POST /api/auth/login` | 已对齐，页面使用中 |
| 获取资料 | `GET /api/user/profile` | `GET /api/user/profile` | 已对齐，页面使用中 |
| 保存资料 | `POST /api/user/profile` | `POST /api/user/profile` | 已对齐，页面使用中 |
| 人格列表 | `GET /api/personas` | `GET /api/personas` | 已对齐，当前无直接 `api.personas` 调用 |
| 人格推荐 | `GET /api/persona-recommendations` | `GET /api/persona-recommendations` | 已对齐，当前无直接调用 |
| 自定义人格列表 | `GET /api/custom-personas` | `GET /api/custom-personas` | 已对齐，页面使用中 |
| 新增自定义人格 | `POST /api/custom-personas` | `POST /api/custom-personas` | 已对齐，页面使用中 |
| 修改自定义人格 | `PUT /api/custom-personas/{id}` | `PUT /api/custom-personas/{id}` | 已对齐，当前无直接调用 |
| 删除自定义人格 | `DELETE /api/custom-personas/{id}` | `DELETE /api/custom-personas/{id}` | 已对齐，当前无直接调用 |
| 新增/按日覆盖日记 | `POST /api/diaries` | `POST /api/diaries` | 已对齐，页面使用中 |
| 按日期查询日记 | `GET /api/diaries/date?date=` | `GET /api/diaries/date?date=` | 已补齐并对齐；不存在时 `data: null` |
| 按周查询日记 | `GET /api/diaries/week?startDate=&endDate=` | 同左 | 已对齐并保留 |
| 按月查询日记 | `GET /api/diaries/month?year=&month=` | 同左 | 已补齐并对齐 |
| 按年查询日记 | `GET /api/diaries/year?year=` | 同左 | 已补齐并对齐 |
| 修改日记 | `PUT /api/diaries/{id}` | `PUT /api/diaries/{id}` | 已补齐并对齐；不存在返回 404 |
| 删除日记 | `DELETE /api/diaries/{id}` | `DELETE /api/diaries/{id}` | 已对齐；不存在返回 404 |
| 搜索日记 | `GET /api/diaries/search` | `GET /api/diaries/search` | 已对齐并保留，当前页面无直接调用 |
| 对战保存为日记 | `POST /api/diaries/from-battle` | `POST /api/diaries/from-battle` | 已对齐，页面使用中 |
| 创建聊天会话 | `POST /api/ai/chat/sessions` | `POST /api/ai/chat/sessions` | 已对齐，当前页面无直接调用 |
| 发送普通聊天 | `POST /api/ai/chat/send` | `POST /api/ai/chat/send` | 已对齐，页面使用中 |
| 清空会话消息 | `DELETE /api/ai/chat/sessions/{id}/messages` | 同左 | 已对齐，页面使用中 |
| 投放漂流瓶 | `POST /api/drift-bottles/throw` | 同左 | 已对齐，页面使用中 |
| 抽取盲盒 | `POST /api/blind-box/draw` | 同左 | 已对齐，页面使用中 |
| 开始盲盒 | `POST /api/blind-box/start` | 同左 | 已对齐，页面使用中 |
| 回复盲盒 | `POST /api/blind-box/reply` | 同左 | 已对齐，页面使用中 |
| 对战会话列表 | `GET /api/persona-battle-sessions` | 同左 | 已对齐，当前页面无直接调用 |
| 新建对战会话 | `POST /api/persona-battle-sessions` | 同左 | 已对齐，当前页面无直接调用 |
| 对战会话详情 | `GET /api/persona-battle-sessions/{id}` | 同左 | 已对齐，当前页面无直接调用 |
| 新增对战消息 | `POST /api/persona-battle-sessions/{id}/messages` | 同左 | 已对齐，当前页面无直接调用 |
| 完成对战会话 | `POST /api/persona-battle-sessions/{id}/finish` | 同左 | 已对齐，当前页面无直接调用 |
| 删除对战会话 | `DELETE /api/persona-battle-sessions/{id}` | 同左 | 已对齐，当前页面无直接调用 |
| 逆地理编码 | `GET /api/location/reverse?lat=&lng=` | 同左 | 已对齐，页面使用中；无 Key 时安全降级 |
| 知识检索 | `GET /api/knowledge/search?q=&topK=` | 同左 | 已对齐，页面使用中 |
| RAG 问答 | `GET /api/knowledge/ask?q=&topK=` | 同左 | 已对齐，页面使用中 |
| 获取用户记忆 | `GET /api/memory/current` | 同左 | 已对齐，页面使用中 |
| 更新用户记忆 | `POST /api/memory/update` | 同左 | 已对齐，页面使用中 |
| 清空用户记忆 | `POST /api/memory/clear` | 同左 | 已对齐，页面使用中 |
| 记忆聊天 | `POST /api/ai/memory-chat/send` | 同左 | 已对齐，页面使用中 |
| 定位工具调用 | `POST /api/ai/tools/location/reverse` | 同左 | 已对齐，页面使用中 |
| 多 Agent 圆桌 | `POST /api/ai/agent/roundtable` | 同左 | 已对齐，页面使用中 |
| 广场列表（遗留） | 原为 `GET /api/plaza` | 实际为 `GET /api/plaza/posts` | 前端无引用，已删除无效定义；后端现有功能不变 |
| 广场发布（遗留） | 原为 `POST /api/plaza/publish` | 实际为 `POST /api/plaza/posts` | 前端无引用，已删除无效定义；后端现有功能不变 |
| 旧版人格对战（遗留） | 原为 `POST /api/persona-battle/start` | 无启用 Controller | 仅停用的 `.bak.vue` 引用，已删除无效 API 定义并将备份文件排除出构建 |

## 后端额外接口

下列后端接口没有对应的 `frontend/src/api/index.ts` 封装，但属于已存在能力，本次未删除也未虚构前端调用：

- `GET /api/ai/chat/sessions`
- `GET /api/ai/chat/sessions/{id}`
- `DELETE /api/ai/chat/sessions/{id}`
- `GET /api/plaza/posts`
- `POST /api/plaza/posts`

## 审计结论

- 当前页面实际使用的 API 均有对应后端接口。
- 日记按日、按月、按年查询和按 ID 修改的四个缺口已补齐。
- 两个广场旧路径和一个停用的人格对战旧路径均无活动页面引用，因此只清理无效前端定义，没有新建兼容假接口。
- `frontend/src/api/http.ts` 当前没有被其他模块导入，但其 Vite 环境变量类型已通过标准 `vite/client` 声明修复；主 API 客户端仍保持现有 `/api` 基础路径，未改变运行行为。
