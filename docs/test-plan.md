# 测试计划与验收标准（test-plan.md）

| 项目 | 内容 |
| --- | --- |
| 文档版本 | v1.0 |
| 适用版本 | 第一阶段（设计）；用例随实现逐步执行 |
| 依据 | 任务书（功能与论文要求）、`requirements.md`（FR/NFR/BR）、`api-design.md`、`parser-design.md` |
| 重要声明 | **本文件不包含任何已执行结果**。所有"实际结果/结论"列一律为"待执行"，由第二阶段真实执行后回填；不得提前填写"通过" |
| 关联文档 | `development-roadmap.md`（里程碑出口条件）、`course-paper-outline.md`（论文中的测试章节） |

---

## 1. 测试目标

1. 证明任务书要求的每一项功能真实可用：多行录入、识别、结构化预览、人工修改、确认保存、列表浏览、新建、编辑、删除、批量删除。
2. 证明核心业务规则成立：解析不落库、保存二次校验、部分失败不丢有效行、地址原文不被截断、批删有校验。
3. 证明课程实践页面与 Servlet 演示**真实生效**，不是静态文字。
4. 为论文提供**可复现**的证据：真实截图、真实接口响应、真实测试记录。

---

## 2. 测试范围与不测范围

| 在范围内 | 不在范围内 |
| --- | --- |
| 解析器规则与边界 | 高并发/压测（无此需求） |
| 8 个 REST 接口的成功/失败路径 | 登录鉴权（无此功能） |
| 数据库读写与事务回滚 | 多浏览器全矩阵（只测 Chrome/Edge 最新两版） |
| 前端核心交互与状态 | 移动端适配（明确不做） |
| Web 基础实践页 19 节 | 视觉设计像素级评审（只做可用性走查） |
| Servlet 映射 10 条矩阵 | 容器性能调优 |
| 安全基线（注入、敏感信息、日志） | 渗透测试 |

---

## 3. 测试分层与工具

| 层级 | 工具 | 范围 | 运行时机 | 是否纳入 `mvn test` |
| --- | --- | --- | --- | --- |
| L1 单元测试 | JUnit 5（无 Spring） | `AddressParserService` 及提取器、`LogMasker`、校验工具 | 每次提交 | ✅ |
| L2 切片测试 | `@WebMvcTest` + MockMvc | Controller 参数、响应结构、异常映射 | 每次提交 | ✅ |
| L3 服务测试 | JUnit 5 + Mockito | `AddressServiceImpl` 事务调用、二次校验、批量边界 | 每次提交 | ✅ |
| L4 集成测试 | `@SpringBootTest` + 真实 MySQL | 真实 SQL、逻辑删除、分页、事务回滚 | 合并前手动执行 | ❌（`@Tag("integration")`，需 `-Dgroups=integration -DexcludedGroups=`） |
| L5 端到端 | Playwright（Chromium） | 10 步核心业务旅程、Web 实践页、Servlet 演示 | 里程碑/验收前 | ❌ |
| L6 手工验收 | 浏览器 + Postman | 视觉、提示文案、二次确认、截图采集 | 验收前 | ❌ |

> 集成测试不引入 Testcontainers（需要 Docker，且属不必要中间件），改用本机 MySQL + 独立测试库 `express_address_book_test`，执行前用 `schema.sql` 初始化。pom 中 surefire 配 `<excludedGroups>integration</excludedGroups>`（`mvn test` 默认跳过），需要跑集成测试时用 `mvn test -Dgroups=integration -DexcludedGroups=`。

---

## 4. 测试环境矩阵

| 项 | 版本/配置 |
| --- | --- |
| 操作系统 | Windows 11（开发机） |
| JDK | 17（LTS） |
| Maven | 3.9+ |
| MySQL | 8.0.x，字符集 utf8mb4，库名 `express_address_book`（测试用 `_test`） |
| Node.js | 20 LTS 或 22 LTS |
| 浏览器 | Chrome 最新版、Edge 最新版 |
| 后端端口 | 8080 |
| 前端端口 | 5173（Vite 代理 `/api`、`/demo`） |
| 分辨率 | 1920×1080 与 1366×768 各走查一次 |

---

## 5. L1 解析器测试用例（TC-PARSE）

用例来源与预期值以 `parser-design.md` §6 为准；下表是**可执行清单**。

