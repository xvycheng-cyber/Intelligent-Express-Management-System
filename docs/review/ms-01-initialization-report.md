# MS-01 工程初始化报告

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-14 |
| 阶段 | MS-01 工程初始化 |
| 仓库 | `Intelligent-Express-Management-System` |
| 系统名称 | 快递通——智能快递地址簿录入与管理系统 |
| 设计基线 | v1.1-final（设计冻结） |
| 状态 | **已完成工程骨架；未实现地址解析与 CRUD；测试结果均为本次真实执行** |

> 本报告只记录实际执行过的命令与结果。数据库初始化/连接验证使用本机临时 MySQL 8.0.43 实例（127.0.0.1:3307）；现有 3306 实例因管理员口令未知**未改动**，详见 §4 与 §9。
>
> **后续变更（2026-11-14）**：应用数据库账号已调整为 **root 本机连接**（口令外置），`express_app` 降级为可选方案；本文 §4/§6 中“默认 express_app”的描述为当时方案，已被 [`database-account-change-report.md`](database-account-change-report.md) 替代。

---

## 1. 项目目录结构

```text
Intelligent-Express-Management-System/
├── AGENTS.md
├── README.md
├── .gitignore
├── docs/
│   ├── requirements.md
│   ├── architecture.md
│   ├── database-design.md
│   ├── api-design.md
│   ├── parser-design.md
│   ├── test-plan.md
│   ├── development-roadmap.md
│   ├── course-paper-outline.md
│   └── review/
│       ├── p0-finalization-report.md
│       └── ms-01-initialization-report.md
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/express/addressbook/
│       │   ├── ExpressAddressApplication.java
│       │   ├── common/
│       │   │   ├── R.java
│       │   │   ├── ResultCode.java
│       │   │   ├── Violation.java
│       │   │   ├── BusinessException.java
│       │   │   └── GlobalExceptionHandler.java
│       │   ├── config/
│       │   │   ├── MybatisPlusConfig.java
│       │   │   └── JacksonConfig.java
│       │   └── controller/
│       │       ├── HealthController.java
│       │       └── DbHealthController.java
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── application-local.example.yml
│       │   └── db/
│       │       ├── schema.sql
│       │       ├── admin-init.sql
│       │       └── data.sql
│       └── test/java/com/express/addressbook/
│           ├── common/RTest.java
│           ├── common/GlobalExceptionHandlerTest.java
│           ├── controller/HealthControllerTest.java
│           └── controller/DbHealthControllerTest.java
└── frontend/
    ├── package.json
    ├── package-lock.json
    ├── .npmrc
    ├── index.html
    ├── vite.config.ts
    ├── tsconfig.json
    ├── .env.example
    └── src/
        ├── main.ts
        ├── App.vue
        ├── env.d.ts
        ├── api/request.ts
        ├── api/health.ts
        ├── layout/AppLayout.vue
        ├── router/index.ts
        ├── styles/index.scss
        ├── types/api.ts
        └── views/
            ├── ImportView.vue
            └── NotFoundView.vue
```

未创建：`backend/src/main/java/com/express/addressbook/{parser,service,mapper,entity,dto,vo,servlet}`、`frontend/src/components` 等业务目录——按里程碑在 MS-02～MS-07 创建。

---

## 2. 创建的文件清单

### 2.1 后端

| 文件 | 说明 |
| --- | --- |
| `backend/pom.xml` | Spring Boot 3.3.5 / Java 17 / Web / Validation / MyBatis-Plus 3.5.7 / MySQL / Test |
| `ExpressAddressApplication.java` | 启动入口 |
| `common/R.java` | 统一响应 `{code,message,data}` |
| `common/ResultCode.java` | 0/40001/40002/40003/40401/40402/40501/50001/50002 |
| `common/Violation.java` | `data.violations[]` 元素 |
| `common/BusinessException.java` | 业务异常 |
| `common/GlobalExceptionHandler.java` | 参数/类型/404/405/数据库/未知异常统一映射；`items` 的 `@Size` 违规映射 40003 |
| `config/MybatisPlusConfig.java` | 分页拦截器 + `@MapperScan` |
| `config/JacksonConfig.java` | Long → String（防 JS 精度丢失） |
| `controller/HealthController.java` | `GET /api/health`，只检查 Web |
| `controller/DbHealthController.java` | `GET /api/health/db`，`SELECT 1` 独立检查数据库 |
| `resources/application.yml` | 127.0.0.1:8080；数据源环境变量；Hikari 延迟连接；MP 配置；无 CORS |
| `resources/application-local.example.yml` | `express_app` + 占位口令，无真实密码 |
| `resources/db/schema.sql` | 建库建表 |
| `resources/db/admin-init.sql` | 创建 `express_app` 并只授 DML |
| `resources/db/data.sql` | 3 条脱敏演示数据，可重复执行 |
| 4 个测试类 | 统一响应、健康检查、异常映射、数据库健康检查 mock |

