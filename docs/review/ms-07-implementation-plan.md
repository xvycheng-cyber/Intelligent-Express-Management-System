# MS-07 Servlet URI 映射演示实施计划

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 版本 | v1.0-plan |
| 依据 | `docs/architecture.md` §7、`requirements.md` FR-17、`ms-07-pre-audit-report.md`、`ms-07-scope-adjustment.md` |
| 状态 | **MS-07.2～MS-07.5 已实施；测试与截图完成，等待正式审查** |

---

## 1. 目标与边界

### 1.1 目标

实现课程 2.20 的真实演示：

- 三个真实 `@WebServlet`：精确、路径、扩展名；
- 一个真实 Spring MVC 对照 Controller；
- 映射优先级、404、405、默认 `/` 边界；
- `/servlet-demo` 页面逐条发起真实 HTTP 请求；
- 可自动化回归；
- 不依赖数据库、不改业务 API。

### 1.2 非目标

- 不实现登录/权限；
- 不修改地址业务逻辑、API 契约或表结构；
- 不新增 npm/Maven 依赖；
- 不把 Servlet 演示写成前端 Mock；
- 不修改 MS-06 的 2.1～2.19 内容。

---

## 2. 目标架构

```text
浏览器
  │
  ├─ GET /servlet-demo
  │      Vite SPA → ServletDemoView.vue
  │      fetch('/demo/...') → Vite proxy → Tomcat
  │
  └─ Tomcat (127.0.0.1:8080)
        ├─ /demo/servlet/exact      → ExactMappingDemoServlet
        ├─ /demo/servlet/*          → PathMappingDemoServlet
        ├─ *.smap                   → ExtensionMappingDemoServlet
        ├─ /demo/spring/**          → DispatcherServlet → ServletMappingDemoController
        ├─ /api/**                  → DispatcherServlet → AddressController
        └─ 其它                     → DispatcherServlet（默认 `/`）
```

注册：

```java
@ServletComponentScan(basePackages = "com.express.addressbook.servlet")
```

---

## 3. 目标文件清单

### 3.1 后端新增

```text
backend/src/main/java/com/express/addressbook/servlet/ExactMappingDemoServlet.java
backend/src/main/java/com/express/addressbook/servlet/PathMappingDemoServlet.java
backend/src/main/java/com/express/addressbook/servlet/ExtensionMappingDemoServlet.java
backend/src/main/java/com/express/addressbook/demo/ServletMappingDemoController.java
```

### 3.2 后端修改

```text
backend/src/main/java/com/express/addressbook/ExpressAddressApplication.java
  + @ServletComponentScan(basePackages = "com.express.addressbook.servlet")
```

### 3.3 前端新增

```text
frontend/src/views/ServletDemoView.vue
frontend/src/api/servletDemo.ts
frontend/src/styles/servlet-demo.scss        （可选，或沿用 App 样式体系）
```

### 3.4 前端修改

```text
frontend/src/router/index.ts                 + /servlet-demo
frontend/src/layout/AppLayout.vue            + activeMenu/pageStage + 导航入口
frontend/vite.config.ts                      + /demo proxy
frontend/src/main.ts                         + 引入 servlet-demo.scss（若独立）
```

### 3.5 测试/证据

```text
backend/src/test/java/com/express/addressbook/servlet/ServletMappingIntegrationTest.java
frontend/scripts/ms07-browser-check.mjs
docs/screenshots/2-20-servlet-mapping.png
docs/review/ms-07-2-report.md
docs/review/ms-07-3-report.md
docs/review/ms-07-4-report.md
docs/review/ms-07-final-report.md
```

---

## 4. 后端设计

### 4.1 Servlet 共同响应

三个 Servlet 返回同构 JSON：