| 编号 | 场景 | 输入（单行） | 预期 | 优先级 |
| --- | --- | --- | --- | --- |
| TC-PARSE-001 | 中文逗号标准行 | `菜小鸟，136xxxx0000，浙江省杭州市西湖区湖滨小区12栋301室` | VALID；姓名/电话/地址均正确 | P0 |
| TC-PARSE-002 | 英文逗号 | `张小明,13900139000,浙江省杭州市滨江区某某小区5栋202室` | VALID | P0 |
| TC-PARSE-003 | Tab 分隔 | `李四\t13800138000\t湖北省武汉市江岸区沿江小区8栋602室` | VALID | P0 |
| TC-PARSE-004 | 中文分号 | `王五；13800138001；湖北省宜昌市西陵区XX路1号` | VALID | P1 |
| TC-PARSE-005 | 英文分号 | `王五;13800138001;湖北省宜昌市西陵区XX路1号` | VALID | P1 |
| TC-PARSE-006 | 电话在行首 | `13800138000，王五，湖北省孝感市孝南区东苑小区15栋201室` | VALID，姓名=王五 | P0 |
| TC-PARSE-007 | 姓名，地址，电话 | `赵六，湖北省宜昌市西陵区XX路1号，13900139000` | VALID，地址不含电话 | P0 |
| TC-PARSE-008 | 地址，姓名，电话 | `湖北省宜昌市西陵区XX路1号，赵六，13900139000` | VALID（走"电话前最后一段"） | P1 |
| TC-PARSE-009 | 地址内英文逗号 | `菜小鸟，浙江省杭州市,西湖区、XX路12号，13800138000` | 地址保留 `,` 与 `、` | P0 |
| TC-PARSE-010 | 地址内顿号 | `菜中鸟，13800138002，湖北省武汉市江岸区沿江路、解放大道12号` | 地址完整保留顿号 | P0 |
| TC-PARSE-011 | 脱敏手机号 | `菜小鸟，136xxxx0000，浙江省杭州市西湖区XX小区` | VALID（BR-13） | P0 |
| TC-PARSE-012 | 缺电话 | `只有姓名没有电话` | INVALID，E103、E105（无地址） | P0 |
| TC-PARSE-013 | 缺姓名 | `13800138000，浙江省杭州市西湖区XX小区` | INVALID，E102 | P0 |
| TC-PARSE-014 | 电话格式错误 | `菜小鸟，12345，浙江省杭州市西湖区XX小区` | INVALID，E104 | P0 |
| TC-PARSE-015 | 缺地址 | `菜小鸟，13800138000，` | INVALID，E105 | P0 |
| TC-PARSE-016 | 电话被括号包裹 | `（13800138000）李四，湖北省武汉市江岸区沿江小区8栋602室` | VALID，电话不含括号 | P1 |
| TC-PARSE-017 | 座机带区号 | `公司前台，0571-88888888，浙江省杭州市西湖区XX大厦8楼` | VALID，电话规范化 | P1 |
| TC-PARSE-018 | `+86` 与空格短横 | `John, +86 138-0013-8000, Room 301, Building 12, Hangzhou` | 电话=13800138000；地址保留英文逗号 | P1 |
| TC-PARSE-019 | 英文姓名 | `John Smith,13800138000,Room 301, Building 12, Hangzhou` | 姓名=John Smith | P1 |
| TC-PARSE-020 | 空行跳过 | 三行文本中间夹一空行 | `skippedLines=1`，行号仍为 1/2/4 | P0 |
| TC-PARSE-021 | 行号连续性 | 第 1 行空、第 2 行合法 | 合法行 `lineNo=2` | P0 |
| TC-PARSE-022 | 全角数字 | `菜小鸟，１３８００１３８０００，浙江省杭州市西湖区XX小区` | VALID，电话转半角 | P1 |
| TC-PARSE-023 | 姓名含数字 | `菜小鸟12，13800138000，浙江省杭州市西湖区XX小区` | INVALID，E106 | P1 |
| TC-PARSE-024 | 地址超长 | 260 字地址 | INVALID，E107 | P1 |
| TC-PARSE-025 | 电话超长 | 21 位纯数字段 | INVALID，E108（超过 20 位） | P2 |
| TC-PARSE-026 | 单行超长 | 501 字符单行 | INVALID，E110 | P2 |
| TC-PARSE-027 | 纯空白行 | `"   "` | 跳过，计入 `skippedLines` | P1 |
| TC-PARSE-028 | 原文保留 | 含 `#`、`-`、`（）` 的地址 | 地址逐字保留 | P0 |
| TC-PARSE-029 | 同一行两个号码 | `菜小鸟，13800138000，13900139000，浙江省杭州市西湖区XX小区` | 取前者为电话，后者留在地址；不崩溃 | P2 |
| TC-PARSE-030 | 一致性/幂等 | 同一输入连续解析 2 次 | 两次结果逐字段相同 | P1 |
| TC-PARSE-031 | 无效行不影响有效行 | 3 行中第 2 行缺电话 | 返回 3 条，2 条 VALID、1 条 INVALID | P0 |
| TC-PARSE-032 | 空文本 | `""` / 纯空行 | 接口 400/40001（DTO 校验拒绝，不进解析器） | P0 |
| TC-PARSE-033 | 空格分隔（无逗号） | `菜小鸟 13600136000 浙江省杭州市西湖区湖滨小区12栋301室` | VALID（空格不作分隔符，靠电话定位） | P1 |
| TC-PARSE-034 | 完全无分隔符 | `菜小鸟13600136000浙江省杭州市西湖区湖滨小区12栋301室` | VALID | P1 |

