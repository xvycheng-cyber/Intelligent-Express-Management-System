# MS-13 最终回归测试报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| HEAD | `8a8f4b829742a2d527845744a2fac32c1157c166` |
| 环境 | JDK 17.0.21 / Maven 3.9.11 / Node 22.14.0 / MySQL 8.0.43 / Chrome 157 / Edge 157 |
| 结论 | 后端 160、前端 build、Chrome/Edge 核心流程通过；MS05 初次因测试数据残留失败，清理后重跑通过 |

---

## 1. Maven

```text
mvn -B clean test -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/express_address_book_ms08_test
Tests run: 160, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

实际执行时间：2026-11-15 22:10；隔离 Schema `express_address_book_ms08_test`。

| 测试类型 | 本轮 | 数量 | 通过 | 失败 | 跳过 |
| --- | --- | ---: | ---: | ---: | ---: |
| Parser/单元/Controller/HTTP/MySQL/DTO/Unicode/LIKE | 是 | 160 | 160 | 0 | 0 |

---

## 2. 前端构建

```text
npm run build
✓ built in 5.10s
exit 0
```

仅提示 chunk 较大（P3），不是构建失败。

---

## 3. 浏览器回归

| 脚本 | 结果 |
| --- | --- |
| `ms04-browser-check.mjs` | PASS |
| `ms05-browser-check.mjs` | 初次 FAIL（'14' !== '11'）；清理测试数据后 **PASS** |
| `ms06-2`～`ms06-9` | 全部 PASS |
| `ms07-3` | PASS |
| `ms08-e2e` | PASS |
| `ms08-edge` | PASS |

### MS05 初次失败分析

- 当前主表初始 0；
- MS04 保存 3 条真实测试记录（id 41～43）且脚本未清理；
- MS05 自建 11 条后总数为 14，断言期望 11；
- 根因是**测试脚本之间的数据隔离不足**，不是业务功能失败；
- 清理本轮测试数据（id 41～58）后重跑 MS05，得到 `MS05_BROWSER_OK`；
- 最终清理 MS05 自己创建的 id 59～70，主表回到 0。

**这是 P2 测试卫生问题，记录在 MS13 剩余问题中。**

---

## 4. 数据库 smoke

```text
GET /api/health     200
GET /api/health/db  200
主表初始/最终 0 行
```

未修改 MySQL 配置/账号/防火墙/结构。

---

## 5. 性能

本轮**未重新执行性能测试**。沿用 MS-08 原始 30 样本证据（P95 2.81/3.59ms）；代码未改，历史证据可参考，但报告不把它冒充本轮新结果。

---

## 6. 结论

- 后端全量：PASS；
- 前端构建：PASS；
- Chrome/Edge：PASS（MS05 数据清理后）；
- 性能：历史证据，未重跑；
- 业务功能回归：通过；
- 测试隔离问题：P2，不影响产品结论。
