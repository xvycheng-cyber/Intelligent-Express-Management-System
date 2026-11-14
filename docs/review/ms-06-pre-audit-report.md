# MS-06 第一阶段前置审计报告

| 项目 | 内容 |
| --- | --- |
| 审计日期 | 2026-11-14 17:41（Asia/Hong_Kong） |
| 审计范围 | MS-06 Web 基础实践前置审计；不开发、不改代码 |
| 结论 | **BLOCKED_MISSING_REQUIREMENTS** |
| 原因摘要 | 原始任务书存在且 2.1～2.19 标题齐全，但 2.1 以外只有标题；2.2～2.17、2.19 没有原文级操作/预期/验收说明，2.18 只有标题和一张参考图，无法在不自行推断的情况下拆解 19 节实现 |

> 本报告只新增文档；没有修改业务代码、数据库、API 或现有页面，没有提交 Git，没有执行 reset/clean/checkout/restore/stash。

---

## 1. Git 状态和 MS-05 提交情况

### 1.1 实际命令结果

```text
git status --short
（无输出）

git branch --show-current
main

git log -5 --oneline
f951d9a MS-05
adf448b MS-04
e12035f 后端
9a4809f 项目开发规划
fd13665 Initial commit

git rev-parse HEAD
f951d9a8dfad2e297e6029a9d0a53b95b038afda
```

HEAD 详情：

```text
f951d9a
2026-11-14 17:35:52 +0800
Chengzi1i
MS-05
```

### 1.2 结论

- MS-05 **确实已提交**，HEAD 为 `MS-05`；
- 工作区 **干净**（无未提交/未跟踪文件，除本报告生成前）；
- MS-04 之前的基础代码也已提交；
- 本次审计未执行任何 Git 写操作。

> 本报告创建后，`docs/review/ms-06-pre-audit-report.md` 会成为新的未跟踪文件；这是本次唯一新增文件。

---

## 2. 课程任务书文件位置

### 2.1 找到的原始任务书

```text
E:\desktop\高级Web&J2EE期末\高级Web项目开发（实践）任务书.docx
```

- 位置在**仓库上一级工作区根目录**，不在仓库 `docs/` 内；
- 文件存在，不是仅存在于历史报告中的引用；
- 本轮实际读取了 docx 的正文、表格和嵌入图片。

### 2.2 读取结果

任务书结构：

- 60 个段落；
- 9 张表格；
- 7 张嵌入图片（封面、2.18 参考网页、2.1 示例效果、2.21 系统参考图等）。

已读取到的内容：

- 任务书总说明；
- 任务（一）快递地址簿录入程序设计；
- 任务（二）名片管理系统（本项目不采用）；
- 附录1 封面示例；
- 附录2 章节结构；
- 附录3 第2章小节标题表；
- 附录4 2.18 线框仿制参考图；
- 附录5 地址簿系统参考图。

**本次未修改/复制任务书文件。**

---

## 3. 课程任务书原文完整性检查

### 3.1 附录3 的章节标题是完整的

任务书附录3 表格顺序如下（原文标题）：

| 序号 | 原文标题 |
| --- | --- |
| 2.1 | 行内显示类型 |
| 2.2 | 区块显示类型 |
| 2.3 | 行内区块显示类型 |
| 2.4 | 弹性显示类型 |
| 2.5 | 网格显示类型 |
| 2.6 | 盒模型各部分 |
| 2.7 | 标准盒模型 |
| 2.8 | 替代盒模型 |
| 2.9 | 常规流布局（静态定位） |
| 2.10 | 相对定位 |
| 2.11 | 绝对定位 |
| 2.12 | 改变绝对定位的定位上下文 |
| 2.13 | 固定定位 |
| 2.14 | 粘性定位 |
| 2.15 | 粘性约束矩形 |
| 2.16 | 定位属性上下左右 |
| 2.17 | 定位属性之间的覆盖影响 |
| 2.18 | 使用线框风格仿制XXX网页的布局 |
| 2.19 | 布局原则思考 |
| 2.20 | Servlet与请求URI的映射规则 |
| 2.21 | 快递地址簿录入程序设计 |
| 2.22 | 名片管理系统设计 |

其中 MS-06 范围是 **2.1～2.19**；2.20 属于 MS-07，2.21 属于系统/论文，2.22 不采用。

**标题没有缺失，顺序完整。**

### 3.2 但“19 节完整要求”不完整

任务书正文对每节的实际说明非常有限：

