# MS-13 最终验收评审报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 分支 / HEAD | `main` / `8a8f4b829742a2d527845744a2fac32c1157c166` |
| MS-13 状态 | **COMPLETED** |
| 项目评审建议 | **CONDITIONALLY ACCEPTED — SUBJECT TO D08 / AC-30 USER-DEFERRED RISK** |
| 正式验收 | **未签字**（本报告是项目评审建议，不是教师/课程组签字） |

---

## 1. 基本信息

- 项目：快递通——智能快递地址簿录入与管理系统；
- 论文题目：快递地址簿录入程序设计；
- 依据：课程任务书、requirements、test-plan、MS-08～MS-12 报告、MS-13 回归；
- 本轮实际执行 Maven 160、前端 build、Chrome/Edge 全套脚本、DB health。

---

## 2. 项目当前版本

```text
branch: main
HEAD: 8a8f4b8 (ms12)
```

工作区在本轮开始干净；本轮只新增/更新审计、状态、交接文档；未 commit/push。

---

## 3. 功能完成情况

| 功能 | 实现 | 测试 | 结论 |
| --- | --- | --- | --- |
| 多行录入/解析 | ImportView/Parser | 160 + MS04 | PASS |
| 人工修正/恢复 | ParseResultTable | MS04 | PASS |
| 保存确认/批量保存 | SaveConfirmDialog/事务 | 160 + MS10 | PASS |
| 列表/分页/搜索 | AddressListView | MS05/API | PASS |
| 新建/编辑/单删/批删 | Controller/Service | MS05/MS10 | PASS |
| Web 2.1～2.19 | `/web-basics` | MS06 | PASS |
| Servlet 2.20 | `/servlet-demo` | MS07/14 tests | PASS |
| 论文/截图 | 2.21 四图/68页 | MS12 | PASS（封面 deferred） |

---

## 4. 测试结果

| 测试 | 本轮结果 |
| --- | --- |
| Maven 全量 | **160/160，0 fail/error/skip，BUILD SUCCESS** |
| 前端 build | **PASS，5.10s** |
| Chrome MS04/05/06/07/E2E | PASS（MS05 初次因数据残留失败，清理后 PASS） |
| Edge | PASS |
| 性能 | 未重跑；引用 MS-08 历史 30 样本 |
| DB | `/api/health/db` 200；主表初始/最终 0 |

---

## 5. AC 矩阵摘要

- AC-01～AC-20：**UNDEFINED**；
- AC-21～AC-29：PASS；
- AC-30：**PARTIAL**；
- AC-31：PASS（性能历史+浏览器本轮）；
- AC-32：PASS（本地；CI NOT RUN）。

---

## 6. MS-09/10/11/12

| 阶段 | 状态 |
| --- | --- |
| MS-09 | COMPLETED（四图/目录/68页；封面 USER DEFERRED） |
| MS-10 | COMPLETED |
| MS-11 | COMPLETED |
| MS-12 | COMPLETED（独立检查 PASS） |

---

## 7. D08/AC-30

- D08：P1 OPEN / USER DEFERRED；
- AC-30：PARTIAL；
- 33060/防火墙未整改；
- 未从第二台设备验证；
- 不能写成 CLOSED/PASS。

---

## 8. CI

- GitHub Actions：**NOT RUN / P2 / USER DEFERRED**；
- 课程任务书无 GitHub/Actions/CI 硬性要求；
- 本地 Maven/build/Playwright 已执行；
- 不把本地结果写成 CI PASS。

---

## 9. 环境

Windows 11、JDK17.0.21、Maven3.9.11、Node22.14、MySQL8.0.43、Chrome/Edge157；后端/前端可启动，DB health 200。

---

## 10. 演示材料

- 5/10 分钟脚本：`ms-13-demo-script-5min.md`、`10min.md`；
- Q&A：30 题；
- 现场演练：MS-10 已完成核心 CRUD/5-10 路径；本轮未重新计时；
- 自动化耗时不能等同现场讲解。

---

## 11. 发现的问题

| 编号 | 级别 | 问题 | 影响 |
| --- | --- | --- | --- |
| MS13-P1-01 | P1 | D08 P1 OPEN，AC-30 PARTIAL | 正式验收不能无条件通过 |
| MS13-P2-01 | P2 | MS05 脚本依赖空表，MS04 留数据导致初次失败 | 测试卫生；已清理重跑 |
| MS13-P2-02 | P2 | CI NOT RUN | 工程化增强，非硬性 |
| MS13-P2-03 | P2 | 封面/个人信息 USER DEFERRED | 论文提交前人工补 |
| MS13-P3-01 | P3 | 性能未本轮重跑 | 引用历史证据 |
| MS13-P3-02 | P3 | 论文 68 页偏厚 | 排版优化 |

---

## 12. 评审结论

课程功能、Web/Servlet 实践、论文四图/目录/PDF、本地测试均达到交付条件；唯一实质未闭合项为 **D08/AC-30**（用户明确暂不处理）。因此：

> **CONDITIONALLY ACCEPTED — SUBJECT TO D08 / AC-30 USER-DEFERRED RISK**

这不是无条件 ACCEPTED，也不是教师/课程组签字。正式验收需用户决定是否接受 D08 风险并完成人工签字/答辩。

---

## 13. 是否可进入后续

- MS-13 阶段工作 **COMPLETED**；
- 项目可进入“条件性验收/答辩”准备；
- 不自动开始新阶段；
- 不修改 D08、不 push、不 commit。

---

## 14. 最终结论

**MS-13 完成；项目建议条件性验收，正式验收未通过。**
