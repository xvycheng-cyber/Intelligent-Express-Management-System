# MS-06 实施计划与 19 节需求追踪

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-06.1 设计冻结 |
| 依据 | 课程任务书原文标题 + 项目负责人批准的 `docs/architecture.md` §6.2～§6.4 + `docs/review/ms-06-scope-adjustment.md` |
| 课程范围 | 2.1～2.19；2.20 归 MS-07；2.21 归系统/论文；2.22 不采用 |
| 设计工具 | UI UX Pro Max 1.x（本机技能） |
| 代码状态 | **本阶段不开发页面、不改路由、不改业务代码、不新增依赖** |

> 课程任务书原文与项目设计严格分开：
> - 课程原文标题保持原样；
> - 2.1～2.17、2.19 采用项目负责人批准的 `architecture.md` §6.2 设计；
> - 2.18 采用负责人批准的“本系统原创线框”替代实现；
> - 不把项目推导写成课程原文。

---

## 1. UI UX Pro Max 使用记录

### 1.1 实际安装状态

```text
C:\Users\Chengzi\.agents\skills\ui-ux-pro-max
├── SKILL.md
├── scripts/search.py
├── data/
├── tests/
└── core.py / design_system.py / reasoning_contract.py / validate_data.py
```

- 技能实际存在且可读取；
- 使用技能自带 Python 搜索脚本；
- 未安装新依赖；
- 未把技能推荐直接变成 npm 包或外部字体依赖；
- 设计时仅使用其设计系统/可访问性/响应式原则，业务与课程要求优先。

### 1.2 MS-06 设计基调

技能推荐方向（企业工具/教育技术）被调整为与现有快递通一致的**深海军蓝 + 蓝色强调 + 浅灰背景 + 白色卡片 + 瑞士式清晰网格**，不采用学术衬线字体，不引入外部字体。

---

## 2. MS-06 统一设计规范

### 2.1 色彩体系

沿用现有项目 token，不新增业务色：

| 用途 | Token/值 |
| --- | --- |
| 侧栏 | `--color-sidebar: #0f172a` |
| 主色 | `--color-primary: #0369a1` |
| 主色深 | `--color-primary-strong: #075985` |
| 主色浅 | `--color-primary-soft: #e0f2fe` |
| 背景 | `--color-bg: #f8fafc` |
| 卡片 | `--color-surface: #ffffff` |
| 边框 | `--color-border: #e2e8f0` |
| 主文字 | `--color-text: #0f172a` |
| 次文字 | `--color-text-secondary: #475569` |
| 弱文字 | `--color-text-muted: #64748b` |
| 成功 | `--color-success: #047857` |
| 警告 | `--color-warning: #b45309` |
| 危险 | `--color-danger: #b91c1c` |

**CSS 演示不得为了美观改变要展示的颜色原理**；演示区颜色只为可读性和层次服务。

### 2.2 字体与字号层级

- 延续 `var(--font-sans)`：`Segoe UI / PingFang SC / Microsoft YaHei / Noto Sans SC / system-ui`；
- 不引入 Google Fonts、外部字体或图标库；
- 代码块使用 `ui-monospace, SFMono-Regular, Consolas, monospace`。

| 层级 | 字号 | 用途 |
| --- | --- | --- |
| 页面标题 | 28～32px | `/web-basics`、`/web-basics/wireframe` |
| 章节标题 | 18～20px | DemoSection |
| 组标题 | 14～16px | 演示区/结论/代码 |
| 正文 | 13～14px | 说明、结论 |
| 辅助 | 11～12px | 截图名、标签、提示 |
| 代码 | 12～13px | CodeBlock |

### 2.3 间距体系

`4 / 8 / 12 / 16 / 20 / 24 / 32 / 48` px。

- 章节间距 24～32px；
- 卡片内边距 16～24px；
- 演示区内部 12～16px；
- 章节导航与内容间距 24px；
- 移动端水平边距 16px。

### 2.4 圆角与阴影

