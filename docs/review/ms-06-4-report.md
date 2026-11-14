# MS-06.4 实施报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-06.4 CSS 盒模型、常规流与相对定位（2.6～2.10） |
| 依据 | `docs/architecture.md` §6.2～§6.4、MS-06 实施计划、MS-06.1～MS-06.3 报告 |
| 范围 | 只实现 2.6～2.10；不实现 2.11～2.19 |
| 结论 | **2.6～2.10 完成；无阻塞；具备进入 MS-06.5 的条件**（待负责人/ChatGPT 复核） |

---

## 1. 本阶段完成内容

- 2.6 盒模型各部分；
- 2.7 `box-sizing: content-box`；
- 2.8 `box-sizing: border-box`；
- 2.9 常规流布局（静态定位）；
- 2.10 相对定位；
- 2.6～2.10 在 `/web-basics` 中挂载为真实可交互组件；
- 2.6～2.10 章节状态更新为“已实现”并支持 hash 导航；
- 五张真实浏览器截图；
- MS-06.3、MS-06.2、MS-04、MS-05 回归；
- MS-06 追踪状态更新。

**未实现**：2.11～2.19、Servlet、2.18 最终线框。

---

## 2. 修改和新增文件

### 新增组件

```text
frontend/src/components/basics/BoxModelDemo.vue
frontend/src/components/basics/BoxSizingDemo.vue
frontend/src/components/basics/NormalFlowDemo.vue
frontend/src/components/basics/RelativePositionDemo.vue
```

### 修改

```text
frontend/src/views/WebBasicsView.vue
frontend/src/styles/basics.scss
docs/review/ms-06-implementation-plan.md
frontend/scripts/ms06-3-browser-check.mjs   # 2.6 状态由未开始改为已实现
```

### 新增测试/截图脚本

```text
frontend/scripts/ms06-4-browser-check.mjs
frontend/scripts/ms06-4-screenshots.mjs
```

### 新增截图

```text
docs/screenshots/2-06-box-parts.png
docs/screenshots/2-07-content-box.png
docs/screenshots/2-08-border-box.png
docs/screenshots/2-09-normal-flow.png
docs/screenshots/2-10-relative.png
```

### 新增报告

```text
docs/review/ms-06-4-report.md
```

### 未修改

- 后端 Java、API、数据库；
- MS-04 批量录入业务逻辑；
- MS-05 地址簿业务逻辑；
- 2.11～2.19 的演示；
- npm 依赖。

---

## 3. 每项课程要求的实现方式

### 2.6 盒模型各部分

文件：`BoxModelDemo.vue`

实现：

- 真实 `div` 目标元素，`box-sizing: content-box`；
- content 内层彩色区域；
- padding 由目标元素背景表现；
- border 由目标元素真实边框表现；
- margin 由父容器背景区域表现；
- 滑块调整 content width/height、padding、border、margin；
- 显示 content、计算 border-box、实际 `offsetWidth/offsetHeight`、计算 margin-box；
- 图例区分四层；
- 使用 `flow-root` 容器隔离 margin 折叠，便于单盒观察；
- 明确说明 margin 不计入边框盒，垂直 margin 可能折叠，不能无条件相加。

验证：

- 默认 content 240×120、padding 18、border 6 → 边框盒 288×168；
- 修改 padding/border 后边框盒增大；
- 修改 margin 不改变边框盒；
- 实际 `offsetWidth/offsetHeight` 与计算值一致。

### 2.7 标准盒模型 content-box

文件：`BoxSizingDemo.vue`（`mode="content-box"`）

实现：

- 真实 `box-sizing: content-box`；
- 控制 width、height、padding、border、margin；
- 内容区尺寸 = CSS width/height；
- 边框盒宽 = width + 2×padding + 2×border；
- 边框盒高同理；
- margin 额外显示，但不属于边框盒；
- 实际 `offsetWidth/offsetHeight` 与计算对照；
- 与 2.8 共用同一视觉标准。

### 2.8 替代盒模型 border-box

文件：`BoxSizingDemo.vue`（`mode="border-box"`）

实现：

- 真实 `box-sizing: border-box`；
- CSS width/height 表示边框盒尺寸；
- content 宽 = width − 2×padding − 2×border；
- content 高同理；
- margin 不包含在 width/height；
- 改 padding/border 时边框盒不变、内容区缩小；
- 与 2.7 并排/同标准对照（页面顺序 2.7→2.8）。

### 2.9 常规流布局

文件：`NormalFlowDemo.vue`

实现：

- 真实 `position: static`；
- block 区块纵向排列；
- inline 文本在文字行中排列；
- 后续块受前面高度/间距影响；
- 控制区块数、auto/固定宽度、高度、间距、行内元素数；
- 额外提供 `top/left` 滑块，验证 static 下偏移无效；
- 不使用 absolute/fixed/transform/JS 坐标；
- 明确说明 static 与脱离文档流的区别。

