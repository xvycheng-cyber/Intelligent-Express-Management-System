# MS-06.5 实施报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-06.5 绝对定位、定位上下文、fixed 与 sticky（2.11～2.15） |
| 依据 | `docs/architecture.md` §6.2～§6.4、MS-06 实施计划、MS-06.1～MS-06.4 报告 |
| 范围 | 只实现 2.11～2.15；不实现 2.16～2.19 |
| 结论 | **2.11～2.15 完成；无阻塞；具备进入 MS-06.6 的条件**（待负责人/ChatGPT 复核） |

---

## 1. 本阶段完成内容

- 2.11 绝对定位 absolute；
- 2.12 定位上下文；
- 2.13 固定定位 fixed；
- 2.14 粘性定位 sticky；
- 2.15 粘性约束矩形；
- 五种定位方式综合对比表（补充）；
- 2.11～2.15 状态更新为“已实现”；
- 五张真实浏览器截图；
- MS-06.2～MS-06.4、MS-04、MS-05 回归。

本阶段未实现 2.16～2.19，未修改后端、API、数据库或业务逻辑。

### 关于 2.15 标题的说明

课程任务书与已批准 `architecture.md` 的 2.15 原始标题是：

```text
2.15 粘性约束矩形
```

本阶段保留该课程编号/标题，实现真实 sticky 约束矩形实验；同时在 2.15 后新增“**五种定位方式综合对比**”补充表，覆盖 static、relative、absolute、fixed、sticky 的脱流、占位、参照、滚动和场景对比。这样不篡改课程原文标题，也满足本阶段的综合对比要求。

---

## 2. 修改和新增文件

### 新增组件

```text
frontend/src/components/basics/AbsolutePositionDemo.vue
frontend/src/components/basics/PositioningContextDemo.vue
frontend/src/components/basics/FixedPositionDemo.vue
frontend/src/components/basics/StickyPositionDemo.vue
frontend/src/components/basics/StickyConstraintDemo.vue
```

### 修改

```text
frontend/src/views/WebBasicsView.vue
frontend/src/styles/basics.scss
docs/review/ms-06-implementation-plan.md
frontend/scripts/ms06-3-browser-check.mjs   # 2.11 状态断言更新
frontend/scripts/ms06-4-browser-check.mjs   # 2.16 仍未开始的断言更新
```

### 新增测试/截图脚本

```text
frontend/scripts/ms06-5-browser-check.mjs
frontend/scripts/ms06-5-screenshots.mjs
```

### 新增截图

```text
docs/screenshots/2-11-absolute.png
docs/screenshots/2-12-absolute-context.png
docs/screenshots/2-13-fixed.png
docs/screenshots/2-14-sticky.png
docs/screenshots/2-15-sticky-rect.png
```

### 新增报告

```text
docs/review/ms-06-5-report.md
```

未新增 npm 依赖，未使用外部字体/图片/服务，未修改后端或数据库。

---

## 3. 每项课程要求的实现方式

### 2.11 绝对定位

文件：`AbsolutePositionDemo.vue`

实现：

- 真实 `position: absolute`；
- 容器 `position: relative` 提供包含块；
- 支持 top/right/bottom/left 与偏移量；
- 支持 static/absolute 切换；
- static 时元素占据 normal flow，后续元素在下方；
- absolute 时元素脱流，后续元素前移；
- 虚线框标注原位置（不参与布局）；
- 可调元素宽/高；
- 代码与真实 style 同步。

验证：

- computed `position` 为 absolute/static；
- static 模式下后续元素 top 更大；
- absolute 模式下 ghost 出现、后续元素上移；
- right 偏移代码同步。

### 2.12 定位上下文

文件：`PositioningContextDemo.vue`

实现：

- outer：`position: relative`；
- parent：可切换 `static` / `relative`；
- child：真实 absolute，top/left 可调；
- parent static 时，child 以 outer 的定位包含块为参照；
- parent relative 后，child 以 parent 为参照，位置发生变化；
- 两个带标签的嵌套层级，说明“最近的父元素 ≠ 最近的定位包含块”；
- 代码与 parent position 同步。

验证：

- parent computed position 切换；
- child 的 `getBoundingClientRect` 明显变化；
- 代码显示 `position: relative/static`。

### 2.13 固定定位

文件：`FixedPositionDemo.vue`

实现：

- 真实 `position: fixed` 小型徽标；
- 支持 top/right/bottom/left 选择与偏移；
- 页面滚动前后徽标相对视口位置不变；
- 滚动容器内放置真实 absolute 对比项，随容器内容滚动；
- `fixed` 与 `absolute` 差异可见；
- 固定徽标 `pointer-events: none`，不拦截点击；
- 通过 `IntersectionObserver` 只在 2.13 区域进入视口时显示，避免覆盖其他页面/章节；
- 支持隐藏徽标；
- 说明 transform 祖先可能改变 fixed 包含块的 caveat，但演示本身不添加 transform。

验证：

- fixed computed position；
- 页面滚动 80px 后 fixed rect 不变；
- scroll container 滚动后 absolute 对比项移动；
- 隐藏/恢复；
- 代码显示 fixed + right/bottom。