**附加结构断言（单元层）**
- `validCount + invalidCount == effectiveLines`
- `totalLines - skippedLines == effectiveLines`
- `items` 顺序与输入行顺序一致
- `AddressParserService` 的字段中不存在 `Mapper`/`DataSource` 类型（反射断言，保障解析与持久化分离）

---

## 6. L2/L3 地址管理用例（TC-ADDR）

### 6.1 保存与校验

| 编号 | 场景 | 步骤 | 预期 | 优先级 |
| --- | --- | --- | --- | --- |
| TC-ADDR-010 | 批量保存合法 3 条 | `POST /batch` | 200，`savedCount=3`，库中 3 条 | P0 |
| TC-ADDR-011 | 批量保存含非法电话 | 第 3 条 `phone=123` | 400/40001，`errors` 指向 `items[2]`，库中 0 条 | P0 |
| TC-ADDR-012 | 批量保存空数组 | `items=[]` | 400/40001 | P0 |
| TC-ADDR-013 | 批量保存 201 条 | 超过上限 | 400/40003 | P1 |
| TC-ADDR-014 | 姓名超长 | 51 字姓名 | 400/40001 | P1 |
| TC-ADDR-015 | 地址超长 | 256 字地址 | 400/40001 | P1 |
| TC-ADDR-016 | 保存前修改值生效 | 把预览里的地址改掉后保存 | 库中为新地址 | P0 |
| TC-ADDR-017 | 解析不入库 | 仅 `POST /parse` 后查总数 | 总数不变 | P0 |
| TC-ADDR-018 | 事务回滚 | L3：Mockito 让第 2 次 `insert` 抛 `DataAccessException`，断言异常向上抛出且无第 3 次调用；L4：在测试库临时撤销 `INSERT` 权限制造真实失败 | 库中第 1 条也不存在（事务回滚） | P0 |

### 6.2 查询

| 编号 | 场景 | 步骤 | 预期 | 优先级 |
| --- | --- | --- | --- | --- |
| TC-ADDR-020 | 默认分页 | `GET ?page=1&size=10` | 10 条以内，`total` 正确，按时间倒序 | P0 |
| TC-ADDR-021 | 第二页 | `page=2` | 与第一页无重复 | P0 |
| TC-ADDR-022 | 关键词命中姓名 | `keyword=菜小鸟` | 只返回该姓名 | P0 |
| TC-ADDR-023 | 关键词命中地址 | `keyword=西湖` | 返回地址含西湖的记录 | P0 |
| TC-ADDR-024 | 关键词命中电话 | `keyword=138` | 返回电话含 138 的记录 | P1 |
| TC-ADDR-025 | 关键词无结果 | `keyword=不存在xyz` | `records=[]`，`total=0`，HTTP 200 | P1 |
| TC-ADDR-026 | 关键词超长 | 51 字 | 400/40001 | P2 |
| TC-ADDR-027 | 分页越界 | `page=999` | `records=[]`，`total` 正确 | P2 |
| TC-ADDR-028 | 查询单条 | `GET /{id}` | 返回完整字段 | P0 |
| TC-ADDR-029 | 查询不存在 | `GET /999999` | 404/40401 | P0 |