| 范围 | 原文实际内容 | 是否足以直接实现 |
| --- | --- | --- |
| 2.1 行内显示类型 | 有 HTML/CSS 示例 + 观察结论 + 效果图 | 部分足够（仍需确认是否为强制实现） |
| 2.2～2.17 | **只有标题**，无操作步骤、预期现象、验收标准 | 不足，必须推断 |
| 2.18 | 只有标题；附录4 有一张学校首页参考图；无文字版布局区域/宽度/验收要求 | 不足，必须推断 |
| 2.19 | 只有标题 | 不足 |
| 2.20 | 只有标题 | MS-07 另行处理 |
| 2.21 | 有任务（一）完整业务描述，但不属于 MS-06 | MS-09/系统范围 |
| 2.22 | 有任务（二）描述，但本项目不采用 | 不实现 |

**结论**：任务书章节标题完整，但 MS-06 需要的“每节操作方式、预期结果、验收方法”在原文中只有 2.1 有较完整示例；其余多数只有标题。现有 `architecture.md` §6.2 虽然有 19 节设计，但它是**后来自行推导的项目设计**，不能冒充任务书原文，也不能据此判断原始要求完整。

### 3.3 因此不提供 19 节详细需求追踪表

按要求：

> 只有在找到完整课程任务书原文后，才允许执行需求拆解；如果 19 节内容不完整，立即停止需求拆解。

本报告**不提供**“每节演示方式/代码示例/验收方式”的完整追踪表，因为那会需要推断原文没有写明的内容。上面 §3.1 只保留原文标题清单作为证据，不是需求拆解。

---

## 4. 第 2.18 节线框图要求

### 4.1 原文可确认内容

- 标题：**使用线框风格仿制XXX网页的布局**；
- 附录4 标题：“使用线框风格仿制XXX网页的布局”实践效果参考；
- 任务书内有一张参考网页图片，内容为**湖北工程学院新技术学院首页**；
- 图片可见区域包括：顶部导航、学校要闻、新闻列表、图片新闻等；
- 任务书没有提供：
  - 参考网页的 HTML/CSS 源文件；
  - 需要仿制的完整区域清单；
  - 目标容器宽度、断点、字体尺寸；
  - 线框稿的具体要求；
  - “原网页/线框稿/实现效果”三图的强制验收格式（该三图要求来自项目设计文档，不是任务书正文）。

### 4.2 待确认

1. 2.18 是否只要求“布局仿制”而不要求像素级复刻？
2. 采用任务书附录4 的学校首页，还是允许换其他“XXX 网页”？
3. 需要仿制哪些区域（导航/轮播/新闻列表/图片区/页脚）？
4. “线框风格”是否要求纯灰阶/无真实图片？
5. 是否强制三图（原网页/线框稿/实现）？
6. 目标宽度是 1280px、1366px，还是自适应？

在这些确认前，不能安全实现 2.18，也不能把 `architecture.md` 里的设计当作课程原文。

---

## 5. 前端现有架构审计

### 5.1 路由

实际 [`frontend/src/router/index.ts`](../frontend/src/router/index.ts)：

```text
/                     → redirect /import
/import               → ImportView
/addresses            → AddressListView
/:pathMatch(.*)*      → NotFoundView
```

**没有** `/web-basics`、`/web-basics/wireframe` 路由，因此无路径冲突。

### 5.2 布局与导航

[`AppLayout.vue`](../frontend/src/layout/AppLayout.vue) 当前：

- 左侧深色侧栏；
- 顶部面包屑 + 后端健康状态；
- 主区域 `slot`；
- 侧栏导航：
  - 批量地址录入 `/import`；
  - 地址簿管理 `/addresses`；
- `activeMenu` 仅识别 import/addresses；
- `pageStage` 仅识别 addresses，否则显示 MS-04；
- 主区域宽度 `min(1440px, 100%)`；
- 已有统一设计 token、响应式断点、`AppIcon`。

**复用可能**：

- AppLayout 可继续承载 `/web-basics`；
- 设计 token 和 AppIcon 可复用；
- 需要新增“Web 基础实践”导航项和 active 识别；
- 需要为 2.18 线框页考虑独立宽度/滚动容器；
- 不应复制一套新的 AppLayout。

### 5.3 现有页面

```text
views/ImportView.vue
views/AddressListView.vue
views/NotFoundView.vue
```

没有 Web 基础实践页面。

### 5.4 现有组件