- 卡片：12px；
- 演示区/代码块：8～10px；
- 按钮/输入：8px；
- 阴影：仅用现有 `--shadow-card`，不新增重阴影；
- 线框页 2.18 不使用圆角/阴影，保持线框低保真。

### 2.5 页面布局

```text
AppLayout
├── 左侧 236px 导航
├── 顶部 64px 顶栏
└── 主内容（max 1440px，padding 28～32px）
    └── WebBasicsView
        ├── 左侧 sticky 章节导航（约 220px）
        └── 右侧章节内容
            └── DemoSection（目标/演示/结论/代码）
```

- `/web-basics` 复用 AppLayout；
- `/web-basics/wireframe` 独立顶层路由，必要时 `meta.fullBleed` 或独立容器；
- 不影响 `/import`、`/addresses` 宽度/布局；
- 2.18 线框页不使用业务表格/弹窗组件。

### 2.6 交互状态

- 可点击元素有 hover/focus-visible/active/disabled；
- focus ring 使用现有 `rgba(3,105,161,.32)`；
- 演示控件有明确标签、当前值/状态；
- loading 不遮挡课程演示；
- 尊重 `prefers-reduced-motion`；
- 所有演示不用只有颜色传达信息，需文字/形状/标签辅助。

### 2.7 响应式规则

| 宽度 | 规则 |
| --- | --- |
| ≥1280px | 章节导航 + 内容双列 |
| 980～1279px | 减少间距，演示区仍并排 |
| 760～979px | 章节导航转顶部横向/滚动 |
| 390px | 单列，演示控件可换行，无页面级横向溢出；局部表格/线框可容器内横向滚动 |

课程最低验收 1366×768；390px 作为额外质量检查，不作为课程硬性移动端要求。

### 2.8 样式隔离

- 所有实践样式必须 `.wb-*` 命名空间；
- 集中在 `frontend/src/styles/basics.scss`；
- 不修改 `styles/index.scss` 中业务 token 语义；
- 不污染 `.address-*`、`.import-*`、Element Plus 业务样式；
- 演示代码使用真实 CSS/HTML，不用截图代替演示。

---

## 3. 19 节需求追踪表

> 字段说明：
> - **项目实现依据**：课程任务书原文标题 + 批准的 `architecture.md` §6.2（2.18 另有 §6.3/§6.4 与范围调整）；
> - **实际演示内容/交互/预期**：按批准设计细化，开发时必须真实呈现；
> - **关键代码**：计划组件/样式，不是已存在代码；
> - **截图**：最终交付文件名（真实截图，后续生成）。

