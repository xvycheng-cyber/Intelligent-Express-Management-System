# MS-06 全阶段正式审查报告

| 项目 | 内容 |
| --- | --- |
| 审查日期 | 2026-11-14 |
| 审查对象 | MS-06.1～MS-06.10、课程实践 2.1～2.19 |
| 审查方式 | 只读源码/文档/截图 + 实际构建/Playwright/MySQL 测试 + Git 状态核查 |
| 审查性质 | 独立正式技术审查，不是开发阶段的延续 |
| 总体结论 | **有条件通过：存在 1 项 P1 课程内容缺口，需整改后复审** |

---

## 1. 审查基本信息

### 1.1 工作区

```text
工作目录：E:\desktop\高级Web&J2EE期末
仓库：Intelligent-Express-Management-System
分支：main
HEAD：6dd2bdbfdb1ca912ccabee3b0f9acee5c1fdca27
最近提交：6dd2bdb ms06
前一提交：a5a23f5 MS-06.1
```

审查开始时工作区：

```text
git status --short
（空）

git diff --stat
（空）

git diff --check
通过
```

MS-06 全部修改已提交，未发现未说明的未提交/未跟踪业务文件；本审查只新增本报告。

### 1.2 审查方法

- 读取 MS-06.1～MS-06.10 文档、实施计划、范围调整、审计记录和最终报告；
- 逐项读取 `frontend/src/components/basics/`、`WebBasicsView.vue`、`WireframeView.vue`、`basics.scss`、路由和测试脚本；
- 实际执行前端构建、全部 MS-06 Playwright、MS04/MS05 回归、后端 146 测试；
- 检查真实 DOM/computed style/尺寸/滚动，而非只检查文字；
- 检查 21 张截图的文件存在性、解码、尺寸、SHA256 重复；
- 检查课程追踪表、阶段报告、截图命名与实际文件对应；
- 记录测试环境问题与失败，不把失败写成通过。

### 1.3 未能执行/不能完全验证的内容

- 没有逐像素 OCR 校验所有截图的每个文字区域；采用实际生成脚本、尺寸/哈希、页面/章节对应和抽样视觉检查；
- 未进行真实用户可用性测试/多浏览器矩阵测试；
- 没有 CI 环境；Playwright 只在本机 Chrome 上执行；
- 2.19 截图没有把章节控制面板放入同一视口（见 P2）；
- 2.18 implemented 截图只展示导入页，地址簿页通过页面内切换测试验证（见 P2）。

---

## 2. 总体结论

# 有条件通过

MS-06.1～MS-06.10 的文档、页面、测试和截图总体完整，MS04/MS05 未受影响；但 **2.2 block 的“相邻垂直 margin 折叠”没有独立可操作演示，也没有对应测试断言**，而它属于已经冻结的 architecture §6.2 第 2.2 项演示要点。因此不能直接判定“全部 19 节完成”。

需要：

1. 修复 2.2 的 margin collapse 演示/测量/测试；
2. 更新 `2-02-block.png` 和 2.2 相关报告/追踪表；
3. 复审通过后，才可正式结束 MS-06。

---

## 3. MS-06.1～MS-06.10 阶段审查表

| 阶段 | 阶段报告 | 报告中的文件是否真实存在 | 目标/证据 | 结论 |
| --- | --- | --- | --- | --- |
| MS-06.1 | 无独立 `ms-06.1-report.md`；由 `ms-06-implementation-plan.md`、`ms-06-scope-adjustment.md`、`ms-06-pre-audit-report.md` 决策记录承载 | 是 | 19 节追踪、2.18 范围调整、实施计划均存在 | **完成，但缺独立阶段报告（P2）** |
| MS-06.2 | `ms-06.2-report.md` | 是 | 路由、导航、骨架、CSS；build/browser 证据 | 完成 |
| MS-06.3 | `ms-06.3-report.md` | 是 | 2.1～2.5；测试/截图存在 | **部分缺口：2.2 margin collapse（P1）** |
| MS-06.4 | `ms-06-4-report.md` | 是 | 2.6～2.10；测试/截图存在 | 完成 |
| MS-06.5 | `ms-06-5-report.md` | 是 | 2.11～2.15；测试/截图存在 | 完成 |
| MS-06.6 | `ms-06-6-report.md` | 是 | 2.16～2.17 + z-index；测试/截图存在 | 完成 |
| MS-06.7 | `ms-06-7-report.md` | 是 | 2.18 Plan/Implemented；两张图真实存在 | 完成 |
| MS-06.8 | `ms-06-8-report.md` | 是 | 2.19 真实 AppLayout 高亮；测试/截图存在 | 完成 |
| MS-06.9 | `ms-06-9-report.md` | 是 | 全量联调、响应式、回归 | 完成，但漏检 2.2 collapse（P2 测试覆盖） |
| MS-06.10 | `ms-06-final-delivery-report.md` | 是 | 截图/文档/论文素材/交付汇总 | **完成，但最终报告把 2.2 缺口写成已完成（P1 文档不一致）** |