- `components/common/AppIcon.vue`
- `components/import/AddressInputPanel.vue`
- `components/import/ParseResultTable.vue`
- `components/import/ParseStatistics.vue`
- `components/import/SaveConfirmDialog.vue`
- `components/address/AddressSearchBar.vue`
- `components/address/AddressTable.vue`
- `components/address/AddressFormDialog.vue`
- `components/address/BatchDeleteDialog.vue`

**没有**：

- `DemoSection`；
- `CodeBlock`；
- `PracticeNav`/章节锚点导航；
- `basics.scss`；
- 可复用的“演示/结论/代码”容器。

### 5.5 API/类型/工具

- `api/request.ts`：Axios + ApiError；
- `api/addresses.ts`：地址 API；
- `api/health.ts`：健康检查；
- `types/address.ts`、`types/api.ts`；
- `utils/textLength.ts`、`addressPayload.ts`、`addressValidation.ts`。

MS-06 的 19 节 CSS/布局演示**不需要新的后端 API**；不得为演示页修改现有 API 契约。

### 5.6 依赖与构建

[`frontend/package.json`](../frontend/package.json)：

- Vue 3.5.13；
- TypeScript 5.7.2；
- Vite 5.4.11；
- Element Plus 2.9.1；
- Vue Router 4.5.0；
- Axios 1.7.9；
- Sass 1.83.0；
- Playwright 1.65.1（devDependency）。

当前 Element Plus 使用全量引入；没有按需组件插件。MS-06 不应为了减几 KB 引入新构建依赖。

### 5.7 Vite 代理

[`vite.config.ts`](../frontend/vite.config.ts) 当前只代理：

```text
/api → http://127.0.0.1:8080
```

**没有 `/demo` 代理**。MS-06 不依赖 `/demo`；但 MS-07 Servlet 演示需要补充 `/demo` 代理（这是 MS-07 前置项，不是 MS-06 代码改动）。

### 5.8 Sass 组织

当前只有 `styles/index.scss`：

- 全局 token；
- body 重置；
- Element Plus 覆盖；
- 焦点/减少动效。

没有 `basics.scss`、章节样式文件或演示容器样式。

### 5.9 是否有可复用演示组件

没有。MS-06 需要新建：

- `PracticeNav`/章节锚点；
- `DemoSection`（标题/目标/演示/观察/代码）；
- `CodeBlock`；
- 2.18 线框页专用布局组件/样式。

不应把现有 `AddressTable`、`ParseResultTable` 等业务组件用于 CSS 演示，避免耦合。

---

## 6. 页面与路由设计建议

### 6.1 建议路由

```text
/web-basics                  → WebBasicsView（2.1～2.19）
/web-basics/wireframe        → WireframeView（2.18）
```

- 使用 `AppLayout`；
- `meta.title` 分别为“Web 基础实践”“线框布局实践”；
- 增加 `meta.section = 'web-basics'` 或类似标识，供 AppLayout active 菜单；
- `/web-basics/wireframe` 作为**独立顶层路由**，避免嵌套 `router-view` 和 AppLayout 主区域宽度限制；
- 不改变 `/import`、`/addresses` 路由。

### 6.2 AppLayout 集成建议

需要修改（**本次不修改**）：

- `activeMenu` 增加 `web-basics`；
- 侧栏新增“Web 基础实践”入口；
- `pageStage` 映射 MS-06；
- 2.18 页面可能需要 `meta.fullBleed` 或独立容器，避免 `min(1440px)` 主区域影响线框宽度；
- 若加 `fullBleed`，只影响目标路由，不影响业务页。

### 6.3 页面职责

| 页面 | 职责 |
| --- | --- |
| `/web-basics` | 19 节目录/锚点导航、每节演示、观察结论、代码片段 |
| `/web-basics/wireframe` | 2.18 参考网页的线框布局，独立滚动/宽度 |

### 6.4 不引入冲突

- 当前无 `/web-basics` 路由；
- 不修改 `/api` 契约；
- 不新增后端依赖；
- 不修改数据库；
- 路由懒加载，避免主包进一步变大。

---

## 7. MS-06 实施阶段拆分（条件性）

> 由于 §3 的原始要求不完整，以下只是**项目执行层面的阶段骨架**，不是 19 节内容拆解；在需求补齐前不得开始实现。

### MS-06.1 页面架构、导航和路由

- `/web-basics`、`/web-basics/wireframe`；
- AppLayout 导航/active/fullBleed 设计；
- 不修改业务页。

### MS-06.2 实践公共组件

- `PracticeNav`；
- `DemoSection`；
- `CodeBlock`；
- 演示容器/章节样式；
- 复用 AppIcon 与设计 token。

