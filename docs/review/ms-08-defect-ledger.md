# MS-08 缺陷台账

| 编号 | 标题 | 等级 | 发现轮次 | 关联 | 状态 |
| --- | --- | --- | --- | --- | --- |
| MS08-D01 | Servlet 405 Allow/HEAD/OPTIONS 语义 | P2 | R2/R3 | AC-28、TC-SERVLET-08/10 | 已修复并回归（160 tests） |
| MS08-D02 | 核心 E2E 首次使用 10 位电话导致无效行 | P2 | R3 | AC-25/26 | 测试代码已修复并回归 |
| MS08-D03 | 旧测试库 38 行数据导致 MS05 总数断言不可隔离 | P2 | R3 | AC-26 | 已改用隔离 Schema；旧库未清理 |
| MS08-D04 | 无 CI workflow | P2 | R5 | AC-32 | 未修复，记录遗留 |
| MS08-D05 | Edge 只做基本页面验证，未跑完整矩阵 | P2 | R4 | AC-31 | 已补测关闭 |
| MS08-D06 | MySQL X Plugin 33060 监听 0.0.0.0 | P3 | R4 | AC-30 | 并入 D08 |
| MS08-D07 | 2.20 截图主图较高，论文排版需拆分 | P3 | R5 | 课程 2.20 | 记录建议 |
| MS08-D08 | MySQL X Plugin 全接口监听 + mysqld 防火墙全端口允许 | **P1** | R1 补充 | AC-30、NFR-05/11 | **OPEN，待用户授权处置** |

---

## MS08-D01 详情

- 复现：`POST /demo/servlet/exact`、`POST /demo/spring/method-only`；HEAD/OPTIONS
- 实际（修复前）：405 无 Allow；自定义 OPTIONS 甚至错误广告 POST
- 预期：405 + `Allow: GET, HEAD, OPTIONS`；HEAD/OPTIONS 200
- 根因：`HttpServlet` 默认 405 和 `GlobalExceptionHandler` 都没有显式 Allow；`doPost` 覆写导致 OPTIONS 广告 POST
- 修改：
  - `MappingDemoServletSupport.service`：支持 GET/HEAD/OPTIONS；其它方法 405 + Allow GET,HEAD,OPTIONS；不再覆写 doPost
  - `GlobalExceptionHandler`：GET 映射补 HEAD，并补 OPTIONS
  - 后端测试断言 HEAD/OPTIONS/405 和 Allow
  - 前端测试断言页面 header 显示 Allow
- 回归：`ServletMappingIntegrationTest` 14/14；`MS07_3_BROWSER_OK`
- 状态：CLOSED

## MS08-D02 详情

- 复现：E2E 电话 `1770000001`（10 位）
- 实际：解析为无效，保存按钮 disabled
- 根因：测试数据少一位
- 修改：`ms08-e2e-check.mjs` 使用 11 位虚构号码
- 回归：`MS08_E2E_OK`
- 状态：CLOSED

## MS08-D03 详情

- 旧测试库 `express_address_book_test` 有 38 行既有数据；
- MS05 假定总数 11，否则需要清理既有测试数据；
- 处理：新建 `express_address_book_ms08_test`，MS04/MS05/E2E 全在隔离 Schema 执行；
- 旧测试库未清理；
- 状态：CLOSED（环境隔离）

## MS08-D04 详情

- 仓库没有 `.github/workflows`；
- 未建立 CI，因为本机 MySQL 测试与浏览器环境依赖未配置到可重复的 CI；
- 状态：OPEN（P2，不阻断本地课程交付）

## MS08-D05 详情

- Edge 1280×720 及 1366/980/760/390 完整页面矩阵已补测；
- Edge 导入保存/搜索删除核心流程通过；
- 状态：CLOSED（2026-11-15 补测）

## MS08-D06 详情

- `netstat` 显示 `33060` MySQL X Plugin 监听 0.0.0.0；
- 应用后端、前端、MySQL 主端口均为 127.0.0.1；
- 防火墙 mysqld 规则允许 TCP/UDP Any 私网/公网；
- 未修改本机 MySQL/防火墙配置；
- 状态：并入 D08

## MS08-D07 详情

- `2-20-servlet-mapping.png` 1366×2239，包含完整证据；
- 论文单页排版建议拆分为主图/矩阵/404 详情；
- 状态：建议（P3）

## MS08-D08 详情（P1）

- MySQL X Plugin ACTIVE；
- `mysqlx_bind_address=*`，33060 对全部接口监听；
- mysqld 防火墙入站规则：TCP/UDP、Any 端口、Any 远端、Private/Public、Allow；
- 从本机 LAN 地址 `192.168.1.2:33060` 可建立 TCP 连接；
- 项目只使用 JDBC 3306，不使用 X Protocol；
- 未从第二台机器验证，但监听/防火墙配置已构成潜在外部暴露；
- 没有修改 MySQL 配置/账号/防火墙（需用户授权）；
- 建议：禁用 X Plugin 或设 `mysqlx_bind_address=127.0.0.1`，并删除/收紧 mysqld 全端口入站规则；
- 人工指南：[`docs/security/ms-08-mysqlx-risk-and-manual-remediation.md`](../security/ms-08-mysqlx-risk-and-manual-remediation.md)；
- 处置后需复测 `netstat`、防火墙和 LAN 连接。
- 状态：OPEN（P1，AC-30 PARTIAL；R2-01～08 PENDING）
