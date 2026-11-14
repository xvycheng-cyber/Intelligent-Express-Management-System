# MS-05 地址簿管理前端报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-05 地址簿管理前端 |
| 范围 | 列表、搜索、分页、新建、编辑、单删、批量删除 |
| 技术栈 | Vue 3 + TypeScript + Vite + Element Plus + Axios + Vue Router + SCSS |
| 构建 | `npm run build`：通过 |
| 浏览器 | Playwright + 本机 Chrome：**MS05_BROWSER_OK** |

---

## 1. 页面设计

地址簿页面沿用了 MS-04 的深海军蓝侧栏、蓝色强调色、白色卡片、浅灰背景、统一圆角和阴影。

页面结构：

1. 页面标题：**收件地址簿**；
2. 指标卡：地址总量、当前页数量、当前选中；
3. 批量删除入口；
4. 统一搜索区；
5. 地址表格；
6. 分页栏；
7. 新建/编辑 Dialog；
8. 批量删除确认 Dialog。

侧边导航已启用：

- 批量地址录入 → `/import`
- 地址簿管理 → `/addresses`

Element Plus 使用 `zh-cn` locale，分页/每页条数等文案为中文。

---

## 2. 新增与修改文件

### 新增

| 文件 | 用途 |
| --- | --- |
| `src/views/AddressListView.vue` | 地址簿主页面 |
| `src/components/address/AddressSearchBar.vue` | 搜索、重置、keyword 码点提示 |
| `src/components/address/AddressTable.vue` | 表格、选择、行操作、空状态 |
| `src/components/address/AddressFormDialog.vue` | 新建/编辑表单 |
| `src/components/address/BatchDeleteDialog.vue` | 批量删除确认 |
| `scripts/ms05-browser-check.mjs` | Playwright 真实浏览器流程 |

### 修改

| 文件 | 变更 |
| --- | --- |
| `src/api/addresses.ts` | 列表、详情、新建、编辑、删除、批量删除 API |
| `src/types/address.ts` | AddressSaveResult、BatchDeleteResult、AddressListQuery |
| `src/layout/AppLayout.vue` | 启用地址簿导航，按路由切换 active 和页脚阶段 |
| `src/router/index.ts` | 新增 `/addresses` 路由 |
| `src/main.ts` | Element Plus 中文 locale |

---

## 3. API 接入

| 接口 | 前端函数 |
| --- | --- |
| `GET /api/addresses` | `listAddresses(query)` |
| `GET /api/addresses/{id}` | `getAddress(id)` |
| `POST /api/addresses` | `createAddress(payload)` |
| `PUT /api/addresses/{id}` | `updateAddress(id, payload)` |
| `DELETE /api/addresses/{id}` | `deleteAddress(id)` |
| `POST /api/addresses/batch-delete` | `batchDeleteAddresses(ids)` |

所有请求继续使用 `src/api/request.ts` 的 Axios 封装和 `ApiError`；未新增第二套请求实现。

---

## 4. 搜索与分页

### 搜索

- 支持姓名/电话/地址；
- Enter 搜索；
- 重置；
- 搜索时回到第 1 页；
- 搜索时清空选择；
- 前端只 trim，不执行 LikeEscaper；
- `%`、`_`、`!` 交由后端按字面处理；
- 50 个 Unicode 码点通过；
- 51 个 Unicode 码点显示提示并禁用搜索按钮；后端仍有独立 40001 校验。

### 分页

- `PageResult` 的 `total/current/size/pages` 保持 number；
- 后端分页，不一次性加载全部数据；
- 每页 10/20/50/100；
- 删除当前页最后一条后，若仍有数据且当前页 >1，自动退回上一页；
- 每页条数修改回到第 1 页；
- 切换页码、size、搜索、重置、刷新均清空选择。

---

## 5. Unicode 输入校验

`AddressFormDialog` 复用 `textLength.ts`：

```ts
Array.from(value).length
```

- 姓名 1～50 码点；
- 51 码点显示表单错误；
- 不使用 `maxlength` 作为唯一限制；
- 支持中文、半角英文和全角英文姓名；
- 电话允许后端已支持的 `+86`、空格、短横、括号等输入；
- 地址按 API/DTO 的 255 长度限制校验。

---

## 6. 新建与编辑

- `POST /api/addresses`；
- 编辑前使用 `GET /api/addresses/{id}` 获取最新记录，避免使用过期列表值；
- `PUT /api/addresses/{id}`；
- 只提交 `receiverName/phone/fullAddress`；
- ID 始终为 string，未使用 `Number()`/`parseInt()`；
- 成功后关闭 Dialog、提示成功、刷新列表；
- 失败保留表单输入并展示 `ApiError.message`；
- 40401 显示记录不存在/已删除，并刷新列表。

---

## 7. 单条删除

使用 `ElMessageBox.confirm`，展示：

- 联系人姓名；
- 脱敏联系电话；
- 地址摘要；
- 逻辑删除说明。

