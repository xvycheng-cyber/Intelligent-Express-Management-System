# README-R1 小范围修正与事实一致性报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 分支 / HEAD | `main` / `92f4f4f99c99956e75768212509260b7378621a1` |
| 范围 | README 小范围事实修正 + 本报告 |
| 结论 | README-R1 完成；未改业务代码/测试/数据库/MySQL/防火墙 |

---

## 1. 任务背景

README 刚完成全面更新。独立审查要求补充性能数据集口径、整理目录树重复表达、校正 MS-09/MS-10 状态，并再次核对技术栈版本和工作区事实。

本次不重新编写 README，不扩大范围。

---

## 2. 修改前发现的问题

1. README 写“10000 条分页查询”，但没有说明 10000 是数据集总量、`pageSize=10`，容易被误读为一次返回 10000 条；
2. 性能说明没有明确“P95 3.59ms 是本机 30 个有效样本结果”，也未强调不是生产承诺；
3. 目录树中 `docs/paper/` 在 `docs` 内部出现后，又在根级重复列了一次，结构表达重复；
4. MS-10 在 README 中写“未开始”，与本轮 README 文档准备的实际工作不一致；本轮只能标记 PREPARATION IN PROGRESS，不能写 COMPLETE；
5. 需要再次确认 MS-08/MS-09 的 PARTIAL/D08 OPEN 状态没有被误改，技术栈版本必须有文件证据。

---

## 3. 修改内容

只修改 `README.md`：

1. 性能行改为“10000 条数据集、pageSize=10 的分页”；
2. 性能表标题改为“10000 条数据集分页（pageSize=10）”；
3. 性能说明新增：
   - 10000 条是测试数据集记录总量；
   - 分页固定 `page=1&size=10`，不是一次返回 10000 条；
   - P95 3.59ms 是本机 30 个有效样本结果，不是生产承诺；
4. 删除根目录树末尾重复的 `docs/paper/` 行，保留 `docs/paper/` 在 `docs` 内的真实职责；
5. 将 MS-10 状态改为 **PREPARATION IN PROGRESS**，明确“仅 README 文档准备；验收/答辩未开始”；
6. 同步当前状态表、进度表、后续规划表的 MS-10 表述；
7. 保持 MS-08 未正式结束、MS-09 PARTIAL、D08 P1 OPEN、AC-30 PARTIAL、AC-31 PASS、AC-32 PASS（CI NOT RUN）不变。

未删除 README 原有重要内容，未弱化 D08 风险。

---

## 4. 性能 JSON 原始字段核对

读取 `docs/test-results/ms-08/perf-results.json`：

```json
"sample": {
  "parserLines": 100,
  "queryPageSize": 10,
  "warmupRuns": 2,
  "measuredRuns": 30
},
"list": {
  "name": "address list page (10000-row dataset)",
  "count": 30,
  "averageMs": 3.12,
  "medianMs": 3.1,
  "p95Ms": 3.59
}
```

读取 `frontend/scripts/ms08-perf-check.mjs`：

- 预热 2 次；
- 30 次有效测量；
- 分页请求为 `/addresses?page=1&size=10`；
- 脚本使用已存在的隔离 Schema 数据集，不重新建 10000 条数据；
- JSON 的 `list.name` 标明 `10000-row dataset`。

因此 README 现在明确区分“10000 条数据集”和“pageSize=10 单页返回”，并注明本机 30 样本，不把结果写成生产性能承诺。

---

## 5. MS-08/MS-09/MS-10 状态核对

| 项 | README 状态 | 证据 |
| --- | --- | --- |
| MS-08 | 已实施，未正式结束 | `ms-08-final-verification-report.md` |
| D08 | P1 OPEN | `ms-08-defect-ledger.md` |
| AC-30 | PARTIAL | MySQL X 33060/防火墙风险 |
| AC-31 | PASS | 性能 + Chrome + Edge |
| AC-32 | PASS（本地验证，CI NOT RUN） | 本地构建/测试 |
| MS-09 | PARTIAL | 封面信息、2.21 四图、PDF 目录待补 |
| MS-10 | PREPARATION IN PROGRESS | 仅 README 文档准备；验收/答辩未开始，不是 COMPLETE |

