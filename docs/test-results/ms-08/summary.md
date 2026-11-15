# MS-08 测试执行摘要

| 项目 | 结果 |
| --- | --- |
| 日期 | 2026-11-15 |
| 隔离 Schema | `express_address_book_ms08_test` |
| 主库 | `express_address_book`，0 行，未写入 |
| 旧测试库 | `express_address_book_test`，38 行，未清理 |
| 后端全量 | 160 tests，0 failures/errors/skips，BUILD SUCCESS |
| 前端构建 | `npm run build`，exit 0 |
| MS07 | `MS07_3_BROWSER_OK` |
| MS04 | `MS04_BROWSER_OK` |
| MS05 | `MS05_BROWSER_OK`（隔离 Schema） |
| MS06.2～06.9 | 全部 BROWSER_OK |
| 核心 E2E | `MS08_E2E_OK` |
| Edge 完整矩阵 | `MS08_EDGE_OK`（5 宽度/5 页面 + 核心导入保存/搜索删除） |
| 性能 | P95 解析 2.81ms；P95 列表 3.59ms |
| 浏览器 | Chrome 全量；Edge 完整矩阵通过 |
| CI | NOT RUN（未创建 workflow） |
| 安全 | MySQL X 33060 暴露，**P1 OPEN**；AC-30 PARTIAL |

本次测试没有把历史 146/158 结果当成本次执行；全量 Maven 使用隔离 Schema 实际重跑 160（新增 HEAD/OPTIONS 语义测试）。

## 测试命令

```text
mvn -B clean test -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/express_address_book_ms08_test
node scripts/ms07-3-browser-check.mjs
node scripts/ms04-browser-check.mjs
node scripts/ms05-browser-check.mjs
node scripts/ms06-2-browser-check.mjs ... ms06-9
node scripts/ms08-e2e-check.mjs
node scripts/ms08-perf-check.mjs
npm run build
```

## 边界

- L1 Parser / L2 MockMvc / L2 真实 HTTP / L3 MySQL / L4 Playwright 分开；
- 404/405 是有意教学状态，未改成 200；
- 405 Allow 已修复并验证；
- 数据库清理只针对本轮隔离 Schema 的虚构数据。
