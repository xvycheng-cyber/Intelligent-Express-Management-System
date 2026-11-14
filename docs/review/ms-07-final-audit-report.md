# MS-07 全阶段统一正式审查报告

| 项目 | 内容 |
| --- | --- |
| 审查日期 | 2026-11-14 |
| 审查范围 | MS-07.1～MS-07.5、课程 2.20、Servlet 后端、前端页面、测试、截图、文档 |
| 审查性质 | 独立只读正式审查；只新增本报告 |
| 最终结论 | **A. 通过** |
| 允许正式结束 MS-07 | **允许**（待 ChatGPT 独立复核和用户确认） |
| 允许进入 MS-08 | **允许**（待前述确认） |

---

## 1. 审查基本信息

```text
工作目录：E:\desktop\高级Web&J2EE期末
仓库：Intelligent-Express-Management-System
分支：main
HEAD：6dd2bdbfdb1ca912ccabee3b0f9acee5c1fdca27
```

审查方式：

- 读取实际源码、路由、代理、测试、截图和全部 MS-07 文档；
- 检查真实 Spring Boot/Tomcat 启动和 HTTP 行为；
- 实际执行 `ServletMappingIntegrationTest`；
- 实际执行 `/servlet-demo` Playwright、MS06.2～MS06.9 回归；
- 实际执行前端生产构建；
- 审查截图内容/尺寸/重复性；
- 核对 Git 和文档一致性。

本审查没有修改任何源码、测试、截图、数据库或已有报告。

---

## 2. Git 和工作区状态

### 2.1 基线

```text
HEAD：6dd2bdbfdb1ca912ccabee3b0f9acee5c1fdca27（6dd2bdb，ms06）
```

MS-07 的 Servlet、页面、测试、截图和文档全部处于未提交工作区；没有找到 MS-07 开始前的独立提交基线。不能把某个历史提交冒充为 MS-07 的准确起点。

### 2.2 当前工作区

存在：

- MS-07.2 后端 Servlet/Controller/测试；
- MS-07.3 前端 `/servlet-demo`、路由、导航、代理；
- MS-07.1～07.5 文档；
- 2-20 三张截图；
- R1/MS-06 历史整改；
- `docs/handover/` 的既有删除/未跟踪状态。

这些状态在审查开始时已存在，未由本次审查清理或归因。

### 2.3 未提交范围

- 未发现 MS-07 修改 AddressController/Service/Mapper、数据库结构或业务 API；
- 发现为支持真实导航，MS06.2/3/7/9 测试做了最小适配（Servlet 入口从“不存在/待实现”变为真实页面）；
- 未发现 MS-07 对 MS-06 课程内容原理的修改。

### 2.4 Git 操作

本次未执行 commit/push/reset/clean/checkout/restore/stash。

---

## 3. 课程任务书 2.20 原文核对

任务书 docx 实际内容：

```text
2.20 | Servlet与请求URI的映射规则
```

任务书开头：

```text
一次请求是按照一个怎样的规则从URI映射到Servlet程序的？
```

第1章实践主题：

```text
（四）Servlet与请求URI的映射规则
```

### 对照表

| 课程原文要求 | 项目实现 | 证据 | 结论 |
| --- | --- | --- | --- |
| 2.20 主题：Servlet 与请求 URI 映射规则 | 三个真实 `@WebServlet` + 规则说明 | `servlet/*`、`/servlet-demo` | 满足 |
| 解释一次请求如何从 URI 映射到 Servlet | 容器映射与 Spring MVC 两阶段说明 | `ServletDemoView`、`ms-07.2-report` | 满足 |
| 未规定必须的 URL Pattern/类名/扩展名 | 项目选择 exact/path/`*.smap` + Spring 对照 | `ms-07-scope-adjustment` | 项目设计，已标注 |

**重要边界：**

- `*.smap`、三个具体类名、Spring MVC 对照、请求矩阵、404/405 展示是项目设计推导；
- 课程原文只有 2.20 主题和 URI→Servlet 规则问题；
- 报告和实现均没有把项目扩展伪称为课程原文。