| 编号 | 课程原文标题 | 项目实现依据 | 实际演示内容 | 交互操作 | 预期现象 | 关键代码 | 截图文件 | 验收标准 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2.1 | 行内显示类型 | 原文标题 + architecture §6.2 | 段落内 `<span>` 设置 width/height/margin/padding/border；旁边保留普通行内文字 | 点击“显示辅助线”/“增加上下 padding”切换；不改变核心 CSS | width/height 被忽略；上下 margin 不生效；上下 padding/border 与相邻行重叠；左右 padding/margin/border 推开文字 | `DisplayTypeDemo.vue`，`.wb-inline` | `2-01-inline.png` | 能观察到 inline 的宽高无效、上下间距不推开行、左右有效 |
| 2.2 | 区块显示类型 | 原文标题 + architecture §6.2 | 两个 `<div>` 独占行、撑满父容器；`margin:0 auto` 居中；相邻块 margin 折叠 | 切换宽度/是否居中；显示 margin 辅助线 | 块独占一行；width 生效；auto 居中；上下 margin 折叠取大值 | `DisplayTypeDemo.vue`，`.wb-block` | `2-02-block.png` | 能观察到块级换行、宽度、居中、margin 折叠 |
| 2.3 | 行内区块显示类型 | 原文标题 + architecture §6.2 | 多个 inline-block 盒子同行；元素间空白产生间隙；vertical-align 改变基线 | 切换 HTML 空白/`vertical-align` | width/height 生效且不换行；空白产生约 4px 间隙；vertical-align 可见 | `DisplayTypeDemo.vue`，`.wb-inline-block` | `2-03-inline-block.png` | 能观察到 inline-block 尺寸生效、空白间隙、垂直对齐差异 |
| 2.4 | 弹性显示类型 | 原文标题 + architecture §6.2 | flex 主轴/交叉轴；justify-content 多值；flex:1；gap 与 margin | 按钮切换 justify-content；切换 gap/margin；切换 flex:1 | 主轴分布变化；flex:1 等分；gap 与 margin 表现不同 | `FlexGridDemo.vue`，`.wb-flex` | `2-04-flex.png` | 能观察到 5 种 justify-content、flex:1、gap/margin 差异 |
| 2.5 | 网格显示类型 | 原文标题 + architecture §6.2 | 3 列 grid；`grid-column:span 2`；gap；Flex 对比 | 切换 span；切换 Flex/Grid 对比 | 二维网格成立；跨列生效；gap 间隔一致 | `FlexGridDemo.vue`，`.wb-grid` | `2-05-grid.png` | 能观察到 grid 二维布局、span、gap 与 Flex 一维差异 |
| 2.6 | 盒模型各部分 | 原文标题 + architecture §6.2 | 四层着色 content/padding/border/margin；四边不同值；总占位计算 | 调整 padding/border/margin 滑块 | 四层可见；总宽高随盒模型变化；标注计算 | `BoxModelDemo.vue`，`.wb-box-*` | `2-06-box-parts.png` | 能区分 content/padding/border/margin 并读到总尺寸 |
| 2.7 | 标准盒模型 | 原文标题 + architecture §6.2 | content-box：width 只算内容；总宽 = width + padding + border | 切换 width/padding/border 数值 | 总宽变化符合 content-box 公式 | `BoxModelDemo.vue`，`.wb-content-box` | `2-07-content-box.png` | 能验证 content-box 总宽计算 |
| 2.8 | 替代盒模型 | 原文标题 + architecture §6.2 | 同一 width 下 border-box 总宽固定、内容区压缩；与 content-box 并排 | 切换 box-sizing 并排对照 | border-box 总宽 = 设定 width；内容区变小 | `BoxModelDemo.vue`，`.wb-border-box` | `2-08-border-box.png` | 能对比两种盒模型同 width 的尺寸差异 |
| 2.9 | 常规流布局（静态定位） | 原文标题 + architecture §6.2 | 块级纵向堆叠、行内横向排列；static 下 top/left 无效；margin 折叠 | 输入 top/left；切换 static/relative 对照 | static 下偏移无效；块级顺序、行内同行；margin 折叠 | `NormalFlowDemo.vue`，`.wb-normal-*` | `2-09-normal-flow.png` | 能观察到常规流与 static 偏移无效、margin 折叠 |
| 2.10 | 相对定位 | 原文标题 + architecture §6.2 | relative + top/left 偏移；原位置仍占位 | 调整 top/left 滑块 | 元素偏移，后续元素不填补原空间 | `PositioningDemo.vue`，`.wb-relative` | `2-10-relative.png` | 能观察到相对定位偏移与占位 |
| 2.11 | 绝对定位 | 原文标题 + architecture §6.2 | absolute 脱离流，后续元素上移；定位祖先前/后差异 | 切换 absolute/relative；切换父级定位 | 元素脱离文档流；无定位祖先参照初始包含块 | `PositioningDemo.vue`，`.wb-absolute` | `2-11-absolute.png` | 能观察到脱流、后续填补、定位参照 |
| 2.12 | 改变绝对定位的定位上下文 | 原文标题 + architecture §6.2 | 同一 absolute 子元素；父 static ↔ relative；padding 参照 | 切换父级 position；切换 padding | 子元素位置明显变化；参照 padding box | `PositioningDemo.vue`，`.wb-context-*` | `2-12-absolute-context.png` | 能观察到父级定位上下文改变带来的位置变化 |
| 2.13 | 固定定位 | 原文标题 + architecture §6.2 | 滚动容器内 fixed 元素；相对视口不动；transform 祖先退化坑 | 滚动演示容器；切换 fixed/static；切换祖先 transform | fixed 元素位置不随滚动；transform 祖先时退化 | `FixedStickyDemo.vue`，`.wb-fixed` | `2-13-fixed.png` | 能观察到 fixed 与滚动关系及 transform 坑 |
| 2.14 | 粘性定位 | 原文标题 + architecture §6.2 | sticky top:0 在滚动容器吸附；阈值前普通流 | 滚动容器；切换 sticky/static | 到达阈值前普通，到达后吸附 | `FixedStickyDemo.vue`，`.wb-sticky` | `2-14-sticky.png` | 能观察到 sticky 阈值行为 |
| 2.15 | 粘性约束矩形 | 原文标题 + architecture §6.2 | sticky 在不同高度父容器中的吸附/被推走 | 调整父容器高度；滚动 | 父容器高度不足时 sticky 被推出约束矩形 | `FixedStickyDemo.vue`，`.wb-sticky-parent` | `2-15-sticky-rect.png` | 能观察到约束矩形对 sticky 的限制 |
| 2.16 | 定位属性上下左右 | 原文标题 + architecture §6.2 | 分别设置 top/right/bottom/left；四项同时 + width:auto 拉伸；static 无效 | 按钮切换单个 offset；切换宽度 auto/固定 | 单边偏移；宽高 auto 时拉伸；static 无效果 | `OffsetConflictDemo.vue`，`.wb-offset-*` | `2-16-offsets.png` | 能观察到四个方向偏移、auto 拉伸、static 无效 |
| 2.17 | 定位属性之间的覆盖影响 | 原文标题 + architecture §6.2 | absolute left+right：auto 拉伸/固定宽 left 生效；top+bottom；relative left+right；margin:auto 居中 | 切换宽/auto、left/right、position | 覆盖规则与预期一致；auto 拉伸；固定宽 left 优先；margin:auto 居中 | `OffsetConflictDemo.vue`，`.wb-offset-*` | `2-17-offset-override.png` | 能逐项复现覆盖规则和居中效果 |
| 2.18 | 使用线框风格仿制XXX网页的布局 | **负责人批准调整**：architecture §6.2/§6.3/§6.4 + `ms-06-scope-adjustment.md` | **快递通自身**低保真线框：系统框架、智能录入页、地址簿页、导航关系（Web 基础实践/Servlet 映射演示标注规划态）；无外部素材 | 切换“系统框架/智能录入/地址簿/导航关系”视图 | 看到真实系统区域、层级、对齐、间距；无外部 Logo/图/真实数据 | `WireframeView.vue`，`.wb-wire-*` | `2-18-wireframe-plan.png`、`2-18-implemented.png` | 两图真实；无 `2-18-original.png`；无外部素材；区域与真实页面一致 |
| 2.19 | 布局原则思考 | 原文标题 + architecture §6.2 | 容器/区域、栅格/间距、border-box、避免绝对定位主布局、z-index、组件化、响应式取舍；逐条对照真实系统 | 切换原则条目，高亮对应真实区域 | 每条原则能指向本系统实际布局/代码；不空谈 | `LayoutPrinciples.vue`，`.wb-principles` | `2-19-principles.png` | 19 条原则均能对应真实页面/代码并可检查 |

