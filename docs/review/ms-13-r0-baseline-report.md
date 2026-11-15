# MS-13-R0 前置基线报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 分支 / HEAD | `main` / `8a8f4b829742a2d527845744a2fac32c1157c166` |
| 工作区 | 干净（执行前） |
| MySQL | 8.0.43，`express_address_book`，初始 0 行 |
| 结论 | 基线符合用户描述；进入最终回归 |

---

## 1. 基线核对

- 当前 HEAD = `8a8f4b8 (ms12)`，与用户提供一致；
- 分支 main；
- 工作区干净；
- MS-09/10/11/12 在 README/进度/路线图中已完成；
- D08 P1 OPEN / USER DEFERRED；
- AC-30 PARTIAL；
- CI NOT RUN / P2 / USER DEFERRED；
- 论文封面/个人信息 USER DEFERRED；
- 四张 2.21 截图、Word/PDF 68 页存在；
- 论文题目未变，未新增 2.22。

## 2. 读取范围

- AGENTS、README、requirements、architecture、database-design、api-design、parser-design、test-plan、development-progress、development-roadmap；
- MS-08～MS-12 最终报告、D08 风险报告、MS-13 交接；
- 论文 Markdown/Word/PDF、截图、交付物；
- 后端 Controller/Service/Parser/Servlet、前端路由/页面、schema.sql；
- Surefire 与历史性能/浏览器证据。

## 3. AC 基线

| 项 | 状态 |
| --- | --- |
| AC-01～AC-20 | UNDEFINED（仓库无定义） |
| AC-21～AC-29 | PASS（历史；本轮回归验证） |
| AC-30 | PARTIAL（D08） |
| AC-31 | PASS（性能历史，Chrome/Edge 历史+本轮） |
| AC-32 | PASS（本地；CI NOT RUN） |

## 4. 过期/矛盾项

- README/进度/路线图在 MS-12 后已同步 MS-09/12 COMPLETED；
- 旧 `security-checks.md` 有 158 历史表述，保留为历史证据；
- 项目整体仍 NOT READY/条件性，不能因 MS-13 回归自动 ACCEPTED。

## 5. MS-13 测试计划

1. Maven 全量 160；
2. 前端 `npm run build`；
3. Chrome/Edge 现有 Playwright 全套；
4. 数据库健康/业务 smoke；
5. 演示脚本/Q&A/验收矩阵；
6. 不修改 D08/MySQL/防火墙/论文封面。

## 6. 无法执行项

- GitHub Actions：NOT RUN（用户人工上传，不执行）；
- D08 整改：USER DEFERRED；
- 论文封面/个人信息：USER DEFERRED。