---

## 4. MS-07 五阶段完成情况

| 阶段 | 实际产物 | 审查结论 |
| --- | --- | --- |
| MS-07.1 | 审计/计划/范围/测试追踪四文档 | 完成；课程原文与项目设计区分清楚 |
| MS-07.2 | 三个 Servlet、Spring Controller、12 个真实 HTTP 测试 | 完成；真实 Tomcat 注册和命中已验证 |
| MS-07.3 | `/servlet-demo`、真实 fetch、路由/导航/代理 | 完成；页面真实请求 |
| MS-07.4 | TC-SERVLET-01～013、回归记录 | 完成；测试追踪表已更新 |
| MS-07.5 | 三张 2.20 截图、最终交付报告、进度/追踪更新 | 完成；最终报告存在 |

说明：仓库实际使用 `ms-07-final-delivery-report.md` 作为 MS-07.5 交付报告，没有单独的 `ms-07.5-report.md`。这不是功能缺失，但属于文档命名差异（P2）。

---

## 5. 后端 Servlet 注册审查

### 5.1 启动类

`backend/src/main/java/com/express/addressbook/ExpressAddressApplication.java`：

```java
@SpringBootApplication
@ServletComponentScan(basePackages = "com.express.addressbook.servlet")
```

### 5.2 三个 Servlet

```text
ExactMappingDemoServlet       @WebServlet("/demo/servlet/exact")
PathMappingDemoServlet        @WebServlet("/demo/servlet/*")
ExtensionMappingDemoServlet   @WebServlet("*.smap")
```

检查结果：

- 没有 `ServletRegistrationBean`；
- 没有第二套注册机制；
- 没有重复注册；
- 没有新增自定义 `/`；
- 没有改变 `DispatcherServlet` 映射；
- 真实 `/demo/servlet/exact` 返回 `ExactMappingDemoServlet`；
- 真实 `/demo/servlet/prefix/a` 返回 `PathMappingDemoServlet`；
- 真实 `/demo/legacy/report.smap` 返回 `ExtensionMappingDemoServlet`。

**结论：注册机制真实有效。**

---

## 6. 三种映射真实性审查

### 6.1 精确映射

- 请求：`GET /demo/servlet/exact`
- 实际：200，`ExactMappingDemoServlet`
- `servletPath=/demo/servlet/exact`
- `pathInfo=null`
- `GET /demo/servlet/exact/extra` 不命中 Exact，落到 Path。

### 6.2 路径映射

- `GET /demo/servlet/prefix/a` → 200，Path
- `GET /demo/servlet/prefix/a/b` → 200，Path
- `servletPath=/demo/servlet`
- `pathInfo=/prefix/a`、`/prefix/a/b`

### 6.3 扩展名映射

- `GET /demo/legacy/report.smap` → 200，Extension
- `servletPath=/demo/legacy/report.smap`
- `pathInfo=null`
- 没有用 Controller 伪造。

**结论：三种映射均为真实容器 Servlet 映射。**

---

## 7. 映射优先级审查

证据来自 `ServletMappingIntegrationTest` 和真实页面矩阵：

| 请求 | 参与 pattern | 实际命中 |
| --- | --- | --- |
| `/demo/servlet/exact` | Exact + Path | Exact |
| `/demo/servlet/exact/extra` | Path | Path |
| `/demo/servlet/prefix/a` | Path | Path |
| `/demo/servlet/prefix/a/b` | Path | Path |
| `/demo/legacy/report.smap` | Extension | Extension |
| `/demo/servlet/legacy/report.smap` | Path + Extension | Path（路径优先） |
| `/demo/legacy/report.json` | 默认 `/` | DispatcherServlet → 404 |

结论：

- 精确优先；
- 最长路径匹配；
- 扩展名匹配；
- 路径与扩展名冲突时路径优先；
- 符合当前 Tomcat/Servlet URL Pattern 行为。

---

## 8. Spring MVC 对照审查

`ServletMappingDemoController`：

```text
GET /demo/spring/info
GET /demo/spring/echo/{name}
GET /demo/spring/method-only
```

