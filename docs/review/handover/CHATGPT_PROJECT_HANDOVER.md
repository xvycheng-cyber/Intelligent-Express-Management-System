# ChatGPT 项目交接档案

> 交接对象：未参与前期开发的新 ChatGPT / 新开发者
> 审计日期：2026-11-14 17:03（Asia/Hong_Kong）
> 仓库：`Intelligent-Express-Management-System`
> 本文件是当前真实状态的交接入口；先读本文件，再读：
> - [`PROJECT_CONTEXT.md`](../PROJECT_CONTEXT.md) — 长期稳定信息
> - [`ORIGINAL_DEVELOPMENT_PLAN.md`](../ORIGINAL_DEVELOPMENT_PLAN.md) — 原始规划与变更
> - [`development-progress.md`](../development-progress.md) — 当前进度、问题、下一阶段
> - [`requirements.md`](../requirements.md)、[`architecture.md`](../architecture.md)、[`api-design.md`](../api-design.md)、[`database-design.md`](../database-design.md)、[`test-plan.md`](../test-plan.md)

---

## 1. 项目基本信息

| 项 | 值 |
| --- | --- |
| 系统名称 | 快递通——智能快递地址簿录入与管理系统 |
| 仓库标识 | `Intelligent-Express-Management-System` |
| 课程论文题目 | 快递地址簿录入程序设计（不得改写） |
| 课程任务 | 任务（一）快递地址簿录入程序设计；不采用任务（二） |
| 当前设计基线 | v1.1-final，后有 MS-02.1～MS-05 实现修正 |
| 当前 Git | `main`，HEAD `adf448b`（`MS-04`，2026-11-14 11:55:19 +0800） |
| 当前工作区 | **MS-05 未提交**；本轮审计新增 4 份文档未提交 |
| 运行边界 | 仅本机 `127.0.0.1`；无登录/权限；课程演示 |

---

## 2. 项目定位与核心业务

### 2.1 定位

课程设计项目，不是真实快递业务系统。核心是“**多行文本 → 解析 → 人工确认 → 地址簿管理**”，并覆盖课程任务书 2.1～2.20 的 Web 实践与 Servlet 映射演示。

### 2.2 核心流程

```text
S1 批量录入
 多行文本 → POST /api/addresses/parse（不写库）
 → 预览 VALID/INVALID → 人工修改
 → 保存确认 → POST /api/addresses/batch（事务）
 → 地址簿列表

S2 新建：列表页 → 填三项 → POST /api/addresses
S3 编辑：详情 → 修改 → PUT /api/addresses/{id}
S4 删除/批删：确认 → DELETE 或 POST /api/addresses/batch-delete
S5 实践验收：/web-basics（未实现）、/servlet-demo（未实现）
```

### 2.3 明确不做

登录/权限、快递下单/轨迹、地图/地址标准化、OCR/语音、Excel 导入导出、AI 云解析、多租户、MQ/缓存/微服务、移动端正式交付。

---

## 3. 技术架构

### 3.1 技术栈实际版本

- Java 17；
- Spring Boot 3.3.5；
- Spring MVC；
- MyBatis-Plus 3.5.7；
- MySQL 8（InnoDB / `utf8mb4`）；
- Jakarta Validation；
- Vue 3.5.13 + TypeScript 5.7.2；
- Vite 5.4.11；
- Element Plus 2.9.1；
- Axios 1.7.9；
- Vue Router 4.5.0；
- Sass 1.83.0；
- Playwright 1.65.1（devDependency，使用本机 Chrome）。

### 3.2 后端分层

```text
Controller → Service → Mapper → MySQL
```

- Controller：收参、触发校验、统一 `R`；
- Service：业务、事务、DTO→Entity；
- Mapper：MyBatis-Plus BaseMapper + 关键词分页 XML；
- Parser：独立纯逻辑，禁止 Mapper/JDBC/IO。

### 3.3 前端分层

```text
views / components / api / types / utils / layout / router / styles
```

实际页面只有：

- `/import` 批量录入；
- `/addresses` 地址簿管理；
- 404。

**没有** `/web-basics`、`/web-basics/wireframe`、`/servlet-demo`。

---

## 4. 数据库设计

单表 `express_address`：

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK AUTO_INCREMENT | API 输出 String |
| `receiver_name` | VARCHAR(50) NOT NULL | Unicode 码点 1～50 |
| `phone` | VARCHAR(20) NOT NULL | 规范化电话 |
| `full_address` | VARCHAR(255) NOT NULL | 保留原文/内部标点 |
| `create_time` | DATETIME | DB 默认 |
| `update_time` | DATETIME | DB 默认 + ON UPDATE |
| `is_deleted` | TINYINT | 逻辑删除 0/1 |

