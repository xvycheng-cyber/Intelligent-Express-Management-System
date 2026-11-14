# MS-03.1 最终独立审查与前后端联调准入

| 项目 | 内容 |
| --- | --- |
| 审查日期 | 2026-11-14 |
| 版本 | MS-03.1 |
| 审查范围 | 三个 P0 契约修复、前后端字段契约、校验、事务、MySQL、安全 |
| 审查方式 | 源码/测试/文档阅读 + 真实 Maven/MySQL 测试 + 实际 HTTP 探针 |
| 代码修改 | **无；本轮只审查并生成本报告** |

---

## 一、三个 P0 的最终验证

### P0-1 分页元数据 JSON 类型

**验证结果：通过。**

源码：

- [PageResult.java](Intelligent-Express-Management-System/backend/src/main/java/com/express/addressbook/vo/PageResult.java) 的 `total/current/size/pages` 为 `long`；
- [JacksonConfig.java](Intelligent-Express-Management-System/backend/src/main/java/com/express/addressbook/config/JacksonConfig.java) 仅注册 `Long.class`，不再注册 `Long.TYPE`。

实际 HTTP：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "records": [],
    "total": 0,
    "current": 1,
    "size": 10,
    "pages": 0
  }
}
```

有记录时：

```json
{
  "records": [{
    "id": "1735",
    "receiverName": "审查乙",
    "phone": "19900000992",
    "fullAddress": "浙江省杭州市",
    "createTime": "2026-11-14 11:20:39",
    "updateTime": "2026-11-14 11:20:39"
  }],
  "total": 1,
  "current": 1,
  "size": 10,
  "pages": 1
}
```

自动化测试使用：

```java
jsonPath("$.data.total").isNumber()
jsonPath("$.data.current").isNumber()
jsonPath("$.data.size").isNumber()
jsonPath("$.data.pages").isNumber()
```

**结论**：四个分页字段为 JSON Number。

### P0-2 ID 仍为 JSON String

**验证结果：通过。**

实际响应：

```json
"id": "1735"
```

测试：

```java
jsonPath("$.data.records[0].id").isString()
```

`Long.class` 的序列化规则仍作用于：

- `AddressVO.id`
- `BatchSaveResult.ids` 中的元素
- 新建/编辑响应 `data.id`

分页 `long` 计数不再被误伤。

### P0-3 keyword 51 码点

**验证结果：通过。**

`AddressService.page` 先 `strip()`，再以 `codePointCount` 校验；超过 50 返回 400/40001，`field=keyword`。

实际 HTTP：

```text
keyword=51 个 ASCII → HTTP 400/40001，field=keyword
keyword=50 个 ASCII → HTTP 200
keyword=50 个中文 → HTTP 200
keyword=51 个中文 → HTTP 400/40001
keyword=50 个扩展平面 → HTTP 200
keyword=51 个扩展平面 → HTTP 400/40001
首尾空格 + 50 码点 → HTTP 200
空/null → HTTP 200
```

### P0-4 page=0 字段

**验证结果：通过。**

```text
GET /api/addresses?page=0
→ HTTP 400/40001
→ violations[0].field = "page"
```

### P0-5 size=101 字段

**验证结果：通过。**

```text
GET /api/addresses?size=101
→ HTTP 400/40001
→ violations[0].field = "size"
```

`GlobalExceptionHandler` 只映射 `list.page`、`list.size`，其他 property path 保持原样。

---

## 二、前后端契约审查

### 2.1 地址列表

```text
GET /api/addresses?page=1&size=10&keyword=西湖
```

响应：

```ts
interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