实际 `/demo/spring/info`：

```json
{
  "handler": "ServletMappingDemoController",
  "mappingType": "SPRING_MVC",
  "controllerMethod": "info",
  "requestUri": "/demo/spring/info"
}
```

与 Servlet 的 `mappingType`/`handler` 明确区分。

容器映射 / DispatcherServlet / Spring HandlerMapping 的关系说明正确：

- 容器先匹配 URL Pattern；
- 请求进入 DispatcherServlet；
- Spring MVC 再匹配 Controller。

---

## 9. 默认映射边界审查

- Spring Boot 未设置 `spring.mvc.servlet.path`，默认 `/`；
- 没有新增 `@WebServlet("/")`；
- `/demo/legacy/report.json` 不匹配自定义 Servlet，进入默认 `/` 的 DispatcherServlet；
- 无 Handler → HTTP 404 + `R.code=40402`；
- 不是自定义默认 Servlet 的响应；
- 没有破坏 `/api/**` 和现有路由。

**结论：默认映射边界说明和实现一致。**

---

## 10. 404 / 405 审查

### 405

- `POST /demo/servlet/exact` → 405
- `POST /demo/servlet/prefix/a` → 405
- `POST /demo/spring/method-only` → 405 + `R.code=40501`
- 没有被改成 200；
- 自定义 Servlet 的 405 由 `HttpServlet` 方法分派/Spring Boot `/error` 输出；Spring 405 由全局异常处理输出。

### 404

- `GET /demo/legacy/report.json` → 404 + `R.code=40402`
- 没有错误命中扩展名 Servlet；
- 前端读取真实 404，不把错误当成功。

### P2 观察

实际 405 响应没有 `Allow` 头（自定义 Servlet 和 Spring 全局异常均未设置）。这不影响本课程演示的状态/命中核心，但 HTTP 语义上可改进。

---

## 11. 前端页面审查

实际页面：`frontend/src/views/ServletDemoView.vue`

包含：

- 规则说明；
- 三种 Servlet 映射；
- Spring MVC 对照；
- 优先级流程；
- 真实请求矩阵；
- 响应状态/耗时/Content-Type/headers/body；
- 405/404/默认边界；
- 清空/全部/单条。

请求实现：

- `frontend/src/api/servletDemo.ts` 使用原生 `fetch`；
- 不被 Axios `/api` 错误拦截器转换状态；
- 真实 `/demo/**`；
- 页面不调用 `/api/addresses`；
- 无 Mock 数据/固定结果。

### 交互

Playwright 实际通过：

- 单条执行；
- 全部执行；
- 清空；
- 刷新；
- 未知路由 404；
- 请求中的禁用状态。

### 导航

- 路由 `/servlet-demo`；
- AppLayout 入口 active；
- PracticeNav 2.20 入口已实现；
- `/web-basics`、`/web-basics/wireframe` 正常；
- 未知子路径仍为 app 404。

### 视觉与响应式

- 延续深海军蓝侧栏、浅灰背景、白色卡片、蓝色强调；
- 无外部字体/资源/新依赖；
- 1366/980/760/390 无页面级横向溢出；
- 390 下矩阵内部滚动，页面可读。

---

## 12. TC-SERVLET-01～013 逐项审查

| 编号 | 测试 | 实际断言/结果 |
| --- | --- | --- |
| 01 | `exactMapping` | 200 / Exact / servletPath / pathInfo null |
| 02 | `exactExtraFallsToPath` | 200 / Path / `/exact/extra` |
| 03 | `pathMapping` | 200 / Path / `/prefix/a` |
| 04 | `pathMappingMultiLevel` | 200 / Path / `/prefix/a/b` |
| 05 | `extensionMapping` | 200 / Extension / `*.smap` |
| 06 | `pathBeatsExtension` | 200 / Path 优先 |
| 07 | `springInfoIsSpringMvc` | 200 / SPRING_MVC / controllerMethod |
| 08 | `exactPostIsMethodNotAllowed` | 405 |
| 09 | `pathPostIsMethodNotAllowed` | 405 |
| 10 | `springPostIsMethodNotAllowedWithUnifiedBody` | 405 / 40501 |
| 11 | `unmatchedPath...Spring404` | 404 / 40402 |
| 12 | `queryDoesNotChangeExactMapping` | 200 / Exact / query |
| 13 | `ms07-3-browser-check.mjs` | 页面真实请求矩阵/404/405/全部/清空/刷新 |

