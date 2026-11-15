# MS-10 最终验收矩阵

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 依据 | `docs/test-plan.md`、需求/架构/API/数据库设计、MS-08 证据、MS-09 交付物 |
| 状态词 | PASS / PARTIAL / FAIL / BLOCKED / NOT VERIFIED / NOT APPLICABLE / UNDEFINED |

> AC-01～AC-20 在仓库现有文档中**没有定义**，本矩阵不得猜测其含义，统一记为 UNDEFINED；新增项目内部追踪项使用 `MS10-TRACE-*`，不冒充原始 AC。

---

## 1. 原始 AC-01～AC-20

| 编号 | 验收内容 | 来源 | 验收方式 | 证据 | 状态 | 阻塞原因 |
| --- | --- | --- | --- | --- | --- | --- |
| AC-01～AC-20 | 未定义 | 仓库无定义表 | 无法验收 | 未找到正式定义 | **UNDEFINED** | 需课程/教师提供定义后才能判定 |

不把这些编号映射成 FR 或猜测含义。

---

## 2. AC-21～AC-32（来自 `docs/test-plan.md`）

| 编号 | 验收内容 | 来源 | 验收方式 | 证据 | 状态 | 阻塞原因 |
| --- | --- | --- | --- | --- | --- | --- |
| AC-21 | 地址解析正确性 | FR-01～03、BR-01～08 | Parser 单测、全量 | 160 tests；Parser 用例 | PASS | — |
| AC-22 | 解析 API 输入边界 | FR-02、BR-05、NFR-02 | Controller/HTTP、200/201 限制 | MockMvc/真实 HTTP、边界测试 | PASS | — |
| AC-23 | 地址 CRUD、分页与搜索 | FR-07～13、BR-10～14 | MySQL 集成、MS05 | `AddressApiMysqlTest`、页面回归 | PASS | — |
| AC-24 | 批量事务与服务端校验 | FR-05、FR-19、BR-09/11/12 | 真实 MySQL 事务 | 批量回滚/201 拒绝用例 | PASS | — |
| AC-25 | 批量录入端到端流程 | FR-01～06 | MS04、E2E | `MS04_BROWSER_OK`、`MS08_E2E_OK` | PASS | — |
| AC-26 | 地址簿端到端流程 | FR-07～14 | MS05、E2E | `MS05_BROWSER_OK`、`MS08_E2E_OK` | PASS | — |
| AC-27 | Web 基础实践回归 | FR-15、FR-16 | MS06.2～06.9 | `MS06_*_BROWSER_OK` | PASS | — |
| AC-28 | Servlet URI 映射回归 | FR-17、课程 2.20 | 后端 12+浏览器 | `MS07_3_BROWSER_OK`；HEAD/OPTIONS | PASS | — |
| AC-29 | API 契约与异常处理 | FR-18、FR-19 | 统一 R/violations、异常状态 | 160 tests、安全探测 | PASS | — |
| AC-30 | 安全与数据保护 | FR-20、BR-16/20/22、NFR-05/11 | 监听/账号/错误响应 | R1/R2 报告、D08 | **PARTIAL** | D08 P1 OPEN：33060+防火墙 |
| AC-31 | 性能与浏览器兼容 | NFR-01、NFR-03 | 性能、Chrome、Edge | 30 样本；`MS08_EDGE_OK` | PASS | — |
| AC-32 | 工程质量与交付复现 | NFR-06～10、DoD | build/160 tests/脚本/证据 | 本地通过 | PASS（CI NOT RUN） | 无 GitHub Actions 执行证据 |

---

## 3. 课程/项目内部追踪项（不是原始 AC）

| 编号 | 验收内容 | 来源 | 验收方式 | 证据 | 状态 | 阻塞原因 |
| --- | --- | --- | --- | --- | --- | --- |
| MS10-TRACE-01 | 课程任务（一）范围 | 任务书 | 文档核对 | 论文/README/MS-01 报告 | PASS | — |
| MS10-TRACE-02 | 需求 FR 覆盖 | `requirements.md` | 追踪/测试 | AC-21～32、MS-08 | PASS | — |
| MS10-TRACE-03 | 核心业务功能 | FR-01～19 | 代码/浏览器 | MS04/05/06/07/08 | PASS | — |
| MS10-TRACE-04 | 后端 API | `api-design.md` | Controller/测试 | 2 健康 + 8 地址接口 | PASS | — |
| MS10-TRACE-05 | 数据库设计 | `database-design.md` | Schema/集成测试 | `schema.sql`、MySQL 测试 | PASS | — |
| MS10-TRACE-06 | 前端交互 | MS04/05 页面 | Playwright | `MS04/MS05_BROWSER_OK` | PASS | — |
| MS10-TRACE-07 | Web 2.1～2.19 | 课程任务书 | 真实页面 | MS06.2～06.9 | PASS | — |
| MS10-TRACE-08 | Servlet 2.20 | 课程任务书 | 真实请求 | MS07/12 后端/浏览器 | PASS | — |
| MS10-TRACE-09 | 自动化测试 | test-plan | Maven/Playwright | 160 tests、11 个脚本 | PASS | — |
| MS10-TRACE-10 | 浏览器兼容 | NFR-03 | Chrome/Edge | Chrome 全量、Edge 矩阵 | PASS | — |
| MS10-TRACE-11 | 性能 | NFR-01 | 真实 HTTP/30 样本 | P95 2.81/3.59ms | PASS | — |
| MS10-TRACE-12 | 安全边界 | NFR-05/11 | 只读核验 | 无认证、本机监听、D08 | **PARTIAL** | D08 P1 OPEN |
| MS10-TRACE-13 | 论文与交付 | MS-09 | 文件/渲染检查 | Word/PDF、截图 | **PARTIAL** | 封面、2.21 四图、PDF 目录 |
| MS10-TRACE-14 | 答辩演示 | MS-10 | 实际演练 | 本轮环境验证 | **BLOCKED** | MySQL 未启动且禁止启动，DB 步骤不可执行 |

---

## 4. 统计

| 状态 | 数量/范围 |
| --- | --- |
| PASS | AC-21～29、AC-31、AC-32；MS10-TRACE-01～11 |
| PARTIAL | AC-30；MS10-TRACE-12、13 |
| BLOCKED | MS10-TRACE-14（DB 演示步骤） |
| UNDEFINED | AC-01～AC-20 |
| FAIL | 0（本轮未发现新的功能失败） |

**结论：** 课程功能与本地测试证据较完整，但 AC-30 未关闭、MS-09 未完成、MS-10 实际全流程演示受 MySQL 限制；**项目尚未达到正式验收条件**。
