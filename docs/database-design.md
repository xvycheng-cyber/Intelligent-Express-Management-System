# 数据库设计（database-design.md）

| 项目 | 内容 |
| --- | --- |
| 文档版本 | v1.0 |
| 数据库 | MySQL 8.0（InnoDB / utf8mb4） |
| ORM | MyBatis-Plus 3.5.x（`BaseMapper` + 少量 XML） |
| 关联文档 | `requirements.md`（BR-10～BR-12）、`api-design.md`（字段名）、`architecture.md`（数据访问层） |
| 设计原则 | 只建必要的表；字段长度与后端校验严格一致；逻辑删除；参数化 SQL；不建外键/触发器/存储过程 |

---

## 1. 设计结论先行

1. **只有一张业务表**：`express_address`。本系统没有用户、没有批次、没有附件，解析结果不落库，因此不需要 `parse_task`、`import_batch`、`user` 等表。多建表只会增加事务与一致性负担。
2. **地址只存 `full_address` 一个字段**，不拆省/市/区/街道。理由：
   - 任务书要求"联系人的全部信息"，AGENTS 明确要求"保留完整收件地址"；
   - 拆分需要行政区划库与地址标准化，属于本项目的范围外能力；
   - 拆分会在保存时改写用户地址（截断/丢标点），违反 BR-04。
   - 任务书附录5 的参考界面把地址拆成"省市区 + 详细地址"，那是**参考实现的选择**；本项目在文档和论文中明确说明差异及理由（见 §9）。
3. **删除是逻辑删除**（`is_deleted`），列表/详情/更新/删除语句默认都带 `is_deleted=0`。
4. **时间由数据库默认值生成**，避免应用与数据库两套时钟。
5. **不建外键**：单表无引用关系，外键只会增加写入开销与导入顺序约束。

---

## 2. 概念模型

```text
┌──────────────────────────────────────────────┐
│              express_address                 │
│  地址簿条目（一条记录 = 一个联系人的一个地址） │
├──────────────────────────────────────────────┤
│ PK  id               BIGINT                  │
│     receiver_name    VARCHAR(50)  收件人姓名  │
│     phone            VARCHAR(20)  联系电话    │
│     full_address     VARCHAR(255) 完整地址    │
│     create_time      DATETIME     创建时间    │
│     update_time      DATETIME     更新时间    │
│     is_deleted       TINYINT      逻辑删除    │
└──────────────────────────────────────────────┘
```

- 解析过程是**内存计算**，不产生任何持久化数据，所以没有"解析记录表"。
- 一次批量保存只产生多条 `express_address` 记录，靠事务保证一致，不需要"批次表"。
- 不做用户体系，所以没有 `user_id` 列（预留会导致无意义的空列与索引）。

---

## 3. 表结构

### 3.1 建库

```sql
CREATE DATABASE IF NOT EXISTS express_address_book
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;
```

### 3.2 建表 DDL

文件位置：`backend/src/main/resources/db/schema.sql`（可重复执行）

```sql
USE express_address_book;

CREATE TABLE IF NOT EXISTS express_address (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    receiver_name VARCHAR(50)  NOT NULL               COMMENT '收件人姓名',
    phone         VARCHAR(20)  NOT NULL               COMMENT '联系电话，保存前按解析规则规范化（去空格/短横/+86，脱敏 x 保留）',
    full_address  VARCHAR(255) NOT NULL               COMMENT '完整收件地址，保留用户原文与内部标点',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted    TINYINT      NOT NULL DEFAULT 0     COMMENT '逻辑删除：0未删除，1已删除',
    PRIMARY KEY (id),
    KEY idx_deleted_create (is_deleted, create_time),
    KEY idx_phone (phone)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '快递地址簿';
```

### 3.3 字段字典

