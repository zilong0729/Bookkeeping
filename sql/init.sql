-- 微信小程序个人记账系统数据库初始化脚本
-- 数据库: bookkeeping
-- 字符集: utf8mb4

-- 创建数据库
CREATE DATABASE IF NOT EXISTS bookkeeping DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bookkeeping;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `openid` VARCHAR(100) NOT NULL COMMENT '微信openid',
    `unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信unionid',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
    `avatar_url` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-正常',
    `role` TINYINT NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户，1-管理员',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_unionid` (`unionid`),
    KEY `idx_status` (`status`),
    KEY `idx_role` (`role`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 账单类别表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL COMMENT '类别ID',
    `user_id` BIGINT NOT NULL DEFAULT '0' COMMENT '用户ID，0表示系统默认类别',
    `name` VARCHAR(50) NOT NULL COMMENT '类别名称',
    `type` TINYINT NOT NULL COMMENT '类型：1-收入，2-支出',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
    `sort_order` INT NOT NULL DEFAULT '0' COMMENT '排序',
    `is_default` TINYINT NOT NULL DEFAULT '0' COMMENT '是否默认：0-否，1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单类别表';

-- 账单表
CREATE TABLE IF NOT EXISTS `record` (
    `id` BIGINT NOT NULL COMMENT '账单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `category_id` BIGINT NOT NULL COMMENT '类别ID',
    `type` TINYINT NOT NULL COMMENT '类型：1-收入，2-支出',
    `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
    `contact_name` VARCHAR(100) DEFAULT NULL COMMENT '往来对象姓名',
    `record_date` DATE NOT NULL COMMENT '账单日期',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_type` (`type`),
    KEY `idx_record_date` (`record_date`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_user_date` (`user_id`, `record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='账单表';

-- 插入默认类别数据（收入）
INSERT INTO `category` (`id`, `user_id`, `name`, `type`, `icon`, `sort_order`, `is_default`) VALUES
(1, 0, '婚宴', 1, 'wedding', 1, 1),
(2, 0, '乔迁', 1, 'house', 2, 1),
(3, 0, '添丁', 1, 'baby', 3, 1),
(4, 0, '春节红包', 1, 'redpacket', 4, 1),
(5, 0, '生日', 1, 'birthday', 5, 1),
(6, 0, '升学', 1, 'education', 6, 1),
(7, 0, '其他收入', 1, 'other', 99, 1);

-- 插入默认类别数据（支出）
INSERT INTO `category` (`id`, `user_id`, `name`, `type`, `icon`, `sort_order`, `is_default`) VALUES
(101, 0, '婚宴', 2, 'wedding', 1, 1),
(102, 0, '乔迁', 2, 'house', 2, 1),
(103, 0, '添丁', 2, 'baby', 3, 1),
(104, 0, '春节红包', 2, 'redpacket', 4, 1),
(105, 0, '生日', 2, 'birthday', 5, 1),
(106, 0, '升学', 2, 'education', 6, 1),
(107, 0, '其他支出', 2, 'other', 99, 1);

-- 用户Token表
CREATE TABLE IF NOT EXISTS `user_token` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `token` VARCHAR(500) NOT NULL COMMENT '登录token',
    `device_type` TINYINT DEFAULT NULL COMMENT '设备类型：1-微信小程序，2-Android，3-iOS，4-Web',
    `device_name` VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    `device_id` VARCHAR(200) DEFAULT NULL COMMENT '设备标识（如设备ID、UUID等）',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理（浏览器信息）',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `last_active_time` DATETIME NOT NULL COMMENT '最后活跃时间',
    `status` TINYINT NOT NULL DEFAULT '1' COMMENT '状态：0-无效，1-有效',
    `login_count` INT NOT NULL DEFAULT '1' COMMENT '登录次数（同一设备）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_token` (`token`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_status` (`status`),
    KEY `idx_deleted` (`deleted`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户Token表';

-- 操作日志表
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
