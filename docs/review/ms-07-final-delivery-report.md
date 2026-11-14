# MS-07 最终交付报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 交付范围 | 课程任务 2.20“Servlet与请求URI的映射规则” |
| 课程论文题目 | **快递地址簿录入程序设计**（未改变） |
| 结论 | **实现、真实 HTTP、Playwright、Maven、截图均完成；建议提交 ChatGPT 独立正式审查，不自行宣布验收通过** |

---

## 1. MS-07 目标与范围

MS-07 的目标是用真实 Servlet 容器映射和 Spring MVC 对照，完成课程 2.20：

- 三个真实 `@WebServlet`；
- Spring MVC Controller 对照；
- 真实 `/demo/**` HTTP 请求；
- `/servlet-demo` 前端请求矩阵；
- 404/405/默认映射边界；
- 自动化测试与真实证据。

不包含：

- 登录/权限；
- 地址业务改造；
- 数据库结构变更；
- 其他课程小节重做；
- MS-08/09。

---

## 2. 课程任务 2.20 对照

任务书 docx 原文：

```text
2.20 | Servlet与请求URI的映射规则
```

任务书开头问题：

```text
一次请求是按照一个怎样的规则从URI映射到Servlet程序的？
```

**区分：**

- 课程原文只规定 2.20 主题和“URI 到 Servlet”的规则问题；
- 精确/路径/扩展名三类映射、Spring MVC 对照、`/servlet-demo` 页面属于项目 `requirements.md` FR-17 与 `architecture.md` §7 的设计推导；
- 本交付没有把项目推导写成课程原文。

---

## 3. 三个真实 Servlet

```text
ExactMappingDemoServlet       /demo/servlet/exact
PathMappingDemoServlet        /demo/servlet/*
ExtensionMappingDemoServlet   *.smap
```

响应字段：

- `handler`；
- `mappingType`（EXACT/PATH/EXTENSION）；
- `mapping`；
- `requestUri`；
- `contextPath`；
- `servletPath`；
- `pathInfo`；
- `method`；
- `query`。

三个 Servlet 不访问 Service/Mapper/数据库。

---

## 4. Servlet 注册机制

`ExpressAddressApplication`：

```java
@SpringBootApplication
@ServletComponentScan(basePackages = "com.express.addressbook.servlet")
```

三个类使用 `@WebServlet`；没有使用 `ServletRegistrationBean`，没有重复注册，没有新增默认 `/`。

真实 `/demo/servlet/exact` 返回 `ExactMappingDemoServlet`，证明 `@WebServlet` 已被嵌入式 Tomcat 注册。

---

## 5. Spring MVC 对照

`ServletMappingDemoController`：

```text
GET /demo/spring/info
GET /demo/spring/echo/{name}
GET /demo/spring/method-only
```

响应明确标识：

```text
handler=ServletMappingDemoController
mappingType=SPRING_MVC
controllerMethod=...
requestUri=...
method=...
```

Controller 不访问业务 Service/Mapper；不是 Servlet 命中结果的伪装。

---

## 6. URI 映射规则

```text
1. 精确匹配
2. 最长路径匹配
3. 扩展名匹配
4. 默认 /
```

实际的容器规则：

- `/demo/servlet/exact` → Exact；
- `/demo/servlet/exact/extra` → Path；
- `/demo/servlet/prefix/a` → Path；
- `/demo/legacy/report.smap` → Extension；
- `/demo/servlet/legacy/report.smap` → Path（路径优先于扩展名）；
- `/demo/spring/info` → DispatcherServlet → Spring Controller。

Spring MVC 的 `@GetMapping` 不是容器 `url-pattern`；它发生在请求进入 DispatcherServlet 之后。

---

## 7. 默认映射边界

当前 Spring Boot：

```text
DispatcherServlet → /
spring.mvc.servlet.path 未配置
```

因此：

- 没有新增 `@WebServlet("/")`；
- 未匹配自定义 Servlet 的请求进入默认 `/`；
- `/demo/legacy/report.json` 无 Spring Handler → 404；
- 该 404 由 Spring 全局异常处理返回 `R`，不是自定义默认 Servlet 的响应。

没有为了演示默认映射而抢占 `/` 或破坏业务路由。

---

## 8. 405 / 404 结果

| 请求 | 状态 | 说明 |
| --- | ---: | --- |
| `POST /demo/servlet/exact` | 405 | 自定义 Servlet 只实现 GET；未改 200 |
| `POST /demo/servlet/prefix/a` | 405 | 同上 |
| `POST /demo/spring/method-only` | 405 | Spring 全局异常，`R.code=40501` |
| `GET /demo/legacy/report.json` | 404 | 默认 `/` → DispatcherServlet → 404，`R.code=40402` |

自定义 Servlet 的 405 由容器/`HttpServlet` 方法分派触发，Spring Boot `/error` 输出错误 JSON；页面如实显示，不把错误状态改成成功。

---

## 9. TC-SERVLET-01～013

