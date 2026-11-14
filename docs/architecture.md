# 总体架构设计（architecture.md）

| 项目 | 内容 |
| --- | --- |
| 文档版本 | v1.1-final（设计冻结） |
| 架构风格 | 前后端分离 + 单体分层（Spring MVC 三层）+ 独立解析组件 |
| 关联文档 | `requirements.md`、`database-design.md`、`api-design.md`、`parser-design.md`、`test-plan.md` |
| 本文范围 | 总体架构、模块划分、目录结构、请求链路、Web 基础实践页面设计、Servlet 映射演示方案 |
| 不在本文范围 | 具体业务代码、SQL 明细（见 `database-design.md`）、接口字段明细（见 `api-design.md`） |

---

## 1. 架构总览

```text
┌──────────────────────────── 浏览器（Chrome / Edge） ────────────────────────────┐
│  Vue 3 + TypeScript + Vite + Element Plus + Vue Router + Axios                  │
│  /import   /addresses   /web-basics   /web-basics/wireframe   /servlet-demo     │
└───────────────┬──────────────────────────────────────────────┬─────────────────┘
                │ /api/**（JSON）                               │ /demo/**（演示请求）
                │                                              │
        ┌───────▼──────────────────────────────────────────────▼───────┐
        │                   Tomcat（嵌入式，端口 8080）                 │
        │  ┌──────────────────── URL 映射（Servlet 容器层）──────────┐  │
        │  │ 精确 /demo/servlet/exact                              │  │
        │  │ 前缀 /demo/servlet/*     扩展名 *.do                   │  │
        │  │ 默认 "/" → DispatcherServlet（Spring MVC 入口）        │  │
        │  └───────────────────────────┬───────────────────────────┘  │
        │                              │                              │
        │  ┌───────────────────────────▼───────────────────────────┐  │
        │  │ DispatcherServlet → HandlerMapping → HandlerAdapter   │  │
        │  │ Controller（薄）→ Service（业务/事务）→ Mapper（SQL） │  │
        │  └───────────────────────────┬───────────────────────────┘  │
        └──────────────────────────────┼──────────────────────────────┘
                                       │ MyBatis-Plus（参数化 SQL）
                              ┌────────▼────────┐
                              │   MySQL 8.0     │
                              │ express_address │
                              └─────────────────┘
```

关键分层原则：

| 层 | 只做 | 禁止 |
| --- | --- | --- |
| Controller | 收参、校验触发、调 Service、组装统一响应 | 写业务分支、写 SQL、直接调 Mapper |
| Service | 业务规则、事务边界、调用解析器 | 处理 `HttpServletRequest`、拼 JSON |
| Parser | 纯文本→结构化（无 IO） | 访问数据库、调用外部服务 |
| Mapper | 单表 CRUD（MyBatis-Plus） | 业务判断 |
| Vue 页面 | 交互与展示 | 直接 `axios`，必须走 `src/api/` |

---

## 2. 技术选型与版本基线

| 层 | 技术 | 版本基线 | 选型理由 |
| --- | --- | --- | --- |
| 语言 | Java | 17 (LTS) | 课程要求；Spring Boot 3 最低要求 |
| 框架 | Spring Boot | 3.x（3.2+） | 内置 Tomcat、自动配置、与 Spring MVC 一体 |
| Web | Spring MVC | 随 Boot | 注解式路由，符合 MVC 知识点的讲解需要 |
| 持久层 | MyBatis-Plus | 3.5.x | 单表 CRUD 零 SQL、逻辑删除与分页开箱可用 |
| 数据库 | MySQL | 8.0 | 课程要求；utf8mb4 |
| 校验 | Jakarta Validation | 随 Boot | DTO 注解式校验，统一异常处理 |
| 前端 | Vue 3 + TypeScript | 3.4+ / 5.x | Composition API + 类型约束 |
| 构建 | Vite | 5.x | 秒级启动，代理配置简单 |
| UI | Element Plus | 2.x | 表格/表单/弹窗/确认框齐全，企业级管理风格 |
| 路由 | Vue Router | 4.x | 页面组织 |
| HTTP | Axios | 1.x | 拦截器统一处理响应与异常 |
| 测试 | JUnit 5 / MockMvc / Playwright | — | 单元 + 切片 + 真实浏览器 |
| 构建 | Maven | 3.9+ | 课程要求 |

> 版本以仓库 `pom.xml` / `package.json` 实际锁定值为准；**不擅自升级大版本**（AGENTS 2.3）。本文档只给基线，不给具体小版本补丁号。

---

## 3. 运行形态与前后端交互

| 项 | 开发形态（默认） | 说明 |
| --- | --- | --- |
| 后端 | `mvn spring-boot:run` → `http://127.0.0.1:8080` | 连本地 MySQL；`server.address=127.0.0.1`，**只监听回环** |
| 前端 | `npm run dev` → `http://127.0.0.1:5173` | Vite 开发服务器；`server.host='127.0.0.1'` |
| 代理 | Vite `server.proxy`：`/api`、`/demo` → `http://127.0.0.1:8080` | 前端同源请求，**开发期无需 CORS** |
| 生产（可选） | 前端 `npm run build` → `dist`；后端 `mvn package` → jar | 不强制；若部署则前端静态文件交由 Nginx 或 Boot 静态目录，并保持仅本机访问 |

`vite.config.ts` 设计要点：

```ts
server: {
  host: '127.0.0.1',   // 仅本机
  port: 5173,
  proxy: {
    '/api':  { target: 'http://127.0.0.1:8080', changeOrigin: true },
    '/demo': { target: 'http://127.0.0.1:8080', changeOrigin: true }
  }
}
```

**为什么不配 CORS**：开发期走代理即同源；额外写 `CorsConfig` 只会增加无用代码与安全面。若将来直连 8080，再按需加。

**为什么不做 SPA 回退**：本课程采用前后端分离开发，Vite 自带 history 回退；不把前端打进 Boot 静态目录，就不需要后端 SPA 转发（详见 `requirements.md` Q-03）。

---

## 4. 后端目录结构与模块

### 4.1 目录结构（设计契约）