```json
{
  "handler": "ExactMappingDemoServlet",
  "mappingType": "EXACT",
  "mapping": "/demo/servlet/exact",
  "requestUri": "/demo/servlet/exact",
  "contextPath": "",
  "servletPath": "/demo/servlet/exact",
  "pathInfo": null,
  "method": "GET",
  "query": ""
}
```

要求：

- `setContentType("application/json;charset=UTF-8")`；
- 使用 `jakarta.servlet.*`，不是 `javax`；
- 不访问 Service/Mapper/数据库；
- 不记录完整业务数据；
- `doGet` 写出 JSON；
- 未实现的 `doPost` 使用 `HttpServlet` 默认 405（不伪造 JSON 405）。

### 4.2 三个映射

| 类 | `@WebServlet` | 预期 |
| --- | --- | --- |
| `ExactMappingDemoServlet` | `/demo/servlet/exact` | exact 优先 |
| `PathMappingDemoServlet` | `/demo/servlet/*` | 最长路径前缀；servletPath=`/demo/servlet` |
| `ExtensionMappingDemoServlet` | `*.smap` | 扩展名；servletPath=完整路径 |

### 4.3 Spring MVC Controller

`ServletMappingDemoController`：

```java
@RestController
@RequestMapping("/demo/spring")
public class ServletMappingDemoController {
    @GetMapping("/info") ...
    @GetMapping("/echo/{name}") ...
    @GetMapping("/method-only") ...
    // 可选：@GetMapping("/wildcard/{*path}")
}
```

返回字段：

```text
handler=ServletMappingDemoController
mappingType=SPRING_MVC
mapping=...
requestUri
method
query
```

不调用业务 Service；不读写数据库。

### 4.4 方法/404 边界

- 自定义 Servlet POST → 405（容器/`HttpServlet`，非 Spring 全局异常）；
- Spring `POST /demo/spring/method-only` → 405（Spring 全局异常，body 为 `R`）；
- `/demo/legacy/report.json` → 默认 `/` → DispatcherServlet → 404（Spring 全局异常，body 为 `R`）；
- 不新增 `@WebServlet("/")`；
- 不修改 `GlobalExceptionHandler` 的 404/405 语义。

---

## 5. 前端设计

### 5.1 路由与导航

- `/servlet-demo` 添加 `ServletDemoView`；
- AppLayout `activeMenu` 增加 `servlet-demo`；
- “Servlet 映射演示”从 disabled span 改为 `RouterLink`，去掉“待实现”；
- `pageStage` 增加 `MS-07 · Servlet 映射演示`；
- 导航顺序：批量录入 / 地址簿 / Web 基础实践 / Servlet 映射演示。

### 5.2 Vite

```ts
'/demo': {
  target: 'http://127.0.0.1:8080',
  changeOrigin: true
}
```

### 5.3 真实请求

`servletDemo.ts` 使用原生 `fetch`，不要用现有 Axios `/api` 包装：

```ts
export async function runServletRequest(method: string, url: string) {
  const response = await fetch(url, { method, headers: { Accept: 'application/json' } })
  const text = await response.text()
  return {
    status: response.status,
    statusText: response.statusText,
    contentType: response.headers.get('content-type'),
    allow: response.headers.get('allow'),
    body: text
  }
}
```

页面必须：

- 每行真实按钮；
- 显示预期命中 / 实际命中；
- 显示 status、content-type、allow、body；
- 405/404 也展示，不因非 2xx 抛异常。
- **不得使用页面内硬编码的“实际命中”**；实际 handler 从响应体解析。

### 5.4 页面结构

```text
标题：Servlet URI 映射演示
说明：课程 2.20；容器映射 vs Spring MVC
对照表：容器 url-pattern / Spring Handler
演示矩阵：请求、预期、实际、状态
默认映射边界卡：DispatcherServlet `/`、404 说明
方法边界卡：405 说明
响应详情：JSON/error body
```

---

## 6. 阶段拆分

### MS-07.1 前置审计与实施设计（本阶段）

