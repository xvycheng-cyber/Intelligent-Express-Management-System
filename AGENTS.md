# AGENTS.md

## 一、项目基本信息

- 仓库标识：`Intelligent-Express-Management-System`
- 系统名称：快递通——智能快递地址簿录入与管理系统
- 课程论文题目：快递地址簿录入程序设计（不得改写）

## 二、当前阶段

MS-01 工程初始化、MS-02 解析模块、MS-03 地址簿后端、MS-03.1 契约修复、MS-04 批量录入前端、MS-05 地址簿管理前端、MS-06 Web 基础实践、MS-07 Servlet 映射演示和 MS-08 全量测试已完成。批量录入、地址查询/搜索/分页、新建/编辑/删除/批量删除、Web 实践 2.1～2.19、Servlet 映射 2.20 已实现；MS-08 仍有一个 P1（MySQL X Plugin 33060/防火墙暴露）待用户处置；Playwright CI 尚未实际运行，用户登录仍不在本项目范围。

### 技术栈

- 后端：Java 17、Spring Boot 3.3.5、Spring MVC、MyBatis-Plus 3.5.7、MySQL 8、Jakarta Validation、JUnit 5、Maven。
- 前端：Vue 3、TypeScript、Vite、Element Plus、Axios、Vue Router。

### 本机数据库

- 本机课程演示使用 MySQL `root` 连接 `127.0.0.1:3306/express_address_book`；口令只放环境变量或本地 `application-local.yml`。
- 不得创建 `root@'%'`、不得开放远程登录、不得把 root 用于公共网络部署；`admin-init.sql` 的 `express_app` 为可选最小权限方案。
- 不修改数据库账号方案时，继续使用现有 root 本地配置。

## 三、核心业务规则

- 多行文本一行一条联系人；空行忽略但保留原始物理行号。
- 分隔符：中文/英文逗号、Tab、中文/英文分号；顿号与空格不作字段分隔。
- 地址必须保留完整原文：只剔除姓名/电话区间，只 trim 首尾空白/分隔符，不改写内部标点。
- 解析与保存严格分离：解析 API 不写数据库；用户确认后保存。
- 部分行失败不得丢弃其他行；每行返回 `lineNo/rawLine/receiverName/phone/fullAddress/status/errors`。
- 姓名：中文/英文均为 **1～50 个 Unicode 码点**；中文 `^[\p{IsHan}·]{1,50}$`，英文 `^[A-Za-zＡ-Ｚａ-ｚ][A-Za-zＡ-Ｚａ-ｚ .'\-．＇－]{0,49}$`。
  - 长度用 `codePointCount`，不要用 UTF-16 `String.length()`。
  - 超过 50 → E111；1～50 内非法字符 → E106。
  - 前端用 `Array.from(value).length`，不能只依赖 HTML `maxlength`。
- 电话：手机/座机/脱敏/`+86`，输出规范化；纯数字 5 位以上但非法：>20 位 E108，否则 E104。
- 解析错误码：E102～E108、E110、E111；**E109 已删除，不得重新引入**。
- 姓名候选回退：不能因为第一个候选非法就终止；优先合法候选，非法前置候选不得残留进地址。
- 电话包裹符与空白：只有确定成对的包裹符才随电话删除；地址内部正常括号不得删除。
- 英文地址词（Room/Building/Street/...、常见城市名）只作为姓名排除的有限启发式，不是完整地名库。

## 四、解析 API 输入上限

`POST /api/addresses/parse` 必须在调用 parser **之前**：

1. `rawText` 存在、非 null、非空白；
2. `rawText.length() <= 20000`；
3. 有效非空行（去 BOM、CRLF/CR/LF、空行）≤200；
4. 超限返回 400/40003，`data.violations[].field=rawText`；
5. 普通字段错误 40001；数量超限优先。

## 五、后端规范

- Controller 薄；业务在 Service；Mapper 只做数据访问。
- 统一响应 `R{code,message,data}`；全局异常处理；字段错误 `data.violations[]`，解析行错误 `items[].errors[]`，不得混用。
- parser 包禁止 Mapper/JDBC/MyBatis-Plus/IO；无 SQL。
- 所有 SQL 参数化；禁止字符串拼接。
- 不向接口/日志暴露口令、SQL、堆栈、完整手机号与详细地址。

## 六、前端规范

- Vue 3 Composition API + TypeScript；页面/组件/API/类型/样式分离。
- Element Plus；统一布局、加载/空/错误状态。
- 所有 HTTP 请求放 `src/api/`；统一 Axios 封装。
- 解析预览表支持编辑、删除行、状态/错误展示；后续保存用 `toPayload` 只提交业务字段。
- 姓名长度用 `Array.from`，不单独依赖 `maxlength`。

## 七、课程实践与论文

- 保留 Web 基础实践页（2.1～2.19）与 Servlet URI 映射演示；两者分别对应不同层次。
- 论文/截图必须基于真实代码与运行结果；不得编造测试、截图或实验过程。
- 真实截图保存到 `docs/screenshots/`。

## 八、测试要求

- 后端优先 `mvn clean test`，必要时 `mvn clean package`。
- 前端 `npm run build`。
- 未实际执行的测试不得标记为通过；Parser 单测、Controller 集成、数据库集成、浏览器测试必须区分。
- 当前基线：**160** 个后端测试全部通过；`npm run build` 通过；MS04/MS05/MS06/MS07 Playwright 回归与核心 E2E 已实际执行；MySQL 使用隔离 Schema `express_address_book_ms08_test`；Playwright CI 尚未实际运行。

## 九、文档与变更

- 设计细节以 `docs/requirements.md`、`architecture.md`、`database-design.md`、`api-design.md`、`parser-design.md`、`test-plan.md`、`development-roadmap.md`、`course-paper-outline.md` 为准。
- 代码与文档不一致时先分析并同步；不要擅自改变整体架构。
- MS-05 变更记录见 `docs/review/ms-05-address-ui-report.md`；MS-04 见 `docs/review/ms-04-import-ui-report.md`；MS-03.1 见 `docs/review/ms-03-remediation-report.md`；MS-03 见 `docs/review/ms-03-backend-report.md`。
- 修改本文件（工作区根目录 `AGENTS.md` 为硬链接）后需重建硬链接并校验 SHA256。