### 3.1 2.18 截图与证据链

| 文件 | 内容 | 生成要求 |
| --- | --- | --- |
| `2-18-wireframe-plan.png` | 本系统原创线框设计板 | 真实浏览器渲染后截图；灰块/细边框/文字标注；无外部素材 |
| `2-18-implemented.png` | `/web-basics/wireframe` 运行截图 | 真实浏览器；与 plan 区域一致；无外部素材 |
| `2-18-original.png` | **取消** | 不生成、不采集外部网页 |

### 3.2 当前实现状态（2026-11-14，MS-06.8 更新）

| 编号 | 当前状态 | 证据/说明 |
| --- | --- | --- |
| 2.1～2.5 | TESTED | MS-06.3 五个显示类型组件、浏览器测试、截图 |
| 2.6 | TESTED | `BoxModelDemo.vue`、截图 `2-06-box-parts.png` |
| 2.7 | TESTED | `BoxSizingDemo.vue`（content-box）、截图 `2-07-content-box.png` |
| 2.8 | TESTED | `BoxSizingDemo.vue`（border-box）、截图 `2-08-border-box.png` |
| 2.9 | TESTED | `NormalFlowDemo.vue`、截图 `2-09-normal-flow.png` |
| 2.10 | TESTED | `RelativePositionDemo.vue`、截图 `2-10-relative.png` |
| 2.11 | TESTED | `AbsolutePositionDemo.vue`、截图 `2-11-absolute.png` |
| 2.12 | TESTED | `PositioningContextDemo.vue`、截图 `2-12-absolute-context.png` |
| 2.13 | TESTED | `FixedPositionDemo.vue`、截图 `2-13-fixed.png` |
| 2.14 | TESTED | `StickyPositionDemo.vue`、截图 `2-14-sticky.png` |
| 2.15 | TESTED | `StickyConstraintDemo.vue`、截图 `2-15-sticky-rect.png`；补充五定位方式对比表 |
| 2.16 | TESTED | `OffsetPropertiesDemo.vue`、截图 `2-16-offsets.png` |
| 2.17 | TESTED | `OffsetConflictDemo.vue`、截图 `2-17-offset-override.png`；补充 `StackingContextDemo.vue`/`2-17-stacking.png` |
| 2.18 | TESTED | `WireframeView.vue` 原创 plan 线框 + 真实 implemented 页面；截图 `2-18-wireframe-plan.png`、`2-18-implemented.png` |
| 2.19 | TESTED | `LayoutPrinciplesDemo.vue` 真实 AppLayout 高亮/响应式测量；截图 `2-19-principles.png` |
| 2.20 | OUT_OF_SCOPE（MS-07） | 仅规划入口，待实现 |
| 2.21 | OUT_OF_SCOPE（系统/论文） | 由业务实现与 MS-09 负责 |
| 2.22 | NOT_APPLICABLE | 任务（二），本项目不采用 |

