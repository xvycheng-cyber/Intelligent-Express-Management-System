# MS-02.2 最终缺陷修复与准入报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-02.2 地址解析器第二轮缺陷修复与最终准入 |
| 范围 | Parser 缺陷修复 + 真实 Parser/HTTP 集成测试；**无地址 CRUD** |
| 测试 | `mvn clean test`：**116 tests, 0 failures, 0 errors, 0 skipped** |
| 打包 | `mvn clean package -DskipTests`：BUILD SUCCESS |

---

## 1. P0 缺陷修复

### 1.1 合法姓名后的非法候选残留

**修复前问题**

```text
张三，菜小鸟12，13800138000，浙江省杭州市西湖区XX小区
→ VALID；姓名=张三；地址=菜小鸟12，浙江省...
```

`NameExtractor` 只删除选中姓名之前的非法候选。

**修复**

- 选中合法姓名后：
  - 删除其前面的全部非法姓名候选；
  - 删除其后的 **E106 明确非法姓名候选**；
  - E111 长片段保留，避免把 260 字地址误删；
- 没有合法姓名时：状态 INVALID，所有非法姓名候选从地址剔除；
- `NameMatch.removalRanges` + `AddressExtractor` 统一处理区间；
- `rawLine` 始终保留原样。

**实际结果**

```text
张三，菜小鸟12，138...，浙江省...     → VALID / 张三 / 浙江省...
张三，王五!，138...，浙江省...        → VALID / 张三 / 浙江省...
菜小鸟12，张三，138...，浙江省...     → VALID / 张三 / 浙江省...
```

新增：TC-PARSE-061、062、063。

### 1.2 所有姓名候选均非法

- 多个非法候选全部检查，不再只处理第一个；
- 无合法姓名 → INVALID；
- 所有非法候选从地址剔除；
- 剩余地址为空 → E105；
- 保留完整 rawLine 与 E106/E111 错误。

实际结果：

```text
菜小鸟12，王五!，138...，浙江省... → INVALID / E106 / 地址=浙江省...
菜小鸟12，王五!，138...           → INVALID / E106,E105 / 地址=null
```

新增：TC-PARSE-067～071。

### 1.3 地址区间安全

- 姓名、电话、地址区间全部来自原串；
- 电话区间包含成对包裹符与空白时才删除；
- 地址中 `12栋301室`、`（东门）`、逗号、顿号保留；
- 删除字段后只去掉相邻重复分隔符，不修改地址内部标点。

实际结果：

- `张三，浙江省...12栋301室，138...` → 地址完整；
- `张三，138...，浙江省...（东门）` → 括号完整；
- `Room 301，138...，浙江省...` → INVALID/E102，地址无 `,,`。

---

## 2. 候选区间处理规则

1. 电话区间：整段优先 → 模式匹配 → 纯数字兜底；
2. 姓名候选：电话前首段 → 电话前末段 → 电话后首段；
3. 选中合法姓名后，删除其前所有非法候选与其后 E106 候选；
4. 无合法姓名时删除所有非法候选；
5. E111 后置片段不盲目删除，避免误删长地址；
6. 地址 = 原串剔除上述区间，首尾 trim，内部标点保留；
7. 原始 rawLine 不做任何修改。

---

## 3. 真实 Parser + HTTP 集成测试

新增 `AddressParseIntegrationTest`：

- `@SpringBootTest` + `@AutoConfigureMockMvc`；
- `AddressParserService` 为真实 Spring Bean（`@SpyBean` 只用于验证是否调用，实际执行真实方法）；
- 覆盖：
  - 正常请求；
  - 20000 字符输入层通过，真实 Parser 对单行返回 E110；
  - 20001 字符 → 400/40003，未调用 Parser；
  - 200 有效行进入真实 Parser；
  - 201 有效行 → 400/40003，未调用 Parser；
  - 混合有效/无效；
  - 响应不含 E109；
  - 解析接口无 Repository/JDBC 调用。

