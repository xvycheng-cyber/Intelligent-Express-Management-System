# MS-02 地址解析模块报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-02 `AddressParserService`（后由 MS-02.1 修复） |
| 范围 | 解析器 + 最小解析 API；无地址 CRUD、无数据库写入 |
| 当前测试 | `mvn clean test`：116 tests，0 failures，0 errors（parser 86 + Controller MockMvc 12 + 真实 HTTP 集成 8 + 码点 5 + 基础 5） |

> 本报告记录 MS-02 原始实现。P0-1 候选回退、P0-2 电话包裹符空白、P0-3 输入上限、P1 长数字/码点/英文歧义/parse(null)/错误顺序等修复见 [`ms-02-remediation-report.md`](ms-02-remediation-report.md)。原报告中“TC-PARSE-001～038 全部覆盖”及“String.length”描述已修正。

---

## 1. 创建的类和文件

包：`com.express.addressbook.parser`

| 文件 | 职责 |
| --- | --- |
| `AddressParserService` | 唯一入口，`@Service`，纯解析 |
| `LineSplitter` | BOM、CRLF/CR、空行、行号 |
| `PhoneExtractor` / `PhonePatterns` / `PhoneMatch` | 电话定位与规范化 |
| `NameExtractor` / `NamePatterns` / `NameMatch` | 姓名候选与校验 |
| `AddressExtractor` | 原串剔除姓名/电话，保留内部标点 |
| `AddressKeywordFilter` | 地址关键词/英文地址词排除 |
| `TextSegments` / `TextNormalizer` | 分隔符、包裹符、全半角 |
| `ParserLimits` / `ParserError` | 长度常量与 E102～E111 文案 |
| `ParsedItem` / `ParseResult` / `LineError` / `ParseStatus` | 输出结构 |
| `ParseInputGuard`（MS-02.1） | API 输入上限前置校验 |
| `strategy/AddressParseStrategy` | P2 扩展点，未实现 AI |

Controller：`AddressController`（仅 `POST /api/addresses/parse`）；DTO：`AddressParseRequest`；校验：`@CodePointLength`。

## 2. 解析流程

```text
parse(rawText)
 ├─ null → IllegalArgumentException
 ├─ 去 BOM、CRLF/CR → LF、按行切分、空行跳过但保留物理行号
 ├─ 单行 >500 → E110
 ├─ 逐行：等长匹配副本 → 电话定位 → 姓名候选 → 原串地址剔除 → 错误聚合
 └─ totalLines/effectiveLines/skippedLines/validCount/invalidCount/items
```

## 3. 姓名识别

- 候选顺序：电话前首段 → 电话前末段 → 电话后首段。
- 中文 `^[\p{IsHan}·]{1,50}$`；英文 `^[A-Za-z][A-Za-z .'\-]{0,49}$`。
- 长度按 Unicode 码点（MS-02.1），不用 `String.length()`；超过 50 → E111，1～50 非法字符 → E106。
- 单字姓名合法；CJK Ext-B `𠀀` 实测通过。
- 非法首候选不再阻断后续合法候选；选中合法姓名前的非法候选从地址剔除，选中后的片段不当姓名删除。

## 4. 电话识别

- 手机/脱敏手机/座机；`+86`、空格、短横、全角数字。
- 整段优先 → 模式匹配 → 纯数字兜底。
- 5 位以上纯数字：>20 位 E108，否则 E104；21/30/31/50 位均已实测。
- 包裹符与空白：只有成对括号（可含空白）随电话删除；地址内部正常括号保留。

## 5. 地址提取

- 从原串剔除姓名/电话区间，不 split + join。
- 只 trim 首尾空白/分隔符；内部英文逗号、顿号、`#`、`-`、括号逐字保留。
- 删除字段后相邻重复分隔符只去掉一个，避免 `Room 301,,`；不全局压缩地址内部标点。

## 6. 错误码

| 码 | 触发 |
| --- | --- |
| E102 | 无姓名候选 |
| E103 | 无电话候选 |
| E104 | 5～20 位纯数字但不匹配手机/座机 |
| E105 | 地址为空 |
| E106 | 姓名 1～50 码点但含非法字符/不匹配规则 |
| E107 | 地址 >255 |
| E108 | 纯数字电话候选 >20 位 |
| E110 | 单行 >500 |
| E111 | 姓名 >50 码点 |

**E109 已删除，不得重新引入**。错误顺序：电话 → 姓名 → 地址。

## 7. 测试

- Parser：86 个（TC-PARSE-001～071）。
- Controller MockMvc：12 个（TC-API-PARSE-001～012）。
- 真实 Parser + HTTP：8 个（TC-HTTP-001～008）。
- 码点校验：5 个。
- 基础：5 个。
- 总计 **116**，全部通过；详见 `test-plan.md` 与 MS-02.2 报告。

## 8. 尚未解决/边界

- `Smith, John` 仍不合并为英文姓名；逗号是字段分隔符。
- 英文地址词是有限启发式，不是完整地名库。
- `String.length` 已改为 Unicode 码点；前端需使用 `Array.from`。
- 真实 MySQL 集成、Playwright 未执行。
- 性能仅有临时 100 行探针（约 1.36ms/次），不是正式压测。

## 9. MS-03 准入

解析器与解析 API 已可用；进入 MS-03 前只需保持 116 测试全通过、不回归 E109/错误顺序/输入上限。地址 CRUD、批量保存/删除仍未实现。
