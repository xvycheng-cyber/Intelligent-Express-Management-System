# MS-06-R1 第二次正式复核报告

| 项目 | 内容 |
| --- | --- |
| 复核日期 | 2026-11-14 |
| 复核对象 | MS-06-R1：2.2 相邻垂直 margin collapse 专项整改 |
| 复核依据 | `ms-06-final-audit-report.md` 的 P1-1、P1-2 |
| 复核性质 | 独立只读复核，不继续整改 |
| 最终结论 | **A. 通过：P1-1、P1-2 均已关闭；无新增 P0/P1；建议正式结束 MS-06** |

---

## 1. 审计基本信息

### 1.1 工作区与 Git

```text
工作目录：E:\desktop\高级Web&J2EE期末
仓库：Intelligent-Express-Management-System
分支：main
HEAD：6dd2bdbfdb1ca912ccabee3b0f9acee5c1fdca27
```

本次复核开始时工作区：

```text
 D docs/handover/CHATGPT_PROJECT_HANDOVER.md
 M docs/review/ms-06-9-report.md
 M docs/review/ms-06-final-delivery-report.md
 M docs/review/ms-06-implementation-plan.md
 M docs/review/ms-06.3-report.md
 M docs/screenshots/2-02-block.png
 M frontend/scripts/ms06-3-browser-check.mjs
 M frontend/scripts/ms06-9-browser-check.mjs
 M frontend/src/components/basics/BlockDemo.vue
 M frontend/src/styles/basics.scss
?? docs/review/handover/
?? docs/review/ms-06-final-audit-report.md
?? docs/review/ms-06-remediation-report.md
```

说明：

- `D docs/handover/CHATGPT_PROJECT_HANDOVER.md`、`?? docs/review/handover/`、`?? docs/review/ms-06-final-audit-report.md` 是 R1 之前已存在的工作区状态，不能归因于本次整改；
- R1 修改/新增文件与整改报告一致，未发现后端、API、数据库或依赖变化；
- 原始正式审查报告 `ms-06-final-audit-report.md` 保持为未修改的历史文件。

### 1.2 复核方法

- 读取 `BlockDemo.vue`、`basics.scss`、`ms06-3`/`ms06-9` 测试及全部相关文档；
- 使用 Playwright 独立查询真实 DOM、computed style、`getBoundingClientRect`；
- 实际执行 MS06.2～MS06.9、MS04、MS05 浏览器回归；
- 实际执行前端构建；
- 实际打开 `2-02-block.png` 检查内容；
- 对照整改报告、阶段报告、最终交付报告和实施计划。

### 1.3 未能验证的内容

- 未在第二台机器/其他浏览器重复 Playwright；
- 后端本次未修改，未重复执行 Maven；以 MS-06.9/审查阶段的 146 测试为依据，本次不把后端测试当作新执行；
- 未对截图中每一像素做 OCR；已实际打开截图检查关键区域；
- 未把 R1 之外的历史工作区变更作为整改成果。

---

### 1.4 审计过程偏差

MS04/MS05 浏览器回归会向隔离测试库 `express_address_book_test` 写入测试行。为保持测试隔离，复核后清除了这些测试行，测试库最终 0 行；主库 `express_address_book` 未触碰。该清理仅涉及测试库测试数据，不涉及主库业务数据或数据库结构；但按“只读、不改数据库”的严格要求，仍应记录为审计过程偏差。后续复审如需绝对只读，应使用预置快照或保留测试行。

---

## 2. P1-1 复核结论

### 2.1 正常文档流 DOM/CSS

实际 DOM：

```html
<div class="wb-margin-normal" data-testid="margin-normal-stage">
  <div class="wb-margin-box first" data-testid="margin-normal-first">…</div>
  <div class="wb-margin-box second" data-testid="margin-normal-second">…</div>
</div>
```

真实浏览器读取结果：