interface AddressVO {
  id: string
  receiverName: string
  phone: string
  fullAddress: string
  createTime: string // yyyy-MM-dd HH:mm:ss
  updateTime: string
}
```

**结论**：可直接映射 TypeScript；`id` 为 string，分页计数为 number。

### 2.2 新建与编辑

```text
POST /api/addresses
PUT /api/addresses/{id}
```

请求字段：

```ts
interface AddressSavePayload {
  receiverName: string
  phone: string
  fullAddress: string
  lineNo?: number
}
```

成功：

```ts
interface IdResult { id: string }
```

**结论**：请求不包含 `id/createTime/updateTime/isDeleted`；未知的 `isEdited/original/rawLine/rawKey/status/errors` 会被 Jackson 忽略，不会入库。

### 2.3 删除

```text
DELETE /api/addresses/{id}
→ { code: 0, message: "OK", data: null }
```

不存在/已删除 → 404/40401。

### 2.4 批量删除

```json
{ "ids": ["1", "3"] }
```

响应：

```ts
interface BatchDeleteResult { deletedCount: number }
```

- `ids` 为字符串数组；
- 1～200 个；
- 服务端去重；
- 部分不存在 → 删除存在的部分，返回实际数量；
- 全部不存在 → 404/40401。

### 2.5 批量保存

```json
{
  "items": [
    {
      "lineNo": 1,
      "receiverName": "菜小鸟",
      "phone": "13600136000",
      "fullAddress": "浙江省杭州市西湖区湖滨小区12栋301室"
    }
  ]
}
```

响应：

```ts
interface BatchSaveResult {
  savedCount: number
  ids: string[]
}
```

- `items` 1～200；
- 201 条 → 400/40003，`field=items`；
- 空数组 → 400/40001；
- 任一字段非法 → 400/40001，整批不保存；
- 成功返回实际数量和字符串 ID。

### 2.6 解析预览

`POST /api/addresses/parse` 保持 MS-02 契约：

```ts
interface ParsedItem {
  lineNo: number
  rawLine: string
  receiverName: string | null
  phone: string | null
  fullAddress: string | null
  status: 'VALID' | 'INVALID'
  errors: LineError[]
}

interface LineError {
  code: string
  field: string
  message: string
}
```

解析行错误在 `items[].errors[]`；请求字段错误在 `data.violations[]`，两者不得混用。

### 2.7 错误响应

```ts
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface Violation {
  field: string
  message: string
  lineNo?: number | null
}
```

- 40001 普通字段错误；
- 40003 数量/长度超限；
- 40401 不存在；
- 50002 数据库错误；
- `data.violations[]` 仅用于请求字段错误。

**结论**：接口字段名可直接映射 TypeScript；未发现新的命名/类型不一致。

---

## 三、数据校验

| 项 | 结果 |
| --- | --- |
| 生产 `AddressSaveDTO` 使用 `@CodePointLength(min=1,max=50)` | 通过 |
| 50 个扩展平面汉字 | 通过 |
| 51 个扩展平面汉字 | 400/40001 |
| 50 个 BMP 汉字 | 通过 |
| 51 个 BMP 汉字 | 400/40001 |
| `+86 138-...`、括号、空格电话 | 通过，保存前规范化 |
| 全角英文姓名 | 通过（`AddressSaveDTO` 允许） |
| 地址 255 字符 | 通过；256 拒绝 |
| 电话 21 位 | 拒绝 |

### P1 限制

1. `AddressService.batchSave` 自身只复核列表非空、≤200、元素非 null；字段级校验由 Controller 的 `@Valid` 完成。直接调用 Service 可绕过字段校验，正式外部 API 不可绕过。
2. `fullAddress` 的 `@Size(max=255)` 按 UTF-16 code unit，数据库 `VARCHAR(255)` 按 MySQL 字符；增补平面地址文本存在语义差异。这是既有冻结规则，本轮未改。
3. `batchDelete` 没有故障注入测试；它只有单条逻辑删除 SQL，已通过正常/部分不存在/全部不存在测试。
4. MySQL 集成测试依赖预创建 `express_address_book_test` 和 `DB_PASSWORD`；不设置环境变量时 16 个测试会跳过。
5. 无 Playwright/浏览器测试；Vue 页面尚未开发。

---

## 四、事务、数据库与安全

### 4.1 批量保存事务

通过真实 API + 真实 MySQL 验证：

- 测试库临时触发器仅对特定测试手机号抛异常；
- 两条 DTO 均合法；
- 第一条正常插入；
- 第二条触发数据库异常；
- Controller 返回 500/50002；
- 第一条同样回滚，`COUNT(*)=0`；
- 临时触发器测试后删除。

这不是“Service 层 300 字地址”测试的替代，而是独立的 API 层回滚测试。

### 4.2 批量删除

- 使用 MyBatis-Plus 逻辑删除；
- 重复 ID 去重；
- 部分不存在只删除存在部分；
- 全部不存在 404/40401；
- 测试库无 `TRUNCATE`、`DROP DATABASE` 或主库清空操作。

### 4.3 SQL 与安全

| 检查项 | 结果 |
| --- | --- |
| LIKE 特殊字符 | `%`、`_`、`!`、`\` 实际按字面匹配 |
| SQL 参数绑定 | 通过；未发现用户输入拼接 |
| `ESCAPE '!'` | Mapper XML 中保留，实测通过 |
| SQL 注入 | 未发现可注入路径 |
| 50002 响应 | 中文通用提示，不含 SQL/密码/堆栈 |
| 真实密码硬编码 | 未发现；测试配置为 `${DB_PASSWORD}` |
| 测试数据/主库 | 测试库 0 行；主库 0 行；测试库触发器已删除 |

---

## 五、测试真实性

### 实际执行

```text
mvn -B clean test
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
mvn exit code: 0

