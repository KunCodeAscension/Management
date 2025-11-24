CREATE TABLE `sys_product` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                           `name` VARCHAR(128) NOT NULL COMMENT '商品名称',
                           `description` varchar(200) DEFAULT NULL COMMENT '商品描述',
                           `url` varchar(512) DEFAULT NULL COMMENT '商品图片',
                           `supplierId` BIGINT NOT NULL COMMENT '供应商账号ID',
                           `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
                           `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0-下架`，1-上架）',
                           `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE `sys_purchase_order` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '采购订单ID',
                                      `storeId` BIGINT NOT NULL COMMENT '门店ID',
                                      `supplierId` BIGINT NOT NULL COMMENT '供应商ID',
                                      `productId` BIGINT NOT NULL COMMENT '商品ID',
                                      `productName` VARCHAR(128) NOT NULL COMMENT '商品名称',
                                      `productUrl` varchar(512) DEFAULT NULL COMMENT '商品图片',
                                      `productDescription` varchar(200) DEFAULT NULL COMMENT '商品描述',
                                      `productPrice` DECIMAL(10,2) NOT NULL COMMENT '采购单价',
                                      `productQuantity` INT NOT NULL COMMENT '采购数量',
                                      `totalAmount` DECIMAL(12,2) NOT NULL COMMENT '明细总金额',
                                      `status` TINYINT NOT NULL COMMENT '状态（0-待发货，1-已发货，2-已入库）',
                                      `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0-手动发起，1-阈值触发）',
                                      `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                      PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单表';

CREATE TABLE `sys_purchase_return` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '退货单ID',
                                       `productId` BIGINT NOT NULL COMMENT '商品ID',
                                       `productName` VARCHAR(128) NOT NULL COMMENT '商品名称',
                                       `productUrl` varchar(512) DEFAULT NULL COMMENT '商品图片',
                                       `productDescription` varchar(200) DEFAULT NULL COMMENT '商品描述',
                                       `productPrice` DECIMAL(10,2) NOT NULL COMMENT '退货单价',
                                       `productQuantity` INT NOT NULL COMMENT '退货数量',
                                       `storeId` BIGINT NOT NULL COMMENT '门店ID',
                                       `supplierId` BIGINT NOT NULL COMMENT '供应商ID',
                                       `totalAmount` DECIMAL(12,2) NOT NULL COMMENT '退货总金额',
                                       `status` TINYINT NOT NULL COMMENT '状态（0-未完成，1-已完成）',
                                       `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购退货表';

CREATE TABLE `sys_amount_order` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '金额订单ID',
                                    `orderId` VARCHAR(64) NOT NULL COMMENT '订单编号',
                                    `type` TINYINT NOT NULL COMMENT '类型（0-采购，1-采退，2-销售，3-销退）',
                                    `payerId` BIGINT NOT NULL COMMENT '付款人ID（门店ID）',
                                    `payeeId` BIGINT NOT NULL COMMENT '收款人ID（供应商ID）',
                                    `amount` DECIMAL(12,2) NOT NULL COMMENT '金额',
                                    `status` TINYINT NOT NULL COMMENT '状态（0-待支付，1-已支付，2-已取消）',
                                    `payType` VARCHAR(32) DEFAULT '' COMMENT '支付方式（alipay-支付宝）',
                                    `tradeNo` VARCHAR(64) DEFAULT '' COMMENT '第三方支付流水号',
                                    `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='金额订单表';

CREATE TABLE `sys_inventory` (
                                 `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '库存ID',
                                 `productId` BIGINT NOT NULL COMMENT '商品ID',
                                 `productName` VARCHAR(128) NOT NULL COMMENT '商品名称',
                                 `productDescription` varchar(200) DEFAULT NULL COMMENT '商品描述',
                                 `productUrl` varchar(512) DEFAULT NULL COMMENT '商品图片',
                                 `productPrice` DECIMAL(10,2) NOT NULL COMMENT '出售单价',
                                 `storeId` BIGINT NOT NULL COMMENT '门店ID',
                                 `warningThreshold` INT NOT NULL DEFAULT 10 COMMENT '预警阈值',
                                 `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                 `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE `sys_inventory_detail` (
                                        `productId` BIGINT NOT NULL COMMENT '商品ID',
                                        `orderId` VARCHAR(64) NOT NULL COMMENT '订单Id',
                                        `type` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0-出库，1-入库）',
                                        `orderType` TINYINT NOT NULL DEFAULT 0 COMMENT '类型（0-采购，1-采退，2-销售，3-销退）',
                                        `productQuantity` INT NOT NULL COMMENT '入库或出库数量',
                                        `createBy` bigint DEFAULT NULL COMMENT '创建人ID',
                                        `createTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        KEY `idx_orderId_productId` (`productId`,`orderId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';