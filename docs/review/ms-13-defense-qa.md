# MS-13 答辩问题库（30 题）

| # | 类别 | 问题 | 简要回答 | 详细回答/依据 | 局限 |
| ---: | --- | --- | --- | --- | --- |
| 1 | 需求 | 为什么做快递地址簿？ | 多行文本录入效率低 | 将姓名/电话/地址规则解析、人工修正、确认保存 | 非商业物流系统 |
| 2 | 需求 | 项目边界？ | 任务（一）本机课程演示 | 无登录/AI/OCR/地图/Excel/物流下单 | 不公网部署 |
| 3 | 架构 | 为什么前后端分离？ | Vue 交互，Spring Boot API | Vue→Axios→Controller→Service→Mapper→MySQL | 契约/联调成本 |
| 4 | 架构 | Controller 能直接访问 Mapper 吗？ | 不能 | Controller 只调用 Service | 课程分层约束 |
| 5 | 架构 | Parser 的职责？ | 纯文本规则解析 | 不依赖 Mapper/JDBC/IO | 有歧义需人工修正 |
| 6 | 前端 | Vue 3 用了什么？ | Composition API + TS | Api/组件/路由/Element Plus | 无 Pinia |
| 7 | 前端 | 原始值/修改值如何区分？ | original 快照 + isRowEdited | `addressPayload.ts` | 前端级 |
| 8 | 前端 | 如何防重复提交？ | 保存中禁用 + signature | `ImportView.vue` | 非服务端幂等 |
| 9 | 后端 | Spring Boot 版本？ | 3.3.5 | `pom.xml` | — |
| 10 | 后端 | 批量保存事务？ | 整批插入、异常回滚 | `AddressService.batchSave` | 无分布式事务 |
| 11 | 数据库 | 主表？ | `express_address` | id/name/phone/address/时间/is_deleted | 单表 |
| 12 | 数据库 | 为什么逻辑删除？ | 保留历史、避免物理删除 | MyBatis-Plus is_deleted | 生产需归档 |
| 13 | 数据库 | 索引？ | is_deleted+create_time、phone | `schema.sql` | — |
| 14 | 解析 | 如何分行/保留行号？ | 去BOM/统一换行/按行 | `LineSplitter` | 空行跳过但保留行号 |
| 15 | 解析 | 电话如何识别？ | 整段优先、手机/脱敏/座机/数字兜底 | `PhoneExtractor` | 规则有限 |
| 16 | 解析 | 地址如何保真？ | 删除姓名/电话区间，保留内部标点 | `AddressExtractor` | 不标准化 |
| 17 | 解析 | 姓名长度？ | 1～50 Unicode 码点 | codePointCount/前端 Array.from | 单字可用 |
| 18 | 解析 | 错误码？ | E102～E108、E110、E111 | 无 E109 | 行级错误 |
| 19 | 解析 | 为什么要人工修正？ | 规则无法覆盖所有歧义 | 预览/恢复/确认保存 | 不承诺100% |
| 20 | API | 解析与保存为何分离？ | 解析不写库，确认后保存 | `/parse` 与 `/batch` | 两次请求 |
| 21 | API | 40001/40003 区别？ | 字段错误 vs 数量/长度超限 | `violations`/`items.errors` | — |
| 22 | API | LIKE 特殊字符？ | `!`/`%`/`_` 转义 | LikeEscaper + `ESCAPE '!'` | — |
| 23 | Servlet | 容器映射顺序？ | 精确>路径前缀>扩展名>默认 | MS07 矩阵 | Spring 是二次匹配 |
| 24 | Servlet | 405 Allow？ | 返回实际支持方法 | GET/HEAD/OPTIONS | 已修复 |
| 25 | Web | 盒模型？ | content/padding/border/margin | 全局 border-box | — |
| 26 | Web | 定位？ | relative/absolute/fixed/sticky | 2.9～2.17 | 2.18 原创线框 |
| 27 | 测试 | 160 tests 是什么？ | 后端全量 | 12 Surefire XML | 本轮实际重跑 |
| 28 | 测试 | 浏览器测试？ | MS04/05/06/07/E2E/Edge | Playwright | MS05 需清数据 |
| 29 | 测试 | 性能 P95？ | 解析2.81ms/分页3.59ms | 30样本/历史 | 非生产承诺 |
| 30 | 安全 | D08 是什么？ | 33060/防火墙风险 | P1 OPEN/USER DEFERRED | AC-30 PARTIAL |

---

## 答辩底线

- 不把本地测试说成 CI；
- 不把规则解析说成 AI；
- 不把本机演示说成生产；
- 不隐瞒 D08/AC-30/封面 deferred。
