# README 全面更新报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 分支 / HEAD | `main` / `4a37f78975f8f4cda3f6131d7e774a9f3714238c` |
| 任务范围 | 仅重写 `README.md`，并新增本报告；未改业务代码/数据库/配置 |
| 结论 | README 已按当前代码、文档、测试和交付状态更新；事实缺口保留为 OPEN |

---

## 1. README 原有问题

原 README 的问题不是单点笔误，而是多个历史快照叠加：

1. 标题仍是仓库英文名，未使用“快递通——智能快递地址簿录入与管理系统”；
2. 第 1 节仍写“MS-02～MS-05”，并称 Playwright CI 尚未开发；
3. 第 9 节仍写“146 个测试”，与当前 160 测试证据不一致；
4. 第 10 节目录仍把前端称为“工程骨架”，并写“Vue 地址录入页面将在 MS-04 创建”；
5. 未反映 MS-06 的 2.1～2.19、MS-07 的 2.20、MS-08 的 160 tests/Chrome/Edge/E2E/性能；
6. 安全章节只写“33060 按需关闭”，未记录 D08 P1、AC-30 PARTIAL、防火墙 Any 规则和未验证边界；
7. 没有功能、架构、数据库字段、完整 API、文档导航和当前进度表。

本次以工作区实际文件和原始证据为准重写，不沿用旧 README 的阶段描述。

---

## 2. 本次更新内容

README 已重写为项目入口，结构包括：

1. 项目标题与课程设计定位；
2. 项目简介与当前状态；
3. 智能录入、地址簿管理、Web 基础实践、Servlet 映射功能；
4. 技术栈（实际版本）；
5. Mermaid 系统架构；
6. 真实目录树；
7. `express_address` 数据库字段与索引；
8. 10 个实际 API；
9. 环境要求；
10. 数据库准备、后端/前端启动、访问地址；
11. 160 tests、Playwright、Chrome/Edge、性能、CI 边界；
12. 当前开发进度与 AC/D08；
13. 安全注意事项；
14. 真实截图；
15. 文档导航；
16. 后续规划；
17. 课程设计许可说明。

---

## 3. 逐章节修改清单

| 章节 | 更新 |
| --- | --- |
| 标题 | 改为“快递通——智能快递地址簿录入与管理系统” |
| 2 项目简介 | 背景、问题、核心流程、课程范围 |
| 2.1 当前状态 | MS-01～MS-10、160 tests、D08/AC |
| 3 项目功能 | 3.1 录入、3.2 地址簿、3.3 Web 实践、3.4 Servlet |
| 4 技术栈 | 用 `pom.xml`、`package.json`、`package-lock.json` 实际版本 |
| 5 架构 | Mermaid 图 + Parser/事务/分层边界 |
| 6 目录 | 真实 `backend/frontend/docs/deliverables` 目录 |
| 7 数据库 | 字段、索引、Schema 边界、口令要求 |
| 8 API | 2 个健康检查 + 8 个地址接口 |
| 9 环境 | JDK/Maven/Node/MySQL/浏览器/Windows |
| 10 运行 | schema/data、PowerShell 环境变量、后端/前端命令 |
| 11 测试 | 160 tests、11 个 Playwright 脚本、性能表、CI NOT RUN |
| 12 进度 | 里程碑表 + AC-30/31/32 + D08 |
| 13 安全 | D08 事实、未验证边界、不公网部署 |
| 14 截图 | 4 张真实截图，BLOCKED 明确说明 |
| 15 文档 | 13 个实际文档/目录链接 |
| 16 规划 | MS-09 PARTIAL、MS-10 未开始 |
| 17 许可 | 课程设计用途与隐私声明 |

---

## 4. 所引用真实测试证据

### 4.1 Surefire

读取 `backend/target/surefire-reports/TEST-*.xml`：

```text
reports=12
tests=160
failures=0
errors=0
skipped=0
```

### 4.2 性能

读取 `docs/test-results/ms-08/perf-results.json`：

| 场景 | 平均 | 中位数 | P95 | 最大 | 最小 |
| --- | ---: | ---: | ---: | ---: | ---: |
| 100 行解析 HTTP | 2.29ms | 2.12ms | 2.81ms | 3.88ms | 1.81ms |
| 10000 条分页 | 3.12ms | 3.10ms | 3.59ms | 3.84ms | 2.64ms |

- 30 个有效样本；预热 2 次；
- 真实 HTTP；
- 隔离 Schema；
- 本机单机结果，不是生产承诺。

### 4.3 浏览器

`docs/test-results/ms-08/summary.md` 与浏览器记录：

