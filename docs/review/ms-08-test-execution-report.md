# MS-08 测试执行报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 阶段 | MS-08.2～MS-08.4 全量测试 |
| 隔离 Schema | `express_address_book_ms08_test` |
| 结论 | L1～L4、核心 E2E、安全/性能/兼容性均已完成；无 P0/P1 |

---

## 1. R1 基线

- Git：`main` / `6dd2bdb`；工作区含 MS-07 未提交实现和 MS-06 R1 历史变更；
- 旧测试库 `express_address_book_test` 有 38 行既有数据，未清理；
- 主库 `express_address_book` 0 行，未写入；
- 新建隔离 Schema `express_address_book_ms08_test`，只写本轮虚构数据。

详细见 `ms-08-pre-audit-report.md`。

---

## 2. R2 后端全量

### 2.1 命令

```text
mvn -B clean test -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/express_address_book_ms08_test
```

### 2.2 结果

```text
Tests run: 158, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
exit 0
```

分层：

| 层级 | 覆盖 |
| --- | --- |
| L1 Parser | 86 个 Parser/解析测试 |
| L2 MockMvc | Controller 契约/异常 |
| L2 真实 HTTP | 真实 Parser/HTTP（含 Servlet 12） |
| L3 MySQL | CRUD、分页、搜索、批量、事务、Unicode、LIKE |
| DTO/Unicode/LIKE/基础 | DTO、LikeEscaper、CodePoint、R/health/异常 |

边界与事务由现有真实 MySQL 测试覆盖；本轮未降低断言。

---

## 3. R3 前端/E2E

实际执行：

```text
MS04_BROWSER_OK
MS05_BROWSER_OK
MS06_2_BROWSER_OK
MS06_3_BROWSER_OK
MS06_4_BROWSER_OK
MS06_5_BROWSER_OK
MS06_6_BROWSER_OK
MS06_7_BROWSER_OK
MS06_8_BROWSER_OK
MS06_9_BROWSER_OK
MS07_3_BROWSER_OK
MS08_E2E_OK
```

核心 E2E 覆盖：

- 3 条虚构联系人解析；
- 保存 3 条；
- 地址簿搜索；
- 编辑并刷新验证；
- 单条删除；
- 剩余两条批量删除；
- 列表清空。

MS04/MS05 均连接隔离 Schema；MS05 的测试前缀数据由其脚本自行清理，没有清理旧测试库或主库。

---

## 4. R4 安全

结果详见 `docs/test-results/ms-08/security-checks.md`。

关键结果：

- 无 `application-local.yml` 被跟踪；
- 无真实口令；
- 无 `v-html`；
- SQL 参数化；
- LIKE 特殊字符测试通过；
- 非法 ID → 400/40001，无堆栈；
- SQL 注入探测 → 200 空结果；
- 后端 127.0.0.1:8080、前端 127.0.0.1:5173、MySQL 127.0.0.1:3306；
- MySQL root 仅 localhost；
- 405 `Allow: GET` 已修复；
- 无登录属于既定范围，未描述为已实现认证。

观察：MySQL X Plugin `33060` 监听 0.0.0.0，非应用端口，记录 P3。

---

## 5. R4 性能

脚本：`frontend/scripts/ms08-perf-check.mjs`

- 隔离 Schema 10,000 条虚构记录；
- 预热 2 次，测量 30 次；
- 100 行解析 P95 **5.33ms**（≤300ms）；
- 10,000 条分页查询 P95 **5.82ms**（≤500ms）。

原始数据：`docs/test-results/ms-08/perf-results.json`。

---

## 6. 浏览器兼容性

- Chrome（本机 Playwright）：全量 MS04/MS05/MS06/MS07 + E2E；
- Edge 1280×720：`/servlet-demo` 基本页面验证通过；
- Edge 未执行完整矩阵，标记 P2；
- 1366/980/760/390 响应式回归通过。

---

## 7. 测试环境问题

1. 第一次 E2E 使用 10 位电话，输入解析为无效行，保存按钮 disabled；改为 11 位虚构电话后通过。属于测试数据问题。
2. MS05 在旧测试库 38 行数据上无法安全运行；改用隔离 Schema 后通过。
3. 405 Allow 修复后，Servlet 和 Spring 405 均返回 `Allow: GET`。

---

## 8. 数据库影响

- 旧测试库 38 行：未改动；
- 主库 0 行：未改动；
- MS-08 隔离 Schema：创建 10,000 条性能虚构记录；
- MS04/MS05/E2E 使用隔离 Schema 的测试数据，测试脚本自行清理；
- 没有执行主库/旧测试库的清理操作。

---

## 9. 未执行

- Edge 完整 MS04～MS07 回归：NOT RUN（P2）；
- CI workflow：NOT RUN（仓库无 `.github/workflows`，未伪造通过）；
- Firefox/Safari：不在 NFR-03 要求内，未测试。

**MS-08.2～08.4 完成，进入缺陷修复与最终回归。**

---

## 10. MS-08-R1 补充核验更新（2026-11-15）

- 后端：新增 HEAD/OPTIONS/Allow 测试，全量 **160 tests** 通过；
- Edge：`ms08-edge-check.mjs` 在 1280/1366/980/760/390 运行 `/import`、`/addresses`、`/web-basics`、`/web-basics/wireframe`、`/servlet-demo`，并执行 Edge 导入保存/搜索删除；`MS08_EDGE_OK`；
- 性能：原始样本已写入 `perf-results.json`；P95 解析 2.81ms、列表 3.59ms；初次汇总保留；
- 405：POST 405 + Allow `GET, HEAD, OPTIONS`；HEAD/OPTIONS 200；自定义 OPTIONS 不再错误广告 POST；
- 安全：发现 MySQL X Plugin 33060 + mysqld 防火墙全端口允许，**P1 D08 OPEN**；AC-30 改为 PARTIAL；
- AC-31 改为 PASS（Edge 完整矩阵）；CI 仍 NOT RUN（P2）。

原 §2 的 158 是 R1 前历史结果；R1 后的实际结果是 160。