### MS-06.3 按课程要求实现各节实践

- **前置条件**：拿到 2.2～2.17、2.19 的原文级要求或负责人确认采用 `architecture.md` §6.2；
- 在此之前不做逐节实现。

### MS-06.4 第 2.18 节线框图

- 前置条件：确认参考网页范围/宽度/区域/三图要求；
- 独立路由和容器；
- 不伪造原网页图片/内容。

### MS-06.5 样式、响应式和交互完善

- 1366×768 和 390px 检查；
- 演示区固定尺寸/滚动容器；
- 焦点、减少动效、可访问性。

### MS-06.6 Playwright 浏览器测试

- 每节真实演示/交互；
- 2.18 路由；
- 控制台无异常；
- 不会用静态文字冒充演示。

### MS-06.7 截图、报告和最终验收

- 真实截图；
- MS-06 报告；
- 与论文 2.1～2.19 对应。

---

## 8. 风险和待确认事项

### 8.1 需求风险

| 风险 | 影响 | 处理 |
| --- | --- | --- |
| 2.2～2.17、2.19 只有标题 | 无法确定操作/预期/验收，容易自行编造 | 请求负责人补充，或书面确认采用 `architecture.md` §6.2 |
| 2.18 只有标题 + 参考图 | 无法确定布局区域/宽度/三图强制程度 | 请求确认；保留参考图，不伪造原网页 |
| 2.1 示例的强制程度不明 | 可能实现偏离 | 确认示例是否为必须行为 |
| 2.20 标题存在但属 MS-07 | 误在 MS-06 做 Servlet 页 | 保持 MS-06/07 边界 |
| 2.21/2.22 不属 MS-06 | 范围膨胀 | 不实现 |
| 现有 `architecture.md` §6.2 是推导设计 | 不能冒充原文 | 需负责人批准后才可作为实现依据 |

### 8.2 工程风险

| 风险 | 影响 | 处理 |
| --- | --- | --- |
| AppLayout 当前只识别 import/addresses | 新页面导航不高亮 | MS-06.1 增加 section 识别 |
| 主区域 1440px 宽度限制 | 2.18 线框宽度可能不够 | 设计 fullBleed/独立容器 |
| 无 DemoSection/CodeBlock | 19 节重复代码/样式漂移 | MS-06.2 建公共组件 |
| 无 `basics.scss` | 样式散落 | 统一章节样式 |
| 无 `/demo` 代理 | MS-07 会阻塞（不是 MS-06） | MS-07 前补 Vite 代理 |
| 主 chunk 已 >500kB | 再加实践页可能更大 | 路由懒加载；不盲目加依赖 |
| 无 `docs/screenshots` | 无法交付论文截图 | MS-06/09 前建立 |

---

## 9. 最终审计结论

# `BLOCKED_MISSING_REQUIREMENTS`

理由：

1. 原始任务书 **存在**，2.1～2.19 标题 **完整**；
2. 但任务书原文只对 2.1 给出 HTML/CSS 示例和效果图，2.2～2.17、2.19 只有标题；
3. 2.18 只有标题和一张参考网页图片，没有文字版区域/宽度/验收要求；
4. 因此无法在不自行推断的情况下生成 19 节需求追踪表和逐节实现计划；
5. 现有 `architecture.md` §6.2 是项目自行推导的设计，不能冒充原始任务书；
6. MS-05 已提交且工作区干净，前端架构没有阻止 MS-06 的结构性冲突；
7. 但需求资料不足是**硬阻塞**，不是代码问题。

### 解除阻塞需要

1. 项目负责人提供 2.2～2.17、2.19 的原文级要求；或
2. 书面确认 `docs/architecture.md` §6.2 可作为 19 节实现依据（并明确它是推导设计）；同时确认 2.18 的参考网页区域/宽度/三图要求；
3. 确认 2.1 示例是否强制；
4. 确认 MS-06 只做 2.1～2.19，2.20 留给 MS-07。

在收到上述确认前，**不开始 MS-06 实现，不创建页面/路由/组件，不修改代码**。

---

## 10. 本次审计范围声明

- 已读取原始任务书 docx 文本/表格/图片；
- 已读取前端路由、布局、页面、组件、API、类型、构建配置；
- 已确认 Git HEAD 为 MS-05 且工作区干净；
- 未修改业务代码、数据库、API、路由或页面；
- 未新增依赖；
- 未提交 Git；
- 未执行 reset/clean/checkout/restore/stash；
- 仅新增本报告。
