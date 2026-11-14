# P0 设计修订最终冻结报告（v1.1-final）

| 项目 | 内容 |
| --- | --- |
| 文档版本 | v1.1-final |
| 日期 | 2026-11-14 |
| 仓库 | `Intelligent-Express-Management-System` |
| 系统名称 | 快递通——智能快递地址簿录入与管理系统 |
| 课程论文题目 | 快递地址簿录入程序设计 |
| 范围 | 第二轮审查遗留的 F-05、F-09、F-03、F-02 四项一致性修复；设计冻结准备 |
| 状态 | **设计冻结；未编写业务代码；未执行任何测试** |

> 本报告只记录设计文档修订与静态一致性检查。所有"已修复"均指文档契约已统一，不代表代码实现或测试通过；`test-plan.md` 中所有用例仍为"待执行"。
>
> **后续变更（2026-11-14，MS-01 之后）**：本机开发环境决定应用数据库连接改用 `root`（密码外置），`express_app` 降级为可选最小权限方案。本文第 1.4、6 节中"默认 express_app、禁止 root"的表述为当时决策，已被 `docs/review/database-account-change-report.md` 替代；其余姓名/40003/isEdited 结论仍然有效。

---

## 1. 四项遗留问题修复情况

| 编号 | 问题 | 修复结果 | 关键落点 |
| --- | --- | --- | --- |
| F-05 | 姓名长度与错误码不一致（正则 15、提示 50、E111 不可达） | **已修复**：统一为 1～50；E106 只管 1～50 内非法字符，E111 管 >50；单字姓名合法 | `parser-design.md` §4.3/§5、`requirements.md` BR-03/§9.11、`api-design.md` §3.2、`test-plan.md` TC-PARSE-035～038 |
| F-09 | 批量 201 条返回 40001 还是 40003 冲突 | **已修复**：1～200 正常；空列表 40001；普通字段错误 40001；>200 为 40003；数量超限优先；`data.violations[]` 统一 | `api-design.md` §1.4/§1.5/§3.2/§7、`architecture.md` §8/§11、`test-plan.md` TC-ADDR-013/063/064 |
| F-03 | `isEdited` 契约模糊，可能进入请求体 | **已修复**：`ParsedRow` 保存 `original` 快照，`isEdited` 由比较派生；`toPayload()` 只提交业务字段；DTO/DB 无该字段；后端不信任 | `requirements.md` BR-19/FR-05、`architecture.md` §5.3/§12、`api-design.md` §3.2/§4.2～4.3、`test-plan.md` TC-UI-017/018/024/025 |
| F-02 | `application.yml` 默认 `root`，违背最小权限 | **曾按 express_app 修复；2026-11-14 MS-01 后改为 root 本机演示（见后续变更报告）** | `architecture.md` §10.1/§10.3、`database-design.md` §11、`README.md` §4、`requirements.md` BR-22/§9.12 |

### 1.1 F-05 细节

- 唯一长度规则：**1～50 个字符**。
- 中文：`^[\p{IsHan}·]{1,50}$`；英文：`^[A-Za-z][A-Za-z .'\-]{0,49}$`（总长 1～50）。
- 正则只校验字符集；长度最终以 `String.length() <= 50` 复核，与 Java `@Size(max=50)` 的 UTF-16 计数一致。
- 单字中文姓名（如"王"）合法。
- 错误码语义：
  - `E102`：完全无类姓名候选段；
  - `E106`：候选存在且长度 1～50，但含数字/特殊字符或不匹配姓名规则；
  - `E111`：候选长度 > 50。
- 数据库 `receiver_name VARCHAR(50)`、前端 `maxlength=50`、后端 `@Size(max=50)`、Parser 规则一致。
- 新增测试：TC-PARSE-037（50 字通过）、TC-PARSE-038（51 字 → E111）。

### 1.2 F-09 细节