阶段报告均有真实文件；MS-06.1 没有独立报告是文档形式差异，不是功能缺失。最终交付报告关于“19 节全部完成”的表述与 2.2 实现不一致，需纠正。

---

## 4. 2.1～2.19 课程要求审查表

| 编号 | 实现/组件 | 原理准确性 | 交互/测试 | 截图 | 结论与问题 |
| --- | --- | --- | --- | --- | --- |
| 2.1 | `InlineDemo.vue` | width/height 普通非替换 inline 不占布局；垂直 margin 不参与、垂直 padding/border 绘制不撑开行框；水平方向有效 | 文本/数量/字号/间距/颜色；MS06.3+9 真实宽度/高度/换行 | 2-01 | 通过 |
| 2.2 | `BlockDemo.vue` | block 独占行、width:auto、固定宽度、居中描述正确；**相邻垂直 margin 折叠只在文字说明，没有实际演示** | 只测宽度/居中/重置，无 collapse 断言 | 2-02 | **P1：margin collapse 未实现/未验证** |
| 2.3 | `InlineBlockDemo.vue` | inline-block 宽高生效、空白间隙、原生换行、inline 对照正确 | 宽度/高度/空白/换行/对照；MS06.3+9 | 2-03 | 通过 |
| 2.4 | `FlexDemo.vue` | 主轴/交叉轴、direction/justify/align/wrap/gap 说明正确 | 方向/对齐/换行/轴标签；MS06.3+9 | 2-04 | 通过 |
| 2.5 | `GridDemo.vue` | grid 轨道、fr、gap、span 说明正确 | 列模式/轨道/间距/跨列；MS06.3+9 | 2-05 | 通过 |
| 2.6 | `BoxModelDemo.vue` | 四层结构、content-box 尺寸公式、margin 在边框盒外；明确 margin 折叠 caveat | 尺寸/实际 offset/padding/border/margin；MS06.4+9 | 2-06 | 通过（margin collapse 仅说明，P3） |
| 2.7 | `BoxSizingDemo.vue` content-box | width/height 是 content；边框盒加 padding+border；margin 不计入 | 实际尺寸/公式/重置；MS06.4+9 | 2-07 | 通过 |
| 2.8 | `BoxSizingDemo.vue` border-box | width/height 是边框盒；content 反算；margin 不计入 | 实际尺寸/不变边框盒/内容缩小；MS06.4+9 | 2-08 | 通过 |
| 2.9 | `NormalFlowDemo.vue` | static、block、inline、后续元素受前序影响；top/left 对 static 无效 | 实际布局/后续 top/静态无效；MS06.4+9 | 2-09 | 通过 |
| 2.10 | `RelativePositionDemo.vue` | relative 保留原占位，视觉偏移；后续不动 | ghost/rect/后续；MS06.4+9 | 2-10 | 通过 |
| 2.11 | `AbsolutePositionDemo.vue` | absolute 脱流、原位置不占位、后续前移 | absolute/static、right 偏移、reset；MS06.5+9 | 2-11 | 通过（ghost 标注文字为空的视觉清晰度 P2） |
| 2.12 | `PositioningContextDemo.vue` | 外层 relative + parent static/relative 的包含块变化正确 | parent 切换、child rect；MS06.5+9 | 2-12 | 通过（多层为 outer+parent 两层，P3） |
| 2.13 | `FixedPositionDemo.vue` | fixed 视口定位、absolute 对比、transform caveat 说明 | 页面滚动 rect、滚动容器 absolute、隐藏/清理；MS06.5+9 | 2-13 | 通过（transform caveat 未单独演示，P2） |
| 2.14 | `StickyPositionDemo.vue` | sticky 阈值前普通流、阈值后吸附；真实滚动容器 | threshold/frame/scroll；MS06.5+9 | 2-14 | 通过 |
| 2.15 | `StickyConstraintDemo.vue` + 五定位方式对比表 | 课程原题粘性约束矩形正确；对比表 static/relative/absolute/fixed/sticky 的脱流/占位/参照/滚动/场景正确 | 约束边界、短父容器、对比表；MS06.5+9 | 2-15 | 通过（用户“定位综合对比”以补充表满足，P2 说明） |
| 2.16 | `OffsetPropertiesDemo.vue` | static/relative/absolute 四方向偏移及参照正确 | 四方向实际位移、mode 切换、code；MS06.6+9 | 2-16 | 通过 |
| 2.17 | `OffsetConflictDemo.vue` + `StackingContextDemo.vue` | left/right/auto/fixed、margin auto、z-index 堆叠上下文正确；未以 DOM 顺序伪装 | 覆盖/拉伸/居中/z-index elementFromPoint；MS06.6+9 | 2-17、2-17-stacking | 通过（z-index 为补充，P2 说明） |
| 2.18 | `WireframeView.vue` | 保留课程原题与批准调整；plan 低线框、implemented 真实页面；无外部素材 | plan/implemented/query/刷新/真实 DOM；MS06.7+9 | 2-18-plan、2-18-implemented | 通过（implemented 截图只展示导入页，P2） |
| 2.19 | `LayoutPrinciplesDemo.vue` | 高亮真实 `.topbar/.sidebar/.app-main`；响应式说明匹配 AppLayout | 三区域 outline/尺寸/开关/清理/四宽度；MS06.8+9 | 2-19 | 通过（截图未包含控制面板，P2；不是原理错误） |

