CREATE TABLE `sys_store` (
                             `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '门店ID（主键）',
                             `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `store_name` VARCHAR(64) NOT NULL COMMENT '门店名称（如"XX市XX区旗舰店"）',
                             `manager_id` BIGINT NOT NULL COMMENT '店长ID（关联sys_user.id，对应"门店店长"角色）',
                             `contact_name` VARCHAR(32) NOT NULL COMMENT '联系人（门店负责人）',
                             `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系电话',
                             `contact_email` VARCHAR(64) DEFAULT '' COMMENT '联系邮箱',
                             `address` VARCHAR(255) NOT NULL COMMENT '详细地址',
                             `longitude` DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
                             `latitude` DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
                             PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统门店表';