```text
backend/
├── pom.xml
├── .gitignore
└── src/
    ├── main/
    │   ├── java/com/express/addressbook/
    │   │   ├── ExpressAddressApplication.java      # @SpringBootApplication + @ServletComponentScan
    │   │   ├── common/
    │   │   │   ├── R.java                          # 统一响应 {code,message,data}
    │   │   │   ├── ResultCode.java                 # 0/40001/40002/40003/40401/40402/40501/50001/50002
    │   │   │   ├── PageResult.java                 # records/total/current/size/pages
    │   │   │   ├── BusinessException.java
    │   │   │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
    │   │   │   ├── LikeEscaper.java                # 关键词 LIKE 通配符转义（! % _）
    │   │   │   ├── Violation.java                  # 字段校验错误项（data.violations[]）
    │   │   │   └── LogMasker.java                  # 手机号/地址脱敏
    │   │   ├── config/
    │   │   │   ├── MybatisPlusConfig.java          # 分页插件
    │   │   │   └── JacksonConfig.java              # Long ID→String；long 计数保持数字
    │   │   ├── controller/
    │   │   │   ├── AddressController.java
    │   │   │   ├── HealthController.java            # GET /api/health（仅 Web）
    │   │   │   ├── DbHealthController.java          # GET /api/health/db（SELECT 1）
    │   │   │   └── demo/ServletMappingDemoController.java # /demo/spring/**（Spring MVC 对照组）
    │   │   ├── servlet/
    │   │   │   ├── ExactMappingServlet.java        # /demo/servlet/exact
    │   │   │   ├── PrefixMappingServlet.java       # /demo/servlet/*
    │   │   │   └── ExtensionMappingServlet.java    # *.do
    │   │   ├── service/
    │   │   │   └── AddressService.java              # 业务规则、事务、DTO→Entity
    │   │   ├── parser/
    │   │   │   ├── AddressParserService.java       # 唯一解析入口，无 Mapper 依赖
    │   │   │   ├── LineSplitter.java
    │   │   │   ├── PhoneExtractor.java
    │   │   │   ├── NameExtractor.java
    │   │   │   ├── AddressExtractor.java
    │   │   │   ├── ParserLimits.java
    │   │   │   ├── ParseInputGuard.java            # MS-02.1 API 上限前置校验
    │   │   │   ├── model/ (ParsedItem, LineError, ParseResult)
    │   │   │   └── strategy/AddressParseStrategy.java  # P2 扩展点
    │   │   ├── mapper/
    │   │   │   └── ExpressAddressMapper.java
    │   │   ├── entity/
    │   │   │   └── ExpressAddress.java
    │   │   ├── dto/                                # 入参
    │   │   │   ├── AddressParseRequest.java         # 解析请求
    │   │   │   ├── AddressSaveDTO.java
    │   │   │   ├── AddressBatchSaveDTO.java
    │   │   │   └── BatchDeleteDTO.java
    │   │   └── vo/                                 # 出参
    │   │       ├── AddressVO.java
    │   │       ├── PageResult.java
    │   │       ├── BatchSaveResult.java
    │   │       └── BatchDeleteResult.java
    │   │   └── validation/
    │   │       ├── CodePointLength.java            # 1～50 Unicode 码点校验
    │   │       └── CodePointLengthValidator.java
    │   └── resources/
    │       ├── application.yml
    │       ├── application-local.example.yml       # 提交示例，真实 application-local.yml 被 .gitignore
    │       ├── db/
    │       │   ├── schema.sql
    │       │   ├── admin-init.sql
    │       │   └── data.sql
    │       └── mapper/ExpressAddressMapper.xml      # 关键词分页自定义 SQL
    └── test/java/com/express/addressbook/
        ├── parser/AddressParserServiceTest.java     # 纯单元，无 Spring
        ├── common/LikeEscaperTest.java              # 转义单元测试（% _ ! \ 中文）
        ├── dto/AddressSaveDTOTest.java              # 生产 DTO 码点/长度
        ├── controller/AddressControllerTest.java    # @WebMvcTest（解析 API）
        ├── integration/AddressParseIntegrationTest.java # 真实 Parser + HTTP
        └── integration/AddressApiMysqlTest.java     # 真 MySQL，独立测试库
```

### 4.2 模块划分（与需求对应）

| 模块 | 职责 | 关键类 | 需求 |
| --- | --- | --- | --- |
| M1 解析模块 | 多行文本 → 结构化条目（无 IO） | `AddressParserService` 及 4 个提取器 | FR-02、FR-03 |
| M2 地址管理模块 | 增删改查、批量、分页搜索、事务 | `AddressController`、`AddressService`、`AddressMapper` | FR-05、FR-07～FR-13 |
| M3 公共支撑模块 | 统一响应、异常、校验、LIKE 转义、脱敏、配置 | `R`、`ResultCode`、`GlobalExceptionHandler`、`LikeEscaper`、`Violation`、`LogMasker` | FR-18～FR-20 |
| M4 前端录入模块 | 文本域、识别、可编辑预览、确认保存 | `ImportView`、`ParseResultTable` | FR-01～FR-06 |
| M5 前端管理模块 | 列表、搜索、分页、新建/编辑、删除/批删 | `AddressListView`、`AddressFormDialog` | FR-07～FR-14 |
| M6 Web 实践模块 | 2.1～2.19 演示页 | `WebBasicsView` + `components/basics/*` | FR-15、FR-16 |
| M7 Servlet 演示模块 | 2.20 容器映射 vs Spring 路由 | `servlet/*` + `ServletDemoView` | FR-17 |
| M8 测试与论文支撑 | 用例、截图、文档 | `test-plan.md`、`docs/screenshots/` | CR-07～CR-13 |

模块依赖（单向，禁止反向）：

```text
M4/M5 → M1/M2 → M3
M6/M7 → （独立，不依赖地址业务）
```

> M6/M7 故意与业务模块解耦：Web 实践页与 Servlet 演示即使业务表空着也能演示，降低答辩风险。

---

## 5. 前端目录结构与路由

### 5.1 目录结构