| 场景 | HTTP | code | `data` |
| --- | --- | --- | --- |
| 1～200 条且字段合法 | 200 | 0 | `{savedCount, ids}` |
| `items=[]` 或缺失 | 400 | 40001 | `violations[]` |
| 普通字段非法（1～200 条） | 400 | 40001 | `violations[].field = items[i].xxx` |
| 含 `null` 元素 | 400 | 40001 | `violations[].field = items[i]` |
| `items` > 200 条 | 400 | **40003** | `violations[].field = items` |
| 201 条且含非法字段 | 400 | **40003**（数量优先） | `violations[].field = items` |
| 数据库异常 | 500 | 50002 | `null` |

实现约束：`GlobalExceptionHandler` 遇到 `items` 的 `@Size` 违规时，必须映射 40003，不得沿用 40001。

### 1.3 F-03 细节

- `ParsedRow` 增加 `original: AddressPayload`（解析时快照）。
- `isEdited = 当前值与 original 任一不同`；恢复原值后自动为 `false`。
- 保存确认统计"人工修改数"时按 `isEdited` 计数。
- `toPayload(row)` 只返回 `{receiverName, phone, fullAddress, lineNo?}`。
- `AddressSaveDTO` 无 `isEdited`；数据库无对应列；后端不把该字段当业务参数。
- 新增/更新测试：TC-UI-017（计数）、TC-UI-018（修改标记）、TC-UI-024（恢复原值）、TC-UI-025（请求体字段）、TC-ADDR-059（服务端忽略）。

### 1.4 F-02 细节

- `application.yml` 的 `username/password` 只写 `${DB_USERNAME}`/`${DB_PASSWORD}`，无 root 默认值。
- 初始化：管理员账号执行 `schema.sql`/`data.sql`、创建应用账号。
- 运行：`express_app`（`'express_app'@'127.0.0.1'`）仅 `SELECT/INSERT/UPDATE/DELETE`。
- 不授予 `DROP`、`ALTER`、`CREATE USER`、`GRANT`；不使用 `'%'` 主机。
- 示例文件 `application-local.example.yml` 仅含占位符。
- README §4 已改为"管理员初始化 → 创建 `express_app` → 应用配置"的顺序。

---

## 2. 文件修改清单

| 文件 | 修改内容 |
| --- | --- |
| `docs/requirements.md` | BR-03（1～50）、BR-19（派生 `isEdited`）、BR-22（账号分离）、FR-05/FR-19、§9.8/§9.11/§9.12、变更记录 v1.1-final |
| `docs/architecture.md` | §5.3 `original`/`toPayload`/`maxlength`、§8 201→40003、§10.1 去 root + 示例、§10.3 账号分离、§11、§12 不变量、§13 步骤、§16 清单、变更记录 |
| `docs/database-design.md` | §8 `isEdited` 说明、§11 账号分离与授权 SQL、变更记录 |
| `docs/api-design.md` | §1.4 40001/40003 职责、§1.5 40003 结构、§3.2 201 与优先级、§4.2 `toPayload`、§4.3 `ParsedRow.original`、§7 异常映射、§8 自测、变更记录 |
| `docs/parser-design.md` | §4.3 1～50 与 `String.length()` 复核、§4.4/E 码语义、§5 E106/E111/E103/E104、算法步骤、变更记录 |
| `docs/test-plan.md` | TC-PARSE-037/038、TC-ADDR-013/063/064、TC-UI-024/025、TC-SEC-02/11/12、§14.1 验证方式、变更记录 |
| `docs/development-roadmap.md` | 新增 MS-00C、MS-01 账号、MS-02 1～50/038、MS-03 201→40003、MS-04 `toPayload`、交付物、变更记录 |
| `docs/course-paper-outline.md` | 2.21.3/2.21.4/2.21.5/2.21.7/2.21.9/2.21.10、七个设计决策、变更记录 |
| `AGENTS.md` | 新增 §1.4 设计版本摘要、目录树补 `.gitignore` 与 `docs/review/` |
| `README.md` | 状态 v1.1-final、修复报告链接、§4 账号分离与授权、目录树 |
| `docs/review/p0-finalization-report.md` | 本报告（新增） |

> `.gitignore` 本次未修改，已覆盖 `application-local.yml`、`.env`、`target/`、`node_modules/`、`dist/`、`test-results/`、`playwright-report/`、`*.log`、`.idea/`、`.vscode/`，并放行 `.env.example`、`application-local.example.yml`。

