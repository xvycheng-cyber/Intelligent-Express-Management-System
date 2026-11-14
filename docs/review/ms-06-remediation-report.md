# MS-06-R1 专项整改报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 对应审查问题 | P1-1、P1-2 |
| 审查报告 | [`ms-06-final-audit-report.md`](ms-06-final-audit-report.md)（保持原样，不修改） |
| 范围 | 2.2 Block 相邻垂直 margin collapse；相关测试/截图/文档 |
| 结论 | **整改已完成，建议进入第二次正式复审；不自行宣布 MS-06 最终通过** |

---

## 1. 整改依据

正式审查报告 P1-1 指出：

- `BlockDemo.vue` 只有 `margin-bottom`，没有两个相邻 block 的上下 margin 对照；
- 测试只验证宽度/居中/reset，未验证 margin collapse；
- `2-02-block.png` 无法证明折叠；
- 最终交付报告仍把 2.2 标为完成。

P1-2 指出：`ms-06.3-report.md` 已承认缺口，但最终交付报告未同步，属于完成声明过度。

本次只处理这两项，不扩大课程范围。

---

## 2. 修改文件

```text
frontend/src/components/basics/BlockDemo.vue
frontend/src/styles/basics.scss
frontend/scripts/ms06-3-browser-check.mjs
frontend/scripts/ms06-9-browser-check.mjs
docs/screenshots/2-02-block.png
docs/review/ms-06.3-report.md
docs/review/ms-06-9-report.md
docs/review/ms-06-final-delivery-report.md
docs/review/ms-06-implementation-plan.md
docs/review/ms-06-remediation-report.md（本报告）
```

未修改：

- 后端、API、数据库；
- 其他课程演示；
- 正式审查报告；
- npm 依赖。

---

## 3. 演示原理

### 3.1 正常文档流

结构只包含两个直接相邻的 block：

```html
<div class="wb-margin-normal">
  <div class="first"></div>
  <div class="second"></div>
</div>
```

```css
.wb-margin-normal { display: block; }
.wb-margin-normal .first  { margin-bottom: 24px; }
.wb-margin-normal .second { margin-top: 16px; }
```

两个 sibling block 的垂直 margin 发生真实折叠；间距为两者的较大值。

- 父容器有 padding/border，用于隔离父子 margin，但不阻止 sibling 折叠；
- 两个实验元素之间没有其它 DOM 元素；
- 没有使用 flex/grid 包装正常流实验；
- 没有 JavaScript 计算/绝对定位/transform 模拟。

### 3.2 flex 对照

```css
.wb-margin-contrast {
  display: flex;
  flex-direction: column;
}
.wb-margin-contrast .first  { margin-bottom: 24px; }
.wb-margin-contrast .second { margin-top: 16px; }
```

flex item 的 margin 不进行相邻折叠，实际距离为 24 + 16 = 40px。

### 3.3 参数

- 第一个 block `margin-bottom`：0～80px，默认 24px；
- 第二个 block `margin-top`：0～80px，默认 16px；
- 实时更新正常流/flex 两组真实块；
- 显示 computed margin、预期值、实际 `getBoundingClientRect` 距离；
- reset 同时恢复两个 margin 和测量结果；
- 动态代码使用当前参数，不是固定字符串。

---

## 4. 实际浏览器测量

默认：

| 项目 | 值 |
| --- | --- |
| margin-bottom | 24px |
| margin-top | 16px |
| 正常流预期 max(24,16) | 24px |
| 正常流实际 | 24px |
| flex 对照预期 24+16 | 40px |
| flex 对照实际 | 40px |

第二组：

| 项目 | 值 |
| --- | --- |
| margin-bottom | 16px |
| margin-top | 32px |
| 正常流预期 max(16,32) | 32px |
| 正常流实际 | 32px |
| flex 对照预期 16+32 | 48px |
| flex 对照实际 | 48px |

测量来自真实 DOM 的 `getBoundingClientRect()`；正常流两个元素在同一帧内同时测量，避免页面平滑滚动造成测量误差。

---

## 5. Playwright 测试

### 5.1 2.2 专项