### 3.3 最终交付追踪（MS-06.10）

> 本表记录最终交付证据；课程原文标题保持原样，2.15/2.17 的补充实验、2.18 的负责人批准替代方案在“遗留/说明”中明确标注。

| 编号 | 课程原文标题 | 实现页面 / Vue 组件 | 关键 CSS | 测试脚本 | 截图 | 证据报告 | 状态 | 遗留/说明 |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 2.1 | 行内显示类型 | `/web-basics#chapter-2.1` / `InlineDemo.vue` | inline、padding/border/margin、width/height | ms06-3、ms06-9 | `2-01-inline.png` | ms-06.3 | 已测试 | — |
| 2.2 | 区块显示类型 | `#chapter-2.2` / `BlockDemo.vue` | block、width auto、margin auto | ms06-3、ms06-9 | `2-02-block.png` | ms-06.3 | 已测试 | — |
| 2.3 | 行内区块显示类型 | `#chapter-2.3` / `InlineBlockDemo.vue` | inline-block、空白、原生换行 | ms06-3、ms06-9 | `2-03-inline-block.png` | ms-06.3 | 已测试 | — |
| 2.4 | 弹性显示类型 | `#chapter-2.4` / `FlexDemo.vue` | flex、direction、justify、align、wrap、gap | ms06-3、ms06-9 | `2-04-flex.png` | ms-06.3 | 已测试 | — |
| 2.5 | 网格显示类型 | `#chapter-2.5` / `GridDemo.vue` | grid、fr、gap、span | ms06-3、ms06-9 | `2-05-grid.png` | ms-06.3 | 已测试 | — |
| 2.6 | 盒模型各部分 | `#chapter-2.6` / `BoxModelDemo.vue` | content/padding/border/margin | ms06-4、ms06-9 | `2-06-box-parts.png` | ms-06-4 | 已测试 | — |
| 2.7 | 标准盒模型 | `#chapter-2.7` / `BoxSizingDemo.vue` | content-box | ms06-4、ms06-9 | `2-07-content-box.png` | ms-06-4 | 已测试 | — |
| 2.8 | 替代盒模型 | `#chapter-2.8` / `BoxSizingDemo.vue` | border-box | ms06-4、ms06-9 | `2-08-border-box.png` | ms-06-4 | 已测试 | — |
| 2.9 | 常规流布局（静态定位） | `#chapter-2.9` / `NormalFlowDemo.vue` | static、block、inline | ms06-4、ms06-9 | `2-09-normal-flow.png` | ms-06-4 | 已测试 | — |
| 2.10 | 相对定位 | `#chapter-2.10` / `RelativePositionDemo.vue` | relative、top/left、原占位 | ms06-4、ms06-9 | `2-10-relative.png` | ms-06-4 | 已测试 | — |
| 2.11 | 绝对定位 | `#chapter-2.11` / `AbsolutePositionDemo.vue` | absolute、offset、脱流 | ms06-5、ms06-9 | `2-11-absolute.png` | ms-06-5 | 已测试 | — |
| 2.12 | 改变绝对定位的定位上下文 | `#chapter-2.12` / `PositioningContextDemo.vue` | 定位祖先、父级 static/relative | ms06-5、ms06-9 | `2-12-absolute-context.png` | ms-06-5 | 已测试 | — |
| 2.13 | 固定定位 | `#chapter-2.13` / `FixedPositionDemo.vue` | fixed、视口、transform 例外说明 | ms06-5、ms06-9 | `2-13-fixed.png` | ms-06-5 | 已测试 | 固定徽标只在区域进入视口时显示 |
| 2.14 | 粘性定位 | `#chapter-2.14` / `StickyPositionDemo.vue` | sticky、top 阈值、滚动容器 | ms06-5、ms06-9 | `2-14-sticky.png` | ms-06-5 | 已测试 | — |
| 2.15 | 粘性约束矩形 | `#chapter-2.15` / `StickyConstraintDemo.vue` + 五定位方式对比表 | sticky、父容器边界 | ms06-5、ms06-9 | `2-15-sticky-rect.png` | ms-06-5 | 已测试 | 对比表为补充，截图仍为课程原 2.15 场景 |
| 2.16 | 定位属性上下左右 | `#chapter-2.16` / `OffsetPropertiesDemo.vue` | static/relative/absolute、四方向 | ms06-6、ms06-9 | `2-16-offsets.png` | ms-06-6 | 已测试 | — |
| 2.17 | 定位属性之间的覆盖影响 | `#chapter-2.17` / `OffsetConflictDemo.vue` + `StackingContextDemo.vue` | left/right、auto/fixed、margin auto、z-index 上下文 | ms06-6、ms06-9 | `2-17-offset-override.png`、`2-17-stacking.png` | ms-06-6 | 已测试 | z-index/堆叠上下文为补充实验 |
| 2.18 | 使用线框风格仿制XXX网页的布局 | `/web-basics/wireframe` / `WireframeView.vue` | plan 低线框、真实 implemented 页面 | ms06-7、ms06-9 | `2-18-wireframe-plan.png`、`2-18-implemented.png` | ms-06-7 | 已测试 | 批准改为快递通自身原创线框，不仿制外部网页 |
| 2.19 | 布局原则思考 | `#chapter-2.19` / `LayoutPrinciplesDemo.vue` | 真实 AppLayout outline、响应式布局 | ms06-8、ms06-9 | `2-19-principles.png` | ms-06-8 | 已测试 | 截图聚焦三区域；控制面板由测试覆盖 |

