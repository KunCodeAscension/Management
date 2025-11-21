CREATE TABLE `sys_operation_log` (
                                     `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志唯一标识',
                                     `operationTime` DATETIME NOT NULL COMMENT '操作执行时间',
                                     `operatorId` BIGINT NOT NULL COMMENT '操作人ID',
                                     `operatorIp` VARCHAR(64) NOT NULL COMMENT '操作人IP地址（支持IPv4/IPv6）',
                                     `operatorDevice` VARCHAR(255) DEFAULT '' COMMENT '操作设备/浏览器信息',
                                     `systemModule` VARCHAR(64) NOT NULL COMMENT '操作所属系统模块（如用户管理、订单审核）',
                                     `operationContent` TEXT COMMENT '操作详细内容（JSON格式，记录修改前后数据）',
                                     `operationResult` VARCHAR(16) NOT NULL COMMENT '操作结果（SUCCESS-成功/FAIL-失败/PARTIAL-部分成功）',
                                     `errorMsg` TEXT COMMENT '错误信息（操作失败时记录）',
                                     PRIMARY KEY (`id`),
                                     KEY `idx_operator_id` (`operatorId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';