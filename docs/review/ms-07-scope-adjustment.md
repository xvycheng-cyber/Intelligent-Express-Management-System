# MS-07 范围调整与设计决策

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-07.1 前置设计 |
| 课程依据 | 任务书 2.20“Servlet与请求URI的映射规则” |
| 状态 | **待用户/ChatGPT批准后进入 MS-07.2** |

---

## 1. 课程原文与项目设计的边界

### 1.1 课程原文

任务书 docx 实际提取：

```text
2.20 | Servlet与请求URI的映射规则
```

第1章相关要求：

```text
（四）Servlet与请求URI的映射规则
```

开头问题：

```text
一次请求是按照一个怎样的规则从URI映射到Servlet程序的？
```

课程**没有**规定：

- 必须使用哪三个 URL Pattern；
- 扩展名必须是 `.do` / `.smap`；
- 必须实现 Spring MVC 对照；
- 必须提供 `/servlet-demo` 页面；
- 默认映射必须如何展示。

### 1.2 项目推导

以下来自 `requirements.md` FR-17 和 `architecture.md` §7，是项目设计：

- 精确 / 路径 / 扩展名三类 Servlet；
- 与 Spring MVC Controller 对照；
- `/servlet-demo` 页面真实请求矩阵；
- 默认 `/` 的边界说明。

---

## 2. 最终建议方案

### 2.1 三种真实 Servlet

| 类型 | 最终建议类名 | 映射 | 说明 |
| --- | --- | --- | --- |
| 精确匹配 | `ExactMappingDemoServlet` | `/demo/servlet/exact` | 精确优先级最高 |
| 路径匹配 | `PathMappingDemoServlet` | `/demo/servlet/*` | 最长路径前缀；`pathInfo` 保留剩余路径 |
| 扩展名匹配 | `ExtensionMappingDemoServlet` | `*.smap` | 扩展名匹配；路径无关 |

选择 `*.smap` 的理由：

- 唯一、无现有业务含义，避免与历史 Struts `.do` 约定混淆；
- 当前项目没有 `.do`/`.smap` 静态资源，不与 Spring 静态资源冲突；
- 课程原文未规定扩展名，项目可以自有；
- 架构 §7 早期的 `*.do` 是设计提案，本文件记录最终改为 `.smap`。如用户坚持沿用 `*.do`，应在本阶段批准前明确提出。

### 2.2 Spring MVC 对照

`ServletMappingDemoController`（`@RestController`）：

```text
GET  /demo/spring/info
GET  /demo/spring/echo/{name}
GET  /demo/spring/method-only
GET  /demo/spring/wildcard/{*path}   （可选，用于 Spring 通配说明）
```

不实现 `/demo/spring/**` 的宽泛 Controller 映射，避免与具体方法产生歧义。

### 2.3 注册方式

在 `ExpressAddressApplication` 增加：

```java
@ServletComponentScan(basePackages = "com.express.addressbook.servlet")
```

三个 Servlet 使用：

```java
@WebServlet(name = "...", urlPatterns = "...")
```

不使用 `ServletRegistrationBean`，以便直接展示 `@WebServlet` 注解；如果 Spring Boot 扫描失败，MS-07.2 必须停下修复注册，不得用前端 mock 替代。

### 2.4 HTTP 方法边界

建议：

- 三个自定义 Servlet **只实现 `doGet`**；
- 因此 `POST` 到 exact/path 由 `HttpServlet` 返回真实 **405**；
- 这能证明“容器先按 URL Pattern 选中 Servlet；HTTP 方法由 Servlet 的 `service/doXxx` 处理”，并满足 TC-SERVLET-08/09；
- 架构 §7 早期建议“三个 Servlet 同时实现 doGet/doPost”会消除精确/路径的 405 证据，最终不采用；
- Spring MVC 的 `POST /demo/spring/method-only` 由 Spring 返回 405；其 body 由 `GlobalExceptionHandler` 统一为 `R`，与自定义 Servlet 的 405 不同，页面应分别展示。

