# 开发进度（development-progress.md）

> 审计时间：2026-11-14 17:03（Asia/Hong_Kong）
> 审计对象：`Intelligent-Express-Management-System` 当前工作区
> 审计方式：读取源码/测试/文档/Git + 复核已有报告。**本次审计没有修改业务代码、数据库结构或业务数据；没有重新执行 Maven 测试/Playwright。**
> 所有“测试通过”均来自已有报告或源码中的测试定义；本文件的“未验证”不代表失败。

> **MS-07.5 状态更新（2026-11-14）**：本文件 §1～§8 保留为 MS-05 时期审计快照；当前状态已变化，以下 §0 为准。历史 NOT_STARTED 不再代表当前事实。

---

## 0. 当前状态（MS-07.5，2026-11-14）

| 里程碑 | 当前状态 | 证据 |
| --- | --- | --- |
| MS-06 Web 实践页 | TESTED | 2.1～2.19；MS06.2～MS06.9 浏览器回归；`2-01`～`2-19` 截图 |
| MS-07 Servlet 演示 | TESTED | 3 个 `@WebServlet` + Spring MVC 对照；`/servlet-demo`；TC-SERVLET-01～013；160 后端测试；`2-20-*` 截图 |
| MS-08 全量测试 | PARTIAL（P1 D08） | 160 后端测试、MS04/05/06/07 回归、E2E、性能/Edge 通过；MySQL X 33060 暴露待用户人工处置；AC-30 PARTIAL |
| MS-09 论文/截图 | PARTIAL | 正文/Word/PDF 已生成；封面信息与 2.21 四个业务截图 BLOCKED |
| MS-10 验收/答辩 | PARTIAL | 验收/Q&A/演示材料完成；实际 DB 演示 BLOCKED |

当前实现事实：

```text
前端：/servlet-demo 可用，导航 active；真实 fetch /demo/**
后端：/demo/servlet/exact、/demo/servlet/*、*.smap、/demo/spring/*
测试：TC-SERVLET-01～013 PASS；Maven 160 PASS；MS04/MS05/MS06 回归 PASS
截图：docs/screenshots/2-20-servlet-mapping*.png
```

历史审计中 `MS-06 NOT_STARTED`、`MS-07 NOT_STARTED`、`/servlet-demo 不存在` 等表述不再适用于当前工作区；保留原文是为了保留审计历史。

---

## 1. 当前开发位置（历史快照）

```text
MS-00 设计基线        ACCEPTED（设计文档冻结）
MS-00B P0 修订        ACCEPTED
MS-00C 第二轮修复      ACCEPTED
MS-01 工程骨架        TESTED
MS-02 解析器          TESTED（含 MS-02.1/02.2）
MS-03 后端 CRUD       TESTED（含 MS-03.1）
MS-04 批量录入前端     TESTED
MS-05 地址簿前端       TESTED，但代码未提交 Git；没有正式验收
MS-06 Web 实践页       NOT_STARTED
MS-07 Servlet 演示     NOT_STARTED
MS-08 全量测试         PARTIAL（P1 D08；160 后端测试/Playwright/E2E/性能已执行）
MS-09 论文/截图        PARTIAL（正文/Word/PDF；封面信息与 2.21 四图 BLOCKED）
MS-10 验收/答辩        PARTIAL（材料完成；实际 DB 演示 BLOCKED）
```

**准确位置**：MS-05 代码与浏览器脚本已完成并有报告，但 **Git HEAD 仍停在 MS-04**，MS-05 文件未提交；因此不能称为“已完成验收”。

---

## 2. 里程碑状态总表