---

## 4. 开发阶段拆分

> 所有阶段只在前一阶段验收后开始；不得合并成一次性大任务。

### MS-06.2 路由、导航、公共组件和页面骨架

| 项 | 内容 |
| --- | --- |
| 文件范围 | `frontend/src/router/index.ts`、`layout/AppLayout.vue`、`views/WebBasicsView.vue`、`views/WireframeView.vue`（骨架）、`components/basics/PracticeNav.vue`、`DemoSection.vue`、`CodeBlock.vue`、`styles/basics.scss` |
| 开发目标 | `/web-basics`、`/web-basics/wireframe` 可打开；导航 active；骨架/章节锚点可用；不实现 19 节内容 |
| 验收标准 | 两个路由 200；`/import`、`/addresses` 不受影响；构建通过；无控制台错误 |
| 测试命令 | `npm run build`；`node scripts/ms04-browser-check.mjs`；`node scripts/ms05-browser-check.mjs` |
| 截图 | 无（骨架截图可留内部，不计最终 19 节证据） |
| 回归 | 地址录入/地址簿无样式/路由回归 |
| 风险 | AppLayout active/fullBleed 改动影响业务页 |

### MS-06.3 2.1～2.5

| 项 | 内容 |
| --- | --- |
| 文件范围 | `DisplayTypeDemo.vue`、`FlexGridDemo.vue`、`basics.scss`、`WebBasicsView` 章节注册 |
| 开发目标 | 行内/块/行内块/flex/grid 五节真实演示 |
| 验收标准 | 每节按 §3 表观察点可见；交互有效；无样式污染 |
| 测试命令 | `npm run build`；`node scripts/ms06-browser-check.mjs --grep 2.1-2.5`（脚本后续建立） |
| 截图 | `2-01`～`2-05` |
| 回归 | 业务页无变化 |
| 风险 | inline 垂直重叠/空白间隙需准确，不可为了美观隐藏 |

