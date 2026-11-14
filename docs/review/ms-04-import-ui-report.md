# MS-04 批量地址录入前端报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-04 批量地址录入前端 |
| 范围 | 批量录入页面、解析预览、人工修正、确认保存；不含地址列表页 |
| 技术栈 | Vue 3 + TypeScript + Vite + Element Plus + Axios + Vue Router + SCSS |
| 构建 | `npm run build`：**通过** |
| 浏览器 | Playwright + 本机 Chrome：**MS04_BROWSER_OK** |

---

## 1. 页面结构

整体布局：

- 左侧深蓝导航栏：品牌、批量地址录入、地址簿管理占位；本机环境状态。
- 顶部栏：工作台面包屑、后端健康状态、本机演示标识。
- 主内容：
  - 页面标题与说明；
  - 200 / 3 / 0 三个定位指标；
  - 四步流程条；
  - 原始文本输入卡片；
  - 解析统计卡片（总记录、有效、无效、人工修改、可提交）；
  - 解析预览表格；
  - 底部保存操作栏。

设计依据 UI UX Pro Max 的 “Trust & Authority / Accessible & Ethical” 方向，采用深海军蓝 + 蓝色强调色、白色卡片、浅灰背景、可见焦点环和响应式布局。未使用 emoji 作为结构图标，图标为内联 SVG。

---

## 2. 新增和修改文件

### 新增

| 文件 | 用途 |
| --- | --- |
| `src/types/address.ts` | ParsedItem / ParseResult / ParsedRow / AddressSavePayload / AddressVO / PageResult |
| `src/api/addresses.ts` | `POST /api/addresses/parse`、`POST /api/addresses/batch` |
| `src/utils/addressPayload.ts` | `toPayload`、`isRowEdited`、`restoreRow`、签名 |
| `src/utils/addressValidation.ts` | 姓名码点/字符、电话、地址前端校验、提交条件 |
| `src/components/common/AppIcon.vue` | 专业线性 SVG 图标 |
| `src/components/import/AddressInputPanel.vue` | 原始文本输入、字符/行数、清空、示例、识别 |
| `src/components/import/ParseStatistics.vue` | 解析统计卡片 |
| `src/components/import/ParseResultTable.vue` | 可编辑预览表格、状态、错误、恢复、原文展开 |
| `src/components/import/SaveConfirmDialog.vue` | 保存确认、异常行原因、实际提交数 |
| `scripts/ms04-browser-check.mjs` | Playwright 端到端脚本 |
| `public/favicon.svg` | 品牌 favicon |

### 修改

| 文件 | 变更 |
| --- | --- |
| `src/views/ImportView.vue` | 完整批量录入页面与状态管理 |
| `src/layout/AppLayout.vue` | 左侧导航 + 顶部区域 + 响应式布局 + 健康状态 |
| `src/styles/index.scss` | 蓝白设计 token、Element Plus 表格/按钮/焦点覆盖 |
| `src/api/request.ts` | 增加 `ApiError`（code/violations/data）、超时/网络提示 |
| `src/types/api.ts` | Violation.lineNo 允许 null |
| `src/router/index.ts` | 标题改为“批量地址录入” |
| `src/views/NotFoundView.vue` | 返回批量录入 |
| `index.html` | favicon |
| `package.json` / `package-lock.json` | 增加 Playwright 1.65.1 开发依赖 |

---

## 3. API 接入情况

| 接口 | 前端使用 |
| --- | --- |
| `POST /api/addresses/parse` | `parseAddresses(rawText)` |
| `POST /api/addresses/batch` | `saveAddressBatch(items)` |
| `GET /api/health` | 顶部服务连接状态 |

保存请求体：

```json
{ "items": [{ "receiverName": "...", "phone": "...", "fullAddress": "...", "lineNo": 1 }] }
```

未调用列表/CRUD 接口，未开发列表页面。

---

