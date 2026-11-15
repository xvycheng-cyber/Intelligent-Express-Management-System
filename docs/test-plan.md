# 测试计划与验收标准（test-plan.md）

| 项目 | 内容 |
| --- | --- |
| 文档版本 | v1.1-final-ms03.1 |
| 适用阶段 | MS-01 初始化、MS-02/02.2 解析器、MS-03/03.1 地址簿后端、MS-04 批量录入前端 |
| 依据 | `requirements.md`、`api-design.md`、`parser-design.md`、`database-design.md` |
| 重要声明 | 只有实际执行的测试才能标记为通过；Parser 单测、Controller MockMvc、真实 Parser/HTTP、MySQL 集成、Playwright 必须分开记录 |

---

## 1. 测试范围

| 层级 | 范围 | 当前状态 |
| --- | --- | --- |
| L1 Parser 单测 | 多行、分隔符、电话、姓名、地址、错误码、边界 | 已执行 86 个 |
| L2 Controller MockMvc | 解析 API 上限与统一错误（mock Parser） | 已执行 12 个 |
| L2 真实 Parser + HTTP | `@SpringBootTest` + 真实 `AddressParserService` | 已执行 8 个 |
| L3 MySQL 集成 | 地址 CRUD、分页、LIKE、批量、事务回滚、逻辑删除、P0 契约 | 已执行 16 个 |
| DTO 校验 | `AddressSaveDTO` 码点/地址/电话/原始格式 | 已执行 10 个 |
| LIKE 转义单测 | `LikeEscaper` | 已执行 4 个 |
| 码点校验单测 | `@CodePointLength` 1/50/51 | 已执行 5 个 |
| 基础测试 | R、health、异常映射、DB health mock | 已执行 5 个 |
| L4 浏览器/Playwright | MS-04 批量录入、MS-05 地址簿管理 | 已执行 2 个端到端脚本；列表管理真实 MySQL/接口 |

当前真实基线：`mvn clean test` = **146 tests, 0 failures, 0 errors, 0 skipped**。

---

## 2. 环境

| 项 | 值 |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.3.5 |
| Maven | 3.9+ |
| MySQL | 8.0.x（`utf8mb4`） |
| 后端监听 | `127.0.0.1:8080` |
| 前端监听 | `127.0.0.1:5173` |

---

## 3. Parser 用例（TC-PARSE）

