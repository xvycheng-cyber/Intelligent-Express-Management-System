# 项目上下文（PROJECT_CONTEXT.md）

> 本文件记录本项目长期稳定的背景、架构、业务边界和开发规范。动态进度、遗留问题和下一阶段任务见：
> - [`ORIGINAL_DEVELOPMENT_PLAN.md`](ORIGINAL_DEVELOPMENT_PLAN.md)
> - [`development-progress.md`](development-progress.md)
> - [`handover/CHATGPT_PROJECT_HANDOVER.md`](handover/CHATGPT_PROJECT_HANDOVER.md)

| 项目 | 内容 |
| --- | --- |
| 仓库标识 | `Intelligent-Express-Management-System` |
| 系统名称 | 快递通——智能快递地址簿录入与管理系统 |
| 课程论文题目 | 快递地址簿录入程序设计（不得改写） |
| 课程任务 | 任务（一）快递地址簿录入程序设计；不采用任务（二）名片管理系统 |
| 当前设计基线 | v1.1-final，后续有 MS-02.1～MS-08 的实际实现与测试修正 |
| 技术栈 | Java 17 + Spring Boot 3.3.5 + Spring MVC + MyBatis-Plus 3.5.7 + MySQL 8；Vue 3 + TypeScript + Vite + Element Plus + Axios + Vue Router + SCSS |
| 运行边界 | 默认仅监听 `127.0.0.1`；无登录、无权限、无公网部署承诺 |

---

## 1. 项目定位

本项目是面向**课程设计与论文**的桌面 Web 应用，核心不是快递下单或物流轨迹，而是：

1. 让用户把多行、格式不统一的联系人文本一次性粘贴进来；
2. 自动识别姓名、联系电话、详细地址；
3. 在表格中预览、人工修正、确认；
4. 以事务方式批量保存到地址簿；
5. 在地址簿中分页浏览、搜索、新建、编辑、删除和批量删除；
6. 通过 Web 基础实践页和 Servlet 映射演示覆盖课程任务书 2.1～2.20；
7. 用真实代码、真实测试和真实截图完成课程论文 2.21。

**任务书原文要求**见 [`requirements.md`](requirements.md) 的 CR-01～CR-18。原始任务书 `高级Web项目开发（实践）任务书.docx` 位于工作区根目录（仓库上一级），不在仓库内；本次审计未重新读取该 docx，只以已有需求文档中的逐条摘录为依据。

---

## 2. 核心业务

### 2.1 业务流程 S1：批量录入

```text
多行原文
  → POST /api/addresses/parse（只解析，不写库）
  → 前端解析预览（VALID / INVALID）
  → 人工修正姓名/电话/地址
  → 保存确认弹窗
  → POST /api/addresses/batch（整批事务）
  → 地址簿列表可见
```

关键规则：

- 空行忽略，但保留物理行号；
- 支持中文/英文逗号、Tab、中文/英文分号；
- 姓名、电话通过模式识别定位，不依赖固定字段顺序；
- 地址保留内部标点和原文，只剔除已识别字段；
- INVALID 行保留错误原因，不得自动变成 VALID；
- 无效行不得进入保存请求；
- 批量保存任一条失败 → 整批回滚；
- 解析接口与保存接口严格分离。

### 2.2 业务流程 S2～S4：地址簿管理

- S2 单条新建：列表页填写姓名/电话/地址 → 保存；
- S3 编辑：加载详情 → 修改 → 保存 → 刷新列表；
- S4 清理：单条删除或当前页批量删除，均有二次确认。

### 2.3 业务流程 S5：课程实践验收

- `/web-basics`：覆盖 2.1～2.19；
- `/web-basics/wireframe`：2.18 线框仿制；
- `/servlet-demo`：2.20 Servlet URI 映射演示；
- 论文 2.21：地址簿系统实现与截图。

---

## 3. 用户与范围

### 3.1 用户

只有一个角色：**录入员**。打开本机页面即可使用；没有注册、登录、会话、权限、验证码。

### 3.2 明确范围内

1. 匿名单用户、本机回环地址提供服务的 Web 系统；
2. 多行文本解析、预览、人工修正、批量保存；
3. 地址簿分页、关键词搜索、新建、编辑、逻辑删除、批量删除；
4. 批量事务和字段校验；
5. 课程 Web 基础实践页、线框页、Servlet 映射演示；
6. 论文素材、真实测试记录、截图。

### 3.3 明确范围外

