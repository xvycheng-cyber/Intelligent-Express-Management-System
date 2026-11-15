# 开发路线图与里程碑（development-roadmap.md）

| 项目 | 内容 |
| --- | --- |
| 文档版本 | v1.1-final（设计冻结） |
| 当前阶段 | **第一阶段：设计冻结（v1.1-final）**，不写业务代码 |
| 依据 | `requirements.md`、`architecture.md`、`database-design.md`、`api-design.md`、`parser-design.md`、`test-plan.md` |
| 排期口径 | 以"周"为相对单位（第 1 周 = 项目启动周）；具体日历日期由小组按学期安排填写，本文档不编造日期 |
| 人力假设 | 3～5 人小组，每人每周有效投入 6～8 小时 |

---

## 1. 阶段总览

```text
第1阶段  设计基线            MS-00  ✅ 已完成（8 份文档）
   ↓
第1.5阶段 P0 设计修订         MS-00B ✅ 已完成（F-01～F-10）
   ↓
第1.6阶段 第二轮遗留修复       MS-00C ✅ 已完成（姓名/40003/isEdited/账号）
   ↓
第2阶段  工程骨架 + 数据库    MS-01
   ↓
第3阶段  解析器（纯逻辑）      MS-02   ← 关键路径，先于前端
   ↓
第4阶段  保存与地址 CRUD      MS-03
   ↓
第5阶段  前端录入主流程        MS-04   ← 任务书核心演示路径打通
   ↓
第6阶段  列表/搜索/批量        MS-05
   ↓
第7阶段  Web 基础实践页        MS-06   ← 可与 MS-04/05 并行
   ↓
第8阶段  Servlet 映射演示      MS-07   ← 可与 MS-05/06 并行
   ↓
第9阶段  测试与缺陷修复        MS-08
   ↓
第10阶段 论文与截图            MS-09
   ↓
第11阶段 验收与答辩准备        MS-10
```

---

## 2. 里程碑明细

### MS-00 设计基线（第 1 周）— ✅ 已完成

| 项 | 内容 |
| --- | --- |
| 目标 | 需求、架构、数据、接口、解析规则、测试、路线图、论文大纲全部成文并互相一致 |
| 产出 | `requirements.md`、`architecture.md`、`database-design.md`、`api-design.md`、`parser-design.md`、`test-plan.md`、`development-roadmap.md`、`course-paper-outline.md` |
| 出口条件 | 8 份文档存在；编号互相引用无断链；任务书 2.1～2.21 全部落在设计中；未编写业务代码 |
| 状态 | 已完成（v1.0） |
| 风险 | 任务书理解偏差 → 已逐条摘录原文并编号（CR-01～CR-18），可回溯 |

### MS-00B P0 设计修订（第 1 周）— ✅ 已完成

| 项 | 内容 |
| --- | --- |
| 目标 | 关闭独立审查提出的 F-01～F-10 共 10 项 P0 问题 |
| 任务 | ① LIKE 转义（`LikeEscaper`/`ESCAPE '!'`）；② 仅本机监听；③ 保存确认与 `isEdited`；④ 批删当前页；⑤ 单字姓名；⑥ `violations` 结构；⑦ 命名统一；⑧ 删除 E109；⑨ 嵌套批量校验；⑩ `.gitignore` + `README.md` |
| 产出 | 8 份文档 v1.1 + `.gitignore` + `README.md` |
| 出口条件 | F-01～F-10 全部关闭；新增测试用例已登记且**未执行**；文档间术语/字段/编号一致 |
| 状态 | 已完成（2026-11-14） |

### MS-00C 第二轮审查遗留修复（第 1 周）— ✅ 已完成

| 项 | 内容 |
| --- | --- |
| 目标 | 关闭第二轮独立审查的 4 项一致性缺口，正式冻结 v1.1 |
| 任务 | ① 姓名统一 1～50（Parser/Validator/前端/DB/错误码）；② 批量 201 条映射 40003；③ `isEdited` 派生与 `toPayload` 投影；④ 数据库账号方案（MS-01 后最终调整为 root 本机，见 `database-account-change-report.md`） |
| 产出 | 8 份文档 v1.1-final、`docs/review/p0-finalization-report.md`、README/AGENTS 同步 |
| 出口条件 | 四项缺口关闭；错误码/姓名长度/批量上限/账号规则/本机监听在文档间一致；未执行测试 |
| 状态 | 已完成（2026-11-14） |

### MS-01 工程骨架与数据库（第 2 周）