```text
frontend/
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── .env.development                # VITE_API_BASE=/api（可选）
└── src/
    ├── main.ts
    ├── App.vue
    ├── api/
    │   ├── request.ts              # Axios 实例 + 拦截器 + ApiError
    │   ├── address.ts              # 8 个地址接口
    │   └── demo.ts                 # /demo/** 演示请求
    ├── layout/
    │   └── AppLayout.vue           # 顶栏 + 侧栏导航 + 主区域
    ├── router/
    │   └── index.ts
    ├── components/
    │   ├── common/
    │   │   ├── PageHeader.vue
    │   │   ├── EmptyState.vue
    │   │   └── CodeBlock.vue       # 展示"关键代码片段"（论文素材）
    │   ├── address/
    │   │   ├── ParsePanel.vue      # 文本域 + 识别按钮 + 示例填充
    │   │   ├── ParseResultTable.vue# 可编辑预览表（行号/状态/错误）
    │   │   ├── AddressFormDialog.vue
    │   │   └── AddressSearchBar.vue
    │   └── basics/
    │       ├── DemoSection.vue     # 统一卡片：标题/目标/演示/观察/代码
    │       ├── DisplayTypeDemo.vue # 2.1～2.3
    │       ├── FlexGridDemo.vue    # 2.4～2.5
    │       ├── BoxModelDemo.vue    # 2.6～2.8
    │       ├── NormalFlowDemo.vue  # 2.9
    │       ├── PositioningDemo.vue # 2.10～2.12
    │       ├── FixedStickyDemo.vue # 2.13～2.15
    │       ├── OffsetConflictDemo.vue # 2.16～2.17
    │       └── LayoutPrinciples.vue# 2.19
    ├── views/
    │   ├── ImportView.vue          # /import
    │   ├── AddressListView.vue     # /addresses
    │   ├── WebBasicsView.vue       # /web-basics
    │   ├── basics/WireframeView.vue# /web-basics/wireframe（2.18）
    │   ├── ServletDemoView.vue     # /servlet-demo
    │   └── NotFoundView.vue
    ├── styles/
    │   ├── index.scss              # 全局变量/重置/Element Plus 覆盖
    │   └── basics.scss             # 实践页专用（盒模型/定位演示）
    ├── types/
    │   ├── address.ts
    │   └── demo.ts
    └── utils/
        └── error.ts                # 后端错误 → 表单错误/toast
```

> `utils/` 是对 AGENTS 示例目录的补充（示例未列全）；不移动、不重命名已有文件。

### 5.2 路由表

| 路径 | 视图 | 说明 |
| --- | --- | --- |
| `/` | redirect | 重定向到 `/import` |
| `/import` | `ImportView` | 智能录入（对应附录5 图5 的功能页，是本系统首页） |
| `/addresses` | `AddressListView` | 地址簿列表、搜索、分页、新建/编辑/删除/批删 |
| `/web-basics` | `WebBasicsView` | 2.1～2.19 实践演示（锚点定位） |
| `/web-basics/wireframe` | `WireframeView` | 2.18 线框风格仿制（独立整页，避免受父容器宽度影响） |
| `/servlet-demo` | `ServletDemoView` | 2.20 Servlet URI 映射演示 |
| `/:pathMatch(.*)*` | `NotFoundView` | 404 |

导航结构（`AppLayout`）：`智能录入 | 地址簿 | Web 基础实践 | Servlet 映射演示`。无登录、无用户菜单、无权限控制。

### 5.3 前端状态与数据流

```text
用户粘贴文本
   → ParsePanel 持有 rawText（本地 ref）
   → 点击识别 → api.parseAddressText(rawText)
   → ParseResultVO → 转换为 ParsedRow[]（rowKey、原始值 original、isEdited=false）
   → ParseResultTable 渲染（el-table + el-input 单元格；姓名输入按 Unicode 码点校验）
   → 用户修改 → 与 original 比较派生 isEdited（恢复原值则为 false）
   → 勾选/删除行（全部在本地）
   → 点击保存 → 本地校验 → 保存确认弹窗（有效 N / 修改 M / 异常 K + 异常行号原因）
   → 用户确认 → toPayload(row) 只投影业务字段 → api.batchSaveAddresses(items)
   → 成功：清空预览 + toast + 可选跳转列表
```

设计要点：

1. **解析结果只存在内存**，不写 `localStorage`（避免个人信息留存）。
2. 预览表用 `rowKey = lineNo + index`，避免编辑时行错位。
3. 表格中"状态"列实时反映当前值是否合法（前端校验与后端规则一致，但以后端为准）。
4. **`isEdited` 是前端派生状态**：`ParsedRow` 保存 `original`（解析时的姓名/电话/地址快照），任一字段与 `original` 不同则 `isEdited=true`，恢复原值后自动回到 `false`；仅用于展示与确认统计（F-03）。
5. **保存确认弹窗**必须显示：有效记录数、人工修改数（按 `isEdited` 统计）、异常记录数、将被跳过的异常行号与原因；用户确认后才发请求（F-03）。
6. **提交投影**：`toPayload(row)` 只返回 `receiverName/phone/fullAddress/lineNo`；`isEdited`、`rowKey`、`original`、`rawLine`、`status`、`errors` 均不得进入 `POST /batch` 请求体。异常记录不允许进入请求；若强行提交，后端 400 整批拒绝（BR-18）。
7. 保存失败时保留预览内容，滚动定位到报错的 `lineNo`；保存按钮在同一请求在途期间禁用，防重复提交。
8. **批量删除（F-04）**：只允许勾选当前页；翻页时清空已选；确认框显示待删除数量；未选中时按钮禁用。

---

## 6. Web 基础实践页面设计（对应 2.1～2.19）

### 6.1 页面总体设计

- 路由：`/web-basics`；左侧固定目录（**自绘粘性导航**，不依赖 Element Plus 的 Anchor 组件，避免版本差异），右侧按小节纵向排列。
- 每个小节统一用 `DemoSection` 卡片，内部固定四块：
  1. **实践目标**（一句话说清"要观察什么"）
  2. **演示区**（真实可交互的 HTML/CSS，不是图片）
  3. **观察结论**（可直接改写进论文的 2.x.1）
  4. **关键代码片段**（`CodeBlock` 展示真实使用的 CSS/HTML，论文 2.x.2 直接引用）
- 页面下方注明"截图存放位置与命名"（`docs/screenshots/2-xx-*.png`）。
- 所有演示使用独立命名空间 class（`.wb-*`），样式集中在 `styles/basics.scss`，避免污染业务页面。
- 演示区高度固定（约 240～320px），超出用滚动容器，保证布局类演示不撑破页面。

### 6.2 小节与演示设计（19 节全覆盖）

