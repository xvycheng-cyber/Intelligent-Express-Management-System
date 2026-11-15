# MS-08-R2 人工风险处置方案与文档整改报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 阶段 | MS-08-R2 风险分析、人工操作指南、文档整改 |
| 结论 | 文档整改完成；D08 仍 P1 OPEN；AC-30 仍 PARTIAL；未执行系统配置变更 |
| 系统变更 | **无 MySQL/防火墙/账号/数据变更** |

---

## 1. 本轮工作范围

只做：

- D08 风险分析；
- MySQL X Plugin/防火墙人工处置指南；
- 操作前备份、验证、回滚说明；
- R2 验收标准；
- MS-08 文档状态同步。

不做：

- 修改 MySQL 配置/变量/插件/服务；
- 修改 Windows 防火墙；
- 重启/停止/启动 MySQL；
- 修改数据库/数据；
- 修改代码/测试；
- commit/push。

---

## 2. 读取和修改的文件

新增：

```text
docs/security/ms-08-mysqlx-risk-and-manual-remediation.md
docs/review/ms-08-r2-manual-remediation-plan-report.md
```

更新：

```text
docs/review/ms-08-final-verification-report.md
docs/review/ms-08-final-delivery-report.md
docs/review/ms-08-defect-ledger.md
docs/test-plan.md
docs/development-roadmap.md
docs/development-progress.md
README.md
```

没有修改 Java、Vue、TypeScript、SQL、`.gitignore`、MySQL 配置或防火墙。

---

## 3. 风险事实

已确认：

- `mysqlx` ACTIVE；
- `mysqlx_bind_address=*`；
- `mysqlx_port=33060`；
- 33060 监听 `[::]:33060`；
- 3306 监听 `127.0.0.1:3306`；
- MySQL 账号仅 localhost，无 `root@'%'`；
- `mysqld` 防火墙入站规则 TCP/UDP、Any/Any、Private/Public Allow；
- 本机 LAN 地址 `192.168.1.2:33060` 可建立 TCP 连接；
- 项目只用 JDBC 3306。

未验证：

- 第二台物理设备的远程连接/登录；
- 外部网络/互联网可达性；
- 是否有其他依赖 X Protocol 的程序。

准确判断：

> **潜在网络暴露风险，尚未完成第二台物理设备远程验证；P1 OPEN。**

---

## 4. 人工处置方案

指南：

[`docs/security/ms-08-mysqlx-risk-and-manual-remediation.md`](../security/ms-08-mysqlx-risk-and-manual-remediation.md)

方案：

- A：禁用 X Plugin（推荐给无 X Protocol 依赖的项目）；
- B：保留 X Plugin，`mysqlx_bind_address=127.0.0.1`；
- 两者都需要人工确认配置、备份、重启、验证；
- 防火墙应人工识别并收紧 mysqld 的 Any/Any 规则，不得直接删除所有 MySQL 规则；
- 提供只读检查、备份、验证、回滚步骤。

未替用户选择，未实施。

---

## 5. R2 验收状态

| 编号 | 状态 |
| --- | --- |
| R2-01 X Plugin | PENDING |
| R2-02 防火墙 | PENDING |
| R2-03 33060 | PENDING |
| R2-04 3306 | PENDING |
| R2-05 JDBC | PENDING |
| R2-06 Maven 160 | PENDING（处置后重跑） |
| R2-07 数据库未改动 | PENDING（处置后核对） |
| R2-08 证据 | PENDING |

未将任何项写成 PASS。

---

## 6. 未执行的系统级操作

明确未执行：

- 未修改 `my.ini`/MySQL 配置；
- 未设置 `mysqlx=0`；
- 未设置 `mysqlx_bind_address`；
- 未启用/禁用/卸载 X Plugin；
- 未重启/停止/启动 MySQL；
- 未修改 Windows 防火墙；
- 未修改账号/密码/授权；
- 未执行 DROP/DELETE/TRUNCATE/数据清理；
- 未执行 Git commit/push/reset/clean/checkout/stash。

---

## 7. 文档同步结果

- D08：P1 OPEN；
- AC-30：PARTIAL；
- AC-31：PASS（Edge 完整矩阵 + 性能原始样本）；
- AC-32：PASS（本地可复现，CI NOT RUN）；
- D01：CLOSED（405 HEAD/OPTIONS 语义）；
- D05：CLOSED（Edge 完整矩阵）；
- MS-08 暂不能正式结束；
- MS-09 不启动；
- MySQL/防火墙未修改，人工处置未完成。

---

## 8. Git 状态

```text
branch: main
HEAD: ef97913（MS07，外部提交）
```

MS-08 变更全部本地未提交；本轮只新增/修改文档。  
未 commit/push/reset/clean/checkout/stash。

---

## 9. 尚未关闭的问题

**D08（P1）**：MySQL X Plugin 33060 潜在暴露 + mysqld 宽泛防火墙规则。

需要用户：

1. 确认无 X Protocol 依赖；
2. 选择方案 A 或 B；
3. 人工备份并修改 MySQL 配置；
4. 人工收紧防火墙；
5. 重启并验证 3306/33060/防火墙；
6. 使用第二台物理设备验证 LAN（无设备则标 NOT VERIFIED）；
7. 授权重新运行 R2-01～08 和 Maven 160。

**MS-08 正式结束被阻塞；不进入 MS-09。**