### 2.14 粘性定位

文件：`StickyPositionDemo.vue`

实现：

- 真实 `position: sticky`；
- 真实滚动容器（非 fixed、非 JS 监听）；
- top 阈值可调；
- 滚动区高度可调；
- “滚动到阈值后 / 回到顶部”；
- 滚动前普通流位置，达到阈值后吸附；
- 代码与实际 threshold 同步。

验证：

- computed `position: sticky`；
- 滚动后 sticky top = frame top + threshold；
- 阈值改为 50 后 top 随之变化；
- reset。

### 2.15 粘性约束矩形

文件：`StickyConstraintDemo.vue`

实现：

- 真实 sticky + 真实父容器（约束矩形）；
- 父容器高度可调；
- top 阈值可调；
- 真实滚动容器，内容足够；
- sticky 只允许在父容器范围内吸附，父容器底部把 sticky 推走；
- 父容器底部显示“约束边界”标注；
- 代码显示 parent height、sticky top。

验证：

- computed sticky；
- 滚动后 parent 上移；
- sticky bottom 不超过 parent bottom；
- 父容器改为 180px 后仍受边界约束；
- reset。

### 补充：五种定位方式对比

在 2.15 后新增综合对比表：

- static：不脱流，保留占位，无定位参照，随文档滚动；
- relative：不脱流，保留占位，自身原位置，随文档滚动；
- absolute：脱流，不保留占位，最近定位祖先，随包含块滚动；
- fixed：脱流，不保留占位，视口（transform 等例外），滚动时固定；
- sticky：不脱流，保留占位，最近滚动容器 + 阈值，阈值后吸附、受父容器限制。

该表作为补充，不改变 2.15 课程标题。

---

## 4. 关键 CSS 原理及实际代码位置

| 内容 | 代码位置 | 关键点 |
| --- | --- | --- |
| 2.11 absolute | `AbsolutePositionDemo.vue` `itemStyle` | absolute 脱流；原位置不保留；后续元素前移 |
| 2.12 containing block | `PositioningContextDemo.vue` `parentStyle` / `childStyle` | 父 static 不是定位祖先；relative 才是 |
| 2.13 fixed | `FixedPositionDemo.vue` `fixedStyle` | 固定相对视口；小徽标不拦截；区域进入视口才显示 |
| 2.14 sticky | `StickyPositionDemo.vue` `stickyStyle` | sticky 阈值吸附；真实滚动容器 |
| 2.15 constraint | `StickyConstraintDemo.vue` `stickyStyle` / parent height | sticky 只在其父容器约束矩形内吸附 |
| 样式 | `frontend/src/styles/basics.scss` `.wb-*` | 全部命名空间隔离 |

---

## 5. 自动化测试结果

### 构建

```text
npm run build
vue-tsc --noEmit + vite build
✓ built in 5.65s
exit code: 0
```

警告（既有）：

- Sass legacy JS API；
- 主 chunk >500 kB；
- `WebBasicsView` 懒加载 chunk 约 71.42 kB，主 chunk 约 1,133 kB；
- 未新增依赖。

### MS-06.5 Playwright

```text
node scripts/ms06-5-browser-check.mjs
MS06_5_BROWSER_OK
```

覆盖：

- 2.11 static/absolute computed position、ghost、后续元素前移、right 偏移、reset；
- 2.12 parent static/relative 切换、child 位置变化；
- 2.13 fixed 页面滚动位置不变、absolute 对比项滚动、隐藏/恢复；
- 2.14 sticky computed、滚动吸附、阈值变化、reset；
- 2.15 sticky 约束矩形、parent 滚动、边界不越界、短父容器；
- 五定位方式对比表；
- hash 导航/刷新；
- 1366/980/760/390 无页面级横向溢出；
- 控制台无错误。

### 开发中曾出现并修复

1. **2.13 absolute 对比项滚动断言失败**
   - 原因：测试用页面滚动改变位置，同时滚动容器 max scroll 较小；
   - 修正：直接操作滚动容器并断言 scrollTop 增加 + 元素位置变化。

2. **2.14 sticky 阈值断言失败**
   - 原因：点击按钮时 Playwright 将按钮滚入视口，frame top 在测量后变化；
   - 修正：直接设置滚动容器的 scrollTop，并在滚动后重新读取 frame rect。

3. **2.15 sticky 在父容器内消失**
   - 原因：父容器 `overflow: hidden` 自己建立了滚动容器，sticky 不再相对外层 frame 吸附；
   - 修正：移除父容器 `overflow: hidden`，让最近滚动容器仍是外层 frame；测试通过。

4. **2.13 fixed 徽标在其它章节出现**
   - 原因：fixed 徽标始终渲染；
   - 修正：IntersectionObserver 只在 2.13 区域进入视口时显示，避免遮挡其它页面/章节。

### 回归