| 编号 | 结果 |
| --- | --- |
| 01 精确命中 | PASS |
| 02 精确不完全匹配 → Path | PASS |
| 03 路径匹配 | PASS |
| 04 多级路径 | PASS |
| 05 扩展名 | PASS |
| 06 路径/扩展名冲突 | PASS |
| 07 Spring MVC | PASS |
| 08 精确 POST 405 | PASS |
| 09 路径 POST 405 | PASS |
| 10 Spring POST 405 | PASS |
| 11 默认 `/` 404 | PASS |
| 12 查询参数 | PASS |
| 13 前端真实请求 | PASS |

追踪表：

```text
docs/review/ms-07-test-traceability.md §7
```

---

## 10. Playwright

```text
node scripts/ms07-3-browser-check.mjs
MS07_3_BROWSER_OK
exit 0
```

验证：

- 页面打开、导航 active；
- 三个 Servlet 与 Spring 真实请求；
- 405/404 真实状态和正文；
- 查询参数；
- 单条、全部、清空、刷新；
- 未知路由 404；
- 不调用 `/api/addresses`；
- 1366/980/760/390 无页面级横向溢出；
- 页面 JS 错误为空。

---

## 11. Maven 测试

```text
mvn -B -Dtest=ServletMappingIntegrationTest test
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
exit 0

mvn -B clean test
Tests run: 158, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
exit 0
```

158 = 原 146 + Servlet 12。

---

## 12. 响应式检查

| 宽度 | 页面整体横向溢出 | 矩阵 |
| --- | --- | --- |
| 1366 | 无 | 完整显示 |
| 980 | 无 | 可读 |
| 760 | 无 | 可读 |
| 390 | 无 | 内部横向滚动 |

矩阵内部滚动是允许的；页面本身不横向溢出。

---

## 13. 截图清单

| 文件 | 尺寸 | 内容 |
| --- | --- | --- |
| `docs/screenshots/2-20-servlet-mapping.png` | 1366×2239 | 完整页面：规则、三种映射、真实矩阵、404 详情、边界 |
| `docs/screenshots/2-20-servlet-mapping-requests.png` | 1066×753 | 完整真实请求矩阵：200/405/404 与实际 handler |
| `docs/screenshots/2-20-servlet-mapping-http.png` | 1066×393 | 最近一次 404 响应头/正文和边界说明 |

全部来自真实浏览器运行，非手工拼接、非 Mock、非旧图。

---

## 14. Git 修改范围

```text
branch: main
HEAD: 6dd2bdb
```

MS-07.2～07.5 新增/修改：

```text
backend/src/main/java/com/express/addressbook/ExpressAddressApplication.java
backend/src/main/java/com/express/addressbook/servlet/
backend/src/main/java/com/express/addressbook/demo/
backend/src/test/java/com/express/addressbook/servlet/
frontend/src/views/ServletDemoView.vue
frontend/src/api/servletDemo.ts
frontend/src/router/index.ts
frontend/src/layout/AppLayout.vue
frontend/src/components/basics/PracticeNav.vue
frontend/src/styles/basics.scss
frontend/vite.config.ts
frontend/scripts/ms07-3-browser-check.mjs
frontend/scripts/ms07-5-screenshots.mjs
frontend/scripts/ms06-2-browser-check.mjs（最小适配）
frontend/scripts/ms06-3-browser-check.mjs（最小适配）
frontend/scripts/ms06-7-browser-check.mjs（最小适配）
frontend/scripts/ms06-9-browser-check.mjs（最小适配）
docs/review/ms-07-*.md
docs/screenshots/2-20-servlet-mapping*.png
docs/development-progress.md
docs/PROJECT_CONTEXT.md
```

未修改主库、数据库结构、业务 API 契约；未 commit/push/reset/clean/checkout/stash。

工作区中的 R1/MS-06 历史变更和 handover 遗留状态保留。

---

## 15. 已知问题

1. 自定义 Servlet 405 的正文来自 Spring Boot `/error`，不是 `R` 成功结构；页面/截图已如实展示。
2. 2.20 截图是三张支持性证据（主图 + 矩阵 + 404 详情），不是单一图片。
3. 390px 下请求矩阵使用内部横向滚动。
4. Playwright 尚未纳入 CI。
5. 主 chunk/Sass warning 为既有问题。
6. 所有 MS-06/MS-07 改动仍未提交 Git。
7. 2.20 课程原文只规定主题，具体映射/类名/扩展名是项目设计，需在论文中明确区分。

---

## 16. 是否建议正式验收

**建议提交 ChatGPT 独立正式审查/验收。**

依据：

- 2.20 真实 Servlet、Spring MVC 对照、前端真实请求均完成；
- TC-SERVLET-01～013 PASS；
- 158 后端测试 PASS；
- MS04/MS05/MS06 回归 PASS；
- 前端构建 PASS；
- 三张真实截图生成；
- 未发现 P0/P1 阻塞。

本报告不自行宣布验收通过；等待 ChatGPT 独立审查和用户确认，不进入 MS-08。