索引：`idx_deleted_create`、`idx_phone`；无外键/唯一键/触发器。

脚本：

- [`../../backend/src/main/resources/db/schema.sql`](../../backend/src/main/resources/db/schema.sql)
- [`../../backend/src/main/resources/db/data.sql`](../../backend/src/main/resources/db/data.sql)
- [`../../backend/src/main/resources/db/admin-init.sql`](../../backend/src/main/resources/db/admin-init.sql)

数据库账号：本机 root，口令 `DB_PASSWORD`/本地 `application-local.yml`；不得提交真实口令；不要创建 `root@'%'`。

**审计限制**：本轮未启动 MySQL，未读取 live schema；以上以 `schema.sql` 和设计文档为依据。

---

## 5. API 情况

### 5.1 已实现接口

| 方法 | 路径 | 状态 |
| --- | --- | --- |
| POST | `/api/addresses/parse` | 已实现 |
| GET | `/api/addresses` | 已实现 |
| GET | `/api/addresses/{id}` | 已实现 |
| POST | `/api/addresses` | 已实现 |
| PUT | `/api/addresses/{id}` | 已实现 |
| DELETE | `/api/addresses/{id}` | 已实现 |
| POST | `/api/addresses/batch` | 已实现 |
| POST | `/api/addresses/batch-delete` | 已实现 |
| GET | `/api/health` | 已实现 |
| GET | `/api/health/db` | 已实现 |

### 5.2 关键契约

- 响应 `{code,message,data}`；
- `data.violations[]`：字段错误；
- `items[].errors[]`：解析行错误；
- 分页 `total/current/size/pages`：**Number**；
- 地址 `id`：**String**；
- 时间：`yyyy-MM-dd HH:mm:ss`；
- 错误码：40001/40002/40003/40401/40402/40501/50001/50002；
- 批量保存 1～200 条，201 → 40003；
- 批量删除 `ids` 1～200 个正整数字符串；
- LIKE `%`、`_`、`!` 字面匹配，后端 `LikeEscaper` + `ESCAPE '!'`；前端不得二次转义。

完整契约见 [`../api-design.md`](../api-design.md)。

---

## 6. 各阶段完成情况

| 阶段 | 状态 | 实际依据 | 说明 |
| --- | --- | --- | --- |
| MS-00 设计基线 | ACCEPTED | 8 份 docs | 设计冻结 |
| MS-00B | ACCEPTED | P0 报告/文档 | F-01～F-10 |
| MS-00C | ACCEPTED | P0 报告/文档 | 姓名/40003/isEdited/账号 |
| MS-01 工程骨架 | TESTED | 初始化报告、代码 | 未正式签字 |
| MS-02 Parser | TESTED | 86 parser 测试 + 8 真实 Parser/HTTP | 含 MS-02.1/02.2 |
| MS-03 后端 CRUD | TESTED | 146 后端测试报告（16 MySQL） | 无正式签字 |
| MS-03.1 契约修复 | TESTED | remediation 报告/最终审查 | 条件通过 |
| MS-04 批量录入 UI | TESTED | build + MS04_BROWSER_OK | 条件通过 |
| MS-05 地址簿 UI | TESTED | build + MS05_BROWSER_OK | **未提交；未独立验收** |
| MS-06 Web 实践页 | NOT_STARTED | 无文件 | — |
| MS-07 Servlet 演示 | NOT_STARTED | 无文件 | — |
| MS-08 全量测试 | PARTIAL | 分散测试/脚本 | 无 R1～R5/CI |
| MS-09 论文/截图 | NOT_STARTED | 无正文/截图 | — |
| MS-10 验收/答辩 | NOT_STARTED | 无演示脚本/Q&A | — |

---

## 7. 当前开发位置与 Git 状态

### 7.1 准确位置

- 代码做到 **MS-05 地址簿管理前端**；
- MS-05 有实现、构建报告、浏览器测试报告；
- **但 Git HEAD 是 MS-04**，MS-05 的所有新组件/页面/测试脚本/报告未提交；
- 没有正式验收；MS-06/07/09/10 未开始；
- 当前不是“项目已完成”，而是“业务主流程前后端已实现、课程实践页与论文未完成”。

### 7.2 HEAD 与最近提交

| 项 | 值 |
| --- | --- |
| branch | `main` |
| HEAD | `adf448b025b8a87eba3b2e9c47daa2a1ea302f2b` |
| subject | `MS-04` |
| time | 2026-11-14 11:55:19 +0800 |
| 前三条 | `e12035f 后端`；`9a4809f 项目开发规划`；`fd13665 Initial commit` |

