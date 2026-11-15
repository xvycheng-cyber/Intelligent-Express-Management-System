# MS-10-R2 文档一致性整改与数据库演示报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 分支 / HEAD | `main` / `a17e9942a91d5cde67f8a9c4bfafd9b254f97988` |
| 范围 | P2/P3 整改、MySQL 实际连接、CRUD、5/10 分钟演示、数据清理 |
| 结论 | 文档整改完成；数据库/CRUD/5/10 分钟核心流程实际执行通过；项目正式验收仍受 D08/MS-09/CI 限制 |

---

## 1. P2/P3 整改

| 编号 | 原问题 | 修改 |
| --- | --- | --- |
| P2-01 | AC-28 写后端 12 | 矩阵与 test-plan 改为 **14** |
| P2-02 | 5/10 分钟有效数量不一致 | 5 分钟固定 3 行（2/1），10 分钟固定 5 行（3/2）；实际 API/UI 验证 |
| P2-03 | 最终报告 HEAD/工作区过期 | 标记“报告撰写时快照”，另注当前 HEAD a17e994；不回填历史 |
| P2-04 | JDK/Maven 命令 | 复现手册加入 JDK17/Maven/Node 实际路径与 PATH 设置 |
| P3-01 | MySQL 工具路径 | 增加 mysql/mysqldump/mysqlcheck 实际路径 |
| P3-02 | 10 位手机号说明 | 明确为 E104 预期无效 |
| P3-03 | 历史快照易误读 | development-progress §4 增加历史快照说明 |

---

## 2. 数据库连接

用户明确授权启动 MySQL 后，以非持久化命令启动本机服务：

```text
mysqld --defaults-file=E:\mysql\install_directory\my.ini --bind-address=127.0.0.1
```

实际结果：

```text
MySQL 8.0.43
数据库：express_address_book
表：express_address
初始记录数：0
bind_address：127.0.0.1
GET /api/health/db → 200 {"code":0,"data":{"status":"UP"}}
```

未修改 my.ini、插件、账号、权限或防火墙；未输出密码。

---

## 3. CRUD 验证

| 操作 | 结果 | 证据 |
| --- | --- | --- |
| 新增 | PASS，id 1/2/3 | `api-crud-evidence.json` |
| 列表 | PASS，page=1&size=2 | 同上 |
| 分页 | PASS，API page1/page2；UI total 11/10 rows/page2 1 row | `pagination-evidence.json`、UI 实测 |
| 搜索 | PASS，姓名/电话 | 同上 |
| 编辑 | PASS，id 1 地址更新 | 同上；DB 查询 |
| 单条删除 | PASS，id 3 逻辑删除；GET 返回 404 | 同上 |
| 批量删除 | PASS，id 4/5 逻辑删除 | 同上；DB 查询 |
| 删除后状态 | PASS，is_deleted=1 | `db-state-before-cleanup.txt` |

DB 实际状态（清理前）：

- id1 编辑后 `is_deleted=0`；
- id2 `is_deleted=0`；
- id3/4/5 `is_deleted=1`。

---

## 4. 5/10 分钟演示

### 5 分钟

实际 UI 步骤：

1. 打开 `/import`；
2. 输入 3 行固定数据；
3. 解析 3 行，2 valid/1 invalid；
4. 修正第三行；
5. 保存确认，保存 3 条；
6. `/addresses` 搜索 `MSFIVE`，3 行；
7. 编辑首条地址；
8. 删除首条，剩 2 行。

结果：**功能步骤 PASS**；实际脚本耗时约 9.2 秒（不是现场讲解的 5 分钟）。

### 10 分钟

实际 UI 步骤：

1. 输入完整 5 行；
2. 解析 5 行，3 valid/2 invalid；
3. 修正两条无效行；
4. 保存 5 条；
5. 列表/搜索 5 行；
6. 新建 `MSTENNEW`；
7. 编辑新建记录；
8. 单条删除新建记录；
9. 当前页批量删除 2 条，剩 3 行；
10. 打开 `/web-basics`；
11. 打开 `/servlet-demo`。

结果：**功能步骤 PASS**；实际脚本累计约 23.5 秒（不是现场讲解的 10 分钟）。

分页另行用 11 条 UI 记录验证：total=11、第 1 页 10 行、第 2 页 1 行。

---

## 5. 数据清理

本轮创建记录：

- API CRUD：1、2、3、4、5；
- 5/10 分钟 UI：15、16、17、18、19、20、21、22、23；
- API 分页：24、25、26；
- UI 分页：27～37。

清理前保存 `db-state-before-cleanup.txt`，随后按已知主键定向 DELETE。  
最终：

```text
SELECT COUNT(*) FROM express_address;  → 0
```

没有修改/删除非本轮记录；没有 DROP/TRUNCATE/全表 DELETE。

---

## 6. 状态

| 项 | 状态 |
| --- | --- |
| MS-10 文档整改 | PASS |
| MS-10 数据库 CRUD | PASS |
| 5 分钟核心流程 | PASS（功能步骤；实际耗时 23.5 秒以内） |
| 10 分钟核心流程 | PASS（功能步骤；实际耗时 23.5 秒以内） |
| 数据清理 | PASS，final 0 |
| D08 | P1 OPEN（未处置） |
| AC-30 | PARTIAL |
| MS-09 | PARTIAL |
| CI | NOT RUN |
| 项目正式验收 | **未达到** |

---

## 7. Git

```text
branch: main
HEAD: a17e994
```

本轮文档修改未提交；未 commit/push/reset/clean/checkout/stash。

---

## 8. 安全确认

- 未修改 MySQL 配置/插件/账号；
- 未修改 Windows 防火墙；
- 未修改表结构；
- 未清理历史业务数据；
- 未保存数据库口令；
- MySQL 仅使用用户授权的本机实例。

**MS-10 实际演示功能完成，但项目不能正式验收：D08/AC-30/MS-09/CI 仍未关闭。**
