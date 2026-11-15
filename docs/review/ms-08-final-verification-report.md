# MS-08-R1 补充核验与最终复核报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 范围 | MySQL X 33060、Edge 完整矩阵、性能原始样本、405 HEAD/OPTIONS、AC-30/31/32、Git |
| 结论 | **MS-08 暂不能正式结束：发现 1 个未关闭 P1（D08）** |

---

## 1. 补充核验范围

本次只做补充核验和必要最小修复：

- 33060/X Plugin/防火墙；
- Edge 真实浏览器矩阵；
- 性能原始样本；
- 405 Allow 与 HEAD/OPTIONS；
- AC-30/31/32 与 Git；
- 更新 MS-08 材料。

没有重新实施 MS-08，没有修改主库/旧测试库，没有修改 MySQL 配置、账号、密码或防火墙。

---

## 2. MySQL X Plugin 33060 与防火墙

### 2.1 事实

```text
mysqlx plugin: ACTIVE
mysqlx_bind_address: *
mysqlx_port: 33060
3306 listening: 127.0.0.1:3306
33060 listening: [::]:33060 (all interfaces)
```

MySQL 账号：

```text
root@localhost
mysql.infoschema@localhost
mysql.session@localhost
mysql.sys@localhost
```

没有 `root@'%'`。

防火墙：

- Domain/Private/Public 状态 ON；
- 策略 `BlockInbound,AllowOutbound`；
- 但存在 **mysqld 入站允许规则**：
  - TCP/UDP；
  - LocalPort Any；
  - RemoteAddress Any；
  - Profile Private/Public；
  - Program `E:\mysql\install_directory\bin\mysqld.exe`。

### 2.2 可达性

- 本机 `127.0.0.1:33060` 可连接；
- 本机 LAN 地址 `192.168.1.2:33060` 可建立 TCP 连接；
- 未从第二台物理机器测试（没有可用第二台机器），因此不能宣称“已证明外部可登录”；
- 但 0.0.0.0/全部接口监听 + 防火墙全端口允许，已构成**潜在外部暴露**。

### 2.3 项目是否使用

项目只使用 JDBC 3306；没有 MySQL X Protocol/33060 依赖或配置。

### 2.4 结论

**D08（P1）**：MySQL X Plugin 未使用但暴露在全部接口，且 mysqld 防火墙允许 Any。未修改配置（需用户授权）。

建议：

- 禁用 X Plugin，或设置 `mysqlx_bind_address=127.0.0.1`；
- 删除/收紧 mysqld 的 Any 入站规则；
- 处置后复测 `netstat`、防火墙规则、LAN 可连接性。

**AC-30：PARTIAL**，不能标记 PASS。

---

## 3. Edge 浏览器完整矩阵

脚本：`frontend/scripts/ms08-edge-check.mjs`
结果：`MS08_EDGE_OK`

使用真实 Edge（Playwright `channel: 'msedge'`），不是 Chrome 结果复制。

覆盖：

- `/import`：真实解析、保存；
- `/addresses`：搜索、删除；
- `/web-basics`；
- `/web-basics/wireframe`；
- `/servlet-demo`：真实 exact 请求、实际 handler。

宽度：

```text
1280×720、1366×768、980px、760px、390px
```

所有页面均检查页面级横向溢出；通过。

**AC-31：PASS（性能 + Chrome + Edge 完整矩阵）。**

---

## 4. 性能原始数据复核

脚本：`frontend/scripts/ms08-perf-check.mjs`
原始样本：`docs/test-results/ms-08/perf-results.json`
初次汇总保留：`docs/test-results/ms-08/perf-results-initial.json`

检查：

- 100 行解析确实通过 HTTP POST `/api/addresses/parse`；
- 分页查询确实通过 HTTP GET `/api/addresses?page=1&size=10`；
- 隔离 Schema 中 10,000 条虚构记录；
- 预热 2 次不计入；
- 30 个有效样本；
- 原始样本保留，无重复；
- 不是单次结果冒充 30 次；
- 不是纯 Parser 内部耗时冒充 HTTP。

独立重算（30 样本，nearest-rank P95、中位数平均）：

| 场景 | 平均 | 中位数 | P95 | 最大 | 最小 |
| --- | ---: | ---: | ---: | ---: | ---: |
| 100 行解析 HTTP | 2.29ms | 2.12ms | 2.81ms | 3.88ms | 1.81ms |
| 10,000 条分页查询 | 3.12ms | 3.10ms | 3.59ms | 3.84ms | 2.64ms |

两项均满足目标（300ms/500ms）。

**AC-31 性能部分 PASS。**

---

