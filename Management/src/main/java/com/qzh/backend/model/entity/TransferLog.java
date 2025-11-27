package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_transfer_log")
public class TransferLog implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 调拨订单ID
     */
    private Long transferOrderId;

    /**
     * 发货仓库ID
     */
    private Long sourceWarehouseId;

    /**
     * 收货仓库ID
     */
    private Long targetWarehouseId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 调拨数量
     */
    private Integer transferQuantity;

    /**
     * 调拨备注（如自动补货调拨、库存不足调拨等）
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}