| 编号 | 场景 | 输入/操作 | 期望 |
| --- | --- | --- | --- |
| TC-PARSE-001 | 中文逗号 | `菜小鸟，136xxxx0000，浙江省...` | VALID |
| TC-PARSE-002 | 英文逗号 | `张小明,13900139000,浙江省...` | VALID |
| TC-PARSE-003 | Tab | `李四\t13800138000\t湖北省...` | VALID |
| TC-PARSE-004 | 中文分号 | `王五；13800138001；湖北省...` | VALID |
| TC-PARSE-005 | 英文分号 | `王五;13800138001;湖北省...` | VALID |
| TC-PARSE-006 | 电话行首 | `13800138000，王五，湖北省...` | 姓名=王五 |
| TC-PARSE-007 | 姓名,地址,电话 | `赵六，湖北省...，13900139000` | 地址不含电话 |
| TC-PARSE-008 | 地址,姓名,电话 | `湖北省...，赵六，13900139000` | 姓名=赵六 |
| TC-PARSE-009 | 地址英文逗号 | `菜小鸟，浙江省杭州市,西湖区、XX路12号，138...` | 地址保留 `,`/`、` |
| TC-PARSE-010 | 地址顿号 | 地址含 `、` | 原样保留 |
| TC-PARSE-011 | 脱敏手机 | `136xxxx0000` | VALID，保留 x |
| TC-PARSE-012 | 缺电话 | `只有姓名没有电话` | E103、E105 |
| TC-PARSE-013 | 缺姓名 | `13800138000，浙江省...` | E102 |
| TC-PARSE-014 | 电话格式错误 | `12345` | E104 |
| TC-PARSE-015 | 缺地址 | `菜小鸟，13800138000，` | E105 |
| TC-PARSE-016 | 电话括号 | `（13800138000）李四，...` | VALID |
| TC-PARSE-017 | 座机 | `0571-88888888` | 规范化为 `057188888888` |
| TC-PARSE-018 | +86/英文地址 | `John, +86 138-0013-8000, Room 301,...` | VALID |
| TC-PARSE-019 | 英文姓名 | `John Smith,...` | VALID |
| TC-PARSE-020 | 空行跳过 | 三行夹空行 | skippedLines=1，行号 1/3 |
| TC-PARSE-021 | 首行空 | 第 2 行合法 | lineNo=2 |
| TC-PARSE-022 | 全角数字 | `１３８...` | 电话转半角 |
| TC-PARSE-023 | 姓名含数字 | `菜小鸟12，...` | E106 |
| TC-PARSE-024 | 地址 260 字 | 260 字地址 | E107 |
| TC-PARSE-025 | 电话 21 位 | 21 位数字 | E108 |
| TC-PARSE-026 | 单行 501 | 501 字符 | E110 |
| TC-PARSE-027 | 纯空白行 | `"   "` | 跳过 |
| TC-PARSE-028 | 原文标点 | 地址含 `#`、`-`、`（）` | 原样保留 |
| TC-PARSE-029 | 同行两号码 | 两个手机号 | 取前者，后者留地址 |
| TC-PARSE-030 | 幂等 | 连续解析两次 | 结果相同 |
| TC-PARSE-031 | 部分无效 | 3 行中 1 行缺电话 | 2 VALID、1 INVALID |
| TC-PARSE-032 | 空文本 | Parser 空串 | parser 无 items；**API 400/40001 由 TC-API-PARSE-002 覆盖** |
| TC-PARSE-033 | 空格分隔 | 空格分隔三段 | VALID |
| TC-PARSE-034 | 无分隔符 | 姓名电话地址相连 | VALID |
| TC-PARSE-035 | 单字姓名 | `王，...` | VALID |
| TC-PARSE-036 | 单字+座机 | `李，0571-...` | VALID |
| TC-PARSE-037 | 50 汉字姓名 | 50 个汉字 | VALID |
| TC-PARSE-038 | 51 汉字姓名 | 51 个汉字 | E111 |
| TC-PARSE-039 | 非法前置候选回退 | `菜小鸟12，张三，138...` | VALID，姓名=张三 |
| TC-PARSE-040 | 中文括号+空白 | `（ 138... ）李四，...` | 地址无 `（  ` |
| TC-PARSE-041 | 英文括号+空白 | `( 138... ) 李四，...` | 地址无括号 |
| TC-PARSE-042 | 多空格 | `（   138...   ）` | VALID，地址干净 |
| TC-PARSE-043 | 地址正常括号 | `浙江省（西湖区）` | 保留 |
| TC-PARSE-044 | 20 位电话 | 20 位纯数字 | E104 |
| TC-PARSE-045 | 21 位电话 | 21 位纯数字 | E108 |
| TC-PARSE-046 | 30 位电话 | 30 位纯数字 | E108 |
| TC-PARSE-047 | 31 位电话 | 31 位纯数字 | E108（不是 E103） |
| TC-PARSE-048 | 50 位电话 | 50 位纯数字 | E108 |
| TC-PARSE-049 | 50 增补平面姓名 | 50 个 `𠀀` | VALID（Unicode 码点=50） |
| TC-PARSE-050 | 51 增补平面姓名 | 51 个 `𠀀` | E111 |
| TC-PARSE-051 | Room 301 无姓名 | Room 301 开头 | E102，地址无 `,,` |
| TC-PARSE-052 | Hangzhou 无姓名 | Hangzhou 开头 | E102 |
| TC-PARSE-053 | Room 英文逗号 | Room 301 开头 | E102，地址无 `,,` |
| TC-PARSE-054 | parse(null) | null | `IllegalArgumentException` |
| TC-PARSE-055 | 地址 255 | 255 字 | VALID |
| TC-PARSE-056 | 地址 256 | 256 字 | E107 |
| TC-PARSE-057 | 单行 500 | 500 字符 | 不出现 E110（可 E107） |
| TC-PARSE-058 | 单行 501 | 501 字符 | E110 |
| TC-PARSE-059 | E109 防回归 | 混合错误文本 | 错误码不含 E109 |
| TC-PARSE-060 | 错误顺序 | 无姓名/电话/地址 | E103、E102、E105 |
| TC-PARSE-061 | 合法姓名+后置非法候选 | `张三，菜小鸟12，138...，地址` | VALID，地址无菜小鸟12 |
| TC-PARSE-062 | 合法姓名+后置非法特殊字符 | `张三，王五!，138...，地址` | VALID，地址无王五! |
| TC-PARSE-063 | 非法前置+合法姓名 | `菜小鸟12，张三，...` | VALID，地址干净 |
| TC-PARSE-064 | 姓名,地址,电话 | `张三，地址，138...` | 地址保留 |
| TC-PARSE-065 | 地址栋/室 | `张三，...12栋301室，138...` | 不删除 12栋301室 |
| TC-PARSE-066 | 电话后地址括号 | `张三，138...，地址（东门）` | 括号保留 |
| TC-PARSE-067 | 多非法候选+地址 | `菜小鸟12，王五!，138...，地址` | INVALID/E106，地址干净 |
| TC-PARSE-068 | 单非法候选+地址 | `菜小鸟12，138...，地址` | INVALID/E106，地址干净 |
| TC-PARSE-069 | 非法候选+地址关键词 | `菜小鸟12，地址，138...` | INVALID/E106，地址保留 |
| TC-PARSE-070 | 全非法+无地址 | `菜小鸟12，王五!，138...` | INVALID/E106/E105，地址 null |
| TC-PARSE-071 | 全非法+地址 | 两个非法候选 + 地址 | INVALID/E106，部分地址可解释 |

