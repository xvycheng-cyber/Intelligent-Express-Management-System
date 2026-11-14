# MS-02.1/02.2 缺陷修复与 API 准入报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-02.1/02.2 AddressParserService 修复 + 最小解析 API |
| 范围 | 只修复 P0/P1、补解析 Controller；**无地址 CRUD** |
| 测试 | `mvn clean test`：**116 tests, 0 failures, 0 errors, 0 skipped**（parser 86 + Controller MockMvc 12 + 真实 HTTP 8 + 码点 5 + 基础 5） |
| 打包 | `mvn clean package -DskipTests`：BUILD SUCCESS |
| 前端 | `npm run build`：成功 |

---

## 1. P0-1 姓名候选回退

**修复前**：`NameExtractor.locate` 遇到第一个非法候选立即返回 E106，`菜小鸟12，张三，138...` 得到 E106、地址混入 `张三`。

**修复**：
- 遍历所有候选，优先返回第一个合法候选；
- 已选中合法姓名时：剔除其前面的所有非法候选；剔除其后的 **E106** 非法候选（明确非法姓名片段）；E111 长片段保留，避免误删 260 字地址；
- 没有合法姓名时：返回 INVALID，并将所有非法姓名候选从地址中剔除；
- `NameMatch` 增加 `removalRanges`，`AddressExtractor` 同时剔除这些区间；
- 原始 `rawLine` 不变。

**实际测试**：`TC-PARSE-039`、`039b`、`039c`、`061`、`062` 通过；`张三，菜小鸟12，138...` 与 `张三，王五!，138...` 均 VALID、地址只剩浙江省…；`菜小鸟12，王五!，138...` 为 INVALID、地址干净。`TC-PARSE-024`（260 字地址）仍通过。

### 1.1 所有姓名候选均非法

- 没有合法姓名时状态 INVALID；
- 所有非法姓名候选都从地址剔除；
- 若剩余地址为空 → 追加 E105；
- 保留 rawLine；错误码保留 E106/E111。
- 测试：TC-PARSE-067（多非法+地址）、068（单非法+地址）、069（非法+地址关键词）、070（全非法无地址）、071（全非法+地址）。

---

## 2. P0-2 电话包裹符与空白

**修复前**：`（ 13800138000 ）李四，...` 返回 VALID，但地址以 `（  ` 开头。

**修复**：
- `PhoneExtractor` 在正则匹配后先扩展紧邻空白，再检查成对包裹符；
- 只有成对/可匹配的括号及其中空白随电话区间剔除；
- 地址内部正常括号不受影响；
- 姓名区间基于扩展后的电话结束位置计算。

**实际测试**：TC-PARSE-040（中文括号+空白）、041（英文括号+空白）、042（多空格）、043（地址正常括号）通过。

---

## 3. P0-3 解析 API 输入上限

新增：

| 文件 | 内容 |
| --- | --- |
| `controller/AddressController.java` | `POST /api/addresses/parse`，只解析不写库 |
| `dto/AddressParseRequest.java` | `{rawText}` 请求 |
| `parser/ParseInputGuard.java` | 调用 Parser 前校验长度/行数 |
| `common/BusinessException` | 增加可选 `data`，供 violations 返回 |
| `GlobalExceptionHandler` | 所有字段错误统一包装 `data.violations[]` |

规则：

- null → 400/40001；
- 空白 → 400/40001；
- `rawText > 20000` → 400/40003（先于空白判断）；
- 有效非空行 >200 → 400/40003；
- 长度优先于空白，数量优先于普通字段错误；
- 有效行用与 parser 一致的 `LineSplitter`：去 BOM、CRLF/CR/LF、空行不计数；
- 超限不进入 parser。

**实际测试**：TC-API-PARSE-001～012 通过，包括 20000/20001、200/201、40003 结构、`verifyNoInteractions`、BOM+CRLF、20001 空白优先 40003。

---

## 4. P1-1 长数字电话

- `PhoneExtractor` 纯数字兜底由 `\d{5,30}` 改为 `\d{5,}`；
- 长度 >20 返回 E108，5～20 返回 E104；
- 21/30/31/50 位均已测试；31 位不再落入 E103。

**实际测试**：TC-PARSE-025、044～048 通过。

---

## 5. P1-2 姓名长度语义

- Parser 使用 `codePointCount(0, length) > 50`；
- 新增 `@CodePointLength(min=1,max=50)` 及 `CodePointLengthValidator`，供后续姓名 DTO 复用；
- 前端新增 `src/utils/textLength.ts`：`Array.from(value).length`；
- 数据库仍 `VARCHAR(50)`，表/库 `utf8mb4`；JDBC `characterEncoding=utf8` 对应 Java UTF-8，连接字符集与表一致。
- 文档明确区分 Unicode Code Point 与 UTF-16 Code Unit，不再要求 `@Size(max=50)` 单独完成姓名长度。