### 7.3 未提交内容（审计快照）

修改：

```text
AGENTS.md
README.md
docs/test-plan.md
frontend/src/api/addresses.ts
frontend/src/components/common/AppIcon.vue
frontend/src/layout/AppLayout.vue
frontend/src/main.ts
frontend/src/router/index.ts
frontend/src/types/address.ts
```

新增：

```text
docs/review/ms-05-address-ui-report.md
frontend/scripts/ms05-browser-check.mjs
frontend/src/components/address/AddressFormDialog.vue
frontend/src/components/address/AddressSearchBar.vue
frontend/src/components/address/AddressTable.vue
frontend/src/components/address/BatchDeleteDialog.vue
frontend/src/views/AddressListView.vue
```

本轮新增 4 份交接文档也会是未提交：

```text
docs/PROJECT_CONTEXT.md
docs/ORIGINAL_DEVELOPMENT_PLAN.md
docs/development-progress.md
docs/handover/CHATGPT_PROJECT_HANDOVER.md
```

**不要**把 MS-05 误认为已提交；不要执行 reset/clean/checkout 丢失当前工作区。

---

## 8. 已实现功能

### 8.1 解析

- 多行/空行/行号/rawLine；
- 中文/英文逗号、Tab、分号；
- 手机/座机/脱敏/+86/全角；
- 姓名候选回退、电话包裹符、地址保真；
- 错误码 E102～E108/E110/E111（E109 已删除）；
- 姓名 Unicode 码点 1～50；
- `POST /parse` 入参上限 20000/200，超限 40003；
- Parser 纯逻辑无 DB。

### 8.2 地址 CRUD

- 分页 + 姓名/电话/地址搜索；
- LIKE 字面匹配；
- 新建/详情/编辑/逻辑删；
- 批量保存事务；
- 批量删除当前页/存在性；
- 统一错误结构；
- PageResult Number、ID String、时间格式。

### 8.3 前端

- 批量录入：输入、识别、统计、可编辑预览、错误、恢复、确认保存、失败保留、防重复；
- 地址簿：真实列表/total、搜索/重置、分页、新建/编辑、单删/批删、选择清空、自动退页、空/错态；
- `isEdited` 派生；`toPayload` 只业务字段；
- 姓名 `Array.from`；ID string；分页 number。

---

## 9. 未实现功能

- `/web-basics` 2.1～2.19；
- `/web-basics/wireframe` 2.18；
- `/servlet-demo`、`/demo/**`；
- `docs/screenshots/`；
- 论文正文/Word/PDF；
- 答辩演示脚本/Q&A；
- 正式 AC 定义；
- 全量 R1～R5 测试记录；
- Playwright CI；
- 登录/权限（明确范围外，不是遗漏）。

---

## 10. 测试证据与真实性

### 10.1 后端

源码 `@Test` 计数 **146**：

| 类别 | 数量 |
| --- | --- |
| Parser | 86 |
| Controller MockMvc | 12 |
| 真实 Parser + HTTP | 8 |
| MySQL 集成 | 16 |
| DTO | 10 |
| LIKE | 4 |
| CodePoint | 5 |
| 基础 | 5 |

历史报告称：