| 项 | 内容 |
| --- | --- |
| 目标 | 前后端能起、能连库、能跑空测试 |
| 任务 | ① 先建 `.gitignore` 与 `README.md`（F-10）；② 建 `backend` Maven 工程（Java 17 + Boot 3.x + MP + Validation + MySQL 驱动）；③ 建 `frontend` Vite 工程（Vue3 + TS + Element Plus + Router + Axios）；④ 配置 `application.yml`（`server.address=127.0.0.1`、`username: ${DB_USERNAME:root}`、`password: ${DB_PASSWORD}`）、`application-local.example.yml`、Vite `host='127.0.0.1'` 与代理；⑤ root 执行 `schema.sql`/`data.sql`；`admin-init.sql` 仅作为可选最小权限方案；⑥ 建目录骨架与空类 |
| 产出 | 可启动的后端（127.0.0.1:8080）、可启动的前端（127.0.0.1:5173）、数据库表、`README.md` 初版 |
| 依赖 | MS-00B |
| 估算 | 2～3 人日 |
| 出口条件 | ① `mvn spring-boot:run` 成功且能查到 `express_address`；② `npm run dev` 打开页面无报错；③ `GET /api/health` 返回 `code:0`/`status:UP`；④ `GET /api/health/db` 返回 `status:UP`（root 连接）；⑤ `mvn clean test` 可运行；⑥ `netstat` 确认 8080/5173 只监听 `127.0.0.1`；⑦ 仓库无 `application-local.yml`、无真实口令 |
| 风险 | 依赖下载慢/版本冲突 → 锁定版本，先跑通最小依赖，再逐个加 |

### MS-02 解析器（第 2～3 周）— 关键路径

| 项 | 内容 |
| --- | --- |
| 目标 | `AddressParserService` 完整实现 `parser-design.md` 的 PR-01～PR-13 |
| 任务 | ① `LineSplitter`（换行/BOM/空白/限长）；② `PhoneExtractor`（手机/座机/脱敏/`+86`/全角）；③ `NameExtractor`（**1～50 个 Unicode 码点**、`\p{IsHan}`、英文、排除词）；④ `AddressExtractor`（原串剔除保真）；⑤ 错误码 E102～E108、E110、E111（**不含 E109**；E106 非法字符、E111 >50）；⑥ 单元测试覆盖 TC-PARSE-001～038；MS-02.1 增补 039～059 与 API 入口 |
| 产出 | `parser` 包 + `AddressParserServiceTest`（74 个用例）+ `ParseInputGuard` + `AddressController`（仅 parse）+ 11 个 Controller 测试 |
| 产出 | `parser` 包 + `AddressParserServiceTest`（74 个用例）+ `ParseInputGuard` + `AddressController`（仅 parse）+ 11 个 Controller 测试 |
| 依赖 | MS-01（仅工程骨架，**不依赖数据库**） |
| 估算 | 3～4 人日 |
| 出口条件 | ① `mvn -Dtest=AddressParserServiceTest test` 全绿；② 覆盖率（parser 包）≥90%；③ 反射断言"无 Mapper/DataSource 依赖"通过；④ `parser-design.md` §6 的 16 条示例结果一致（不一致则改代码或改文档并说明）；⑤ 单字姓名、50/51 字边界用例通过 |
| 风险 | 正则误判、地址被截断 → 先写用例再写实现（表驱动），每加一条规则补一条用例 |

### MS-03 保存与地址 CRUD 后端（第 3～4 周）

| 项 | 内容 |
| --- | --- |
| 目标 | 8 个接口全部可用，事务与校验到位 |
| 状态 | **已完成（MS-03/03.1）**：146 个测试通过；MySQL 16 个集成测试；Vue 页面未开发 |
| 任务 | ① Entity/Mapper/VO/DTO；② `AddressService`（分页、关键词 **LikeEscaper**、新建、编辑、逻辑删、批删、批量保存事务）；③ `AddressController`；④ 统一响应 `R`、`ResultCode`、`Violation`、`GlobalExceptionHandler`（`items`/`ids` 的 `@Size` 违规 → 40003）；⑤ Jackson 配置（时间格式、Long→String）；⑥ MP 分页与逻辑删除配置；⑦ **嵌套校验**（`List<@NotNull @Valid>`、`@Size(max=200)`）；⑧ L2/L3 测试 |
| 产出 | `/api/addresses/**` 全通；`AddressControllerTest`、`AddressApiMysqlTest`、`AddressSaveDTOTest`、`LikeEscaperTest` |
| 依赖 | MS-01、MS-02（parse 需要解析器） |
| 估算 | 4～5 人日 |
| 出口条件 | ① `mvn test` 全绿；② `AddressApiMysqlTest` 覆盖 CRUD/分页/LIKE/批量/事务回滚；③ 批量保存失败整批回滚（真实 MySQL）；④ `keyword=%`/`_` 只匹配字面字符；⑤ 200 条 200、201 条 400/40003；⑥ 响应结构与 `api-design.md` 完全一致（`violations`） |
| 风险 | 逻辑删除与 `@TableLogic` 配置漂移 → 同时用全局配置 + 注解，并有测试断言删除后查不到 |

