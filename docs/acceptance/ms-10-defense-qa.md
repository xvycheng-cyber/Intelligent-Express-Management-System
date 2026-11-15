# MS-10 答辩问题与参考答案

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 题量 | 46 题 |
| 依据 | 当前源码、README、MS-08 证据、MS-09 报告 |
| 原则 | 只答已实现；不把计划/本地验证写成已完成/CI 通过 |

---

## A. 项目背景与需求

### A1. 为什么选择快递地址簿录入？

**推荐回答：** 快递收件信息常以多行文本出现，姓名/电话/地址混排，人工逐条拆分效率低。该场景范围清晰，适合课程设计展示解析、CRUD、前端交互和 Web 基础实践。

**依据：** `docs/requirements.md`、`docs/paper/快递地址簿录入程序设计.md`。  
**追问：** 为什么不做完整物流系统？  
**不要夸大：** 本项目不是商业快递平台，不含下单/轨迹。

### A2. 解决了什么问题？

**推荐回答：** 把多行文本规则解析为结构化联系人，提供有效/无效行提示、人工修正、确认后批量保存，减少手工拆分和误录。

**依据：** `ImportView.vue`、Parser、`AddressController`。  
**追问：** 解析一定正确吗？  
**不要夸大：** 规则解析有歧义，需人工修正，不是 100% 自动。

### A3. 核心功能是什么？

**推荐回答：** 多行录入/解析/预览/修正/保存，地址簿列表/分页/搜索/新建/编辑/单删/批删，以及 `/web-basics` 和 `/servlet-demo` 课程实践。

**依据：** README 功能章节。  
**追问：** 有登录吗？  
**不要夸大：** 没有登录/权限。

### A4. 项目边界是什么？

**推荐回答：** 课程任务（一）本机演示；无登录、AI、OCR、地图、Excel、物流下单、多租户、微服务；数据库仅本机课程演示。

**依据：** `requirements.md` 范围、README。  
**追问：** 能否直接上线？  
**不要夸大：** 不适合公网/生产。

---

## B. 系统架构

### B1. 为什么前后端分离？

**推荐回答：** Vue 负责交互与展示，Spring Boot 提供 REST API；职责分离，便于独立测试 Parser/API 和前端页面。

**依据：** `AppLayout.vue`、`api/`、`AddressController`。  
**追问：** 代价是什么？  
**不要夸大：** 增加接口契约、跨域/代理和联调成本。

### B2. 为什么使用 Spring Boot？

**推荐回答：** 快速集成 Spring MVC、Validation、MyBatis-Plus 和测试；内嵌 Tomcat 便于本机运行。

**依据：** `pom.xml`、`ExpressAddressApplication`。  
**追问：** 用了 Spring Cloud 吗？  
**不要夸大：** 没有微服务/注册中心。

### B3. 为什么使用 Vue 3？

**推荐回答：** Composition API 适合录入/预览等状态组合，Element Plus 提供表格、弹窗、表单组件，TypeScript 提供契约约束。

**依据：** `package.json`、`ImportView.vue`。  
**追问：** 用了 Pinia 吗？  
**不要夸大：** 没有全局状态库。

### B4. 前后端如何通信？

**推荐回答：** Axios 通过 Vite 代理 `/api` 到 `127.0.0.1:8080`；统一 `R{code,message,data}`；生产不在本课程范围。

**依据：** `vite.config.ts`、`src/api/`。  
**追问：** CORS 怎么处理？  
**不要夸大：** 开发期用 Vite 代理，不是生产 CORS 方案。

### B5. 为什么使用 MyBatis-Plus？

**推荐回答：** 单表 CRUD、分页、逻辑删除配置简洁，SQL 可控；复杂查询仍用显式 Mapper/XML。

**依据：** `pom.xml`、`AddressMapper`。  
**追问：** 为什么不用 JPA？  
**不要夸大：** 选型是课程项目取舍，不是唯一正确。

---

## C. 地址解析算法

### C1. 地址文本如何拆分？

**推荐回答：** 去 BOM、统一换行、按物理行切分；跳过空行但保留原始行号；分隔符支持中英文逗号、Tab、中英文分号。

**依据：** `LineSplitter.java`、`TextSegments`。  
**追问：** 空行会丢行号吗？  
**不要夸大：** 空行跳过但物理行号保留。

### C2. 如何识别姓名？

**推荐回答：** 在电话前后候选中按规则选择合法姓名；中文/英文 1～50 个 Unicode 码点；单字姓名可用；有限地址词排除。

**依据：** `NameExtractor.java`、`parser-design.md`。  
**追问：** 英文地址词会误判吗？  
**不要夸大：** 是有限启发式，不是完整地名库。

