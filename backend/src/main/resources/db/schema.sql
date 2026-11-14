-- ============================================================
-- 快递通——智能快递地址簿录入与管理系统
-- 数据库与表结构初始化（由管理员账号执行）
-- 可重复执行；应用运行账号不应执行本文件。
-- ============================================================

CREATE DATABASE IF NOT EXISTS express_address_book
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE express_address_book;

CREATE TABLE IF NOT EXISTS express_address (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    receiver_name VARCHAR(50)  NOT NULL               COMMENT '收件人姓名，1～50 个 Unicode 码点',
    phone         VARCHAR(20)  NOT NULL               COMMENT '联系电话，保存前按规则规范化',
    full_address  VARCHAR(255) NOT NULL               COMMENT '完整收件地址，保留原文与内部标点',
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