### 2.2 详细缺陷证据

**实现位置**：

```text
frontend/src/components/basics/BlockDemo.vue
frontend/src/styles/basics.scss  .wb-block-demo__item
frontend/scripts/ms06-3-browser-check.mjs 2.2 断言
```

实际 `blockStyle`：

```ts
marginBottom: `${gap.value}px`
```

每个块只有 `margin-bottom`，没有 `margin-top`，也没有两个相邻块各带上下 margin 的对照；因此**浏览器虽然会真实执行 margin，但没有可观察的相邻垂直 margin 折叠现象**。

测试只验证：

- computed display block；
- auto/fixed width；
- 固定宽度 + margin auto 居中；
- reset；
- 没有断言相邻块之间的实际距离、上下 margin 合并结果。

截图 `2-02-block.png` 只显示块和间距，不能证明 margin collapse。

`ms-06.3-report.md` 的已知问题中已经写过“2.2 的 margin 折叠只在结论中说明，未单独做折叠对照实验”，但 `ms-06-final-delivery-report.md` 仍把 2.2 标为完成，属于 **P1 文档与实际不一致**。

---

## 5. CSS 原理与源码审查

### 5.1 优点

- `.wb-*` 命名空间覆盖实践样式；
- `basics.scss` 集中；
- 未发现 `!important`；
- 未发现 `styles/basics.scss` 对 `.address-*`/`.import-*` 的宽泛选择器污染；
- 未发现用 JS 计算坐标代替 absolute/relative/fixed/sticky 位置；
- 2.13 fixed 只是 IntersectionObserver 控制**徽标可见性**，定位仍由 CSS fixed 完成；
- 2.19 用 DOM 查询和 outline 高亮真实布局，未改变布局；
- `FixedPositionDemo` 有 observer 清理；
- `LayoutPrinciplesDemo` 有 resize listener 清理；
- 有 `prefers-reduced-motion` 规则。

### 5.2 关键原理核查