---

## 3. 错误码统一表

### 3.1 HTTP 业务码

| code | HTTP | 含义 | 触发 | `data` |
| --- | --- | --- | --- | --- |
| 0 | 200 | 成功 | — | 业务数据 |
| 40001 | 400 | 字段校验失败 | 必填/格式/单字段长度错误、空列表、null 元素 | `{violations[]}` |
| 40002 | 400 | 请求体缺失/JSON 非法 | 空 body、非法 JSON | `null` |
| 40003 | 400 | 请求数据超限 | `rawText`>20000、有效行>200、`items`>200、`ids`>200 | `{violations[]}` |
| 40401 | 404 | 数据不存在 | 编辑/删除不存在的 id | `null` |
| 40402 | 404 | 接口不存在 | 未映射的 `/api/**` | `null` |
| 40501 | 405 | 方法不支持 | 错误 HTTP 方法 | `null` |
| 50001 | 500 | 服务器内部错误 | 未捕获异常 | `null` |
| 50002 | 500 | 数据库操作失败 | 事务回滚/连接失败 | `null` |

**优先级**：`items` 数量超限（40003）优先于字段错误（40001）；数量合法后才报字段错误。

### 3.2 解析行错误码（E1xx）

| 错误码 | 含义 | 触发 |
| --- | --- | --- |
| E102 | 未识别到姓名 | 无任何类姓名候选段 |
| E103 | 未识别到电话 | 无模式匹配且无 5～30 位纯数字段 |
| E104 | 电话格式不正确 | 5～20 位纯数字段但不匹配手机/座机 |
| E105 | 未识别到地址 | 剔除姓名/电话后为空 |
| E106 | 姓名不合法 | 候选 1～50 但含数字/特殊字符 |
| E107 | 地址过长 | >255 |
| E108 | 电话过长 | 纯数字候选 >20 |
| E110 | 单行过长 | >500 字符 |
| E111 | 姓名过长 | 类姓名候选 >50 |

> E109 已删除，不再作为有效错误码；E101 不保留占位。

---

## 4. 姓名校验规则（唯一规则）

| 层 | 规则 | 位置 |
| --- | --- | --- |
| Parser 中文 | `^[\p{IsHan}·]{1,50}$` | `parser-design.md` §4.3 |
| Parser 英文 | `^[A-Za-z][A-Za-z .'\-]{0,49}$` | 同上 |
| Parser 长度复核 | `String.length() <= 50` | 同上 |
| Java Validator | `@NotBlank` + `@Size(max=50)` | `api-design.md` §3.2 |
| 前端输入 | `maxlength=50`，提示"1～50 个字符" | `architecture.md` §5.3 |
| 数据库 | `VARCHAR(50)` | `database-design.md` §3 |
| 解析错误 | 1～50 内非法 → E106；>50 → E111 | `parser-design.md` §5 |
| 单字姓名 | 合法（如"王"） | TC-PARSE-035 |
| 边界测试 | 50 字通过、51 字 E111 | TC-PARSE-037/038 |

> 说明：Parser 正则负责字符集；长度以 `String.length()` 复核，与 `@Size` 的 UTF-16 计数一致。扩展 B 等补充平面生僻字按 2 个 UTF-16 单元计，属已知边界（课程数据不涉及）。

---

## 5. 前端与后端数据契约

### 5.1 TypeScript

```ts
export interface AddressPayload {
  receiverName: string;
  phone: string;
  fullAddress: string;
}

export interface AddressSaveItem extends AddressPayload {
  lineNo?: number;          // 仅错误定位；不含 isEdited
}

export interface ParsedRow extends ParsedItem {
  rowKey: string;
  original: AddressPayload; // 解析时快照
  isEdited: boolean;        // 由 current 与 original 比较派生
}

export function toPayload(row: ParsedRow): AddressSaveItem;
```

### 5.2 Java DTO