## 5. 405 Allow 与 HEAD/OPTIONS

### 实际行为（修复后）

自定义 Servlet：

```text
HEAD /demo/servlet/exact       → 200
OPTIONS /demo/servlet/exact    → 200, Allow: GET, HEAD, OPTIONS
POST /demo/servlet/exact       → 405, Allow: GET, HEAD, OPTIONS
```

Spring MVC：

```text
HEAD /demo/spring/info         → 200
OPTIONS /demo/spring/info      → 200, Allow: GET,HEAD,OPTIONS
POST /demo/spring/method-only  → 405, Allow: GET, HEAD, OPTIONS
```

修复前：

- 自定义 OPTIONS 会错误广告 POST（因为覆写 doPost）；
- POST 405 只给 `Allow: GET`，不完整。

修复：

- `MappingDemoServletSupport.service`：GET/HEAD/OPTIONS 允许；其它方法 405 + Allow GET,HEAD,OPTIONS；不再覆写 doPost；
- `GlobalExceptionHandler`：GET 时补 HEAD，补 OPTIONS；不再硬编码所有端点。
- 后端新增 HEAD/OPTIONS 测试；全量变为 **160 tests**。

**状态：D01 CLOSED。**

---

## 6. AC-30 / AC-31 / AC-32 最终状态

| AC | 状态 | 依据 |
| --- | --- | --- |
| AC-30 | **PARTIAL** | X Plugin 33060 + mysqld 防火墙允许；P1 D08 未关闭 |
| AC-31 | **PASS** | 性能原始样本；Chrome；Edge 完整矩阵 |
| AC-32 | **PASS** | 本地 build/Maven/Playwright/E2E 可复现；CI 未运行（P2） |

> AC-32 的 PASS 不等于 GitHub Actions CI 已通过；CI 仍为 NOT RUN。

---

## 7. 新增/更新缺陷

| 编号 | 等级 | 内容 | 状态 |
| --- | --- | --- | --- |
| D01 | P2 | 405 Allow/HEAD/OPTIONS 语义 | CLOSED（160 tests） |
| D05 | P2 | Edge 完整矩阵 | CLOSED（`MS08_EDGE_OK`） |
| D08 | **P1** | MySQL X Plugin 0.0.0.0 + mysqld 防火墙全端口允许 | **OPEN** |

没有已修复但未回归的 P1。

---

## 8. 修复和回归证据

```text
mvn -B clean test -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/express_address_book_ms08_test
Tests run: 160, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

MS07_3_BROWSER_OK
MS08_EDGE_OK
```

- 160 = 原 158 + HEAD/OPTIONS 两个测试；
- 正常 GET/映射优先级未改变；
- 没有降低断言；
- 没有修改业务 API/数据库。

---

## 9. Git 工作区状态

```text
branch: main
HEAD: ef97913（MS07，外部提交）
MS-08.1 初始审计 HEAD: 6dd2bdb
```

MS-08 的修改、测试、文档和证据均为本地未提交；没有 commit/push/reset/clean/checkout/stash。  
`docs/test-results/ms-08/` 通过 `.gitignore` 例外保留在本轮工作区。

`git diff --check` 通过；AGENTS 硬链接已重建并 SHA256 一致。

---

## 10. 最终结论建议

**MS-08 暂不能正式结束。**

原因：D08（P1）— MySQL X Plugin 33060 对全部接口监听，mysqld 防火墙允许 Any，项目不使用 X Protocol；虽未从第二台机器验证登录，但潜在外部暴露未解除。AC-30 为 PARTIAL。

待用户明确授权并处置以下之一后复审：

1. 禁用 MySQL X Plugin；或
2. 设置 `mysqlx_bind_address=127.0.0.1`；并
3. 删除/收紧 mysqld 的 Any 入站规则。

处置后复测 33060、防火墙、LAN 可达性，AC-30 才能 PASS。

**不进入 MS-09，不自行宣布 MS-08 正式通过。**

---

## 11. MS-08-R2 人工处置状态（2026-11-15）

人工风险与处置指南已新增：

[`docs/security/ms-08-mysqlx-risk-and-manual-remediation.md`](../security/ms-08-mysqlx-risk-and-manual-remediation.md)

当前状态：

- D08：**P1 OPEN**；
- AC-30：**PARTIAL**；
- MySQL 配置、X Plugin、账号、服务：**未修改**；
- Windows 防火墙：**未修改**；
- MySQL 未重启/停止/启动；
- 未执行任何改变数据库状态的 SQL；
- R2-01～R2-08：**PENDING**（等待用户人工处置和下一次授权）。

**MS-08 暂不能正式结束；MS-09 暂不启动。**
