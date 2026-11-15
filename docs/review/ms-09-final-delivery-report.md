# MS-09 论文与截图最终交付报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 分支 / HEAD | `main` / `4a37f78975f8f4cda3f6131d7e774a9f3714238c` |
| 范围 | 课程论文、真实截图、图表、Word/PDF、附录、自审 |
| 性质 | 论文与截图交付；不是新功能开发 |
| 结论 | **内容完整度较高，但为 PARTIAL：封面信息待填、2.21 四个业务截图 BLOCKED、PDF 目录为可更新域占位** |

---

## 1. 基本信息

- 论文题目：**快递地址簿录入程序设计**
- 系统名称：快递通——智能快递地址簿录入与管理系统
- 仓库标识：`Intelligent-Express-Management-System`
- 对应课程设计任务（一）
- Git：`main`，HEAD `4a37f78`；本轮未 commit/push/reset/clean/checkout/stash。
- 未修改 Java/Vue/TypeScript/SQL/MySQL 配置/防火墙/数据库。

---

## 2. 论文交付物

| 交付物 | 路径 | 是否存在 | 说明 |
| --- | --- | --- | --- |
| Markdown 源稿 | `docs/paper/快递地址簿录入程序设计.md` | 是 | 完整章节 |
| Word | `deliverables/MS-09/快递地址簿录入程序设计.docx` | 是 | 67 页；含图、表、页脚页码 |
| PDF | `deliverables/MS-09/快递地址簿录入程序设计.pdf` | 是 | 67 页；由最终 docx 转换 |
| 图片 | `docs/screenshots/` | 是 | 2.1～2.20 与 2.21 部分 |
| 图表清单 | `deliverables/MS-09/figure-list.md` | 是 | 含 BLOCKED 标记 |
| 表清单 | `deliverables/MS-09/table-list.md` | 是 | 8 张表 |
| 参考文献 | `docs/paper/references.md` | 是 | GB/T 7714 风格 |
| 缺信息清单 | `docs/paper/ms-09-missing-information.md` | 是 | 封面/分工/模板/截图缺口 |
| 本报告 | `docs/review/ms-09-final-delivery-report.md` | 是 | 交付报告 |

---

## 3. 章节验收表

| 章节 | 状态 | 说明 |
| --- | --- | --- |
| 1.1 盒模型与显示类型 | PASS | 理论 + 2.1～2.8 |
| 1.2 布局 | PASS | 理论 + 2.9～2.19 |
| 1.3 MVC模式 | PASS | 请求链路 + 分层 |
| 1.4 Servlet与请求URI映射 | PASS | 容器/Spring 分层 + 2.20 |
| 2.1～2.5 显示类型 | PASS | 真实截图 + 代码路径 |
| 2.6～2.8 盒模型 | PASS | 两种盒模型计算 |
| 2.9～2.17 定位 | PASS | 逐节现象 + 2.15 约束矩形 |
| 2.18 线框仿制 | PASS（参考图有来源） | 原图源任务书附录4；实现为原创线框 |
| 2.19 布局原则 | PASS | 真实 AppLayout 高亮 |
| 2.20 Servlet 映射 | PASS | 10 条矩阵 + 图22/23 |
| 2.21 快递地址簿 | **PARTIAL** | 10 个子节齐全；2.21.8 的图27～30 BLOCKED |
| 结束语/参考文献/附录 | PASS | 附录 A～D |

标题与编号严格按课程大纲；没有 2.22。

---

## 4. 截图验收表

| 图号 | 文件 | 来源 | 真实运行 | 正文引用 | 检查 |
| --- | --- | --- | --- | --- | --- |
| 1～17 | `2-01`～`2-17` | 本项目 | 是 | 是 | PASS |
| 18 | `2-18-original.png` | 课程任务书附录4 | 参考图 | 是 | 已标注来源，非项目实现 |
| 19 | `2-18-wireframe-plan.png` | 本项目 | 是 | 是 | PASS |
| 20 | `2-18-implemented.png` | 本项目 | 是 | 是 | PASS |
| 21 | `2-19-principles.png` | 本项目 | 是 | 是 | PASS |
| 22 | `2-20-servlet-mapping.png` | 本项目 | 是 | 是 | PASS |
| 23 | `2-20-mapping-flow.png` | 自绘 | 否（示意图） | 是 | 已在图题标注 |
| 24 | `2-21-import.png` | 本项目 | 是 | 是 | PASS |
| 25 | `2-21-parse-result.png` | 本项目 | 是 | 是 | PASS |
| 26 | `2-21-edit-preview.png` | 本项目 | 是 | 是 | PASS（保存弹窗） |
| 27 | 地址列表 | — | **BLOCKED** | 正文说明 | 无图；未用 Mock/旧图 |
| 28 | 编辑弹窗 | — | **BLOCKED** | 正文说明 | 无图 |
| 29 | 删除确认 | — | **BLOCKED** | 正文说明 | 无图 |
| 30 | 批量删除确认 | — | **BLOCKED** | 正文说明 | 无图 |
| 31 | `2-21-test-record.png` | 实际 Maven 日志 | 报告截图 | 是 | PASS（真实 160 输出） |