### 6.3 新建/编辑/删除

| 编号 | 场景 | 步骤 | 预期 | 优先级 |
| --- | --- | --- | --- | --- |
| TC-ADDR-030 | 新建成功 | `POST /` 三字段合法 | 200，返回 id，库中新增 | P0 |
| TC-ADDR-031 | 新建缺姓名 | `receiverName=""` | 400/40001 | P0 |
| TC-ADDR-032 | 编辑成功 | `PUT /{id}` | 200，库中更新，`update_time` 变化 | P0 |
| TC-ADDR-033 | 编辑不存在 | `PUT /999999` | 404/40401 | P0 |
| TC-ADDR-034 | 删除单条 | `DELETE /{id}` | 200；列表不再出现；库中 `is_deleted=1` | P0 |
| TC-ADDR-035 | 重复删除 | 对同一 id 再删 | 404/40401 | P1 |
| TC-ADDR-036 | 删除后编辑 | 对已删 id 执行 PUT | 404/40401 | P1 |
| TC-ADDR-037 | 批量删除 2 条 | `POST /batch-delete` | 200，`deletedCount=2` | P0 |
| TC-ADDR-038 | 批量删除空数组 | `ids=[]` | 400/40001 | P0 |
| TC-ADDR-039 | 批量删除非法 id | `ids=["abc"]` | 400/40001 | P0 |
| TC-ADDR-040 | 批量删除混合存在与不存在 | `["1","999999"]` | 删除存在的，`deletedCount=1` | P1 |
| TC-ADDR-041 | 批量删除重复 id | `["1","1"]` | 去重，`deletedCount=1` | P2 |
| TC-ADDR-042 | 批量删除 >200 | 201 个 id | 400/40003 | P2 |
| TC-ADDR-043 | 保存前电话规范化 | 新建时 `phone="+86 138-0013-8000"` | 200；库中存 `13800138000` | P1 |
| TC-ADDR-044 | 脱敏号可保存 | 新建时 `phone="136xxxx0000"` | 200；库中原样保留 `x` | P1 |
| TC-ADDR-045 | 地址标点不被改写 | 新建时地址含 `,`、`、`、`（）` | 200；库中与请求逐字相同 | P0 |

---

## 7. 接口契约用例（TC-API）

| 编号 | 场景 | 预期 |
| --- | --- | --- |
| TC-API-001 | 成功响应外层结构 | 必含 `code`、`message`、`data`；`code=0` |
| TC-API-002 | 参数校验失败结构 | HTTP 400，`code=40001`，`data.errors[]` 含 `field`、`message` |
| TC-API-003 | 路径参数类型错误 | `GET /api/addresses/abc` → 400/40001，提示"参数类型不正确" |
| TC-API-004 | JSON 格式错误 | 发送 `{bad json` → 400/40002 |
| TC-API-005 | 空 body | `POST /api/addresses` 无 body → 400/40002 |
| TC-API-006 | 不存在的路径 | `GET /api/not-exist` → 404，`code=40402`，不返回 HTML 堆栈页 |
| TC-API-007 | 方法不支持 | `GET /api/addresses/batch-delete` → 405，`code=40501` |
| TC-API-008 | 响应不含敏感信息 | 500 时 `message` 不含 SQL/堆栈/表名 |
| TC-API-009 | id 为字符串 | 响应 `id` 为 `"1"` 而非 `1` |
| TC-API-010 | 时间格式 | `createTime` 匹配 `^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$` |
| TC-API-011 | 空列表为 `[]` | 无数据时 `records` 为 `[]`，不为 `null` |
| TC-API-012 | 不返回 `isDeleted` | 所有 VO 无该字段 |

---

## 8. 前端交互用例（TC-UI）