| 里程碑 | 状态 | 代码实现 | 测试证据 | 正式验收 | 结论 |
| --- | --- | --- | --- | --- | --- |
| MS-00 设计基线 | ACCEPTED | 8 份文档 | 文档交叉引用检查（历史报告） | 文档冻结 | 稳定 |
| MS-00B P0 修订 | ACCEPTED | 文档修订 | `p0-finalization-report.md` | 报告关闭 | 稳定 |
| MS-00C 第二轮修复 | ACCEPTED | 文档修订 | `p0-finalization-report.md` | 报告关闭 | 稳定 |
| MS-01 工程骨架 | TESTED | 后端/前端/DB 脚本/健康检查 | `ms-01-initialization-report.md` | 无正式签字 | 可用 |
| MS-02 Parser | TESTED | parser 包、86 个 Parser 测试、8 个真实 Parser/HTTP 测试 | `ms-02-final-report.md` | 报告为条件准入 | 可用 |
| MS-03 后端 CRUD | TESTED | 8 个 API、Service/Mapper/Entity | 146 个后端测试报告（含 16 MySQL） | `ms-03-final-review.md` 条件通过 | 可用 |
| MS-03.1 契约修复 | TESTED | 分页 Number、ID String、keyword/page/size、电话/姓名 | `ms-03-remediation-report.md` | 条件通过 | 可用 |
| MS-04 批量录入前端 | TESTED | `ImportView` + 4 个 import 组件 + API/类型 | `npm build` + `MS04_BROWSER_OK` 报告 | 条件通过 | 可用 |
| MS-05 地址簿前端 | **TESTED** | `AddressListView` + 4 个 address 组件 | `npm build` + `MS05_BROWSER_OK` 报告 | **无独立验收** | 代码在磁盘，未提交 |
| MS-06 Web 实践页 | NOT_STARTED | 无 `WebBasicsView`/`WireframeView` | 无 | 无 | 未开始 |
| MS-07 Servlet 演示 | NOT_STARTED | 无 `servlet/`、`demo/` 文件 | 无 | 无 | 未开始 |
| MS-08 全量测试 | PARTIAL | 有分散测试/脚本 | 无 R1～R5 汇总、无 CI | 无 | 部分 |
| MS-09 论文/截图 | PARTIAL | 正文、Word/PDF、真实截图 | 2.21 列表/编辑/删除/批删 BLOCKED；封面待补 | 无 | 初稿已生成 |
| MS-10 验收/答辩 | PARTIAL | 验收矩阵、复现、演示脚本、46 题 Q&A、故障/证据 | 实际 DB 演示 BLOCKED；D08 P1 | 部分 | 材料完成 |

**状态定义**：IMPLEMENTED = 代码存在；TESTED = 有测试/构建/浏览器执行证据；ACCEPTED = 明确正式验收；PARTIAL = 部分；NOT_STARTED = 未开始；BLOCKED = 有阻塞证据。

---

## 3. 已实现功能（代码定位）

### 3.1 后端

| 功能 | 代码 |
| --- | --- |
| 解析 API | `POST /api/addresses/parse` → `AddressController.parse` → `ParseInputGuard` → `AddressParserService` |
| 列表/分页/搜索 | `AddressController.list` → `AddressService.page` → `ExpressAddressMapper.selectPageByKeyword` + XML |
| 详情 | `AddressController.get` → `AddressService.getById` |
| 新建 | `POST` → `AddressService.create` |
| 编辑 | `PUT /{id}` → `AddressService.update`（只 set 三字段） |
| 单删 | `DELETE /{id}` → `AddressService.delete`（`@TableLogic`） |
| 批量保存 | `POST /batch` → `AddressService.batchSave` `@Transactional` |
| 批量删除 | `POST /batch-delete` → `AddressService.batchDelete` |
| 统一响应/异常 | `R`, `ResultCode`, `Violation`, `GlobalExceptionHandler` |
| LIKE 转义 | `LikeEscaper` + `ESCAPE '!'` XML |
| 姓名码点 | `CodePointLength` + `CodePointLengthValidator` + Parser `codePointCount` |
| JSON ID/分页 | `JacksonConfig`（只 Long 包装类转字符串；PageResult long 保持数字） |
| 健康检查 | `HealthController` / `DbHealthController` |

关键文件：

- [`AddressController.java`](../backend/src/main/java/com/express/addressbook/controller/AddressController.java)
- [`AddressService.java`](../backend/src/main/java/com/express/addressbook/service/AddressService.java)
- [`ExpressAddressMapper.xml`](../backend/src/main/resources/mapper/ExpressAddressMapper.xml)
- [`GlobalExceptionHandler.java`](../backend/src/main/java/com/express/addressbook/common/GlobalExceptionHandler.java)
- [`AddressSaveDTO.java`](../backend/src/main/java/com/express/addressbook/dto/AddressSaveDTO.java)

### 3.2 前端