| 小节 | 演示要点（必须真实可见的现象） | 组件 | 截图 |
| --- | --- | --- | --- |
| 2.1 行内显示类型 | 段落内 `<span>` 设 `width/height` 无效；上下 `margin` 不生效；上下 `padding`/`border` 与相邻行重叠；左右 `padding/margin/border` 推开文字 | `DisplayTypeDemo` | `2-01-inline.png` |
| 2.2 区块显示类型 | `<div>` 独占一行、默认宽度撑满父容器；`width` 生效；`margin: 0 auto` 水平居中；相邻块上下 `margin` 发生折叠 | `DisplayTypeDemo` | `2-02-block.png` |
| 2.3 行内区块显示类型 | `inline-block` 的 `width/height` 生效且不换行；元素间空白产生约 4px 间隙；`vertical-align` 改变基线对齐 | `DisplayTypeDemo` | `2-03-inline-block.png` |
| 2.4 弹性显示类型 | `display:flex` 主轴/交叉轴；`justify-content` 五种取值对比；`flex:1` 等分；`gap` 与 `margin` 的差别 | `FlexGridDemo` | `2-04-flex.png` |
| 2.5 网格显示类型 | `display:grid` + `grid-template-columns: repeat(3, 1fr)`；`grid-column: span 2` 合并；`gap`；与 Flex 的"一维 vs 二维"对比 | `FlexGridDemo` | `2-05-grid.png` |
| 2.6 盒模型各部分 | 一个盒子四层着色（content/padding/border/margin），四边取不同值，标注总占位尺寸计算过程 | `BoxModelDemo` | `2-06-box-parts.png` |
| 2.7 标准盒模型 | `box-sizing: content-box`：`width` 只算内容；总宽 = width + padding + border | `BoxModelDemo` | `2-07-content-box.png` |
| 2.8 替代盒模型 | 同一 `width:200px` 下 `border-box` 的总宽仍为 200，内容区被压缩；两盒并排对照 | `BoxModelDemo` | `2-08-border-box.png` |
| 2.9 常规流布局（静态定位） | 块级纵向堆叠、行内横向排列；`position: static` 下 `top/left` 完全无效；相邻块 `margin` 折叠取大值 | `NormalFlowDemo` | `2-09-normal-flow.png` |
| 2.10 相对定位 | `position: relative` + `top/left` 偏移，原位置仍占位（后面的元素不动），偏移参照自身原位置 | `PositioningDemo` | `2-10-relative.png` |
| 2.11 绝对定位 | `position: absolute` 脱离常规流，后续元素上移填补其原空间；参照最近定位祖先；无定位祖先时参照初始包含块 | `PositioningDemo` | `2-11-absolute.png` |
| 2.12 改变绝对定位的定位上下文 | 同一绝对定位子元素，切换父级 `position: static` ↔ `relative`，子元素位置明显变化；再加 `padding` 验证参照的是 padding box | `PositioningDemo` | `2-12-absolute-context.png` |
| 2.13 固定定位 | `position: fixed` 相对视口，滚动演示容器时位置不变；补充"祖先有 `transform` 时 fixed 会退化为相对该祖先"的坑 | `FixedStickyDemo` | `2-13-fixed.png` |
| 2.14 粘性定位 | `position: sticky; top: 0` 在滚动容器内吸附；未到达阈值前与普通流一致 | `FixedStickyDemo` | `2-14-sticky.png` |
| 2.15 粘性约束矩形 | sticky 元素只在父容器（约束矩形）内生效：父容器高度不足时被"推走"；演示同一 sticky 在不同高度父容器中的差异 | `FixedStickyDemo` | `2-15-sticky-rect.png` |
| 2.16 定位属性上下左右 | 分别只用 `top` / `right` / `bottom` / `left`；四项同时设置且 `width:auto` 时的拉伸；`static` 下全部无效 | `OffsetConflictDemo` | `2-16-offsets.png` |
| 2.17 定位属性之间的覆盖影响 | 绝对定位同时设 `left+right`：`width:auto` 拉伸 / `width` 固定时 `left` 生效、`right` 被忽略；`top+bottom` 同理；相对定位同时 `left+right` 时 `left` 生效；`margin:auto` 居中 | `OffsetConflictDemo` | `2-17-offset-override.png` |
| 2.18 使用线框风格仿制XXX网页的布局 | **项目负责人批准的替代实现**：不再仿制外部网页，改为用低保真线框展示**快递通自身**的真实页面布局：系统框架、智能录入页、地址簿页、导航关系；灰块/细边框/文字标注，无外部 Logo/Banner/图片/字体 | `WireframeView` | `2-18-wireframe-plan.png`、`2-18-implemented.png` |
| 2.19 布局原则思考 | 容器与区域划分、栅格与间距系统、盒模型统一（全局 `border-box`）、避免用绝对定位做主布局、层级 `z-index` 管理、可维护性（命名/组件化）、响应式取舍；逐条对照本系统真实布局 | `LayoutPrinciples` | `2-19-principles.png` |

### 6.3 2.18 原创系统线框图实现方案

> **范围调整（2026-11-14，项目负责人批准）**：课程任务书原文标题仍为“使用线框风格仿制XXX网页的布局”，附录4 给出外部学校首页参考图。项目最终**不仿制外部网页**，改用本系统自身的真实页面作为设计对象；不得在论文/验收中宣称完全按原文完成了外部网页仿制。详见 `docs/review/ms-06-scope-adjustment.md`。

| 项 | 方案 |
| --- | --- |
| 设计对象 | 本系统真实页面：`AppLayout`、`ImportView`、`AddressListView`、导航关系；不引入外部网页内容 |
| 布局骨架 | 线框画板：A. 系统框架（顶部栏/左侧导航/主内容/页面标题与操作区）；B. 智能录入页（文本输入/解析操作/预览/确认保存）；C. 地址簿页（搜索/列表/操作/分页）；D. 导航关系（智能录入/地址簿/Web 基础实践/Servlet 映射演示，后两者标注规划态） |
| 线框风格 | 灰阶低保真：`#f1f5f9`/`#e2e8f0` 占位块、细边框、文字区域名；无真实图片、无 Logo、无业务数据；不使用外部字体 |
| 与实现的关系 | `2-18-wireframe-plan.png` 为线框设计板；`2-18-implemented.png` 为 `/web-basics/wireframe` 的真实浏览器截图，不用代码绘制图冒充 |
| 证据链 | 仅两张：`2-18-wireframe-plan.png`（原创线框设计）+ `2-18-implemented.png`（真实浏览器运行）；**取消** `2-18-original.png` |
| 不虚构 | 导航中的 Web 基础实践/Servlet 映射演示在 MS-06/07 实现前必须标注“规划/待实现”，不得画成已可用业务功能 |

### 6.4 截图与论文的衔接

