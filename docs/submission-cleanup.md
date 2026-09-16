# MoodBoard 最终提交目录清理审计

审计只分类，不执行删除。运行时数据库和用户数据未被改动。

## 必须保留

| 路径 | 用途 |
| --- | --- |
| `backend/src/main` | Spring Boot 业务代码与配置 |
| `backend/src/test` | JUnit/MockMvc 集成测试 |
| `backend/pom.xml` | Maven 依赖与构建配置 |
| `frontend/src` | Vue/TypeScript 源码 |
| `frontend/src/data/personas.ts` | 16 种人格前端数据，被多个活动组件直接导入 |
| `frontend/package.json`、`package-lock.json` | 前端依赖和锁定版本 |
| `frontend/vite.config.ts`、`tsconfig.json` | 开发代理与 TypeScript 配置 |
| `frontend/index.html`、PostCSS/Tailwind 配置 | 前端构建入口与样式工具配置 |
| `tests/api_test.py`、`tests/test_api_pytest.py` | API 自动化测试 |
| `tests/reports/api_report.html` | 已有历史 pytest HTML 证据，可保留 |
| `docs` | 测试材料、API 审计、技术说明、验收清单和截图 |
| `README.md`、`.gitignore`、`.env.example` | 项目说明、安全忽略规则和变量示例 |

原 `.gitignore` 的 `data/` 规则会误忽略 `frontend/src/data/personas.ts`。本轮已改成仅匹配根目录的 `/data/`，使必要源码能够进入最终版本。

## 不应提交

| 路径/模式 | 当前发现 | 原因 |
| --- | ---: | --- |
| `frontend/node_modules/` | 4,356 个文件，约 83.48 MiB | 可由 `npm install` 还原 |
| `backend/target/` | 85 个文件，约 58.19 MiB | Maven 编译和打包产物 |
| `frontend/dist/` | 9 个文件，约 0.27 MiB | Vite 构建产物 |
| `.pytest_cache/` | 4 个文件 | pytest 缓存 |
| `tests/__pycache__/`、`*.pyc` | 2 个文件，约 0.02 MiB | Python 字节码缓存 |
| `.vscode/` | 1 个本地设置文件 | IDE 本地配置 |
| `.github/modernize/`、`backend/.github/modernize/` | 本地升级工具 hook 和日志 | 与课程项目运行无关 |
| `*.log`、`*.swp`、临时 Office 文件 | 已发现升级日志及 `.git` 内 swap 文件 | 本地过程文件 |
| `*.bak`、`*.bak.vue` | 后端停用 Controller 备份；前端停用 Vue 备份 | 不参与当前运行或构建，应在最终清理时经确认移出提交包 |
| `.git/` | Git 元数据，约 3.47 MiB | 压缩包提交一般不需要 |
| `.codex-tmp/`、`*.inspect.ndjson` | 本轮审计过程文件 | 仅用于本轮工作，最终包不需要 |
| `.env`、`.env.*`（除 `.env.example`） | 当前未发现需提交的真实环境文件 | 可能包含私密变量 |
| `start-backend-deepseek.ps1` | 已改为环境变量读取，但被 Git 忽略 | 本地便捷脚本，不是必要启动方式 |

## 需要人工判断

| 路径 | 当前情况 | 建议 |
| --- | --- | --- |
| `backend/data/moodboard.mv.db` | 约 2.18 MiB，包含本地 H2 业务/演示数据 | 不自动删除。若老师需要预置演示数据可单独备份并说明；否则提交包中排除，由应用首次启动创建新库 |
| `backend/uploads/`、`uploads/` | 运行时上传目录，当前由 `.gitignore` 排除 | 若存在必须展示的素材，先确认隐私与来源；否则不提交 |
| `docs/07-AI应用专项测试.md` | 本任务开始前已处于删除状态 | 由用户决定是否从 Git 历史恢复；本轮未恢复 |
| `tests/reports/api_report.html` | 历史报告与本轮结果同为 13 passed，但时间较早 | 可保留为历史证据；如需最新 HTML，应在最终清理前重新生成并注明环境 |

## 最终清理建议顺序

1. 先备份并决定是否携带 H2 演示数据。
2. 确认是否恢复 `docs/07-AI应用专项测试.md`。
3. 删除依赖、构建、缓存、升级工具和备份文件。
4. 重新执行 `npm install && npm run build`、`mvn test` 和 pytest。
5. 对最终目录再做一次 Key 扫描，然后制作提交包。
