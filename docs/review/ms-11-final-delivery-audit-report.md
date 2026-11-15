# MS-11 项目最终交付核验报告

| 项目 | 内容 |
| --- | --- |
| 审计日期 | 2026-11-15 |
| 分支 / HEAD | `main` / `ccb52c269569466bdfcd91d4cee5a67f7678251a` |
| 工作区初始状态 | 3 份 MS-10 归档文档未提交（README/development-progress/development-roadmap） |
| 审计性质 | 只读核验 + 允许范围内的证据整理；不开发新功能 |
| MS-11 阶段状态 | **COMPLETED（审计收尾完成）** |
| 项目整体状态 | **NOT READY**（D08/AC-30；MS-09 已由 MS-12 完成） |

---

## 1. 审计范围与阅读情况

### 1.1 已阅读/核对

| 领域 | 内容 | 是否阅读 |
| --- | --- | --- |
| 项目入口 | README、development-progress、development-roadmap | 是 |
| 需求/架构 | requirements、architecture、database-design、api-design、parser-design | 是 |
| 课程要求 | course-paper-outline、论文正文/摘要/章节 | 是 |
| 后端 | Controller、Service、Mapper、Entity、DTO/VO、Parser、Servlet、异常/配置 | 是（按目录与关键源码） |
| 前端 | router、views、components、api、layout、styles | 是（按目录/核心页面） |
| 数据库 | schema.sql、实体映射、逻辑删除配置 | 是（只读） |
| 测试 | test-plan、Surefire、MS-08 测试结果、MS-10 CRUD/UI 证据 | 是 |
| 论文/交付 | MS-09 Word/PDF、截图、缺信息记录 | 是（文件核验） |
| 阶段报告 | MS-01～MS-10、MS-10-R1/R2、MS-10 关闭归档 | 是 |
| Git | 分支、HEAD、工作区、历史提交 | 是（只读） |

### 1.2 未执行/未阅读

- 未逐行阅读全部历史测试源码；按测试计划、Surefire 报告和核心测试类抽查。
- 未重新运行 160 项 Maven 测试、全量 Playwright 或性能测试；复用已有证据。
- 未连接数据库做写操作；MySQL 运行状态只做健康检查，未写数据。
- 未打开外部 GitHub；CI 状态无法验证，继续 NOT RUN。
- 未修改论文封面/个人信息（用户明确忽略）；未补 2.21 四张论文截图。

---

## 2. 项目当前版本与环境

```text
branch: main
HEAD: ccb52c2 (ms10)
工作区：README/development-progress/development-roadmap 的 MS-10 归档修改未提交
```

本轮实际 smoke check：

```text
GET /api/health       200
GET /api/health/db    200
GET http://127.0.0.1:5173/  200
```

环境：

- Windows 11 10.0.26200；
- JDK 17.0.21；
- Maven 3.9.11；
- Node 22.14.0 / npm 10.9.2；
- MySQL 8.0.43；数据库 `express_address_book`，审计时 0 行；
- Chrome 157 / Edge 157（既有证据）；
- 后端 127.0.0.1:8080、前端 127.0.0.1:5173、MySQL 127.0.0.1:3306。

**没有把 `GET /api/health` 单独当作数据库可用证据。**

---

## 3. 原始需求核验

项目名称：快递通——智能快递地址簿录入与管理系统。  
课程论文题目：**快递地址簿录入程序设计**（未改变）。  
对应课程设计任务（一）。

课程要求与项目推导设计已区分：

- 课程任务（一）核心：多行地址录入、解析、人工修正、保存、地址簿管理；
- 课程 Web 实践：2.1～2.19；
- 课程 Servlet 实践：2.20；
- 2.22 名片管理系统属于任务（二），未实现、未写入论文。

---

## 4. 功能实现核验

| 功能 | 实际实现 | 测试/证据 | 结论 |
| --- | --- | --- | --- |
| 多行输入/分行 | `ImportView.vue`、`LineSplitter` | MS04/API | COMPLETE |
| 地址解析 | Parser 包 + `/api/addresses/parse` | 160 tests、MS08 | COMPLETE |
| 有效/无效行 | `ParseResultTable`、行级 errors | MS04/API | COMPLETE |
| 原始值/当前值 | `original` 快照、`isRowEdited` | MS04/单元测试 | COMPLETE |
| 人工修改/恢复 | 行内输入、`restoreRow` | MS04 | COMPLETE |
| 保存确认 | `SaveConfirmDialog` | MS04、MS10 UI | COMPLETE |
| 批量保存 | `/api/addresses/batch`、事务 | 160 tests、MS10 | COMPLETE |
| 保存失败提示 | `save-error` | MS04 | COMPLETE |
| 防重复提交 | payload signature/前端状态 | MS04 | COMPLETE（前端级） |
| 地址列表 | `/addresses`、分页接口 | MS05/API | COMPLETE |
| 分页 | page/size/total/pages | API/UI 分页证据 | COMPLETE |
| 姓名/电话/地址搜索 | keyword 查询、LikeEscaper | MS05/测试 | COMPLETE |
| 新建 | 表单 + POST | MS05 | COMPLETE |
| 编辑 | PUT + 表单回显 | MS05/MS10 | COMPLETE |
| 单条删除 | DELETE + 二次确认 + 逻辑删除 | MS05/MS10 | COMPLETE |
| 批量删除 | 当前页多选 + batch-delete | MS05/MS10 | COMPLETE |
| Web 2.1～2.19 | `/web-basics`、basics 组件 | MS06 浏览器/截图 | COMPLETE |
| Web 2.18 线框 | `/web-basics/wireframe` | MS06 截图 | COMPLETE |
| Servlet 2.20 | 3 个 `@WebServlet` + Spring MVC 对照 | MS07/160 tests | COMPLETE |
| 登录/权限 | 未实现 | — | NOT APPLICABLE（课程范围外） |