| 原理 | 代码 | 结果 |
| --- | --- | --- |
| 2.1 inline | `InlineDemo.vue` inlineStyle/BoxDecoration | 宽高/垂直 padding 说明准确 |
| 2.2 margin collapse | `BlockDemo.vue` blockStyle | **缺失可观察 collapse 实验** |
| 2.4 flex 轴 | `FlexDemo.vue` axis labels/stageStyle | 正确 |
| 2.5 grid | `GridDemo.vue` gridTemplate | 正确 |
| 2.7/2.8 box-sizing | `BoxSizingDemo.vue` contentWidth/borderBoxWidth | 正确 |
| 2.9 static | `NormalFlowDemo.vue` position static | 正确 |
| 2.10 relative | `RelativePositionDemo.vue` relativeStyle | 正确 |
| 2.11 absolute | `AbsolutePositionDemo.vue` absolute style | 正确 |
| 2.12 containing block | `PositioningContextDemo.vue` parentStyle | 正确 |
| 2.13 fixed | `FixedPositionDemo.vue` fixedStyle | 正确 |
| 2.14 sticky | `StickyPositionDemo.vue` stickyStyle | 正确 |
| 2.15 constraint | `StickyConstraintDemo.vue` parent height | 正确 |
| 2.17 z-index | `StackingContextDemo.vue` contextStyle | 正确 |
| 2.18 plan/implemented | `WireframeView.vue` | 正确，无外部仿制 |
| 2.19 real DOM | `LayoutPrinciplesDemo.vue` | 正确 |

---

## 6. 功能与交互审查

### 导航/路由

- `/web-basics`、`/web-basics/wireframe` 可访问；
- 19 节导航顺序 2.1～2.19 正确；
- 2.1～2.17、2.19 hash 可进入并刷新；
- 2.18 `?mode=plan`、`?mode=implemented`、`?page=import/addresses` 可切换并刷新；
- `/servlet-demo` 仍是 404，没有虚假路由。

### 交互

- 各章节控件、动态代码、reset 已由 MS06.3～MS06.6 测试覆盖；
- 2.13 fixed 徽标只在区域可见时出现，离开后消失；
- 2.19 高亮/关闭/恢复/离开清理通过；
- 2.18 真实页面预览为 inert 只读，打开真实页面链接可用；
- 没有发现控件完全无响应或状态跨章节残留。

### 2.2 例外

2.2 目前不是“交互失效”，而是**课程要求中的 margin collapse 子行为未提供独立演示和测试**。

---

## 7. 响应式审查

实际 MS06.9 在 1366/980/760/390 执行：

| 宽度 | 19 节入口/演示 | 页面级横向溢出 | 2.18 | 2.19 |
| --- | --- | --- | --- | --- |
| 1366 | 通过 | 无 | 通过 | 通过 |
| 980 | 通过 | 无 | 通过 | 通过 |
| 760 | 通过 | 无 | 通过 | 通过 |
| 390 | 通过 | 无 | 通过 | 通过 |

允许的内部滚动：

- 2.13/2.14/2.15 滚动容器；
- 2.18 plan board；
- 2.17 定位舞台；
- 固定宽 flex/grid/inline-block 演示；
- 代码/表格。

未发现整体页面横向滚动。

---

## 8. 自动化测试与业务回归

### 8.1 实际执行

