# MS-05 地址簿管理前端最终验收报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 审查范围 | MS-05 地址簿管理前端、测试脚本、MS-04/05 回归 |
| 审查方式 | 代码审查 + 真实 Maven 测试 + 真实 MySQL 测试库 + Playwright/Chrome |
| 业务代码修改 | **无** |
| Git 提交 | **未执行**（按任务要求只审查，不提交） |
| 结论 | **满足提交条件；建议提交 Git** |

---

## 1. Git 状态

### 1.1 分支与提交

```text
branch: main
HEAD: adf448b025b8a87eba3b2e9c47daa2a1ea302f2b
subject: MS-04
time: 2026-11-14 11:55:19 +0800
```

最近 5 条：

```text
adf448b MS-04
e12035f 后端
9a4809f 项目开发规划
fd13665 Initial commit
```

### 1.2 审查开始时的 `git status --short`

```text
 M AGENTS.md
 M README.md
 M docs/test-plan.md
 M frontend/src/api/addresses.ts
 M frontend/src/components/common/AppIcon.vue
 M frontend/src/layout/AppLayout.vue
 M frontend/src/main.ts
 M frontend/src/router/index.ts
 M frontend/src/types/address.ts
?? docs/ORIGINAL_DEVELOPMENT_PLAN.md
?? docs/PROJECT_CONTEXT.md
?? docs/development-progress.md
?? docs/handover/
?? docs/review/ms-05-address-ui-report.md
?? frontend/scripts/ms05-browser-check.mjs
?? frontend/src/components/address/
?? frontend/src/views/AddressListView.vue
```

### 1.3 `git diff --stat`

```text
 AGENTS.md                                  |   6 +-
 README.md                                  |   8 +-
 docs/test-plan.md                          |  30 +-
 frontend/src/api/addresses.ts              |  51 ++++
 frontend/src/components/common/AppIcon.vue |  16 +-
 frontend/src/layout/AppLayout.vue          |  20 +-
 frontend/src/main.ts                       |   3 +-
 frontend/src/router/index.ts               |   6 +
 frontend/src/types/address.ts              |  14 +
 9 files changed, 137 insertions(+), 17 deletions(-)
```

MS-05 未跟踪文件：

```text
docs/review/ms-05-address-ui-report.md
frontend/scripts/ms05-browser-check.mjs
frontend/src/components/address/AddressFormDialog.vue
frontend/src/components/address/AddressSearchBar.vue
frontend/src/components/address/AddressTable.vue
frontend/src/components/address/BatchDeleteDialog.vue
frontend/src/views/AddressListView.vue
```

**重要**：HEAD 仍是 MS-04；MS-05 的新页面/组件/脚本/报告均未提交。未执行 reset/clean/checkout/stash/commit。

---

## 2. 代码审查结果

### 2.1 列表、搜索、重置、分页

- `AddressListView` 使用真实 `GET /api/addresses`；
- `current/size/keyword` 作为查询参数；
- 搜索回到第 1 页并清空选择；
- 重置清空 keyword 并回第 1 页；
- 分页 `current/page-size` 来自后端响应；
- 删除最后一页数据后自动回退；
- `loading`/`requestSequence` 防止旧请求覆盖新请求；
- 结果为空显示无数据/无搜索结果。

### 2.2 新建/编辑

- `AddressFormDialog` 复用 create/edit 模式；
- 编辑先 `GET /api/addresses/{id}` 取最新记录；
- 只提交 `receiverName/phone/fullAddress`；
- ID 使用 string；
- 姓名 `Array.from` 1～50 码点；
- 电话与地址复用前端校验；
- 失败保留输入，40401 有提示。

### 2.3 删除与批量删除

- 单删有确认框，展示姓名/脱敏电话/地址摘要；
- 批删仅提交当前页 `selectedRows` 的 ID；
- 无 `reserve-selection`；
- 重复 ID 由后端去重；
- 使用后端实际 `deletedCount` 显示；
- 部分不存在不作全部失败。

### 2.4 选择状态

以下操作均清空选择：

- 翻页；
- size 改变；
- 搜索；
- 重置；
- 新建/编辑后刷新；
- 单删；
- 批删；
- 手动刷新。

数据刷新后再次 `clearSelection()`，避免 Element Plus 内部选择残留。

### 2.5 空态、加载态、错误态

- 初始 `v-loading`；
- 表格 loading；
- 空数据/无搜索结果两种文案；
- 列表请求失败 alert；
- 表单错误；
- 删除/批删确认；
- 保存/操作成功提示。

### 2.6 类型与契约

- `AddressVO.id: string`；
- `PageResult.total/current/size/pages: number`；
- `AddressSavePayload` 仅业务字段；
- `LineError` 与 `Violation` 分离；
- 无 `Number()`/`parseInt()` 处理 ID；
- 分页字段未转 string。

### 2.7 390px 布局

- 检查了地址簿页面 390px；
- 初次检查发现分页栏横向溢出（`scrollWidth=412`，`innerWidth=390`）；
- 通过移动端分页 `flex-wrap` 修复；
- 修复后 `scrollWidth=390`，无页面级横向溢出；
- 表格自身横向滚动由表格容器承担。

### 2.8 状态竞争/泄漏

- 列表请求有 `requestSequence`；
- 表单提交有 `formSubmitting`；
- 批删有 `batchDeleting`；
- 单删有 `deleteLoading`；
- 打开编辑有 `detailLoading`；
- 没有发现组件卸载后的定时器/全局监听器；
- `loadAddresses` 对过期请求只忽略结果，不写状态。

---

## 3. 发现的问题

### P0

**无已确认的 P0 业务缺陷。**

