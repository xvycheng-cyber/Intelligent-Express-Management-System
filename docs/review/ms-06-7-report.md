# MS-06.7 实施报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-06.7 2.18 原创线框图设计与实现 |
| 依据 | `docs/architecture.md` §6.2～§6.4、MS-06 实施计划、2.18 范围调整 |
| 范围 | 只完成 2.18；不实现 2.19 |
| 结论 | **2.18 完成；无阻塞；具备进入 MS-06.8 的条件**（待负责人/ChatGPT 复核） |

---

## 1. 课程原文要求

课程任务书附录3 的原始标题：

```text
2.18 使用线框风格仿制XXX网页的布局
```

任务书附录4 曾提供外部学校首页参考图。课程原文没有提供：
- 外部网页源文件；
- 必须仿制的完整区域清单；
- 宽度、断点、字体、截图规格；
- 外部素材授权。

因此不能把项目后来推导的实现写成课程原文，也不能宣称完成了外部网页仿制。

---

## 2. 项目调整依据

项目负责人已批准：

1. 采用 `docs/architecture.md` §6.2～§6.4 作为项目实现依据；
2. 2.18 不仿制任何外部网页；
3. 不使用湖北工程学院新技术学院首页；
4. 不采集外部网页截图、Logo、Banner、图片或字体；
5. 改为使用线框风格设计**快递通自身**的真实页面布局；
6. 只交付：
   - `2-18-wireframe-plan.png`
   - `2-18-implemented.png`
7. 取消 `2-18-original.png`；
8. Plan 与 Implemented 必须严格分开。

依据文件：

- [`ms-06-scope-adjustment.md`](ms-06-scope-adjustment.md)
- [`ms-06-pre-audit-report.md`](ms-06-pre-audit-report.md) 决策记录
- [`ms-06-implementation-plan.md`](ms-06-implementation-plan.md)

---

## 3. 计划视图实现

文件：`frontend/src/views/WireframeView.vue`

Plan 模式：

- 真实浏览器渲染的 Vue 线框设计板，不是图片；
- 低保真灰阶、细边框、文字标签；
- 体系结构：
  - 顶部系统栏；
  - 左侧工作台 / 课程实践导航；
  - 主内容区；
  - 智能录入页区域；
  - 地址簿页区域；
  - 导航关系说明。
- 智能录入页包含：
  - 页面标题/操作区；
  - 四步流程；
  - 多行文本输入区；
  - 解析结果预览/可编辑表格；
  - 确认保存与异常提示。
- 地址簿页包含：
  - 页面标题/新建/刷新；
  - 总记录/当前页/已选中指标；
  - 姓名/电话/地址搜索；
  - 地址列表与行操作；
  - 分页。
- 导航关系明确显示：
  - 批量地址录入 → 地址簿管理；
  - Web 基础实践（进行中）；
  - Servlet 映射演示（待实现，不创建虚假路由）。
- 无外部 Logo/Banner/图片/字体；
- 没有以截图图片作为主要内容；
- 所有区域来自当前真实业务路由与组件。

---

## 4. 实现视图实现

Implemented 模式：

- 外层是真实 `AppLayout`（真实侧栏、顶栏、导航）；
- 下方以 **真实 Vue 页面组件**展示：
  - `ImportView`（真实批量录入页）；
  - `AddressListView`（真实地址簿页）；
- 通过“智能录入页 / 地址簿页”切换；
- 预览容器使用 `inert` + `pointer-events:none`，避免在预览中误触业务写操作；
- 提供“打开真实页面 →”链接，进入真实路由操作；
- 页面内容不是截图、不是静态图片、不是手工拼图；
- 地址簿页的数据来自真实 API（测试时使用隔离测试库，未伪造截图数据）；
- 外层 AppLayout 是真实组件，不是仿制的外部网页。

### 路由与模式

```text
/web-basics/wireframe?mode=plan
/web-basics/wireframe?mode=implemented
/web-basics/wireframe?mode=implemented&page=import
/web-basics/wireframe?mode=implemented&page=addresses
```

- 默认模式为 plan；
- `?mode` 切换后刷新仍保持；
- `?page` 保持真实页面的预览选择；
- 没有创建 `/servlet-demo` 或虚假 Servlet 页面。

---

## 5. 两张截图的来源和路径

均来自 Playwright + 本机 Chrome 的真实浏览器运行：

```text
docs/screenshots/2-18-wireframe-plan.png
docs/screenshots/2-18-implemented.png
```

| 文件 | 尺寸 | 来源 |
| --- | --- | --- |
| `2-18-wireframe-plan.png` | 1028×655 | `wireframe-plan-board` 元素真实截图 |
| `2-18-implemented.png` | 1366×900 | implemented 模式真实视口截图，含真实 AppLayout 与真实 ImportView |

- **未生成 `2-18-original.png`**；
- 没有外部网页截图；
- 没有把计划图当作实现截图；
- 两张图来自同一个页面组件的两种模式。

---

## 6. 与真实业务页面的对应关系

