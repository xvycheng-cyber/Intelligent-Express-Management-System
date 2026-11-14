# MS-07 TC-SERVLET 测试追踪表（规划）

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 依据 | 任务书 2.20、`requirements.md` FR-17、`architecture.md` §7、`ms-07-scope-adjustment.md` |
| 状态 | **规划已执行；结果见 §7（2026-11-14 MS-07.4）** |
| 测试库 | 不访问业务表；真实 Tomcat HTTP + Playwright |

> `docs/test-plan.md` 当前没有 TC-SERVLET 章节；本文件补足规划，不修改原 test-plan 历史。执行后必须回到本表记录实际结果，不能预先写“通过”。

---

## 1. 约定

- 后端地址：`http://127.0.0.1:8080`
- 前端地址：`http://127.0.0.1:5173`
- 后端测试类：`ServletMappingIntegrationTest`
- 前端测试：`frontend/scripts/ms07-browser-check.mjs`
- 所有后端测试使用 `@SpringBootTest(webEnvironment = RANDOM_PORT)` + 真实 HTTP 客户端；不使用 MockMvc 证明容器映射。
- 所有演示端点无数据库访问。

---

## 2. TC-SERVLET-01～013

| 编号 | 测试目的 | 方法 | 请求 URL | 预期命中 | 预期 HTTP | 验证方式 | 自动化位置 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| TC-SERVLET-01 | 精确映射命中 | GET | `/demo/servlet/exact` | `ExactMappingDemoServlet` | 200 | body `handler`/`mappingType=EXACT`/`servletPath=/demo/servlet/exact`/`pathInfo=null` | 后端 `ServletMappingIntegrationTest#exactMapping` |
| TC-SERVLET-02 | 精确映射不完全匹配时落到路径映射 | GET | `/demo/servlet/exact/extra` | `PathMappingDemoServlet` | 200 | `mappingType=PATH`；`servletPath=/demo/servlet`；`pathInfo=/exact/extra` | 后端 `#exactExtraFallsToPath` |
| TC-SERVLET-03 | 路径映射命中 | GET | `/demo/servlet/prefix/a` | `PathMappingDemoServlet` | 200 | `pathInfo=/prefix/a`；不是 Exact | 后端 `#pathMapping` |
| TC-SERVLET-04 | 路径映射捕获多级路径 | GET | `/demo/servlet/prefix/a/b` | `PathMappingDemoServlet` | 200 | `pathInfo=/prefix/a/b`；完整保留 | 后端 `#pathMappingMultiLevel` |
| TC-SERVLET-05 | 扩展名映射命中 | GET | `/demo/legacy/report.smap` | `ExtensionMappingDemoServlet` | 200 | `mappingType=EXTENSION`；`servletPath` 为完整路径；`pathInfo=null` | 后端 `#extensionMapping` |
| TC-SERVLET-06 | 路径映射与扩展名冲突时路径优先 | GET | `/demo/servlet/legacy/report.smap` | `PathMappingDemoServlet` | 200 | 不是 `ExtensionMappingDemoServlet`；`servletPath=/demo/servlet`；`pathInfo=/legacy/report.smap` | 后端 `#pathBeatsExtension` |
| TC-SERVLET-07 | Spring MVC 对照 | GET | `/demo/spring/info` | `ServletMappingDemoController` | 200 | body `handler`/`mappingType=SPRING_MVC`；不落入 Servlet | 后端 `#springInfo`；Playwright 矩阵 |
| TC-SERVLET-08 | 精确 Servlet 不支持 POST → 405 | POST | `/demo/servlet/exact` | `ExactMappingDemoServlet` 的 405 分支 | 405 | 真实状态 405；不经过 Spring 全局异常；不返回 exact 200 body | 后端 `#exactPostMethodNotAllowed` |
| TC-SERVLET-09 | 路径 Servlet 不支持 POST → 405 | POST | `/demo/servlet/prefix/a` | `PathMappingDemoServlet` 的 405 分支 | 405 | 真实状态 405；不返回 path 200 body | 后端 `#pathPostMethodNotAllowed` |
| TC-SERVLET-10 | Spring MVC 不支持 POST → 405 | POST | `/demo/spring/method-only` | `ServletMappingDemoController` 的 405 | 405 | Spring 全局异常：HTTP 405；body `R.code=40501`（可附 `Allow`） | 后端 `#springPostMethodNotAllowed`；Playwright |
| TC-SERVLET-11 | 未匹配请求与默认映射边界 | GET | `/demo/legacy/report.json` | 默认 `/` → `DispatcherServlet` → Spring 404 | 404 | 不落入 Extension Servlet；Spring 全局异常 body `R.code=40402`；说明不是自定义默认 Servlet | 后端 `#defaultMappingBoundary` |
| TC-SERVLET-12 | 查询参数不改变映射 | GET | `/demo/servlet/exact?trace=query-42` | `ExactMappingDemoServlet` | 200 | handler 仍为 Exact；body `query=trace=query-42`；映射类型不变 | 后端 `#queryDoesNotChangeMapping` |
| TC-SERVLET-13 | 前端页面真实 HTTP 请求 | GET/POST | 页面 `/servlet-demo` 的矩阵请求 | 按行对应真实容器/Spring | 200/404/405 | Playwright 点击真实按钮；用响应事件/DOM 验证实际状态和 handler；不允许 route mock | `ms07-browser-check.mjs` |