### MS-06.4 2.6～2.10

| 项 | 内容 |
| --- | --- |
| 文件范围 | `BoxModelDemo.vue`、`NormalFlowDemo.vue`、`basics.scss` |
| 开发目标 | 盒模型各部分/标准/替代、常规流、相对定位 |
| 验收标准 | 尺寸/计算/占位/折叠现象可见 |
| 测试命令 | build + ms06 脚本分组 |
| 截图 | `2-06`～`2-10` |
| 回归 | 无业务页变化 |
| 风险 | 计算标签与实际 CSS 不同步 |

### MS-06.5 2.11～2.15

| 项 | 内容 |
| --- | --- |
| 文件范围 | `PositioningDemo.vue`、`FixedStickyDemo.vue`、`basics.scss` |
| 开发目标 | 绝对/上下文/固定/粘性/约束矩形 |
| 验收标准 | 脱流、参照、fixed、sticky、推走现象可见 |
| 测试命令 | build + ms06 脚本分组 |
| 截图 | `2-11`～`2-15` |
| 回归 | 无业务页变化 |
| 风险 | fixed 演示污染全局视口；必须用独立滚动容器 |

### MS-06.6 2.16～2.17

| 项 | 内容 |
| --- | --- |
| 文件范围 | `OffsetConflictDemo.vue`、`basics.scss` |
| 开发目标 | 四方向偏移、覆盖规则、margin auto 居中 |
| 验收标准 | 每项可切换、现象准确 |
| 测试命令 | build + ms06 脚本分组 |
| 截图 | `2-16`～`2-17` |
| 回归 | 无业务页变化 |
| 风险 | 覆盖规则在浏览器实现差异；需固定 Chrome/Edge 版本 |

### MS-06.7 2.18 原创系统线框图