```text
node scripts/ms06-3-browser-check.mjs
MS06_3_BROWSER_OK
```

新增断言：

- 两个实验元素 computed display 为 block；
- 正常流容器 display block，flex 对照 display flex；
- 两个正常流元素是直接相邻 sibling；
- 24/16 → 实际 24px；
- 16/32 → 实际 32px；
- flex 对照 40/48px；
- 动态 CSS 同步 `margin-top: 32px`、`display: flex`；
- reset 恢复 24/16 且实际间距恢复 24px；
- 控制台无新增错误。

### 5.2 集成

```text
node scripts/ms06-9-browser-check.mjs
MS06_9_BROWSER_OK
```

新增：

- 2.2 专项间距断言；
- 1366/980/760/390 全量页面无整体横向溢出。

### 5.3 全量回归

```text
MS06_8_BROWSER_OK
MS06_7_BROWSER_OK
MS06_6_BROWSER_OK
MS06_5_BROWSER_OK
MS06_4_BROWSER_OK
MS06_2_BROWSER_OK
MS04_BROWSER_OK
MS05_BROWSER_OK
```

- 2.1、2.3、2.4、2.5 截图未改；
- 仅 2-02 重新生成；
- 2.1/2.3/2.4/2.5 旧截图已保留，未用新图冒充其它实验。

第一次集成运行因“分两次读取两个元素的 rect，页面平滑滚动造成 11px 误差”而失败；改为同一帧同时测量后通过。该问题是测试测量方式问题，不是页面布局错误。

---

## 6. 截图

```text
docs/screenshots/2-02-block.png
```

- 尺寸：784×1781；
- 真实浏览器 section 截图；
- 可见：
  - 两个 margin 滑块；
  - 正常流实验（24/16、实际 24.0px）；
  - flex 对照（实际 40.0px）；
  - 结论和动态代码；
- 无调试面板、报错或手工拼图。

---

## 7. 响应式

集成测试在 1366/980/760/390 下检查：

- 2.2 实验存在；
- 页面整体无横向溢出；
- 控制面板/结果/代码可读；
- 正常流/flex 对照保持真实布局；
- 页面级横向溢出：无。

---

## 8. MS04/MS05

```text
MS04_BROWSER_OK
MS05_BROWSER_OK
```

- 批量录入、解析、修正、保存正常；
- 地址簿查询、分页、编辑、删除、批删正常；
- 业务页面样式和导航未受影响。

---

## 9. 构建

```text
npm run build
✓ built in 5.24s
exit code: 0
```

既有警告：

- Sass legacy JS API；
- 主 chunk >500KB。

未新增依赖；未新增外部字体/资源。

---

## 10. 文档修正（P1-2）

- `ms-06.3-report.md` 追加 §15 整改记录，不改写历史；
- `ms-06-9-report.md` 追加 §11 回归记录；
- `ms-06-final-delivery-report.md` 更新 2.2 证据、构建时间和 R1 说明；
- `ms-06-implementation-plan.md` §3.3 2.2 行更新为真实 `BlockDemo` + margin collapse；
- 正式审查报告保持原样。

最终交付报告不再把“未验证的 2.2 margin collapse”当作已完成证据；现在有真实实现和测试支撑。

---

## 11. Git 状态

### 开始

```text
branch: main
HEAD: 6dd2bdb
status:
 D docs/handover/CHATGPT_PROJECT_HANDOVER.md
 ?? docs/review/handover/
 ?? docs/review/ms-06-final-audit-report.md
```

上述 handover 删除/新增是整改前已存在的状态，未修改或清理。

### 结束

本次整改新增/修改：

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

未执行 commit/push/reset/clean/checkout/stash；未清理数据库；未修改主库。

---

## 12. 是否建议复审

**建议进入第二次正式复审。**

复审重点：

1. 2.2 正常流 vs flex 对照是否真实；
2. 24/16 → 24、16/32 → 32，flex → 40/48；
3. reset 和动态代码；
4. `2-02-block.png` 是否与当前源码一致；
5. 最终交付报告/追踪表是否仍有过期完成声明；
6. 全量回归和响应式。

本报告不自行宣布 MS-06 最终通过。