```java
public record AddressSaveDTO(
        @NotBlank @Size(max = 50) String receiverName,
        @NotBlank @Size(max = 20) @Pattern(...) String phone,
        @NotBlank @Size(max = 255) String fullAddress,
        Integer lineNo) {
}
// 无 isEdited 字段
```

### 5.3 请求/响应

```json
// POST /api/addresses/batch 请求（只允许业务字段）
{ "items": [ { "lineNo": 1, "receiverName": "菜小鸟", "phone": "13800138000", "fullAddress": "浙江省杭州市..." } ] }

// 字段错误（40001）
{ "code": 40001, "message": "参数校验失败", "data": { "violations": [ { "field": "items[0].phone", "lineNo": 1, "message": "联系电话格式不正确" } ] } }

// 数量超限（40003）
{ "code": 40003, "message": "单次最多保存 200 条", "data": { "violations": [ { "field": "items", "message": "单次最多保存 200 条" } ] } }
```

### 5.4 边界规则

| 规则 | 约定 |
| --- | --- |
| `isEdited` | 仅前端派生；`toPayload` 剥离；不入库；后端不信任 |
| `original` | 仅前端快照；不提交 |
| `lineNo` | 可选，仅用于错误回填 |
| `items` | 1～200；空 → 40001；>200 → 40003 |
| 事务 | 任一条写入异常整批回滚 |

---

## 6. 数据库账号权限方案

| 账号 | 用途 | 权限 | 使用时机 |
| --- | --- | --- | --- |
| 管理员（如 `root`） | 建库/建表/建应用账号/演示数据 | DDL + 用户管理 | 仅本机初始化 |
| `express_app` | 应用运行 | `SELECT/INSERT/UPDATE/DELETE` on `express_address` | 每次启动 |

```sql
-- 管理员执行
CREATE USER 'express_app'@'127.0.0.1' IDENTIFIED BY '<LOCAL_STRONG_PASSWORD>';
GRANT SELECT, INSERT, UPDATE, DELETE
    ON express_address_book.express_address
    TO 'express_app'@'127.0.0.1';
SHOW GRANTS FOR 'express_app'@'127.0.0.1';
```

- 不授予 `DROP`、`ALTER`、`CREATE USER`、`GRANT`；不使用 `'%'` 主机。
- `application.yml`：`${DB_USERNAME}`/`${DB_PASSWORD}`，无 root 默认。
- `application-local.example.yml`：`express_app` + `CHANGE_ME_LOCAL_ONLY`，无真实口令。
- 口令只在本机配置或环境变量；`.gitignore` 已忽略 `application-local.yml`、`.env`。

---

## 7. 文档一致性检查

| 检查项 | 结果 | 证据 |
| --- | --- | --- |
| 姓名 1～50（Parser/Validator/前端/DB/E 码） | ✅ 一致 | parser §4.3/§5、api §3.2、architecture §5.3/§12、database §3、test TC-PARSE-037/038 |
| 手机号 ≤20、地址 ≤255 | ✅ 一致 | database §3、api §3.2、requirements BR-12 |
| 批量 1～200；201→40003 | ✅ 一致 | api §1.4/§3.2/§7、architecture §8/§11、test TC-ADDR-013/063/064、roadmap MS-03 |
| `isEdited` 契约 | ✅ 一致 | requirements BR-19、architecture §5.3/§12、api §3.2/§4.3、test TC-UI-024/025 |
| `data.violations[]` vs `items[].errors[]` | ✅ 一致 | api §1.5/§3.1、requirements §9.7、architecture §12 |
| 数据库账号分离/无 root | ✅ 一致 | architecture §10.1/§10.3、database §11、README §4、test TC-SEC-11/12 |
| 仅监听 127.0.0.1 | ✅ 一致 | architecture §3/§10/§13、README §8、test TC-SEC-08 |
| API 请求/响应结构 | ✅ 一致 | api §3.2/§4、report §5 |
| 版本号 | ✅ 全部 v1.1-final | 8 份文档头部 |
| 测试执行 | ⬜ 未执行 | `test-plan.md` 结果表仍为"待执行" |

---

## 8. 尚未解决的问题