| 检查项 | 实际值 | 结论 |
| --- | --- | --- |
| 正常流容器 display | `block` | 通过 |
| 第一个实验元素 display | `block` | 通过 |
| 第二个实验元素 display | `block` | 通过 |
| 两元素直接相邻 sibling | `true` | 通过 |
| 父容器 padding | `12px` | 可接受，未阻断 sibling collapse |
| 父容器 border-top | `1px` | 可接受，未阻断 sibling collapse |
| 第一个 margin-bottom | `24px` | 通过 |
| 第二个 margin-top | `16px` | 通过 |
| 元素实际边框盒间距 | **24px** | 通过 |

- 没有 flex/grid/absolute/transform 包裹正常流实验；
- 两个实验元素之间没有其它 DOM 元素；
- 没有用 JS 设置元素位置；
- 测量使用真实 `getBoundingClientRect()`；
- 父容器 padding/border 隔离了父子 margin，但没有破坏两个 sibling 的折叠。

### 2.2 正常流测量

独立运行页面得到：

| margin-bottom | margin-top | 实际边框盒距离 | 预期 |
| ---: | ---: | ---: | ---: |
| 24px | 16px | 24px | 24px |
| 16px | 32px | 32px | 32px |

页面实际显示：

```text
正常文档流：实际边框盒间距 24.0px；预期 max(24,16)=24px
```

修改参数后重新测量，结果变为 32px，说明不是固定文本。

### 2.3 Flex 对照

实际值：

| 检查项 | 实际值 | 结论 |
| --- | --- | --- |
| flex 容器 display | `flex` | 通过 |
| flex-direction | `column` | 通过 |
| 容器 gap | `normal`（无额外 gap） | 通过 |
| 第一个 item margin-bottom | 24px | 通过 |
| 第二个 item margin-top | 16px | 通过 |
| 实际边框盒距离（24/16） | **40px** | 通过 |
| 实际边框盒距离（16/32） | **48px** | 通过 |

flex 对照是真实 flex 布局，不是把正常流结果改用 `Math.max` 后伪造。

### 2.4 参数、动态代码、Reset

- 两个独立滑块：0～80px；
- 默认 24px / 16px；
- 正常流与 flex 两组同时更新；
- 实际距离随参数变化；
- 代码块显示当前参数和两种布局；
- Reset 恢复 24/16，实际间距恢复 24px/40px；
- 测试已验证 reset 后的数值和 DOM 间距。

### 2.5 P1-1 关闭结论

**P1-1 已关闭。**

证据：

- 真实正常流相邻 block 和真实 flex 对照；
- 真实 DOM 测量 24/40、32/48；
- Playwright `MS06_3_BROWSER_OK`、`MS06_9_BROWSER_OK`；
- `2-02-block.png` 已重生成并实际检查；
- 未发现用 JS 人工计算/绝对定位/transform 模拟布局。

### 2.6 P1-1 的剩余 P2 观察

页面标签显示的是参数值（`margin-bottom: 24px`、`margin-top: 16px`），`measurements.computedBottom/Top` 虽在脚本中被读取，但没有单独渲染“computed margin”字段；测试也主要验证实际 rect 和参数，而没有单独断言 computed margin 文本。

- 影响：不影响真实折叠行为、实际距离或 P1-1 的核心验收；
- 归类：**P2 证据完整性**；
- 建议后续如需更严格论文证据，可在结果区增加 computed margin 行；
- 不阻断 P1-1 关闭。

---

## 3. P1-2 复核结论

### 3.1 文档交叉检查

