# MS-10 从零复现手册

| 项目 | 内容 |
| --- | --- |
| 目标 | 让未参与开发的人按文档完成环境检查、启动和核心流程验证 |
| 前提 | Windows、JDK 17、Maven、Node 20/22、MySQL 8、Chrome/Edge |
| 重要限制 | 本阶段禁止启动/停止/重启 MySQL；MySQL 未运行时，数据库相关步骤为 BLOCKED |

---

## 1. 获取代码

```powershell
git clone https://github.com/xvycheng-cyber/Intelligent-Express-Management-System.git
cd Intelligent-Express-Management-System
```

不执行 reset/clean/checkout；保留用户未提交文件。

## 2. 检查版本

```powershell
java -version
mvn -v
node -v
npm -v
```

要求：

- Java **17**（不要让系统默认 Java 26 冒充）；
- Maven 3.9+；
- Node 20/22 LTS；
- MySQL 8.0.x。

本机 JDK 17 路径：

```text
C:\Users\Chengzi\AppData\Local\Temp\dsh-ms01-tools\jdk17\jdk-17.0.21+9
```

本机 Maven：

```text
C:\Users\Chengzi\AppData\Local\Programs\IntelliJ IDEA\plugins\maven\lib\maven3\bin\mvn.cmd
```

## 3. 准备 MySQL

> 本阶段不启动 MySQL。以下命令由用户在已启动的本机 MySQL 上执行。

```powershell
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

可选脱敏演示数据：

```powershell
mysql -u root -p express_address_book < backend/src/main/resources/db/data.sql
```

注意：

- `schema.sql` 创建正式 `express_address_book`；
- 不要把测试 Schema 当正式库；
- 不要执行 DROP/TRUNCATE/全表 DELETE；
- 不要创建 `root@'%'`。

## 4. 配置连接（不提交口令）

```powershell
$env:DB_URL = "jdbc:mysql://127.0.0.1:3306/express_address_book?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<本机口令>"
```

或复制 `application-local.example.yml` 为未跟踪的 `application-local.yml`。

## 5. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

本机已验证的另一种方式：

```powershell
java -jar target/express-address-book-0.1.0-SNAPSHOT.jar
```

确认监听：

```powershell
netstat -ano | findstr ":8080"
```

预期：`127.0.0.1:8080`。

## 6. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

确认：`127.0.0.1:5173`，Vite 代理 `/api`、`/demo` 到 8080。

## 7. 访问与健康检查

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8080/api/health
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:5173/
```

预期：200。

数据库检查：

```powershell
Invoke-WebRequest -UseBasicParsing http://127.0.0.1:8080/api/health/db
```

需要真实 MySQL；MySQL 未启动时返回 500，不能写成通过。

## 8. 完成一次解析

打开 `http://127.0.0.1:5173/import`，粘贴：

```text
菜小鸟，13800138000，浙江省杭州市西湖区湖滨小区12栋301室
张三，13900139000，浙江省杭州市余杭区文一西路100号
只有姓名没有电话
```

点击“识别地址”。预期：2 条有效、1 条无效；无效行显示缺失电话/地址/姓名原因。

本阶段已实际通过 HTTP 解析验证（2 valid / 1 invalid），但不写数据库。

## 9. 保存一次地址

前提：MySQL 可用。

在预览表修正无效行，点击“保存地址”，确认弹窗显示有效/修改/异常/实际提交数量，确认保存。

本阶段 MySQL 未启动，**BLOCKED — NOT EXECUTED**；不得伪造成功。

## 10. 查看地址簿

打开 `/addresses`，查看列表、总数、分页。

本阶段 **BLOCKED**（数据库不可用）。

## 11. 编辑

点击列表行“编辑”，修改姓名/电话/地址，保存。

本阶段 **BLOCKED**。

## 12. 删除

点击“删除”，在二次确认中确认；只做逻辑删除。

本阶段 **BLOCKED**；不要执行物理删除。

## 13. 运行基本测试

```powershell
cd backend
mvn -B clean test
```

完整 160 项需要 MySQL 环境；本阶段未重新运行，引用 MS-08 证据 `Tests run: 160`。

前端构建：

```powershell
cd frontend
npm run build
```

## 14. 关闭应用

前端 `Ctrl+C`；后端 `Ctrl+C`。不要停止/重启 MySQL。

## 15. 复现结果

| 步骤 | 本轮结果 |
| --- | --- |
| 后端启动 | 实际成功（8080） |
| 前端启动 | 实际成功（5173） |
| `/api/health` | 200 |
| 页面访问 | 200 |
| 解析 | 200，2 valid/1 invalid |
| `/api/health/db` | 500（MySQL 未启动） |
| 保存/列表/编辑/删除 | **BLOCKED** |

**结论：运行方式可复现；数据库业务全流程未在本轮完整复现。**
