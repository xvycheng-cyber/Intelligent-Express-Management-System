-- ============================================================
-- 【可选方案】最小权限应用账号初始化
-- 当前默认方案：本机课程演示使用 root 连接（密码由环境变量/application-local.yml 提供），
-- 不要求执行本文件。仅在希望从 root 切换到 express_app 时，由管理员账号执行。
-- 重要：
--   1. 执行前把 CHANGE_ME_STRONG_PASSWORD 替换为本机强口令；
--   2. 不要提交替换后的文件到 Git；
--   3. express_app 只允许对业务表做 DML，不授予 DDL 与用户管理权限。
-- ============================================================

CREATE USER IF NOT EXISTS 'express_app'@'127.0.0.1'
    IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';

ALTER USER 'express_app'@'127.0.0.1'
    IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';

GRANT SELECT, INSERT, UPDATE, DELETE
    ON express_address_book.express_address
    TO 'express_app'@'127.0.0.1';

-- 复核：应只看到 SELECT/INSERT/UPDATE/DELETE，且没有 DROP/CREATE USER/GRANT
SHOW GRANTS FOR 'express_app'@'127.0.0.1';