确认后才调用 `DELETE /api/addresses/{id}`。

成功：提示、刷新、更新总数；当前页为空时自动退页。

---

## 8. 批量删除

- 只允许当前页选择；
- 不使用 `reserve-selection`；
- 请求 `ids` 为当前页实际勾选 ID 的 `string[]`；
- 确认弹窗展示选择数量、姓名、地址摘要；
- 使用后端 `deletedCount` 显示实际删除数量；
- 部分不存在不当作全部失败；
- 成功后清空选择并刷新。

---

## 9. 选择状态

| 操作 | 是否清空 |
| --- | --- |
| 当前页改变 | 是 |
| 每页数量改变 | 是 |
| 搜索 | 是 |
| 重置搜索 | 是 |
| 新建保存后刷新 | 是 |
| 编辑保存后刷新 | 是 |
| 单条删除 | 是 |
| 批量删除 | 是 |
| 手动刷新 | 是 |

表格数据刷新后再次调用 `clearSelection()`，避免 Element Plus 内部选择状态残留。

---

## 10. TypeScript 类型

```ts
interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

interface AddressVO {
  id: string
  receiverName: string
  phone: string
  fullAddress: string
  createTime: string
  updateTime: string
}

interface AddressSavePayload {
  receiverName: string
  phone: string
  fullAddress: string
  lineNo?: number
}
```

分页字段保持 number，地址 ID 保持 string。

---

## 11. npm run build 实际结果

```text
npm run build
vue-tsc --noEmit + vite build
✓ built
exit code: 0
```

警告：

- Sass legacy JS API deprecation；
- 主 chunk >500 kB（Element Plus），不影响构建。

---

## 12. Playwright 实际结果

命令：

```text
node scripts/ms05-browser-check.mjs
MS05_BROWSER_OK
```

环境：

- 前端 Vite dev `127.0.0.1:5173`；
- 后端 `127.0.0.1:8080`；
- 独立 MySQL 测试库；
- Playwright 1.65.1 + 本机 Chrome。

已实际验证：

1. 地址簿页面打开；
2. 真实列表加载；
3. 真实 total；
4. 姓名搜索；
5. 电话搜索；
6. 地址搜索；
7. `%` 字面搜索；
8. `_` 字面搜索；
9. 50 个 Unicode 码点关键词；
10. 51 个码点被前端阻止；
11. 搜索重置；
12. 分页切换；
13. 每页数量修改；
14. 翻页清空选择；
15. 搜索清空选择；
16. 新建地址（51 码点前端校验 + 正常创建）；
17. 编辑地址；
18. 编辑后刷新保持新值；
19. 单条删除；
20. 批量选择；
21. 批量删除确认；
22. 批量请求只包含 2 个选中的 ID；
23. 删除当前页最后一条自动回上一页；
24. 无数据/无搜索结果 Empty；
25. 网络失败提示（Playwright 路由模拟 50002）。

**Mock 说明**：

- 解析、列表、搜索、新建、编辑、单删、批删均走真实后端/MySQL；
- 第 25 项网络失败使用 Playwright 路由模拟 50002，不是后端真实故障注入；
- 真实 50002/事务回滚由 MS-03.1 的 MySQL 测试覆盖。

控制台：无未预期 pageerror；模拟 500 的资源错误在断言中排除。

---

## 13. 数据库测试记录

- 测试前通过真实 API 清理并创建 11 条固定测试数据；
- 包含普通地址、`%` 地址、`_` 地址；
- 测试中创建/编辑/删除的均为测试数据；
- 测试结束清理测试数据；
- 测试库最终 0 行；
- 主库 `express_address_book` 未被测试写入，最终 0 行；
- 未执行 DROP DATABASE / TRUNCATE / 主库清空。

---

## 14. 未完成问题

1. Playwright 未纳入 CI，仍是独立 Node 脚本。
2. 网络错误测试使用路由模拟，未在实际后端故障注入。
3. 未做正式大数据量性能测试。
4. 移动端通过 Playwright 390px 检查；初次发现分页栏横向溢出，已通过 `flex-wrap` 修复，当前 `scrollWidth=390`。
5. 地址列表页没有列配置/密度切换等高级管理功能。
6. 用户登录、权限控制未实现（项目范围外）。

---

## 15. MS-06 准入建议

**有条件通过，可进入 MS-06。**

条件：

1. 继续复用 `AddressVO/PageResult`，分页字段为 number、ID 为 string；
2. 不修改后端已冻结的 P0 契约；
3. 搜索不自行执行 LikeEscaper，不把 URL keyword 二次转义；
4. 姓名继续 `Array.from`；
5. 保存继续 `toPayload`；
6. 翻页/size/搜索/重置/刷新清空选择；
7. 批量删除只提交当前页实际选择；
8. Playwright 必须继续实际执行，不得将路由模拟当成真实后端故障测试。

**MS-05 已完成并暂停，不进入 MS-06。**