### 2.2 前端

| 文件 | 说明 |
| --- | --- |
| `frontend/package.json` | Vue 3.5 / TS 5.7 / Vite 5.4 / Element Plus 2.14 / Axios / Vue Router |
| `frontend/vite.config.ts` | host 127.0.0.1、port 5173、`/api` 代理到 127.0.0.1:8080 |
| `frontend/tsconfig.json` | strict、bundler、`@/*` 别名 |
| `frontend/.npmrc` | `script-shell=powershell.exe`（路径含 `&` 时避免 cmd 解析失败） |
| `frontend/index.html` | 入口页 |
| `frontend/src/main.ts` | Vue + Element Plus + Router + 全局样式 |
| `frontend/src/layout/AppLayout.vue` | 统一头部/主区/底部布局 |
| `frontend/src/router/index.ts` | `/` → `/import` 占位页、404 |
| `frontend/src/views/ImportView.vue` | 初始化占位页 + 健康状态展示 |
| `frontend/src/api/request.ts` | Axios 统一封装，解包 `{code,message,data}` |
| `frontend/src/api/health.ts` | 健康检查请求 |
| `frontend/src/types/api.ts` | ApiResponse / Violation / HealthData |
| `frontend/src/styles/index.scss` | 全局变量与基础样式 |
| `frontend/.env.example` | `VITE_API_BASE=/api` 示例 |

### 2.3 文档与基础文件

| 文件 | 变更 |
| --- | --- |
| `README.md` | 更新为 MS-01 实际命令、健康检查、账号分离、目录 |
| `AGENTS.md` | 增加 MS-01 当前阶段、目录 review、硬链接提醒 |
| `docs/review/ms-01-initialization-report.md` | 本报告 |
| `.gitignore` | 已覆盖 `application-local.yml`、`.env`、`target/`、`node_modules/`、`dist/`、`test-results/`、`playwright-report/`、`*.log`、`.idea/`、`.vscode/` |

---

## 3. 依赖版本（实际解析/安装）

| 组件 | 版本 |
| --- | --- |
| Java | Temurin 17.0.21+9（编译与运行） |
| Maven | Apache Maven 3.9.11（IntelliJ 自带） |
| Spring Boot | 3.3.5 |
| MyBatis-Plus | 3.5.7（`mybatis-plus-spring-boot3-starter`） |
| MySQL Connector/J | 8.3.0 |
| Hibernate Validator | 由 Boot 3.3.5 管理（Jakarta Validation） |
| JUnit | 5.10.5 |
| MySQL Server | 8.0.43 |
| Node.js / npm | 22.14.0 / 10.9.2 |
| Vue | 3.5.45 |
| TypeScript | 5.7.3 |
| Vite | 5.4.21 |
| Element Plus | 2.14.8 |
| Axios | 1.21.1 |
| Vue Router | 4.6.5 |
| Sass | 1.108.2 |

---

## 4. 数据库初始化方式

### 4.1 脚本与账号分离

| 脚本 | 执行账号 | 作用 |
| --- | --- | --- |
| `schema.sql` | 管理员 | 建库、建 `express_address` 表 |
| `admin-init.sql` | 管理员 | 创建 `'express_app'@'127.0.0.1'`，仅授 `SELECT/INSERT/UPDATE/DELETE` |
| `data.sql` | 管理员 | 3 条脱敏演示数据 |

