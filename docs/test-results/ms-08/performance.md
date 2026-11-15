# MS-08 性能测试记录

脚本：`frontend/scripts/ms08-perf-check.mjs`
原始数据：`docs/test-results/ms-08/perf-results.json`

环境：

- Node v22.14.0
- Intel Core Ultra 7 265K，20 CPU
- 32 GB 内存
- MySQL 8.0.43
- 隔离 Schema，10,000 条虚构地址
- 预热 2 次，测量 30 次

结果（2026-11-15 复核重跑，30 个原始样本见 `perf-results.json`）：

| 场景 | 目标 | 平均 | 中位数 | P95 | 最大 | 结论 |
| --- | ---: | ---: | ---: | ---: | ---: | --- |
| 100 行解析（HTTP） | ≤300ms | 2.29ms | 2.12ms | 2.81ms | 3.88ms | PASS |
| 10,000 条分页查询（HTTP） | ≤500ms | 3.12ms | 3.10ms | 3.59ms | 3.84ms | PASS |

独立复核：

- 30 个样本，无重复；
- 预热 2 次未计入；
- P95 采用 nearest-rank；中位数为中间两值平均；
- 原始样本已写入 `perf-results.json`；
- 初次汇总保留在 `perf-results-initial.json`，未覆盖。

说明：

- 解析为 HTTP 全链路时间，不是单次 Parser 内部时间；
- 查询为 `page=1&size=10`，真实 MySQL + Spring MVC + JSON；
- 10,000 条均为本轮生成的虚构数据，位于隔离 Schema；
- 未进行无上限压力测试。
