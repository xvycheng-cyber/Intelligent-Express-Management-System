# 数据库账号调整报告（root 本机演示）

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 仓库 | `Intelligent-Express-Management-System` |
| 数据库 | MySQL 8.0.43 / `express_address_book` / `127.0.0.1:3306` |
| 应用账号 | `root`（仅本机课程演示） |
| 口令来源 | 环境变量 `DB_PASSWORD` 或本地 `application-local.yml`（已忽略）；**未写入任何 Git 跟踪文件** |
| 设计变更 | 替代 v1.1-final 的“默认 express_app、禁止 root”方案 |

> 本报告只记录实际执行结果。实际 root 口令不写入本文件、README、代码或截图。

---

## 1. 修改文件

| 文件 | 修改内容 |
| --- | --- |
| `backend/src/main/resources/application.yml` | `username: ${DB_USERNAME:root}`、`password: ${DB_PASSWORD}`（无默认密码）；仍监听 `127.0.0.1:8080` |
| `backend/src/main/resources/application-local.example.yml` | 改为 root + `CHANGE_ME_LOCAL_ONLY` 占位符；说明仅本机演示 |
| `backend/src/main/resources/db/admin-init.sql` | 从“默认应用账号”改为**可选**最小权限方案，注释明确当前默认 root |
| `README.md` | 数据库初始化改为 root；环境变量/本地配置；安全限制；可选 express_app |
| `AGENTS.md` | §1.4/§1.5 数据库账号摘要改为 root 本机演示，express_app 可选 |
| `docs/requirements.md` | BR-22、§9.12 改为 root 本机方案；记录替代关系 |
| `docs/architecture.md` | §10.1 YAML、§10.3、§12 不变量、§13 步骤、§16 清单、变更记录 |
| `docs/database-design.md` | §11 账号方案重写为 root 默认 + express_app 可选；变更记录 |
| `docs/test-plan.md` | TC-SEC-02/11/12 重写，新增 TC-SEC-13（失败处理）/14（MySQL 监听）；AC-30 |
| `docs/development-roadmap.md` | MS-01 任务/出口条件、MS-00C 说明、变更记录 |
| `docs/course-paper-outline.md` | 2.21.3 与“设计决策 7”改为 root 本机；保留安全边界说明 |
| `docs/review/p0-finalization-report.md` | 增加后续变更说明与第 11 节，标注 F-02/BR-22 已被替代 |
| `docs/review/database-account-change-report.md` | 本报告（新增） |
| `frontend/package.json`、`frontend/scripts/build.mjs` | 为规避当前 Windows 路径/脚本 shell 导致的 `npm run build` 退出码问题，改为 Node 包装脚本（不影响业务） |
| `.gitignore` | 已确认忽略 `application-local.yml`、`.env`；无需改动 |

---

## 2. 当前数据库连接配置

### 2.1 `application.yml`

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://127.0.0.1:3306/express_address_book?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD}
```

### 2.2 `application-local.example.yml`

```yaml
spring:
  datasource:
    username: root
    password: CHANGE_ME_LOCAL_ONLY