**实际测试**：
- 50/51 普通汉字通过/E111；
- 50/51 个 `𠀀`（增补平面）通过/E111；
- `CodePointLengthTest` 5 个通过（1/50/51 普通与扩展平面）。

---

## 6. P1-3 英文地址/姓名歧义

- 英文地址词明确写入 `parser-design.md`：Room/Building/Street/Road/Avenue/Floor/Apt/Suite/Unit/Block/Lane/District/Province/City + 常见城市名；
- `Room 301`、`Hangzhou` 无姓名时返回 E102，不再被当姓名；
- `AddressExtractor` 删除字段后只去掉相邻重复分隔符，`Room 301,,` 变为 `Room 301，浙江省…`，不改写地址内部正常标点；
- `rawLine` 完整保留。

**实际测试**：TC-PARSE-051、052、053 通过；`Room 301` 地址不含 `,,`/`，，`。

**限制**：英文地址词是有限启发式，不是完整地名库；真实英文人名可能与城市名冲突，需人工修正。

---

## 7. P1-4 错误顺序

`AddressParserService.parseLine` 统一为：

1. 电话 E103/E104/E108；
2. 姓名 E102/E106/E111；
3. 地址 E105/E107。

TC-PARSE-060 验证无姓名/电话/地址的 `浙江省…` 行错误顺序为 E103、E102、E105。

---

## 8. P1-5 parse(null)

- `AddressParserService.parse(null)` 抛 `IllegalArgumentException("rawText must not be null")`；
- API 仍由 `ParseInputGuard` 返回 400/40001；
- 空字符串/空白文本仍按解析/API 契约处理。

**实际测试**：TC-PARSE-054 通过。

---

## 9. P1-6 性能探针

临时 Java 探针（未加入正式测试依赖）：

- 100 行，原文 20,621 字符；
- 每行接近 500 字符，混合有效/无效、逗号/Tab/分号分隔；
- 33 条 VALID、67 条 INVALID；
- 预热 20 次，执行 100 次：**min 0.82ms，平均 1.36ms**。

结论：未发现明显正则回溯；模式受长度限制。该结果不是正式压测，也未加入 CI 性能门禁。

---

## 10. 全部测试实际结果

```text
mvn -B clean test
Tests run: 116, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
mvn exit code: 0
```

| 测试类 | 数量 | 失败/错误/跳过 |
| --- | --- | --- |
| `AddressParserServiceTest` | 86 | 0/0/0 |
| `AddressControllerTest`（MockMvc + mock Parser） | 12 | 0/0/0 |
| `AddressParseIntegrationTest`（真实 Parser + HTTP） | 8 | 0/0/0 |
| `CodePointLengthTest` | 5 | 0/0/0 |
| 基础测试（R/health/异常/DB health mock） | 5 | 0/0/0 |
| **合计** | **116** | **0/0/0** |

```text
mvn -B clean package -DskipTests
BUILD SUCCESS
```

前端：

```text
npm run build
vue-tsc --noEmit + vite build
npm exit code: 0
```

补充边界：

- 电话 20/21/30/31/50 位；
- 地址 255/256；
- 单行 500/501；
- 原文 19999 / 20000+（Parser）与 API 20000/20001；
- 200/201 有效行；
- 50/51 增补平面汉字；
- E109 防回归；
- 错误顺序。

---

## 11. 尚未解决的问题

1. 真实 MySQL 集成测试未执行（本阶段不写库）。
2. Playwright/浏览器人工走查未执行。
3. 性能只是临时探针，不是正式压测。
4. 英文地址词是有限列表，不能保证覆盖所有英文地名。
5. `Smith, John` 仍不合并为英文姓名（逗号是字段分隔符）。
6. 前端只新增 `Array.from` 计数工具，尚无地址录入页面可接入；后续 MS-04 必须使用。
7. 当前解析 API 的 Controller 测试使用 mock Parser，未做真实 Parser + HTTP 的端到端测试；Parser 单测与 Controller 测试已分别覆盖。
8. 未实现任何地址 CRUD、批量保存/删除、分页搜索。

---

## 准入结论

MS-02.2 的 P0/P1 修复与解析 API 已完成，116 个测试全部通过；可以进入 MS-03，但 MS-03 不得回退本报告的 P0 约束，并需在地址保存 DTO 使用 `@CodePointLength`、前端使用 `Array.from`。
