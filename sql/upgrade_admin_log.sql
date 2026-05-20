-- ============================================
-- 增量SQL：管理员体系 + 操作日志
-- 在已有数据库上执行
-- ============================================

USE bookkeeping;

-- 1. 用户表增加角色字段
ALTER TABLE `user` ADD COLUMN `role` TINYINT NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户，1-管理员' AFTER `status`;
ALTER TABLE `user` ADD INDEX `idx_role` (`role`);

-- 2. 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL COMMENT '日志ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '操作用户ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '操作用户名',
    `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(200) DEFAULT NULL COMMENT '请求方法（类名.方法名）',
    `request_url` VARCHAR(500) DEFAULT NULL COMMENT '请求URL',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP请求方法（GET/POST等）',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数',
    `response_result` TEXT DEFAULT NULL COMMENT '返回结果',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '操作状态：0-失败，1-成功',
    `error_msg` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    `execution_time` BIGINT DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation` (`operation`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
