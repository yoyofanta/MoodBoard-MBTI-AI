# MoodBoard

## 项目简介

MoodBoard 是一个面向情绪记录和自我梳理场景的 Web 全栈课程项目。用户可以记录每日情绪、按不同时间维度查看日记，并通过 AI 树洞、MBTI 人格对话、RAG 知识问答、用户记忆和多 Agent 圆桌获得不同角度的回应。

项目默认使用 mock AI，老师拿到代码后无需第三方 Key 即可启动和演示基础流程；如需验证真实模型或高德逆地理编码，可通过环境变量启用。

## 核心功能

- 用户注册、登录、游客账号流程及个人资料
- 情绪日记新增、按日期读取、编辑、删除
- 周、月、年日记视图及情绪统计展示
- 日常树洞与 MBTI 人格聊天
- 情绪漂流瓶和心灵盲盒
- 自定义人格面具
- 基于字符 Jaccard 相似度、标签加权和 TopK 的轻量 RAG
- 数据库持久化用户 Memory，并在后续 Prompt 中注入
- 2–4 个角色顺序发言的多 Agent 圆桌与综合总结
- 浏览器定位及可选的高德逆地理编码
- 广场帖子后端 API；当前活动前端页面未接入

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、TypeScript、Vite、Vue Router、Axios、HTML/CSS |
| 后端 | Java 17、Spring Boot 3.3.5、Spring Web、Spring Data JPA |
| 数据库 | H2 文件数据库 |
| AI | Java HttpClient、DeepSeek 兼容 Chat API、内置 mock 模式 |
| 测试 | Spring Boot Test、JUnit 5、MockMvc、pytest、requests、vue-tsc |

POM 中存在 MySQL 和 Redis 相关依赖，但当前运行配置使用 H2，代码中没有启用 Redis Repository，也没有配置 MySQL 数据源。

## 系统架构

```text
Vue 3 前端
    ↓ Axios / REST API
Spring Boot Controller
    ↓
Service / Repository
    ↓
H2 数据库

Service
    ├─ mock / DeepSeek AI
    ├─ 轻量 RAG
    ├─ 用户 Memory
    └─ 高德逆地理编码（可选）
```

前端开发服务器通过 Vite 将 `/api` 代理到 `http://localhost:8888`。后端统一返回 `{ code, message, data? }`。

## 项目目录

```text
MoodBoard-Complete/
├─ backend/
│  ├─ src/main/java/com/moodboard/
│  │  ├─ controller/
│  │  ├─ service/
│  │  ├─ entity/
│  │  └─ repository/
│  ├─ src/main/resources/application.yml
│  ├─ src/test/
│  └─ pom.xml
├─ frontend/
│  ├─ src/
│  │  ├─ api/
│  │  ├─ components/
│  │  ├─ data/
│  │  └─ pages/
│  ├─ package.json
│  └─ vite.config.ts
├─ tests/
├─ docs/
├─ .env.example
└─ README.md
```

## 启动方式

### 环境要求

- JDK 17 或兼容版本
- Maven 3.9 或兼容版本
- Node.js 18 或更高版本
- npm

### 后端

```bash
cd backend
mvn spring-boot:run
```

后端地址：`http://localhost:8888`

H2 控制台：`http://localhost:8888/h2-console`

```text
JDBC URL: jdbc:h2:file:./data/moodboard;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
User Name: sa
Password: 留空
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端开发地址：`http://localhost:5173`

正式构建：

```bash
npm run build
```

## AI 模式

默认配置：

```text
AI_MODE=mock
DEEPSEEK_API_KEY=
```

mock 模式不会调用真实 DeepSeek API。启用 real 模式时，在当前终端设置环境变量：

```powershell
$env:AI_MODE="real"
$env:DEEPSEEK_API_KEY="<your-key>"
cd backend
mvn spring-boot:run
```

可选变量包括 `AI_CHAT_URL`、`AI_MODEL`、`AI_TEMPERATURE` 和 `AI_TIMEOUT_SECONDS`。不要把真实 Key 写入仓库文件。