- 所有截图统一存 `docs/screenshots/`，命名 `2-xx-名称.png`，与上表一致。
- **2.18 只保留 `2-18-wireframe-plan.png` 与 `2-18-implemented.png`；取消 `2-18-original.png`，不采集外部网站截图。**
- `CodeBlock` 展示的代码必须与仓库中的真实源码一致（复制自源码，不手写伪代码）。
- 论文 2.x.1 的"观察结论"文字可从本页"观察结论"块直接提炼，但必须以**本机实际运行现象**为准；如浏览器差异导致现象不同，论文需如实说明浏览器与版本。

---

## 7. Servlet与请求URI的映射规则演示方案（对应附录3 的 2.20 小节）

> 本节对应任务书附录3 的 **2.20 Servlet与请求URI的映射规则**；2.21 快递地址簿录入程序设计由本文 §1～§5、§9～§11 与其余五份文档共同覆盖。

### 7.1 两套映射必须分清

| 维度 | Servlet 容器映射 | Spring MVC 路由匹配 |
| --- | --- | --- |
| 发生位置 | Tomcat（Web 容器）内部，请求进入 Servlet 之前 | DispatcherServlet 内部，请求已进入 Spring |
| 配置方式 | `@WebServlet` / `web.xml` / `ServletRegistrationBean` | `@RequestMapping/@GetMapping` |
| 匹配单位 | Servlet 的 `url-pattern` | Handler 的路径表达式 + HTTP 方法 + 参数 + 头 |
| 匹配规则 | 精确 → 最长路径前缀 → 扩展名 → 默认 `/` | PathPattern 匹配，支持路径变量、通配、正则；受 `consumes/produces/params` 约束 |
| 是否看 HTTP 方法 | **不看**（同一映射响应所有方法） | **看**（方法不匹配返回 405） |
| 路径变量 | 不支持 | 支持 `/api/addresses/{id}` |
| 冲突表现 | 同一 exact 映射重复注册 → 启动失败 | 同一路径+方法重复映射 → 启动失败（Ambiguous mapping） |
| 本项目实例 | `/demo/servlet/exact`、`/demo/servlet/*`、`*.do` | `/api/addresses/parse`、`/demo/spring/echo/{name}` |

> 一句话总结（论文 1.4 可直接用）：**容器先按 url-pattern 把请求交给某个 Servlet；Spring MVC 的 `@GetMapping` 只是 DispatcherServlet 收到请求后的二次分发，两者不是同一层规则。**

### 7.2 演示用 Servlet（3 个）

| 类 | 映射 | 演示的规则 | 关键响应字段 |
| --- | --- | --- | --- |
| `ExactMappingServlet` | `/demo/servlet/exact` | 精确匹配优先级最高 | `servletPath=/demo/servlet/exact`，`pathInfo=null` |
| `PrefixMappingServlet` | `/demo/servlet/*` | 最长路径前缀匹配 | `servletPath=/demo/servlet`，`pathInfo=/prefix/a/b` |
| `ExtensionMappingServlet` | `*.do` | 扩展名匹配 | `servletPath=/demo/legacy/report.do`，`pathInfo=null` |

注册方式：主类加 `@ServletComponentScan(basePackages = "com.express.addressbook.servlet")`，三个类用 `@WebServlet(name = "...", urlPatterns = "...")`。

统一响应（三个 Servlet 返回同构 JSON，便于前端表格渲染）：

```json
{
  "handler": "PrefixMappingServlet",
  "mappingType": "PATH_PREFIX",
  "mapping": "/demo/servlet/*",
  "requestUri": "/demo/servlet/prefix/a/b",
  "contextPath": "",
  "servletPath": "/demo/servlet",
  "pathInfo": "/prefix/a/b",
  "method": "GET",
  "query": "x=1"
}
```

实现要点（写进代码规范）：
1. `resp.setContentType("application/json;charset=UTF-8")`，否则 Tomcat 默认 ISO-8859-1 会乱码。
2. 用 `request.getServletPath()` / `getPathInfo()` / `getRequestURI()` 区分三种匹配。
3. 不输出任何业务数据，只回显映射信息（无副作用、无数据库依赖）。
4. 三个 Servlet 都同时实现 `doGet` 与 `doPost`（复用同一个回显方法），用于证明"容器映射与 HTTP 方法无关"。
5. Spring Boot 3 使用 Jakarta EE 命名空间，Servlet 需继承 `jakarta.servlet.http.HttpServlet`，不要误用 `javax.servlet`。

### 7.3 Spring MVC 对照组

`ServletMappingDemoController`（`@RestController`）：

| 映射 | 演示点 |
| --- | --- |
| `@GetMapping("/demo/spring/info")` | 与 Servlet 并列，证明"同一条 URL 前缀，走的是不同机制" |
| `@GetMapping("/demo/spring/echo/{name}")` | 路径变量 —— 容器级映射做不到 |
| `@RequestMapping("/demo/spring/**")` | 通配匹配由 Spring 自己解析 |
| `@GetMapping("/demo/spring/method-only")` | 仅 GET 映射，用 POST 访问 → 405，证明 Spring 看 HTTP 方法 |

### 7.4 演示矩阵（页面上逐条真实请求）

| # | 请求 | 预期命中 | 观察点 |
| --- | --- | --- | --- |
| 1 | `GET /demo/servlet/exact` | `ExactMappingServlet` | 精确匹配优先于前缀 |
| 2 | `GET /demo/servlet/exact/extra` | `PrefixMappingServlet`（`pathInfo=/exact/extra`） | 多一层路径就落到前缀映射 |
| 3 | `GET /demo/servlet/prefix/a/b` | `PrefixMappingServlet`（`pathInfo=/prefix/a/b`） | 前缀映射捕获整段剩余路径 |
| 4 | `GET /demo/legacy/report.do` | `ExtensionMappingServlet` | 扩展名匹配与路径无关 |
| 5 | `GET /demo/legacy/report.json` | 默认映射 → DispatcherServlet → 404 | 只注册了 `*.do`，`.json` 不匹配 |
| 6 | `POST /demo/servlet/exact` | `ExactMappingServlet` | **容器映射不看 HTTP 方法** |
| 7 | `GET /demo/spring/info` | Spring MVC Controller | 同一容器内两套机制并存 |
| 8 | `GET /demo/spring/echo/张三` | Spring MVC Controller | 路径变量只有 Spring 支持 |
| 9 | `POST /demo/spring/method-only` | 405 | **Spring 路由看 HTTP 方法** |
| 10 | `GET /api/addresses?page=1&size=10` | Spring MVC Controller | 真实业务接口也走 DispatcherServlet |

