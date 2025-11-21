create database managerment DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

CREATE TABLE `sys_user` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                            `userAccount` varchar(50) NOT NULL COMMENT '登录账号',
                            `userPassword` varchar(100) NOT NULL COMMENT '登录密码',
                            `userName` varchar(50) NOT NULL COMMENT '真实姓名',
                            `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
                            `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0-禁用,1-正常)',
                            `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `userAccount` (`userAccount`)
) ENGINE=InnoDB COMMENT='用户表';

CREATE TABLE `sys_role` (
                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
                            `roleName` varchar(50) NOT NULL COMMENT '角色名称',
                            `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
                            `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `roleName` (`roleName`)
) ENGINE=InnoDB COMMENT='角色表';

CREATE TABLE `sys_permission` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
                                  `name` varchar(100) NOT NULL COMMENT '权限名称',
                                  `description` varchar(200) DEFAULT NULL COMMENT '权限描述',
                                  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `roleName` (`name`)
) ENGINE=InnoDB COMMENT='权限资源表';

CREATE TABLE `sys_role_permission` (
                                       `roleId` bigint NOT NULL COMMENT '角色ID',
                                       `permissionId` bigint NOT NULL COMMENT '权限ID',
                                       `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                       UNIQUE KEY `uk_roleid_permissionid` (`roleId`,`permissionId`)
) ENGINE=InnoDB COMMENT='角色权限关联表';


CREATE TABLE `sys_user_role` (
                                 `userId` bigint NOT NULL COMMENT '用户ID',
                                 `roleId` bigint NOT NULL COMMENT '角色ID',
                                 `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（分配角色时间）',
                                 `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                 UNIQUE KEY `uk_userid_roleid` (`userId`, `roleId`)
) ENGINE=InnoDB COMMENT='用户角色关联表';

CREATE TABLE `sys_page` (
                            `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '页面ID',
                            `parentId` BIGINT DEFAULT NULL COMMENT '父页面ID，NULL 或 0 表示顶级',
                            `name` VARCHAR(100) NOT NULL COMMENT '页面/菜单显示名称',
                            `path` VARCHAR(200) DEFAULT NULL COMMENT '前端路由路径，如 /users',
                            `component` VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径/名称（前端用于动态路由）',
                            `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
                            `orderNum` INT NOT NULL DEFAULT 0 COMMENT '排序值，越大越靠前',
                            `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见（菜单展示）:1=显示,0=隐藏',
                            `meta` JSON DEFAULT NULL COMMENT '扩展字段，存放额外信息（如权限提示、layout 等）',
                            `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            `createBy` BIGINT DEFAULT NULL COMMENT '创建人ID',
                            PRIMARY KEY (`id`),
                            KEY `idx_parent` (`parentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面/菜单表';

CREATE TABLE `sys_role_page` (
                                 `roleId` BIGINT NOT NULL COMMENT '角色ID',
                                 `pageId` BIGINT NOT NULL COMMENT '页面ID',
                                 `grantType` TINYINT NOT NULL DEFAULT 1 COMMENT '授权类型：1=访问/显示权限,2=完全控制/管理（可自定义）',
                                 `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                 `createBy` BIGINT DEFAULT NULL COMMENT '创建人ID',
                                 UNIQUE KEY `uk_roleid_pageid` (`roleId`,`pageId`),
                                 KEY `idx_role` (`roleId`),
                                 KEY `idx_page` (`pageId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-页面关联表';

CREATE TABLE `sys_page_permission` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `pageId` BIGINT NOT NULL COMMENT '所属页面ID',
                                       `permissionId` BIGINT NOT NULL COMMENT 'sys_permission.id',
                                       `action` VARCHAR(100) DEFAULT NULL COMMENT '动作标识，如 create/update/delete/export',
                                       `description` VARCHAR(200) DEFAULT NULL,
                                       `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       `createBy` BIGINT DEFAULT NULL COMMENT '创建人ID',
                                       PRIMARY KEY (`id`),
                                       UNIQUE KEY `uk_page_perm` (`pageId`,`permissionId`),
                                       KEY `idx_page` (`pageId`),
                                       KEY `idx_permission` (`permissionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面按钮/动作与权限表';

ALTER TABLE `sys_user` ADD COLUMN `email` varchar(100) NOT NULL COMMENT '用户邮箱' AFTER `phone`