### 2.10 相对定位

文件：`RelativePositionDemo.vue`

实现：

- 两栏对照：static 与 relative；
- 四个方向 top/right/bottom/left 可选；
- 偏移量 0～60px；
- 右侧虚线 ghost 标注原位置；
- relative 元素真实 `position: relative` 视觉偏移；
- 后续元素位置保持；
- 不使用 JS 修改坐标；
- 明确说明原始空间仍保留。

---

## 4. 关键 CSS 原理及实际代码位置

| 内容 | 代码位置 | 关键点 |
| --- | --- | --- |
| 2.6 四层盒模型 | `BoxModelDemo.vue` `targetStyle` / `calculated` | content-box；border box = content + 2×padding + 2×border；margin 在边框盒外 |
| 2.6 实际尺寸 | `BoxModelDemo.vue` `measure()` | `offsetWidth/offsetHeight` |
| 2.7 content-box | `BoxSizingDemo.vue` `isContentBox` / `borderBoxWidth` | width = content；padding/border 撑大边框盒 |
| 2.8 border-box | `BoxSizingDemo.vue` `contentWidth` / `borderBoxWidth` | width = border box；content 反向计算 |
| 2.9 常规流 | `NormalFlowDemo.vue` `blockStyle` / 模板 | static、block、inline；无绝对定位 |
| 2.10 相对定位 | `RelativePositionDemo.vue` `relativeStyle` | `position:relative` + 单方向偏移；原位置保留 |
| 全部样式 | `frontend/src/styles/basics.scss` `.wb-*` | 不污染业务页面 |

---

## 5. 自动化测试结果

### 构建

```text
npm run build
vue-tsc --noEmit + vite build
✓ built in 4.96s
exit code: 0
```

警告（既有）：

- Sass legacy JS API；
- 主 chunk >500 kB；
- `WebBasicsView` 懒加载 chunk 约 54.22 kB，主 chunk 约 1,133 kB；
- 未新增依赖，未为消除警告重构。

### MS-06.4 Playwright

```text
node scripts/ms06-4-browser-check.mjs
MS06_4_BROWSER_OK
```

覆盖：

- 2.6 四层结构、计算/实际尺寸、padding/border 增长、margin 不改变边框盒；
- 2.7 content-box 边框盒公式、content 尺寸；
- 2.8 border-box 固定边框盒、content 缩小；
- 2.9 block/inline computed display、static position、后续元素推移、static top/left 无效；
- 2.10 static/relative computed position、top/right 偏移、ghost 原位置、后续元素不动；
- 动态代码同步；
- reset；
- hash 导航/刷新；
- 1366/980/760/390 无页面级横向溢出；
- 控制台无错误。

开发中曾出现：

```text
2.10 right offset missing
```

原因是 ghost 使用 `inset:0` 时宽度铺满 lane，和 190px 的 relative box 不同宽；
改为 ghost 使用与 box 相同的 190×64（窄屏 100% 宽）后通过。

### 回归

```text
node scripts/ms06-3-browser-check.mjs
MS06_3_BROWSER_OK

node scripts/ms06-2-browser-check.mjs
MS06_2_BROWSER_OK

node scripts/ms04-browser-check.mjs
MS04_BROWSER_OK

node scripts/ms05-browser-check.mjs
MS05_BROWSER_OK
```

MS-06.3 脚本原断言 2.6 为“未开始”，本阶段已更新为“已实现”，并在测试中同时检查 2.11 仍为“未开始”。

### 后端

本阶段未修改后端，**未重跑 Maven 测试**；不得将历史 146 基线描述为本次执行结果。此前 MS-06.2 阶段已实际通过 146。

### 数据库

- 测试库最终 0 行；
- 主库 0 行；
- 未修改结构，未执行破坏性清理。

---

## 6. 响应式检查结果

| 宽度 | 2.6～2.10 可见 | 页面级横向溢出 | 控件 |
| --- | --- | --- | --- |
| 1366 | 通过 | 无 | 可操作 |
| 980 | 通过 | 无 | 可操作 |
| 760 | 通过 | 无 | 可操作 |
| 390 | 通过 | 无 | 可操作 |

说明：

- 2.6/2.7/2.8 的 box stage 在窄屏使用容器内滚动；
- 2.10 两栏在 ≤760px 变成单列；
- 代码区使用 `pre-wrap`，窄屏可读；
- 控件在 390px 下单列排列，没有重叠或页面溢出。

---

## 7. 截图文件清单

