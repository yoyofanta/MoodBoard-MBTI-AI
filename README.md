# MoodBoard

## 项目简介

MoodBoard 是一个 Vue 3 + Spring Boot 的情绪记录与 AI 对话课程项目，支持情绪日记、AI 树洞、人格对话、轻量 RAG、Memory、多 Agent 圆桌和情绪漂流瓶。

## 功能

- 注册、登录、游客演示与个人资料引导
- 情绪日记 Create / Read / Update / Delete，周/月/年视图
- 仅允许今天及过去日期记录，禁止未来日期创建和导航
- 日常树洞、人格对话、人格面具、心灵盲盒、Multi-Agent
- 情绪漂流瓶：选择情绪、匿名内容、AI 温柔回复
- RAG 知识问答、结构化 Memory 与 Prompt 注入

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Vue Router、Axios
- 后端：Java 17、Spring Boot、Spring Data JPA、H2
- AI：Mock AI / DeepSeek

## 系统架构

```text
Vue/Vite ── Axios REST ── Spring Boot ── JPA ── H2
                              ├─ Mock / DeepSeek
                              ├─ Jaccard + 标签加权 RAG
                              └─ Memory / Multi-Agent
```

## 项目目录

```text
backend/     Spring Boot 后端、实体、控制器、测试
frontend/    Vue 3 前端
tests/       pytest 接口测试
docs/        项目文档与验收材料
```

## 运行模式

### AI 运行模式与真实 DeepSeek 配置

当前提供三种真实运行方式：

| 模式 | Spring Boot | DeepSeek Key | AI | 数据 | 用途 |
| --- | --- | --- | --- | --- | --- |
| Netlify Demo | 不需要 | 不需要 | Mock | localStorage | 在线展示 |
| 本地 Mock | 需要 | 不需要 | Mock | H2 | 稳定完整演示 |
| 本地 Real | 需要 | 需要 | DeepSeek | H2 | 真实 AI 验证 |

#### Netlify Demo Mode

Netlify 当前只部署 Vue/Vite 前端，使用 `VITE_DEMO_MODE=true`，不启动 Spring Boot、不需要 DeepSeek Key，使用 Mock AI 和浏览器 localStorage，适合在线预览和答辩备用。当前版本尚未实现在线 BYOK（用户填写自己的 DeepSeek API Key）功能，也不能通过 Netlify 直接调用真实 DeepSeek。未来如需实现，必须额外部署安全后端、Serverless Function 或 API Proxy。

#### 本地完整 Mock 模式

架构为 Vue → REST API → Spring Boot → H2，设置 `VITE_DEMO_MODE=false`，不需要 DeepSeek Key，用于完整功能稳定演示。

#### 本地 Real DeepSeek 模式

架构为 Vue → REST API → Spring Boot → DeepSeek API。启动后端前设置：

```powershell
$env:DEEPSEEK_API_KEY="用户自己的 DeepSeek API Key"
$env:AI_MODE="real"
```

然后按本 README 的本地完整模式启动后端；前端设置 `$env:VITE_DEMO_MODE="false"`。浏览器地址为 `http://localhost:5173`，后端地址为 `http://localhost:8888`。

#### API Key 安全说明

`DEEPSEEK_API_KEY` 只能由 Spring Boot 后端读取。禁止写入 Vue 源码、`VITE_` 环境变量、`application.yml` 真实值、README、`.env.example`、GitHub、Netlify 或公开截图。Vite 的 `VITE_` 变量会注入浏览器可访问的前端产物，不适合保存秘密；推荐使用操作系统环境变量。

### 本地完整模式

要求：JDK 17+、Maven 3.9+、Node.js 18+、npm。

后端：

```powershell
cd backend
mvn clean package
$jar = Get-ChildItem .\target\*.jar |
  Where-Object { $_.Name -notlike "*.original" } |
  Select-Object -First 1
java -jar $jar.FullName
```

后端地址：`http://localhost:8888`；H2 控制台：`http://localhost:8888/h2-console`。

前端：