## 高德地图

浏览器负责获取经纬度，后端可使用高德 Web 服务完成逆地理编码：

```powershell
$env:AMAP_KEY="<your-key>"
```

未配置 `AMAP_KEY` 时，接口返回安全兜底信息，页面可继续保存经纬度和日记内容，不会调用真实高德服务。

## 核心接口

完整对照见 [API 审计](docs/api-audit.md)。

```text
POST   /api/auth/register
POST   /api/auth/login
GET    /api/user/profile
POST   /api/user/profile

POST   /api/diaries
GET    /api/diaries/date
GET    /api/diaries/week
GET    /api/diaries/month
GET    /api/diaries/year
GET    /api/diaries/search
POST   /api/diaries/from-battle
PUT    /api/diaries/{id}
DELETE /api/diaries/{id}

POST   /api/ai/chat/send
POST   /api/ai/memory-chat/send
GET    /api/knowledge/search
GET    /api/knowledge/ask
GET    /api/memory/current
POST   /api/memory/update
POST   /api/memory/clear
POST   /api/ai/tools/location/reverse
POST   /api/ai/agent/roundtable
```

## 真实实现说明

### RAG

`KnowledgeBaseService` 从 H2 读取知识片段，用字符集合 Jaccard 相似度计算基础分数；查询直接命中标签时，每个标签增加 0.15 权重；随后按分数降序返回 TopK。检索结果会被拼接进 RAG Prompt。当前没有 Embedding 或向量数据库。

### Memory

`user_memory` 表保存最近情绪摘要、最近人格、最近对话摘要、上一次问题和回答。系统通过规则提取有限的情绪关键词，并在下一次 Memory Chat、RAG 或 Agent 圆桌中构造文本 Prompt。它不是通用语义记忆或向量记忆系统。

### Multi-Agent

`AgentRoundtableService` 按用户选择的 2–4 个 Agent 顺序调用 `MemoryChatService`。后一个 Agent 的输入中包含前面 Agent 的回复，最后由本地代码生成综合摘要并更新用户 Memory。活动前端将圆桌会话保存在按用户区分的 localStorage 中。

### 图片

当前日记编辑器支持选择图片并生成本地预览，但没有图片上传 API。刷新后的图片持久化不应视为已实现。

## 测试

截至 2026-09-13 的真实自动化结果：

- `npm run build`：成功，包含 vue-tsc 与 Vite production build。
- `mvn test`：成功，1 个日记生命周期集成测试通过。
- `mvn package`：成功。
- `py -m pytest tests/test_api_pytest.py -q`：`13 passed in 0.47s`。

功能测试表共有 88 条有效用例，其中 24 条有执行证据：23 条通过、1 条失败、64 条未执行；已执行通过率 95.8%。日记删除和 Multi-Agent 圆桌已完成人工回归并通过。BUG-002 仍需 real DeepSeek 验证，低优先级用例仍有未执行项。

相关材料：

- [测试材料审计](docs/test-material-audit.md)
- [测试总结](docs/04-测试总结.md)
- [最终人工验收清单](docs/final-manual-checklist.md)

## 主要特色

- Vue 与 Spring Boot 构成完整前后端 REST 架构
- 情绪日记支持日、周、月、年多个时间维度
- 默认 mock、可选 real 的 AI 双模式
- 无额外向量服务依赖的轻量 RAG
- 基于 JPA 的用户 Memory 持久化与 Prompt 注入
- 带上下文传递的顺序多 Agent 协作
- JUnit、pytest 和前端正式构建组成的可重复验证基线

## 已知限制

- 身份认证使用内存 token，密码未做生产级哈希，不适用于正式生产环境。
- 默认数据库是本地 H2。
- 图片仅支持本地预览，未实现上传持久化。
- RAG 是字符相似度与标签加权，不是语义向量检索。
- real AI 和真实高德能力依赖外部服务与用户自行提供的 Key。
- 大部分浏览器功能用例尚待人工执行。