| 文档 | 实际状态 | 结论 |
| --- | --- | --- |
| `ms-06.3-report.md` | 原始历史保留；追加 §15 记录原缺口、修复、测量、测试 | 通过 |
| `ms-06-9-report.md` | 追加 §11 R1 回归记录 | 通过 |
| `ms-06-final-delivery-report.md` | 2.2 行改为 BlockDemo + margin collapse；增加 R1 证据；构建时间更新 | 通过 |
| `ms-06-implementation-plan.md` | §3.3 2.2 行更新；§3.1 2.2 行更新 | 通过 |
| `ms-06-remediation-report.md` | 记录 P1-1/P1-2、修改、测量、测试、截图、Git | 通过（有一处 P2 表述） |
| `ms-06-final-audit-report.md` | 保留原始 P1/P2，不被覆盖 | 通过（历史记录） |

### 3.2 历史是否保留

- `ms-06.3-report.md` 原 §14 仍保留“无 P0/P1”的历史结论；新增 §15 明确正式审查发现缺口并记录整改，不删除历史；
- 原始审查报告仍保留 P1-1/P1-2 的原始结论；
- 没有把原审查报告改写为“已通过”。

### 3.3 是否存在新矛盾

未发现以下问题：

- 没有把 2.18 写成外部网页仿制；
- 没有把 2.15/2.17 补充内容写成课程原文；
- 没有把未执行的测试写成通过；
- 没有把 2.2 截图路径写错；
- 没有删除阶段报告中的历史缺口。

发现一处表述精度问题（P2）：

- `ms-06-remediation-report.md` 称“显示 computed margin”，但页面实际显示的是参数标签和实际 rect 距离；computed margin 被读取但未单独渲染。
- 不改变 P1-2 核心关闭：最终报告已不再把未经证据的 2.2 当成完成，且实际实验/测试/截图已存在。

### 3.4 P1-2 关闭结论

**P1-2 已关闭。**

---

## 4. 回归测试

### 4.1 实际执行

```text
node scripts/ms06-3-browser-check.mjs
MS06_3_BROWSER_OK
exit 0

node scripts/ms06-9-browser-check.mjs
MS06_9_BROWSER_OK
exit 0

node scripts/ms06-8-browser-check.mjs
MS06_8_BROWSER_OK
exit 0

node scripts/ms06-7-browser-check.mjs
MS06_7_BROWSER_OK
exit 0

node scripts/ms06-6-browser-check.mjs
MS06_6_BROWSER_OK
exit 0

node scripts/ms06-5-browser-check.mjs
MS06_5_BROWSER_OK
exit 0

node scripts/ms06-4-browser-check.mjs
MS06_4_BROWSER_OK
exit 0

node scripts/ms06-2-browser-check.mjs
MS06_2_BROWSER_OK
exit 0

node scripts/ms04-browser-check.mjs
MS04_BROWSER_OK
exit 0

node scripts/ms05-browser-check.mjs
MS05_BROWSER_OK
exit 0
```

### 4.2 响应式

MS06.9 在以下宽度执行：

- 1366×900；
- 980px；
- 760px；
- 390px。

结果：

- 2.2 实验存在；
- 页面整体无横向溢出；
- 参数面板、正常流/flex 结果、代码区可读；
- 无控制台新增错误。

### 4.3 数据库

- 测试库最终 0 行；
- 主库 0 行；
- MS04/MS05 浏览器回归会向隔离测试库写入测试行；复核后清理测试库，未触碰主库业务数据；
- 未修改数据库结构；主库未执行任何写操作；
- 未使用真实业务数据执行破坏性操作。

### 4.4 后端

本阶段没有后端/API 变更，未重复执行 Maven；沿用上一阶段实际执行的 146 个后端测试通过记录。本次不将后端测试算作新执行。

---

## 5. 截图审查

### 5.1 `docs/screenshots/2-02-block.png`

- 路径：存在；
- 解码：Pillow 正常打开；
- 尺寸：784×1781；
- 来源：真实浏览器 section 截图；
- 内容实际检查：
  - 两个 margin 滑块 24px / 16px；
  - A 正常文档流：第一个/第二个 block，实际 24.0px，预期 max(24,16)=24px；
  - B flex 对照：实际 40.0px，预期 24+16=40px；
  - 结论说明；
  - 动态 CSS 代码；
