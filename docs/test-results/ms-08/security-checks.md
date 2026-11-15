# MS-08 安全测试记录

| 检查 | 结果 | 证据 |
| --- | --- | --- |
| 仓库无 `application-local.yml` | PASS | `Get-ChildItem` 未发现 |
| `.env`/本地配置未跟踪 | PASS | `.gitignore` + 工作区检查 |
| 示例配置无真实密码 | PASS | `CHANGE_ME_LOCAL_ONLY` 占位符 |
| Git 跟踪文件无真实口令 | PASS | 搜索只发现占位符/环境变量 |
| 无 `v-html` | PASS | `frontend/src` grep 无结果 |
| SQL 参数化 | PASS | Mapper/XML 使用参数绑定；无用户输入拼接 |
| LIKE 特殊字符 | PASS | LikeEscaper 及 MS05 搜索用例 |
| 非法 ID | PASS | `GET /api/addresses/not-a-number` → 400/40001，无堆栈 |
| SQL 注入探测 | PASS | keyword `' OR 1=1 --` → 200 空结果，无 SQL 泄露 |
| 错误响应无 SQL/堆栈/口令 | PASS | 全局异常返回 R，不输出堆栈 |
| 后端监听 | PASS | `127.0.0.1:8080` |
| 前端监听 | PASS | `127.0.0.1:5173` |
| MySQL 主端口 | PASS | `127.0.0.1:3306` |
| MySQL root 主机 | PASS | `root@localhost`；无 `root@'%'` |
| 405 Allow | PASS | `Allow: GET, HEAD, OPTIONS`（修复后） |
| MySQL X Plugin 33060 | **P1 OPEN** | 全部接口监听 + mysqld 防火墙允许；AC-30 PARTIAL |
| 批量事务/服务端校验 | PASS | 158 后端测试中的真实 MySQL 事务用例 |
| 无身份认证 | 已知范围限制 | 课程项目未要求登录；不描述为已实现安全认证 |

## 观察

- **P1 D08**：MySQL X Plugin ACTIVE，`mysqlx_bind_address=*`，33060 监听全部接口；
- mysqld 防火墙入站规则为 TCP/UDP Any → Any，Private/Public Allow；
- 从本机 LAN 地址 `192.168.1.2:33060` 可建立 TCP 连接；未从第二台机器验证，但已具备潜在外部暴露；
- 项目只使用 JDBC 3306，不使用 X Protocol；
- 未修改 MySQL/防火墙配置（需用户授权）；建议禁用 X Plugin 或限制到 127.0.0.1，并收紧 mysqld 防火墙规则；
- AC-30 因此为 **PARTIAL/P1**，不是 PASS。
- 项目没有登录/权限；这是既定课程范围，不构成本次安全缺陷。
