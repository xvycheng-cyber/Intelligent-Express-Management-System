# MS-10 演示数据与数据安全

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 原则 | 虚构数据、隔离环境、只读优先、人工备份/恢复 |
| 本阶段执行 | 在用户授权后启动 MySQL，完成 CRUD/5-10 分钟核心演示，最终清理本轮数据；未改配置/防火墙 |

---

## 1. 演示数据

固定使用以下虚构文本（可在 `/import` 直接粘贴）：

```text
5 分钟（前 3 行）：
MSFIVEAA，17790004001，浙江省杭州市西湖区MS10-DEMO-5-A
MSFIVEAB，17790004002，浙江省杭州市余杭区MS10-DEMO-5-B
MSFIVEAX

10 分钟（完整 5 行）：
MSTENAA，17790005001，浙江省杭州市西湖区MS10-DEMO-10-A
MSTENAB，17790005002，浙江省杭州市余杭区MS10-DEMO-10-B
MSTENAX
MSTENAC，0571-88886666，浙江省杭州市拱墅区MS10-DEMO-10-C
MSTENAZ，1779000500，浙江省杭州市滨江区MS10-DEMO-10-D
```

说明：

- 姓名、电话、地址均为虚构；
- 第 3 行用于展示“无电话/无地址”的无效行；
- 第 5 行手机号 10 位，**预期为无效**（E104 格式错误）；
- 前 3 行预期 **2 有效 / 1 无效**；完整 5 行预期 **3 有效 / 2 无效**；
- 座机行用于展示多格式电话，属于有效行；
- 不包含真实快递收件人信息。

## 2. Schema 隔离

| Schema | 用途 |
| --- | --- |
| `express_address_book` | 正式业务库；演示数据不得无条件批量灌入 |
| `express_address_book_ms08_test` | 已存在的隔离测试库；本轮禁止启动/写入 |
| `express_address_book_test` | 旧测试库 38 行；禁止清理 |

演示前必须确认连接的是哪个 Schema；不能用测试 Schema 冒充正式库。

## 3. 演示前备份（人工）

仅在用户明确同意并已启动 MySQL 时执行；本阶段未执行：

```powershell
& 'E:\mysql\install_directory\bin\mysqldump.exe' -u root -p --single-transaction --routines --triggers express_address_book > backup_express_address_book_YYYYMMDD_HHMM.sql
```

MySQL 工具实际路径：

```text
mysql.exe      E:\mysql\install_directory\bin\mysql.exe
mysqldump.exe  E:\mysql\install_directory\bin\mysqldump.exe
mysqlcheck.exe E:\mysql\install_directory\bin\mysqlcheck.exe
```

这些路径当前不在全局 PATH；不要假设 `mysqldump`/`mysql` 可直接调用。

要求：

- 口令交互输入，不写入命令行历史/脚本；
- 备份文件放用户指定目录，不入 Git；
- 记录备份时间、Schema、行数；
- 不在演示机上随意覆盖旧备份。

## 4. 恢复（人工、显式）

恢复会覆盖目标库数据，必须由用户确认后手动执行，本阶段不执行：

```powershell
Get-Content -Raw .\backup_express_address_book_YYYYMMDD_HHMM.sql |
  & 'E:\mysql\install_directory\bin\mysql.exe' -u root -p express_address_book
```

恢复前：

- 确认目标 Schema；
- 再次备份当前数据；
- 确认没有其他使用者；
- 不得用恢复操作清理用户数据。

## 5. 演示数据写入原则

- 优先页面手工输入，不自动批量写库；
- 如果使用已有测试脚本，只允许指向明确隔离 Schema；
- 不新增自动写正式库的脚本；
- 不执行 DROP/TRUNCATE/全表 DELETE；
- 不修改正式 Schema；
- 演示结束不自动清理；是否清理由用户决定并记录。

## 6. 本阶段实际数据操作

```text
启动 MySQL：已由用户授权启动（mysqld --defaults-file...）
数据库：express_address_book（本次确认）
初始记录数：0
数据库写入：仅本轮演示记录
数据库备份：未执行
数据库恢复：未执行
DROP/TRUNCATE：未执行
全表 DELETE：未执行
清理方式：按已知本轮主键定向 DELETE
最终记录数：0
```

本轮创建并清理的记录主键：

- API CRUD：1、2、3、4、5；
- 5/10 分钟 UI：15、16、17、18、19、20、21、22、23；
- API 分页：24、25、26；
- UI 分页：27～37。

没有修改或删除非本轮记录；清理前已保存 `docs/test-results/ms-10/db-state-before-cleanup.txt`。

## 7. 风险提示

- 无身份认证，不得公网演示；
- 33060/D08 未处置；
- 不要把真实手机号/地址放入截图或公开仓库；
- 演示失败时优先用既有真实截图/报告说明，不伪造保存成功。