未把“代码存在”直接等同 UI 通过；表格中的 COMPLETE 同时要求真实页面/接口证据或既有浏览器证据。

---

## 5. 验收矩阵

### 5.1 AC-01～AC-20

仓库现有 `test-plan.md` 只定义了 AC-21～AC-32；`development-progress.md` 明确记录 AC-01～AC-20 没有定义表。

**结论：AC-01～AC-20 = UNDEFINED，不猜测、不补造。**

### 5.2 AC-21～AC-32

| 编号 | 内容 | 状态 | 证据 | 遗留 |
| --- | --- | --- | --- | --- |
| AC-21 | 解析正确性 | PASS | Parser/160 tests | — |
| AC-22 | 输入边界 | PASS | Controller/HTTP | — |
| AC-23 | CRUD/分页/搜索 | PASS | MySQL 集成/MS05 | — |
| AC-24 | 批量事务/校验 | PASS | 事务测试 | — |
| AC-25 | 批量录入 E2E | PASS | MS04/MS08 | — |
| AC-26 | 地址簿 E2E | PASS | MS05/MS08 | — |
| AC-27 | Web 2.1～2.19 | PASS | MS06 | — |
| AC-28 | Servlet 2.20 | PASS | 14 个 Servlet 测试/浏览器 | — |
| AC-29 | API 契约/异常 | PASS | 160 tests/探测 | — |
| AC-30 | 安全与数据保护 | **PARTIAL** | D08 | 33060/防火墙 |
| AC-31 | 性能/兼容 | PASS | 30 样本/Edge | — |
| AC-32 | 工程/复现 | PASS（CI NOT RUN） | 160/build/scripts | CI |

D08 仍 **P1 OPEN**；AC-30 不改为 PASS。

---

## 6. 测试证据核验

- Surefire：12 个 XML，合计 **160 tests / 0 failures / 0 errors / 0 skipped**（2026-11-15 00:44 记录）；
- 当前代码自该测试后未改业务代码（HEAD 之后为文档 commit）；证据仍可参考，但报告日期/commit 需保留；
- Playwright：Chrome MS04/05/06/07、`MS08_E2E_OK`、`MS08_EDGE_OK`；
- 性能：30 样本、预热 2 次，P95 2.81ms/3.59ms；
- MS-10 实际 CRUD/UI/分页证据：`docs/test-results/ms-10/`；
- 本轮 smoke：health/db/front 200，未重跑全量。

**CI = NOT RUN；本地通过不等于 GitHub Actions 通过。**

### 6.1 CI 是否属于课程硬性验收要求（MS-11-R1 核验）

| 核验项目 | 原始要求 | 项目现状 | 判断 | 证据 |
| --- | --- | --- | --- | --- |
| GitHub Actions 使用 | 课程任务书未出现 GitHub/Actions/CI 字样（ASCII 检索 0 次） | 无 workflow 执行 | **非课程硬性要求** | `高级Web项目开发（实践）任务书.docx` |
| 远程 CI 执行记录 | 任务书未要求 | 无远端运行记录 | **非硬性** | 任务书 |
| AC-32 | 项目自定义验收项：build/160 tests/脚本/证据；CI 未运行记 P2 | 本地 160 tests/build/证据存在 | **AC-32 本地 PASS；CI 不单独否决 AC-32** | `docs/test-plan.md:316,323` |
| 本地测试能否满足 | 课程未指定必须远程 CI | 本地 Maven/Playwright 已执行 | **可作为课程验收证据** | Surefire/Playwright |
| CI 当前状态 | — | **NOT RUN / USER DEFERRED** | 工程化增强项，不是硬性阻塞 | 无 `.github/workflows` 运行证据 |

**结论：** 在现有课程原始要求和项目 AC-32 定义下，**GitHub Actions CI 不是硬性验收条件**；它是 **P2 工程化增强 / USER DEFERRED**。CI NOT RUN 的事实保留，不改成 PASS；它不单独阻止正式验收，但 D08/AC-30、MS-09 仍使项目 **NOT READY**。

---

## 7. MS-09 遗留

