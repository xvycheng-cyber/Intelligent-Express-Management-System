# MS-11 项目最终交付清单

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| HEAD | `ccb52c269569466bdfcd91d4cee5a67f7678251a` |
| 状态词 | COMPLETE / PARTIAL / MISSING / BLOCKED / NOT APPLICABLE / USER DEFERRED |
| 项目整体 | **NOT READY** |

---

## 1. 源码

| 交付项 | 状态 | 证据/说明 |
| --- | --- | --- |
| 后端源码 | COMPLETE | `backend/` |
| 前端源码 | COMPLETE | `frontend/` |
| Parser | COMPLETE | `backend/.../parser/` |
| 地址 Controller/Service/Mapper | COMPLETE | `AddressController`、`AddressService`、`AddressMapper` |
| Servlet 演示 | COMPLETE | `servlet/`、`demo/` |
| Web 基础页面 | COMPLETE | `WebBasicsView`、`WireframeView`、`basics/` |
| 构建配置 | COMPLETE | `pom.xml`、`package.json`、lock |

## 2. 数据库脚本

| 交付项 | 状态 | 证据 |
| --- | --- | --- |
| `schema.sql` | COMPLETE | 建库/表/索引 |
| `data.sql` | COMPLETE | 可选脱敏数据 |
| 逻辑删除映射 | COMPLETE | `application.yml`、实体/Service |
| 账号/口令方案 | PARTIAL | 本地 root 演示；未做生产账号方案 |

## 3. 运行说明

| 交付项 | 状态 |
| --- | --- |
| README | COMPLETE |
| 环境清单 | COMPLETE |
| 从零复现手册 | COMPLETE |
| 数据安全说明 | COMPLETE |

## 4. 测试

| 交付项 | 状态 | 说明 |
| --- | --- | --- |
| Maven 160 tests | COMPLETE（历史证据） | 2026-11-15 Surefire，12 XML |
| 前端构建 | COMPLETE | `npm run build` |
| Playwright Chrome | COMPLETE | MS04/05/06/07/E2E |
| Edge | COMPLETE | `MS08_EDGE_OK` |
| 性能 | COMPLETE | 30 样本/隔离 Schema |
| GitHub Actions CI | **NOT RUN / USER DEFERRED** | 无远端执行证据；课程原始要求/AC-32 为 P2 增强，不是硬性阻塞 |
| MS-10 CRUD/UI | COMPLETE | `docs/test-results/ms-10/` |

## 5. 截图/证据

| 交付项 | 状态 |
| --- | --- |
| 2.1～2.17 | COMPLETE |
| 2.18 原图/线框/实现 | COMPLETE（原图来源任务书附录4） |
| 2.19 | COMPLETE |
| 2.20 页面/示意 | COMPLETE |
| 2.21 录入/解析/保存确认/测试记录 | COMPLETE |
| 2.21 地址列表/编辑/删除/批删 | COMPLETE（MS-12） |
| 测试证据索引 | COMPLETE |

## 6. 课程论文

| 交付项 | 状态 |
| --- | --- |
| Markdown 正文 | COMPLETE |
| 第1章 1.1～1.4 | COMPLETE |
| 第2章 2.1～2.21 | COMPLETE |
| Word | COMPLETE（68 页，目录已更新） |
| PDF | COMPLETE（68 页，目录/页码已更新） |
| 封面/个人信息 | **USER DEFERRED** |
| 2.21 四图同步论文 | COMPLETE（MS-12） |

## 7. 答辩与运行材料

| 交付项 | 状态 |
| --- | --- |
| 5/10 分钟脚本 | COMPLETE（功能步骤已实测；时长非现场） |
| 46 题 Q&A | COMPLETE |
| 故障手册 | COMPLETE |
| 证据索引 | COMPLETE |
| 数据安全 | COMPLETE |

## 8. 安全与验收

| 交付项 | 状态 |
| --- | --- |
| D08 | **P1 OPEN / USER DEFERRED** |
| AC-30 | **PARTIAL** |
| 项目正式验收 | **NOT READY** |
| 项目整体 ACCEPTED | **不可用/无证据** |

## 9. 总结

- **COMPLETE**：源码、数据库脚本、运行文档、测试证据（本地）、2.1～2.20 截图、MS-10 材料；
- **PARTIAL**：论文 Word/PDF 格式、账号生产方案；
- **MISSING/BLOCKED**：2.21 四张业务截图；
- **USER DEFERRED**：D08、封面/个人信息、GitHub/CI、远程上传；
- **整体**：NOT READY。

**不得把 USER DEFERRED 或 MISSING 写成 COMPLETE。**