| 编号 | 场景 | 步骤 | 预期 |
| --- | --- | --- | --- |
| TC-UI-001 | 空文本识别 | 文本域为空点识别 | 提示"请输入内容"，不发请求 |
| TC-UI-002 | 识别 loading | 输入 3 行点识别 | 按钮 loading，表格出现 3 行 |
| TC-UI-003 | 失败行展示 | 3 行中 1 行缺电话 | 该行红色状态 + 原因，另 2 行正常 |
| TC-UI-004 | 单元格编辑 | 修改第 1 行姓名 | 表格值变化，状态重新计算 |
| TC-UI-005 | 删除预览行 | 点某行删除 | 该行消失，其余行保留（行号不变） |
| TC-UI-006 | 保存成功 | 保存全部有效行 | toast 成功，预览清空，列表可查 |
| TC-UI-007 | 保存失败保留数据 | 把电话改成 `123` 后保存 | 报错定位到该行，预览内容不丢 |
| TC-UI-008 | 列表加载态 | 刷新列表页 | 出现 loading/骨架，不闪白 |
| TC-UI-009 | 空数据态 | 清空库后访问列表 | 显示"暂无地址"空状态 |
| TC-UI-010 | 搜索 | 输入关键词回车 | 结果更新，页码回 1 |
| TC-UI-011 | 新建表单校验 | 空表单提交 | 三字段分别提示必填 |
| TC-UI-012 | 删除二次确认 | 点删除 | 弹出确认框；点取消不发请求 |
| TC-UI-013 | 批量删除未选中 | 不勾选点批量删除 | 按钮禁用或提示"请先选择" |
| TC-UI-014 | 批量删除确认 | 勾选 2 条确认 | 请求发出，成功后列表刷新、勾选清空 |
| TC-UI-015 | 网络异常 | 停掉后端后操作 | 明确错误提示，不白屏、不抛控制台未捕获异常 |
| TC-UI-016 | 分辨率 1366×768 | 全流程走查 | 无横向滚动条溢出、按钮不被遮挡 |

---

## 9. Web 基础实践页用例（TC-WEB）

对 19 个小节逐节验证（对应 `architecture.md` §6.2）：

| 编号 | 小节 | 验证点 | 通过标准 |
| --- | --- | --- | --- |
| TC-WEB-01 | 2.1 行内显示类型 | 改 `width/height` | 尺寸无变化；上下 margin 无位移；上下 padding/border 与邻行重叠 |
| TC-WEB-02 | 2.2 区块显示类型 | 相邻两个 div | 各占一行；`margin:0 auto` 居中生效；`margin` 折叠可见 |
| TC-WEB-03 | 2.3 行内区块 | 两个 `inline-block` | 同行排列；元素间有空白间隙；`vertical-align` 改变对齐 |
| TC-WEB-04 | 2.4 弹性 | 切换 `justify-content` | 五个取值位置明显不同；`flex:1` 等分 |
| TC-WEB-05 | 2.5 网格 | 切换列数与 `span` | 二维排布正确；与 Flex 对比说明一致 |
| TC-WEB-06 | 2.6 盒模型各部分 | 观察四层标注 | 能读出 content/padding/border/margin 尺寸 |
| TC-WEB-07 | 2.7 标准盒模型 | `content-box` | 总宽 = width + padding + border（数值可核对） |
| TC-WEB-08 | 2.8 替代盒模型 | `border-box` | 总宽 = width，内容区变小 |
| TC-WEB-09 | 2.9 常规流 | 静态定位 + top/left | top/left 无效；块纵向堆叠 |
| TC-WEB-10 | 2.10 相对定位 | 偏移 | 原位置保留，后续元素不移动 |
| TC-WEB-11 | 2.11 绝对定位 | 偏移 | 后续元素上移；参照最近定位祖先 |
| TC-WEB-12 | 2.12 定位上下文 | 切换父级 `relative` | 子元素位置明显改变 |
| TC-WEB-13 | 2.13 固定定位 | 滚动容器 | 元素相对视口不动 |
| TC-WEB-14 | 2.14 粘性定位 | 滚动 | 到达阈值后吸附 |
| TC-WEB-15 | 2.15 粘性约束矩形 | 两种父容器高度 | 父容器不足时 sticky 被推走 |
| TC-WEB-16 | 2.16 上下左右 | 四个方向分别设置 | 现象与说明一致 |
| TC-WEB-17 | 2.17 覆盖影响 | 同时设 left+right | `width:auto` 拉伸；`width` 固定时 left 生效 |
| TC-WEB-18 | 2.18 线框仿制 | 打开整页 | 区域划分与原网页一致；三张截图齐备 |
| TC-WEB-19 | 2.19 布局原则 | 逐条对照 | 每条原则都能指向本项目真实代码位置 |
| TC-WEB-20 | 锚点导航 | 点击左侧目录 | 平滑滚动到对应小节，标题高亮 |