---

## 4. 解析 API 用例（TC-API-PARSE）

`AddressControllerTest` 使用 `@WebMvcTest`，Parser 用 Mock。

| 编号 | 场景 | 期望 |
| --- | --- | --- |
| TC-API-PARSE-001 | 正常请求 | 200/code=0 |
| TC-API-PARSE-002 | 空文本 | 400/40001，violations.rawText |
| TC-API-PARSE-003 | null 文本 | 400/40001 |
| TC-API-PARSE-004 | 20000 字符 | 进入 parser，200 |
| TC-API-PARSE-005 | 20001 字符 | 400/40003 |
| TC-API-PARSE-006 | 200 有效行 | 200 |
| TC-API-PARSE-007 | 201 有效行 | 400/40003 |
| TC-API-PARSE-008 | 40003 结构 | data.violations[0].field=rawText |
| TC-API-PARSE-009 | 超限不进 Parser | `never`/`verifyNoInteractions` |
| TC-API-PARSE-010 | 不写数据库 | 无 Repository/JDBC，仅 parser 调用 |
| TC-API-PARSE-011 | BOM+CRLF | 200 行不误判 201 |
| TC-API-PARSE-012 | 数量优先 | 20001 空白 → 40003 而非 40001 |
| TC-HTTP-001 | 真实 Parser 正常 | 200，items 字段正确 |
| TC-HTTP-002 | 20000 输入层通过 | 200，真实 Parser 返回 E110（单行） |
| TC-HTTP-003 | 20001 拒绝 | 400/40003，Spy 验证未调用 Parser |
| TC-HTTP-004 | 200 行进入真实 Parser | 200，items=200 |
| TC-HTTP-005 | 201 行拒绝 | 400/40003，未调用 Parser |
| TC-HTTP-006 | 混合有效/无效 | 2 VALID / 1 INVALID |
| TC-HTTP-007 | E109 不出现在响应 | 响应字符串不含 E109 |
| TC-HTTP-008 | 无 Repository/JDBC | 仅真实 Parser 调用 |

---

## 4.1 MS-04 批量录入浏览器用例

执行脚本：`frontend/scripts/ms04-browser-check.mjs`（Playwright + 本机 Chrome，Vite dev + 实际后端）。

| 编号 | 场景 | 实际结果 |
| --- | --- | --- |
| TC-UI-001 | 首页打开 | 通过；标题/侧栏/空状态 |
| TC-UI-002 | 单条联系人 | 通过；1 行结果 |
| TC-UI-003 | 多条联系人 | 通过；3 行结果 |
| TC-UI-004 | 中文逗号 | 通过 |
| TC-UI-005 | 英文逗号 | 通过 |
| TC-UI-006 | Tab | 通过 |
| TC-UI-007 | 空行 | 跳过空行，不计记录 |
| TC-UI-008 | 解析结果展示 | 通过 |
| TC-UI-009 | 异常数据展示 | 通过 |
| TC-UI-010/011/012 | 修改姓名/电话/地址 | 通过 |
| TC-UI-013 | 恢复原始内容 | 通过 |
| TC-UI-014 | 人工修改标记 | 通过 |
| TC-UI-015 | Unicode 姓名 | 51 码点提示超限；50 扩展平面通过 |
| TC-UI-016 | 保存确认弹窗 | 通过 |
| TC-UI-017 | INVALID 不提交 | 通过；实际提交 2，未修正 1 行 |
| TC-UI-018 | 保存成功 | 通过；实际保存 3 条 |
| TC-UI-019 | 保存失败保留数据 | 通过（浏览器层模拟 50002；实际后端 50002 已有 MySQL 测试） |
| TC-UI-020 | 连续点击防重复 | 通过（模拟成功响应，请求计数 1） |