### 2.5 默认映射

**不新增 `@WebServlet("/")`。**

当前 Spring Boot 的 `DispatcherServlet` 默认使用 `/`：

```text
不匹配自定义 Servlet 的请求 → 默认 `/` → DispatcherServlet → Spring MVC
```

因此：

- `/demo/legacy/report.smap` → Extension Servlet；
- `/demo/servlet/...` → Path/Exact Servlet；
- `/demo/spring/...` → DispatcherServlet → Controller；
- `/demo/legacy/report.json` → 默认 `/` → DispatcherServlet → 无 Handler → 404（由全局异常处理返回 JSON）；
- 不能把 `/demo/legacy/report.json` 的 404 说成“自定义默认 Servlet”；
- 不应为了演示默认 Servlet 而抢占 `/`，否则会破坏所有业务路由。

### 2.6 响应契约

三个 Servlet 返回同构 JSON，至少：

```json
{
  "handler": "PathMappingDemoServlet",
  "mappingType": "PATH",
  "mapping": "/demo/servlet/*",
  "requestUri": "/demo/servlet/prefix/a/b",
  "contextPath": "",
  "servletPath": "/demo/servlet",
  "pathInfo": "/prefix/a/b",
  "method": "GET",
  "query": "trace=1"
}
```

- UTF-8；
- 不访问数据库；
- 不输出业务数据；
- 不写日志中的完整地址/手机号；
- 不改变现有 `/api/**` 契约。

### 2.7 前端

- 新路由 `/servlet-demo`；
- 导航从“待实现”改为真实 `RouterLink`；
- Vite 增加 `/demo` 代理；
- 页面使用原生 `fetch`，保留 status/headers/body，不使用 Axios 的 `/api` 包装做 404/405 判断；
- 页面真实发起请求，禁止前端 mock 结果；
- 页面说明“容器映射 vs Spring MVC”；
- 页面不修改业务页面布局。

### 2.8 截图命名

建议：

```text
docs/screenshots/2-20-servlet-mapping.png
```

来自真实页面，显示映射矩阵、一次真实请求的状态/命中结果、Spring 对照和默认映射说明。若篇幅需要，可另加 `2-20-servlet-mapping-404-405.png`，但最终交付至少一张真实截图。

---

## 3. 与既有文档的差异

| 文档 | 既有内容 | 最终调整 |
| --- | --- | --- |
| `architecture.md` §7 | `PrefixMappingServlet`、`*.do`、doGet+doPost | 类名改为 `PathMappingDemoServlet`；扩展名改为 `*.smap`；只实现 doGet 以保留真实 405 |
| `requirements.md` FR-17 | 精确/前缀/扩展名/默认四类 | 不变；术语“前缀”改为“路径”，含义相同 |
| `test-plan.md` | 无 TC-SERVLET | 新增规划文件，不修改原 test-plan 历史 |
| `PROJECT_CONTEXT.md` | `/servlet-demo` 设计存在、代码不存在 | 与当前一致 |
| `development-progress.md` | MS-07 NOT_STARTED | 与当前一致 |

架构旧建议不是课程原文，也不是已实现事实；本文件是 MS-07 的最终设计输入，需用户批准。

---

## 4. 不在本次范围

- 不修改数据库/业务 API/登录；
- 不实现真实 HTTPS/生产部署；
- 不把 Servlet 演示接入地址业务；
- 不新增第三方依赖；
- 不进入 MS-07.2。

---

## 5. 待用户确认

1. 是否批准扩展名使用 `*.smap`（而不是 architecture 旧提案 `*.do`）；
2. 是否批准三个 Servlet 只实现 GET，以真实 405 作为教学证据；
3. 是否批准截图命名 `2-20-servlet-mapping.png`；
4. 是否批准“默认 `/` 由 DispatcherServlet 占用、不新增自定义默认 Servlet”的说明方案；
5. 是否批准 MS-07.2～MS-07.5 的拆分。

**未获批准前不得实现 Servlet 或 `/servlet-demo`。**