### C3. 如何识别手机号？

**推荐回答：** 优先整段匹配，再按手机/脱敏手机/座机模式，最后纯数字兜底；输出规范化；>20 位 E108，其他非法 E104。

**依据：** `PhoneExtractor.java`、`PhonePatterns`。  
**追问：** 包裹符怎么处理？  
**不要夸大：** 只有确定成对包裹符才随电话删除。

### C4. 如何处理无效行？

**推荐回答：** 每行独立返回 `status/errors`；无效行不影响其他行；保存只提交可提交行，前端显示原因并允许人工修正。

**依据：** `ParseResultTable.vue`、`AddressParserService`。  
**追问：** 会丢弃无效行吗？  
**不要夸大：** 不丢弃，保留原始行供修正。

### C5. 为什么采用规则解析而不是 AI？

**推荐回答：** 规则可解释、可测试、离线、无模型/隐私依赖，适合课程任务；AI 未实现，只在不足/展望中提及。

**依据：** `parser-design.md`、README。  
**追问：** 规则不够智能怎么办？  
**不要夸大：** 通过人工修正兜底，不宣称 AI。

### C6. 解析失败如何处理？

**推荐回答：** 返回行级 E102～E111（无 E109）；前端显示错误、保留原始值；用户修正后再保存。

**依据：** `docs/parser-design.md`。  
**追问：** 会抛 500 吗？  
**不要夸大：** 可预期解析错误走行级错误，不暴露堆栈。

### C7. 如何避免解析直接写数据库？

**推荐回答：** 解析 API 只调用 Parser；Parser 不依赖 Mapper/JDBC；保存是独立 API，用户确认后才写。

**依据：** `AddressController.parse`、`AddressParserService`。  
**追问：** 前端会自动保存吗？  
**不要夸大：** 不，保存需确认。

---

## D. 数据库设计

### D1. 为什么使用 MySQL？

**推荐回答：** 课程要求关系型数据库；MySQL 8 支持事务、索引、utf8mb4，和 MyBatis-Plus 集成成熟。

**依据：** `database-design.md`、`pom.xml`。  
**追问：** 为什么不用 NoSQL？  
**不要夸大：** 单表结构化地址不需要 NoSQL。

### D2. 主要表有哪些字段？

**推荐回答：** `express_address`：id、receiver_name、phone、full_address、create_time、update_time、is_deleted。

**依据：** `schema.sql`。  
**追问：** 为什么不拆省市区？  
**不要夸大：** 课程重点是完整地址保真，未做行政区划标准化。

### D3. 为什么使用逻辑删除？

**推荐回答：** 保留历史、避免物理删除不可逆；MyBatis-Plus 自动过滤 `is_deleted=1`。

**依据：** `application.yml`、`AddressService.delete`。  
**追问：** 数据会无限增长吗？  
**不要夸大：** 会，生产需归档策略，本项目未做。

### D4. 为什么需要索引？

**推荐回答：** `(is_deleted, create_time)` 支撑列表过滤/排序；`phone` 支撑电话搜索；避免全表扫描。

**依据：** `schema.sql`。  
**追问：** 索引越多越好吗？  
**不要夸大：** 不是，增加写成本；本项目按查询需要建。

### D5. 批量保存如何保证事务一致性？

**推荐回答：** Service `@Transactional(rollbackFor=Exception.class)`；逐条插入，任一条失败抛异常整批回滚。

**依据：** `AddressService.batchSave`。  
**追问：** 部分成功可以接受吗？  
**不要夸大：** 本项目选择整批回滚，不返回半成功。

---

## E. 前端设计

### E1. 如何实现批量录入？

**推荐回答：** 文本框收集多行文本，调用解析 API，生成可编辑预览行；有效/无效统计；保存只提交可提交行。

**依据：** `ImportView.vue`。  
**追问：** 会一次提交 200 条以上吗？  
**不要夸大：** 上限 200，超过 40003。

### E2. 如何实现人工修正？

**推荐回答：** 预览表每行可编辑姓名/电话/地址；实时字段计数和错误提示；支持恢复原始值。

**依据：** `ParseResultTable.vue`、`restoreRow`。  
**追问：** 修正后状态怎么变？  
**不要夸大：** 前端校验通过才可提交，后端仍二次校验。

### E3. 如何区分原始值和修改值？

**推荐回答：** 每行保存 `original` 快照；`isRowEdited` 比较当前字段与 original；修改计数据此计算。

**依据：** `addressPayload.ts`。  
**追问：** 修改后再恢复算修改吗？  
**不要夸大：** 恢复 original 后不算修改。

