package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购订单表实体
 * 对应数据库表：sys_purchase_order
 */
@Data
@TableName("sys_purchase_order")
public class PurchaseOrder implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 采购订单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productUrl;

    /**
     * 商品描述
     */
    private String productDescription;

    /**
     * 采购单价
     */
    private BigDecimal productPrice;

    /**
     * 采购数量
     */
    private Integer productQuantity;

    /**
     * 明细总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态（0-待发货，1-已发货，2-已入库）
     */
    private Integer status;

    /**
     * 类型（0-手动发起，1-阈值触发）
     */
    private Integer type;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人ID
     */
    private Long createBy;
}