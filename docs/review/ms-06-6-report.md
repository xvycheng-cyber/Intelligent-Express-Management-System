# MS-06.6 实施报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-06.6 CSS 定位偏移与元素覆盖（2.16～2.17） |
| 依据 | `docs/architecture.md` §6.2～§6.4、MS-06 实施计划、MS-06.1～MS-06.5 报告 |
| 范围 | 2.16、2.17；补充 z-index/堆叠上下文实验 |
| 结论 | **2.16～2.17 完成；无阻塞；具备进入 MS-06.7 的条件**（待负责人/ChatGPT 复核） |

---

## 1. 本阶段完成内容

- 2.16 定位属性上下左右；
- 2.17 定位属性之间的覆盖影响；
- 补充：元素覆盖 / z-index / 堆叠上下文；
- 2.16、2.17 状态更新为“已实现”；
- 三张真实浏览器截图（含 z-index 补充截图）；
- MS-06.2～MS-06.5、MS-04、MS-05 回归。

未实现 2.18、2.19；未修改后端、API、数据库。

### 关于 2.17 课程标题与 z-index 要求

课程任务书/已批准架构中 2.17 的原始标题是：

```text
2.17 定位属性之间的覆盖影响
```

本阶段保留该课程标题，实现 left/right、top/bottom 覆盖规则；同时按本轮要求补充“**元素覆盖与 z-index / 堆叠上下文**”实验，作为 2.17 的补充内容。没有把 z-index 冒充成课程原 2.17 的唯一内容，也没有修改课程标题。

---

## 2. 实现文件

### 新增组件

```text
frontend/src/components/basics/OffsetPropertiesDemo.vue
frontend/src/components/basics/OffsetConflictDemo.vue
frontend/src/components/basics/StackingContextDemo.vue
```

### 修改

```text
frontend/src/views/WebBasicsView.vue
frontend/src/styles/basics.scss
docs/review/ms-06-implementation-plan.md
frontend/scripts/ms06-3-browser-check.mjs
frontend/scripts/ms06-4-browser-check.mjs
frontend/scripts/ms06-5-browser-check.mjs
```

### 新增测试/截图脚本

```text
frontend/scripts/ms06-6-browser-check.mjs
frontend/scripts/ms06-6-screenshots.mjs
```

### 新增截图

```text
docs/screenshots/2-16-offsets.png
docs/screenshots/2-17-offset-override.png
docs/screenshots/2-17-stacking.png   # z-index/堆叠上下文补充
```

### 新增报告

```text
docs/review/ms-06-6-report.md
```

未新增 npm 依赖，未使用外部字体/图片/服务，未修改后端或数据库。

---

## 3. 关键 CSS 原理

### 2.16 定位属性上下左右

文件：`OffsetPropertiesDemo.vue`

- 支持 static / relative / absolute 切换；
- 支持 top / right / bottom / left 与 0～80px 偏移；
- 定位参照：
  - static：不适用偏移；
  - relative：自身原位置；
  - absolute：舞台的定位包含块；
- static 下 top/left 设置不产生定位效果；
- relative 保留原占位；absolute 脱离文档流；
- 同一偏移值在不同模式/参照下的视觉结果不同；
- 代码与当前 mode/axis/offset 同步。

### 2.17 定位属性之间的覆盖影响

文件：`OffsetConflictDemo.vue`

场景：

1. absolute：left + right，`width:auto` → 左右拉伸；
2. absolute：left + right，固定 width → left 生效、right 被忽略；
3. absolute：top + bottom，`height:auto` → 上下拉伸；
4. relative：left + right → left 生效、right 被忽略；
5. absolute：固定 width + 左右 `margin:auto` → 居中。

关键结论：

- 四个方向不是无条件同时生效；
- 过度约束时按 CSS 规则取舍；
- 没有用 JS 修改元素坐标；
- 代码与场景/偏移/尺寸同步。

### 2.17 补充：z-index 与堆叠上下文

文件：`StackingContextDemo.vue`

实验：

- 父级 `position:relative; z-index:1` 建立堆叠上下文；
- A 是上下文内子元素，z-index 可设 0～999；
- B 是上下文外元素，z-index 可设 0～10；
- 默认 A 子 z-index 999，B z-index 2：
  - 父上下文开启：B 在 A 上，因为父上下文整体 z=1 < B 的 z=2；
  - 父上下文关闭：A/B 进入同一比较，A 的 999 在 B 上；
- 不使用 DOM 顺序伪装；
- 通过 `elementFromPoint` 检查重叠处最上层元素。

---

## 4. Playwright 证据

### 构建

