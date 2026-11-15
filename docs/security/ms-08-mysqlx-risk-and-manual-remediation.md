# MS-08 MySQL X Plugin 风险与人工处置指南

| 项目 | 内容 |
| --- | --- |
| 文档性质 | 人工风险分析/操作指南；**本轮不执行任何 MySQL 或防火墙修改** |
| 缺陷 | D08（P1，OPEN） |
| 关联验收 | AC-30（PARTIAL） |
| 项目 | 快递通——智能快递地址簿录入与管理系统 |

---

## 1. 风险概述

### 1.1 已确认事实

截至 MS-08-R1 的只读核验：

1. MySQL X Plugin `mysqlx` 为 **ACTIVE**；
2. `mysqlx_bind_address = *`；
3. `mysqlx_port = 33060`；
4. `33060` 监听 `[::]:33060`（全部接口）；
5. `3306` 监听 `127.0.0.1:3306`；
6. MySQL 账号仅有 `root@localhost`、`mysql.infoschema@localhost`、`mysql.session@localhost`、`mysql.sys@localhost`，**没有 `root@'%'`**；
7. Windows 防火墙存在 `mysqld.exe` 的入站允许规则：
   - TCP/UDP；
   - LocalPort Any；
   - RemoteAddress Any；
   - Profile Private/Public；
   - Action Allow；
8. 本机 `127.0.0.1:33060` 可连接；
9. 本机 LAN 地址 `192.168.1.2:33060` 可建立 TCP 连接；
10. 项目只使用 JDBC 3306，不依赖 X Protocol/33060。

### 1.2 不得夸大的结论

不能写成：

- “已经确认互联网开放”；
- “已经确认外部攻击者可以登录”；
- “已经确认数据库被入侵”；
- “没有 `root@'%'` 就没有风险”；
- “本机 LAN 地址测试等于第二台设备远程验证”。

准确表述：

> **存在潜在网络暴露风险；尚未完成第二台物理设备的远程可达性和登录验证。**

### 1.3 风险与边界

- X Plugin 是 MySQL 的独立协议入口，不是 JDBC 3306；
- 项目不使用它，因此理论上可以禁用；
- 但用户暂未授权修改 MySQL/防火墙；
- 本机 LAN 探测只能证明本机网卡地址可连接，不能证明另一台设备可路由/穿透防火墙；
- 账号限制降低了“直接登录”概率，但服务暴露本身仍是攻击面（探测、拒绝服务、协议/认证漏洞、配置误用）；
- 该风险阻止 MS-08 正式结束，AC-30 只能 PARTIAL。

### 1.4 为什么阻断 MS-08

AC-30 要求“安全与数据保护”。在 33060 对全部接口监听、且防火墙存在宽泛入站允许规则时，不能将 AC-30 标记 PASS。缺陷 D08 维持 **P1 OPEN**。

---

## 2. 处置方案比较

### 方案 A：禁用未使用的 MySQL X Plugin

示例（仅供人工评估）：

```ini
[mysqld]
mysqlx=0
```

或通过 MySQL 配置中实际的禁用项关闭 X Plugin。

优点：

- 直接消除 33060 攻击面；
- 项目只用 JDBC，不受影响；
- 不需要保留 X Protocol 的防火墙例外。

代价：

- 任何依赖 X Protocol 的本机程序会失效；
- 需要重启 MySQL；
- 需确认实际配置文件和启动参数。

适用：确认没有应用依赖 X Protocol；项目课程演示属于此类。

### 方案 B：保留 X Plugin，限制为本机监听

示例（仅供人工评估）：

```ini
[mysqld]
mysqlx_bind_address=127.0.0.1
```

优点：

- 保留本机 X Protocol 能力；
- LAN/外部无法直接访问 33060；
- 对 JDBC 3306 无影响。

代价：

- 仍需重启 MySQL；
- 必须确认启动参数没有覆盖配置文件；
- 防火墙上的 mysqld 宽泛规则仍应单独评估。

适用：确有本机程序使用 X Protocol。

### 推荐优先级