### E4. 如何防止重复提交？

**推荐回答：** 保存中禁用按钮；保存成功后记录 payload signature，同一内容再次保存提示已保存/禁用；不是分布式幂等。

**依据：** `ImportView.vue`、`payloadSignature`。  
**追问：** 能防多用户并发重复吗？  
**不要夸大：** 不能，本项目无多用户/服务端幂等键。

### E5. 地址簿如何分页？

**推荐回答：** 后端分页参数 page/size，MyBatis-Plus 返回 records/total/pages；前端显示总数、当前页、上下页。

**依据：** `AddressController.list`、`AddressListView.vue`。  
**追问：** 搜索和分页能组合吗？  
**不要夸大：** 可以，关键词和分页一起传；未做复杂排序。

---

## F. Web 基础实践

### F1. CSS 盒模型是什么？

**推荐回答：** content/padding/border/margin 四层；content-box 与 border-box 的 width 含义不同；项目全局 border-box。

**依据：** `/web-basics` 2.6～2.8、`2-06`～`2-08` 截图。  
**追问：** margin 在盒模型内吗？  
**不要夸大：** margin 在边框盒外，且可能折叠。

### F2. 常规流与定位有什么区别？

**推荐回答：** static 按文档流占位；relative 保留占位偏移；absolute 脱流按定位祖先；fixed 通常按视口；sticky 在滚动约束内吸附。

**依据：** 2.9～2.17 页面。  
**追问：** absolute 一定相对父元素吗？  
**不要夸大：** 相对最近有效定位祖先，不一定是直接父元素。

### F3. 如何实现页面布局？

**推荐回答：** AppLayout 用 Flex 分侧栏/工作区，Grid/Flex 分内容；不拿 absolute 搭整页；窄屏切换导航。

**依据：** `AppLayout.vue`、2.19。  
**追问：** 用了浮动吗？  
**不要夸大：** 没有用浮动搭整页。

### F4. 2.1～2.19 实践内容是什么？

**推荐回答：** display、盒模型、Flex/Grid、常规流、五种定位、sticky 约束、offset 覆盖、2.18 线框、2.19 布局原则。

**依据：** `WebBasicsView.vue`、MS06 报告。  
**追问：** 2.18 是仿制外部网页吗？  
**不要夸大：** 参考图来自任务书附录4；实现是经批准的快递通原创线框，不复制外部品牌/内容。

---

## G. Servlet 映射

### G1. Servlet 是什么？

**推荐回答：** Java Web 容器管理的请求处理组件；本项目三个 `@WebServlet` 用于演示精确/路径/扩展名映射。

**依据：** `servlet/`。  
**追问：** 和 Controller 一样吗？  
**不要夸大：** 层次不同；Spring MVC Controller 由 DispatcherServlet 分派。

### G2. Servlet 容器如何映射请求？

**推荐回答：** 精确 > 最长路径前缀 > 扩展名 > 默认 `/`；容器按 URL Pattern 选 Servlet。

**依据：** `ServletMappingIntegrationTest`、2.20 矩阵。  
**追问：** `/demo/servlet/legacy/report.smap` 命中谁？  
**不要夸大：** 路径优先于扩展名，命中 PathMappingDemoServlet。

### G3. Servlet 与 Spring MVC 有什么区别？

**推荐回答：** Servlet 是容器级组件；Spring MVC 的 DispatcherServlet 是默认 `/` 的 Servlet，再由 HandlerMapping 选 Controller；两者不是同一层规则。

**依据：** `ServletMappingDemoController`、架构文档。  
**追问：** 能说 Tomcat 直接映射 @GetMapping 吗？  
**不要夸大：** 不能；先到 DispatcherServlet。

### G4. GET、HEAD、OPTIONS 有什么区别？

**推荐回答：** GET 取资源；HEAD 同 GET 头但无响应体；OPTIONS 查询支持的方法/能力；本项目 HEAD/OPTIONS 200，POST 405。

**依据：** R1 实测和 160 tests。  
**追问：** HEAD 有 body 吗？  
**不要夸大：** 不应有响应体。

### G5. 405 响应中的 Allow 有什么作用？

**推荐回答：** 告知客户端该资源实际支持的方法；本项目 POST 405 返回 `Allow: GET, HEAD, OPTIONS`。

**依据：** `MappingDemoServletSupport.service`、`GlobalExceptionHandler`。  
**追问：** 为什么以前 OPTIONS 广告 POST？  
**不要夸大：** 旧实现覆写 doPost 导致；R1 改为 service 边界拦截并修复。

---

## H. 测试与性能

### H1. 项目进行了哪些测试？

