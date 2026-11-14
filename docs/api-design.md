# 接口设计（api-design.md）

| 项目 | 内容 |
| --- | --- |
| 文档版本 | v1.1-final（设计冻结） |
| 协议 | HTTP/1.1 + JSON（`Content-Type: application/json;charset=UTF-8`） |
| 服务地址（开发） | 后端 `http://127.0.0.1:8080`，前端 `http://127.0.0.1:5173`（Vite 代理 `/api`、`/demo`）；**仅本机回环，不对外监听** |
| 接口前缀 | 地址管理固定 `/api/addresses`；Servlet 演示固定 `/demo/**` |
| 关联文档 | `requirements.md`（FR/BR）、`database-design.md`（字段）、`parser-design.md`（行错误码）、`architecture.md`（分层） |
| 约束 | 路径、方法、字段名一经确定不得单方面修改；改接口必须同步改本文件 + 前端 `types` + 测试用例 |

---

## 1. 通用约定

### 1.1 统一响应体

所有 `/api/**` 接口（成功与失败）都返回同一外层结构：

```json
{
  "code": 0,
  "message": "OK",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | number | 业务码。`0` 成功；非 0 失败，见 §1.4 |
| `message` | string | 面向用户的中文提示；失败时必须是可读的一句话 |
| `data` | object \| array \| null | 业务数据；失败时为 `null`；参数校验失败时为 `{ "violations": [...] }`（注意：不是 `errors`） |

### 1.2 HTTP 状态码

采用"HTTP 状态码表达传输/资源语义，`code` 表达业务语义"的双轨方式：

| 场景 | HTTP | `code` |
| --- | --- | --- |
| 成功 | 200 | 0 |
| 参数校验失败 / 请求体不合法 | 400 | 40001 / 40002 / 40003 |
| 记录不存在（或已删除） | 404 | 40401 |
| 接口路径不存在 | 404 | 40402 |
| 未预期异常（含数据库异常） | 500 | 50001 / 50002 |
| 方法不支持 | 405 | 40501 |

> 本项目所有"成功"统一返回 HTTP 200（新建也返回 200，不返回 201），以降低前端处理分支；这是明确约定，不是遗漏。

### 1.3 数据类型约定

| 项 | 约定 | 原因 |
| --- | --- | --- |
| `id` | **字符串**（如 `"1"`），请求路径与数组同 | MySQL BIGINT 超出 JS 安全整数范围 |
| 时间 | `yyyy-MM-dd HH:mm:ss`（如 `"2026-11-14 10:30:00"`），时区 `Asia/Shanghai` | 与数据库 DATETIME 直观对应，避免 ISO 时区换算歧义 |
| 空列表 | `[]`，不返回 `null` | 前端可安全 `.map` |
| 分页 | `{ records, total, current, size, pages }` | 与 MyBatis-Plus `IPage` 字段同名，减少映射代码 |
| 字段命名 | 小驼峰；数据库下划线由 MyBatis-Plus 自动映射 | 见 `database-design.md` §8 |

### 1.4 业务码表

| code | 含义 | HTTP | 典型触发 | 前端处理 |
| --- | --- | --- | --- | --- |
| 0 | 成功 | 200 | — | 正常取 `data` |
| 40001 | 参数校验失败 | 400 | 姓名为空、电话格式错误、`id` 非数字 | 表单字段级报错 + toast |
| 40002 | 请求体缺失或 JSON 格式错误 | 400 | 空 body、非法 JSON | toast |
| 40003 | 请求数据超限 | 400 | 文本 > 20000 字符、有效行 > 200、批量保存 `items` > 200 条、批量删除 > 200 个；`data.violations[]` 至少含一条 `{field,message}` | toast 提示上限 + 定位字段 |
| 40401 | 数据不存在 | 404 | 编辑/删除不存在的 id | toast + 刷新列表 |
| 40402 | 接口不存在 | 404 | 访问未映射的 `/api/xxx` | 提示"接口不存在"，开发期排错 |
| 50001 | 服务器内部错误 | 500 | 未捕获异常 | 通用错误提示 |
| 50002 | 数据库操作失败 | 500 | 事务回滚、连接失败 | 通用错误提示 + 建议重试 |
| 40501 | 请求方法不支持 | 405 | `GET /api/addresses/batch-delete` | 开发期错误，仅日志 |

**40001 与 40003 的职责边界**：

- **40001 = 字段值不合法**：类型/格式/必填/长度（单字段）不合法，`violations[].field` 指向具体字段（如 `items[2].phone`）。
- **40003 = 请求集合/文本超限**：`rawText` 过长、有效行 >200、`items` >200、`ids` >200；`violations[].field` 指向集合或文本字段（如 `items`、`rawText`、`ids`）。
- **优先级**：当 `items` 同时超过 200 且含非法字段时，**40003 优先**（先判定数量上限）；数量合法后再按 40001 报字段错误。
- 两者都使用 `data.violations[]`；只有 `violations` 的字段路径与 `message` 不同。

**禁止**把 SQL 语句、表名、堆栈、连接串写进 `message`。

### 1.5 参数校验失败时的 `data.violations`

**字段校验错误统一使用 `data.violations[]`**，与解析结果的 `items[].errors[]` 严格区分（F-06）：`violations` 描述"请求字段不合法"，`items[].errors` 描述"某一行解析失败"。

```json
{
  "code": 40001,
  "message": "参数校验失败",
  "data": {
    "violations": [
      { "field": "phone", "message": "手机号格式不正确" },
      { "field": "receiverName", "message": "收件人姓名不能为空" }
    ]
  }
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `violations[].field` | string | 字段路径；单条请求为 `phone`，批量保存为 `items[2].phone` |
| `violations[].message` | string | 面向用户的中文提示，**不回显原始值**（避免把手机号/地址带进错误信息） |
| `violations[].lineNo` | number \| null | 可选，仅批量保存且前端传入 `lineNo` 时回填 |

```json
{ "field": "items[2].phone", "lineNo": 3, "message": "第 3 行手机号格式不正确" }
```

> `items[].errors[]` 的字段是 `code/field/message`（行级错误码 E1xx），与 `violations[]` 不同构，前端不得共用同一个类型。
>
> **40003 的 `data` 结构**：与 40001 相同，使用 `data.violations[]`；例如 `items` 超过 200 条时：
> `{ "field": "items", "message": "单次最多保存 200 条" }`。

### 1.6 分页参数

| 参数 | 类型 | 默认 | 约束 |
| --- | --- | --- | --- |
| `page` | int | 1 | ≥1；超过总页数时返回空 `records` 与正确 `total` |
| `size` | int | 10 | 1～100；>100 按 40001 处理（不静默截断） |
| `keyword` | string | 空 | trim 后按 **Unicode 码点** ≤50；空串等价于不筛选；`%`、`_`、`!` 按字面匹配（见 §1.8） |

### 1.7 幂等与事务

| 接口 | 事务 | 幂等性 |
| --- | --- | --- |
| `POST /parse` | 无（只读计算） | 天然幂等 |
| `POST /batch` | 单事务，全成功或全回滚 | 不幂等（重复提交会产生重复数据） |
| `POST /` | 单条插入 | 不幂等 |
| `PUT /{id}` | 单条更新 | 幂等（同内容重复提交结果一致） |
| `DELETE /{id}` | 单条逻辑删 | 第二次返回 40401（已删除视作不存在） |
| `POST /batch-delete` | 单事务；已不存在的 id 跳过，返回实际删除数 | 宽松幂等（重复提交 `deletedCount` 递减） |

> 前端在批量保存成功后必须清空预览并禁用"保存"按钮，避免双击重复提交；这是前端职责，不是后端去重。保存请求成功后前端必须取消 loading，并保证同一时刻只有一个保存请求在途（重复点击直接忽略）。

### 1.8 关键词通配符转义（F-01）

参数化 SQL 只能防注入，不能阻止 `%`/`_` 的 LIKE 通配语义。因此 **`keyword` 在进入查询前必须由 `LikeEscaper` 转义一次**：

| 输入字符 | 转义结果 | 说明 |
| --- | --- | --- |
| `!` | `!!` | 转义符自身必须双写，且**第一步**处理，避免后续把新生成的 `!` 再转义一次 |
| `%` | `!%` | 字面百分号 |
| `_` | `!_` | 字面下划线 |
| `\` | `\`（不变） | 显式 `ESCAPE '!'` 后，反斜杠不再承担转义含义；参数绑定传递原字符，按字面匹配 |

- 顺序固定为：`!` → `%` → `_`，**每个字符只处理一次**，禁止对已转义串再次调用（防二次转义）。
- 生成的模式串为 `"%" + escaped + "%"`，通过 MyBatis-Plus `apply("receiver_name LIKE {0} ESCAPE '!'", pattern)` 或 XML `LIKE #{pattern} ESCAPE '!'` 绑定；**禁止字符串拼接**。
- 反例：`keyword=%` 若不转义会匹配全表；转义后只匹配字段中真的包含 `%` 的记录。
- 单元测试必须覆盖 `%`、`_`、`!`、`\`、中文与组合串（见 `test-plan.md` TC-LIKE）。

---

## 2. 接口清单

| # | 方法 | 路径 | 用途 | 需求 |
| --- | --- | --- | --- | --- |
| 1 | POST | `/api/addresses/parse` | 解析多行文本，**不落库** | FR-02、FR-03 |
| 2 | POST | `/api/addresses/batch` | 批量保存（确认后的数据） | FR-05 |
| 3 | GET | `/api/addresses` | 分页 + 关键词查询 | FR-08、FR-09 |
| 4 | GET | `/api/addresses/{id}` | 查询单条 | FR-11（编辑回填） |
| 5 | POST | `/api/addresses` | 新建单条 | FR-07、FR-10 |
| 6 | PUT | `/api/addresses/{id}` | 编辑单条 | FR-11 |
| 7 | DELETE | `/api/addresses/{id}` | 删除单条（逻辑删） | FR-12 |
| 8 | POST | `/api/addresses/batch-delete` | 批量删除（逻辑删） | FR-13 |

### 2.1 健康检查（MS-01）

| # | 方法 | 路径 | 用途 | 是否检查数据库 |
| --- | --- | --- | --- | --- |
| 1 | GET | `/api/health` | Web 服务存活 | 否 |
| 2 | GET | `/api/health/db` | 应用账号 `SELECT 1` 连接检查 | 是 |

两者均返回统一 `{code,message,data}`；`/api/health` 不得冒充数据库健康检查。

---

## 3. 接口详情

### 3.1 POST `/api/addresses/parse`

**用途**：把多行文本解析为结构化联系人列表。**只解析，不写库**（BR-05）。

请求：

```json
{ "rawText": "菜小鸟，136xxxx0000，浙江省杭州市西湖区湖滨小区12栋301室\n张小明\t13900139000\t浙江省杭州市滨江区某某小区5栋202室\n只有姓名没有电话" }
```

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `rawText` | string | 是 | 非空（trim 后）；≤20000 字符；有效行 ≤200 |

成功响应（`code=0`）：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "totalLines": 3,
    "effectiveLines": 3,
    "skippedLines": 0,
    "validCount": 2,
    "invalidCount": 1,
    "items": [
      {
        "lineNo": 1,
        "rawLine": "菜小鸟，136xxxx0000，浙江省杭州市西湖区湖滨小区12栋301室",
        "status": "VALID",
        "receiverName": "菜小鸟",
        "phone": "136xxxx0000",
        "fullAddress": "浙江省杭州市西湖区湖滨小区12栋301室",
        "errors": []
      },
      {
        "lineNo": 2,
        "rawLine": "张小明\t13900139000\t浙江省杭州市滨江区某某小区5栋202室",
        "status": "VALID",
        "receiverName": "张小明",
        "phone": "13900139000",
        "fullAddress": "浙江省杭州市滨江区某某小区5栋202室",
        "errors": []
      },
      {
        "lineNo": 3,
        "rawLine": "只有姓名没有电话",
        "status": "INVALID",
        "receiverName": "只有姓名没有电话",
        "phone": null,
        "fullAddress": null,
        "errors": [
          { "code": "E103", "field": "phone", "message": "未识别到联系电话" },
          { "code": "E105", "field": "fullAddress", "message": "未识别到详细地址" }
        ]
      }
    ]
  }
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalLines` | number | 按 `\r\n`/`\n`/`\r` 切分后的物理行数（含空行） |
| `effectiveLines` | number | 去掉空行后的行数 |
| `skippedLines` | number | 被跳过的空行数 |
| `validCount` / `invalidCount` | number | `status` 计数，满足 `validCount + invalidCount = effectiveLines` |
| `items[].lineNo` | number | **原始行号，从 1 开始**（BR-07） |
| `items[].rawLine` | string | 原始行文本（未 trim 的原样，供人工比对） |
| `items[].status` | enum | `VALID` / `INVALID` |
| `items[].receiverName` | string \| null | 失败时可能为部分结果或 null |
| `items[].phone` | string \| null | 解析阶段的展示级规范化结果（去空格与短横、去 `+86`），见 `parser-design.md` PR-03 |
| `items[].fullAddress` | string \| null | 原样保留内部标点 |
| `items[].errors[]` | array | 行级错误，`code` 见 `parser-design.md` E1xx；`VALID` 时为空数组 |

失败：

| 情况 | HTTP | code | data |
| --- | --- | --- | --- |
| `rawText` 缺失/空串 | 400 | 40001 | `violations[].field=rawText` |
| `rawText` 超过 20000 字符或有效行 > 200 | 400 | 40003 | `violations[].field=rawText` |

> MS-02.1 实现 `AddressController` + `ParseInputGuard`：长度/行数在进入 Parser **之前**校验；`ParseResult` 只由 parser 产生，接口不写数据库。

> 注意：**整行无法解析不是接口失败**，仍然返回 HTTP 200 + `items[].status=INVALID`。这样前端才能展示部分成功结果（BR-06）。

**契约测试要点**：识别后调用 `GET /api/addresses`，`total` 必须不变。

---

### 3.2 POST `/api/addresses/batch`

**用途**：保存用户在预览表中**确认（可能已修改）**的记录。

请求：

```json
{
  "items": [
    { "lineNo": 1, "receiverName": "菜小鸟", "phone": "13600136000", "fullAddress": "浙江省杭州市西湖区湖滨小区12栋301室" },
    { "lineNo": 2, "receiverName": "张小明", "phone": "13900139000", "fullAddress": "浙江省杭州市滨江区某某小区5栋202室" }
  ]
}
```

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `items` | array | 是 | `@NotEmpty`；1～200 条（`@Size(max=200)`）；元素**不得为 null**（`List<@NotNull @Valid AddressSaveDTO>`） |
| `items[].lineNo` | number | 否 | 仅用于错误提示回填 |
| `items[].receiverName` | string | 是 | `@NotBlank` + `@CodePointLength(min=1,max=50)`，按 Unicode 码点计数；前端用 `Array.from`，不依赖 `maxlength` |
| `items[].phone` | string | 是 | `@NotBlank`、`@Size(max=20)`、`@Pattern`；允许规范化手机/座机/脱敏，以及常见 `+86`、空格、短横、成对包裹括号输入；服务端保存前按 PR-03 规范化 |
| `items[].fullAddress` | string | 是 | `@NotBlank`；trim 后非空，≤255 |

**DTO 设计契约（F-09）**：

```java
public record AddressBatchSaveDTO(
        @NotEmpty(message = "没有可保存的记录")
        @Size(max = 200, message = "单次最多保存 200 条")
        List<@NotNull(message = "记录不能为空") @Valid AddressSaveDTO> items) {
}
```

- Controller 使用 `@Valid @RequestBody AddressBatchSaveDTO body`；
- Service 入口再次断言 `items` 非空、≤200、元素非 null（防止绕过 Controller 的内部调用）；
- 普通字段错误 → 400/40001；`items` 超过 200 → 400/40003（`violations[].field=items`）；两者都由 `GlobalExceptionHandler` 统一包装 `data.violations[]`；
- 当 `items` 同时超 200 且含非法字段时，按 §1.4 的优先级返回 40003；
- 整个方法在 `@Transactional(rollbackFor = Exception.class)` 内，任一写入异常整批回滚。

**`isEdited` 约定**：`isEdited` 是前端 `ParsedRow` 的派生状态（与 `original` 比较），**不属于** `AddressSaveDTO`，请求体只允许 `receiverName/phone/fullAddress/lineNo`；即使请求携带 `isEdited`，后端也不得将其视为可信业务参数、不得入库。

成功响应：

```json
{ "code": 0, "message": "OK", "data": { "savedCount": 2, "ids": ["1", "2"] } }
```

失败：

| 情况 | HTTP | code | 说明 |
| --- | --- | --- | --- |
| 任一条字段非法（items 数量 1～200） | 400 | 40001 | **整批不保存**，`data.violations` 指出第几条哪个字段 |
| `items` 为空数组或缺失 | 400 | 40001 | 提示"没有可保存的记录" |
| `items` 含 `null` 元素 | 400 | 40001 | 提示"记录不能为空" |
| `items` 超过 200 条 | 400 | 40003 | `violations[].field=items`；**数量超限优先于字段错误** |
| 数据库异常 | 500 | 50002 | 事务回滚，`savedCount` 不存在 |

**服务端必须重新校验**：不信任 `parse` 的结果，也不信任前端修改后的值。即使用户把合法电话改成 `123`，也必须被拒绝；**不得静默丢弃请求中的非法记录**（BR-18）。

---

### 3.3 GET `/api/addresses`

**用途**：分页浏览 + 关键词搜索。

请求示例：

```text
GET /api/addresses?page=1&size=10&keyword=西湖
```

| 参数 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `page` | int | 否 | 默认 1，≥1 |
| `size` | int | 否 | 默认 10，1～100 |
| `keyword` | string | 否 | trim 后按 Unicode 码点 ≤50；同时模糊匹配姓名/电话/地址；`%`、`_`、`!` 按字面匹配（§1.8） |

成功响应：

```json
{
  "code": 0,
  "message": "OK",
  "data": {
    "records": [
      {
        "id": "2",
        "receiverName": "菜中鸟",
        "phone": "136xxxx0001",
        "fullAddress": "湖北省武汉市江岸区沿江小区8栋602室",
        "createTime": "2026-11-14 10:30:00",
        "updateTime": "2026-11-14 10:30:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1
  }
}
```

- 排序：`create_time DESC, id DESC`（BR-14）。
- 关键词为空时不做筛选；**关键词先经 `LikeEscaper` 转义，再以 `LIKE '%…%' ESCAPE '!'` 绑定**（见 §1.8）。`LIKE '%…%'` 不会使用索引，万级数据可接受（见 `database-design.md` §4）。
- 响应中**不返回** `isDeleted`。
- `total/current/size/pages` 必须是 JSON Number；`records[].id` 必须是 JSON String（Long 精度保护）；时间格式为 `yyyy-MM-dd HH:mm:ss`。
- `keyword` 由 Service 校验：`strip()` 后按 Unicode 码点计数，>50 返回 400/40001，`field=keyword`。
- `page/size` 方法参数校验错误的 `field` 分别是 `page`、`size`。

---

### 3.4 GET `/api/addresses/{id}`

**用途**：编辑前回填（也可用于详情展示）。

```text
GET /api/addresses/2
```

成功：`data` 为单条 `AddressVO`（字段同 §3.3 的 `records[]`）。

失败：`id` 非正整数 → 400/40001；不存在或已删除 → 404/40401。

---

### 3.5 POST `/api/addresses`

**用途**：新建单条地址（列表页"新建地址"按钮）。

请求：

```json
{ "receiverName": "菜大鸟", "phone": "136xxxx0002", "fullAddress": "湖北省孝感市孝南区东苑小区15栋201室" }
```

成功：`{ "code": 0, "message": "OK", "data": { "id": "3" } }`

失败：字段校验 → 400/40001。

> 请求体**不能**包含 `id`、`createTime`、`updateTime`、`isDeleted`；即使包含也被忽略（DTO 不接收这些字段）。

---

### 3.6 PUT `/api/addresses/{id}`

**用途**：编辑单条地址。

```text
PUT /api/addresses/3
{ "receiverName": "菜大鸟", "phone": "13600136002", "fullAddress": "湖北省孝感市孝南区东苑小区15栋201室" }
```

成功：`{ "code": 0, "message": "OK", "data": { "id": "3" } }`

失败：400/40001（字段非法）；404/40401（记录不存在或已删除）。

> 更新采用"全字段覆盖"，不是 PATCH。前端表单必须提交三个字段的完整值。

---

### 3.7 DELETE `/api/addresses/{id}`

**用途**：删除单条（逻辑删除）。

```text
DELETE /api/addresses/3
```

成功：`{ "code": 0, "message": "OK", "data": null }`

失败：404/40401（不存在或已删除）；400/40001（`id` 非正整数）。

> 二次确认由前端负责（`ElMessageBox.confirm`），后端不再重复询问。

---

### 3.8 POST `/api/addresses/batch-delete`

**用途**：批量删除（逻辑删除）。

请求：

```json
{ "ids": ["1", "3"] }
```

| 字段 | 类型 | 必填 | 约束 |
| --- | --- | --- | --- |
| `ids` | string[] | 是 | 1～200 个；每个必须是正整数字符串；服务端去重 |

成功：`{ "code": 0, "message": "OK", "data": { "deletedCount": 2 } }`

失败：

| 情况 | HTTP | code | data |
| --- | --- | --- | --- |
| `ids` 为空/缺失 | 400 | 40001 | `violations[].field=ids` |
| 含非数字、0、负数 | 400 | 40001 | `violations[].field=ids` |
| 超过 200 个 | 400 | 40003 | `violations[].field=ids` |
| 全部 id 都已不存在 | 404 | 40401 | `null` |

> 部分 id 不存在时**不报错**，删除存在的部分并返回实际 `deletedCount`；这是为了容忍列表数据过期（别人已删/已刷新）。此行为与"批量保存全成功或全失败"不同，属有意设计，已在前端提示中体现。
>
> **前端选择规则（F-04）**：批量删除只允许选择**当前页**记录，翻页时清空选择；确认框显示待删除数量。后端不信任前端选择范围，必须重新校验每个 id 的格式（正整数）与存在性，并对已删除/不存在的 id 按上述宽松策略处理。

---

## 4. 前端调用层设计（src/api/）

### 4.1 文件划分

| 文件 | 职责 |
| --- | --- |
| `src/api/request.ts` | Axios 实例：`baseURL`、超时、请求/响应拦截、`code!==0` 统一抛错、网络异常提示 |
| `src/api/address.ts` | 8 个接口函数，只做参数拼装与类型标注，不含 UI 逻辑 |
| `src/types/address.ts` | `AddressItem`、`AddressPayload`、`ParseResult`、`ParsedItem`、`ParsedRow`、`PageResult<T>`、`ApiResponse<T>`、`Violation`、`ApiError` |
| `src/utils/error.ts` | 把后端 `message` / `violations` 转成表单错误与 toast 文案 |

### 4.2 函数签名（设计契约）

```ts
// src/api/address.ts
export function parseAddressText(rawText: string): Promise<ParseResult>;
export function toPayload(row: ParsedRow): AddressSaveItem;   // 只投影业务字段，剥离 isEdited/original/rawLine 等
export function batchSaveAddresses(items: AddressSaveItem[]): Promise<{ savedCount: number; ids: string[] }>;
export function getAddressPage(params: AddressQuery): Promise<PageResult<AddressItem>>;
export function getAddressById(id: string): Promise<AddressItem>;
export function createAddress(payload: AddressPayload): Promise<{ id: string }>;
export function updateAddress(id: string, payload: AddressPayload): Promise<{ id: string }>;
export function deleteAddress(id: string): Promise<void>;
export function batchDeleteAddresses(ids: string[]): Promise<{ deletedCount: number }>;
```

### 4.3 类型定义（设计契约）

```ts
// src/types/address.ts
export interface ApiResponse<T> { code: number; message: string; data: T; }
export interface PageResult<T> { records: T[]; total: number; current: number; size: number; pages: number; }

export interface AddressItem {
  id: string;
  receiverName: string;
  phone: string;
  fullAddress: string;
  createTime: string;
  updateTime: string;
}

export interface AddressPayload {
  receiverName: string;
  phone: string;
  fullAddress: string;
}

export interface AddressSaveItem extends AddressPayload { lineNo?: number; } // 不含 isEdited
export interface AddressQuery { page: number; size: number; keyword?: string; }

export type ParseStatus = 'VALID' | 'INVALID';
export interface ParseItemError { code: string; field: string; message: string; }
export interface ParsedItem {
  lineNo: number;
  rawLine: string;
  status: ParseStatus;
  receiverName: string | null;
  phone: string | null;
  fullAddress: string | null;
  errors: ParseItemError[];
}
/** 前端派生行：original 为解析时快照；isEdited = 当前值与 original 任一不同 */
export interface ParsedRow extends ParsedItem {
  rowKey: string;
  original: AddressPayload;
  isEdited: boolean;
}
export interface ParseResult {
  totalLines: number;
  effectiveLines: number;
  skippedLines: number;
  validCount: number;
  invalidCount: number;
  items: ParsedItem[];
}

/** 请求字段校验错误（data.violations[]） */
export interface Violation {
  field: string;
  message: string;
  lineNo?: number;
}
export interface ApiError extends Error {
  code: number;
  violations: Violation[];
}
```

### 4.4 拦截器行为约定

1. 响应 `code === 0` → 直接 resolve `data`（页面拿到的不是整包，减少样板代码）。
2. `code !== 0` → reject 一个 `ApiError { code, message, violations }`；由页面决定 toast 还是表单报错（`violations` 为数组，字段校验失败时非空）。
3. HTTP 层失败（超时/断网/500 非 JSON）→ 统一文案"网络异常，请稍后重试"或"服务器开小差了"，**不显示原始堆栈**。
4. 打印日志时不得输出完整请求体（含手机号/地址）。

---

## 5. 调试示例（curl）

```bash
# 1. 解析（不落库）
curl -X POST http://127.0.0.1:8080/api/addresses/parse \
  -H "Content-Type: application/json" \
  -d '{"rawText":"菜小鸟，136xxxx0000，浙江省杭州市西湖区湖滨小区12栋301室"}'

# 2. 分页查询
curl "http://127.0.0.1:8080/api/addresses?page=1&size=10&keyword=西湖"

# 3. 新建
curl -X POST http://127.0.0.1:8080/api/addresses \
  -H "Content-Type: application/json" \
  -d '{"receiverName":"菜大鸟","phone":"13600136002","fullAddress":"湖北省孝感市孝南区东苑小区15栋201室"}'

# 4. 批量删除
curl -X POST http://127.0.0.1:8080/api/addresses/batch-delete \
  -H "Content-Type: application/json" \
  -d '{"ids":["1","3"]}'
```

> Windows PowerShell 下 `curl` 为 `Invoke-WebRequest` 别名，建议用 Postman 或 `curl.exe`。

---

## 6. Servlet 演示接口（非 REST，仅供 2.20）

详见 `architecture.md` §7。此处只列路径，避免与 `/api/addresses` 混淆：

| 路径 | 匹配类型 | 归属 |
| --- | --- | --- |
| `/demo/servlet/exact` | 容器精确映射 | Servlet |
| `/demo/servlet/*` | 容器路径前缀映射（如 `/demo/servlet/prefix/a/b`） | Servlet |
| `*.do`（如 `/demo/legacy/report.do`） | 容器扩展名映射 | Servlet |
| `/demo/spring/**` | Spring MVC 路由（DispatcherServlet 内部匹配） | Controller |

前端 `/servlet-demo` 页面通过 `fetch` 调用这些地址，把**真实响应**渲染成"命中处理器"表格。

---

## 7. 全局异常映射

| 异常 | HTTP | code | message 示例 |
| --- | --- | --- | --- |
| `MethodArgumentNotValidException` | 400 | 40001 | 参数校验失败（附 `violations`） |
| `MethodArgumentNotValidException`（`items` 的 `@Size` 违规） | 400 | 40003 | 单次最多保存 200 条（`violations[].field=items`） |
| `ConstraintViolationException` | 400 | 40001 | 参数校验失败 |
| `HttpMessageNotReadableException` | 400 | 40002 | 请求数据格式不正确 |
| `MethodArgumentTypeMismatchException`（`id=abc`） | 400 | 40001 | 参数类型不正确：id |
| `HttpRequestMethodNotSupportedException` | 405 | 40501 | 请求方法不支持 |
| `NoResourceFoundException` / `NoHandlerFoundException` | 404 | 40402 | 接口不存在 |
| `BusinessException(40003)` | 400 | 40003 | 文本过长，最多 200 行 / 20000 字符 |
| `BusinessException(40401)` | 404 | 40401 | 记录不存在或已被删除 |
| `DuplicateKeyException`（本项目无唯一键，兜底） | 500 | 50002 | 数据保存失败，请稍后重试 |
| `DataAccessException` | 500 | 50002 | 数据库操作失败，请稍后重试 |
| 其它 `Exception` | 500 | 50001 | 服务器内部错误 |

异常处理器只记录 `logger.error("...", e)` 到服务端日志，返回体不含 `e.getMessage()` 原文（可能含 SQL）。`GlobalExceptionHandler` 在转换 `MethodArgumentNotValidException` 时，若违规字段为 `items` 且约束为 `Size`，必须映射为 40003，而不是 40001。

---

## 8. 接口自测清单（执行后回填）

| # | 用例 | 期望 | 实际 | 结论 |
| --- | --- | --- | --- | --- |
| 1 | parse 正常 3 行 | 200，`validCount=3` | 待执行 | 待执行 |
| 2 | parse 缺电话 | 200，1 条 `INVALID`，`E103` | 待执行 | 待执行 |
| 3 | parse 空文本 | 400，40001 | 待执行 | 待执行 |
| 4 | parse 后列表总数不变 | 总数不变 | 待执行 | 待执行 |
| 5 | batch 保存 3 条 | 200，`savedCount=3` | 待执行 | 待执行 |
| 6 | batch 第 3 条电话非法 | 400，整批未入库 | 待执行 | 待执行 |
| 7 | 列表分页 size=10 | `records.length<=10`，`total` 正确 | 待执行 | 待执行 |
| 8 | 关键词检索 | 只返回匹配项 | 待执行 | 待执行 |
| 9 | 编辑不存在 id | 404 | 待执行 | 待执行 |
| 10 | 删除后列表不再出现 | 40401/已消失 | 待执行 | 待执行 |
| 11 | 批量删除空数组 | 400 | 待执行 | 待执行 |
| 12 | `keyword=%` | 只返回字段含字面 `%` 的记录，不返回全表 | 待执行 | 待执行 |
| 13 | `keyword=_` | 只返回字段含字面 `_` 的记录 | 待执行 | 待执行 |
| 14 | 批量保存含 null 元素 | 400/40001，`violations` 指向该元素 | 待执行 | 待执行 |
| 15 | 批量保存 201 条 | 400/40003，`violations[].field=items` | 待执行 | 待执行 |
| 16 | 请求体含 `isEdited` | 被忽略，库中无该字段 | 待执行 | 待执行 |
| 17 | 批量保存 200 条 | 200，`savedCount=200` | 待执行 | 待执行 |
| 18 | 201 条且其中含非法字段 | 400/40003（数量优先），不是 40001 | 待执行 | 待执行 |
| 19 | `POST /batch` 请求体 | 只含 `receiverName/phone/fullAddress/lineNo`，无 `isEdited/original/rawLine` | 待执行 | 待执行 |

> 该表用于第二阶段真实联调记录，**当前全部为"待执行"**，不得提前填写结果。

---

## 9. 变更记录

| 版本 | 日期 | 变更 | 说明 |
| --- | --- | --- | --- |
| v1.0 | 2026-11-14 | 8 个接口契约定稿 | 与 `requirements.md` FR-02～FR-13 对齐；尚未实现 |
| v1.1 | 2026-11-14 | P0 修订：LIKE 转义（§1.8）、`violations` 替换 `errors`、批量保存嵌套校验与 `isEdited`、批量删除当前页规则、本机访问地址 | 仍未实现 |
| v1.1-final | 2026-11-14 | 第二轮审查遗留修复：40001/40003 职责与 201 条映射、`toPayload` 与 `original`/`isEdited` 契约、请求体只含业务字段、500/200 边界自测 | 仍未实现 |
| v1.1-final-ms02.1 | 2026-11-14 | 解析 API 实现：`POST /api/addresses/parse`、`ParseInputGuard`、`data.violations[]`、rawText 20000/200 行 40003；姓名 `@CodePointLength` | 已实现；解析 API 12 个集成测试通过 |
| v1.1-final-ms03 | 2026-11-14 | 地址簿 CRUD、分页搜索、批量保存/删除、独立 MySQL 集成 | 已实现；137 个测试通过；Vue 未开发 |