```text
npm run build
✓ built in 5.00s
exit code: 0

node scripts/ms06-9-browser-check.mjs
MS06_9_BROWSER_OK
node scripts/ms06-8-browser-check.mjs
MS06_8_BROWSER_OK
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

mvn -B clean test
Tests run: 146, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### 8.2 测试环境问题

第一次浏览器回归时后端 `target` jar 不存在（`mvn clean test` 后未重新 package），Vite 代理返回 500，相关脚本失败。随后执行 `mvn package -DskipTests` 并重启后端，所有脚本通过。该失败是环境/启动顺序问题，不是业务代码回归。

### 8.3 测试有效性

**有效部分**：

- 使用 computed display/position；
- 使用 `getBoundingClientRect` 验证真实尺寸/位置；
- 使用 `elementFromPoint` 验证真实覆盖；
- 使用真实滚动容器；
- 使用真实页面/API；
- 覆盖 reset、动态代码、响应式。

**不足**：

- 多个脚本大量使用 `waitForTimeout`，存在时序脆弱性（P2）；
- 2.2 没有 collapse 行为断言（P1）；
- 2.15 对比表仅检查关键词，未逐格校验（P2）；
- 2.19 截图与控制面板不在同一视口，测试覆盖但截图证据不足（P2）。

### 8.4 业务回归

- MS04：批量录入、解析、修正、保存通过；
- MS05：列表、搜索、分页、编辑、删除、批删通过；
- 主库 0 行；测试库最终 0 行；
- 未使用真实业务数据做破坏性测试。

---

## 9. 截图与论文素材审查

### 9.1 文件完整性

21 张 PNG 全部存在、Pillow 可解码、SHA256 无重复：

```text
2-01 2-02 2-03 2-04 2-05 2-06 2-07 2-08 2-09 2-10
2-11 2-12 2-13 2-14 2-15 2-16 2-17-offset-override
2-17-stacking 2-18-wireframe-plan 2-18-implemented 2-19
```

- `2-18-original.png` 不存在；
- 2.18 只有批准的两张图；
- 2.13/2.18-implemented/2.19 为 1366×900 视口；其余为 784 宽 section 截图；
- 没有调试面板/文字报错。

### 9.2 内容匹配

- 2.1～2.10、2.11～2.17 截图与对应组件/阶段报告一致；
- 2.13 截图包含真实 fixed 徽标；
- 2.18 plan 是真实浏览器渲染的线框板；
- 2.18 implemented 是真实 AppLayout + ImportView；
- 2.19 是真实 AppLayout 三区域 outline。

### 9.3 不足

- **2-02-block.png** 不展示 margin collapse，不能作为该原理的证据（P1 同一问题）；
- **2-19-principles.png** 不包含 2.19 控制面板，控制面板由 Playwright 验证（P2）；
- **2-18-implemented.png** 只展示导入页；地址簿页通过测试验证，但截图未覆盖（P2）；
- **2-11-absolute.png** ghost 标注为空白虚线，缺少“原位置”文字，教学可读性一般（P2）；
- **2-15-sticky-rect.png** 主要展示约束矩形，不展示五定位方式对比（对比表无独立截图，P2）。

### 9.4 论文素材

- 论文题目保持“快递地址簿录入程序设计”；
- 2.18 的范围调整未被写成外部网页仿制；
- Web 基础实践与快递业务功能的边界在最终报告中有说明；
- 未生成夸大功能的完整论文；
- 2.2 缺口使该节素材尚不完整。

---

## 10. Git 与工程状态

### 10.1 审查开始

```text
branch: main
HEAD: 6dd2bdbfdb1ca912ccabee3b0f9acee5c1fdca27
git status --short: 空
git diff --stat: 空
git diff --check: 通过
```

MS-06 修改已提交，未发现未说明的未提交/未跟踪业务文件。

### 10.2 审查结束

新增本审查报告后，工作区只有一个未跟踪文件：

```text
docs/review/ms-06-final-audit-report.md
```

没有修改业务代码、测试代码、截图、数据库或已有阶段报告。未执行 commit/push/reset/clean/checkout/stash。

### 10.3 依赖/构建

- 未新增 npm 依赖；
- 前端构建通过，5.00s；
- 后端 146 通过；
- 既有 Sass legacy API 与大 chunk 警告未新增处理。

---

## 11. 问题清单

### P0

无。

### P1

**P1-1 2.2 缺失可观察的垂直 margin collapse 演示与测试**

- 阶段：MS-06.3 / 2.2
- 文件：`frontend/src/components/basics/BlockDemo.vue`、`frontend/src/styles/basics.scss`、`frontend/scripts/ms06-3-browser-check.mjs`
- 位置：`blockStyle` 只有 `marginBottom`，无 `marginTop`/双块上下 margin 对照；测试无 collapse 断言
- 实际表现：页面只显示 block 间距、宽度和居中；文字提到 margin 折叠，但没有现象
- 正确预期：至少能展示两个相邻块同时设置上下 margin 时，最终间距=max/折叠规则，而不是简单相加
- 影响：architecture §6.2 的 2.2 演示要点未完全实现；截图也无法证明
- 修复建议：在 BlockDemo 增加 top/bottom margin 控件或独立“margin collapse”对照，使用真实相邻块；增加 computed rect 测试，重新生成 2-02 截图

**P1-2 最终交付报告对 2.2 的完成声明与已知缺口矛盾**

- 文件：`docs/review/ms-06-final-delivery-report.md`、`docs/review/ms-06.3-report.md`
- 实际：ms-06.3 已承认“2.2 未单独做折叠对照实验”，最终报告仍把 19 节全部标完成
- 影响：正式交付结论过度声明
- 修复：在修复 P1-1 后同步所有报告；若暂不修复，最终报告必须改为“有条件完成”

### P2

1. **2.19 截图不含控制面板**：`2-19-principles.png` 只显示真实 AppLayout 三区域高亮，控制面板在页面下方；截图证据有限。
2. **2.18 implemented 截图只展示导入页**：地址簿页通过预览切换测试，但没有单独实现截图。
3. **2.15 对比表无独立截图**：2.15 仍以粘性约束矩形截图为主，五定位方式对比只以表格/测试存在。
4. **2.11 ghost 缺少文字标签**：虚线只标记原位置，未在图上直接显示“原位置”，需依赖下方说明。
5. **测试时序依赖**：多个脚本使用 `waitForTimeout`，对机器负载敏感；已通过但增加误报风险。
6. **2.15 对比表测试较弱**：只检查关键词，不逐行校验所有属性。
7. **MS-06.1 无独立阶段报告**：由 plan/scope/pre-audit 承担，追溯性略弱。
8. **2.13 transform 祖先例外仅文字说明**，没有单独开关实验。
9. **2.19 高亮依赖临时 class**：若异常卸载可能残留（当前有 cleanup，风险低）。

### P3

- 主 chunk >500 kB、Sass legacy warning；
- 代码块/section 截图尺寸不统一（设计允许）；
- Playwright 未纳入 CI；
- 可访问性可进一步做 axe/键盘专项审计；
- 2.12 仅两层上下文，更深嵌套可扩展。

---

## 12. 整改建议

### 必须修复（复审前）

1. **2.2 margin collapse**
   - 在 `BlockDemo.vue` 增加相邻块上下 margin 的独立对照/测量；
   - 显示“24px + 16px → 实际 24px（或按真实规则）”而不是简单相加；
   - 增加 `ms06-3` / `ms06-9` 的真实 rect 断言；
   - 重新生成 `2-02-block.png`；
   - 更新 ms-06.3、ms-06-9、最终交付报告和追踪表。

2. **最终交付声明**
   - 修复前不得把 2.2 标为完成；
   - 修复后再给出“19/19 完成”的正式结论。

### 建议修复（不阻塞其他章节）

- 2.19 增加能同时看到三区域高亮和控制面板的截图/说明；
- 2.18 可选择补充地址簿 implemented 截图，但不改变“只交付两张”的批准限制时，需在报告中保持说明；
- 2.15 对比表与 2.17 stacking 截图的证据说明继续明确；
- 替换/补充 2.11 ghost 的文字标签；
- 逐步减少固定 `waitForTimeout`。

### 可选优化

- Playwright CI；
- chunk 拆分/Element Plus 按需加载；
- 更系统的可访问性测试；
- 2.12 更深层定位上下文演示。

---

## 13. 最终交付判定

### 是否可以正式结束 MS-06？

**暂不可以。** 需修复 P1-1（2.2 margin collapse）并修正 P1-2 的完成声明后，重新审查。

### 是否可以进入下一里程碑？

**不建议。** 课程追踪表有 1 项 P1 内容缺口，正式交付标准尚未满足。

### 是否需要整改后再次审查？

**需要。** 复审至少应验证：

- 2.2 真实相邻块 margin collapse 行为；
- 对应测试断言和 `2-02` 截图；
- 报告/追踪表不再把该节写成已完成而未说明缺口。

### 已知限制

- 2.18 按批准范围使用本系统原创线框，不是外部网页仿制；
- 2.15/2.17 的补充内容与课程原题的关系已在报告中说明；
- 2.19 截图聚焦真实 AppLayout 区域，不包含控制面板；
- Playwright 为本机 Chrome，无 CI；
- 2.2 之外未发现 P0/P1 功能/业务回归问题。

**审查结束。未修改任何项目源码、测试、截图、数据库或已有文档，只新增本报告。**
