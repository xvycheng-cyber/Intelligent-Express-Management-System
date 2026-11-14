# Intelligent-Express-Management-System

> 系统名称：快递通——智能快递地址簿录入与管理系统
> 仓库标识：`Intelligent-Express-Management-System`
> 课程论文题目：快递地址簿录入程序设计（对应《高级 Web 项目开发（实践）》任务书任务（一））

## 当前状态

**MS-01 工程初始化、MS-02 地址解析、MS-03 地址簿后端、MS-03.1 契约修复与 MS-04 批量录入前端已完成（2026-11-14）**。当前包含可启动的 Spring Boot 后端、可启动的 Vue 3 前端、批量地址录入页面、解析预览/人工修正/确认保存流程、地址 CRUD/分页搜索/批量保存/批量删除后端接口；地址簿列表管理页面尚未开发。

实际测试：`mvn clean test` 共 **146** 个测试（parser 86 + Controller MockMvc 12 + 真实 Parser/HTTP 8 + MySQL 16 + DTO 10 + LIKE 4 + 码点 5 + 基础 5），全部通过；前端 `npm run build` 通过。

设计文档入口：[`docs/requirements.md`](docs/requirements.md)、[`docs/architecture.md`](docs/architecture.md)、[`docs/database-design.md`](docs/database-design.md)、[`docs/api-design.md`](docs/api-design.md)、[`docs/parser-design.md`](docs/parser-design.md)、[`docs/test-plan.md`](docs/test-plan.md)、[`docs/development-roadmap.md`](docs/development-roadmap.md)、[`docs/course-paper-outline.md`](docs/course-paper-outline.md)。

审查与修复记录：[`docs/review/ms-01-initialization-report.md`](docs/review/ms-01-initialization-report.md)、[`docs/review/ms-02-parser-report.md`](docs/review/ms-02-parser-report.md)、[`docs/review/ms-02-remediation-report.md`](docs/review/ms-02-remediation-report.md)、[`docs/review/ms-02-final-report.md`](docs/review/ms-02-final-report.md)、[`docs/review/ms-03-backend-report.md`](docs/review/ms-03-backend-report.md)、[`docs/review/ms-03-remediation-report.md`](docs/review/ms-03-remediation-report.md)、[`docs/review/ms-03-final-review.md`](docs/review/ms-03-final-review.md)、[`docs/review/ms-04-import-ui-report.md`](docs/review/ms-04-import-ui-report.md)、[`docs/review/database-account-change-report.md`](docs/review/database-account-change-report.md)、[`docs/review/p0-finalization-report.md`](docs/review/p0-finalization-report.md)。

---

## 1. 项目介绍

系统面向快递业务中的联系人地址录入场景：用户在多行文本框中粘贴多条联系人信息，点击"识别"后由规则解析器转换为结构化数据；用户在预览表中核对、修改，并在保存确认弹窗中确认后写入数据库。

MS-02 实现解析模块与解析 API；MS-03/03.1 实现地址 CRUD、分页搜索、批量保存/删除后端；MS-04 实现批量录入前端。地址簿列表管理页面与 Playwright CI 尚未开发。

## 2. 技术栈

| 层 | 技术 / 本机验证版本 |
| --- | --- |
| 后端 | Java 17（Temurin 17.0.21）、Spring Boot 3.3.5、Spring MVC、MyBatis-Plus 3.5.7、MySQL Connector/J 8.3.0、Jakarta Validation、JUnit 5、Maven 3.9.11 |
| 前端 | Vue 3.5.45、TypeScript 5.7.3、Vite 5.4.21、Element Plus 2.14.8、Axios 1.21.1、Vue Router 4.6.5、Sass 1.108.2 |
| 数据库 | MySQL 8.0.43，字符集 `utf8mb4` |

## 3. 环境要求

| 项 | 要求 |
| --- | --- |
| JDK | 17（LTS） |
| Maven | 3.9+ |
| MySQL | 8.0.x，`utf8mb4` |
| Node.js | 20 LTS 或 22 LTS（本机验证 22.14.0 / npm 10.9.2） |
| 端口 | 后端 8080、前端 5173，均只监听 `127.0.0.1` |

## 4. 数据库初始化