未修改 `docs/review/ms-08-r2-manual-remediation-plan-report.md` 的历史“MS-09 不启动”表述。

---

## 6. 技术栈版本核对

只读取，未升级依赖：

- `backend/pom.xml`：Spring Boot 3.3.5、MyBatis-Plus 3.5.7、Java 17；
- `frontend/package.json` 范围：Vue `^3.5.13`、TypeScript `~5.7.2`、Vite `^5.4.11`、Element Plus `^2.9.1`、Axios `^1.7.9`、Vue Router `^4.5.0`、Sass `^1.83.0`、Playwright `^1.65.1`；
- `frontend/package-lock.json` 实际锁定：Vue 3.5.45、TypeScript 5.7.3、Vite 5.4.21、Element Plus 2.14.8、Axios 1.21.1、Vue Router 4.6.5、Sass 1.108.2、Playwright 1.65.1。

README 同时保留“package.json 范围”和“lock 实际锁定值”，未把范围写成精确版本，未修改依赖文件。

---

## 7. README 链接与图片检查

脚本解析 README 相对链接/图片：

```text
links=23
bad=0
```

包括：

- `docs/requirements.md`、`architecture.md`、`database-design.md`、`api-design.md`、`parser-design.md`、`test-plan.md`、`development-roadmap.md`、`development-progress.md`、`course-paper-outline.md`；
- `docs/review/`、`docs/security/`、`docs/test-results/ms-08/`；
- `docs/paper/快递地址簿录入程序设计.md`、`deliverables/MS-09/`；
- 4 张真实截图。

所有图片文件实际存在；未新增虚假截图。

---

## 8. `git diff --check`

```text
git diff --check
exit=0
```

README 已清理行尾空格。

---

## 9. 实际修改文件清单

```text
M README.md
?? docs/review/readme-r1-correction-report.md
```

本次除报告外只改 README；未修改 Java/Vue/TypeScript/SQL/测试/数据库/MySQL/防火墙。

---

## 10. 当前 HEAD

```text
branch: main
HEAD: 92f4f4f99c99956e75768212509260b7378621a1
```

---

## 11. Git 工作区状态

任务开始时工作区为干净状态（HEAD `92f4f4f`）。本次产生：

```text
M README.md
?? docs/review/readme-r1-correction-report.md
```

未执行 commit/push/reset/clean/checkout/restore/stash。

---

## 12. 未执行事项

- 未运行完整 Maven 测试；
- 未运行 Playwright 全量回归；
- 未重新运行性能测试；
- 未启动/停止/重启 MySQL；
- 未修改 MySQL/防火墙/账号；
- 未修改论文、Word/PDF、截图、MS-09 交付物；
- 未修改 MS-08 历史报告；
- 未开始 MS-10 验收/答辩实施。

---

## 13. 遗留问题

1. **D08 P1 OPEN**：MySQL X Plugin 33060 全接口监听 + mysqld 宽泛防火墙规则；
2. AC-30 仍为 PARTIAL；
3. 未完成第二台物理设备远程验证；
4. GitHub Actions CI 仍 NOT RUN；
5. MS-09 仍为 PARTIAL：封面信息、2.21 四张业务截图、PDF 目录待补；
6. 项目无登录/权限，不能公网部署；
7. MS-10 仅 README 文档准备，验收/答辩未开始。

---

## 14. 结论

README-R1 完成：

- 性能数据集/pageSize/样本/非生产承诺表述已准确；
- 目录树不再重复表达 `docs/paper/`；
- MS-08、MS-09、MS-10 状态与当前事实一致；
- D08/AC-30 没有被误改为 PASS/CLOSED；
- 技术栈版本有 `pom.xml`、`package.json`、`package-lock.json` 证据；
- README 链接/图片检查通过；
- `git diff --check` 通过；
- 未修改业务代码、数据库、MySQL 配置和防火墙；
- 未 commit/push。

**等待 ChatGPT 独立审查；不开始 MS-10。**