| 字段 | 类型 | 允许 NULL | 默认值 | 约束/来源 | 后端校验（与列宽一致） | 说明 |
| --- | --- | --- | --- | --- | --- | --- |
| `id` | BIGINT | 否 | AUTO_INCREMENT | 主键 | 保存时忽略；路径参数必须为正整数 | 自增，不复用 |
| `receiver_name` | VARCHAR(50) | 否 | 无 | 业务必填 | `@NotBlank`、`@Size(max=50)`；去首尾空白后非空 | 中文/英文姓名均可 |
| `phone` | VARCHAR(20) | 否 | 无 | 业务必填 | `@NotBlank`、`@Size(max=20)`、`@Pattern`（见 parser PR-03）；保存前按 PR-03 规范化 | 规范化后号码；脱敏形态（`136xxxx0000`）保留 `x` |
| `full_address` | VARCHAR(255) | 否 | 无 | 业务必填 | `@NotBlank`、`@Size(max=255)`；去首尾空白后非空 | 内部标点原样保留 |
| `create_time` | DATETIME | 否 | CURRENT_TIMESTAMP | 数据库生成 | 请求不可写 | 列表默认排序字段 |
| `update_time` | DATETIME | 否 | CURRENT_TIMESTAMP ON UPDATE | 数据库生成 | 请求不可写 | 编辑后自动更新 |
| `is_deleted` | TINYINT | 否 | 0 | 逻辑删除 | 请求不可写 | 0/1 |

> 长度对齐说明：`receiver_name` 50、`phone` 20、`full_address` 255 三个数字同时出现在**数据库列宽、DTO 注解、前端 `maxlength`** 三处，任何一处改动必须三处同步（见 §8 检查清单）。

### 3.4 字符集与排序规则

| 项 | 值 | 理由 |
| --- | --- | --- |
| 字符集 | `utf8mb4` | 支持中文、emoji（地址/备注里可能出现）、生僻字 |
| 排序规则 | `utf8mb4_0900_ai_ci` | MySQL 8 默认；大小写与重音不敏感，搜索"abc"可匹配"ABC" |
| 连接参数 | `useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai` | 避免中文乱码与时区偏移 |

---

## 4. 索引设计

| 索引 | 列 | 用途 | 说明 |
| --- | --- | --- | --- |
| PRIMARY | `id` | 主键查询、编辑、删除 | 聚簇索引 |
| `idx_deleted_create` | (`is_deleted`, `create_time`) | 列表默认查询 `WHERE is_deleted=0 ORDER BY create_time DESC` | 与列表 SQL 的最左前缀匹配 |
| `idx_phone` | (`phone`) | 按完整电话精确查找/后续去重检查 | 单列 |

**关于关键词搜索**：`keyword` 采用 `LIKE '%kw%'`，前后都有通配符，**不会使用任何索引**，属于全表扫描。本课程数据规模（万级以内）可接受，故不引入全文索引/ES。此结论在 `test-plan.md` 有对应性能用例（TC-PERF-02），并用实测数据说话，不预设"很快"。

**没有唯一索引**：同一个人可以有多条地址；同一地址也可能被不同收件人使用。加唯一约束会误伤正常数据。重复提示列为 Backlog（见 `requirements.md` §5.3）。

---

## 5. 逻辑删除的落地

| 环节 | 做法 |
| --- | --- |
| 配置 | `mybatis-plus.global-config.db-config.logic-delete-field: isDeleted`、`logic-delete-value: 1`、`logic-not-delete-value: 0` |
| 实体 | `ExpressAddress.isDeleted` 加 `@TableLogic`（与全局配置二选一，本项目**两者都标注**，防止配置漂移） |
| 查询 | 所有 `selectList/selectById/selectPage` 自动追加 `is_deleted=0` |
| 更新 | `updateById` 自动追加 `is_deleted=0`，删除后记录不可再被编辑 |
| 删除 | `deleteById` / `deleteBatchIds` 实际执行 `UPDATE ... SET is_deleted=1 WHERE id=? AND is_deleted=0` |
| 唯一性 | 逻辑删除后占用原 id，不产生唯一冲突（本项目无唯一索引，无此问题） |
| 物理清理 | 提供手工 SQL（见 §6），不做定时任务（不引入调度组件） |