`express_app` 不含 `DROP/ALTER/CREATE USER/GRANT`；应用 `application.yml` 使用 `${DB_USERNAME}`/`${DB_PASSWORD}`，无 root 默认值。

### 4.2 本次验证方式

- 现有 `127.0.0.1:3306` 实例**正在运行但 root 口令未知**，为避免破坏用户数据，本次未对其执行任何 DDL。
- 使用独立临时实例 **MySQL 8.0.43，`127.0.0.1:3307`**（独立 data 目录，未使用 3306 数据）执行：
  1. `schema.sql` → 建库建表成功；
  2. `admin-init.sql`（临时替换占位口令）→ 创建 `express_app` 成功；
  3. `data.sql` → 3 条数据成功；
  4. `express_app` 连接：`SELECT COUNT(*)` = 3；
  5. `SHOW GRANTS`：只有 `USAGE` + 目标表 `SELECT, INSERT, UPDATE, DELETE`；
  6. `CREATE TABLE` 被拒绝（ERROR 1142），证明无 DDL；
  7. `INSERT/SELECT/UPDATE/DELETE` 成功，测试行已删除，最终 3 条。
- 后端以 `DB_URL=...:3307`、`DB_USERNAME=express_app` 启动，`GET /api/health/db` 返回 `UP`；MySQL `PROCESSLIST` 可见 2 个 `express_app` 连接。

> **3306 正式库尚未初始化**：需要用户用本机 root 口令执行 `schema.sql`、替换口令后的 `admin-init.sql`、可选 `data.sql`。这是 MS-01 唯一未落到“用户现有 3306 实例”的项。

---

## 5. 启动命令

### 5.1 后端

```bash
cd backend
# 方式 A：环境变量（推荐）
export DB_USERNAME=express_app
export DB_PASSWORD=<本机口令>
mvn spring-boot:run

# 方式 B：application-local.yml
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
# 填写口令后
mvn spring-boot:run
```

或打包运行：

```bash
mvn clean package
java -jar target/express-address-book-0.1.0-SNAPSHOT.jar
```

### 5.2 前端

```bash
cd frontend
npm install
npm run dev
```

打开 `http://127.0.0.1:5173`。

> 本仓库路径含 `&`；`frontend/.npmrc` 已设置 PowerShell script-shell，`dev/build` 使用显式 Node 入口，已实际验证。

---

## 6. 实际执行的测试与命令

| # | 命令/操作 | 结果 |
| --- | --- | --- |
| 1 | `mvn -B -DskipTests compile`（JDK 26 临时用于下载依赖） | BUILD SUCCESS |
| 2 | `JAVA_HOME=JDK17 mvn -B clean test` | **Tests run: 5, Failures: 0, Errors: 0** |
| 3 | `JAVA_HOME=JDK17 mvn -B clean package -DskipTests` | BUILD SUCCESS，生成 28.6 MB 可执行 jar |
| 4 | `npm install`（Node 22.14.0） | added 110 packages |
| 5 | `npm run build` | `vue-tsc --noEmit` 通过；Vite 构建 1679 modules，BUILD 成功 |
| 6 | `java -jar ...`（JDK17） | Started ExpressAddressApplication in 2.57 s |
| 7 | `GET http://127.0.0.1:8080/api/health` | `{"code":0,"message":"OK","data":{"status":"UP"}}` |
| 8 | `GET http://127.0.0.1:8080/api/health/db`（3307 实例） | `{"code":0,"message":"OK","data":{"status":"UP"}}` |
| 9 | `GET /api/not-exist` | 404，`{"code":40402,"message":"接口不存在","data":null}` |
| 10 | `POST /api/health` | 405，`{"code":40501,"message":"请求方法不支持","data":null}` |
| 11 | `npm run dev` | Vite ready，`http://127.0.0.1:5173/` 返回 200 |
| 12 | `GET http://127.0.0.1:5173/api/health` | 代理返回 `code:0`、`status:UP` |
| 13 | `GET http://127.0.0.1:5173/api/health/db` | 代理返回 `code:0`、`status:UP` |
| 14 | MySQL 脚本与账号（3307） | schema/admin/data 成功；app SELECT 3；DDL 被拒 |
| 15 | `netstat -ano` | 8080/5173 仅 `127.0.0.1` 监听（见 §8） |

