# MS-13 验收矩阵

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| HEAD | `8a8f4b8` |
| 依据 | requirements、test-plan、课程任务书、MS-08～MS-12 证据、MS-13 回归 |
| 状态词 | PASS / PARTIAL / FAIL / BLOCKED / NOT RUN / USER DEFERRED / UNDEFINED |

---

## 1. AC-01～AC-20

仓库现有文档只定义了 AC-21～AC-32；`development-progress.md` 明确 AC-01～AC-20 没有定义表。

| 编号 | 验收内容 | 优先级 | 依据 | 历史状态 | 本轮 | 证据 | 结论 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AC-01～AC-20 | 未定义 | — | 无正式定义 | UNDEFINED | **UNDEFINED** | 无定义表 | 不猜测、不判 PASS |

---

## 2. AC-21～AC-32

| 编号 | 验收内容 | 优先级 | 依据 | 历史状态 | 本轮结果 | 证据 | 结论 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| AC-21 | 地址解析正确性 | P0 | FR-01～03、BR-01～08 | PASS | PASS | 本轮 Maven 160 | 满足 |
| AC-22 | 解析 API 输入边界 | P0 | FR-02、BR-05、NFR-02 | PASS | PASS | Controller/HTTP 测试 | 满足 |
| AC-23 | CRUD/分页/搜索 | P0 | FR-07～13、BR-10～14 | PASS | PASS | MySQL 集成/MS05 | 满足 |
| AC-24 | 批量事务/服务端校验 | P0 | FR-05、FR-19 | PASS | PASS | 160 tests | 满足 |
| AC-25 | 批量录入 E2E | P0 | FR-01～06 | PASS | PASS | MS04/MS08_E2E | 满足 |
| AC-26 | 地址簿 E2E | P0 | FR-07～14 | PASS | PASS | MS05/MS08_E2E | 满足 |
| AC-27 | Web 2.1～2.19 | P0 | FR-15/16 | PASS | PASS | MS06 2～9 | 满足 |
| AC-28 | Servlet 2.20 | P0 | FR-17、课程 2.20 | PASS | PASS | 14 Servlet tests/MS07 | 满足 |
| AC-29 | API 契约/异常 | P0 | FR-18/19 | PASS | PASS | 160 tests | 满足 |
| AC-30 | 安全与数据保护 | P1 | FR-20、NFR-05/11 | PARTIAL | **PARTIAL** | D08 | 未满足（D08） |
| AC-31 | 性能与浏览器兼容 | P1 | NFR-01/03 | PASS | PASS（性能历史+本轮浏览器） | perf JSON/Chrome/Edge | 满足（性能未重跑） |
| AC-32 | 工程质量/复现 | P1 | NFR-06～10 | PASS（CI P2） | PASS（本地；CI NOT RUN） | build/160/证据 | 满足本地；CI 非硬性 |

---

## 3. 课程任务书与项目推导 AC 的边界

- 课程任务书未出现 GitHub/Actions/CI；CI 不是硬性要求；
- AC-21～AC-32 是项目推导/验收矩阵，不是任务书原文编号；
- 项目自定义 AC 不能冒充课程原文；
- D08/AC-30 是项目风险，不是课程功能失败，但仍影响项目正式验收。

---

## 4. 结论

- P0 功能：全部通过；
- 课程 Web/Servlet 实践：通过；
- 论文/截图：MS-09/MS-12 已完成；
- 唯一业务验收未闭合项：**AC-30 PARTIAL / D08 P1 OPEN**；
- CI：NOT RUN / P2，不是硬性；
- 项目建议：**CONDITIONALLY ACCEPTED — SUBJECT TO D08 / AC-30 USER-DEFERRED RISK**，不是正式签字验收。