| 页面/能力 | 代码 |
| --- | --- |
| 批量录入 | `src/views/ImportView.vue` |
| 输入面板 | `components/import/AddressInputPanel.vue` |
| 预览表 | `components/import/ParseResultTable.vue` |
| 统计卡 | `components/import/ParseStatistics.vue` |
| 保存确认 | `components/import/SaveConfirmDialog.vue` |
| 地址列表 | `src/views/AddressListView.vue` |
| 搜索栏 | `components/address/AddressSearchBar.vue` |
| 地址表格 | `components/address/AddressTable.vue` |
| 新建/编辑 | `components/address/AddressFormDialog.vue` |
| 批量删除 | `components/address/BatchDeleteDialog.vue` |
| API | `src/api/addresses.ts`, `src/api/request.ts` |
| 类型 | `src/types/address.ts`, `src/types/api.ts` |
| 姓名/校验/投影 | `utils/textLength.ts`, `utils/addressValidation.ts`, `utils/addressPayload.ts` |

### 3.3 当前路由

- `/` → `/import`；
- `/import` → `ImportView`；
- `/addresses` → `AddressListView`（MS-05，未提交）；
- 404 → `NotFoundView`；
- **没有** `/web-basics`、`/web-basics/wireframe`、`/servlet-demo`。

### 3.4 数据库

- 表结构脚本：[`schema.sql`](../backend/src/main/resources/db/schema.sql)；
- 演示数据：[`data.sql`](../backend/src/main/resources/db/data.sql)；
- 可选账号：[`admin-init.sql`](../backend/src/main/resources/db/admin-init.sql)；
- 实际表：`express_address`，单表，逻辑删除，`utf8mb4`；
- **没有迁移框架**（没有 Flyway/Liquibase）；只有初始化脚本；
- `database-design.md` 引用的 `docs/sql/clean.sql` **不存在**（文档/实现不一致）。

---

## 4. 已存在但未完成原始范围的功能

以下是原始计划要求、当前**没有实现**的功能：

| 功能 | 原始里程碑 | 当前事实 |
| --- | --- | --- |
| `/web-basics` 2.1～2.19 | MS-06 | 只有架构/需求设计，没有视图/组件 |
| `/web-basics/wireframe` 2.18 | MS-06 | 没有视图/截图 |
| `/servlet-demo` 2.20 | MS-07 | 没有 servlet/demo 代码 |
| `/demo/**` 映射矩阵 | MS-07 | 不存在 |
| `docs/screenshots/` | MS-06/09 | 目录不存在 |
| 论文正文 | MS-09 | 只有 `course-paper-outline.md` |
| 答辩脚本/Q&A | MS-10 | 不存在 |
| 全量 R1～R5 测试记录 | MS-08 | 分散测试/报告，无汇总记录 |
| Playwright CI | MS-08 | 只有独立脚本 |
| AC-01～AC-32 | MS-10 | **无定义表** |

---

## 5. 测试证据（现有，不重新执行）

### 5.1 后端测试定义

从源码 `@Test` 计数：

| 测试类 | @Test 数 |
| --- | --- |
| `AddressParserServiceTest` | 86 |
| `AddressControllerTest` | 12 |
| `AddressParseIntegrationTest` | 8 |
| `AddressApiMysqlTest` | 16 |
| `AddressSaveDTOTest` | 10 |
| `LikeEscaperTest` | 4 |
| `CodePointLengthTest` | 5 |
| `GlobalExceptionHandlerTest` | 1 |
| `RTest` | 2 |
| `HealthControllerTest` | 1 |
| `DbHealthControllerTest` | 1 |
| **合计** | **146** |

已有报告声称：

```text
mvn -B clean test
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

证据来源：

- [`ms-02-final-report.md`](review/ms-02-final-report.md)：116；
- [`ms-03-remediation-report.md`](review/ms-03-remediation-report.md)：146；
- [`test-plan.md`](test-plan.md)：146 基线。

**审计限制**：本次没有重新运行 Maven；`backend/target` 中没有 surefire 报告，只有构建产物。146 是源码测试定义 + 已有报告，不是本次新执行结果。

### 5.2 真实 MySQL 测试

`AddressApiMysqlTest`：

- `@SpringBootTest` + `@ActiveProfiles("test")`；
- `@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")`；
- 测试库 `express_address_book_test`；
- 测试前后清理 `199` 前缀测试数据（脚本/测试）；
- 已有报告称 16 个测试通过，含 CRUD、分页、LIKE、批删、批量回滚、50002、P1 契约。

**审计限制**：

- 本次 MySQL 未启动，未读取 live schema；
- 没有本次测试输出/surefire 报告；
- 不把“过去 16 个通过”当作本次重新验证。

### 5.3 前端构建

已有报告称：

```text
npm run build
vue-tsc --noEmit + vite build
✓ built
```