- 没有调试面板/报错；
- 没有手工拼接或覆盖数字；
- 关键区域未被裁掉。

### 5.2 其他截图

- 2.01、2.03、2.04、2.05 已恢复为 R1 前版本，未因重新生成脚本产生无关变化；
- 2.18 仍只有批准的两张图；
- 2.18-original 不存在；
- 21 张截图均存在、可解码、无重复哈希。

结论：**截图证据通过**。

---

## 6. 修改范围审查

### 6.1 R1 修改/新增

```text
M frontend/src/components/basics/BlockDemo.vue
M frontend/src/styles/basics.scss
M frontend/scripts/ms06-3-browser-check.mjs
M frontend/scripts/ms06-9-browser-check.mjs
M docs/screenshots/2-02-block.png
M docs/review/ms-06.3-report.md
M docs/review/ms-06-9-report.md
M docs/review/ms-06-final-delivery-report.md
M docs/review/ms-06-implementation-plan.md
?? docs/review/ms-06-remediation-report.md
```

未发现：

- 后端/API/数据库文件变化；
- 新增 npm 依赖；
- 修改正式审查报告；
- 其他课程章节源码变化；
- 其他截图变化。

### 6.2 整改前遗留变更

```text
 D docs/handover/CHATGPT_PROJECT_HANDOVER.md
?? docs/review/handover/
?? docs/review/ms-06-final-audit-report.md
```

这些在 R1 开始前已存在，未归因于 R1，未清理或修改。

---

## 7. 问题清单

### P0

无。

### P1

无新增 P1。

- P1-1：关闭；
- P1-2：关闭。

### P2

**P2-1（新发现，文档/证据完整性）**

- 页面读取了 `measurements.computedBottom/Top`，但没有在 UI 中单独显示 computed margin 字段；
- 测试主要验证参数和实际 rect，没有 computed margin 文本断言；
- 实际折叠、间距、flex 对照和 reset 均已验证，因此不影响 P1-1 的核心关闭；
- 建议：在结果区增加 computed margin 行，并在测试中加一行断言（非阻塞）。

**P2-2（历史遗留）**

- 整改报告/最终报告的措辞“显示 computed margin”比实际 UI 更绝对；
- 建议下次文档维护时精确改为“显示参数值和实际边框盒距离”。

### P3

- Playwright 未纳入 CI；
- 主 chunk/Sass legacy warning 既有；
- 2.19 截图与控制面板不同视口（原审查 P2，未在本次范围）；
- 2.18 implemented 截图只展示导入页（原审查 P2）。

---

## 8. 最终结论

# A. 通过

依据：

1. **P1-1 已关闭**：真实正常流 block + flex 对照，真实 DOM 测量 24/40、32/48，测试和截图均通过；
2. **P1-2 已关闭**：历史缺口保留，最终报告/追踪表已同步，原始审查报告未被篡改；
3. **无新增 P0/P1**；
4. 全量 MS06、MS04、MS05 回归通过；
5. 截图真实完整且与当前实现一致；
6. 没有超范围修改或破坏原有功能；
7. 回归结果与整改报告一致（唯一差异是 P2 的 computed margin 展示精度）。

### 是否允许正式结束 MS-06

**允许。** MS-06 可以正式结束。

### 是否允许进入下一里程碑

**允许。** 建议在进入 MS-07 前保留本复核报告与 `2-02-block.png` 作为证据。

### 保留的已知限制

- P2-1/P2-2：computed margin 的显式展示/文档措辞可再精炼；
- 原审查的 2.19 截图、2.18 地址簿截图覆盖问题属于非阻塞 P2；
- 本次复核未重复执行后端 Maven 测试，依据为无后端变更及上一阶段 146 测试记录。

**复核结束。除本报告外，未修改任何项目文件。**
