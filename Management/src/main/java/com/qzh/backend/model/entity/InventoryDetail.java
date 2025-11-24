package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 库存明细表实体
 * 对应数据库表：sys_inventory_detail
 */
@Data
@TableName("sys_inventory_detail")
public class InventoryDetail implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 订单Id
     */
    private String orderId;

    /**
     * 类型（0-出库，1-入库）
     */
    private Integer type;

    /**
     * 类型（0-采购，1-采退，2-销售，3-销退）
     */
    private Integer orderType;

    /**
     * 入库或出库数量
     */
    private Integer productQuantity;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}