### P1

**无阻塞提交的 P1 代码缺陷。**

### P2

| 编号 | 问题 | 影响 | 状态 |
| --- | --- | --- | --- |
| P2-1 | `ms04-browser-check.mjs` 保存 3 条测试数据后没有自行清理 | 测试库需要人工清理；不影响主库 | 未修改；本轮已人工清理 |
| P2-2 | `ms05-browser-check.mjs` 通过 API 逻辑删除测试数据，物理行可能残留 | 测试库可能累积 is_deleted=1 行 | 未修改；本轮清理到 0 |
| P2-3 | MS-05 网络错误/部分防重复场景使用 Playwright 路由 Mock | 不是真实后端故障注入 | 报告已标注 |
| P2-4 | 390px 仅做人工/一次性脚本检查，未纳入 ms05 主脚本 | 移动端回归不自动 | 建议补 |
| P2-5 | Playwright 未纳入 CI | 不能自动证明每次提交 | 后续 MS-08/CI |
| P2-6 | MS-05 全部代码未提交 Git | HEAD 无 MS-05；换机/误操作可能丢失 | 建议提交 |
| P2-7 | `AddressListView` 的 `deleteLoading` 只做防重入，未禁用表格按钮 | 用户可能重复点，但 handler 已拦截 | 非阻塞 |

### P3

- 主前端 chunk >500 kB（Element Plus 全量）；
- Sass legacy API warning；
- 地址簿无高级列配置；
- 无正式性能测试；
- 无登录/权限（范围外）；
- 无 CI 截图/报告归档。

---

## 4. 修改内容

**本次审查没有修改任何业务代码。**

只新增本报告：

```text
docs/review/ms-05-final-acceptance-report.md
```

测试脚本/组件/页面未改动。

---

## 5. 测试命令与真实结果

### 5.1 后端 Maven

环境：JDK 17.0.21；MySQL 8 本机；`DB_USERNAME=root`；`DB_PASSWORD` 来自本地环境；测试库 `express_address_book_test`。

```text
mvn -B clean test
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
mvn exit code: 0
```

### 5.2 前端构建

```text
npm run build
vue-tsc --noEmit + vite build
✓ built in 5.30s
npm exit code: 0
```

警告：

- Sass legacy JS API deprecation；
- 主 chunk >500 kB；
- 不影响构建成功。

### 5.3 MS-04 浏览器测试

```text
node scripts/ms04-browser-check.mjs
MS04_BROWSER_OK
```

真实后端/MySQL 测试库；本脚本保存了 3 条测试数据，脚本自身没有清理；审查时已人工清理测试库。

### 5.4 MS-05 浏览器测试

```text
node scripts/ms05-browser-check.mjs
MS05_BROWSER_OK
```

实际执行：

- 地址簿页面；
- 真实列表/total；
- 姓名/电话/地址搜索；
- `%`/`_` 字面搜索；
- 50/51 Unicode 码点关键词；
- 重置；
- 分页/每页数量；
- 翻页/搜索清空选择；
- 新建/编辑/刷新；
- 单删；
- 批量选择/确认/仅提交选中 ID；
- 删除最后一条自动退页；
- 空数据/无搜索结果；
- 网络错误提示（Playwright 路由 Mock 50002）。

### 5.5 390px 布局

临时 Playwright 检查（390×844，真实列表 3 条测试数据）：

```text
MS05_390_OK {"scrollWidth":390,"innerWidth":390}
```

页面级无横向溢出；表格自身在容器内横向滚动。测试数据随后清理。

### 5.6 数据库

- Maven 测试后：测试库 `express_address_book_test` 0 行；
- MS-04/MS-05 浏览器测试后：测试库 12 条逻辑删除行，已人工 `DELETE` 清理到 0；
- 390px 临时检查后：测试数据已清理到 0；
- 主库 `express_address_book`：0 行，未被测试写入；
- 未执行 DROP DATABASE / TRUNCATE / 主库清理。

---

## 6. 未完成事项

1. MS-05 尚未提交 Git；
2. Playwright 未纳入 CI；
3. 测试脚本清理逻辑需要统一（MS-04 未清理，MS-05 逻辑删除残留）；
4. 390px 未纳入 ms05 主脚本；
5. 无正式性能测试；
6. 无用户登录/权限（范围外）。

---

## 7. MS-05 是否满足提交条件

**满足提交条件。**

理由：

- 后端 146 测试实际通过；
- 前端构建实际通过；
- MS-04/MS-05 Playwright 实际通过；
- 代码审查无 P0/P1 阻塞；
- 分页 Number、ID String、选择清空、批删 ID、自动退页、390px 均已验证；
- 测试库/主库无业务数据损坏。

P2 问题（测试清理、CI、Mock）不阻塞 MS-05 代码提交，但应在 MS-08 前处理。

---

## 8. 是否建议提交 Git

**建议提交。**

建议提交内容：

- `frontend/src/components/address/`；
- `frontend/src/views/AddressListView.vue`；
- `frontend/src/api/addresses.ts`；
- `frontend/src/types/address.ts`；
- `frontend/src/router/index.ts`；
- `frontend/src/layout/AppLayout.vue`；
- `frontend/src/main.ts`；
- `frontend/src/components/common/AppIcon.vue`；
- `frontend/scripts/ms05-browser-check.mjs`；
- `docs/review/ms-05-address-ui-report.md`；
- 本报告；
- 相关 AGENTS/README/test-plan 状态同步。

**但本任务明确要求不提交；本次没有执行 git add/commit。** 提交前建议先审阅完整 diff，特别是未跟踪的 5 个 MS-05 前端文件和脚本。
