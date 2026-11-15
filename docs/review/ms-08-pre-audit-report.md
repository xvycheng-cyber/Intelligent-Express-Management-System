# MS-08 前置审计与验收基线报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 阶段 | MS-08.1 前置审计与验收基线 |
| 范围 | Git、测试资产、隔离数据库、AC-21～AC-32、R1～R5 |
| 结论 | 基线建立；使用隔离 Schema，不使用主库/旧测试库做写测试 |

---

## 1. Git 与工作区基线

```text
branch: main
HEAD at MS-08.1 start: 6dd2bdbfdb1ca912ccabee3b0f9acee5c1fdca27
Current HEAD after external MS07 commit: ef97913（MS07）
```

说明：MS-08.1 开始时审计到 `6dd2bdb`；随后仓库出现外部 `MS07` 提交 `ef97913`，该提交不是本次 MS-08 创建。当前 MS-08 修改相对于 `ef97913`；本报告保留开始审计事实。

审查开始时工作区包含 MS-06 R1、MS-07 全部实现/测试/截图/文档和既有 handover 状态；未找到 MS-07 独立提交基线，不能把旧提交冒充准确起点。

没有执行 commit/push/reset/clean/checkout/restore/stash；没有清理既有工作区变更。

---

## 2. 测试资产盘点

### 后端测试类

```text
AddressParserServiceTest
AddressControllerTest
AddressParseIntegrationTest
AddressApiMysqlTest
AddressSaveDTOTest
LikeEscaperTest
CodePointLengthTest
GlobalExceptionHandlerTest
RTest
HealthControllerTest
DbHealthControllerTest
ServletMappingIntegrationTest
```

历史基线：158 tests（原 146 + Servlet 12）。

### 前端脚本

```text
ms04-browser-check.mjs
ms05-browser-check.mjs
ms06-2-browser-check.mjs
ms06-3-browser-check.mjs
ms06-4-browser-check.mjs
ms06-5-browser-check.mjs
ms06-6-browser-check.mjs
ms06-7-browser-check.mjs
ms06-8-browser-check.mjs
ms06-9-browser-check.mjs
ms07-3-browser-check.mjs
ms07-5-screenshots.mjs
```

本阶段新增：

```text
ms08-e2e-check.mjs
ms08-perf-check.mjs
```

### 测试配置

- `backend/src/test/resources/application-test.yml`
- `frontend/vite.config.ts` proxy `/api`、`/demo`
- 测试数据库：隔离 Schema `express_address_book_ms08_test`
- 旧测试库 `express_address_book_test` 有 38 行既有测试数据，未清理
- 主库 `express_address_book` 0 行，未写入

---

## 3. AC-21～AC-32 定义

已写入 `docs/test-plan.md` 第 10 节，包含：

- 12 条 AC；
- P0/P1 优先级；
- 对应 FR/BR/NFR；
- 测试/证据映射；
- 状态与通过条件。

AC 是项目推导验收标准，不是课程任务书原文。

---

## 4. R1～R5 轮次

已写入 `docs/test-plan.md` 第 11 节：

- R1 基线/环境/追踪；
- R2 后端 L1～L3；
- R3 前端 L4/E2E；
- R4 安全/性能/兼容；
- R5 缺陷修复/回归/证据。

---

## 5. 数据安全基线

本轮创建并使用：

```text
express_address_book_ms08_test
```

只对本轮 Schema 的虚构测试数据执行 `TRUNCATE`/插入/删除；没有：

- 清理 `express_address_book_test` 的 38 行既有数据；
- 修改主库 `express_address_book`；
- 修改数据库账号/root 主机（root 仅 localhost）；
- 创建远程用户。

---

## 6. 已知风险

1. `mvn clean test` 的全量测试会使用测试库夹具；本轮用系统属性覆盖到隔离 Schema。
2. MS05 脚本假定测试库只有其测试前缀的 11 行；旧测试库有 38 行既有数据，不能直接对旧库运行。
3. 无 CI 环境；GitHub Actions 未配置。
4. Node/Playwright 使用本机 Chrome；Edge 只做基本页面验证。
5. 项目没有身份认证，属于既定课程范围，不作为缺陷。

**MS-08.1 完成，进入 MS-08.2，不等待用户确认。**
