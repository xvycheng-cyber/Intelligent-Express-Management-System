# MS-08 浏览器兼容性与响应式记录

| 浏览器/宽度 | 结果 | 说明 |
| --- | --- | --- |
| Chrome（本机，全部 Playwright） | PASS | MS04/05/06/07 + E2E |
| Edge（本机） | PASS | 1280/1366/980/760/390；`/import`、`/addresses`、`/web-basics`、`/web-basics/wireframe`、`/servlet-demo`；导入保存/搜索删除 |
| 1366 | PASS | 无页面级横向溢出 |
| 980 | PASS | 页面可读，矩阵可用 |
| 760 | PASS | 页面可读 |
| 390 | PASS | 页面无整体溢出；矩阵内部滚动 |

## 未执行

- Firefox/Safari 不在 NFR-03 要求内，未测试。
- Edge 通过 `ms08-edge-check.mjs` 实测，不是 Chrome 结果复制。