**推荐回答：** Parser 单测、Controller/MockMvc、真实 HTTP、MySQL 集成、DTO/Unicode/LIKE、Playwright、E2E、Chrome/Edge、性能、405/HEAD/OPTIONS。

**依据：** `test-plan.md`、`docs/test-results/ms-08/`。  
**追问：** 都本次跑的吗？  
**不要夸大：** 160/浏览器/性能是 MS-08 已执行证据，本轮未重跑全部。

### H2. 如何理解 160 个后端测试？

**推荐回答：** Surefire 12 个报告汇总 160 tests，0 failures/errors/skips；包含 Servlet 14 个；不是 160 个业务接口。

**依据：** `backend/target/surefire-reports`。  
**追问：** 没有 MySQL 也能 160 吗？  
**不要夸大：** MySQL 集成需要环境；跳过不等于 160 全通过。

### H3. Playwright 测试覆盖了什么？

**推荐回答：** MS04 录入、MS05 地址簿、MS06 2.1～2.19、MS07 2.20、核心 E2E、Chrome/Edge 宽度矩阵。

**依据：** `frontend/scripts/`、`MS08_EDGE_OK`。  
**追问：** CI 跑了吗？  
**不要夸大：** 没有，CI NOT RUN。

### H4. 如何解释性能测试的 P95？

**推荐回答：** 30 个有效样本（预热 2 次）的第 95 百分位；100 行解析 P95 2.81ms；10000 条数据集、pageSize=10 分页 P95 3.59ms；本机结果，不是生产承诺。

**依据：** `perf-results.json`。  
**追问：** 是一次返回 10000 条吗？  
**不要夸大：** 不是，每页 10 条。

### H5. 测试环境和生产环境有什么区别？

**推荐回答：** 本机单机、固定数据、无并发/网络/真实用户；生产需容量、并发、监控、安全和 CI。

**依据：** 测试报告边界。  
**追问：** 能保证线上 3ms 吗？  
**不要夸大：** 不能。

---

## I. 安全与不足

### I1. 为什么系统没有登录功能？

**推荐回答：** 课程任务（一）不含认证；系统仅本机演示；没有实现就不能声称有权限控制。

**依据：** `requirements.md`、README。  
**追问：** 没有登录安全吗？  
**不要夸大：** 不安全，不能公网部署。

### I2. 为什么不建议公网部署？

**推荐回答：** 无认证/权限、无 HTTPS/审计/限流，数据库 root 本机演示，且 D08 未处置。

**依据：** README 安全章节。  
**追问：** 加个密码就能上线吗？  
**不要夸大：** 还需要完整安全设计和加固。

### I3. D08 是什么？

**推荐回答：** MySQL X Plugin 33060 全接口监听 + mysqld 宽泛防火墙规则的潜在暴露；项目只用 JDBC 3306；P1 OPEN。

**依据：** `ms-08-defect-ledger.md`、R2 指南。  
**追问：** 已经外部登录成功了吗？  
**不要夸大：** 未从第二台物理设备验证，不能声称。

### I4. MySQL X Plugin 33060 有什么风险？

**推荐回答：** 33060 对全部接口监听，防火墙存在 Any 允许规则；虽然账号仅 localhost，仍是额外攻击面；建议禁用或限制本机并收紧规则。

**依据：** R1/R2 报告。  
**追问：** 为什么没修？  
**不要夸大：** 用户未授权修改 MySQL/防火墙；本轮禁止。

### I5. 当前有哪些未完成事项？

**推荐回答：** D08 P1 OPEN、AC-30 PARTIAL、MS-09 封面/2.21 四图/PDF 目录、CI NOT RUN、无认证；MS-10 的 CRUD/5-10 核心流程已完成，但项目正式验收仍受这些前置项限制。

**依据：** 验收矩阵、MS-09 报告。  
**追问：** 项目能正式验收吗？  
**不要夸大：** 不能，当前只能 PARTIAL。

### I6. 如果继续开发，下一步是什么？

**推荐回答：** 用户授权后处置 D08；补 MS-09 封面/四张真实截图；补 CI；再考虑认证/部署。MS-10 的 CRUD/5-10 演示已完成，不再列为待办。

**依据：** MS-10 遗留清单。  
**追问：** 是先做 AI 吗？  
**不要夸大：** 不是，安全和证据优先；AI 未实现。

---

## 附：答辩表达底线

- 不把“本地通过”说成“CI 通过”；
- 不把“规则解析”说成“AI”；
- 不把“本机演示”说成“生产系统”；
- 不把“计划”说成“已实现”；
- 不隐瞒 D08/AC-30/MS-09/CI。