- 注册、登录、找回密码、角色权限、验证码；
- 快递下单、运单查询、物流轨迹、快递公司 API；
- 地图选点、地址标准化/纠错、经纬度、行政区划库；
- OCR、语音、通讯录同步；
- Excel/CSV 导入导出（P2，不是 MVP）；
- 云端 AI 解析（P2；本项目未实现）；
- 多用户/多租户、审计日志、消息推送、缓存、微服务；
- 移动端/小程序/桌面客户端（当前只做桌面 Web；MS-04/MS-05 做了响应式样式，但不是移动端交付承诺）。

无身份认证不是“已实现认证”，只是课程范围限制；运行期缓解是仅监听 `127.0.0.1`。

---

## 4. 技术栈与版本

### 4.1 后端

| 组件 | 版本/配置 |
| --- | --- |
| Java | 17 |
| Spring Boot | 3.3.5 |
| Spring MVC | Boot 3.3.5 内 |
| MyBatis-Plus | 3.5.7（`mybatis-plus-spring-boot3-starter`） |
| MySQL | 8.x，InnoDB，`utf8mb4` |
| Validation | Jakarta Validation |
| Test | JUnit 5 / Spring Boot Test / MockMvc / Mockito |
| Build | Maven |

实际依赖见 [`backend/pom.xml`](../backend/pom.xml)。

### 4.2 前端

| 组件 | 版本 |
| --- | --- |
| Vue | 3.5.13 |
| TypeScript | 5.7.2 |
| Vite | 5.4.11 |
| Element Plus | 2.9.1 |
| Axios | 1.7.9 |
| Vue Router | 4.5.0 |
| Sass | 1.83.0 |
| Playwright | 1.65.1（devDependency，浏览器测试脚本使用本机 Chrome channel） |

实际依赖见 [`frontend/package.json`](../frontend/package.json)。

### 4.3 运行方式

- 后端：`cd backend && mvn spring-boot:run` → `http://127.0.0.1:8080`；
- 前端：`cd frontend && npm run dev` → `http://127.0.0.1:5173`；
- Vite 代理 `/api` → `http://127.0.0.1:8080`；
- 数据库：`express_address_book`；默认 root 本机连接，口令通过 `DB_PASSWORD` 或本地 `application-local.yml`，不得入库；
- 测试库：`express_address_book_test`；真实 MySQL 测试使用，测试表结构与 schema 一致。

---

## 5. 系统架构

### 5.1 后端分层

```text
HTTP
  → Controller（薄：收参/触发校验/调用 Service/统一响应）
  → Service（业务规则、事务、DTO→Entity）
  → Mapper（MyBatis-Plus BaseMapper + 自定义 XML）
  → MySQL
```

关键类：

- [`AddressController`](../backend/src/main/java/com/express/addressbook/controller/AddressController.java)：8 个地址 API；
- [`AddressService`](../backend/src/main/java/com/express/addressbook/service/AddressService.java)：分页、搜索、CRUD、批量；
- [`AddressParserService`](../backend/src/main/java/com/express/addressbook/parser/AddressParserService.java)：纯解析，无 Mapper/JDBC；
- [`ExpressAddressMapper`](../backend/src/main/java/com/express/addressbook/mapper/ExpressAddressMapper.java) + [`ExpressAddressMapper.xml`](../backend/src/main/resources/mapper/ExpressAddressMapper.xml)；
- [`ExpressAddress`](../backend/src/main/java/com/express/addressbook/entity/ExpressAddress.java)；
- [`GlobalExceptionHandler`](../backend/src/main/java/com/express/addressbook/common/GlobalExceptionHandler.java)；
- [`JacksonConfig`](../backend/src/main/java/com/express/addressbook/config/JacksonConfig.java)；
- [`MybatisPlusConfig`](../backend/src/main/java/com/express/addressbook/config/MybatisPlusConfig.java)。

### 5.2 前端分层

```text
views/          页面
components/     可复用组件
api/            统一 Axios 封装 + 接口函数
types/          API/地址类型
utils/          姓名码点、地址校验、toPayload
layout/         侧栏 + 顶栏
router/         路由
styles/         设计 token 与 Element Plus 覆盖
```

当前实际路由：

