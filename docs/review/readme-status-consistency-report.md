# README 当前状态一致性修正报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-16 |
| 分支 / HEAD | `main` / `3b443876612ed4986f134d7743a2e1d0e702f92d` |
| 修改范围 | 仅 `README.md` |
| 依据 | MS-13-R0/回归/验收矩阵/最终评审/交接；development-progress；development-roadmap |
| 结论 | README 状态已与 MS-13 当前状态一致 |

---

## 1. 修改前发现的问题

1. 顶部里程碑顺序为 MS-09、MS-10、MS-12、MS-13、MS-11，MS-11 排在 MS-13 后；
2. “MS-01～MS-08 主要开发”写作“MS-08 因 D08 尚未正式结束”，把阶段开发和安全风险混在一起；
3. MS-10 行“当前为条件性验收”与“MS-10 阶段完成时 NOT READY”混用，历史/当前边界不够清楚；
4. 2.21 截图说明仍写“四张截图仍是 BLOCKED 项”，与 MS-12 已完成冲突；
5. 性能指标未明确是 MS-08 历史证据；
6. AC-31/AC-32 虽写 CI NOT RUN，但证据范围不够清晰；
7. 项目整体条件性验收与教师正式验收的边界不够突出。

---

## 2. 修改内容

| 项目 | 修改 |
| --- | --- |
| 里程碑顺序 | 调整为 MS-09、MS-10、MS-11、MS-12、MS-13 |
| MS-01～MS-08 | 写作“功能/测试实施完成；D08 风险独立保留” |
| MS-08 状态 | 明确“主要开发完成 ≠ D08 关闭”；D08 仍 P1 OPEN / USER DEFERRED |
| MS-10 | 明确 NOT READY 是 MS-10 阶段完成时的历史状态，当前为条件性验收 |
| 2.21 截图 | 改为“MS-12 已补齐四张真实截图并插入论文/Word/PDF” |
| 性能 | 明确标记为 **MS-08 历史证据**；MS-13 未重跑 |
| AC-31/AC-32 | 注明 AC-31 性能为历史、浏览器含本轮；AC-32 为本地 build/160 tests，CI NOT RUN |
| 当前项目 | 保持 **CONDITIONALLY ACCEPTED**（D08/AC-30 风险；正式签字未完成） |
| D08/AC-30 | 保持 **P1 OPEN / PARTIAL** |
| CI | 保持 **NOT RUN / P2 / USER DEFERRED**，注明非课程硬性 |

---

## 3. 修改前后对照

| 项 | 修改前 | 修改后 |
| --- | --- | --- |
| 里程碑顺序 | 09、10、12、13、11 | **09、10、11、12、13** |
| MS-08 | 因 D08 尚未正式结束 | 功能/测试实施完成；D08 风险独立 |
| MS-10 | 历史/当前混写 | 明确历史 NOT READY → 当前条件性验收 |
| 2.21 截图 | 仍 BLOCKED | MS-12 已补齐 |
| 性能 | 未标历史 | 明确 MS-08 历史证据 |
| 项目整体 | 条件性验收 | 保持条件性验收 |
| 正式验收 | 未取得签字 | 明确未取得教师/课程组签字 |
| D08/AC-30 | P1/PARTIAL | 保持 |
| CI | NOT RUN | NOT RUN / P2 / USER DEFERRED |

---

## 4. 核验结果

- 里程碑顺序：MS-09 → MS-10 → MS-11 → MS-12 → MS-13；
- 当前项目：**CONDITIONALLY ACCEPTED**；
- 正式验收：**未签字/未通过**；
- D08：**P1 OPEN / USER DEFERRED**；
- AC-30：**PARTIAL**；
- CI：**NOT RUN / P2 / USER DEFERRED**；
- AC-31：PASS（性能历史；Chrome/Edge 本轮）；
- AC-32：PASS（本地 build/160 tests；CI NOT RUN）；
- 未把 AC-30/CI 写成 PASS；
- 未修改业务代码、数据库、MySQL、防火墙、论文或论文交付物。

---

## 5. 文件与 Git

修改文件：

```text
M README.md
?? docs/review/readme-status-consistency-report.md
```

`git diff --check`：**exit 0**。  
README 相对链接/图片检查：**32 个，bad=0**。  
未 commit/push/reset/clean/checkout/stash。

---

## 6. 未解决事项

- D08 P1 OPEN / USER DEFERRED；
- AC-30 PARTIAL；
- 论文封面/个人信息 USER DEFERRED；
- CI NOT RUN / P2；
- 正式验收未签字。

---

## 7. 结论

README 已准确呈现 MS-01～MS-13 的阶段状态、当前条件性验收、正式验收未签字以及 D08/AC-30/CI 的真实遗留状态；未改变任何验收结论。

**修改完成，等待用户检查。**