MS-04 `MS04_BROWSER_OK`、MS-05 `MS05_BROWSER_OK` 报告存在；脚本：

- `frontend/scripts/ms04-browser-check.mjs`；
- `frontend/scripts/ms05-browser-check.mjs`；
- 使用 Playwright 1.65.1 + 本机 Chrome；
- 真实后端/MySQL（MS-04/05 列表/保存）；
- 某些失败/防重复场景使用路由模拟；
- **未纳入 CI**，没有持久化截图/测试报告。

### 5.4 没有测试证据的项目

- `/web-basics`、`/servlet-demo`：没有实现，也没有测试；
- 论文：没有正文/截图；
- 全量 R1～R5：未执行/未汇总；
- AC-01～AC-32：没有定义，无法验收；
- 真实主库 CRUD：没有执行（只使用测试库）。

---

## 6. Git 状态（审计快照）

| 项 | 值 |
| --- | --- |
| 分支 | `main` |
| HEAD | `adf448b025b8a87eba3b2e9c47daa2a1ea302f2b` |
| HEAD 提交 | `MS-04` |
| HEAD 时间 | 2026-11-14 11:55:19 +0800 |
| 最近 4 条提交 | `adf448b MS-04`；`e12035f 后端`；`9a4809f 项目开发规划`；`fd13665 Initial commit` |
| 工作区 | **有未提交 MS-05 修改/新增** |

未提交修改（核心）：

```text
M AGENTS.md
M README.md
M docs/test-plan.md
M frontend/src/api/addresses.ts
M frontend/src/components/common/AppIcon.vue
M frontend/src/layout/AppLayout.vue
M frontend/src/main.ts
M frontend/src/router/index.ts
M frontend/src/types/address.ts

?? docs/review/ms-05-address-ui-report.md
?? frontend/scripts/ms05-browser-check.mjs
?? frontend/src/components/address/
?? frontend/src/views/AddressListView.vue
```

本次审计新增的四份交接文档也将以未提交文件出现；**没有执行 git add/commit/reset/clean/checkout**。

---

## 7. 问题清单

### P0

**未发现已确认的核心运行时 P0 缺陷**（在已实现的解析/CRUD/前端录入/列表范围内，报告和源码未显示业务不可用或数据破坏）。

但以下属于**最终交付层面的 P0/阻塞风险**，必须区分：

| 编号 | 问题 | 影响 | 当前状态 |
| --- | --- | --- | --- |
| P0-X1 | MS-06/07 课程实践页完全未实现 | 任务书 2.1～2.20 无法验收；论文第2章缺少依据 | NOT_STARTED |
| P0-X2 | MS-09 论文/截图未开始 | 课程最终交付无法完成 | NOT_STARTED |
| P0-X3 | MS-05 代码未提交 Git | 工作区丢失/换机即失去实现；HEAD 不含 MS-05 | 未提交 |
| P0-X4 | AC-01～AC-32 无定义 | “AC 全部满足”无法证明，最终验收标准缺失 | 文档缺口 |

这些是**项目验收阻塞项**，不是已实现功能的代码 Bug。

### P1

| 编号 | 问题 | 影响 | 建议 |
| --- | --- | --- | --- |
| P1-1 | 146 后端测试没有本次 surefire/CI 产物 | 无法独立复核；报告与代码可能漂移 | 提交后执行 `mvn clean test` 并保存 surefire 汇总 |
| P1-2 | MySQL 16 测试依赖 `DB_PASSWORD` + 预建测试库 | 未配置时静默跳过，容易误以为全通过 | 在 README/CI 明确；增加测试库初始化脚本/条件报告 |
| P1-3 | Playwright 未纳入 CI，部分失败/防重复用路由 Mock | 真实浏览器回归不可重复 | 建立最小 CI 或固定脚本/证据；文档标注 Mock |
| P1-4 | `development-roadmap.md`/`test-plan.md` 状态与当前代码不一致 | 新 ChatGPT 可能误判进度 | 以本审计文档为准，后续更新状态 |
| P1-5 | `docs/screenshots` 不存在 | 论文/验收缺真实截图 | MS-06/MS-09 采集 |
| P1-6 | `AddressService.batchSave` 仅做列表级复核；字段校验依赖 Controller | 内部直接调用 Service 可写入不符合 DTO 的数据 | 若要防御内部调用，在 Service 增加统一校验 |
| P1-7 | 测试库用逻辑删除清理，物理残留可能累积 | 测试库长期膨胀/误判 | 测试后只物理清理本次测试数据 |
| P1-8 | 未定义 AC；路线图引用 AC-01～32 | 无法做正式验收 | 补 AC 定义或明确以 FR 验收要点替代 |