| 项 | 内容 |
| --- | --- |
| 目标 | 审计现状、核对课程原文、明确设计/范围/测试 |
| 允许修改 | 仅新增 `docs/review/ms-07-*.md` 四份规划文档 |
| 禁止 | 业务代码、数据库、页面、导航、依赖、commit |
| 依赖 | 无 |
| 步骤 | 读取资料；检查 Git；检查 Spring/前端；核对 docx；写审计/范围/计划/追踪表 |
| 测试方式 | 文档路径/引用/源码静态核对；不执行修改 |
| 交付物 | 四份规划文档 |
| 验收 | 原文/设计/事实分清；TC-SERVLET 完整；阶段可执行 |
| 风险 | 课程原文只有标题，需明确不夸大 |
| 停止条件 | 文档完成即停止，等待批准 |

### MS-07.2 后端真实 Servlet 与 Spring MVC 对照

| 项 | 内容 |
| --- | --- |
| 目标 | 注册真实 Servlet，建立 Controller，HTTP 可命中 |
| 允许修改 | 启动类、`servlet/` 三类、`demo/ServletMappingDemoController` |
| 禁止 | 前端、业务 API/DB、测试脚本、截图、导航 |
| 依赖 | MS-07.1 批准 |
| 步骤 | 加 `@ServletComponentScan`；写三个 `@WebServlet`；统一 JSON；写 Controller；打包启动；curl 验证 01～11 |
| 测试方式 | 真实 HTTP/curl；确认 3 个 Servlet 注册、404/405、Spring 对照；不依赖前端 |
| 交付物 | 后端代码 + MS-07.2 报告 |
| 验收 | TC-SERVLET-01～11 真实状态/命中；无启动冲突；业务 API 无回归 |
| 风险 | `/` 冲突；405 body 非 JSON；扩展名/路径优先级 |
| 停止条件 | 后端 HTTP 验证通过后停止，不写前端 |

### MS-07.3 前端 `/servlet-demo`

| 项 | 内容 |
| --- | --- |
| 目标 | 真实请求矩阵页面、导航、代理 |
| 允许修改 | `ServletDemoView.vue`、`servletDemo.ts`、router、AppLayout、vite、样式 |
| 禁止 | 后端业务/数据库、MS-06 页面、mock 数据 |
| 依赖 | MS-07.2 后端可运行 |
| 步骤 | 加代理；写 fetch 封装；写矩阵页；加路由/导航；手动真实请求；错误/空状态 |
| 测试方式 | Vite + 后端；真实请求；响应显示；405/404；不 mock |
| 交付物 | 页面 + 样式 + MS-07.3 报告 |
| 验收 | 页面可打开；每个按钮真实请求；无 console error；响应与 curl 一致 |
| 风险 | fetch 非 2xx；404/405 被误当网络错误；布局 |
| 停止条件 | 页面真实请求通过后停止，不写自动化 |

### MS-07.4 自动化测试与集成回归

| 项 | 内容 |
| --- | --- |
| 目标 | 后端真实容器测试 + Playwright + 全量回归 |
| 允许修改 | `ServletMappingIntegrationTest.java`、`ms07-browser-check.mjs` |
| 禁止 | 修改业务功能、放宽既有断言、mock 真实映射 |
| 依赖 | MS-07.2/07.3 |
| 步骤 | `@SpringBootTest(RANDOM_PORT)` + real HTTP；13 条用例；Playwright 真实按钮/状态；MS06/MS04/05 回归；build |
| 测试方式 | 真实 Tomcat HTTP；浏览器 fetch；不使用 MockMvc 代替容器 |
| 交付物 | 测试代码 + MS-07.4 报告 |
| 验收 | TC-SERVLET-01～13 通过；MS06.2～9、MS04/05 无回归；build 通过 |
| 风险 | 随机端口；数据库；405 差异；测试时序 |
| 停止条件 | 测试通过后停止，不截图/不写最终文档 |

