# MoodBoard × MBTI 完整项目（DeepSeek 版）

这是一个可直接运行的 **情绪日记 + AI 树洞 + MBTI 人格互动** 项目，适合作为软件测试实习生简历项目。

## 一、项目功能

- 📅 情绪日记：周日历、每日情绪 emoji、日记编辑、本周情绪图谱、长图保存
- 💬 AI 树洞：16 种 MBTI 人格切换、单人格聊天、阅后即焚
- 🎭 人格面具：基于 MBTI 创建自己的专属 AI 人格，支持自定义 Prompt
- 💌 心灵盲盒：随机人格 + 随机深度话题 + 3 轮轻对话
- ⚔️ 多人格对战：选择 2-4 个人格参与对谈，每个被选人格至少发言一次，最后生成分析结果
- 👤 个人中心：用户资料、常驻人格、我的人格面具
- 🗄️ 数据库：默认 H2，本地即可跑；也可切换 MySQL
- 🤖 AI：默认 Mock，不配置 API Key 也能跑；配置后可接 DeepSeek

## 二、技术栈

### 后端

```text
SpringBoot 3.3.5
Java 17
Spring Data JPA
H2 / MySQL
Java HttpClient 调用 DeepSeek API
```

### 前端

```text
Vue3
Vite
TypeScript
Vue Router
Axios
Tailwind CSS
html2canvas
```

---

# 三、启动项目

## 1. 启动后端

进入后端目录：

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8888
```

H2 数据库控制台：

```text
http://localhost:8888/h2-console
```

H2 默认配置：

```text
JDBC URL: jdbc:h2:file:./data/moodboard;MODE=MySQL;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE
User: sa
Password: 留空
```

## 2. 启动前端

新开一个终端，进入前端目录：

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

---

# 四、接入 DeepSeek API

项目默认是 Mock 模式，不需要 API Key 也可以跑通全部功能。

如果你要接 DeepSeek，请不要把 API Key 写进代码里，推荐用环境变量。

## Windows PowerShell

在 `backend` 目录下执行：

```powershell
$env:AI_MODE="real"
$env:DEEPSEEK_API_KEY="your_deepseek_api_key"
$env:AI_CHAT_URL="https://api.deepseek.com/chat/completions"
$env:AI_MODEL="deepseek-v4-flash"
$env:AI_THINKING_TYPE="disabled"
$env:AI_REASONING_EFFORT="high"
mvn spring-boot:run
```

## macOS / Linux

```bash
cd backend

export AI_MODE=real
export DEEPSEEK_API_KEY="你的DeepSeek API Key"
export AI_CHAT_URL="https://api.deepseek.com/chat/completions"
export AI_MODEL="deepseek-v4-flash"
export AI_THINKING_TYPE="disabled"
export AI_REASONING_EFFORT="high"

mvn spring-boot:run
```

## 模型建议

```text
deepseek-v4-flash：推荐，便宜、速度快，适合聊天、情绪陪伴、心灵盲盒。
deepseek-v4-pro：更强，适合多人格对战最终分析，但成本更高。
```

## thinking 参数

```text
AI_THINKING_TYPE=disabled  普通聊天推荐，速度更快。
AI_THINKING_TYPE=enabled   多人格分析更深入。
AI_THINKING_TYPE=omit      如果第三方兼容接口不支持 thinking 字段，就用这个。
```

对应文件：

```text
backend/src/main/resources/application.yml
backend/src/main/java/com/moodboard/service/AiService.java
```

---

# 五、核心接口

```text
POST /api/auth/register                 注册
POST /api/auth/login                    登录
GET  /api/user/profile                  获取用户资料
POST /api/user/profile                  保存用户资料
GET  /api/personas                      获取16种人格
POST /api/diaries                       保存情绪日记
GET  /api/diaries/week                  获取某周日记
GET  /api/diaries/search                检索日记
POST /api/diaries/from-battle           保存多人格对战为日记
POST /api/ai/chat/send                  单人格聊天
POST /api/custom-personas               创建人格面具
GET  /api/custom-personas               获取人格面具列表
POST /api/blind-box/draw                抽心灵盲盒
POST /api/blind-box/start               开始盲盒对话
POST /api/blind-box/reply               继续盲盒对话
POST /api/ai/persona-battle/start       开始多人格对战
GET  /api/plaza/posts                   公共广场列表
POST /api/plaza/posts                   匿名发布
```

---

# 六、多人格对战接口示例

```http
POST http://localhost:8080/api/ai/persona-battle/start
Authorization: Bearer 你的登录token
Content-Type: application/json
```

请求体：

```json
{
  "topic": "我纠结要不要换专业，但又怕自己后悔。",
  "personas": ["INTJ", "INFP", "ENFP", "ISTJ"],
  "roundsPerPersona": 1,
  "ephemeral": false
}
```

说明：

```text
personas 最少 2 个，最多 4 个。
每个被选中的人格至少说一句话。
roundsPerPersona = 1 表示每个人格说 1 句。
roundsPerPersona = 2 表示每个人格说 2 句。
ephemeral = true 表示阅后即焚，不保存对战记录。
```

---

# 七、MySQL 切换方式

修改：

```text
backend/src/main/resources/application.yml
```

把 H2 配置换成：

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/moodboard_mbti?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
```

先创建数据库：

```sql
CREATE DATABASE moodboard_mbti DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

# 八、测试建议

这个项目适合写进测试简历。你可以做：

```text
1. 功能测试：注册登录、情绪日记、AI聊天、人格面具、盲盒、多人格对战
2. 接口测试：Postman 测正常参数、空参数、非法参数、超长文本、未登录访问
3. 数据库测试：验证日记、聊天、人格面具、对战记录是否正确入库
4. 异常测试：DeepSeek API Key错误、接口超时、AI返回为空
5. 兼容性测试：Chrome、Edge、移动端浏览器
6. 自动化测试：Selenium 自动登录、创建日记、发送树洞消息
7. 性能测试：JMeter 对 /api/ai/chat/send 和 /api/ai/persona-battle/start 做基础并发测试
```

---

系统支持 Mock / API 双模式：

Mock 模式：
用于本地功能测试、接口测试和回归测试，不依赖外部 AI 服务，结果稳定。

API 模式：
用于真实接口联调测试，验证 AI 服务调用、超时处理、异常返回等场景。

# 九、简历写法

项目名称：

```text
MoodBoard × MBTI 情绪日记与 AI 树洞人格互动系统
```

技术栈：

```text
Vue3、TypeScript、Tailwind CSS、SpringBoot、JPA、H2/MySQL、Axios、DeepSeek API、Postman、JMeter、Selenium
```

项目描述：

```text
设计并实现一款轻量级情绪日记与 AI 树洞应用，包含情绪周历、AI 单人格聊天、MBTI 人格切换、自定义人格面具、心灵盲盒、多人格对战、阅后即焚和个人中心等功能。后端根据用户职业、年龄段、MBTI 人格设定和自定义 Prompt 动态拼装系统提示词，实现不同人格视角下的 AI 回复。
```

测试亮点：

```text
围绕登录注册、日记 CRUD、AI 聊天、人格面具、心灵盲盒、多人格对战、阅后即焚、接口异常、数据一致性、AI 超时等场景设计测试用例；使用 Postman 进行接口测试，使用 MySQL/H2 校验数据一致性，使用 JMeter 做基础并发测试。
```