- `MS04_BROWSER_OK`；
- `MS05_BROWSER_OK`；
- `MS06_2`～`MS06_9_BROWSER_OK`；
- `MS07_3_BROWSER_OK`；
- `MS08_E2E_OK`；
- `MS08_EDGE_OK`（1280/1366/980/760/390）；
- `MS08_PERF_OK`。

### 4.4 AC/D08

- AC-30：**PARTIAL**；
- AC-31：**PASS**；
- AC-32：**PASS（本地验证；CI NOT RUN）**；
- D08：**P1 OPEN**。

README 未把 AC-30 写成 PASS，也未宣称 MS-08 正式结束。

---

## 5. 图片和链接检查

README 相对链接/图片共 23 个，使用脚本逐一解析：

```text
links=23
bad=0
```

实际检查的图片：

- `docs/screenshots/2-21-import.png`；
- `docs/screenshots/2-21-parse-result.png`；
- `docs/screenshots/2-18-implemented.png`；
- `docs/screenshots/2-20-servlet-mapping.png`。

未展示不存在的 2.21 列表/编辑/删除/批删截图；在 README 中明确标记 BLOCKED。

---

## 6. 版本信息核对

| 技术 | README 版本 | 核对来源 |
| --- | --- | --- |
| Java | 17 / Temurin 17.0.21 | `pom.xml` + 本机运行 |
| Spring Boot | 3.3.5 | `pom.xml` |
| MyBatis-Plus | 3.5.7 | `pom.xml` |
| MySQL | 8.0.43 | 本机运行/文档 |
| Vue | 3.5.45 | `package-lock.json`（`package.json` 为 `^3.5.13`） |
| TypeScript | 5.7.3 | 锁文件（`~5.7.2`） |
| Vite | 5.4.21 | 锁文件（`^5.4.11`） |
| Element Plus | 2.14.8 | 锁文件（`^2.9.1`） |
| Axios | 1.21.1 | 锁文件（`^1.7.9`） |
| Vue Router | 4.6.5 | 锁文件（`^4.5.0`） |
| Sass | 1.108.2 | 锁文件（`^1.83.0`） |
| Playwright | 1.65.1 | 锁文件 |
| Node / npm | 22.14.0 / 10.9.2 | 本机验证 |

README 同时注明 `package.json` 范围与锁文件实际安装值的区别，避免把范围写成精确版本。

---

## 7. D08/AC 状态核对

| 项 | README | 证据 |
| --- | --- | --- |
| D08 | P1 OPEN | `ms-08-defect-ledger.md`、R2 报告 |
| AC-30 | PARTIAL | X Plugin/防火墙风险 |
| AC-31 | PASS | 性能 + Chrome + Edge |
| AC-32 | PASS（CI NOT RUN） | 本地构建/测试可复现 |
| MS-08 正式结束 | 否 | D08 未关闭 |
| MS-09 | PARTIAL | 论文/PDF 已有，2.21 四图 BLOCKED |
| MS-10 | 未开始 | 路线图 |

---

## 8. 未解决问题

1. **D08 P1 OPEN**：MySQL X Plugin 33060 全接口监听 + mysqld 宽泛防火墙规则；未修改 MySQL/防火墙；
2. 未从第二台物理设备验证远程可达性/登录；
3. 没有登录/权限，不能公网部署；
4. GitHub Actions CI **NOT RUN**；
5. MS-09 封面信息待填，2.21 地址列表/编辑/删除/批删四图 **BLOCKED**，PDF 目录仍为可更新域占位；
6. `docs/test-results/ms-08/security-checks.md` 有一处“158 后端测试”旧表述（与 160 证据不一致）；本任务未改该文档，已在报告中记录；
7. README 中的 160 测试需在已存在的隔离 Schema 上重跑，不能把无数据库时的跳过结果当成 160。

---

## 9. Git 工作区

```text
branch: main
HEAD: 4a37f78975f8f4cda3f6131d7e774a9f3714238c
```

本任务只修改：

```text
M README.md
?? docs/review/readme-update-report.md
```

工作区还保留用户此前 MS-09 的未提交文件（论文、交付物、截图、状态文档），本任务未覆盖或清理。  
未执行 commit/push/reset/clean/checkout/restore/stash。

`git diff --check` 通过。

---

## 10. 最终结论

README 已从 MS-05 阶段的历史说明更新为当前项目入口，功能、技术栈、架构、数据库、API、运行、测试、进度和安全状态均与工作区实际文件/证据一致。

**README 更新完成；MS-08 未正式结束、MS-09 为 PARTIAL、MS-10 未开始、D08 仍 P1 OPEN。**  
等待 ChatGPT 独立审查；不 commit、不 push、不开始其他里程碑。