| Plan 区域 | 真实页面 |
| --- | --- |
| 顶部系统栏 | AppLayout topbar |
| 左侧导航 | AppLayout sidebar |
| 智能录入页 | `/import` → `ImportView` |
| 地址簿页 | `/addresses` → `AddressListView` |
| 搜索/列表/分页 | `AddressSearchBar` / `AddressTable` / Element Plus 分页 |
| 新建/编辑/删除 | `AddressFormDialog` / 删除确认 |
| Web 基础实践入口 | `/web-basics` |
| Servlet 映射演示 | 仅规划入口，待 MS-07；没有虚假路由 |

Implemented 页面的真实预览与 Plan 的区域一一对应；截图中的导入页来自真实组件，不是静态模板。

---

## 7. Playwright 测试结果

### 构建

```text
npm run build
vue-tsc --noEmit + vite build
✓ built in 6.62s
exit code: 0
```

既有警告：

- Sass legacy JS API；
- 主 chunk >500 kB；
- `WireframeView` lazy chunk 约 7.41 kB；
- 未新增依赖。

### MS-06.7

```text
node scripts/ms06-7-browser-check.mjs
MS06_7_BROWSER_OK
```

覆盖：

- 默认 plan 模式；
- plan board、import/address 区域、导航关系；
- 无 `<img>`、无 `2-18-original` 文本；
- implemented 模式；
- 真实 `.import-page` / `.address-page` DOM；
- preview 容器 `inert`；
- 查询参数 mode/page；
- 刷新保持；
- 直接 URL；
- `/servlet-demo` 不存在；
- 1366/980/760/390 无页面级横向溢出；
- 控制台无错误。

### 回归

```text
node scripts/ms06-6-browser-check.mjs
MS06_6_BROWSER_OK

node scripts/ms06-5-browser-check.mjs
MS06_5_BROWSER_OK

node scripts/ms06-4-browser-check.mjs
MS06_4_BROWSER_OK

node scripts/ms06-3-browser-check.mjs
MS06_3_BROWSER_OK

node scripts/ms06-2-browser-check.mjs
MS06_2_BROWSER_OK

node scripts/ms04-browser-check.mjs
MS04_BROWSER_OK

node scripts/ms05-browser-check.mjs
MS05_BROWSER_OK
```

MS-06.2 测试已更新为新的 plan 文案和 implemented 真实页面断言；不再检查旧占位符。

### 后端

本阶段未修改后端，**未重跑 Maven 测试**；不把历史 146 基线写成本次执行结果。

### 数据库

- 测试库最终 0 行；
- 主库 0 行；
- 未执行破坏性数据库操作。

---

## 8. 响应式结果

| 宽度 | Plan | Implemented import | Implemented addresses | 页面级横向溢出 |
| --- | --- | --- | --- | --- |
| 1366 | 通过 | 通过 | 通过 | 无 |
| 980 | 通过 | 通过 | 通过 | 无 |
| 760 | 通过 | 通过 | 通过 | 无 |
| 390 | 通过 | 通过 | 通过 | 无 |

Plan board 在窄屏使用容器内横向滚动；真实页面预览使用内部滚动，不撑破页面。

---

## 9. 范围偏差

- **没有仿制外部网页**：这是项目负责人批准的替代实现，不是课程原文外部仿制的完成声明；
- 课程标题保留“使用线框风格仿制XXX网页的布局”，2.18 的实际交付为快递通自身原创线框；
- Implemented 截图展示真实 ImportView；地址簿页面也可在页面内切换预览，但第二张截图只选择导入页作为代表，未拼图伪造“两页同时展示”；
- 未实现 2.19；
- 未创建虚假 Servlet 路由；
- 未修改业务 API/数据库/后端。

---

## 10. 已知问题

1. Implemented 预览是真实组件，但为只读（inert），实际交互需打开对应真实路由；
2. implement 截图只展示导入页，地址簿页通过页面内切换验证，未额外生成第二张实现截图；
3. Plan board 在 390px 下需要内部横向滚动；
4. 主 chunk/Sass warning 为既有问题；
5. 当前所有 MS-06.2～MS-06.7 改动均未提交 Git。

---

## 11. Git 状态

开始 HEAD：

```text
a5a23f5
```

MS-06.2～MS-06.6 改动在开始时均未提交，本阶段全部保留；未执行 reset/clean/checkout/restore/stash。

结束时新增/修改包括：

- `WireframeView.vue` 完整 2.18 实现；
- `basics.scss` 的 `.wb-plan-*` / `.wb-live-*`；
- `ms06-7` 测试与截图脚本；
- MS-06.7 报告；
- `2-18-wireframe-plan.png`、`2-18-implemented.png`；
- `ms-06-implementation-plan.md` 状态更新；
- MS-06.2 测试对新 plan/implemented 的适配；
- 既有 MS-06.2～MS-06.6 未提交成果。

`git diff --check` 通过。

**未执行 Git commit、reset、clean、checkout、restore、stash 或 `git add .`。**

---

## 12. 是否具备进入 MS-06.8 的条件

**具备。**

- 2.18 Plan/Implemented 完成且严格分开；
- 两张真实截图生成；
- 没有 `2-18-original.png`，没有外部素材；
- 真实 AppLayout + 真实页面组件；
- 构建、MS-06.7 Playwright、MS-06.2～MS-06.6、MS-04/MS-05 回归通过；
- 无 P0/P1 阻塞；
- 无新增依赖、无后端/API/数据库修改。

**本阶段停止，等待负责人/ChatGPT 审核后再进入 MS-06.8（2.19 布局原则）。**