```

### 2.3 实际运行方式

```bash
export DB_USERNAME=root
export DB_PASSWORD=<本机 root 口令>
mvn spring-boot:run
```

或复制示例为 `application-local.yml` 后填写口令（该文件被 `.gitignore` 忽略）。

---

## 3. 数据库账号设计

| 账号 | 状态 | 用途 |
| --- | --- | --- |
| `root` | **当前默认** | 本机建库/建表 + 应用运行；仅 127.0.0.1 |
| `express_app` | **可选** | 需要最小权限时执行 `admin-init.sql`；不执行不影响 root |

- `schema.sql` 不创建业务账号，只建库建表。
- `admin-init.sql` 已标注为可选，不再作为默认初始化步骤。
- 未创建 `root@'%'`；未重置现有 root 密码；未删除数据库；未修改现有用户数据。

---

## 4. 安全限制

1. MySQL 3306 仅绑定 `127.0.0.1`（本次启动使用 `--bind-address=127.0.0.1`；`my.ini` 未修改，建议自行加入 `bind-address=127.0.0.1`）。
2. Spring Boot 继续监听 `127.0.0.1:8080`；Vite 继续监听 `127.0.0.1:5173`。
3. root 仅用于个人电脑课程设计与本地演示，**不得用于公共网络部署**。
4. 口令只通过环境变量或本地 `application-local.yml` 提供；不写入 Git、README、论文截图。
5. 不创建 `root@'%'`，不开放远程 root 登录。
6. 不将应用配置用于公网/局域网暴露；MySQL X Plugin 33060 仍可能监听 `0.0.0.0`，如不需要可在本机关闭。

---

## 5. 已执行的验证（真实）

| # | 验证 | 实际结果 |
| --- | --- | --- |
| 1 | root 连接 `127.0.0.1:3306` | 成功，`SELECT VERSION()` = 8.0.43 |
| 2 | `SELECT user,host FROM mysql.user WHERE user='root'` | 仅 `root@localhost`，无 `root@'%'` |
| 3 | `SHOW DATABASES` | 初始只有系统库；`express_address_book` 不存在 |
| 4 | 执行 `schema.sql`（root） | 成功创建 `express_address_book` 与 `express_address`；表 0 行，未修改任何已有用户数据 |
| 5 | `SELECT 1` / 表访问 | 成功；`SELECT COUNT(*)` = 0 |
| 6 | 启动后端（root + 本地口令） | `Started ExpressAddressApplication`，健康检查 `{"code":0,"message":"OK","data":{"status":"UP"}}` |
| 7 | `GET /api/health/db` | `{"code":0,"message":"OK","data":{"status":"UP"}}` |
| 8 | MySQL `PROCESSLIST` | 可见 `root@localhost` 连接 `express_address_book`（应用连接） |
| 9 | 错误口令启动（8081） | `/api/health/db` 返回 **HTTP 500 / code 50002**，message 为通用“数据库操作失败，请稍后重试”；响应不含 SQL/口令/堆栈 |
| 10 | `mvn clean test`（JDK 17） | Tests run: 5, Failures: 0, Errors: 0 |
| 11 | `mvn clean package -DskipTests`（JDK 17） | BUILD SUCCESS |
| 12 | `npm run dev` / Vite 代理 | `127.0.0.1:5173` 返回 200；代理 `/api/health` 与 `/api/health/db` 均返回 `status:UP` |
| 13 | `npm run build`（当前配置） | 通过：`vue-tsc --noEmit` + Vite `1679 modules`，npm 退出码 0 |
| 14 | 仓库口令扫描 | 对真实口令字面值执行 `git grep` 与源码扫描，均无结果；示例文件只有 `CHANGE_ME_LOCAL_ONLY` |
| 15 | 监听地址 | 8080/5173 仅 `127.0.0.1`；3306 仅 `127.0.0.1`；33060 仍为 `0.0.0.0`（X Plugin） |

> 验证完成后已停止后端/前端；MySQL 3306 由本次启动，验证后可停止或按需保留。

---

## 6. 尚未完成的验证

| # | 项 | 说明 |
| --- | --- | --- |
| 1 | `data.sql` 演示数据导入 | 未向 3306 导入，避免写入非必要数据；表当前 0 行 |
| 2 | 持久化 MySQL `bind-address=127.0.0.1` | 本次通过命令行参数生效；`my.ini` 未修改，需用户自行加入后重启服务 |
| 3 | MySQL X Plugin 33060 | 仍监听 `0.0.0.0:33060`；本机演示可关闭或忽略，公共网络禁止 |
| 4 | `express_app` 可选方案 | 未在 3306 创建；需要时执行 `admin-init.sql` |
| 5 | 业务接口/Playwright | MS-02 之后才有业务接口，未执行 |
| 6 | 3306 的 root 密码持久化 | 未修改 root 密码；继续使用现有口令 |

---

## 7. 是否具备继续 MS-02 的条件

**具备（有条件）**：

- root 连接、`SELECT 1`、`express_address_book` 访问、Spring Boot 健康检查、失败错误处理均已实际验证；
- 配置和文档已统一为 root 本机方案，未发现明文口令；
- 前端代理、8080/5173 监听未被破坏；
- `mvn clean test` 5/5、前端构建通过。

**建议进入 MS-02 前完成**：

1. 在 `my.ini` 增加 `bind-address=127.0.0.1`（若尚未配置），重启 MySQL 后复核 `netstat`。
2. 确认 3306 的 `express_address_book` 为你希望使用的库（当前表 0 行；如需演示数据可执行 `data.sql`）。
3. 提交本次配置/文档变更（口令仍只留在本地环境变量或忽略文件）。

当前仍处于 MS-01 后、MS-02 前；未实现任何地址解析或 CRUD。