集成测试数量：**8**。

---

## 4. CodePointLength

`CodePointLengthValidator` 使用：

```java
Character.codePointCount(value, 0, value.length())
```

测试覆盖：

- 1 个码点；
- 50 个 BMP 汉字；
- 51 个 BMP 汉字；
- 50 个 CJK 扩展平面汉字；
- 51 个 CJK 扩展平面汉字。

结果：**5 个测试全部通过**。

后续约束：

- MS-03 的 `AddressSaveDTO.receiverName` 必须使用 `@CodePointLength(min=1,max=50)`；
- 不允许仅用 `@Size(max=50)` 替代；
- MS-04 前端姓名输入必须使用 `Array.from(value).length`，不能仅依赖 `maxlength`。

---

## 5. 输入边界

| 项 | 结果 |
| --- | --- |
| 20000 字符 | 通过 API 输入限制；真实 Parser 执行；单行 >500 返回 E110 |
| 20001 字符 | 400/40003，未进入 Parser |
| 200 有效行 | 通过；真实 Parser 返回 200 条 items |
| 201 有效行 | 400/40003，未进入 Parser |
| 20001 空白 | 400/40003（数量优先于空值） |
| BOM + CRLF 200 行 | 仍为 200 行 |

---

## 6. 电话错误码

| 位数 | 错误码 | 说明 |
| --- | --- | --- |
| 20 | E104 | 不超过 20，但不符合手机/座机模式 |
| 21 | E108 | 超过 20 |
| 30 | E108 | 超过 20 |
| 31 | E108 | 超过 20，不再落入 E103 |
| 50 | E108 | 超过 20 |

---

## 7. 性能验证

临时探针（当前代码）：

- 100 行；
- 20,621 字符；
- 33 VALID / 67 INVALID；
- 预热 20 次，执行 100 次；
- min **0.79 ms**，avg **1.30 ms**。

结论：未发现明显正则回溯；模式受单行 500 字符限制。该结果**不是正式压力测试**，没有加入 CI 性能门禁。

---

## 8. 全部测试结果

```text
mvn -B clean test
Tests run: 116, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
mvn exit code: 0
```

| 测试类 | 数量 | 结果 |
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

前端本轮未修改，沿用此前 `npm run build` 通过结果；未重新执行前端构建。

---

## 9. 尚未执行的测试

1. 真实 MySQL 集成测试（本阶段无 CRUD）。
2. MySQL 读写前后行数对比（无 DB 写入端到端）。
3. Playwright/浏览器人工走查。
4. 正式压测；当前仅临时探针。
5. 前端姓名输入实际接入（当前只有工具函数，无录入页）。
6. 真实 `AddressSaveDTO` 的 `@CodePointLength` 接入（MS-03）。

**明确区分**：本报告中的 116 个测试 = Parser 86 + Controller MockMvc 12 + 真实 Parser/HTTP 8 + 码点 5 + 基础 5；不是 116 个浏览器/数据库测试。

---

## 10. MS-03 准入结论

**有条件通过。**

已完成：

- 合法姓名后的 E106 候选残留修复；
- 所有候选非法时的 INVALID 与地址清理；
- 真实 Parser + HTTP 集成测试；
- 电话 20/21/30/31/50、姓名 1/50/51 与扩展平面；
- API 20000/20001、200/201、40003、未调用 Parser；
- E109 防回归、错误顺序、parse(null)。

进入 MS-03 前仍需遵守：

1. `AddressSaveDTO` 使用 `@CodePointLength(min=1,max=50)`，不得只用 `@Size`；
2. 前端姓名输入使用 `Array.from(value).length`；
3. 不改回 E109、不破坏错误顺序与输入上限；
4. 不把 MySQL/Playwright 未执行项写成通过。

当前暂停，不进入 MS-03 CRUD。