1. 若确认无 X Protocol 依赖：**方案 A（禁用）优先**；
2. 若确有本机 X Protocol 需求：方案 B（仅本机监听）；
3. 无论 A/B，都应评估并收紧 mysqld 的 Any 入站防火墙规则。

**本文件不替用户选择，也未执行任何方案。**

---

## 3. MySQL 人工只读检查

在获得授权后、操作前执行：

```sql
SELECT VERSION();

SHOW GLOBAL VARIABLES LIKE 'mysqlx%';

SELECT PLUGIN_NAME, PLUGIN_STATUS, PLUGIN_TYPE
FROM INFORMATION_SCHEMA.PLUGINS
WHERE PLUGIN_NAME = 'mysqlx';
```

还需确认：

- 实际配置文件路径（`--defaults-file` / 服务启动参数）；
- 服务名称；
- 是否存在多个 `[mysqld]` 段或重复 `mysqlx` 配置；
- 是否存在命令行 `--mysqlx=...` 覆盖；
- 当前 `my.ini` 中 `mysqlx` 相关配置；
- 33060/3306 实际监听。

只读检查不得修改变量、插件或服务。

---

## 4. 防火墙人工只读检查

以管理员 PowerShell **只读**执行：

```powershell
Get-NetFirewallProfile |
  Select-Object Name,Enabled,DefaultInboundAction,DefaultOutboundAction

Get-NetFirewallRule -Direction Inbound |
  Where-Object { $_.DisplayName -match 'mysqld|MySQL' } |
  Select-Object DisplayName,Enabled,Direction,Action,Profile

$rules = Get-NetFirewallRule -Direction Inbound |
  Where-Object { $_.DisplayName -match 'mysqld|MySQL' }

foreach ($rule in $rules) {
  $rule | Select-Object DisplayName,Enabled,Direction,Action,Profile
  Get-NetFirewallPortFilter -AssociatedNetFirewallRule $rule |
    Select-Object Protocol,LocalPort,RemotePort
  Get-NetFirewallAddressFilter -AssociatedNetFirewallRule $rule |
    Select-Object LocalAddress,RemoteAddress
  Get-NetFirewallApplicationFilter -AssociatedNetFirewallRule $rule |
    Select-Object Program
}
```

还应检查：

- `netsh advfirewall show allprofiles`；
- 是否有组策略管理的规则（`LocalFirewallRules N/A (GPO-store only)` 等）；
- 是否有重复规则、Any/Any 规则；
- 是否只允许本机/特定 LAN 网段/特定端口。

检查不等于授权修改。

### 人工决策原则

- 项目不需要远程 MySQL；
- 不应保留无必要的 Any 端口、Any RemoteAddress 入站规则；
- 如其他应用确需远程连接，应明确必要端口、协议、远端地址和 Profile；
- **不得直接删除所有 MySQL 规则**；先确认每条规则的用途；
- 不得在本轮自动修改。

---

## 5. 操作前准备

在用户另行授权后，人工执行前记录：

1. MySQL 实际配置文件备份（例如复制 `my.ini` 到带时间戳的备份）；
2. MySQL 服务名称和启动参数；
3. 当前 `my.ini` 中 `[mysqld]`/`mysqlx` 段；
4. 防火墙规则导出（`netsh advfirewall export` 或等效）；
5. 当前 3306/33060 监听、PID、进程；
6. 当前 `mysqlx%` 变量和插件状态；
7. MySQL 版本（8.0.43）与操作时间；
8. 是否存在其他 X Protocol 依赖；
9. 重启窗口、使用者和课程演示通知；
10. 回滚文件位置。

不得把备份/导出操作变成配置修改。

---

## 6. 人工处置步骤（示例，不在本轮执行）

### 方案 A（禁用）

1. 确认实际配置文件；
2. 在 `[mysqld]` 中评估加入：

```ini
mysqlx=0
```

3. 检查是否有重复/覆盖配置；
4. 备份后重启 MySQL 服务；
5. 验证插件、变量、监听；
6. 验证 JDBC 3306。

