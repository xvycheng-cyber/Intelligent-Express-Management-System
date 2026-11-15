# MS-08 核心 E2E 记录

脚本：`frontend/scripts/ms08-e2e-check.mjs`

场景：

1. `/import` 输入三条虚构联系人；
2. 解析 3 行；
3. 保存 3 条；
4. `/addresses` 搜索测试前缀；
5. 编辑第一条并刷新验证；
6. 单条删除；
7. 选中剩余两条并批量删除；
8. 确认列表清空。

结果：

```text
MS08_E2E_OK
```

数据库：仅隔离 Schema `express_address_book_ms08_test`；测试后本轮 E2E 数据已删除。