| 路径 | 视图 | 状态 |
| --- | --- | --- |
| `/` | 重定向 `/import` | 已实现 |
| `/import` | `ImportView` | 已实现 |
| `/addresses` | `AddressListView` | 已实现（MS-05，代码未提交） |
| `/web-basics` | `WebBasicsView` | 已实现（MS-06） |
| `/web-basics/wireframe` | `WireframeView` | 已实现（MS-06） |
| `/servlet-demo` | `ServletDemoView` | 已实现（MS-07.5） |
| 其他 | `NotFoundView` | 已实现 |

### 5.3 解析与保存分离

`parser` 包只做纯逻辑；`AddressController.parse` 只调用解析器，不访问 Mapper。保存由 `AddressService` 执行，有独立事务。该分层是项目核心不变量，不能合并。

---

## 6. 数据库设计

### 6.1 表

只有一张业务表：`express_address`。

| 列 | 类型 | 约束/说明 |
| --- | --- | --- |
| `id` | BIGINT | PK，AUTO_INCREMENT |
| `receiver_name` | VARCHAR(50) | NOT NULL；Unicode 码点 1～50 |
| `phone` | VARCHAR(20) | NOT NULL；保存前规范化 |
| `full_address` | VARCHAR(255) | NOT NULL；保留原文与内部标点 |
| `create_time` | DATETIME | 默认 CURRENT_TIMESTAMP |
| `update_time` | DATETIME | 默认 CURRENT_TIMESTAMP，ON UPDATE |
| `is_deleted` | TINYINT | 逻辑删除 0/1 |

索引：

- PRIMARY (`id`)；
- `idx_deleted_create` (`is_deleted`, `create_time`)；
- `idx_phone` (`phone`)。

初始化文件：[`schema.sql`](../backend/src/main/resources/db/schema.sql)、[`data.sql`](../backend/src/main/resources/db/data.sql)、[`admin-init.sql`](../backend/src/main/resources/db/admin-init.sql)。

### 6.2 关键设计决策

- 只建一张业务表；不拆省/市/区；不建用户、批次、解析任务表；
- 逻辑删除，列表/详情/更新/删除默认过滤 `is_deleted=0`；
- 时间由数据库默认值生成；
- 无外键；
- 关键词 `LIKE '%…%' ESCAPE '!'` 全表扫描，课程数据规模可接受；
- 相同姓名/电话允许多条，无唯一索引；
- 字符集 `utf8mb4`，排序规则 `utf8mb4_0900_ai_ci`。

### 6.3 长度语义

| 字段 | 语义 |
| --- | --- |
| 姓名 | Unicode 码点，1～50 |
| 电话 | 字符，最多 20 |
| 详细地址 | UTF-16 code unit，最多 255（与冻结文档一致） |
| rawText | UTF-16 code unit，最多 20000 |
| 单行文本 | 500 code unit（Parser 单行上限） |

注意：姓名刻意用码点；地址/原文的是既有冻结规则，不能未经完整分析改为码点。

---

## 7. API 概览

完整字段以 [`api-design.md`](api-design.md) 为准。

| 方法 | 路径 | 用途 |
| --- | --- | --- |
| POST | `/api/addresses/parse` | 多行解析，不写库 |
| GET | `/api/addresses` | 分页 + 关键词 |
| GET | `/api/addresses/{id}` | 详情 |
| POST | `/api/addresses` | 新建 |
| PUT | `/api/addresses/{id}` | 编辑 |
| DELETE | `/api/addresses/{id}` | 逻辑删除 |
| POST | `/api/addresses/batch` | 批量保存（事务） |
| POST | `/api/addresses/batch-delete` | 批量逻辑删除 |
| GET | `/api/health` | Web 健康检查 |
| GET | `/api/health/db` | 数据库连接检查 |

统一外层：

```json
{ "code": 0, "message": "OK", "data": {} }
```

业务码：0 / 40001 / 40002 / 40003 / 40401 / 40402 / 40501 / 50001 / 50002。字段错误在 `data.violations[]`；解析行错误在 `items[].errors[]`，两者不得混用。

分页字段 `total/current/size/pages` 是 JSON Number；地址 `id` 是 JSON String；时间为 `yyyy-MM-dd HH:mm:ss`。

---

## 8. 核心不变量

1. `parser` 不访问 Mapper/JDBC/MyBatis/IO；
2. 解析 API 不写库；
3. 保存 API 服务端重新校验，不信任前端；
4. INVALID 行不能进入批量保存；
5. 批量保存整批事务；批量删除只处理当前页选择的 ID；
6. LIKE 的 `%`、`_`、`!` 只转义一次，用 `ESCAPE '!'`；
7. 姓名长度统一 Unicode 码点；`Array.from` / `codePointCount`；
8. `isEdited/original/rawLine` 只属于前端派生状态，不进入 DTO/数据库；
9. 只监听 `127.0.0.1`；
10. 密码只通过环境变量/本地配置提供；
11. 不引入 Redis/MQ/搜索引擎/登录体系/AI 云解析。

