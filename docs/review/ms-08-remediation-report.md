# MS-08 缺陷修复与完整回归报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 范围 | MS08-D01～D03 修复，D04～D07 记录 |
| 结论 | P0 0；P1 1（D08 待用户处置）；P2 修复 3 项、遗留 2 项 |

---

## 1. 修复清单

### D01 405 Allow

修改：

```text
backend/src/main/java/com/express/addressbook/servlet/MappingDemoServletSupport.java
backend/src/main/java/com/express/addressbook/common/GlobalExceptionHandler.java
backend/src/test/java/com/express/addressbook/servlet/ServletMappingIntegrationTest.java
frontend/scripts/ms07-3-browser-check.mjs
```

- 自定义 Servlet：`Allow: GET, HEAD, OPTIONS` + 405；HEAD/OPTIONS 200；
- Spring MVC：按 `getSupportedHttpMethods()` 动态生成 Allow，并补 HEAD/OPTIONS；
- 没有把所有端点硬编码为 GET；
- 正常 GET、Servlet 映射优先级、状态码未改变。

实际验证：

```text
POST /demo/servlet/exact       → 405, Allow: GET, HEAD, OPTIONS
POST /demo/spring/method-only  → 405, Allow: GET, HEAD, OPTIONS
```

### D02 E2E 测试数据

`ms08-e2e-check.mjs` 首次用 10 位电话导致无效行；改为 11 位虚构号码，业务实现未改。

### D03 数据库隔离

- 新建 `express_address_book_ms08_test`；
- MS04/MS05/E2E 均连接隔离 Schema；
- 旧测试库 38 行未清理，主库未写入；
- 性能数据 10,000 条仅在本轮隔离 Schema。

---

## 2. 回归结果

```text
mvn -B clean test -Dspring.datasource.url=jdbc:mysql://127.0.0.1:3306/express_address_book_ms08_test
Tests run: 160, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

MS07_3_BROWSER_OK
MS04_BROWSER_OK
MS05_BROWSER_OK
MS06_2_BROWSER_OK
MS06_3_BROWSER_OK
MS06_4_BROWSER_OK
MS06_5_BROWSER_OK
MS06_6_BROWSER_OK
MS06_7_BROWSER_OK
MS06_8_BROWSER_OK
MS06_9_BROWSER_OK
MS08_E2E_OK
MS08_PERF_OK

npm run build
✓ built in 5.17s
```

---

## 3. 遗留

- D04 CI 未运行（P2）；
- D05 Edge 完整矩阵已在 R1 补测通过（CLOSED）；
- D06 并入 D08：MySQL X 33060 + 防火墙允许（P1 OPEN）；
- D07 2.20 主图高 2239px，论文排版需拆分（P3）。

没有未修复 P0；存在 1 个未修复 P1（D08，需用户授权修改 MySQL/防火墙）。

---

## 4. Git

未 commit/push/reset/clean/checkout/stash；工作区保留 MS-06 R1、MS-07、MS-08 变更。

**MS-08 修复与回归完成，进入最终交付报告。**