> 注意：`is_deleted` 用 `TINYINT` 而不是 `BOOLEAN`，是为了兼容 MySQL 与 MyBatis-Plus 的 0/1 约定，且便于将来扩展删除原因。

---

## 6. 初始化与运维脚本

| 脚本 | 位置 | 作用 |
| --- | --- | --- |
| `schema.sql` | `backend/src/main/resources/db/schema.sql` | 建库建表，可重复执行 |
| `data.sql` | `backend/src/main/resources/db/data.sql` | 3 条**演示数据**（脱敏号码、虚构地址），供首次启动与截图使用 |
| `clean.sql` | `docs/sql/clean.sql` | 手工物理清理已删除数据（仅开发环境使用） |

演示数据示例（虚构，不含任何真实个人信息）：

```sql
INSERT INTO express_address (receiver_name, phone, full_address) VALUES
('菜小鸟', '136xxxx0000', '浙江省杭州市西湖区湖滨小区12栋301室'),
('菜中鸟', '136xxxx0001', '湖北省武汉市江岸区沿江小区8栋602室'),
('菜大鸟', '136xxxx0002', '湖北省孝感市孝南区东苑小区15栋201室');
```

> 这三条即任务书与附录5 中出现的示例，号码采用脱敏形态，与 BR-13 一致。

物理清理（开发环境，谨慎执行）：

```sql
DELETE FROM express_address WHERE is_deleted = 1 AND update_time < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

---

## 7. 数据访问层设计

### 7.1 实体

```java
// entity/ExpressAddress.java（设计契约，第二阶段实现）
@TableName("express_address")
public class ExpressAddress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String receiverName;
    private String phone;
    private String fullAddress;
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime createTime;
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private LocalDateTime updateTime;
    @TableLogic
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Integer isDeleted;
}
```

- `create_time` / `update_time` / `is_deleted` 用 `FieldStrategy.NEVER`，保证应用永远不会覆盖数据库默认值。
- 开启 `map-underscore-to-camel-case`，无需逐列写 `@TableField("receiver_name")`。

### 7.2 Mapper

```java
@Mapper
public interface AddressMapper extends BaseMapper<ExpressAddress> { }
```

- 单表 CRUD 全部使用 `BaseMapper`，不手写 SQL。
- 仅当需要"关键词 + 分页"优化时才补一个 XML 方法；本设计优先使用 `LambdaQueryWrapper`（自动参数化、自动带逻辑删除条件）。

### 7.3 典型查询

| 场景 | 实现 | 生成的 SQL（示意） |
| --- | --- | --- |
| 分页列表 | `selectPage(page, wrapper.eq(...).and(w -> w.like(name).or().like(phone).or().like(address)).orderByDesc(createTime, id))` | `SELECT ... WHERE is_deleted=0 [AND (receiver_name LIKE ? OR phone LIKE ? OR full_address LIKE ?)] ORDER BY create_time DESC, id DESC LIMIT ?,?` |
| 详情 | `selectById(id)` | `SELECT ... WHERE id=? AND is_deleted=0` |
| 新建 | `insert(entity)` | `INSERT INTO express_address (receiver_name, phone, full_address) VALUES (?,?,?)` |
| 编辑 | `updateById(entity)` | `UPDATE express_address SET receiver_name=?, phone=?, full_address=? WHERE id=? AND is_deleted=0` |
| 单删 | `deleteById(id)` | `UPDATE express_address SET is_deleted=1 WHERE id=? AND is_deleted=0` |
| 批删 | `deleteBatchIds(ids)` | `UPDATE express_address SET is_deleted=1 WHERE id IN (?,?,...) AND is_deleted=0` |

> 所有 `LIKE` 的参数都用 `%kw%` 拼在**参数值**里（MyBatis `#{}`），不是拼在 SQL 文本里，天然防注入。

---

## 8. 与接口/前端字段映射（防漂移）