### MS-04 前端录入主流程（第 4～5 周）

| 项 | 内容 |
| --- | --- |
| 目标 | "粘贴 → 识别 → 修改 → 保存 → 列表可见"打通 |
| 任务 | ① `AppLayout` + 路由；② `api/request.ts`、`api/address.ts`（含 `toPayload`）、`types/address.ts`；③ `ImportView` + `ParsePanel` + `ParseResultTable`（可编辑、状态、错误、删除行、`original` 快照、派生 `isEdited`、姓名按 Unicode 码点校验）；④ **保存确认弹窗**（有效/修改/异常计数 + 行号原因）与错误定位；⑤ `toPayload` 只投影业务字段，保存按钮防重复提交；⑥ 加载/空/错误状态；⑦ 恢复原值后 `isEdited=false` |
| 产出 | `/import` 可用；核心链路 E2E 可跑 |
| 依赖 | MS-02、MS-03 |
| 估算 | 4～5 人日 |
| 出口条件 | ① 手工走通 AC-01～AC-06、AC-29；② 修改后的值真正入库；③ 失败行可人工补齐后保存、异常行不提交；④ 无 `any` 滥用、`npm run build` 通过 |
| 风险 | 预览表格编辑卡顿/行错位 → 稳定 `rowKey`，避免整表重建 |

### MS-05 列表、搜索、新建/编辑、删除与批量删除（第 5～6 周）

| 项 | 内容 |
| --- | --- |
| 目标 | 地址簿管理功能完整 |
| 任务 | ① `AddressListView`（表格、分页、排序、loading、空态）；② `AddressSearchBar`（关键词 + 回车 + 重置）；③ `AddressFormDialog`（新建/编辑复用，校验）；④ 单删二次确认；⑤ 批删（**仅当前页**勾选、翻页清空、确认数量、未选中禁用）；⑥ 操作后局部刷新与页码回退 |
| 产出 | `/addresses` 全部功能 |
| 依赖 | MS-03、MS-04（共用 API 层） |
| 估算 | 4～5 人日 |
| 出口条件 | ① AC-07～AC-12、AC-27 通过；② 删除/批删有明确确认；③ 翻页清空选择；④ 空态与错误态正常；⑤ 1366×768 无溢出 |
| 风险 | 删除后当前页为空 → 删除后若当前页无数据自动回退一页 |

### MS-06 Web 基础实践页（第 6～7 周，可与 MS-04/05 并行）

| 项 | 内容 |
| --- | --- |
| 目标 | 2.1～2.19 全部可演示 |
| 任务 | ① `WebBasicsView` + 锚点导航；② `DemoSection`、`CodeBlock`；③ 8 个演示组件（见 `architecture.md` §6.2）；④ `styles/basics.scss`；⑤ `WireframeView`（2.18）；⑥ 逐节撰写"观察结论"；⑦ 采集 19+3 张截图 |
| 产出 | `/web-basics`、`/web-basics/wireframe`、`docs/screenshots/2-01…2-19` |
| 依赖 | MS-01（前端骨架）；与业务解耦，可独立开发 |
| 估算 | 5～6 人日 |
| 出口条件 | ① TC-WEB-01～020 通过；② 每节有"演示 + 结论 + 代码"；③ 2.18 三图齐备；④ 截图命名与文档一致 |
| 风险 | 演示受屏幕宽度影响 → 固定演示容器尺寸；sticky/fixed 需固定滚动容器 |

### MS-07 Servlet 映射演示（第 7 周，可与 MS-06 并行）