控制台：无未预期错误；模拟 500 的资源错误在断言中显式排除。

---

## 4.2 MS-05 地址簿浏览器用例

执行脚本：`frontend/scripts/ms05-browser-check.mjs`（Playwright + 本机 Chrome，Vite dev + 实际后端/MySQL）。

| 编号 | 场景 | 实际结果 |
| --- | --- | --- |
| TC-ADDR-UI-001 | 地址簿打开 | 通过 |
| TC-ADDR-UI-002/003 | 真实列表与 total | 通过 |
| TC-ADDR-UI-004/005/006 | 姓名/电话/地址搜索 | 通过 |
| TC-ADDR-UI-007/008 | `%`/`_` 字面搜索 | 通过 |
| TC-ADDR-UI-009/010 | 50/51 Unicode 码点关键词 | 50 通过；51 前端阻止 |
| TC-ADDR-UI-011 | 重置搜索 | 通过 |
| TC-ADDR-UI-012/013 | 分页/每页数量 | 通过 |
| TC-ADDR-UI-014/015 | 翻页/搜索清空选择 | 通过 |
| TC-ADDR-UI-016/017 | 新建/编辑 | 通过 |
| TC-ADDR-UI-018 | 编辑后刷新 | 通过 |
| TC-ADDR-UI-019 | 单条删除 | 通过 |
| TC-ADDR-UI-020/021/022 | 批量选择/确认/仅提交选中 ID | 通过；请求体 2 个 ID |
| TC-ADDR-UI-023 | 删当前页最后一条自动退页 | 通过 |
| TC-ADDR-UI-024 | 无数据/无搜索结果 | 通过 |
| TC-ADDR-UI-025 | 网络错误提示 | 通过（Playwright 路由模拟 50002） |

---

## 5. MS-03 MySQL 集成用例

使用独立 `express_address_book_test`，测试数据仅使用 `199` 前缀并在测试前后清理；不操作主库业务数据。