---

## 3. 映射优先级覆盖

| 优先级 | 请求 | 预期 |
| --- | --- | --- |
| 精确 | `/demo/servlet/exact` | Exact |
| 最长路径 | `/demo/servlet/exact/extra`、`/demo/servlet/prefix/a/b` | Path |
| 扩展名 | `/demo/legacy/report.smap` | Extension |
| 路径 vs 扩展名 | `/demo/servlet/legacy/report.smap` | Path（路径优先） |
| 默认 `/` | `/demo/legacy/report.json` | DispatcherServlet → Spring 404 |

**特别说明：**

- 不新增 `@WebServlet("/")`；
- 默认 `/` 当前由 Spring Boot 的 DispatcherServlet 占用；
- 因此“默认映射边界”是 Spring MVC 的 404，不是自定义默认 Servlet 的响应。

---

## 4. 预期响应字段

自定义 Servlet：

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

Spring MVC：

```json
{
  "handler": "ServletMappingDemoController",
  "mappingType": "SPRING_MVC",
  "mapping": "/demo/spring/info",
  "requestUri": "/demo/spring/info",
  "method": "GET",
  "query": ""
}
```

---

## 5. 执行前依赖

- 后端 `mvn package -DskipTests` 或 `mvn spring-boot:run`；
- MySQL 可启动，但 demo 端点不查询业务表；
- 前端 Vite 5173，代理 `/demo`；
- 测试库不得使用主库清理命令；
- 真实 Servlet 测试必须启动嵌入式 Tomcat。

---

## 6. 执行后记录字段

每项执行后补充：

```text
实际状态码
实际 handler
实际 body 摘要
实际 content-type
执行命令
执行时间
通过/失败
失败原因
```

> 规划基线为 PLANNED；2026-11-14 MS-07.4 的实际执行结果见 §7。

---

## 7. MS-07.4 实际执行结果

执行时间：2026-11-14。

后端：

```text
mvn -B -Dtest=ServletMappingIntegrationTest test
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
exit 0
```

前端：

```text
node scripts/ms07-3-browser-check.mjs
MS07_3_BROWSER_OK
exit 0
```

| 编号 | 实际结果 | 证据 |
| --- | --- | --- |
| TC-SERVLET-01 | PASS | `ServletMappingIntegrationTest#exactMapping` |
| TC-SERVLET-02 | PASS | `#exactExtraFallsToPath` |
| TC-SERVLET-03 | PASS | `#pathMapping` |
| TC-SERVLET-04 | PASS | `#pathMappingMultiLevel` |
| TC-SERVLET-05 | PASS | `#extensionMapping` |
| TC-SERVLET-06 | PASS | `#pathBeatsExtension` |
| TC-SERVLET-07 | PASS | `#springInfoIsSpringMvc` + Playwright |
| TC-SERVLET-08 | PASS | `#exactPostIsMethodNotAllowed` |
| TC-SERVLET-09 | PASS | `#pathPostIsMethodNotAllowed` |
| TC-SERVLET-10 | PASS | `#springPostIsMethodNotAllowedWithUnifiedBody` + Playwright |
| TC-SERVLET-11 | PASS | `#unmatchedPathUsesDefaultDispatcherMappingAndReturnsSpring404` + Playwright |
| TC-SERVLET-12 | PASS | `#queryDoesNotChangeExactMapping` + Playwright |
| TC-SERVLET-13 | PASS | `ms07-3-browser-check.mjs` 的真实页面请求矩阵 |

补充：

- 全量后端 `mvn clean test`：158 tests，0 failures/errors/skips，BUILD SUCCESS；
- 前端构建：`npm run build`，exit 0；
- MS04/MS05、MS06.2～MS06.9 浏览器回归均通过；
- 具体证据见 `ms-07.4-report.md`。