| 项 | 内容 |
| --- | --- |
| 目标 | 2.20 可运行、可讲清 |
| 任务 | ① 3 个 `@WebServlet` + `@ServletComponentScan`；② `ServletMappingDemoController`（Spring 对照组，含路径变量与 405 场景）；③ `ServletDemoView` 演示矩阵（真实请求 + 实际命中）；④ 规则顺序图；⑤ 采集截图 |
| 产出 | `/servlet-demo`、`/demo/**` 端点 |
| 依赖 | MS-01 |
| 估算 | 2～3 人日 |
| 出口条件 | ① TC-SERVLET-01～013 通过；② 页面"实际命中"来自真实响应；③ 能口头解释 §7.1 对照表 |
| 风险 | 忘记 `@ServletComponentScan`、`*.do` 冲突 → 启动日志核对；演示路径独立于 `/api` |

### MS-08 测试与缺陷修复（第 8～9 周）

| 项 | 内容 |
| --- | --- |
| 目标 | 按 `test-plan.md` 完成 R1～R5 轮次并修复缺陷 |
| 任务 | ① 跑 L1～L4（含 `LikeEscaperTest`）；② Playwright 10 步 + 补充场景（E2E-02～07）；③ 安全用例 TC-SEC（含本机监听）；④ 性能记录 TC-PERF；⑤ 缺陷回归；⑥ 回填结果表与附录 A |
| 产出 | 测试记录（真实）、缺陷清单与修复提交 |
| 依赖 | MS-03～MS-07 |
| 估算 | 4～5 人日 |
| 出口条件 | P0 100% 通过、P1 ≥95%、无未修复 P0；AC-21～AC-32 满足 |
| 风险 | 时间不足想"跳过测试" → 先保 P0；未执行项如实标注 |

### MS-09 论文与截图（第 9～10 周）

| 项 | 内容 |
| --- | --- |
| 目标 | 按 `course-paper-outline.md` 完成课程论文 |
| 任务 | ① 第 1 章四节（1.1～1.4）；② 第 2 章 2.1～2.21 全部小节；③ 2.21 系统设计与实现；④ 真实测试数据；⑤ 封面/分工/参考文献；⑥ 图表编号与目录；⑦ 截图统一整理 |
| 产出 | 论文 Word/PDF、`docs/screenshots/` 完整图集 |
| 依赖 | MS-06、MS-07、MS-08 |
| 估算 | 6～8 人日 |
| 出口条件 | ① 2.1～2.21 无缺项；② 每节含概述/代码/效果图；③ 截图与代码一致；④ 测试章节引用真实记录；⑤ 查重与格式自检 |
| 风险 | 截图为旧版本 → 冻结代码后统一重截 |

### MS-10 验收与答辩准备（第 10～11 周）

| 项 | 内容 |
| --- | --- |
| 目标 | 现场可演示、可回答问题 |
| 任务 | ① 从零按 README 复现一遍；② 准备演示脚本（5 分钟版/10 分钟版）；③ 准备问题清单（盒模型、定位、MVC、Servlet 映射、解析规则、事务、LIKE 转义、无认证边界）；④ 备份数据库与演示数据；⑤ `netstat` 复核仅本机监听；⑥ 代码冻结 |
| 产出 | 演示脚本、答辩 Q&A、可运行环境 |
| 依赖 | MS-08、MS-09 |
| 状态 | ✅ **COMPLETED（阶段已关闭）**；MS-10 阶段时项目整体为 NOT READY |
| 估算 | 2 人日 |
| 出口条件 | 原计划 AC-01～AC-32 全部满足；MS-10 阶段时为 NOT READY（D08/AC-30；CI P2），当前为条件性验收 |

---

## 3. 排期汇总

| 周次 | 里程碑 | 主要工作 | 可并行 |
| --- | --- | --- | --- |
| 第 1 周 | MS-00 ✅ | 设计文档 8 份 | — |
| 第 1 周 | MS-00B ✅ | P0 设计修订（F-01～F-10） | — |
| 第 1 周 | MS-00C ✅ | 第二轮遗留修复（姓名/40003/isEdited/账号），v1.1-final 冻结 | — |
| 第 2 周 | MS-01 | 工程骨架、数据库、代理 | — |
| 第 2～3 周 | MS-02 ✅ | 解析器 + 单测 | — |
| 第 3～4 周 | MS-03 ✅ | 后端 CRUD + 事务 + MySQL 集成 | — |
| 第 4～5 周 | MS-04 | 录入主流程前端 | MS-06 可启动 |
| 第 5～6 周 | MS-05 | 列表/搜索/批量 | MS-06、MS-07 |
| 第 6～7 周 | MS-06 | Web 实践页 2.1～2.19 | MS-05、MS-07 |
| 第 7 周 | MS-07 | Servlet 演示 2.20 | MS-06 |
| 第 8～9 周 | MS-08 | 全量测试与修复 | — |
| 第 9～10 周 | MS-09 | 论文与截图 | — |
| 第 10～11 周 | MS-10 | 验收与答辩 | — |