> 页面上的"预期命中"与"实际命中"必须分两列显示；实际值来自 `fetch` 的真实响应，不允许前端写死结论。

### 7.5 页面设计（`/servlet-demo`）

1. 顶部说明卡：一张"容器映射 vs Spring 路由"对照表（同 §7.1）。
2. 中部演示矩阵：每行一个"发起请求"按钮，右侧显示状态码、耗时、响应 JSON（`el-collapse` 折叠）。
3. 底部"规则顺序"图：精确 → 前缀 → 扩展名 → 默认 `/`，并高亮当前请求命中的那一层。
4. 附加说明：`@ServletComponentScan` 的作用、`spring.mvc.servlet.path` 默认值 `/`、为什么 `/api/**` 从未进入自定义 Servlet。

### 7.6 风险与规避

| 风险 | 规避 |
| --- | --- |
| 忘记 `@ServletComponentScan`，Servlet 不生效 | 启动日志确认 3 个 Servlet 已注册；演示页第 1 条请求即验证 |
| `*.do` 与 Spring 静态资源/其他映射冲突 | 演示前缀独立为 `/demo/legacy/`，不碰 `/api` |
| Tomcat 中文乱码 | 显式设置 `Content-Type` 与 `charset=UTF-8` |
| `pathInfo` 为 `null` 时前端显示异常 | 前端统一 `?? 'null'` 展示，避免 `undefined` |
| 评审老师直接访问 `/demo/servlet/exact` 看到 JSON 觉得"没页面" | 演示页明确写"该端点是给演示页调用的机器可读响应"，并提供按钮触发 |

---

## 8. 请求链路（MVC 实践说明，对应论文 1.3）

以 `POST /api/addresses/parse` 为例：

```text
① 浏览器 fetch/axios POST /api/addresses/parse  {rawText}
② Tomcat 连接器解析 HTTP → 按 url-pattern 匹配，"/" 默认映射交给 DispatcherServlet
③ DispatcherServlet 查询 HandlerMapping，命中 AddressController#parse
④ HandlerAdapter 用 Jackson 把 JSON 反序列化为 ParseRequest，执行 @Valid 校验
⑤ Controller 调 AddressParserService.parse(rawText)（纯内存计算）
⑥ Controller 用 R.ok(data) 包装结果
⑦ HttpMessageConverter 序列化为 JSON 返回
⑧ 前端 request.ts 拦截器判断 code===0 → resolve(data) → 表格渲染
```

以 `POST /api/addresses/batch` 为例，多了事务边界：

```text
Controller → AddressService.batchSave(items)  [@Transactional(rollbackFor = Exception.class)]
   → 嵌套 Bean Validation（List<@NotNull @Valid AddressSaveDTO>）
        · 普通字段错误 → 400/40001 + violations
        · items 数量 >200（@Size 违规）→ 400/40003 + violations[field=items]
   → Service 复核 items 非空/≤200/元素非 null（防绕过 Controller）
   → AddressMapper.insert × N
   → 提交；任一条异常 → 全部回滚
```

MVC 三要素在本项目中的落点：

| MVC | 后端 | 前端 | 说明 |
| --- | --- | --- | --- |
| Model | `ExpressAddress`、DTO、VO、Service 中的业务状态 | `types/address.ts` 类型、组件内状态 | 前后端分离下 Model 分居两侧，靠 JSON 契约对齐 |
| View | JSON（`R<T>`） | Vue 组件 + Element Plus | 后端不再渲染页面，View 由前端承担 |
| Controller | `AddressController`（薄） | 路由 + 页面事件处理 | 后端只做请求编排，不写业务 |

> 论文 1.3 要写清"分离式 MVC"与传统 JSP MVC 的差异，并指出本项目仍严格保留 Controller/Service/Mapper 的分层。

---

## 9. 解析与保存分离的架构保障

| 保障手段 | 具体做法 |
| --- | --- |
| 接口分离 | `POST /parse` 只读；`POST /batch`、`POST /` 才写 |
| 依赖隔离 | `parser` 包不 import 任何 Mapper/JdbcTemplate/HTTP 客户端 |
| 事务位置 | 只有 `AddressServiceImpl` 的写方法带 `@Transactional`，解析方法不带 |
| 二次校验 | 保存服务不信任已解析结果，重新执行 Bean Validation |
| 规范化归属 | 电话规范化只在 `parser` 的 `PhoneExtractor` 实现一次，保存服务复用同一工具类（不复制规则）；姓名/地址只 trim |
| 无效行拦截 | 解析器只标记 `INVALID`；保存服务收到非法数据一律 400 整批拒绝，不静默丢弃（BR-18） |
| 代码审查项 | 检查 `AddressController.parse` 调用链中不存在 `insert/update` |
| 自动化断言 | 单测断言"解析服务无 Mapper 依赖"（反射检查字段类型）+ 接口测试断言"解析后总数不变" |

---

## 10. 配置、密钥与日志

### 10.1 配置分层

| 文件 | 是否入库 | 内容 |
| --- | --- | --- |
| `application.yml` | ✅ | 端口、监听地址、数据源 URL、MyBatis-Plus 全局配置、Jackson、日志级别；口令用 `${DB_PASSWORD}` 占位 |
| `application-local.yml` | ❌（`.gitignore`） | 本机数据库账号口令 |
| `application-local.example.yml` | ✅ | root 模板（无真实口令），口令通过环境变量或本地文件提供 |
| `.env` / `.env.local` | ❌（`.gitignore`） | 前端/脚本本地变量 |
| `.env.example` | ✅ | 变量名示例，不含真实值 |

```yaml
# application.yml（示意）
server:
  address: 127.0.0.1            # F-02：仅本机回环，禁止监听 0.0.0.0
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/express_address_book?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}     # 本机课程演示默认 root；口令无默认值
    password: ${DB_PASSWORD}          # 必填，仅环境变量/application-local.yml
  config:
    import: optional:file:./application-local.yml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
      id-type: auto
  configuration:
    map-underscore-to-camel-case: true
```

`application-local.example.yml`（可提交的模板，不含真实口令）：

```yaml
# 复制为 application-local.yml 后填写；application-local.yml 已被 .gitignore 忽略
spring:
  datasource:
    username: root
    password: CHANGE_ME_LOCAL_ONLY
```