| # | 问题 | 处理建议 |
| --- | --- | --- |
| 1 | F-01 的 `apply` 与 XML 二选一未定 | MS-03 前定稿；两者行为等价，差别在可测试性/维护性 |
| 2 | `\p{IsHan}` 在 Java 17 的实际行为未运行验证 | MS-02 用单测覆盖扩展汉字 |
| 3 | 英文 `Smith, John`（姓, 名）不支持 | MS-02 记录限制，MS-09 写入论文"局限性" |
| 4 | 连续多个逗号的专项测试缺失 | MS-02 补 TC-PARSE 用例 |
| 5 | Element Plus 翻页清空的具体实现未写 | MS-05 用 `@current-change` + `clearSelection` |
| 6 | 后端无保存幂等键 | MS-03 可选；前端禁用按钮 + 论文说明 |
| 7 | `application-local.example.yml`、`.env.example`、`schema.sql`、`data.sql` 实体文件未创建 | MS-01 创建 |
| 8 | v1.1-final 与 `.gitignore`/README 未提交 | 见 §10；提交后再进入 MS-01 |
| 9 | AGENTS.md 为硬链接，编辑可能断链 | 每次编辑后校验/重建；或改符号链接（需权限） |
| 10 | 包名/库名仍为 `com.express.addressbook`/`express_address_book` | 可选，不影响功能 |

---

## 9. MS-01 准入条件检查

| # | 条件 | 状态 |
| --- | --- | --- |
| 1 | 四项遗留问题关闭 | ✅ |
| 2 | 姓名 1～50 全链路一致 | ✅ |
| 3 | 201 条 → 40003、字段错误 → 40001 | ✅ |
| 4 | `isEdited` 不入请求/库，`toPayload` 明确 | ✅ |
| 5 | 应用账号非 root、仅 DML；初始化账号分离 | ✅ |
| 6 | 本机监听、无认证声明、日志脱敏 | ✅ |
| 7 | 8 份文档版本 v1.1-final、交叉引用一致 | ✅ |
| 8 | 设计文档已提交（当前未提交） | ⬜ 待用户提交 |
| 9 | 测试未执行（设计阶段正常） | ⬜ 未执行 |

**结论：有条件通过（Conditional Go）。** 设计契约已达到 MS-01 工程初始化标准；在**提交 v1.1-final**后即可开始建工程骨架。业务编码（MS-02/03/04）前需完成第 8 节第 1～4 项的落地决策/验证。

---

## 10. Git 建议（未执行）

当前 `git status`：9 个已跟踪文件修改、`.gitignore`/`README.md` 未跟踪、本报告未跟踪；**未 add/commit/push**。

建议一次性提交以下文件：

```text
AGENTS.md
README.md
.gitignore
docs/requirements.md
docs/architecture.md
docs/database-design.md
docs/api-design.md
docs/parser-design.md
docs/test-plan.md
docs/development-roadmap.md
docs/course-paper-outline.md
docs/review/p0-finalization-report.md
```

建议提交说明：

```text
docs: freeze design v1.1-final (name length, 40003, isEdited, DB account)

- unify name length to 1..50 (E106 invalid / E111 >50)
- map batch items >200 to 40003 with violations[field=items]
- derive isEdited from original snapshot; add toPayload projection
- separate express_app runtime account from admin init; remove root default
- add docs/review/p0-finalization-report.md
```

> 请确认后再执行 `git add` / `git commit`；本任务未执行任何 Git 写操作。

---

## 11. 后续数据库账号变更（MS-01 之后）

2026-11-14 MS-01 初始化后，根据本机开发环境决定：

- 应用连接账号由 `express_app` 调整为 **`root`**（`127.0.0.1:3306/express_address_book`）；
- 口令通过环境变量 `DB_PASSWORD` 或本地 `application-local.yml` 提供，**不写入 Git**；
- `admin-init.sql` 的 `express_app` 保留为**可选最小权限方案**，不再默认执行；
- root 仅限本机课程演示，禁止 `root@'%'`、远程登录与公共网络部署。

详细验证、文件修改与安全限制见 `docs/review/database-account-change-report.md`。本文 F-02/BR-22 的“默认 express_app”表述已被该变更替代。