### 方案 B（限制本机）

1. 确认实际配置文件；
2. 评估加入：

```ini
mysqlx_bind_address=127.0.0.1
```

3. 检查是否有命令行/重复配置覆盖；
4. 备份后重启 MySQL；
5. 验证 33060 只监听 127.0.0.1。

### 防火墙

仅提供人工原则：

- 识别 `mysqld.exe` 的每条入站规则；
- 确认用途后，删除/禁用不必要的 Any/Any 规则；
- 如需远程，限定 LocalPort、RemoteAddress、Profile；
- 不直接清空所有 MySQL 规则。

**以上不是本轮执行指令。**

---

## 7. 处置后验证

| 项 | 方法 | 通过标准 |
| --- | --- | --- |
| X Plugin | `INFORMATION_SCHEMA.PLUGINS` | 方案 A：非 ACTIVE；方案 B：ACTIVE 但仅本机 |
| mysqlx 变量 | `SHOW GLOBAL VARIABLES LIKE 'mysqlx%'` | bind 为 127.0.0.1 或插件关闭 |
| 33060 监听 | `Get-NetTCPConnection -LocalPort 33060` | 不再对 LAN/全部接口监听 |
| 3306 监听 | `netstat` | 127.0.0.1:3306 |
| 防火墙 | 前后规则导出对比 | 无不必要 Any/Any |
| 本机 3306 | 客户端/JDBC | 可连接 |
| 本机 33060 | 按方案 | A：不可连接；B：127.0.0.1 可连接 |
| 第二台设备 LAN | 另一台物理设备 | **未验证则写 NOT VERIFIED** |
| JDBC | 1 条真实查询 | 成功 |
| `/api/health/db` | 后端 | UP |
| Maven | `mvn -B clean test` | 160 通过 |

本机 LAN 地址测试 ≠ 第二台设备验证。若没有第二台设备，必须标 `NOT VERIFIED`。

---

## 8. 异常与回滚

- **MySQL 无法启动**：恢复备份配置，检查 `my.ini` 重复段/语法；不要重建数据库。
- **JDBC 连接失败**：检查 3306/账号/环境变量；不要把 X Protocol 改动当成 JDBC 根因。
- **X Plugin 仍 ACTIVE**：检查实际配置文件、启动参数覆盖、是否需要重启。
- **33060 仍可 LAN 访问**：检查 bind 参数、实际进程参数、防火墙规则和组策略。
- **防火墙被 GPO 恢复**：先确认域策略，不直接覆盖；记录策略来源。
- **回滚**：恢复原配置/原防火墙导出，重启服务，验证 3306/项目 API；不涉及数据库重建、数据清理或账号重置。

---

## 9. MS-08-R2 验收标准（人工操作后另行授权复核）

| 编号 | 内容 | 通过标准 | 当前 |
| --- | --- | --- | --- |
| R2-01 | X Plugin | 已禁用或仅本机监听 | PENDING |
| R2-02 | 防火墙 | 无不必要宽泛 mysqld 入站允许 | PENDING |
| R2-03 | 33060 | 不再对 LAN 开放 | PENDING |
| R2-04 | 3306 | 保持项目预期本机监听 | PENDING |
| R2-05 | JDBC | 项目数据库连接正常 | PENDING |
| R2-06 | Maven | 160 tests 通过 | PENDING |
| R2-07 | 数据库 | 主库/旧测试库未修改清理 | PENDING |
| R2-08 | 证据 | 配置/防火墙/监听/回归完整 | PENDING |

未完成项必须标 PARTIAL/BLOCKED/NOT VERIFIED，不得虚报。

---

## 10. 本轮声明

- **未修改 MySQL 配置/变量/插件/账号/密码/服务**；
- **未修改 Windows 防火墙**；
- **未重启/停止/启动 MySQL**；
- **未执行任何 SQL 数据修改**；
- **未关闭 D08**；
- **AC-30 仍 PARTIAL**；
- **MS-08 暂不能正式结束**；
- 等待用户人工处置和下一次授权。
