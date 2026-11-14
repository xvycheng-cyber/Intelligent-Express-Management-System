# MS-03 后端业务模块报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-03 快递地址簿后端业务模块 |
| 范围 | Entity/Mapper、分页搜索、CRUD、批量保存/删除、MySQL 集成测试 |
| 测试 | `mvn -B clean test`：**146 tests, 0 failures, 0 errors, 0 skipped**（MS-03.1 后） |
| 打包 | `mvn -B clean package -DskipTests`：BUILD SUCCESS |
| 前端 | 本阶段未修改前端；沿用此前 `npm run build` 通过 |

---

## 1. 实际代码结构

```text
backend/src/main/java/com/express/addressbook/
├── common/
│   ├── LikeEscaper.java
│   ├── BusinessException.java / GlobalExceptionHandler.java / R.java / ResultCode.java / Violation.java
├── config/ (MybatisPlusConfig, JacksonConfig)
├── controller/AddressController.java
├── dto/
│   ├── AddressSaveDTO.java
│   ├── AddressBatchSaveDTO.java
│   ├── BatchDeleteDTO.java
│   └── AddressParseRequest.java
├── entity/ExpressAddress.java
├── mapper/ExpressAddressMapper.java
├── service/AddressService.java
├── vo/
│   ├── AddressVO.java
│   ├── PageResult.java
│   ├── BatchSaveResult.java
│   └── BatchDeleteResult.java
├── parser/...（MS-02 保持不变，仅将 PhoneExtractor.normalizePhone 设为 public 供保存复用）
└── validation/CodePointLength.java / CodePointLengthValidator.java
```

Mapper XML：`backend/src/main/resources/mapper/ExpressAddressMapper.xml`。

---

## 2. 新增和修改的文件

### 新增

| 文件 | 说明 |
| --- | --- |
| `entity/ExpressAddress.java` | 表映射、自增主键、`@TableLogic` |
| `mapper/ExpressAddressMapper.java` | BaseMapper + 关键词分页方法 |
| `resources/mapper/ExpressAddressMapper.xml` | `LIKE ... ESCAPE '!'`、分页、排序 |
| `common/LikeEscaper.java` | `!`/`%`/`_` 转义 |
| `dto/AddressSaveDTO.java` | 姓名码点、电话、地址校验 |
| `dto/AddressBatchSaveDTO.java` | 嵌套校验 + 1～200 |
| `dto/BatchDeleteDTO.java` | ids 1～200、正整数字符串 |
| `vo/AddressVO.java`、`PageResult.java`、`BatchSaveResult.java`、`BatchDeleteResult.java` | 出参 |
| `service/AddressService.java` | 业务规则、事务 |
| `resources/.../application-test.yml` | 独立测试库配置（密码来自环境变量） |

### 修改

| 文件 | 说明 |
| --- | --- |
| `controller/AddressController.java` | 保留 parse，新增列表/详情/新增/编辑/删除/批量保存/批量删除 |
| `common/GlobalExceptionHandler.java` | `items`/`ids` 的 `@Size` → 40003；`data.violations[]` |
| `parser/PhoneExtractor.java` | `normalizePhone` 设为 public，供保存复用（解析规则未改） |
| `schema.sql` | 姓名注释改为 Unicode 码点（结构未改） |
| `application.yml` | `mapper-locations` |

---

## 3. API 完成情况

| 方法 | 路径 | 状态 |
| --- | --- | --- |
| POST | `/api/addresses/parse` | MS-02 已有，未重写 |
| GET | `/api/addresses` | 已完成 |
| GET | `/api/addresses/{id}` | 已完成 |
| POST | `/api/addresses` | 已完成 |
| PUT | `/api/addresses/{id}` | 已完成 |
| DELETE | `/api/addresses/{id}` | 已完成 |
| POST | `/api/addresses/batch` | 已完成 |
| POST | `/api/addresses/batch-delete` | 已完成 |

统一响应 `R{code,message,data}`；错误码 0/40001/40002/40003/40401/40402/40501/50001/50002 未改语义。

---

## 4. DTO 校验规则

### AddressSaveDTO

| 字段 | 约束 |
| --- | --- |
| receiverName | `@NotBlank` + `@CodePointLength(1..50)` + 中文/英文姓名 `@Pattern` |
| phone | `@NotBlank`、`@Size(max=20)`、`@Pattern`（规范化、`+86`、空格/短横、成对包裹括号；保存前规范化） |
| fullAddress | `@NotBlank`、`@Size(max=255)` |
| lineNo | 可选，仅错误回填，不入库 |

### AddressBatchSaveDTO

- `@NotEmpty`；`@Size(max=200)`；
- `List<@NotNull @Valid AddressSaveDTO>`；
- 空列表 40001；201 条 40003（`items`）；
- 数量优先于字段错误。

### BatchDeleteDTO

- `ids` 1～200 个；
- 每个为 `^[1-9]\d*$`；
- 201 个 40003（`ids`）；空 40001；
- 服务端去重，重复 ID 只删除一次。

---

## 5. CodePointLength 生产接入

