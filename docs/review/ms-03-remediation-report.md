# MS-03.1 接口契约修复与前端联调准入报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-03.1 三个 P0 契约修复 + P1 回归 |
| 测试 | `mvn -B clean test`：**146 tests, 0 failures, 0 errors, 0 skipped** |
| 打包 | `mvn -B clean package -DskipTests`：BUILD SUCCESS |
| 数据库 | 独立 `express_address_book_test`；主库未被测试写入 |
| 前端 | 本轮未开发 Vue 页面 |

---

## 1. 三个 P0 修复

### P0-1 分页元数据 JSON 类型

**原因**：`JacksonConfig` 同时对 `Long.class` 和 `Long.TYPE` 注册 `ToStringSerializer`，导致 `PageResult` 的 `long total/current/size/pages` 也变成字符串。

**修复**：
- 仅保留 `Long.class` 的字符串序列化，用于实体 ID；
- 删除 `Long.TYPE` 注册，`long` 计数保持 JSON Number；
- `AddressVO` 仍使用 `Long id`，输出字符串，避免 JS 精度丢失。

**实际 HTTP**：

```json
{"code":0,"message":"OK","data":{"records":[],"total":0,"current":1,"size":10,"pages":0}}
```

有记录时：

```json
{"id":"1735","createTime":"2026-11-14 11:20:39","updateTime":"2026-11-14 11:20:39"}
```

`total/current/size/pages` 为 Number，`id/createTime/updateTime` 符合契约。

### P0-2 keyword 长度

**修复**：
- Controller 移除无效的 `@Size`；
- `AddressService.page` 先 `strip()`，再用 `codePointCount` 校验；
- 超过 50 码点返回 400/40001，`violations[].field=keyword`；
- 空/null 不筛选；`LikeEscaper` 逻辑不变。

实际：

```text
50 ASCII/中文/扩展平面码点 → HTTP 200
51 ASCII/中文/扩展平面码点 → HTTP 400/40001, field=keyword
首尾空格 + 50 码点 → HTTP 200
空/null → HTTP 200
```

### P0-3 page/size 字段映射

`GlobalExceptionHandler` 只将 `list.page`、`list.size` 映射为 `page`、`size`，其他 property path 保持原样。

实际：

```text
page=0/-1 → 40001, field=page
size=0/101 → 40001, field=size
```

---

## 2. 分页 JSON 类型测试

`AddressApiMysqlTest` 增加：

```java
jsonPath("$.data.total").isNumber()
jsonPath("$.data.current").isNumber()
jsonPath("$.data.size").isNumber()
jsonPath("$.data.pages").isNumber()
jsonPath("$.data.records[0].id").isString()
```

不再使用宽松 `.value(3)` 代替类型断言。

---

## 3. ID 序列化测试

- `records[].id` 实际为 JSON String；
- 创建响应 `data.id` 为 String；
- `BatchSaveResult.ids` 中的 `Long` 仍为字符串；
- 分页计数不再被 ID 序列化规则误伤。

---

## 4. keyword 长度测试

`AddressApiMysqlTest.keywordLengthUsesCodePointsAfterTrim` 覆盖：

- 50 ASCII；
- 51 ASCII；
- 50 中文；
- 51 中文；
- 50 扩展平面；
- 51 扩展平面；
- 首尾空格后 50；
- 空关键词；
- null（无参数）。

全部按 `codePointCount` 语义执行。

---

## 5. page/size 字段映射测试

`pageSizeValidationUsesStableFieldNames` 覆盖：

- page=0；
- page=-1；
- size=0；
- size=101；
- 合法 page=1、size=100。

失败字段分别为 `page`、`size`，不再出现 `list.page`、`list.size`。

---

## 6. Unicode HTTP 测试

真实 Controller + 真实 MySQL 测试：

- 50 个 BMP 汉字 → 200；
- 51 个 BMP 汉字 → 400/40001；
- 50 个扩展平面汉字 → 200；
- 51 个扩展平面汉字 → 400/40001。

生产 `AddressSaveDTO.receiverName` 使用 `@CodePointLength(min=1,max=50)`；不是只测独立 Validator。