| 编号 | 场景 | 实际验证 |
| --- | --- | --- |
| TC-MYSQL-001 | 新增/查询/编辑/删除/更新时间 | create → get → PUT → get → DELETE → 404；update_time 前进 |
| TC-MYSQL-002 | 分页/排序/总记录数 | total/current/size/pages；create_time DESC,id DESC |
| TC-MYSQL-003 | 姓名/电话/地址关键词 | 三种字段均可命中 |
| TC-MYSQL-004 | LIKE 特殊字符 | `%`、`_`、`!`、`\` 均按字面匹配 |
| TC-MYSQL-005 | 批量保存 | 2 条 savedCount=2，可查询 |
| TC-MYSQL-006 | 批量保存 200 / 空列表 | 200 成功；空列表 40001 |
| TC-MYSQL-007 | 批量 201 / 字段错误 | 40003（items）；字段错误 40001 |
| TC-MYSQL-008 | 批量删除/重复/部分不存在/全部不存在 | 去重、部分删除、全不存在 40401 |
| TC-MYSQL-009 | 事务回滚 | 第二条 DB 失败后第一条回滚，count=0 |
| TC-MYSQL-010 | 逻辑删除 | is_deleted=1；GET 404；列表过滤 |
| TC-MYSQL-011 | 404/40001/40003 | 缺失记录、非法 id、201 ids |
| TC-MYSQL-012 | 分页 JSON 类型/ID/日期 | total/current/size/pages 为 Number；records[].id 为 String；时间格式 `yyyy-MM-dd HH:mm:ss` |
| TC-MYSQL-013 | keyword 码点边界 | ASCII/中文/扩展平面 50/51；trim 后 50；空/null |
| TC-MYSQL-014 | page/size 字段映射 | page=0/-1、size=0/101 → field=page/size |
| TC-MYSQL-015 | Unicode HTTP | 50 BMP/50 Ext 成功；51 BMP/51 Ext 40001 |
| TC-MYSQL-016 | 前端状态字段忽略 | isEdited/original/rawLine/rawKey/status/errors 不写库 |
| TC-MYSQL-017 | API 层事务回滚 | 测试库临时触发器令第二条 DB 失败；50002；第一条回滚 |
| TC-MYSQL-018 | 数据库异常 | 50002，中文提示，不泄露 SQL/密码/堆栈 |
| TC-MYSQL-019 | 原始电话格式 | `+86 199-0000-0092` 保存为 `19900000092` |

> 这些是真实 MySQL 测试，不是 Mock 测试；主库 `express_address_book` 未被写入。

---

## 6. 安全 / 数据库 / 监听用例

| 编号 | 场景 | 期望 |
| --- | --- | --- |
| TC-SEC-02 | 仓库无明文口令 | `application-local.yml` 忽略，示例仅占位 |
| TC-SEC-08 | 8080/5173 仅本机 | `netstat` 只 127.0.0.1 |
| TC-SEC-11 | root 本机连接 | root 仅 localhost/127.0.0.1，无 `root@'%'` |
| TC-SEC-13 | DB 失败处理 | 500/50002 通用错误 |
| TC-SEC-14 | MySQL 监听 | 建议 127.0.0.1:3306 |

---

## 7. 边界覆盖清单

必须覆盖并已覆盖：

- 姓名 50/51 BMP、50/51 增补平面码点；
- 地址 255/256；
- 电话 20/21/30/31/50 位；
- 单行 500/501；
- 原文 19999/20000+（Parser 层）与 API 20000/20001；
- 200/201 有效行；
- BOM、CRLF、空行；
- 地址内部多逗号、顿号、括号；
- 电话括号空白；
- 非法首候选 + 合法候选；
- E109 防回归。

---

## 8. 实际执行结果（2026-11-14）

```text
mvn -B clean test
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| 测试类 | 数量 | 失败/错误/跳过 |
| --- | --- | --- |
| `AddressParserServiceTest` | 86 | 0/0/0 |
| `AddressControllerTest`（MockMvc） | 12 | 0/0/0 |
| `AddressParseIntegrationTest`（真实 Parser+HTTP） | 8 | 0/0/0 |
| `AddressApiMysqlTest`（真实 MySQL） | 16 | 0/0/0 |
| `AddressSaveDTOTest` | 10 | 0/0/0 |
| `LikeEscaperTest` | 4 | 0/0/0 |
| `CodePointLengthTest` | 5 | 0/0/0 |
| 基础测试（R/health/异常/DB mock） | 5 | 0/0/0 |
| **合计** | **146** | **0/0/0** |

前端：本轮未修改前端，沿用此前 `npm run build` 通过。

**未执行**：Playwright 纳入 CI、正式压力测试。MySQL 集成与 MS-04/MS-05 浏览器脚本已实际执行；地址簿高级配置未开发。

---

## 9. 结论与限制

- Parser 单测、Controller MockMvc、真实 Parser/HTTP、MySQL 集成必须分开引用，不能互相冒充。
- TC-PARSE-032 的 API 400/40001 由 Controller/HTTP 测试覆盖，Parser 单测只验证空结果。
- 性能只做了临时 100 行探针（约 1.30ms/次），不是正式压测。
- MS-03 已实现地址 CRUD、分页搜索、批量保存/删除；MS-04/MS-05 已实现批量录入与地址簿管理前端及 Playwright 端到端脚本；Playwright CI 尚未实现。
- MySQL 测试使用独立 `express_address_book_test`；主库未被测试写入。

---

## 10. MS-08 项目推导验收标准 AC-21～AC-32

> 以下 AC 根据 `requirements.md` 的 CR/FR/BR/NFR 与 MS-08 路线图推导，是项目级验收标准，不是课程任务书原文。