---

## 9. 开发规范与完成定义

### 9.1 后端

- Controller 薄；业务在 Service；Mapper 只做数据访问；
- DTO 负责请求校验；Entity 不作为请求/响应；
- 逻辑删除用 `@TableLogic` + 全局配置；
- 批量保存/删除在 Spring Bean 公共方法上使用 `@Transactional`，不能同类自调用；
- SQL 必须参数化，搜索转义后绑定；
- 异常统一由 `GlobalExceptionHandler` 转换，不向页面暴露堆栈。

### 9.2 前端

- Composition API + TypeScript；
- API 全部放 `src/api`，使用统一 Axios `request`/`ApiError`；
- 页面/组件/类型/工具拆分；
- 姓名用 `Array.from`；ID 用 string；分页计数 number；
- `toPayload` 只提交业务字段；
- 列表选择只当前页，翻页/搜索/size/刷新清空；
- 加载/空/错误状态齐全；破坏性操作二次确认。

### 9.3 Definition of Done

原始路线图的定义（见 [`development-roadmap.md`](development-roadmap.md) §6）包括代码、测试、契约、状态、日志、现场演示和截图。**本审计严格区分**：

- IMPLEMENTED：代码存在且结构可读；
- TESTED：有可指向的测试/构建/浏览器执行证据；
- ACCEPTED：有明确人工验收结论；
- PARTIAL：部分完成；
- NOT_STARTED：未开始；
- BLOCKED：有明确阻塞证据。

当前没有正式签字验收的里程碑；详见进度文档。

---

## 10. 重要技术决策与调整

### 10.1 原始冻结决策

- 单表、`full_address` 不拆分；
- 逻辑删除；
- 无认证、本机回环；
- 解析纯逻辑、解析/保存分离；
- 批量保存 200 上限、201 → 40003；
- `isEdited` 派生、不入库；
- 错误结构分层；
- LIKE 字面匹配。

### 10.2 后续确认的调整

| 调整 | 来源/说明 |
| --- | --- |
| 数据库账号改为本机 root，口令外置；`express_app` 可选 | MS-01 后 `database-account-change-report.md` / requirements BR-22 |
| 姓名统一按 Unicode 码点 1～50 | MS-00C/MS-02.1，后续 DTO/前端同步 |
| `rawText ≤20000`、有效行 ≤200、201→40003 | MS-02.1 解析 API 输入上限 |
| 电话长数字 >20 → E108；E109 删除 | MS-02.1/MS-02.2 |
| 分页元数据必须 Number、ID 必须 String | MS-03.1 P0 契约修复 |
| keyword 按码点 ≤50、page/size 错误字段映射 | MS-03.1 |
| 保存接口允许常见原始电话/全角英文姓名并规范化 | MS-03.1 |
| 元素 Plus 使用 zh-cn、移动端分页换行 | MS-05 |
| Playwright 仅作为开发脚本，不是 CI | MS-04/MS-05 报告 |

### 10.3 临时建议/Backlog（不是正式需求）

- 登录/权限；
- AI 解析；
- Excel/CSV；
- 地图/地址标准化；
- 高级列表配置；
- 移动端正式适配；
- Playwright CI。

这些不能作为“已经承诺而未完成”的正式需求，除非课程要求或用户另行确认。

---

## 11. 文档入口

| 文档 | 作用 |
| --- | --- |
| [`requirements.md`](requirements.md) | 原始需求/BR/FR/NFR/范围 |
| [`architecture.md`](architecture.md) | 架构、模块、前端设计、实践页设计 |
| [`database-design.md`](database-design.md) | 表/索引/逻辑删除 |
| [`api-design.md`](api-design.md) | API 契约、错误结构 |
| [`parser-design.md`](parser-design.md) | 解析规则 PR-01～PR-13 |
| [`test-plan.md`](test-plan.md) | 测试层级、用例、执行结果 |
| [`development-roadmap.md`](development-roadmap.md) | 原始里程碑与排期 |
| [`course-paper-outline.md`](course-paper-outline.md) | 论文大纲 |
| [`review/`](review/) | MS-01～MS-05 历史报告 |