mvn -B clean package -DskipTests
BUILD SUCCESS
```

| 类别 | 数量 | 真实性 |
| --- | --- | --- |
| Parser 单元测试 | 86 | 真实执行 |
| Controller MockMvc | 12 | 真实 MockMvc，mock Parser/Service |
| 真实 Parser + HTTP | 8 | 真实 Parser，无 MySQL 写入 |
| 真实 MySQL 集成 | 16 | 连接独立 `express_address_book_test` |
| DTO 校验 | 10 | 真实 Validator/DTO |
| LIKE 单元 | 4 | 真实执行 |
| 码点校验 | 5 | 真实执行 |
| 基础测试 | 5 | 真实执行 |
| **合计** | **146** | **0/0/0** |

**注意**：`AddressApiMysqlTest` 使用 `@EnabledIfEnvironmentVariable(DB_PASSWORD)`；未设置 `DB_PASSWORD` 时 MySQL 测试会跳过，不能把此时的结果等同于 146 个全执行。

### 已实际验证

- P0-1～P0-5；
- 51 BMP/51 扩展平面/50 BMP/50 扩展平面；
- LIKE 四种特殊字符；
- 批量回滚；
- 50002；
- 逻辑删除；
- 分页类型和 ID 类型；
- page/size 字段映射；
- keyword 码点边界；
- 电话原始格式规范化；
- 全角英文姓名。

### 未执行

- Vue 页面/Playwright；
- 浏览器人工走查；
- 正式性能压测；
- `batchDelete` 故障注入；
- 真实主库 CRUD（只在独立测试库执行）；
- 自动创建测试数据库（当前需预先存在）。

---

## 六、前端实施建议

1. **可直接接入的 API**
   - `POST /api/addresses/parse`：仅预览，不落库；
   - `POST /api/addresses/batch`：确认后保存；
   - `GET /api/addresses`：列表/分页/搜索；
   - `POST /api/addresses`、`PUT /api/addresses/{id}`：新建/编辑；
   - `DELETE /api/addresses/{id}`、`POST /api/addresses/batch-delete`：删除。

2. **分页类型**

```ts
type PageResult<T> = {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}
```

不要把 `total/current/size/pages` 声明为 `string`。

3. **ID 类型**：统一 `string`；禁止 `number` 或 `parseInt` 后参与业务判断。

4. **姓名计数**

```ts
Array.from(value).length >= 1 && Array.from(value).length <= 50
```

不得只依赖 `maxlength`。

5. **电话输入**
   - 前端提示接受规范化手机/座机/脱敏；
   - 支持 `+86 138-...`、`（138...）`、`138 0013 8000`；
   - 提交前不必手写 normalize，但保存以服务端规范化值为准；
   - 不要把 `isEdited/original` 等服务端不接收字段发入保存 DTO。

6. **解析预览结构**
   - `items[].errors[]` 是行级错误；
   - `data.violations[]` 是请求字段错误；
   - 不要混用两个类型；
   - `rawLine` 只用于显示/定位，不提交保存。

7. **toPayload**

```ts
{
  receiverName: row.receiverName,
  phone: row.phone,
  fullAddress: row.fullAddress,
  lineNo: row.lineNo
}
```

只提交这四个字段；不提交 `status/errors/isEdited/original/rawLine/rawKey`。

8. **保存确认弹窗**
   - 有效行数；
   - 已修改行数；
   - 异常/不可保存行数；
   - 行号 + 错误原因；
   - 保存按钮防重复点击；
   - 确认后调用批量保存。

9. **翻页清空选择**
   - 翻页、修改 size、重新搜索时清空 `selectedIds`；
   - 批量删除只提交当前页已选 ID；
   - 后端仍会独立校验 ID 格式和存在性，前端选择状态不可信。

---

## 七、最终结论

# **有条件通过**

已实际验证的 P0：

- 分页四字段为 JSON Number；
- ID 为 JSON String；
- keyword 51 码点 → 40001；
- page=0 → field=page；
- size=101 → field=size。

进入 MS-04 前建议保持以下条件：

1. 前端按本报告类型定义实现，分页数字、ID 字符串；
2. 姓名使用 `Array.from` 计数；
3. 保存只提交业务字段；
4. MySQL 测试需预先建立独立测试库并设置 `DB_PASSWORD`，否则 16 个 MySQL 测试会跳过；
5. 不在前端把 50002 的通用提示替换成 SQL/堆栈信息；
6. Playwright 必须实际执行，不能以 MockMvc/MySQL 测试代替浏览器测试。

**本轮仅审查，未修改代码。**