> 本机课程演示使用 `root` 连接 `127.0.0.1:3306/express_address_book`；口令只通过环境变量或本地 `application-local.yml` 提供，**不得写入 Git**。root 仅限本机课程演示，禁止公共网络部署。

1. 建库建表：

   ```bash
   mysql -u root -p < backend/src/main/resources/db/schema.sql
   ```

2. 可选：导入脱敏演示数据：

   ```bash
   mysql -u root -p express_address_book < backend/src/main/resources/db/data.sql
   ```

3. 配置 root 口令（任选其一，均不入 Git）：

   ```bash
   export DB_USERNAME=root
   export DB_PASSWORD=<本机 root 口令>
   ```

   或复制 `backend/src/main/resources/application-local.example.yml` 为 `application-local.yml` 后填写口令。

4. 可选最小权限方案：替换 `admin-init.sql` 中的占位口令后执行；不执行则继续使用 root。

`application.yml` 中 `username` 默认 `root`，`password` 只使用 `${DB_PASSWORD}`，没有默认密码。

## 5. 后端启动

```bash
cd backend
mvn spring-boot:run
```

打包运行：

```bash
mvn clean package
java -jar target/express-address-book-0.1.0-SNAPSHOT.jar
```

## 6. 前端启动

```bash
cd frontend
npm install
npm run dev
```

打开 `http://127.0.0.1:5173`。Vite 将 `/api` 代理到 `http://127.0.0.1:8080`，开发期不额外开放 CORS。

> 仓库路径含 `&`；`frontend/.npmrc` 设置 `script-shell=powershell.exe`，`dev/build` 使用显式 Node 入口，已实际验证。

## 7. 健康检查与解析 API

| 接口 | 用途 | 是否检查数据库 |
| --- | --- | --- |
| `GET /api/health` | Web 服务存活 | 否 |
| `GET /api/health/db` | 应用账号 `SELECT 1` | 是 |
| `POST /api/addresses/parse` | 多行地址解析，不写库 | 否 |
| `GET /api/addresses` | 分页 + 三字段关键词 | 是 |
| `GET /api/addresses/{id}` | 单条详情 | 是 |
| `POST /api/addresses` | 新建单条 | 是 |
| `PUT /api/addresses/{id}` | 编辑单条 | 是 |
| `DELETE /api/addresses/{id}` | 逻辑删除 | 是 |
| `POST /api/addresses/batch` | 批量保存（事务） | 是 |
| `POST /api/addresses/batch-delete` | 批量逻辑删除 | 是 |

解析请求：

```json
{
  "rawText": "菜小鸟，13800138000，浙江省杭州市西湖区某某小区"
}
```

输入上限：`rawText` 非空、≤20000 字符、有效非空行 ≤200；超限返回 `400/40003`，`data.violations[].field=rawText`。

## 8. 本机访问与安全

- 系统**不包含身份认证**，这是课程任务（一）的范围限制，不是已实现的安全能力。
- 后端/前端/Vite 代理只监听 `127.0.0.1`；不得暴露到局域网或公网。
- 数据库 root 仅限本机课程演示；不创建 `root@'%'`，不开放远程登录，不提交口令。
- MySQL 建议 `bind-address=127.0.0.1`；33060 等附加端口按需关闭。
- 演示数据为虚构数据；不得上传真实联系人信息。

## 9. 测试

```bash
cd backend && mvn test
cd frontend && npm run build
```

当前后端 **146** 个测试全部通过（含真实 MySQL 集成 16 个）；前端构建通过；`frontend/scripts/ms04-browser-check.mjs` 已用本机 Chrome + Playwright 实际跑通批量录入流程；MySQL 集成使用独立 `express_address_book_test`，主库未被测试写入；Playwright 尚未纳入 CI，浏览器人工走查未执行。

## 10. 目录

```text
Intelligent-Express-Management-System/
├── AGENTS.md
├── README.md
├── .gitignore
├── docs/                     # 设计文档与 review 报告
├── backend/                  # Spring Boot、parser、解析 API、地址 CRUD
└── frontend/                 # Vue 3 + Vite 工程骨架
```

地址后端业务包（service/mapper/entity/vo）已实现；Vue 地址录入页面将在 MS-04 创建。
