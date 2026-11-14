# MS-07.1 Servlet 映射演示前置审计报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-07.1 前置审计与实施设计 |
| 课程依据 | 任务书附录3 的 **2.20 Servlet与请求URI的映射规则** |
| 审计范围 | 后端 Servlet/MVC、前端路由/代理、测试基础、文档与 Git |
| 结论 | **可以进入方案评审；MS-07.2 尚未开始；需用户批准设计决策后再实现** |

---

## 1. 审计基线与资料路径

### 1.1 Git/工作区

```text
工作目录：E:\desktop\高级Web&J2EE期末
仓库：Intelligent-Express-Management-System
分支：main
HEAD：6dd2bdbfdb1ca912ccabee3b0f9acee5c1fdca27
```

审计开始时工作区存在 R1 整改及 R1 前遗留变更：

```text
 D docs/handover/CHATGPT_PROJECT_HANDOVER.md
 M docs/review/ms-06-9-report.md
 M docs/review/ms-06-final-delivery-report.md
 M docs/review/ms-06-implementation-plan.md
 M docs/review/ms-06.3-report.md
 M docs/screenshots/2-02-block.png
 M frontend/scripts/ms06-3-browser-check.mjs
 M frontend/scripts/ms06-9-browser-check.mjs
 M frontend/src/components/basics/BlockDemo.vue
 M frontend/src/styles/basics.scss
?? docs/review/handover/
?? docs/review/ms-06-final-audit-report.md
?? docs/review/ms-06-remediation-report.md
?? docs/review/ms-06-second-audit-report.md
```

本阶段不恢复、不清理、不提交这些改动。

### 1.2 文档实际路径

要求中列出的根目录文档实际位置如下：

| 要求路径 | 实际路径 | 状态 |
| --- | --- | --- |
| `PROJECT_CONTEXT.md` | `docs/PROJECT_CONTEXT.md` | 存在 |
| `ORIGINAL_DEVELOPMENT_PLAN.md` | `docs/ORIGINAL_DEVELOPMENT_PLAN.md` | 存在 |
| `development-progress.md` | `docs/development-progress.md` | 存在 |
| `requirements.md` | `docs/requirements.md` | 存在 |
| `docs/architecture.md` | `docs/architecture.md` | 存在 |
| `docs/api-design.md` | `docs/api-design.md` | 存在 |
| `docs/database-design.md` | `docs/database-design.md` | 存在 |
| `docs/test-plan.md` | `docs/test-plan.md` | 存在 |
| MS-06 最终报告 | `docs/review/ms-06-final-delivery-report.md` | 存在 |
| MS-06 第二次复核 | `docs/review/ms-06-second-audit-report.md` | 存在 |

根目录不存在 `PROJECT_CONTEXT.md`、`ORIGINAL_DEVELOPMENT_PLAN.md`、`development-progress.md`；不能把这些文档当作缺失，实际位于 `docs/`。

### 1.3 课程任务书原文

任务书文件：

```text
E:\desktop\高级Web&J2EE期末\高级Web项目开发（实践）任务书.docx
```

从 docx 表格提取到的原文：

```text
2.20 | Servlet与请求URI的映射规则
```

任务书开头相关要求：

```text
如何更好地处理GUI程序的模块划分？一次请求是按照一个怎样的规则从URI映射到Servlet程序的？
```

第1章实践总结要求：

```text
（四）Servlet与请求URI的映射规则
```

**核对结论：**

- 课程原文确实存在 2.20，标题为“Servlet与请求URI的映射规则”；
- 任务书没有规定必须使用哪三个 URL Pattern、哪种扩展名、哪个类名；
- 精确匹配/路径匹配/扩展名匹配/默认映射四类，是项目 `requirements.md` FR-17 和 `architecture.md` §7 的项目设计；
- `*.smap`、`*.do` 等具体扩展名不是课程原文；
- 不能把之前 ChatGPT 的方案写成已经批准的课程原文。

---

## 2. 现有后端审计

### 2.1 启动类与注册方式