| 编号 | 验收内容 | 优先级 | 主要依据 | 对应测试/证据 | 状态 |
| --- | --- | --- | --- | --- | --- |
| AC-21 | 地址解析正确性 | P0 | FR-01～03、BR-01～08 | Parser 86 tests；MS08 全量 | PASS |
| AC-22 | 解析 API 输入边界 | P0 | FR-02、BR-05、NFR-02 | Controller/HTTP、200/201 限制 | PASS |
| AC-23 | 地址 CRUD、分页与搜索 | P0 | FR-07～13、BR-10～14 | AddressApiMysqlTest、MS05 | PASS |
| AC-24 | 批量事务与服务端校验 | P0 | FR-05、FR-19、BR-09/11/12 | 真实 MySQL 事务、201 拒绝 | PASS |
| AC-25 | 批量录入端到端流程 | P0 | FR-01～06 | MS04、`ms08-e2e-check.mjs` | PASS |
| AC-26 | 地址簿端到端流程 | P0 | FR-07～14 | MS05、`ms08-e2e-check.mjs` | PASS |
| AC-27 | Web 基础实践回归 | P0 | FR-15、FR-16 | MS06.2～06.9 | PASS |
| AC-28 | Servlet URI 映射回归 | P0 | FR-17、课程 2.20 | MS07 后端 14 + 浏览器；Allow 回归 | PASS |
| AC-29 | API 契约与异常处理 | P0 | FR-18、FR-19 | 统一 R/violations、异常状态 | PASS |
| AC-30 | 安全与数据保护 | P1 | FR-20、BR-16/20/22、NFR-05/11 | 安全扫描、监听、账号、错误响应；MySQL X 33060 暴露 | **PARTIAL（P1 未关闭）** |
| AC-31 | 性能与浏览器兼容性 | P1 | NFR-01、NFR-03 | `ms08-perf-check.mjs`；Chrome/Edge 完整矩阵 | PASS |
| AC-32 | 工程质量与交付复现 | P1 | NFR-06～10、DoD | build/160 tests/脚本/证据；CI 未运行 | PASS（CI 为 P2） |

### 10.1 通过条件

- AC-21～AC-29：对应测试全部通过，无 P0/P1；
- AC-30：无硬编码凭据、无未转义 HTML、无主库写入；但 MySQL X Plugin 33060 监听 `0.0.0.0`，且防火墙存在 mysqld 全端口入站允许，**PARTIAL/P1**；无认证不作为缺陷；
- AC-31：100 行解析 P95 ≤300ms；1 万条查询 P95 ≤500ms；Chrome 与 Edge 5 宽度/页面矩阵通过；
- AC-32：build、后端全量测试、Playwright、证据目录和文档可复现；CI 未运行记为 P2。

---

## 11. MS-08 R1～R5 测试轮次

| 轮次 | 范围 | 执行状态 |
| --- | --- | --- |
| R1 | Git 基线、环境、隔离测试库、测试资产、AC 定义、追踪矩阵 | 完成 |
| R2 | L1 Parser、L2 MockMvc/真实 HTTP、DTO/Unicode/LIKE、L3 MySQL、事务 | 完成：160 tests |
| R3 | MS04、MS05、MS06、MS07、核心 E2E | 完成：全部 BROWSER_OK / `MS08_E2E_OK` |
| R4 | TC-SEC、TC-PERF、浏览器兼容性、响应式 | 完成（Edge 完整矩阵）；CI NOT RUN；AC-30 PARTIAL/P1 |
| R5 | P0/P1/P2 缺陷、405 Allow、构建、全量回归、证据归档 | 完成；见 MS-08 报告 |

### 11.1 层级边界

- L1 Parser 单测：纯内存；
- L2 MockMvc：Controller 契约；
- L2 真实 Parser + HTTP：Spring Boot 真实 HTTP；
- L3 MySQL：独立 `express_address_book_ms08_test`；
- L4 Playwright：真实浏览器 + 真实后端；
- 测试中的 Playwright 路由模拟只用于故障注入，不冒充真实业务响应。

---

## 12. MS-08-R2 人工处置验收（PENDING）

D08 的 MySQL X Plugin/防火墙风险尚未处置；人工指南：

[`docs/security/ms-08-mysqlx-risk-and-manual-remediation.md`](security/ms-08-mysqlx-risk-and-manual-remediation.md)

| 编号 | 内容 | 当前状态 |
| --- | --- | --- |
| R2-01 | X Plugin 已禁用或仅本机监听 | PENDING |
| R2-02 | 无不必要宽泛 mysqld 入站规则 | PENDING |
| R2-03 | 33060 不对 LAN 开放 | PENDING |
| R2-04 | 3306 保持本机监听 | PENDING |
| R2-05 | 项目 JDBC 正常 | PENDING |
| R2-06 | Maven 160 通过 | PENDING（处置后重跑） |
| R2-07 | 主库/旧测试库未修改 | PENDING（处置后核对） |
| R2-08 | 配置/防火墙/监听/回归证据完整 | PENDING |

R2 只能在用户完成人工处置并再次授权后执行；不得将本表预填为 PASS。