**关键路径**：MS-01 → MS-02 → MS-03 → MS-04 → MS-05 → MS-08 → MS-09 → MS-10。
**可并行支线**：MS-06（Web 实践）与 MS-07（Servlet 演示）不依赖业务数据，适合在业务开发受阻时推进。

---

## 4. 小组分工建议

| 角色 | 职责 | 对应里程碑 |
| --- | --- | --- |
| 组长 / 架构 | 接口与数据契约、代码审查、进度与合并 | MS-00～MS-03、MS-10 |
| 后端 A | 解析器 + 单测 | MS-02 |
| 后端 B | CRUD、事务、异常、接口测试 | MS-03、MS-08 |
| 前端 A | 录入页 + 预览表 + API 层 | MS-04 |
| 前端 B | 列表/搜索/批量 + 表单弹窗 | MS-05 |
| 实践页负责人 | 2.1～2.19 + 2.18 线框 | MS-06 |
| Servlet 负责人 | 2.20 + 对照 Controller | MS-07 |
| 论文统筹 | 章节分工、图表、格式、查重 | MS-09 |
| 测试负责人 | 用例执行、记录、缺陷跟踪 | MS-08 |

> 人数少于 5 时合并角色，但**解析器负责人与论文 2.21 负责人建议分开**，避免自测自写。

---

## 5. 交付物清单

| 交付物 | 状态 | 说明 |
| --- | --- | --- |
| `docs/requirements.md` | ✅ 已完成 | 第一阶段 |
| `docs/architecture.md` | ✅ 已完成 | 第一阶段 |
| `docs/database-design.md` | ✅ 已完成 | 第一阶段 |
| `docs/api-design.md` | ✅ 已完成 | 第一阶段 |
| `docs/parser-design.md` | ✅ 已完成 | 第一阶段 |
| `docs/test-plan.md` | ✅ 已完成 | 第一阶段（v1.1-final，无执行结果） |
| `docs/development-roadmap.md` | ✅ 已完成 | 第一阶段（v1.1-final） |
| `docs/course-paper-outline.md` | ✅ 已完成 | 第一阶段（v1.1-final） |
| `docs/review/p0-finalization-report.md` | ✅ 已完成 | 第二轮遗留修复报告 |
| `.gitignore` | ✅ 已完成 | P0 修订（F-10） |
| `README.md` | ✅ 已完成 | P0 修订（F-10） |
| 后端工程 | ✅ 已完成 | MS-01～MS-03 |
| 前端工程 | ✅ 已完成 | MS-01、MS-04、MS-05 |
| Web 实践页 | ✅ 已完成 | MS-06 |
| Servlet 演示 | ✅ 已完成 | MS-07 |
| 测试记录 | ⚠ MS-08 已执行但 D08 P1 待处置 | MS-08；AC-30 PARTIAL；CI 未运行 |
| 论文 | ✅ MS-09 COMPLETED（四图/目录/Word/PDF） | 封面 USER DEFERRED |
| 论文收尾 | ✅ MS-12 完成四图/TOC/Word/PDF | 项目整体建议条件性验收 |
| 最终回归 | ✅ MS-13 完成 160 tests/build/Chrome/Edge/AC | D08/AC-30 未关闭；正式签字未完成 |
| 验收材料 | ✅ MS-10 COMPLETED（阶段关闭） | 文档/CRUD/5-10/分页完成；当时 NOT READY |
| `docs/screenshots/` | ✅ 2.1～2.20 已采集 | MS-06/MS-07 |

> **项目整体：CONDITIONALLY ACCEPTED（D08/AC-30 USER DEFERRED；正式签字未完成）。** MS-09～MS-13 均已完成阶段收尾；CI P2 NOT RUN；不得写成无条件 ACCEPTED。

---

## 6. 完成定义（Definition of Done）

一个功能只有同时满足下列条件才算"完成"：

1. 代码实现并通过 `mvn test` / `npm run build`。
2. 有对应的测试用例（P0 必测）。
3. 接口与 `api-design.md` 一致；若有变更，文档已同步。
4. 前端有加载态、空态、错误态。
5. 日志无敏感信息。
6. 能现场演示（不是"理论上可以"）。
7. 截图已入库（如影响论文）。