文件：`backend/src/main/java/com/express/addressbook/ExpressAddressApplication.java`

现状：

```java
@SpringBootApplication
public class ExpressAddressApplication {
    public static void main(String[] args) { ... }
}
```

检查结果：

| 检查项 | 现状 |
| --- | --- |
| `@ServletComponentScan` | **不存在** |
| `ServletRegistrationBean` | **不存在** |
| `@WebServlet` | **不存在** |
| Servlet Filter | **不存在** |
| Servlet Listener | **不存在** |
| `servlet/` 包 | **不存在** |
| `/demo/**` Controller | **不存在** |

结论：目前没有任何自定义 Servlet，也没有启用 Servlet 注解扫描；不能假设 `@WebServlet` 会自动生效。

### 2.2 DispatcherServlet 与容器

- Spring Boot 使用嵌入式 Tomcat；
- `application.yml` 没有 `spring.mvc.servlet.path`，默认 `DispatcherServlet` 映射为 `/`；
- `server.servlet.context-path` 未配置，默认空；
- `spring.mvc.throw-exception-if-no-handler-found: true`；
- `spring.web.resources.add-mappings: false`。

因此：

- `/api/**` 由 `/` 的 `DispatcherServlet` 接收；
- 不存在的 Spring 路径会进入 Spring 异常处理，而不是直接由 Tomcat 返回纯 404；
- 自定义 Servlet 映射（精确/路径/扩展名）必须优先于 `/` 才会被 Tomcat 命中；
- **默认 `/` 当前由 DispatcherServlet 占用，不能为了演示另加 `@WebServlet("/")`。**

### 2.3 现有 Controller

`AddressController`：

```text
/api/addresses
  POST /parse
  GET /
  GET /{id}
  POST /
  PUT /{id}
  DELETE /{id}
  DELETE /batch
```

其他：

```text
/health
/health/db
```

这些路径是 Spring MVC 路由，不是 Servlet 容器映射，不能与 `/demo/servlet/**` 混淆。

### 2.4 统一异常处理

`GlobalExceptionHandler`：

- `NoResourceFoundException` / `NoHandlerFoundException` → HTTP 404 + `R` code 40402；
- `HttpRequestMethodNotSupportedException` → HTTP 405 + `R` code 40501；
- 其他异常按业务码映射。

设计含义：

- Spring MVC 的 404/405 会得到统一 JSON；
- **自定义 Servlet 的 405 不经过 `@RestControllerAdvice`**：`HttpServlet` 默认方法分派会直接返回 405；
- 404 是否经过 Spring 取决于 URL 是否进入 DispatcherServlet；本项目的默认 `/` 会把它交给 Spring。

### 2.5 测试基础

- `backend/src/test` 已有单元、MockMvc、真实 Parser、MySQL 集成测试；
- 现有 `@SpringBootTest`/`@AutoConfigureMockMvc` 不能证明 `@WebServlet` 被嵌入式 Tomcat 注册；
- 要验证真实容器映射，必须使用 `webEnvironment = RANDOM_PORT` + 真实 HTTP 客户端；
- `test-plan.md` **没有 TC-SERVLET 章节**，仅有 TC-PARSE/API/ADDR 等；
- 本阶段只规划测试，不写测试。

---

## 3. 现有前端审计

### 3.1 路由

`frontend/src/router/index.ts` 当前只有：

```text
/            → /import
/import
/addresses
/web-basics
/web-basics/wireframe
/:pathMatch(.*)*
```

**没有 `/servlet-demo` 路由。**

### 3.2 导航

`AppLayout.vue` 的“课程实践”区域：

- “Web 基础实践”是 `RouterLink`；
- “Servlet 映射演示”仍是：

```html
<span class="side-nav__item is-disabled" aria-disabled="true" title="MS-07 实现，当前为规划入口">
  ...
  <span class="side-nav__soon">待实现</span>
</span>
```

`activeMenu` 也没有 `servlet-demo` 分支。

### 3.3 HTTP 与代理