---

## 10. Servlet 映射用例（TC-SERVLET）

对应 `architecture.md` §7.4 的 10 条矩阵：

| 编号 | 请求 | 预期 | 优先级 |
| --- | --- | --- | --- |
| TC-SERVLET-01 | `GET /demo/servlet/exact` | 命中 Exact servlet，`pathInfo=null` | P0 |
| TC-SERVLET-02 | `GET /demo/servlet/exact/extra` | 命中 Prefix servlet，`pathInfo=/exact/extra` | P0 |
| TC-SERVLET-03 | `GET /demo/servlet/prefix/a/b` | 命中 Prefix servlet | P0 |
| TC-SERVLET-04 | `GET /demo/legacy/report.do` | 命中 Extension servlet | P0 |
| TC-SERVLET-05 | `GET /demo/legacy/report.json` | 404（无扩展名映射） | P0 |
| TC-SERVLET-06 | `POST /demo/servlet/exact` | 仍命中 Exact servlet（容器不看方法） | P0 |
| TC-SERVLET-07 | `GET /demo/spring/info` | 命中 Spring Controller | P0 |
| TC-SERVLET-08 | `GET /demo/spring/echo/张三` | 命中 Controller，路径变量正确 | P0 |
| TC-SERVLET-09 | `POST /demo/spring/method-only` | 405（Spring 看方法） | P0 |
| TC-SERVLET-10 | `GET /api/addresses?page=1&size=10` | 命中 AddressController | P0 |
| TC-SERVLET-11 | 演示页"实际命中"列 | 与响应 JSON 的 `handler` 一致，非前端写死 | P0 |
| TC-SERVLET-12 | 中文响应 | `pathInfo` 含中文时不乱码 | P1 |
| TC-SERVLET-13 | 静态资源不受影响 | `/`、`/import` 等前端路由正常（开发期由 Vite 提供） | P1 |

---

## 11. 端到端旅程（TC-E2E，Playwright）

按 AGENTS 9.3 的 10 步设计，**必须真实执行**：

| 步骤 | 操作 | 断言 |
| --- | --- | --- |
| 1 | 打开 `http://localhost:5173/` | 进入 `/import`，标题可见 |
| 2 | 在 `[data-test=parse-textarea]` 填入 3 行 | 输入值正确 |
| 3 | 点 `[data-test=parse-btn]` | 表格出现 3 行，状态为 3 条 VALID |
| 4 | 检查解析结果 | 姓名/电话/地址与预期一致 |
| 5 | 修改第 1 行地址 | 单元格值更新 |
| 6 | 点 `[data-test=save-btn]` | toast 成功，预览清空 |
| 7 | 进入 `/addresses` | 列表出现 3 条（含修改后的地址） |
| 8 | 点编辑，改电话并保存 | 列表显示新电话 |
| 9 | 删除一条，确认 | 总数减 1 |
| 10 | 勾选 2 条批量删除，确认 | 总数再减 2，剩 0 条 |

补充场景（同文件内独立 test）：
- E2E-02：Bad path —— 输入含非法电话的 3 行，识别后保存，断言报错且库无新增。
- E2E-03：Web 实践页 —— 逐节存在 19 个标题，锚点可跳转。
- E2E-04：Servlet 演示页 —— 点第 1、4 条按钮，断言"实际命中"列文本与预期一致。
- E2E-05：空状态 —— 清空数据后访问列表，断言空文案出现。

**Playwright 约定**
- 首次执行前：`npm i -D @playwright/test` + `npx playwright install chromium`（仅安装 Chromium，避免下载全部浏览器）。
- `playwright.config.ts`：`baseURL=http://localhost:5173`，`use: { browserName: 'chromium', viewport: { width: 1440, height: 900 } }`，`trace: 'on-first-retry'`。
- 前后端需**先启动**（不在配置里自动拉起，避免掩盖环境问题）。
- **数据隔离**：E2E 使用专用库 `express_address_book_e2e`，每个 test 开始前执行 `TRUNCATE express_address`（或 `DELETE` + 重置自增）；断言基于本次用例自己创建的数据，不依赖历史数据。E2E-05 依赖清空动作，放在最后执行或独立 `describe`。
- 选择器优先 `data-test`，不依赖 Element Plus 内部 class。
- 截图输出 `docs/screenshots/e2e/`，失败时保留 trace。