---

## 7. Git 与协作规范

| 项 | 约定 |
| --- | --- |
| 分支 | `main`（可运行）、`feat/*`、`fix/*`；合并前自查 |
| 提交信息 | `feat(parser): 支持全角数字`、`fix(list): 删除后页码回退` |
| 提交粒度 | 一个可运行的小步；不提交半成品到 `main` |
| 忽略文件 | `.gitignore` 必须含 `application-local.yml`、`.env`、`.env.*`、`target/`、`node_modules/`、`dist/`、`test-results/`、`playwright-report/`、`*.log`、`.idea/`、`.vscode/`；保留 `.env.example` 与 `application-local.example.yml` |
| 代码审查 | 至少 1 人 review；重点看 Controller 是否越界、`parser` 是否被污染、SQL 是否参数化、关键词是否转义、`isEdited` 是否误入库 |
| 密钥 | 禁止入库；`.env` 与 `application-local.yml` 只在本地 |

---

## 8. 风险与缓冲

| 风险 | 概率 | 影响 | 应对 |
| --- | --- | --- | --- |
| 解析规则反复调整 | 中 | 拖慢 MS-03/04 | 用例先行，规则冻结在 MS-02 出口 |
| MySQL 环境问题 | 中 | 演示受阻 | MS-01 就强制跑通；备 `data.sql` |
| 前端页面耗时超预期 | 中 | 论文截图延后 | 实践页与业务页并行；先结构后美化 |
| 论文写作集中到最后 | 高 | 质量下降 | 每完成一节即写 2.x 草稿并截图 |
| 范围膨胀 | 中 | 核心做不完 | 以 `requirements.md` §5.2 为红线，新增需求记 Backlog |
| 测试时间不足 | 中 | 无法证明质量 | P0 先跑；未执行如实记录 |
| 组员投入不一致 | 中 | 进度不均 | 按里程碑拆分，接口先定，减少互相等待 |

**缓冲**：在第 8 周预留 2 天机动；第 10 周预留 1 天处理格式与截图返工。

---

## 9. 下一步（进入第二阶段前需要确认）

1. 确认 `requirements.md` §8 的 5 个开放问题（尤其 Q-01 脱敏手机号、Q-03 部署形态）。
2. 确认 2.18 仿制对象（默认任务书附录4 的学校首页）。
3. 确认小组分工与周次安排（本文档给相对周次，具体日期由小组填）。
4. 确认后按 MS-01 开始搭建工程骨架：先提交 v1.1-final、创建业务数据库账号，再建工程；**解析器先行**，再写业务 CRUD，最后前端联调。

---

## 10. 变更记录

| 版本 | 日期 | 变更 | 说明 |
| --- | --- | --- | --- |
| v1.0 | 2026-11-14 | 11 个里程碑、排期、分工、完成定义定稿 | 第一阶段交付；后续按实际执行情况更新状态列 |
| v1.1 | 2026-11-14 | 新增 MS-00B（P0 修订已完成）；MS-01～MS-05、MS-08、MS-10 纳入 F-01～F-10 工作项与验收 | 仍未编写业务代码 |
| v1.1-final | 2026-11-14 | 新增 MS-00C（第二轮遗留修复已完成）；MS-01 增加业务账号、MS-02 姓名 1～50、MS-03 201→40003、MS-04 `toPayload`；交付物增加修复报告 | 仍未编写业务代码 |
| v1.1-final-ms01 | 2026-11-14 | MS-01 实际落地后调整：root 本机连接、健康检查出口条件、`admin-init.sql` 降级为可选方案 | 未进入 MS-02 |
| v1.1-final-ms02.1 | 2026-11-14 | MS-02.1 缺陷修复：候选回退、包裹符空白、E108、Unicode 码点、英文地址歧义、parse(null)、错误顺序、解析 API 输入上限；新增修复报告 | 测试 93 个全通过；未进入 MS-03 CRUD |
| v1.1-final-ms03 | 2026-11-14 | MS-03：Entity/Mapper XML、分页搜索、CRUD、批量保存/删除、MySQL 集成测试 | 137 个测试；Vue 未开发 |
| v1.1-final-ms03.1 | 2026-11-14 | MS-03.1：分页 JSON 类型、keyword 码点上限、page/size 字段映射、原始电话/全角姓名、API 层事务回滚 | 146 个测试；MS-04 准入 |
