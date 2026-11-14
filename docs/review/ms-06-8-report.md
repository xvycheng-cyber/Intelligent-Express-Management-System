# MS-06.8 实施报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-06.8 2.19 页面布局原则 |
| 依据 | `docs/architecture.md` §6.2～§6.4、MS-06 实施计划、MS-06.1～MS-06.7 报告 |
| 范围 | 只完成 2.19；不进入 MS-06.9 |
| 结论 | **2.19 完成；无阻塞；具备进入 MS-06.9 的条件**（待负责人/ChatGPT 复核） |

---

## 1. 实现原理

文件：`frontend/src/components/basics/LayoutPrinciplesDemo.vue`

2.19 不使用静态示意图，而是：

1. 在真实页面中查询 `.app-shell`、`.topbar`、`.sidebar`、`.app-main`；
2. 通过 `wb-layout-highlight` class 给真实 DOM 添加 `outline`；
3. 显示真实区域的尺寸测量值；
4. 根据真实 `window.innerWidth` 和 AppLayout 断点显示宽屏/中窄屏/窄屏说明；
5. 切换高亮、刷新测量、离开页面时清理 class。

高亮使用 `outline`，不占布局空间，不改变业务排版，也不拦截点击。

---

## 2. 真实 DOM 对应关系

| 原理区域 | 真实 DOM | 说明 |
| --- | --- | --- |
| 页面整体 | `.app-shell` | 整体 flex 容器；≤980px 改为上下结构 |
| 顶部栏 | `.topbar` | 面包屑、服务状态、本机演示；≤640px 精简状态项 |
| 侧边导航 | `.sidebar` | 宽屏 236px；≤980px 变为顶部横向导航 |
| 主内容 | `.app-main` | min(1440px, 100%)，包含页面标题、操作区、内容卡片 |
| 工作区 | `.workspace` | 顶栏 + 主内容 + 页脚的纵向容器 |

这些类名来自真实 `AppLayout.vue`，没有为演示虚构容器。

---

## 3. 样式实现

文件：`frontend/src/styles/basics.scss`

```css
.app-shell.wb-layout-highlight .topbar {
  outline: 3px solid #0ea5e9;
  outline-offset: -3px;
}

.app-shell.wb-layout-highlight .sidebar {
  outline: 3px solid #f59e0b;
  outline-offset: -3px;
}

.app-shell.wb-layout-highlight .app-main {
  outline: 3px solid #10b981;
  outline-offset: -3px;
}
```

同时提供：

- 图例（顶部栏/侧边导航/主内容区）；
- 真实测量指标；
- `.app-shell` / `.topbar` / `.sidebar` / `.app-main` 对应表；
- 响应式模式说明；
- 高亮开关与刷新测量按钮。

未修改业务布局；新增样式均 `.wb-*` 命名空间。

---

## 4. 响应式

真实 AppLayout 的实际行为：

| 宽度 | 实际布局 |
| --- | --- |
| 1366 | 左侧 236px 导航 + 右侧工作区；主内容 max 1440px |
| 980 | `.app-shell` 改为上下结构，侧栏变为顶部横向导航 |
| 760 | 与 980 相同上下结构；主内容缩窄 |
| 390 | 侧栏横向滚动；顶栏隐藏服务状态/用户信息；主内容单列 |

组件根据 `window.innerWidth` 显示：

- `宽屏：左侧固定导航 + 右侧工作区`
- `中窄屏：侧栏变为顶部横向导航`
- `窄屏：侧栏上下堆叠，顶栏连接/用户信息精简`

这些说明与 `AppLayout.vue` 的 980px / 640px 媒体查询一致。

---

## 5. 测试结果

### 构建

```text
npm run build
vue-tsc --noEmit + vite build
✓ built in 5.42s
exit code: 0
```

既有警告：

- Sass legacy JS API；
- 主 chunk >500 kB。

### MS-06.8 Playwright

```text
node scripts/ms06-8-browser-check.mjs
MS06_8_BROWSER_OK
```

覆盖：

- 2.19 状态“已实现”、2.18 状态“已实现”；
- 默认高亮开启；
- `.app-shell` 真实 class 添加；
- `.topbar` / `.sidebar` / `.app-main` 的 outline style/width；
- 真实尺寸测量 > 0；
- 关闭高亮后 outline 消失且布局宽度不变；
- 重新开启；
- 点击真实导航到 `/import`，离开后高亮 class 被清理；
- 1366/980/760/390 模式文字、真实区域存在、无页面级横向溢出；
- 控制台无错误。

### 回归

```text
node scripts/ms06-7-browser-check.mjs
MS06_7_BROWSER_OK

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

旧测试中“2.19 未开始”的断言已改为“2.18 已实现”。

### 后端

未修改后端，**未重跑 Maven 测试**；不把历史 146 基线写成本次结果。

### 数据库

- 测试库最终 0 行；
- 主库 0 行；
- 未执行破坏性数据库操作。

---

## 6. 截图

```text
docs/screenshots/2-19-principles.png
```

- 实际尺寸：1366×900；
- 真实浏览器视口截图；
- 显示真实 `.topbar`、`.sidebar`、`.app-main` 的三色高亮；
- 不包含调试面板或错误遮挡；
- 截图中页面滚动在顶部，以同时展示三个真实区域；2.19 的具体控制面板位于页面下方，交互测试已单独验证。

不在截图中伪造业务数据或布局。

---

## 7. 已知问题

1. 2.19 截图聚焦真实 AppLayout 的三区域高亮，未把页面下方的 2.19 控制面板塞进同一张 1366×900 视口；控制面板通过自动化与页面滚动验证；
2. 高亮使用 `outline`，只在 2.19 演示挂载期间存在；离开页面会清理；
3. 当前所有 MS-06.2～MS-06.8 改动均未提交 Git；
4. 主 chunk/Sass warning 为既有问题；
5. 未修改业务布局、API、数据库。

---

## 8. Git 状态

开始 HEAD：

```text
a5a23f5
```

MS-06.2～MS-06.7 改动在开始时均未提交，本阶段全部保留；未执行 reset/clean/checkout/restore/stash。

结束时新增/修改包括：

- `LayoutPrinciplesDemo.vue`；
- `WebBasicsView.vue` 2.19 状态/演示挂载；
- `basics.scss` `.app-shell.wb-layout-highlight` 与 `.wb-layout-*`；
- `ms06-8` 测试/截图脚本；
- `2-19-principles.png`；
- `ms-06-implementation-plan.md` 状态更新；
- 旧测试状态断言更新；
- 既有 MS-06.2～MS-06.7 未提交成果。

`git diff --check` 通过。

**未执行 Git commit、reset、clean、checkout、restore、stash 或 `git add .`。**

---

## 9. 是否具备进入 MS-06.9 的条件

**具备。**

- 2.19 基于真实 AppLayout 完成；
- 三个真实区域可高亮/关闭，DOM 对应准确；
- 四种宽度响应式通过；
- 构建、MS-06.8 Playwright、MS-06.7～MS-06.2、MS-04/MS-05 回归通过；
- 真实截图生成；
- 无 P0/P1 阻塞；
- 无新增依赖、无后端/API/数据库修改。

**本阶段停止，等待负责人/ChatGPT 审核后再进入 MS-06.9（全量联调、响应式和 Playwright 回归）。**