```powershell
cd frontend
npm ci
$env:VITE_DEMO_MODE="false"
npm run dev
```

前端地址：`http://localhost:5173`。Vite 开发代理将 `/api` 转发到 8888 端口。

### Real DeepSeek 模式

启动后端前设置：

```powershell
$env:DEEPSEEK_API_KEY="用户自己的 Key"
$env:AI_MODE="real"
```

API Key 只能放在后端环境变量，禁止写入源码、`VITE_` 前端变量或提交 GitHub。Real AI 依赖网络、Key 和外部服务；未设置时使用 Mock AI。

### Demo Mode

```powershell
cd frontend
npm ci
$env:VITE_DEMO_MODE="true"
npm run dev
```

Demo Mode 不依赖 Spring Boot，使用 Mock API 与浏览器 `localStorage`，用于 Netlify 和答辩备用。普通 Demo 用户的日记、Memory、Profile 和聊天数据按账号隔离；游客账号可以保留示例数据。Demo Mode 不等于正式后端部署。

## 环境变量

| 变量 | 作用 |
| --- | --- |
| `VITE_DEMO_MODE` | 前端 Demo 开关，`true` 使用 Mock |
| `VITE_API_BASE_URL` | 正式 API 地址；本地通常使用 `/api` 代理 |
| `AI_MODE` | 后端 `mock` 或 `real` |
| `DEEPSEEK_API_KEY` | 后端 DeepSeek Key，禁止放入前端 |
| `AMAP_KEY` | 可选的后端高德逆地理编码 Key |

## Netlify Demo

Netlify 只部署 Vue/Vite 前端，不部署 Spring Boot。执行 `cd frontend; npm ci; $env:VITE_DEMO_MODE="true"; npm run build`，将 `frontend/dist` 发布到 Netlify。Demo 数据保存在浏览器 `localStorage`，普通用户按账号隔离，游客可保留示例资料/日记/Memory。README 不预置未确认的 Netlify URL；完整 CRUD、正式登录和 H2 数据库仍需要可访问的 Spring Boot 后端。

## 登录与用户数据

后端使用随机 Token，并在内存中维护 Token → userId 映射；请求使用 `Authorization: Bearer <token>`。当前不是 JWT，JWT/Redis 仅属于未来优化方向。

- 默认登录和注册后的当前会话使用 `sessionStorage`
- 勾选“记住我”后才使用 `localStorage`
- 注册流程：注册 → 当前会话自动登录 → onboarding → 填写资料 → 保存 → 日记主页
- 普通新用户不会自动生成 Mooder、student、18-22、INFJ；游客 Demo 可保留示例 Profile
- Demo 业务数据使用按用户区分的 `moodboard_demo_<user>_...` key；退出登录只清理认证信息

## AI 实现

- RAG：字符集合 Jaccard 相似度 + 标签加权 + TopK，不是向量数据库。
- Memory：结构化字段持久化 + Prompt 注入。
- Multi-Agent：2～4 个角色顺序调用，后续 Agent 可以读取前序回复。
- 情绪漂流瓶：复用日记 12 种情绪，保存 `moodEmoji`、`moodLabel`、匿名正文、用户和创建时间；AI 根据情绪与正文生成回复，Demo 按用户保存。

## 测试

- 功能测试：88 条总用例，24 条已执行，23 条通过，64 条未执行，95.8%
- Bug：14；10 已关闭，3 已修复，1 待回归
- pytest：13 passed
- `npm run build`：成功
- `mvn test`：成功

## 已知限制

- H2 当前用于本地课程项目。
- Token → userId 映射当前保存在后端内存，重启后失效。
- Real AI 依赖网络、API Key 和外部服务。
- RAG 当前不是向量检索。
- Netlify 不是 Spring Boot 正式部署；Demo 使用 localStorage。
- 图片尚未形成完整后端上传服务，仅支持本地预览。

## 团队分工

- 潘子彤：组长 / 后端
- 安姣凝：前端
- 张越：AI
- 开发周期：1 个月（4 周）
