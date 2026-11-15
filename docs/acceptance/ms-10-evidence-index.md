# MS-10 证据索引

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 状态词 | EXISTS / NEEDS_CAPTURE / BLOCKED / NOT APPLICABLE |
| 原则 | 只列真实存在证据；不伪造截图/测试结果 |

---

## 1. 界面截图

| 证据 | 路径 | 状态 | 说明 |
| --- | --- | --- | --- |
| 2.1 行内 | `docs/screenshots/2-01-inline.png` | EXISTS | MS06 真实截图 |
| 2.2 区块 | `docs/screenshots/2-02-block.png` | EXISTS | 含 margin 对照 |
| 2.3 inline-block | `docs/screenshots/2-03-inline-block.png` | EXISTS | — |
| 2.4 Flex | `docs/screenshots/2-04-flex.png` | EXISTS | — |
| 2.5 Grid | `docs/screenshots/2-05-grid.png` | EXISTS | — |
| 2.6 盒模型 | `docs/screenshots/2-06-box-parts.png` | EXISTS | — |
| 2.7 content-box | `docs/screenshots/2-07-content-box.png` | EXISTS | — |
| 2.8 border-box | `docs/screenshots/2-08-border-box.png` | EXISTS | — |
| 2.9 常规流 | `docs/screenshots/2-09-normal-flow.png` | EXISTS | — |
| 2.10 relative | `docs/screenshots/2-10-relative.png` | EXISTS | — |
| 2.11 absolute | `docs/screenshots/2-11-absolute.png` | EXISTS | — |
| 2.12 定位上下文 | `docs/screenshots/2-12-absolute-context.png` | EXISTS | — |
| 2.13 fixed | `docs/screenshots/2-13-fixed.png` | EXISTS | — |
| 2.14 sticky | `docs/screenshots/2-14-sticky.png` | EXISTS | — |
| 2.15 sticky 约束 | `docs/screenshots/2-15-sticky-rect.png` | EXISTS | — |
| 2.16 offsets | `docs/screenshots/2-16-offsets.png` | EXISTS | — |
| 2.17 覆盖 | `docs/screenshots/2-17-offset-override.png` | EXISTS | 另有 stacking 补充 |
| 2.18 原网页参考 | `docs/screenshots/2-18-original.png` | EXISTS | 来源任务书附录4 |
| 2.18 线框稿 | `docs/screenshots/2-18-wireframe-plan.png` | EXISTS | 本项目 |
| 2.18 实现 | `docs/screenshots/2-18-implemented.png` | EXISTS | 本项目 |
| 2.19 布局原则 | `docs/screenshots/2-19-principles.png` | EXISTS | 真 AppLayout |
| 2.20 映射演示 | `docs/screenshots/2-20-servlet-mapping.png` | EXISTS | 真实请求 |
| 2.20 映射示意 | `docs/screenshots/2-20-mapping-flow.png` | EXISTS | 自绘，不是运行截图 |
| 2.21 录入 | `docs/screenshots/2-21-import.png` | EXISTS | MS09 真实 |
| 2.21 解析 | `docs/screenshots/2-21-parse-result.png` | EXISTS | MS09 真实 |
| 2.21 保存确认 | `docs/screenshots/2-21-edit-preview.png` | EXISTS | MS09 真实 |
| 2.21 地址列表 | — | **BLOCKED** | MySQL 未启动 |
| 2.21 编辑弹窗 | — | **BLOCKED** | 同上 |
| 2.21 删除确认 | — | **BLOCKED** | 同上 |
| 2.21 批量删除 | — | **BLOCKED** | 同上 |
| 2.21 测试记录 | `docs/screenshots/2-21-test-record.png` | EXISTS | 真实 Maven 160 日志整理 |

---

## 2. 测试与性能证据

| 证据 | 路径 | 状态 |
| --- | --- | --- |
| 测试摘要 | `docs/test-results/ms-08/summary.md` | EXISTS |
| 安全记录 | `docs/test-results/ms-08/security-checks.md` | EXISTS（有一处 158 旧表述） |
| 性能原文 | `docs/test-results/ms-08/perf-results.json` | EXISTS |
| 兼容性 | `docs/test-results/ms-08/browser-compatibility.md` | EXISTS |
| E2E | `docs/test-results/ms-08/core-e2e.md` | EXISTS |
| MS-08 最终核验 | `docs/review/ms-08-final-verification-report.md` | EXISTS |
| MS-08 缺陷台账 | `docs/review/ms-08-defect-ledger.md` | EXISTS |
| MS-09 交付报告 | `docs/review/ms-09-final-delivery-report.md` | EXISTS |
| MS-09 Word/PDF | `deliverables/MS-09/` | EXISTS |
| MS-10 演练 | `docs/test-results/ms-10/demo-rehearsal-report.md` | EXISTS |

---

## 3. 架构与数据库证据

| 证据 | 来源 | 状态 |
| --- | --- | --- |
| Mermaid 架构 | `README.md` §5 | EXISTS |
| 表结构 | `backend/src/main/resources/db/schema.sql` | EXISTS |
| API 契约 | `docs/api-design.md` | EXISTS |
| 解析规则 | `docs/parser-design.md` | EXISTS |
| 数据库设计 | `docs/database-design.md` | EXISTS |

---

## 4. 答辩材料

| 材料 | 路径 | 状态 |
| --- | --- | --- |
| 5/10 分钟脚本 | `docs/acceptance/ms-10-demo-script.md` | EXISTS |
| 46 题 Q&A | `docs/acceptance/ms-10-defense-qa.md` | EXISTS |
| 故障手册 | `docs/acceptance/ms-10-troubleshooting.md` | EXISTS |
| 环境清单 | `docs/acceptance/ms-10-environment-checklist.md` | EXISTS |
| 复现手册 | `docs/acceptance/ms-10-reproduction-guide.md` | EXISTS |
| 数据安全 | `docs/acceptance/ms-10-data-safety.md` | EXISTS |
| 验收矩阵 | `docs/acceptance/ms-10-acceptance-matrix.md` | EXISTS |

---

## 5. 缺失/阻塞

- 地址列表、编辑、删除、批量删除的真实运行截图：**BLOCKED**；
- MySQL 数据库健康/完整业务演示：**BLOCKED**（本轮未启动且禁止启动）；
- MS-09 封面信息、PDF 目录：待补，不属于 MS-10 擅自修改范围；
- D08 处置证据：不存在，P1 OPEN；
- GitHub Actions CI：NOT RUN。

**本索引不把 BLOCKED 写成 EXISTS。**
