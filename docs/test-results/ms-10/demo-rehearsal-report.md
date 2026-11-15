# MS-10 演示演练记录

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 环境 | Windows 11 / JDK 17.0.21 / Node 22.14.0 / Chrome 157 / Edge 157 |
| 数据库 | MySQL 8.0.43，`express_address_book`，初始 0 行 |
| 启动方式 | 用户授权后启动 `mysqld`；后端 8080、前端 5173 |
| 结论 | **CRUD 与 5/10 分钟核心步骤实际执行通过；本记录保留真实耗时** |

---

## 1. 连接检查

| 检查 | 结果 |
| --- | --- |
| MySQL 3306 | `127.0.0.1:3306` LISTENING |
| `GET /api/health` | 200 |
| `GET /api/health/db` | 200，`status=UP` |
| 数据库 | `express_address_book` |
| 表 | `express_address` |
| 初始行数 | 0 |

## 2. CRUD 证据

`docs/test-results/ms-10/api-crud-evidence.json`：

| 操作 | 结果 |
| --- | --- |
| 新增 3 条 | 200，id=1/2/3 |
| 列表 page1 size2 | 200 |
| 姓名/电话搜索 | 200，命中 |
| 编辑 id1 | 200，DB 地址更新 |
| 单删 id3 | 200；后查 404；DB `is_deleted=1` |
| 批量新增/删除 id4/5 | 200；DB `is_deleted=1` |

分页证据：

- API page1 size2 / page2 size2：`pagination-evidence.json`；
- UI 11 条：total=11，第 1 页 10 行，第 2 页 1 行。

## 3. 5 分钟演练

实际 UI 脚本 `docs/test-results/ms-10/run-demo.mjs`：

1. 3 行固定数据 → 3 行解析（2 有效/1 无效）；
2. 修正无效行；
3. 保存 3 条；
4. 搜索 `MSFIVE`，3 行；
5. 编辑首条；
6. 删除首条，剩 2 行。

实际脚本耗时约 **9.2 秒**；不是现场讲解的 5 分钟。

## 4. 10 分钟演练

1. 完整 5 行 → 5 行解析（3 有效/2 无效）；
2. 修正 2 条无效行；
3. 保存 5 条；
4. 搜索 `MSTEN`，5 行；
5. 新建 `MSTENNEW`；
6. 编辑新建记录；
7. 单删新建记录；
8. 当前页批量删除 2 条，剩 3 行；
9. `/web-basics` 加载；
10. `/servlet-demo` 加载；
11. 分页通过 11 条 UI 实测（total 11，page2 1 行）。

实际脚本累计约 **23.5 秒**；不是现场讲解的 10 分钟。

## 5. 数据清理

本轮主键：

- CRUD：1、2、3、4、5；
- 5/10 UI：15～23；
- API 分页：24、25、26；
- UI 分页：27～37。

清理前保存 `db-state-before-cleanup.txt`；按主键定向 DELETE，最终：

```text
SELECT COUNT(*) FROM express_address;  → 0
```

## 6. 证据

- `docs/test-results/ms-10/api-crud-evidence.json`
- `docs/test-results/ms-10/pagination-evidence.json`
- `docs/test-results/ms-10/pagination-ui-ids.json`
- `docs/test-results/ms-10/ui-demo-evidence.json`
- `docs/test-results/ms-10/db-state-before-cleanup.txt`
- `docs/test-results/ms-10/five-*.png`、`ten-*.png`
- `docs/test-results/ms-10/run-demo.mjs`

## 7. 未执行/限制

- 未对 MySQL 配置/插件/账号/防火墙做任何修改；
- 未 DROP/TRUNCATE/全表 DELETE；
- D08 仍 P1 OPEN；
- AC-30 仍 PARTIAL；
- MS-09 仍 PARTIAL；
- CI NOT RUN；
- 项目正式验收仍未达到。