| 数据库列 | Java 属性 | JSON 字段（请求/响应） | 前端 TS 字段 | 校验上限 |
| --- | --- | --- | --- | --- |
| `receiver_name` | `receiverName` | `receiverName` | `receiverName` | 50 |
| `phone` | `phone` | `phone` | `phone` | 20 |
| `full_address` | `fullAddress` | `fullAddress` | `fullAddress` | 255 |
| `create_time` | `createTime` | `createTime`（`yyyy-MM-dd HH:mm:ss`） | `createTime: string` | — |
| `update_time` | `updateTime` | `updateTime` | `updateTime: string` | — |
| `id` | `id` | `id`（**字符串**传给前端，见下） | `id: string` | — |
| `is_deleted` | `isDeleted` | **不返回** | 无 | — |

**id 类型说明**：MySQL `BIGINT` 超出 JavaScript `Number.MAX_SAFE_INTEGER`，因此响应中的 `id` 统一序列化为 **字符串**（由 `JacksonConfig` 对所有 `Long` 统一注册 `ToStringSerializer`，VO 不逐字段加注解），前端 TS 用 `string`，路径参数与 `ids` 数组也传字符串，后端 `@PathVariable Long` 自动转换。此约定在 `api-design.md` 同步声明，禁止前端用 `number`。

**跨文档检查清单（每次改表必查）**：
1. `database-design.md` 字段字典；
2. `api-design.md` 请求/响应字段表；
3. `architecture.md` 目录结构中的 `types/address.ts`；
4. 前端表单 `maxlength` 与后端注解；
5. `test-plan.md` 的边界值用例（50/51、20/21、255/256）。

---

## 9. 与任务书附录5 参考实现的差异说明（论文需写）

| 项 | 附录5 参考界面 | 本项目 | 理由 |
| --- | --- | --- | --- |
| 地址字段 | "省市区" + "详细地址" 两个输入框 | 单个"详细地址" | 拆分需要行政区划数据且会截断用户原文；本项目要求"保留完整地址" |
| 登录 | 有登录页（图4） | 无登录 | 任务（一）未要求；不擅自增加身份体系 |
| 列表 | 卡片列表 + 批量删除 + 新建地址 | Element Plus 表格 + 分页 + 关键词搜索 + 同一组操作 | 课程要求"查询页面以列表形式浏览"，表格更利于分页与批量勾选 |
| 电话 | 显示为 `136 xxxx 0000` | 保存前规范化（去 `+86`、空格、短横），脱敏 `x` 原样保留 | 统一标识符格式，便于查找；不把脱敏号改成真号 |

---

## 10. 容量与备份

| 项 | 估算 |
| --- | --- |
| 单行平均长度 | 约 300～400 字节（含索引） |
| 1 万条数据占用 | 约 4 MB |
| 建议备份 | `mysqldump -u<user> -p express_address_book > backup.sql`（开发期每周一次即可） |
| 恢复 | `mysql -u<user> -p express_address_book < backup.sql` |

---

## 11. 安全与账号

1. 应用使用**专用数据库账号**，只授予 `express_address_book` 库的 `SELECT, INSERT, UPDATE, DELETE`，不授予 `DROP`。
2. 数据库口令只能出现在 `backend/src/main/resources/application-local.yml`（已在 `.gitignore`）或环境变量 `DB_USERNAME` / `DB_PASSWORD`；`application.yml` 只允许写占位符 `${DB_PASSWORD}`，**禁止**把真实口令写入 `application.yml`、README、论文、截图。
3. 论文截图不得出现数据库连接串、口令、真实联系人信息；演示数据使用 §6 的脱敏数据。
4. 所有 SQL 使用 `#{}` 参数占位；禁止 `String` 拼接（含 `ORDER BY` 字段名，排序字段写死在代码中）。

---

## 12. 变更记录

| 版本 | 日期 | 变更 | 说明 |
| --- | --- | --- | --- |
| v1.0 | 2026-11-14 | 建表与字段字典定稿 | 第一阶段设计，未执行任何 DDL |