> 当前默认使用 root（本机课程演示）；`admin-init.sql` 的 `express_app` 仅作为可选最小权限方案，不执行不影响 root 运行。

### 10.2 日志规则

| 规则 | 做法 |
| --- | --- |
| 不记录完整电话/地址 | `LogMasker.maskPhone()` → `136****0000`；`maskAddress()` → 前 6 字 + `...` |
| 不记录请求体 | 拦截器/Controller 不打 `log.info(body)` |
| SQL 日志 | 默认关闭；本地排错临时开启后必须关闭，且截图不得含参数字面量 |
| 异常日志 | 服务端 `log.error` 带堆栈；返回体只给通用文案；`violations[].message` 不回显用户原始值 |

### 10.3 访问边界（F-02）

1. 后端 `server.address=127.0.0.1`，前端 `server.host='127.0.0.1'`，只允许本机访问。
2. README、论文、答辩演示脚本均需写明："本系统无身份认证，仅供本机课程演示，不得暴露到公共网络或局域网。"
3. **仅监听 localhost 是运行期缓解，不等于实现了身份认证**；不得在任何文档或论文中宣称"已通过 localhost 实现安全认证"。
4. **数据库账号（本机演示）**：应用使用 root 连接 `127.0.0.1:3306/express_address_book`；口令只放环境变量或 `application-local.yml`；不得创建 `root@'%'`、不得开放远程登录、不得用于公共网络部署。`admin-init.sql` 的 `express_app` 仅作为可选最小权限方案。
5. 验证方式：`netstat -ano | findstr :8080` 应只见 `127.0.0.1:8080`（或 `[::1]:8080`），不得出现 `0.0.0.0:8080`；MySQL 建议 `bind-address=127.0.0.1`。

---

## 11. 事务与一致性

| 操作 | 事务 | 说明 |
| --- | --- | --- |
| 批量保存 | `@Transactional(rollbackFor = Exception.class)` | 先嵌套校验；普通字段错误 40001，`items`>200 为 40003；全部合法才提交，一条不写 |
| 批量删除 | 同上 | `ids` 去重后一次 `deleteBatchIds` |
| 单条增删改 | 单语句，隐式事务 | 无需显式注解 |
| 解析 | 无事务 | 不写库 |

注意点：
1. `@Transactional` 必须落在 `public` 方法上，且由**外部**调用（避免同类自调用失效）。
2. 事务方法内不要做耗时操作（解析已在前一个请求完成，天然分离）。
3. 单次批量保存 ≤200 条，保证单事务体量可控。
4. 批量保存不得"跳过非法项继续插"，否则违反 BR-18。

---

## 12. 目录与代码约束（架构不变量）

1. Controller 中不出现 `LambdaQueryWrapper`、`Mapper`、`@Transactional`。
2. `parser` 包不出现持久化/网络/文件 API。
3. 所有 `/api/**` 接口返回 `R<T>`；异常统一由 `GlobalExceptionHandler` 转换（含 404/405，见 `api-design.md` §7）。
4. `id` 一律字符串进前端；时间格式统一 `yyyy-MM-dd HH:mm:ss`。
5. 前端所有 HTTP 调用只出现在 `src/api/`；页面不直接 `import axios`。
6. 业务页面不写行内样式做布局，统一样式文件与 class 命名（`wb-` 前缀仅用于实践页）。
7. `/demo/**` 与 `/api/**` 严格分离，演示代码不进入业务 Service。
8. 任何截图、日志、文档不得包含真实手机号与详细住址。
9. 关键词查询必须先经 `LikeEscaper`（`!`→`!!`、`%`→`!%`、`_`→`!_`），SQL 使用 `ESCAPE '!'`，同一关键词只转义一次。
10. 字段校验错误只使用 `data.violations[]`；解析行错误只使用 `items[].errors[]`，禁止同名混用。
11. `isEdited` 只存在于前端状态；`toPayload()` 只投影 `receiverName/phone/fullAddress/lineNo`，不进入请求体、不进入数据库。
12. 默认只监听 `127.0.0.1`；不得为图方便改成 `0.0.0.0`。
13. 姓名长度统一 1～50 个 Unicode 码点：Parser `codePointCount`、Java `@CodePointLength(min=1,max=50)`、前端 `Array.from(value).length`、数据库 `VARCHAR(50)`；超 50 用 E111，1～50 内非法字符用 E106。**不要用 `String.length()`/HTML `maxlength` 作为唯一长度约束**。
14. 本机课程演示使用 root 连接 `127.0.0.1:3306/express_address_book`；口令只通过环境变量或 `application-local.yml` 提供，不得写入 Git；不得创建 `root@'%'` 或开放远程登录；`express_app` 为可选最小权限方案。
15. 批量保存数量：1～200；`items` 为空 → 40001，`items`>200 → 40003（`violations[].field=items`），普通字段错误 → 40001。

---

## 13. 部署与启动（README 将细化）

| 步骤 | 命令/操作 | 验证 |
| --- | --- | --- |
| 1 建库 | 本机 root 执行：`mysql -u root -p < backend/src/main/resources/db/schema.sql` | `SHOW TABLES` 有 `express_address` |
| 2 演示数据（可选） | 本机 root 执行：`mysql -u root -p express_address_book < .../data.sql` | `SELECT COUNT(*)` = 3 |
| 3 配置口令 | 复制 `application-local.example.yml` → `application-local.yml`，填写本机 root 口令；或使用 `DB_USERNAME`/`DB_PASSWORD` 环境变量 | 不入 Git |
| 4（可选）最小权限 | 需要时执行 `admin-init.sql` 创建 `express_app`；不执行则继续用 root | 非默认方案 |
| 5 启动后端 | `cd backend && mvn spring-boot:run` | `http://127.0.0.1:8080/api/addresses?page=1&size=10` 返回 `code:0`；`netstat -ano \| findstr :8080` 仅见 `127.0.0.1:8080` |
| 6 启动前端 | `cd frontend && npm install && npm run dev` | `http://127.0.0.1:5173` 打开录入页 |
| 7 联调 | 录入 → 识别 → 修改 → 确认 → 保存 → 列表 | 数据真正入库 |
| 8 测试 | `mvn test`、`npm run build`、Playwright | 见 `test-plan.md` |
| 9 复现说明 | 按 `README.md` 从零执行一次 | 与本节步骤一致 |

---

## 14. 明确不引入的技术及理由

