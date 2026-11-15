# MS-10 演示演练记录

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 环境 | Windows 11 / JDK 17.0.21 / Node 22.14.0 / Chrome 157 / Edge 157 |
| 数据库 | MySQL 未启动；`127.0.0.1:3306` 无监听 |
| 浏览器 | **未在本轮启动浏览器全流程**；使用既有 Chrome/Edge 真实证据 |
| 结论 | 健康/前端/解析实际通过；保存/列表/编辑/删除 **BLOCKED — NOT EXECUTED** |

---

## 1. 启动

后端：

```text
java -jar backend/target/express-address-book-0.1.0-SNAPSHOT.jar
```

环境变量 `DB_URL` 指向 `express_address_book_ms08_test`，但 MySQL 未启动。

结果：

```text
127.0.0.1:8080 LISTENING
127.0.0.1:5173 LISTENING
```

MySQL 端口：

```text
127.0.0.1:3306 无监听
```

未启动/停止/重启 MySQL。

---

## 2. 实际检查

| 步骤 | 命令 | 实际结果 |
| --- | --- | --- |
| 后端存活 | `Invoke-WebRequest http://127.0.0.1:8080/api/health` | **200** `{"code":0,"data":{"status":"UP"}}` |
| 数据库健康 | `Invoke-WebRequest http://127.0.0.1:8080/api/health/db` | **500**（数据库不可用） |
| 前端页面 | `Invoke-WebRequest http://127.0.0.1:5173/` | **200**，返回 HTML |
| 解析 | `POST /api/addresses/parse`，3 行虚构文本 | **200**，valid=2，invalid=1 |
| 地址列表 | `GET /api/addresses?page=1&size=10` | **500**（数据库不可用） |
| 保存 | 页面/接口 | **BLOCKED — NOT EXECUTED** |
| 编辑/删除/批删 | 页面 | **BLOCKED — NOT EXECUTED** |

解析输入：

```text
菜小鸟，13800138000，浙江省杭州市西湖区湖滨小区12栋301室
张三，13900139000，浙江省杭州市余杭区文一西路100号
只有姓名没有电话
```

解析结果：2 条有效、1 条无效；第三条无电话/地址，符合“部分行失败不丢弃其他行”。

---

## 3. 演示流程完成度

| 演示步骤 | 状态 |
| --- | --- |
| 启动后端 | 实际通过 |
| 启动前端 | 实际通过 |
| 打开页面 | HTTP 200；未做浏览器截图 |
| 固定虚构数据 | 已准备 |
| 解析 | 实际通过（HTTP） |
| 检查解析结果 | 通过 valid/invalid 计数 |
| 人工修正 | 未在浏览器执行；代码/既有截图存在 |
| 保存确认 | 未执行 |
| 批量保存 | BLOCKED |
| 地址簿列表/搜索 | BLOCKED |
| 编辑/删除/批删 | BLOCKED |

---

## 4. 失败/阻塞原因

- MySQL 服务未启动；
- 本阶段明确禁止启动/停止/重启 MySQL；
- 没有可用数据库连接，地址列表/保存/编辑/删除无法真实执行；
- 未使用 Mock、静态 HTML、旧图或伪造响应来冒充。

---

## 5. 使用的既有证据

- `docs/screenshots/2-21-import.png`（MS-09 真实）；
- `docs/screenshots/2-21-parse-result.png`；
- `docs/screenshots/2-21-edit-preview.png`；
- `docs/screenshots/2-21-test-record.png`；
- `docs/test-results/ms-08/` 的 160 tests/性能/浏览器记录。

这些是 MS-08/MS-09 已执行证据，不冒充本轮新执行结果。

---

## 6. 结论

**5 分钟演示：PARTIAL（解析/健康/前端可演示；保存/列表未执行）。**  
**10 分钟演示：PARTIAL/BLOCKED（数据库业务步骤未执行）。**  
**完整项目验收：未达到。**

未伪造任何截图、数据库结果或测试输出。