```text
npm run build
vue-tsc --noEmit + vite build
✓ built in 5.61s
exit code: 0
```

既有警告：

- Sass legacy JS API；
- 主 chunk >500 kB；
- `WebBasicsView` 懒加载 chunk 约 83.45 kB。

### MS-06.6

```text
node scripts/ms06-6-browser-check.mjs
MS06_6_BROWSER_OK
```

覆盖：

- 2.16 static/relative/absolute computed position；
- static 偏移无效；
- top/right/bottom/left 四方向真实位移；
- 动态代码同步与 reset；
- 2.17 absolute 左右拉伸、固定宽度 left 生效、上下拉伸、relative left 生效、margin auto 居中；
- z-index 同上下文/不同堆叠上下文覆盖关系；
- hash 导航/刷新；
- 1366/980/760/390 无页面级横向溢出；
- 控制台无错误。

测试使用真实 `getBoundingClientRect` 与 `elementFromPoint`，不是只看文字。

### 回归

```text
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

MS-06.3/04/05 的状态断言已更新：2.16/2.17 已实现，2.19 仍“未开始”。

### 后端

本阶段未修改后端，**未重跑 Maven 测试**；不把历史 146 基线写成本次执行结果。

### 数据库

- 测试库最终 0 行；
- 主库 0 行；
- 未执行破坏性数据库操作。

---

## 5. 响应式结果

| 宽度 | 2.16/2.17 | 页面级横向溢出 |
| --- | --- | --- |
| 1366 | 通过 | 无 |
| 980 | 通过 | 无 |
| 760 | 通过 | 无 |
| 390 | 通过 | 无 |

- 定位舞台内部可滚动；
- z-index 卡片在 390px 缩窄；
- 控件单列排列；
- 代码区可换行。

---

## 6. 截图清单

```text
docs/screenshots/2-16-offsets.png
docs/screenshots/2-17-offset-override.png
docs/screenshots/2-17-stacking.png
```

实际尺寸：

| 文件 | 尺寸 | 说明 |
| --- | --- | --- |
| `2-16-offsets.png` | 784×972 | 2.16 section |
| `2-17-offset-override.png` | 784×940 | 2.17 覆盖影响 |
| `2-17-stacking.png` | 784×962 | z-index/堆叠上下文补充 |

均为真实浏览器截图。

---

## 7. 业务回归结果

| 页面 | 结果 |
| --- | --- |
| `/import` | `MS04_BROWSER_OK` |
| `/addresses` | `MS05_BROWSER_OK` |

业务页面未修改，`.wb-*` 样式隔离，无新增控制台错误。

---

## 8. 已知问题

1. 2.18、2.19 未实现，符合本阶段范围；
2. 课程 2.17 原题为“定位属性之间的覆盖影响”；z-index/堆叠上下文是补充实验；
3. 2.16 的 relative 偏移会与后续 normal flow 元素发生视觉重叠，这是 relative 保留原占位的真实表现；
4. z-index 实验只覆盖“父级上下文 + 外部元素”一组关键对照，未穷举所有 stacking context 触发条件；
5. 主 chunk warning、Sass warning 为既有问题；
6. 2.16/2.17 截图是 section 截图（784 宽），z-index 补充截图单独存在；
7. 未重跑后端 Maven 测试，因为无后端改动；
8. 当前 MS-06.2～MS-06.6 改动均未提交 Git。

---

## 9. Git 开始/结束状态

开始时 HEAD：

```text
a5a23f5
```

MS-06.2～MS-06.5 改动在开始时均未提交，本阶段全部保留；未执行 reset/clean/checkout/restore/stash。

结束时新增/修改包括：

- `docs/review/ms-06-implementation-plan.md` 状态表；
- 三个新组件；
- `WebBasicsView.vue` / `basics.scss`；
- `ms06-6` 测试/截图脚本；
- 本报告与三张截图；
- MS-06.2～MS-06.5 既有未提交成果。

`git diff --check` 通过。

**未执行 Git commit、reset、clean、checkout、restore、stash 或 `git add .`。**

---

## 10. 是否具备进入 MS-06.7 的条件

**具备。**

- 2.16、2.17 完成；
- z-index/堆叠上下文补充实验完成；
- 四方向偏移和覆盖规则通过真实浏览器验证；
- 构建、MS-06.6 Playwright、MS-06.5～MS-06.2、MS-04/MS-05 回归通过；
- 无 P0/P1 阻塞；
- 无新增依赖、无外部素材、无后端/API/数据库修改。

**本阶段停止，等待负责人/ChatGPT 审核后再进入 MS-06.7（2.18 原创系统线框）。**