```text
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

MS-06.3/06.4 的状态断言已更新：2.11 变为“已实现”，2.16 仍“未开始”。

### 后端

本阶段未修改后端，**未重跑 Maven 测试**；不得把历史 146 基线写成本次执行结果。

### 数据库

- 测试库最终 0 行；
- 主库 0 行；
- 未执行破坏性数据库操作。

---

## 6. 响应式检查结果

| 宽度 | 2.11～2.15 | 页面级横向溢出 |
| --- | --- | --- |
| 1366 | 通过 | 无 |
| 980 | 通过 | 无 |
| 760 | 通过 | 无 |
| 390 | 通过 | 无 |

- 定位演示容器内部可滚动/裁剪，页面本身无横向溢出；
- 2.12 父/子容器在窄屏收窄；
- 2.13 fixed 徽标只在区域可见时显示，且 `pointer-events:none`；
- 2.14/2.15 滚动容器保持明确高度和足够滚动距离；
- 代码区可换行，不破坏布局。

---

## 7. 截图文件清单

```text
docs/screenshots/2-11-absolute.png
docs/screenshots/2-12-absolute-context.png
docs/screenshots/2-13-fixed.png
docs/screenshots/2-14-sticky.png
docs/screenshots/2-15-sticky-rect.png
```

实际尺寸：

| 文件 | 尺寸 | 说明 |
| --- | --- | --- |
| `2-11-absolute.png` | 784×872 | section 截图 |
| `2-12-absolute-context.png` | 784×931 | section 截图 |
| `2-13-fixed.png` | 1366×900 | 视口截图，包含真实 fixed 徽标 |
| `2-14-sticky.png` | 784×848 | section 截图，滚动到吸附状态 |
| `2-15-sticky-rect.png` | 784×931 | section 截图，滚动到约束边界 |

均为真实浏览器截图，不是设计稿、手工拼图或代码编辑器截图。

---

## 8. MS-04 / MS-05 回归结果

| 页面 | 结果 |
| --- | --- |
| `/import` | `MS04_BROWSER_OK` |
| `/addresses` | `MS05_BROWSER_OK` |

业务页面未修改；`.wb-*` 样式隔离；AppLayout 导航正常；无新增控制台错误。

---

## 9. 已知问题及未解决风险

1. 2.16～2.19 未实现，符合本阶段范围；
2. 2.18 仍是骨架，最终线框/截图待 MS-06.7；
3. 2.13 fixed 徽标使用 IntersectionObserver 控制显示，这是可见性控制，不改变 fixed 定位原理；
4. 2.15 课程原题为“粘性约束矩形”，综合对比表是补充，不改变课程标题；
5. 2.12 展示 outer + parent 两层上下文；更深层的复杂嵌套未额外实现；
6. 2.11 原位置 ghost 是 CSS 绝对定位的标注层，不参与布局；
7. 2.13 fixed 祖先 transform 例外只做文字说明，未单独做 transform 开关；
8. 主 chunk warning、Sass warning 为既有问题；
9. 2.13 截图为 1366×900 视口截图，其它为 section 截图，尺寸不同；
10. 未重跑后端 Maven 测试，因为无后端改动。

---

## 10. Git 开始和结束状态

### 开始

```text
git status --short
 M docs/review/ms-06-implementation-plan.md
 M frontend/scripts/ms04-browser-check.mjs
 M frontend/src/components/common/AppIcon.vue
 M frontend/src/layout/AppLayout.vue
 M frontend/src/main.ts
 M frontend/src/router/index.ts
?? docs/review/ms-06-4-report.md
?? docs/review/ms-06.2-report.md
?? docs/review/ms-06.3-report.md
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

git rev-parse --short HEAD
a5a23f5
```

MS-06.2～MS-06.4 改动在开始时均未提交，本阶段全部保留；未执行 reset/clean/checkout/restore/stash。

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
?? docs/review/ms-06.5-report.md
?? docs/screenshots/
?? frontend/scripts/ms06-2-browser-check.mjs
?? frontend/scripts/ms06-3-browser-check.mjs
?? frontend/scripts/ms06-3-screenshots.mjs
?? frontend/scripts/ms06-4-browser-check.mjs
?? frontend/scripts/ms06-4-screenshots.mjs
?? frontend/scripts/ms06-5-browser-check.mjs
?? frontend/scripts/ms06-5-screenshots.mjs
?? frontend/src/components/basics/
?? frontend/src/styles/basics.scss
?? frontend/src/types/practice.ts
?? frontend/src/views/WebBasicsView.vue
?? frontend/src/views/WireframeView.vue
```

`git diff --check`：通过。

**未执行 Git commit、reset、clean、checkout、restore、stash 或 `git add .`。**

---

## 11. 是否具备进入 MS-06.6 的条件

**具备。**

- 2.11～2.15 全部完成；
- 五定位方式综合对比表完成；
- 定位演示可操作、可验证；
- 2.16～2.17 仍为未开始；
- 构建、MS-06.5 Playwright、MS-06.4/03/02、MS-04/MS-05 回归通过；
- 五张真实截图已生成；
- 无 P0/P1 阻塞；
- 无新增依赖、无外部素材、无后端/API/数据库修改。

**本阶段停止，等待负责人/ChatGPT 审核后再进入 MS-06.6（2.16～2.17）。**