- `frontend/vite.config.ts` 只代理 `/api`；
- `request.ts` 的 Axios 基址是 `/api`；
- 没有 `/demo` 代理；
- **不能用现有 Axios 包装直接请求 `/demo`**，因为基址和错误拦截不适合展示原始 404/405；
- MS-07.3 需要独立的原生 `fetch` 封装，保留 HTTP 状态、响应头和正文。

### 3.4 页面状态

- `/servlet-demo` 页面不存在；
- 没有 Servlet 演示截图；
- 没有 Servlet 映射相关组件或样式；
- 导航中的“待实现”是真实状态，不能提前改成已实现。

---

## 4. 课程原文、项目事实和项目设计的分界

| 类别 | 内容 | 依据 |
| --- | --- | --- |
| 课程原文 | 2.20 标题“Servlet与请求URI的映射规则”；第1章 1.4；一次请求如何从 URI 映射到 Servlet | 任务书 docx |
| 项目已有事实 | 当前无 `@WebServlet`、无 `@ServletComponentScan`、无 `/servlet-demo`、Vite 只代理 `/api`、`DispatcherServlet` 默认 `/` | 源码/配置 |
| 项目设计 | 精确/路径/扩展名三 Servlet + Spring MVC 对照 + 默认映射说明 + `/servlet-demo` 页面 | `requirements.md` FR-17、`architecture.md` §7 |
| 仍待批准 | 扩展名选 `*.smap` 还是 `*.do`；类名；自定义 Servlet 是否只实现 GET（以产生真实 405）；前端矩阵范围 | 本报告与 scope adjustment |

架构 §7 已有建议（`*.do`、`@ServletComponentScan`、三个 Servlet、Spring Controller），但它是设计提案，不是课程原文，也尚未实现。

---

## 5. 主要风险

| 风险 | 影响 | 规避 |
| --- | --- | --- |
| 忘加 `@ServletComponentScan` | 三个 Servlet 完全不注册，页面全落到 Spring | 启动时检查注册日志；TC-01 立即验证 |
| `@WebServlet("/")` 与 DispatcherServlet `/` 冲突 | 启动失败或吞掉全部 Spring 路由 | **禁止**新增自定义 `/`；默认边界只观察 DispatcherServlet |
| 路径 `*` 与扩展名冲突 | 请求命中错误处理器 | 用 `/demo/servlet/*` + `*.smap` 测试路径优先 |
| 405 经过 Spring 全局异常，body 与自定义 Servlet 不一致 | 页面/测试误判 | 分开断言自定义 Servlet 405 与 Spring 405 |
| 404 被 Spring 包装 | 与 Tomcat 原生 404 混淆 | 明确默认 `/` 由 DispatcherServlet 接收；页面显示这是 Spring 404 |
| `fetch`/Axios 对非 2xx 抛错 | 405/404 矩阵显示失败 | 使用原生 fetch，保留 status/headers/body |
| `*.smap` 与架构旧提案 `*.do` 不一致 | 文档/实现漂移 | 由 scope adjustment 决定最终扩展名并记录 |
| 只做 MockMvc | 证明不了容器 Servlet 映射 | `RANDOM_PORT` 真实 HTTP 测试 |
| 本机端口占用/数据库 | 测试环境失败 | 固定端口 8080/5173；测试库隔离；演示端点无 DB |

---

## 6. 前置审计结论

1. **2.20 原文存在**，但只给出规则主题，没有规定三个具体 URL Pattern。
2. 当前项目**没有** Servlet 演示代码、`@ServletComponentScan`、`/servlet-demo`、`/demo` 代理。
3. 当前 `DispatcherServlet` 使用默认 `/`，没有自定义默认 Servlet。
4. 需要先批准 `ms-07-scope-adjustment.md` 中的扩展名、类名、方法边界和默认映射方案，再进入 MS-07.2。
5. 测试计划缺少 TC-SERVLET，必须由 `ms-07-test-traceability.md` 补齐规划，不能宣称已经执行。

**本阶段不创建 Servlet、不修改启动类、不新增 Controller、不创建页面。**