### P2

- `database-design.md` 引用 `docs/sql/clean.sql`，文件缺失；
- `test-plan.md` 头部版本/适用阶段仍停在 MS-04，未写 MS-05；
- `architecture.md` 前端目录结构包含未实现的 `/web-basics`、`/servlet-demo`、列表高级组件，需区分设计契约与实际代码；
- 没有 `backend/AGENTS.md`、`frontend/AGENTS.md`（可选）；
- 没有代码覆盖率报告；
- 没有正式性能基准（仅临时探针）；
- Playwright 是 devDependency，但没有 npm script/CI；
- `AddressApiMysqlTest` 使用固定 `199` 前缀清理，若测试库有同前缀用户数据存在误删风险（当前测试库专用）；
- 主库 live schema 未在本次审计中读取。

### P3

- 主前端 chunk >500 kB，Element Plus 全量引入；
- Sass legacy API 警告；
- 地址簿无列配置/密度等高级功能（非 MVP）；
- 移动端仅做基础响应式，不承诺正式移动端；
- 无登录/权限/审计，课程范围限制；
- 无唯一约束/去重（设计选择）；
- `AddressService` 可分页查询未标 `readOnly`；
- `PhoneExtractor.normalizePhone` 为 public，层级边界略耦合。

---

## 8. 下一阶段计划（建议顺序）

### 第 0 步：冻结当前工作区（不在本次审计执行）

1. 审阅 MS-05 diff；
2. 对 MS-05 做独立验收（列表/搜索/分页/新建/编辑/删除/批删/选择清空）；
3. 提交 MS-05；记录 commit；
4. 明确 146 测试和 MS05_BROWSER_OK 的证据。

> 本次审计不执行 commit；这一步需要用户确认。

### 第 1 步：MS-06 Web 基础实践页（原始下一里程碑）

- `/web-basics` 2.1～2.19；
- `/web-basics/wireframe` 2.18；
- 每节“演示 + 结论 + 代码”；
- 采集真实截图到 `docs/screenshots/`；
- 不能以设计文档替代代码。

### 第 2 步：MS-07 Servlet URI 映射演示

- 3 个 `@WebServlet`；
- `ServletMappingDemoController` 对照；
- `/servlet-demo` 真实请求矩阵；
- 固定 405/默认映射等边界。

### 第 3 步：MS-08 全量测试与缺陷修复

- 定义 AC-01～AC-32（或明确替代标准）；
- 跑 L1～L4；
- Playwright 完整流程 + 安全/性能；
- 修复 P0/P1；
- 保存真实测试证据。

### 第 4 步：MS-09 论文与截图

- 2.1～2.21；
- 使用真实截图、真实测试记录；
- 不编造结果。

### 第 5 步：MS-10 验收与答辩

- 从零按 README 复现；
- 演示脚本/Q&A；
- 确认 127.0.0.1、无认证声明、数据备份。

### 不调整原始顺序的理由

MS-06/07 不依赖新业务数据，可在 MS-05 验收后并行；MS-08 依赖业务与课程实践页；MS-09 依赖 MS-06/07/08。不要为了“看起来完成”跳过 MS-06/07 或直接进入论文。

---

## 9. 审计限制

1. 本次**没有修改**代码/数据库/业务数据；
2. 本次**没有重新运行** Maven/Playwright/前端构建；结论基于源码、历史报告和可读配置；
3. MySQL 未启动，live schema 未读取；schema 以 `schema.sql` 和设计文档为准；
4. Git 操作只读；没有 reset/clean/checkout/stash/commit；
5. 历史报告中的 146/浏览器结果只能作为历史证据，不等于本次重新执行；
6. AC 定义缺失，不能声称“全部验收通过”；
7. 未检查真实用户数据（不存在）；测试库/主库数据未被修改。

---

## 10. 相关文档

- [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md)
- [`ORIGINAL_DEVELOPMENT_PLAN.md`](ORIGINAL_DEVELOPMENT_PLAN.md)
- [`handover/CHATGPT_PROJECT_HANDOVER.md`](handover/CHATGPT_PROJECT_HANDOVER.md)
- [`review/ms-05-address-ui-report.md`](review/ms-05-address-ui-report.md)