2.21 四图 BLOCKED 原因：本轮 MS-09 红线禁止启动/重启 MySQL，未启动数据库；没有可用安全隔离连接，不得用 Mock、静态 HTML、旧图或合成图替代。

---

## 5. 事实与测试核验

| 项 | 结果 | 证据 |
| --- | --- | --- |
| Maven 全量 | 160 tests，0 failures/errors/skips，BUILD SUCCESS | `ms08r1-fulltest.log` / `docs/test-results` |
| Parser/Controller/真实 HTTP/MySQL/DTO/Unicode/LIKE | 已分层执行 | `docs/test-results/ms-08/` |
| Chrome MS04/05/06/07 | PASS | Playwright 记录 |
| Edge 完整矩阵 | PASS（1280/1366/980/760/390） | `MS08_EDGE_OK` |
| 核心 E2E | PASS | `MS08_E2E_OK` |
| 100 行解析 | P95 2.81ms；30 样本 | `perf-results.json` |
| 10000 条分页 | P95 3.59ms；30 样本 | 同上 |
| 405/HEAD/OPTIONS | POST 405 + `Allow: GET, HEAD, OPTIONS`；HEAD/OPTIONS 200 | 160 tests |
| AC-30 | **PARTIAL** | D08 P1 OPEN |
| AC-31 | PASS | 性能 + Chrome + Edge |
| AC-32 | PASS（本机） | CI NOT RUN |
| D08 | **P1 OPEN** | MySQL X Plugin/防火墙；未处置 |

---

## 6. 格式自审

已实际渲染检查 PDF：

- 封面、摘要、目录、第1章、第2章、2.18 三图、2.20 矩阵、2.21 主要截图、参考文献、附录；
- 67 页；图片可见，页脚页码正常；
- 表格未超出页边距；代码块可读；
- PDF 由最终 docx 转换，中文正常；代码字体被替换为 Courier（P3）。

已知格式问题：

- **目录页显示“目录将在 Word 中更新”占位**，PDF 没有自动目录条目；Word 中 TOC 域可更新。属 **P2**。
- 封面所有个人信息为占位符。
- 2.21 图27～30 缺失。
- 2.21 测试记录图是实际 Maven 日志整理的**报告截图**，不是终端截图；已在图题注明。
- PDF 67 页，长图较多，论文排版偏厚（P3）。

---

## 7. 缺失信息

必须由用户补充：

- 教学系、班号、专业名称；
- 组长/成员学号、姓名；
- 提交日期；
- 真实分工与占比；
- 教师指定字体/字号/行距/页边距/封面模板。

见 `docs/paper/ms-09-missing-information.md`。

---

## 8. 自审缺陷分级

| 级别 | 数量 | 内容 |
| --- | ---: | --- |
| P0 | 0 | 无 |
| P1 | 0 | 无论文题目/结构/证据造假 |
| P2 | 3 | 封面信息缺失；2.21 四张业务截图 BLOCKED；PDF 目录占位 |
| P3 | 2 | 67 页偏厚；代码字体替换 Courier |

---

## 9. 最终结论

- 论文正文、章节结构、图表、Word/PDF、附录已生成并检查；
- **不能宣称“可直接提交教师”**：封面信息未填、2.21 四张核心界面截图缺失、PDF 目录未填充；
- **MS-09 为 PARTIAL**；
- **MS-08 D08 仍 P1 OPEN，AC-30 PARTIAL，MS-08 未正式结束**；
- **MS-10 不启动**；
- 未 commit/push；等待用户补充信息与 ChatGPT 独立复核。