| 技术 | 不引入理由 |
| --- | --- |
| Spring Security / JWT / Shiro | 无身份体系需求；引入即范围膨胀。运行期以"仅监听 127.0.0.1"缓解暴露风险，**这不等于认证** |
| Redis / 缓存 | 数据量小，无热点；一致性更简单 |
| 消息队列 | 无异步/削峰需求 |
| Elasticsearch | 万级数据 `LIKE` 足够；见 `database-design.md` §4 |
| Docker / K8s | 课程验收在本机，非必需；避免环境问题掩盖代码问题 |
| Testcontainers | 需要 Docker；集成测试改为本地 MySQL + `@Tag("integration")` |
| 代码生成器（MP Generator） | 单表手写更可控，避免生成冗余代码 |
| 前端状态库（Pinia） | 页面级状态足够；减少概念负担 |
| 微服务/网关 | 单体分层已满足全部需求 |
| 云端 AI 解析 | P2；默认关闭，需脱敏与显式授权（见 `parser-design.md` §10） |

---

## 15. 风险与应对

| 风险 | 影响 | 应对 |
| --- | --- | --- |
| 本机无 MySQL 8 或口令错误 | 后端起不来，演示中断 | 提前按 §13 走通；准备 `data.sql` 快速恢复 |
| 解析规则在真实文本上误判 | 交付出错 | 先跑 `test-plan.md` 的 TC-PARSE 全量；解析结果可人工修改 |
| Servlet 演示与 Spring 映射混淆讲不清 | 论文 1.4/2.20 失分 | 按 §7.1 对照表讲；页面用真实请求证明 |
| `position: sticky` 受父容器约束 | 演示"没粘住" | 固定滚动容器高度，先讲 2.15 约束矩形再演示 |
| `fixed` 遇 `transform` 祖先 | 现象与预期不符 | 演示区避免 `transform`；单独用文字说明该坑 |
| 大整数 id 前端丢精度 | 编辑/删除错行 | 统一字符串 id（已在 §12 立为不变量） |
| 论文截图与代码不一致 | 学术诚信问题 | 截图只从当前运行版本截取；改代码后重截 |
| 前端代理未生效 | 请求 404/返回 HTML | 检查 `vite.config.ts` 的 `/api`、`/demo` 两条代理 |
| 范围膨胀（注册登录、AI、导出） | 核心功能做不完 | 以 `requirements.md` §5.2 为界，一律记为 Backlog |
| 服务误监听 `0.0.0.0`，同网段可读/删全部联系人 | 隐私与误删事故 | 固定 `server.address=127.0.0.1`；MS-10 用 `netstat` 复核；论文声明无认证 |
| LIKE 关键词含 `%`/`_` 导致全表命中 | 搜索行为错误、误以为泄露 | `LikeEscaper` + `ESCAPE '!'`；TC-LIKE 与 TC-ADDR-046/047 覆盖 |

---

## 16. 跨文档一致性检查清单

设计阶段收口时逐项核对（本阶段已按此清单自检）：

- [x] 接口路径与 `api-design.md` §2 完全一致（8 个 + `/demo/**`）
- [x] 字段名与 `database-design.md` §8 映射表一致
- [x] 字段长度 50/20/255 在需求、数据库、API、前端三处一致
- [x] 解析规则编号 PR-01～PR-13 与 `parser-design.md` 一致
- [x] 行错误码 E102～E108、E110、E111 与 `parser-design.md` §5 一致（E109 已删除）
- [x] 页面路由与 `course-paper-outline.md` 的截图清单一致
- [x] 2.1～2.19 小节名称与任务书附录3 **逐字一致**
- [x] 2.20、2.21 与附录3 一致；2.22 明确不适用
- [x] 测试用例前缀（TC-PARSE/TC-LIKE/TC-ADDR/TC-API/TC-WEB/TC-SERVLET/TC-E2E/TC-SEC）与 `test-plan.md` 一致
- [x] 里程碑与 `development-roadmap.md` 一致
- [x] 关键词转义、`violations`、`isEdited`、本机监听、命名约定在八份文档中一致
- [x] 姓名 1～50 个 Unicode 码点、手机号 11 位/≤20、地址 ≤255、批量 1～200 在需求/数据库/API/Parser/前端/测试中一致
- [x] 40001（字段校验）与 40003（数量/长度超限）职责已分离；201 条映射 40003
- [x] 本机 root 账号与口令外置（环境变量/application-local.yml），未创建 `root@'%'`；`express_app` 为可选最小权限方案
- [x] `isEdited` 只在前端，`toPayload()` 仅提交业务字段；Java DTO 无该字段
- [x] `.gitignore` 与 `README.md` 已创建，且与本文 §10/§13 一致

---

## 17. 变更记录

| 版本 | 日期 | 变更 | 说明 |
| --- | --- | --- | --- |
| v1.0 | 2026-11-14 | 架构、模块、目录、Web 实践页、Servlet 演示方案定稿 | 第一阶段设计，未编写业务代码 |
| v1.1 | 2026-11-14 | P0 修订：LikeEscaper/Violation、保存确认与 isEdited、当前页批删、本机监听、嵌套校验、控制器包调整、application-local 示例文件更名、.gitignore/README | 未编写业务代码 |
| v1.1-final | 2026-11-14 | 第二轮审查遗留修复：姓名 1～50、`toPayload` 投影与 `original` 快照、201 条映射 40003、应用账号与初始化账号分离、配置示例去 root | 未编写业务代码 |
| v1.1-final-ms01 | 2026-11-14 | MS-01 后按开发环境调整为 root 本机连接（口令外置）；`express_app` 降级为可选方案 | 仅配置/文档，无业务代码 |
| v1.1-final-ms02.1 | 2026-11-14 | 解析 API、ParseInputGuard、CodePointLength、候选回退/包裹符/E108/英文地址/parse(null) 修复 | 95 个测试通过；无 CRUD |
| v1.1-final-ms03 | 2026-11-14 | Entity/Mapper XML/AddressService/CRUD/批量/分页搜索；独立 MySQL 测试库 | 137 个测试通过；前端未开发 |
| v1.1-final-ms03.1 | 2026-11-14 | 分页 JSON 类型、keyword 码点上限、page/size 字段映射；原始电话/全角姓名；API 事务回滚 | 146 个测试通过；MS-04 放行 |
| v1.1-final-ms06.1 | 2026-11-14 | 2.18 经项目负责人批准改为本系统原创线框；取消外部网页仿制/原网页截图；保留两张截图；补充 MS-06 实施依据 | 仅设计调整；未开发页面 |