**TC-13 不是仅检查标题**；它实际点击按钮并验证状态/handler/body/响应式。

---

## 13. 自动化测试和构建结果

### 本次实际执行

```text
mvn -B -Dtest=ServletMappingIntegrationTest test
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
exit 0

node scripts/ms07-3-browser-check.mjs
MS07_3_BROWSER_OK
exit 0

MS06_2_BROWSER_OK
MS06_3_BROWSER_OK
MS06_4_BROWSER_OK
MS06_5_BROWSER_OK
MS06_6_BROWSER_OK
MS06_7_BROWSER_OK
MS06_8_BROWSER_OK
MS06_9_BROWSER_OK

npm run build
✓ built in 5.17s
exit 0
```

历史报告中的 158 tests 全量结果作为历史证据保留；本次未重复全量 Maven，因为测试库有 38 条既有测试数据，完整套件会进行测试数据清理，审查要求不允许擅自删除数据。该限制记录在 §17。

---

## 14. MS04/MS05/MS06 回归

### MS06

本次实际执行 MS06.2～MS06.9，全部 `BROWSER_OK`；MS-07 没有破坏 Web 基础实践页、2.18 线框、2.19 高亮。

### MS04/MS05

**本次未执行**。

原因：

- 审计开始时隔离测试库 `express_address_book_test` 有 38 行既有数据；
- MS05 脚本按 `199100` 前缀清理/种入 11 行，并断言总数 11；
- 在 38 行既有数据存在时执行会失败或需要删除既有测试数据；
- 审查要求不得擅自清理数据库；
- 因此记录为未执行，而不是伪造通过。

历史 MS-07.4 阶段曾实际执行 MS04/MS05 并记录通过；该结果只能作为历史证据，不能冒充本次执行。

主库 `express_address_book` 为 0 行，未查询/修改业务数据。

---

## 15. 截图审查

| 截图 | 尺寸 | 内容 | 结论 |
| --- | --- | --- | --- |
| `2-20-servlet-mapping.png` | 1366×2239 | 完整页面、规则、三映射、矩阵、404 详情、边界 | 有效 |
| `2-20-servlet-mapping-requests.png` | 1066×753 | 完整真实请求矩阵、200/405/404、实际 handler | 有效 |
| `2-20-servlet-mapping-http.png` | 1066×393 | 404 响应头/正文和边界 | 有效 |

- 三张均实际解码；
- 来自真实浏览器；
- 有意的 404/405 状态未被修改成 200；
- 无手工拼接；
- 没有用旧图冒充；
- 截图文件未发现重复哈希。

---

## 16. 文档一致性审查

| 文档 | 结论 |
| --- | --- |
| `ms-07-pre-audit-report.md` | 课程原文/项目设计/现状区分清楚 |
| `ms-07-implementation-plan.md` | 已更新为 MS-07 完成状态 |
| `ms-07-scope-adjustment.md` | `.smap`、GET-only、默认 `/` 决策有记录 |
| `ms-07-test-traceability.md` | 01～13 已记录 PASS |
| `ms-07.2-report.md` | 后端实现/HTTP 结果与源码一致 |
| `ms-07.3-report.md` | 页面/路由/真实请求与源码一致 |
| `ms-07.4-report.md` | 测试/回归记录；数据库影响已说明 |
| `ms-07-final-delivery-report.md` | 16 项交付内容完整；没有把项目设计写成课程原文 |
| `development-progress.md` | 添加 MS-07.5 当前状态覆盖层；历史审计保留 |
| `PROJECT_CONTEXT.md` | `/web-basics`、`/servlet-demo` 状态已同步 |
| `ms-06-implementation-plan.md` | 2.20 已标为 TESTED（MS-07.5） |

