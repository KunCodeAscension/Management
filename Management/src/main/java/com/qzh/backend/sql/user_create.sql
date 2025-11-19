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