- `AddressSaveDTO.receiverName` 已实际使用 `@CodePointLength(min=1,max=50)`；
- 不是只依赖 `@Size`；
- `AddressSaveDTOTest` 10 个测试（含 MS-03.1 原始电话/全角英文）：
  - 1 个字；
  - 50/51 BMP；
  - 50/51 扩展平面；
  - 地址 255/256；
  - 电话 21。

---

## 6. MyBatis-Plus 与 XML

- Entity：`@TableId(type=AUTO)`、`@TableLogic(value="0",delval="1")`；
- Mapper 继承 `BaseMapper<ExpressAddress>`；
- 自定义分页查询 XML：
  - `WHERE is_deleted = 0`；
  - `receiver_name/phone/full_address` 三字段 LIKE；
  - `LIKE CONCAT('%', #{keywordPattern}, '%') ESCAPE '!'`；
  - `ORDER BY create_time DESC, id DESC`；
- 使用 `PaginationInnerInterceptor`。

---

## 7. LIKE 特殊字符

`LikeEscaper.escape`：

```text
! -> !!
% -> !%
_ -> !_
```

顺序固定，参数绑定；反斜杠保持字面量。

MySQL 实测：

- `%` 只命中包含 `%` 的记录；
- `_`、`!`、`\` 均按字面匹配；
- 普通中文/英文/电话/地址关键词均可命中。

---

## 8. 事务实现

- `AddressService.create/update/delete/batchDelete/batchSave` 均为 `@Transactional(rollbackFor = Exception.class)`；
- Controller 调用 Spring Bean，不存在同类 `this.xxx()` 绕过代理；
- `update` 使用 `LambdaUpdateWrapper` 只更新三个业务字段，避免覆盖 `create_time/is_deleted` 等；
- 批量保存整批插入；
- 事务回滚测试：第一条合法 + 第二条 300 字地址触发数据库失败，最终 `COUNT(*)=0`，实际通过。

---

## 9. MySQL 真实测试结果

测试库：`express_address_book_test`（独立创建，未操作主库 `express_address_book`）。

测试账号密码来自 `DB_PASSWORD` 环境变量，测试代码/配置无硬编码口令。

`AddressApiMysqlTest` 16 个测试全部通过（MS-03.1 后；含契约/事务触发）：

- 新增/查询/编辑/删除/更新时间；
- 分页排序/总数；
- 姓名/电话/地址关键词；
- `%`/`_`/`!`/`\` 字面匹配；
- 批量保存 2 条、200 条；
- 批量保存 201 条 → 40003；空列表 → 40001；
- 批量删除重复/部分不存在/全部不存在；
- 事务回滚；
- 逻辑删除过滤；
- 40401/40001/40003。

测试结束后测试库计数为 0；主库计数仍为 0，未被测试写入。

---

## 10. Maven 测试结果

```text
mvn -B clean test
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
mvn exit code: 0
```

| 测试类 | 数量 |
| --- | --- |
| `AddressParserServiceTest` | 86 |
| `AddressControllerTest` | 12 |
| `AddressParseIntegrationTest` | 8 |
| `AddressApiMysqlTest` | 16 |
| `AddressSaveDTOTest` | 10 |
| `LikeEscaperTest` | 4 |
| `CodePointLengthTest` | 5 |
| 基础测试 | 5 |
| **合计** | **146** |

```text
mvn -B clean package -DskipTests
BUILD SUCCESS
```

---

## 11. 未执行的测试

1. Playwright/浏览器人工走查。
2. Vue 地址录入/列表页面（MS-04）。
3. 正式压力测试；当前无性能门禁。
4. `root` 主库的 CRUD 集成（测试仅在独立测试库进行）。
5. 批量删除事务失败注入（删除为单条 SQL，未单独注入失败）。

---

## 12. 已知问题

1. `AddressSaveDTO.phone` 接受常见原始电话（`+86 138-...`、空格、括号）并在保存前规范化；不支持任意未知格式。
2. `AddressSaveDTO.receiverName` 允许全角英文姓名；不再拒绝解析器输出的 `Ｊｏｈｎ`。
3. 分页 `page/size` 的 `ConstraintViolation` 已映射为 `page/size`；`keyword` 由 Service 按码点校验。
4. 无唯一约束；相同姓名/电话允许多条。
5. 批量删除部分不存在按接口约定宽松处理；全部不存在 40401。
6. 测试库需预先创建 `express_address_book_test`；未做自动建库/迁移。
7. MySQL 测试通过 `@EnabledIfEnvironmentVariable(DB_PASSWORD)` 控制，未设置环境变量时会跳过。
8. `PhoneExtractor.normalizePhone` 改为 public 是保存复用的最小暴露，解析规则未变。

---

## 13. MS-04 前端开发准入条件

1. 前端姓名输入使用 `Array.from(value).length`，长度 1～50 码点；
2. 列表/CRUD/批删调用按 API 文档字段；
3. 保存只提交 `receiverName/phone/fullAddress/lineNo`，不得提交 `isEdited/original`；
4. 解析 API 输入上限仍由后端 `ParseInputGuard` 保证；
5. 不修改后端错误码语义；
6. Playwright 必须实际执行，不得用 MockMvc 结果冒充浏览器测试。

**MS-03 已完成并暂停，不进入 MS-04。**