### 文档差异

- 没有独立 `ms-07.5-report.md`；实际用 `ms-07-final-delivery-report.md` 承担 07.5 交付。P2 命名差异。
- `development-progress.md` 的 §1～§8 仍是 MS-05 时期历史快照，但顶部 §0 明确覆盖；不应把历史 NOT_STARTED 当当前事实。

---

## 17. 数据库安全与测试影响

审计开始时：

```text
test_total=38
main_total=0
```

本次执行：

- `ServletMappingIntegrationTest`：12 个，不访问地址表；
- `/servlet-demo` Playwright：不调用地址 API；
- MS06：不写业务表；
- 没有执行 MS04/MS05，避免在 38 行测试数据上删除/写入；
- 没有执行 `DELETE`/`TRUNCATE`/`DROP`/初始化；
- 主库未修改；
- 测试库计数未因本次审查改变（仍为 38）。

---

## 18. 问题清单

### P0

无。

### P1

无。

### P2

1. **405 响应缺少 `Allow` 头**
   - 位置：自定义 Servlet 405、Spring 全局 405
   - 实际：状态正确 405，但响应头无 `Allow`
   - 影响：HTTP 语义可改进，不影响本课程核心状态演示
   - 建议：在后续优化中统一设置 `Allow: GET`

2. **MS04/MS05 本次未执行**
   - 原因：测试库 38 行既有数据，MS05 脚本假定 11 行并会清理测试前缀
   - 影响：本次没有重新验证业务回归；历史 MS-07.4 有通过记录
   - 建议：在专用空测试库/明确授权下复跑

3. **缺少独立 `ms-07.5-report.md`**
   - 实际：使用 `ms-07-final-delivery-report.md`
   - 影响：命名与提示词不完全一致，不影响交付内容
   - 建议：后续文档可保持最终报告作为 07.5 报告，或补一个索引文件

4. **全量 Maven 158 未在本次审计重跑**
   - 原因：测试库数据安全；仅执行 12 个 Servlet 集成测试
   - 历史报告有 158 PASS；本次不把它写成新执行结果

5. **自定义 Servlet 405 正文来自 Spring Boot `/error`**
   - 页面已如实显示；不是成功 JSON，不影响状态正确性

### P3

- Playwright 未纳入 CI；
- 主 chunk/Sass legacy warning；
- 390px 矩阵内部滚动；
- 2.20 截图是三张支持性图而非单张；
- 截图主图高 2239px，适合完整证据但不适合单页论文排版，可按论文排版拆分使用。

---

## 19. 最终审查结论

# A. 通过

依据：

- 课程 2.20 的真实 Servlet 映射已完成；
- `@ServletComponentScan` + `@WebServlet` 真实注册；
- 三种映射、优先级、Spring MVC 对照、默认 `/`、405/404 均通过实际 HTTP/Playwright 验证；
- TC-SERVLET-01～013 完整；
- 前端页面真实请求，无 Mock；
- 截图真实、可解码、无重复；
- 文档/追踪表/进度文档已同步；
- 未发现 P0/P1；
- 未修改业务 API/数据库/主库数据；
- 本次审查只新增本报告。

---

## 20. 是否允许正式结束 MS-07

**允许正式结束 MS-07。**

依据：核心 2.20 验收标准已满足；P2 项不阻断。

---

## 21. 是否允许进入 MS-08

**允许进入 MS-08**，但需以 ChatGPT 独立复核和用户确认为准。

本次审查不自行宣布 MS-07 最终验收通过。

---

## 22. 后续整改建议

1. 在后续 HTTP 优化中为 405 增加 `Allow` 头；
2. 在专用空测试库/明确授权下复跑 MS04/MS05 与全量 Maven；
3. 保持 `ms-07-final-delivery-report.md` 作为 07.5 交付索引，必要时补命名说明；
4. 将 Playwright 纳入 CI；
5. 论文截图可按主图、矩阵、404 详情分别排版。

**审查结束。未修改任何业务源码、测试、截图、数据库或已有报告，只新增本报告。**