### 11.1 测试标识（data-test）契约

| 位置 | 标识 |
| --- | --- |
| 录入文本域 | `parse-textarea` |
| 识别按钮 | `parse-btn` |
| 预览表格 | `parse-table` |
| 保存按钮 | `save-btn` |
| 列表表格 | `address-table` |
| 搜索框 | `search-input` |
| 新建按钮 | `create-btn` |
| 行内编辑按钮 | `edit-btn` |
| 行内删除按钮 | `delete-btn` |
| 批量删除按钮 | `batch-delete-btn` |

> Element Plus 的确认框按钮用文案定位（"确定"/"取消"），避免依赖内部结构。

---

## 12. 安全与性能用例

### 12.1 安全（TC-SEC）

| 编号 | 检查 | 方法 | 通过标准 |
| --- | --- | --- | --- |
| TC-SEC-01 | SQL 注入 | `keyword=' OR '1'='1`、`keyword=%` | 参数化生效，无异常/无全表泄露 |
| TC-SEC-02 | 口令不入库 | 检索仓库 | 无明文数据库口令；`application-local.yml` 被忽略 |
| TC-SEC-03 | 日志脱敏 | 触发解析与保存，看日志 | 无完整手机号/地址 |
| TC-SEC-04 | 错误信息不泄露 | 故意触发 500 | 响应无 SQL/堆栈/表名 |
| TC-SEC-05 | 越权（无鉴权） | 直接调用全部接口 | 属预期：本系统无身份体系，需在论文说明 |
| TC-SEC-06 | 参数边界 | 超长、特殊字符、emoji | 不崩溃；按校验返回 400 |
| TC-SEC-07 | XSS | 地址填 `<script>alert(1)</script>` | Vue 默认转义，页面不执行脚本 |

### 12.2 性能（TC-PERF）

| 编号 | 场景 | 目标 | 记录方式 |
| --- | --- | --- | --- |
| TC-PERF-01 | 解析 100 行 | ≤300 ms | 单测中 `System.nanoTime()` 计时，记录到本文件附录 A |
| TC-PERF-02 | 1 万条分页查询 | ≤500 ms | MySQL 造数后 Postman 计时 |
| TC-PERF-03 | 批量保存 200 条 | ≤2 s | Postman 计时 |

> 目标值用于判断是否可接受，不作为"已达标"的结论；附录 A 记录真实数字。

---

## 13. 验收标准（AC）

### 13.1 功能验收

| 编号 | 验收项 | 通过条件 | 对应需求 |
| --- | --- | --- | --- |
| AC-01 | 多行文本录入 | 能粘贴/输入多行，含中英文分隔符与 Tab | FR-01 |
| AC-02 | 识别可用 | 点识别得到结构化结果，且**数据库无新增** | FR-02、BR-05 |
| AC-03 | 结果可核对 | 表格含行号、原始行、三字段、状态、原因 | FR-03 |
| AC-04 | 结果可修改 | 三字段均可编辑，失败行可人工补齐 | FR-04、BR-08 |
| AC-05 | 部分失败不丢 | 3 行中 1 行失败，另 2 行仍可保存 | FR-03、BR-06 |
| AC-06 | 确认保存 | 保存的是修改后的值；非法值被后端拒绝 | FR-05、BR-12 |
| AC-07 | 列表浏览 | 分页正确、排序稳定、空状态正常 | FR-08、FR-14 |
| AC-08 | 搜索 | 姓名/电话/地址均可命中 | FR-09 |
| AC-09 | 新建 | 三字段校验有效，保存后可查 | FR-07、FR-10 |
| AC-10 | 编辑 | 回填正确，保存生效 | FR-11 |
| AC-11 | 删除 | 二次确认；取消不发请求；确认后消失 | FR-12 |
| AC-12 | 批量删除 | 未选禁用/提示；确认后数量正确；非法 ids 被拒 | FR-13、BR-11 |
| AC-13 | 地址完整性 | 含 `,`、`、`、`#`、`（）` 的地址保存后逐字不变 | BR-04 |
| AC-14 | 统一响应 | 所有 `/api/**` 接口（含 404/405）外层结构一致，`code=0` 表示成功 | FR-18 |
| AC-15 | 异常可读 | 参数缺失/格式错误/不存在均有中文提示且无堆栈 | FR-18、NFR-05 |