### MS-07.5 截图、文档和最终交付

| 项 | 内容 |
| --- | --- |
| 目标 | 真实截图、报告、追踪表、交付结论 |
| 允许修改 | `2-20-servlet-mapping.png`、MS-07 报告、追踪表；不修改旧报告 |
| 禁止 | 业务代码、数据库、无关截图、Git commit |
| 依赖 | MS-07.4 通过 |
| 步骤 | 截图；核对源码/测试/截图；写最终报告；更新状态；Git 审计 |
| 测试方式 | 截图实际打开；最终 build；全量回归记录；路径核对 |
| 交付物 | 截图 + 最终报告 + 追踪表 |
| 验收 | 证据真实；无 P0/P1；2.20 可复核 |
| 风险 | 截图与源码不一致；文档夸大 |
| 停止条件 | 交付报告完成即停止，等待正式审查；不进入 MS-08 |

---

## 7. 验收矩阵

| 验收 | 方式 |
| --- | --- |
| 真实 `@WebServlet` | 后端启用 `@ServletComponentScan`；启动日志 + TC-01 |
| 精确/路径/扩展名 | TC-01/02/03/05 |
| 路径 vs 扩展名 | TC-04 |
| Spring MVC 对照 | TC-07 |
| 405 | TC-08/09/10 |
| 404/默认 `/` | TC-11 |
| 查询参数 | TC-12 |
| 前端真实请求 | TC-13 + Playwright |
| 无 DB 依赖 | 代码审查 + 测试不启业务表 |
| 业务回归 | MS04/MS05 |
| 课程回归 | MS06.2～MS06.9 |
| 构建 | `npm run build` |

---

## 8. 测试命令（规划）

```bash
# 后端完整（包含新集成测试）
mvn clean test

# 前端
npm run build
node scripts/ms07-browser-check.mjs

# 回归
node scripts/ms06-9-browser-check.mjs
node scripts/ms06-2-browser-check.mjs
node scripts/ms04-browser-check.mjs
node scripts/ms05-browser-check.mjs
```

真实 Servlet 测试必须使用嵌入式 Tomcat；`MockMvc` 不能作为容器映射的唯一证据。

---

## 9. 风险与停止条件汇总

- 未批准 `*.smap`/类名/只实现 GET 前，不进入 MS-07.2；
- 任何阶段发现 `/` 冲突、业务 API 回归、全局 404/405 契约破坏，立即停止并报告；
- 不修改数据库/API 契约；
- 不把前端模拟结果当作真实响应；
- 不在本阶段创建 Servlet 或页面。

---

## 10. 待批准项

1. `*.smap` 扩展名；
2. `*DemoServlet` 类名；
3. 自定义 Servlet 只实现 GET（真实 405）；
4. 默认 `/` 由 DispatcherServlet 占用，不新增自定义 `/`；
5. 截图 `2-20-servlet-mapping.png`；
6. 五阶段拆分。

**批准后从 MS-07.2 开始；本阶段停止。**

---

## 11. MS-07.5 实施完成记录（2026-11-14）

最终状态：

- 三个 Servlet、Spring MVC 对照 Controller、`/servlet-demo` 页面均已实现；
- 注册方式为 `@ServletComponentScan` + `@WebServlet`；
- 扩展名最终为 `*.smap`；
- 自定义 Servlet 只实现 GET，POST 真实 405；
- 默认 `/` 仍由 DispatcherServlet 占用，没有新增默认 Servlet；
- TC-SERVLET-01～013 全部通过；
- Maven 158 tests、前端 build、MS04/MS05/MS06 回归通过；
- 截图：
  - `2-20-servlet-mapping.png`
  - `2-20-servlet-mapping-requests.png`
  - `2-20-servlet-mapping-http.png`

最终证据与未完成事项见 `docs/review/ms-07-final-delivery-report.md`。本计划不再作为待执行计划使用。