```text
mvn -B clean test
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**本次审计没有重新运行 Maven**；`backend/target` 没有 surefire 报告，只有 jar。不要把历史报告当作本次重跑结果。

### 10.2 MySQL

`AddressApiMysqlTest`：

- `@SpringBootTest` + `test` profile；
- `DB_PASSWORD` 未设置时跳过；
- 测试库 `express_address_book_test`；
- 历史报告 16 个通过；
- 本轮未启动 MySQL，未读取 live schema；
- 不声称本次真实数据库通过。

### 10.3 前端

历史报告/脚本：

```text
npm run build
node scripts/ms04-browser-check.mjs → MS04_BROWSER_OK
node scripts/ms05-browser-check.mjs → MS05_BROWSER_OK
```

Playwright：

- 1.65.1 + 本机 Chrome；
- Vite dev + 真实后端/MySQL；
- MS-05 覆盖 25 项；
- 部分错误/防重复使用路由 Mock（报告已说明）；
- 未纳入 CI；
- 本轮没有重新运行。

### 10.4 未验证

- 本次 Maven 146 重跑；
- 本次 Playwright；
- live MySQL schema；
- `/web-basics`、`/servlet-demo`；
- 论文/截图；
- AC 验收。

---

## 11. 已知 Bug / 问题

### P0（交付阻塞，不是已实现功能运行时 Bug）

1. **MS-06/07 完全未实现**：任务书 2.1～2.20 无法验收，论文第2章缺少依据。
2. **MS-09 论文/截图未开始**：最终课程交付无法完成。
3. **MS-05 未提交 Git**：当前 HEAD 为 MS-04；换机/清理会丢失 MS-05。
4. **AC-01～AC-32 未定义**：路线图引用但无定义表，无法正式证明“AC 全部满足”。

### P1

1. 146 测试没有本次 surefire/CI 产物；
2. MySQL 16 测试依赖 `DB_PASSWORD` + 预建测试库，否则跳过；
3. Playwright 未纳入 CI，部分失败场景 Mock；
4. roadmap/test-plan 状态与当前代码漂移；
5. 无截图目录；
6. `AddressService.batchSave` 字段级校验依赖 Controller；直接调用 Service 可绕过；
7. 测试库逻辑删除可能残留；
8. AC 缺失导致验收标准不完整。

### P2

- `clean.sql` 缺失；
- `test-plan` 头部仍写 MS-04；
- architecture 目录含未实现页面；
- 无 `AGENTS.md` 子文件；
- 无覆盖率报告；
- 无正式性能基准；
- 无 Playwright npm script/CI；
- live DB 未审计。

### P3

- 主 chunk >500 kB；
- Sass legacy warning；
- 无高级列表配置；
- 移动端仅基础响应式；
- 无登录/权限（范围外）；
- 无唯一约束/去重（设计选择）；
- `AddressService` 分页未标 readOnly；
- `PhoneExtractor.normalizePhone` public 层级耦合。

---

## 12. 技术债务

1. **课程实践页缺失** 是最主要债务，不是“前端小功能”，而是任务书硬性要求；
2. **论文/截图缺失**；
3. **MS-05 未提交**；
4. 测试证据分散在 Markdown，缺 CI/持久报告；
5. MySQL 测试依赖本地环境；
6. Playwright 脚本是独立 Node 脚本，非正式测试套件；
7. 无 AC 定义；
8. 文档状态列滞后（roadmap/test-plan）；
9. 前端全量 Element Plus chunk 大；
10. 无覆盖率/性能基线。

---

## 13. 最近一次开发记录

- MS-05 地址簿前端完成：
  - `AddressListView`、搜索、表格、表单、批删；
  - `MS05_BROWSER_OK` 报告；
  - Element Plus zh-cn；
  - 390px 分页溢出修复；
  - 选择清空/自动退页；
- 但 **未提交 Git**，HEAD 仍是 MS-04；
- 本次审计没有改代码/数据库，只新增四份文档。

---

## 14. 下一阶段开发目标

### 14.1 立即（用户确认后）

1. 审阅并提交 MS-05（不要丢失工作区）；
2. 给 MS-05 做独立验收；
3. 固定 146 后端测试和 MS-04/05 浏览器证据；
4. 补 AC-01～AC-32 定义或明确替代标准。

### 14.2 原计划下一步：MS-06

**Web 基础实践页 2.1～2.19**

必须交付：

- `/web-basics`：19 节，每节“演示 + 结论 + 代码”；
- `/web-basics/wireframe`：2.18 线框仿制；
- 真实截图 `docs/screenshots/`；
- 不能只写设计文档或静态文字。

### 14.3 然后：MS-07

- 3 个 Servlet；
- `/demo/**` 与 Spring 对照；
- `/servlet-demo` 真实命中矩阵；
- 405/默认映射边界。

### 14.4 然后：MS-08

- R1～R5、L1～L4；
- Playwright 全流程；
- 安全/性能；
- 修 P0/P1；
- 真实测试记录。

### 14.5 最后：MS-09 / MS-10

- 论文 2.1～2.21；
- 截图；
- 从零复现；
- 演示脚本/Q&A。

---

## 15. 下一阶段任务拆分

### MS-05 收尾（非新功能）

- [ ] 审阅 diff：`frontend/src/components/address/*`、`AddressListView.vue`、API/类型；
- [ ] 跑一次真实 MySQL + 浏览器（test DB，清测试数据）；
- [ ] 确认选择清空/自动退页/批删 ID；
- [ ] 提交 MS-05；
- [ ] 记录 commit/测试证据；
- [ ] 更新 roadmap/test-plan 状态。

### MS-06

- [ ] 建 `WebBasicsView`、`WireframeView`、`DemoSection`、`CodeBlock`；
- [ ] 2.1～2.8 盒模型/显示；
- [ ] 2.9～2.17 布局/定位；
- [ ] 2.18 线框；
- [ ] 2.19 布局原则；
- [ ] 每节真实可交互；
- [ ] 19 节截图；
- [ ] 构建 + 浏览器检查。

### MS-07

- [ ] 3 个 `@WebServlet`；
- [ ] `ServletMappingDemoController`；
- [ ] 映射矩阵；
- [ ] 405/默认映射；
- [ ] 页面真实请求结果；
- [ ] 截图。

### MS-08

- [ ] 定义 AC；
- [ ] P0 100% / P1≥95%；
- [ ] Playwright 完整；
- [ ] 安全/性能；
- [ ] 缺陷清单与回归。

### MS-09

- [ ] 论文正文；
- [ ] 2.21 真实实现；
- [ ] 截图与代码一致；
- [ ] 查重/格式。

### MS-10

- [ ] 从零按 README；
- [ ] 5/10 分钟演示；
- [ ] Q&A；
- [ ] 数据备份；
- [ ] 127.0.0.1 复核；
- [ ] 说明无认证。

---

## 16. 下一阶段验收标准

### MS-05 收尾

- 列表/搜索/分页/新建/编辑/单删/批删真实可用；
- 分页字段 Number、ID String；
- 选择只当前页，翻页/搜索/refesh 清空；
- 批删请求只有选中 ID；
- 空/错/loading 态；
- 浏览器测试实际执行；
- 代码已提交。

### MS-06

- 2.1～2.19 每节真实演示；
- 2.18 三图/线框；
- 构建通过；
- 无控制台错误；
- 截图与代码一致。

### MS-07

- 3 个 Servlet 真实注册；
- 每类映射真实命中；
- 405/默认映射；
- 页面结果来自真实 HTTP。

### MS-08

- AC 定义；
- P0 100%、P1≥95%；
- 无未修复 P0；
- 真 MySQL/Playwright；
- 安全/性能。

### MS-09/10

- 论文 2.1～2.21；
- 真实截图/测试；
- 从零复现；
- 不编造、不临时手改数据。

---

## 17. 需要 ChatGPT 重点关注

1. **不要把 `docs/architecture.md` 的设计目录当成实际实现**；很多页面/组件不存在。
2. **不要只看 `npm run build` 就认为功能完成**；MS-06/07 没有代码。
3. **不要把历史报告当成本次测试重跑**；本轮审计没有执行 Maven/Playwright。
4. **MS-05 在磁盘上，但未提交 Git**；先审阅/提交，不要 reset/clean。
5. **AC-01～AC-32 没有定义**；验收前必须补。
6. **MS-06/07 是任务书 2.1～2.20 的硬性要求**，不能跳过做论文。
7. **无认证是本机演示范围限制**，不要写成已实现安全认证。
8. **MySQL 测试需要 `DB_PASSWORD`/test DB**；主库未参与测试。
9. **测试数据/业务数据不要清空主库**；Playwright 脚本清理逻辑需要检查。
10. **不要擅自修改已冻结 API 契约**（分页 Number、ID String、LikeEscaper、错误码）。

---

## 18. 快速启动（新 ChatGPT 的操作顺序）

1. 读本文件 §1～§10；
2. 读 [`PROJECT_CONTEXT.md`](../PROJECT_CONTEXT.md) 技术/边界；
3. 读 [`ORIGINAL_DEVELOPMENT_PLAN.md`](../ORIGINAL_DEVELOPMENT_PLAN.md) 确认原始计划；
4. 读 [`development-progress.md`](../development-progress.md) 当前状态/P0-P1；
5. 读 [`api-design.md`](../api-design.md)、[`database-design.md`](../database-design.md)、[`test-plan.md`](../test-plan.md)；
6. 用 `git status` 确认 MS-05 未提交；
7. 不要运行 reset/clean/checkout；
8. 如要跑测试，先确认 `express_address_book_test`、`DB_PASSWORD`，测试后只清理本次数据；
9. 下一步先收尾 MS-05，再 MS-06。

---

## 19. 交接结论

- **项目不是已完成**；
- 完成的是“解析 + 后端 CRUD + 批量录入 + 地址簿管理”；
- 未完成的是“Web 基础实践页 + Servlet 演示 + 全量测试 + 论文 + 验收”；
- 当前正确位置是 **MS-05 代码完成但未提交/未正式验收，MS-06 之前**；
- 最重要风险是 **MS-05 未提交、MS-06/07 未实现、论文/截图未开始、AC 未定义、测试证据不可独立重放**；
- 不应跳过原始 MS-06/07 直接进入 MS-09/MS-10。