### 6.1 未执行/受限项

- 用户现有 **3306** 实例的 DDL 与 `express_app` 创建：**未执行**（root 口令未知）。
- Playwright 端到端：**未执行**（MS-08 范围，尚未安装 Playwright）。
- 浏览器人工走查/截图：**未执行**。
- 业务接口（parse/CRUD/批量）：**不存在**，未测试。

### 6.2 说明

- 第一次 `mvn package` 在旧后端仍占用 jar 时失败（无法重命名 jar）；停止旧进程后 `mvn clean package` 成功。该失败不影响最终结果，已记录以保持透明。
- 首次 `npm run build` 因仓库路径含 `&`、npm 默认 cmd shell 解析 `.bin` 失败；首次 `npm run dev` 因 PowerShell 执行策略禁止 `vite.ps1`。通过 `frontend/.npmrc`（`script-shell=powershell.exe`）与显式 Node 入口修复，最终 `npm run dev` / `npm run build` 均实际通过。
- 首次 `mvn compile` 使用系统 JDK 26 仅用于快速下载依赖；最终的 `mvn clean test` 与 `mvn clean package` 均在 **Temurin JDK 17.0.21** 下执行并通过。
- 启动日志有 `No MyBatis mapper was found ...` 警告：MS-01 尚无 Mapper，属预期；MS-03 创建 Mapper 后消除。`@MapperScan` 保留在设计约定的配置类中。
- 前端构建有 Element Plus 大 chunk（>500 kB）与 Sass legacy API 警告，不影响构建成功；后续可按需优化。

---

## 7. 测试结果（真实）

### 7.1 后端

```text
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

覆盖：`R` 成功/失败、`/api/health` 统一结构、`BusinessException`→404 统一结构、`DbHealthController` mock `SELECT 1`。

### 7.2 前端

```text
vue-tsc --noEmit: pass
vite build: 1679 modules transformed, built
```

---

## 8. 网络监听检查

`netstat -ano | findstr ":8080 :5173"` 的实际输出（本次）：

```text
TCP    127.0.0.1:5173    0.0.0.0:0    LISTENING   2284
TCP    127.0.0.1:8080    0.0.0.0:0    LISTENING   47008
```

- 8080、5173 均只监听 `127.0.0.1`，未出现 `0.0.0.0:8080` / `0.0.0.0:5173`。
- 临时 MySQL 3307 仅 `127.0.0.1:3307`；现有 3306 实例监听 `0.0.0.0:3306`（安装默认），不属于本应用启动配置，建议本机使用时不暴露。
- 验证完成后已停止本次启动的后端、前端、3306 与 3307 测试实例；再次启动按 §5 命令执行。

---

## 9. 尚未完成的任务

| # | 任务 | 说明 |
| --- | --- | --- |
| 1 | 在现有 3306 实例执行 `schema.sql`、`admin-init.sql`、`data.sql` | 需本机 root 口令；本次用 3307 临时实例验证 |
| 2 | `AddressParserService` 与解析单测 | MS-02 |
| 3 | 实体/Mapper/DTO/VO、地址 CRUD、批量保存/删除、搜索分页 | MS-03～MS-05 |
| 4 | Web 基础实践页 2.1～2.19 | MS-06 |
| 5 | Servlet URI 映射演示 | MS-07 |
| 6 | Playwright 10 步与测试记录 | MS-08 |
| 7 | 论文 2.1～2.21 与真实截图 | MS-09 |
| 8 | 清理 `@MapperScan` 无 Mapper 警告 | MS-03 创建 Mapper 后 |
| 9 | `.gitignore`/README/AGENTS 与 v1.1-final 的提交 | 用户确认后执行 Git 操作 |

---

## 10. 结论

MS-01 工程初始化完成：前后端可构建、可启动，健康检查与代理链路真实可用；数据库脚本与 `express_app` 最小权限已在独立 MySQL 8.0.43 实例验证。当前未实现任何地址解析或 CRUD 业务，符合“MS-01 只做初始化”的范围。

**下一步：MS-02 解析器。** 本报告完成后暂停，不自动进入 MS-02。
