# MS-10 演示环境清单

| 项目 | 内容 |
| --- | --- |
| 日期 | 2026-11-15 |
| 主机 | Windows 11 专业版 10.0.26200 |
| 性质 | 本机课程演示环境，不是生产部署 |

---

## 1. 版本

| 组件 | 实际版本 | 说明 |
| --- | --- | --- |
| Windows | Windows 11 专业版 10.0.26200 | 当前验证主机 |
| JDK | OpenJDK 17.0.21 | 项目要求 Java 17；**系统默认 `java` 为 26，不能用于本项目验证** |
| JDK 路径 | `C:\Users\Chengzi\AppData\Local\Temp\dsh-ms01-tools\jdk17\jdk-17.0.21+9` | 本机工具链路径 |
| Maven | 3.9.11 | `mvn` 未加入全局 PATH 时使用 IntelliJ 自带 Maven 路径 |
| Node.js | v22.14.0 | 项目要求 20/22 LTS |
| npm | 10.9.2 | 随 Node |
| MySQL | 8.0.43 Community Server | 通过 `mysqld --version` 读取；**本阶段未启动/停止/重启服务** |
| Chrome | 157.0.8103.52 | Playwright 本机 Chrome |
| Edge | 157.0.4341.57 | Playwright `channel:msedge` |
| Playwright | 1.65.1 | `frontend/package-lock.json` |

## 2. 端口与监听

| 服务 | 端口 | 预期监听 |
| --- | --- | --- |
| 后端 Spring Boot | 8080 | `127.0.0.1:8080` |
| 前端 Vite | 5173 | `127.0.0.1:5173` |
| MySQL JDBC | 3306 | `127.0.0.1:3306` |
| MySQL X Plugin | 33060 | **D08 风险；不应作为项目依赖** |

项目不使用 33060；D08 仍 P1 OPEN，禁止为了方便演示修改监听或防火墙。

## 3. 数据库与 Schema

| Schema | 用途 | 注意 |
| --- | --- | --- |
| `express_address_book` | 正式业务 Schema | 不得用测试/演示数据批量污染 |
| `express_address_book_ms08_test` | MS-08 隔离测试/性能 | 本轮不得启动 MySQL 或写入 |
| `express_address_book_test` | 旧测试库 | 38 行历史数据，未清理 |

演示数据必须使用虚构姓名/电话/地址；不得使用真实收件人信息。

## 4. 环境变量

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/express_address_book?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<本机口令>"
```

口令只放在本机环境变量或未跟踪的 `application-local.yml`；不写入 README、脚本、日志或截图。

## 5. 启动命令

```powershell
cd backend
mvn spring-boot:run
```

或：

```powershell
mvn clean package
java -jar target/express-address-book-0.1.0-SNAPSHOT.jar
```

```powershell
cd frontend
npm install
npm run dev
```

## 6. 访问与健康检查

```text
前端：http://127.0.0.1:5173
后端：http://127.0.0.1:8080
存活：GET /api/health
数据库：GET /api/health/db
```

`/api/health` 不检查数据库；`/api/health/db` 需要真实 MySQL。不能把前者当作数据库健康证明。

## 7. 常见启动失败

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| `java` 版本为 26 | 系统 PATH 默认 JDK 不是 17 | 用 JDK 17 显式路径或设置 `JAVA_HOME` |
| `mvn` 找不到 | Maven 未全局安装 | 使用 IntelliJ Maven 路径或安装 Maven 3.9+ |
| `node`/`npm` 找不到 | Node 未加入 PATH | 使用本机 Node 22 路径 |
| 8080 被占用 | 旧后端未退出 | 只读查 PID 后停止自己的进程，不杀未知进程 |
| 5173 被占用 | Vite 已在运行 | `strictPort` 会失败；先确认已有实例 |
| 数据库连接失败 | MySQL 未启动/口令错误/Schema 不存在 | 本阶段禁止代启动 MySQL；记录 BLOCKED，请用户处理 |
| `/api/health/db` 500 | 数据库不可用 | 检查 MySQL 与 Schema；不要修改配置 |
| 页面无数据 | 后端未启动/Db 错误 | 查看网络请求和 `/api/health` |

## 8. 安全限制

- 后端/前端仅监听 `127.0.0.1`；
- 无登录/权限；
- 不适合公网；
- 3306/33060 不向公网开放；
- D08 P1 OPEN；
- 本次不得修改 MySQL/防火墙。

**本清单不声称数据库已启动；MS-10 演示时 MySQL 未启动，数据库相关步骤标记 BLOCKED。**