## 4. TypeScript 类型

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
```

- 分页数量全部为 `number`；
- 地址 ID 为 `string`；
- `AddressSavePayload` 只包含 `receiverName/phone/fullAddress/lineNo`；
- `LineError` 与 `Violation` 分离。

---

## 5. Unicode 姓名校验

`getCodePointLength` 使用：

```ts
Array.from(value).length
```

姓名输入框：
- 无 `maxlength` 截断；
- 实时显示 `x / 50 码点`；
- 51 个 BMP 汉字提示超限；
- 50 个扩展平面汉字通过；
- 51 个扩展平面汉字拒绝；
- 前端正则允许中文、英文半角/全角姓名。

**实际发现的缺陷**：初版为姓名输入框设置了 `maxlength="80"`，导致 50 个扩展平面汉字（100 个 UTF-16 code unit）被浏览器截断为 40 个码点。已删除该属性，并通过 Playwright 验证 50 个扩展平面汉字显示为 `50 / 50`。

---

## 6. 电话输入规则

前端采用与后端 `AddressSaveDTO`/`PhoneExtractor` 对齐的规则，允许：

- 标准手机号；
- 座机；
- 脱敏手机号；
- `+86` 前缀；
- 空格、短横线；
- 中文/英文成对括号。

保存后以服务端规范化结果为数据库值；前端不自行维护另一套手机号格式。

---

## 7. isEdited 状态

`ParsedRow` 保存：

- `original`：后端解析的原始业务字段；
- 当前可编辑字段；
- 后端 `status/errors`；
- `rowKey/lineNo/rawLine`。

`isEdited` 不存储为独立布尔值，而是由：

```ts
isRowEdited(row)
```

实时比较当前字段与 `original` 派生。恢复原始值后自动变回 false；不依赖点击状态。

---

## 8. toPayload 规则

`toPayload(row)` 仅投影：

```ts
{
  receiverName: row.receiverName.trim(),
  phone: row.phone.trim(),
  fullAddress: row.fullAddress.trim(),
  lineNo: row.lineNo
}
```

不提交 `rowKey/original/isEdited/rawLine/rawKey/status/errors`，也不使用 `JSON.stringify(row)` 整体提交。

---

## 9. 保存确认逻辑

`SaveConfirmDialog` 显示：

- 实际提交数量；
- 人工修改数量；
- 原解析 INVALID 数量；
- 未修正行数；
- 每条未修正记录的行号与原因。

规则：
- 未修正 INVALID 不进入 `items`；
- 可提交数量为 0 时禁止保存；
- 点击保存先弹窗，不立即请求；
- 确认后才调用批量保存；
- 保存成功提示实际数量；
- 保存失败保留原始输入与编辑结果。

---

## 10. npm run build 结果

```text
npm run build
vue-tsc --noEmit + vite build
✓ built
exit code: 0
```

构建警告：
- Sass legacy JS API deprecation；
- 主 chunk >500 kB（Element Plus 打包体积），不影响构建通过。

---

## 11. Playwright 实际测试结果

命令：

```text
node scripts/ms04-browser-check.mjs
MS04_BROWSER_OK
```

环境：
- 前端 Vite dev `127.0.0.1:5173`；
- 后端 `127.0.0.1:8080`，独立 MySQL 测试库；
- Playwright 1.65.1，本机 Chrome channel。

已实际验证：

| 项 | 结果 |
| --- | --- |
| 首页打开 | 通过 |
| 单条联系人 | 通过 |
| 多条联系人 | 通过 |
| 中文逗号 | 通过 |
| 英文逗号 | 通过 |
| Tab | 通过 |
| 空行 | 跳过，不计记录 |
| 解析结果显示 | 通过 |
| 异常数据显示 | 通过 |
| 修改姓名/电话/地址 | 通过 |
| 恢复原始内容 | 通过 |
| 人工修改标记 | 通过 |
| 51 BMP / 50 扩展平面 Unicode | 通过 |
| 保存确认弹窗 | 通过 |
| INVALID 不进入提交 | 通过 |
| 保存成功 | 通过；实际写入 3 条测试记录 |
| 保存失败保留数据 | 通过（浏览器层模拟 50002） |
| 连续点击防重复 | 通过（模拟成功响应，请求计数 1） |
| 控制台 | 无未预期 pageerror；模拟 500 的资源消息已排除 |

测试数据在结束后清理：`express_address_book_test` 0 行；主库未被测试写入。

**真实性说明**：
- 解析与成功保存走真实后端/MySQL；
- 保存失败与防重复使用 Playwright 路由拦截模拟响应（因为浏览器脚本不修改后端）；
- 真实 50002/事务回滚已在 MS-03.1 的后端 MySQL 测试中验证。

---

## 12. 未完成问题

1. 地址簿列表管理页面未开发（本阶段范围外）。
2. Playwright 未纳入 CI；当前为独立 Node 脚本。
3. 保存失败/防重复的浏览器测试使用路由模拟，不是真实后端故障注入。
4. 未做 20000 字符/200 行的浏览器级边界测试；后端已有覆盖。
5. 未做正式性能压测。
6. 移动端仅做 390px 视觉检查，未建立移动端自动化测试。
7. 地址列表/分页前端接口尚未接入。

---

## 13. MS-05 准入结论

**有条件通过，可进入 MS-05。**

条件：
1. 复用 `AddressVO/PageResult` 类型，分页字段保持 number，ID 保持 string；
2. 列表页不得修改后端已冻结的 P0 契约；
3. 继续使用 `Array.from` 姓名计数；
4. 保存时继续使用 `toPayload`，不提交前端状态字段；
5. 翻页/改 size/搜索时清空选择；
6. Playwright 浏览器测试必须继续实际执行，不得用 Mock 结果代替。

**MS-04 已完成并暂停，不进入 MS-05 列表页开发。**