```text
docs/screenshots/2-06-box-parts.png
docs/screenshots/2-07-content-box.png
docs/screenshots/2-08-border-box.png
docs/screenshots/2-09-normal-flow.png
docs/screenshots/2-10-relative.png
```

实际尺寸（Playwright 1366×900 窗口，section 截图）：

| 文件 | 尺寸 |
| --- | --- |
| `2-06-box-parts.png` | 784×1085 |
| `2-07-content-box.png` | 784×1026 |
| `2-08-border-box.png` | 784×1012 |
| `2-09-normal-flow.png` | 784×1104 |
| `2-10-relative.png` | 784×825 |

均为真实浏览器截图，不是设计稿或手工拼图。

---

## 8. MS-04 / MS-05 回归结果

| 页面 | 结果 |
| --- | --- |
| `/import` | `MS04_BROWSER_OK` |
| `/addresses` | `MS05_BROWSER_OK` |

业务页面没有修改；新增样式全部 `.wb-*`；AppLayout 导航状态正常；无新增控制台错误。

---

## 9. 已知问题及未解决风险

1. 2.11～2.19 未实现，符合本阶段范围；
2. 2.18 仍是骨架，最终线框/截图待 MS-06.7；
3. 2.6 的 margin 折叠只做文字说明，未提供独立折叠实验；当前用 flow-root 隔离以便观察单盒；
4. 2.6 的 margin-box 是计算值，不是 `getBoundingClientRect` 直接返回值；垂直 margin 折叠时不能简单套用；
5. 2.10 的 ghost 是 CSS 绝对定位的**标注层**，不是用绝对定位模拟 relative 布局；真实页面布局由 relative 元素承担；
6. 主 chunk warning、Sass warning 为既有问题；
7. 2.7/2.8 的截图宽度为 784；使用统一 1366×900 浏览器窗口；
8. 未重跑后端 Maven 测试，原因是没有后端改动；
9. 当前所有 MS-06.2～MS-06.4 改动均未提交 Git。

---

## 10. Git 开始和结束状态

### 开始

```text
git status --short
 M frontend/scripts/ms04-browser-check.mjs
 M frontend/src/components/common/AppIcon.vue
 M frontend/src/layout/AppLayout.vue
 M frontend/src/main.ts
 M frontend/src/router/index.ts
?? docs/review/ms-06.2-report.md
?? docs/review/ms-06.3-report.md
?? docs/screenshots/
?? frontend/scripts/ms06-2-browser-check.mjs
?? frontend/scripts/ms06-3-browser-check.mjs
?? frontend/scripts/ms06-3-screenshots.mjs
?? frontend/src/components/basics/
?? frontend/src/styles/basics.scss
?? frontend/src/types/practice.ts
?? frontend/src/views/WebBasicsView.vue
?? frontend/src/views/WireframeView.vue

git rev-parse --short HEAD
a5a23f5
```

MS-06.2/MS-06.3 改动在开始时仍未提交，本阶段全部保留；未执行 reset/clean/checkout/restore/stash。

### 结束

```text
 M docs/review/ms-06-implementation-plan.md
 M frontend/scripts/ms04-browser-check.mjs
 M frontend/src/components/common/AppIcon.vue
 M frontend/src/layout/AppLayout.vue
 M frontend/src/main.ts
 M frontend/src/router/index.ts
?? docs/review/ms-06.2-report.md
?? docs/review/ms-06.3-report.md
?? docs/review/ms-06.4-report.md
?? docs/screenshots/
?? frontend/scripts/ms06-2-browser-check.mjs
?? frontend/scripts/ms06-3-browser-check.mjs
?? frontend/scripts/ms06-3-screenshots.mjs
?? frontend/scripts/ms06-4-browser-check.mjs
?? frontend/scripts/ms06-4-screenshots.mjs
?? frontend/src/components/basics/
?? frontend/src/styles/basics.scss
?? frontend/src/types/practice.ts
?? frontend/src/views/WebBasicsView.vue
?? frontend/src/views/WireframeView.vue
```

`git diff --check`：通过。

**未执行 Git commit、reset、clean、checkout、restore、stash 或 `git add .`。**

---

## 11. 是否具备进入 MS-06.5 的条件

**具备。**

- 2.6～2.10 全部完成；
- 真实浏览器可操作、尺寸/计算/布局可验证；
- 2.6～2.10 状态已更新为“已实现”；
- 2.11～2.19 保持未开始/骨架/待实现；
- 构建、MS-06.4 Playwright、MS-06.3/MS-06.2、MS-04/MS-05 回归通过；
- 五张真实截图已生成；
- 无 P0/P1 阻塞；
- 无新增依赖、无外部素材、无业务 API/数据库修改。

**本阶段停止，等待负责人/ChatGPT 审查批准后再进入 MS-06.5（2.11～2.15）。**
