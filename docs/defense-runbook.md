# MoodBoard 答辩启动说明

## A. 正常答辩

后端（项目根目录的 `backend`）：

```bash
java -jar target/moodboard-mbti-backend-1.0.0.jar --server.port=8888
```

前端（项目根目录的 `frontend`）：

```bash
npm run dev
```

浏览器打开 `http://localhost:5173`。本地完整版本默认使用 Spring Boot + H2；未配置 real AI 时使用后端 mock 模式。

## B. 后端启动失败

进入 `backend` 目录，使用已经打好的 JAR 启动：

```bash
java -jar target/moodboard-mbti-backend-1.0.0.jar --server.port=8888
```

## C. 本地项目完全失败

打开 Netlify 备用链接。该链接部署的是前端 `VITE_DEMO_MODE=true` 演示版本，数据保存在浏览器 localStorage，仅用于答辩展示，不代表真实后端数据。

## D. 学校网络断网

继续使用本地 Mock 完整版。Spring Boot 后端的 mock AI 不依赖外部 DeepSeek；real DeepSeek 和高德服务仍需要网络及相应 Key。

## Netlify 部署提示

持续部署时连接 GitHub 仓库，Base directory 使用 `frontend`，Build command 使用 `npm ci && npm run build`，Publish directory 使用 `dist`，并设置环境变量 `VITE_DEMO_MODE=true`。手工部署时运行 `cd frontend && npm ci && npm run build`，将 `frontend/dist` 上传为站点目录。

Netlify 只托管静态前端，不托管现有 Spring Boot 后端；正式 CRUD、账号、H2 和 real AI 仍依赖可访问的 Spring Boot 后端。