### 13.2 课程实践验收

| 编号 | 验收项 | 通过条件 |
| --- | --- | --- |
| AC-16 | 2.1～2.19 页面 | 19 节全部可运行、演示真实生效、有观察结论与代码块 |
| AC-17 | 2.18 线框仿制 | 实现页 + 线框稿 + 原网页三图齐备，区域划分一致 |
| AC-18 | 2.20 Servlet 演示 | 10 条矩阵实测通过；能讲清"容器映射 vs Spring 路由" |
| AC-19 | MVC 体现 | 分层目录清晰；能现场指出一次请求的完整链路 |
| AC-20 | 论文素材 | 每节都有真实截图；截图与当前代码一致；无编造 |

### 13.3 工程验收

| 编号 | 验收项 | 通过条件 |
| --- | --- | --- |
| AC-21 | 后端测试 | `mvn clean test` 通过；不允许跳过或注释测试 |
| AC-22 | 前端构建 | `npm run build` 无 TypeScript 错误、无构建告警阻塞 |
| AC-23 | E2E | 10 步旅程真实执行通过（失败需有记录与修复） |
| AC-24 | 安全 | TC-SEC 全通过；仓库无密钥、无真实个人信息 |
| AC-25 | 可复现 | 按 README 从零可启动；`schema.sql`/`data.sql` 可重复执行 |
| AC-26 | 一致性 | 四份设计文档与实际代码字段/接口一致（不一致先改文档） |

---

## 14. 测试结果记录表（待执行）

> 当前阶段**只建表不填结果**。第二阶段每执行一轮填写一行，并保留命令输出截图。

| 轮次 | 日期 | 范围 | 命令 | 执行人 | 通过/总数 | 结论 | 备注 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| R1 | 待定 | L1 解析器 | `mvn -Dtest=AddressParserServiceTest test` | 待定 | 待执行 | 待执行 | — |
| R2 | 待定 | L2/L3 Controller/Service | `mvn test` | 待定 | 待执行 | 待执行 | — |
| R3 | 待定 | L4 集成（真实 MySQL） | `mvn test -Dgroups=integration -DexcludedGroups=` | 待定 | 待执行 | 待执行 | 需先建 `_test` 库 |
| R4 | 待定 | L5 E2E | `npx playwright test` | 待定 | 待执行 | 待执行 | — |
| R5 | 待定 | L6 手工验收 | 浏览器 + Postman | 待定 | 待执行 | 待执行 | 含截图采集 |

### 附录 A：性能记录（待执行）

| 编号 | 数据量 | 实测耗时 | 目标 | 结论 |
| --- | --- | --- | --- | --- |
| TC-PERF-01 | 100 行 | 待执行 | ≤300 ms | 待执行 |
| TC-PERF-02 | 1 万条 | 待执行 | ≤500 ms | 待执行 |
| TC-PERF-03 | 200 条 | 待执行 | ≤2 s | 待执行 |

---

## 15. 缺陷管理与出口条件

| 项 | 约定 |
| --- | --- |
| 缺陷记录 | GitHub Issues，标签 `bug/P0`、`bug/P1`、`bug/P2`，附复现步骤与环境 |
| 回归 | 修复后必须重跑相关用例 + `mvn test` |
| 准出条件 | P0 用例 100% 通过；P1 ≥ 95%；无未修复 P0 缺陷；AC-01～AC-26 全部满足 |
| 论文数据 | 只引用**已执行**轮次的真实数字；未执行部分写"待后续版本验证" |

---

## 16. 不伪造声明

1. 本文件所有"预期/目标"均为设计值，**不是**执行结果。
2. 论文中的测试章节必须引用本文件附录的真实记录，并注明执行日期、环境、命令。
3. 若某条用例因环境无法执行，必须写明"未执行及原因"，不得写"通过"。
4. 截图必须来自当前仓库代码的运行实例；不得使用任务书附录中的参考图充当实现效果（2.18 的"原网页"图除外，且需标注来源）。

---

## 17. 变更记录

| 版本 | 日期 | 变更 | 说明 |
| --- | --- | --- | --- |
| v1.0 | 2026-11-14 | 用例体系（TC-PARSE/ADDR/API/UI/WEB/SERVLET/E2E/SEC/PERF）与 AC-01～AC-26 定稿 | 尚未执行任何用例 |