| 项 | 状态 |
| --- | --- |
| Markdown 正文 | EXISTS |
| 第1章 1.1～1.4 | 完整 |
| 第2章 2.1～2.21 | 完整 |
| Word | EXISTS |
| PDF | EXISTS（67 页，可打开；TOC 为可更新占位） |
| 2.1～2.20 截图 | EXISTS |
| 2.21 录入/解析/保存确认/测试记录 | EXISTS |
| 2.21 地址列表/编辑/删除/批量删除截图 | **MISSING/BLOCKED** |
| 封面/个人信息 | 用户明确忽略，USER DEFERRED |
| PDF 目录完整填充 | PARTIAL |

MS-09 的四图、图号/引用、Word 目录/PDF 已由 MS-12 补齐；封面/个人信息 USER DEFERRED。**MS-09 = COMPLETED**（当前状态以 MS-12 报告为准）。

---

## 8. D08、AC-30 与 CI

- D08：**P1 OPEN（用户决定暂不处理）**；
- AC-30：**PARTIAL**；
- MySQL X Plugin 33060 仍监听全部接口；mysqld 防火墙仍宽泛；
- 未修改 MySQL/防火墙/账号；
- 未从第二台物理设备验证远程可达性；
- GitHub Actions：**NOT RUN / USER DEFERRED**；按任务书/AC-32 属 P2 工程化增强，不是硬性验收条件。
- GitHub Actions：**NOT RUN**，无 `.github/workflows` 证据；不访问 GitHub。

---

## 9. 环境复现

- README、环境清单、复现手册存在；
- JDK17/Maven/Node 实际路径已记录；
- 后端/前端 smoke 实际 200；
- 数据库 health 200；
- `express_address_book` 当前 0 行；
- 没有修改系统环境变量或 MySQL 配置。

---

## 10. 交付材料核验

| 类别 | 状态 |
| --- | --- |
| 源码 | COMPLETE |
| DB schema/data 脚本 | COMPLETE |
| README/运行说明 | COMPLETE（MS-10 已归档） |
| 环境/复现/数据安全 | COMPLETE |
| 用户操作手册/演示脚本/Q&A | COMPLETE |
| 测试证据 | COMPLETE（本地；CI NOT RUN） |
| 2.1～2.20 截图 | COMPLETE |
| 2.21 四张业务截图 | MISSING/BLOCKED |
| 论文正文/Word/PDF | PARTIAL（TOC/四图） |
| 最终审查报告 | 本报告 |

---

## 11. 发现的问题

| 编号 | 级别 | 问题 | 证据 | 处理 |
| --- | --- | --- | --- | --- |
| MS11-P1-01 | P1 | D08 未关闭，安全验收不能 PASS | D08 台账/R2 | 用户 deferred |
| MS11-P1-02 | P1 | 项目正式验收仍缺 MS-09 完整截图/PDF 目录 | MS09 报告/文件 | **CLOSED by MS-12** |
| MS11-P2-01 | P2 | 2.21 地址列表/编辑/删除/批删截图缺失 | `docs/screenshots` | 需要真实 DB/授权 |
| MS11-P2-02 | P2 | PDF TOC 为可更新占位 | `deliverables/MS-09` | 待授权格式修复 |
| MS11-P2-03 | P2 | CI NOT RUN | 无 workflow | 用户 deferred |
| MS11-P3-01 | P3 | MS-10 自动脚本耗时 23.5s 不能等同现场演示 | `ui-demo-evidence` | 文档已注明 |
| MS11-P3-02 | P3 | `security-checks.md` 有历史 158 表述 | 文件 | 历史证据，未改 |
| MS11-P3-03 | P3 | project progress 有历史快照 | 文档 | 已加说明 |

---

## 12. 不能处理/待授权

1. D08/33060/防火墙：用户明确暂不处理；
2. MS-09 四张业务截图/PDF 目录：**MS-12 已补齐**；
3. PDF TOC/Word 格式：需论文编辑授权；
4. CI：用户 GitHub 人工上传，不执行；
5. 论文封面/个人信息：用户忽略；
6. 正式验收：需 D08/MS-09 关闭后另行评审；CI 为 P2，不是硬性条件。

---

## 13. 当前项目状态

| 项 | 状态 |
| --- | --- |
| 功能实现 | 课程核心功能完成 |
| 本地测试 | 160 通过（历史证据） |
| 浏览器 | Chrome/Edge 通过（历史证据） |
| 论文 | COMPLETED（封面 USER DEFERRED） |
| 证据 | 大部分完整，2.21 四图缺 |
| D08 | P1 OPEN |
| AC-30 | PARTIAL |
| MS-10 | COMPLETED（阶段关闭） |
| MS-11 | **COMPLETED（审计收尾）** |
| 项目整体 | **NOT READY**（D08/AC-30） |
| 正式验收 | **未通过** |

---

## 14. 最终建议

1. 不把项目标记为正式验收通过；
2. 保持 D08/AC-30 的真实状态；MS-09 已由 MS-12 完成；CI 保持 NOT RUN（P2 增强，不是硬性条件）；
3. 后续按 `ms-11-remaining-work.md` 分级处理；
4. 由用户决定 D08、CI；论文截图/格式已由 MS-12 完成；
5. 待阻塞项关闭后，再进入正式验收。

**MS-11 审计收尾完成（COMPLETED）；项目整体仍 NOT READY。**