---

## 7. API 层批量事务回滚

在独立测试库创建临时触发器：

```sql
CREATE TRIGGER ms031_fail_insert BEFORE INSERT ON express_address
FOR EACH ROW
BEGIN
  IF NEW.phone = '19999999999' THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'controlled failure';
  END IF;
END
```

请求中两条 DTO 均合法：

- 第一条正常写入；
- 第二条触发受控数据库异常；
- Controller 返回 500/50002；
- 第一条也回滚，`COUNT(*)=0`；
- 响应不包含触发器错误、SQL、密码、堆栈；
- 测试结束删除临时触发器。

这是真实 API 层事务回滚测试，不是 Service 层 300 字地址测试。

---

## 8. 数据库异常测试

`databaseExceptionDoesNotLeakDetails`：

- HTTP 500；
- 业务码 50002；
- 中文提示“数据库操作失败，请稍后重试”；
- 响应不含 SQL、`controlled failure`、`password`、`Exception`、`java.`、堆栈。

---

## 9. 电话和姓名格式约定

### 电话

`AddressSaveDTO.phone` 接受：

- 规范化手机/座机/脱敏；
- `+86 138-0013-8000`；
- `（13800138000）`；
- `138 0013 8000`。

`AddressService` 保存前调用 `PhoneExtractor.normalizePhone`，库中统一为去掉空格/短横/包裹符/`+86` 的形态。例如 `+86 199-0000-0092` → `19900000092`。

### 姓名

- 中文 1～50 个 Unicode 码点；
- 英文允许半角与全角字母，避免解析器输出 `Ｊｏｈｎ` 后保存无提示失败；
- 仍使用 `@CodePointLength`，不是 `@Size`。

### 日期

`AddressVO.createTime/updateTime` 使用 `yyyy-MM-dd HH:mm:ss`，与 API 文档示例一致；不再输出 ISO `T` 格式。

---

## 10. 全部实际测试结果

```text
mvn -B clean test（DB_USERNAME/DB_PASSWORD 指向独立测试库）
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
mvn exit code: 0

mvn -B clean package -DskipTests
BUILD SUCCESS
```

| 测试类 | 数量 | 结果 |
| --- | --- | --- |
| `AddressParserServiceTest` | 86 | 0/0/0 |
| `AddressControllerTest`（MockMvc） | 12 | 0/0/0 |
| `AddressParseIntegrationTest`（真实 Parser/HTTP） | 8 | 0/0/0 |
| `AddressApiMysqlTest`（真实 MySQL） | 16 | 0/0/0 |
| `AddressSaveDTOTest` | 10 | 0/0/0 |
| `LikeEscaperTest` | 4 | 0/0/0 |
| `CodePointLengthTest` | 5 | 0/0/0 |
| 基础测试 | 5 | 0/0/0 |
| **合计** | **146** | **0/0/0** |

测试库与主库最终计数均为 0；临时触发器已删除；主库未被测试写入。

---

## 11. 尚未执行的测试

1. Playwright/浏览器人工走查。
2. Vue 地址录入/列表页面（MS-04）。
3. `batchDelete` 的数据库失败注入（当前为单条逻辑删除 SQL）。
4. 正式性能压测。
5. 真实根主库的 CRUD 集成（测试只使用独立测试库）。
6. MySQL 测试依赖预创建 `express_address_book_test` 和 `DB_PASSWORD` 环境变量；未设置时跳过。

---

## 12. MS-04 准入结论

**有条件通过，可进入 MS-04。**

条件：

1. 前端姓名输入使用 `Array.from(value).length`；
2. 前端接收分页字段为 Number、ID 为 String；
3. 不提交 `isEdited/original/rawLine/rawKey/status/errors`；
4. 电话输入使用保存接口支持的格式或先经过解析器规范化；
5. 不回退 `keyword` 码点上限、`page/size` 字段映射、事务和 LIKE 转义；
6. Playwright 必须实际执行，不得用 MockMvc/MySQL 测试冒充浏览器测试。

**MS-03.1 完成，暂停，不进入 MS-04 页面开发。**