| 项 | 内容 |
| --- | --- |
| 文件范围 | `views/WireframeView.vue`、`styles/basics.scss`、路由/导航 |
| 开发目标 | 本系统自身低保真线框；无外部素材；两图真实截图 |
| 验收标准 | §3/§3.1 全部；无 `2-18-original.png`；区域与真实 AppLayout/Import/Address 一致 |
| 测试命令 | build + ms06 2.18 + 浏览器截图 |
| 截图 | `2-18-wireframe-plan.png`、`2-18-implemented.png` |
| 回归 | 业务页不动 |
| 风险 | 误用外部图/真实数据；把规划入口画成已实现 |

### MS-06.8 2.19

| 项 | 内容 |
| --- | --- |
| 文件范围 | `LayoutPrinciples.vue`、`basics.scss` |
| 开发目标 | 原则逐条对照本系统真实布局/代码 |
| 验收标准 | 每条原则可点击高亮真实区域/代码；不空谈 |
| 测试命令 | build + ms06 脚本 |
| 截图 | `2-19-principles.png` |
| 回归 | 无业务页变化 |
| 风险 | 变成纯文字列表；必须有交互/对照 |

### MS-06.9 全量联调、响应式和 Playwright

| 项 | 内容 |
| --- | --- |
| 文件范围 | `script/ms06-browser-check.mjs`（新建，不修改业务代码）；必要时样式修正 |
| 开发目标 | 19 节交互/视觉/响应式/可访问性全量回归 |
| 验收标准 | 每节有真实断言；1366×768 与 390px 无页面级溢出；控制台无异常；业务页回归通过 |
| 测试命令 | `npm run build`；`node scripts/ms06-browser-check.mjs`；`node scripts/ms04...`；`node scripts/ms05...`；必要时 `mvn -B clean test` |
| 截图 | 按需复核 |
| 回归 | MS-04/MS-05 全流程 |
| 风险 | fixed/sticky 测试不稳定；Element Plus 样式影响演示 |

### MS-06.10 真实截图、文档和最终验收

| 项 | 内容 |
| --- | --- |
| 文件范围 | `docs/screenshots/`、MS-06 报告、test-plan/README/AGENTS 更新 |
| 开发目标 | 19 节真实截图、报告、论文素材 |
| 验收标准 | 文件名全；截图来自真实浏览器；2.18 仅两张；论文/验收如实写范围调整 |
| 测试命令 | build + ms06 + 文件清单核对 |
| 截图 | 2-01～2-19 全部 |
| 回归 | 最终业务回归 |
| 风险 | 截图与代码不一致、旧版本截图 |

---

## 5. 依赖与风险

### 依赖

- MS-05 已提交且工作区干净（审计时 HEAD `f951d9a MS-05`）；
- 现有 AppLayout/设计 token/Element Plus；
- 不需要新后端 API/数据库/依赖；
- 需要后续 `docs/screenshots/` 目录。

### 风险

1. **课程原文与项目设计混淆**：必须保留原文标题，注明推导依据；
2. **2.18 外部素材风险**：明确禁用，两图以外不采集；
3. **CSS 现象不准确**：视觉美化不能改变原理；
4. **fixed/sticky 污染页面**：独立滚动容器；
5. **AppLayout 改动影响业务页**：只加 nav/meta，回归 MS-04/05；
6. **主包体积**：路由懒加载；不增依赖；
7. **390px 溢出**：线框/演示容器内部滚动；
8. **截图为假**：必须真实浏览器生成；
9. **19 节遗漏**：本节表格作为 checklist；
10. **范围膨胀**：不实现 2.20/2.21/2.22。

---

## 6. 待确认事项

1. 2.18 的“线框设计板”是否由 `WireframeView` 的 plan 视图生成，还是独立静态画板？建议用同一页面的 plan 模式，避免两套实现；
2. 2.18 的导航是否展示 Servlet 演示“待实现”占位？按本项目真实状态建议展示但明确标注；
3. 2.19 是否允许以本系统真实布局高亮为主，而不做额外组件？建议允许；
4. 截图目录创建时机：MS-06.10 前建立。
