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
 * 采购退货表实体
 * 对应数据库表：sys_purchase_return
 */
@Data
@TableName("sys_purchase_return")
public class PurchaseReturn implements Serializable {

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 退货单ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

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
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 商品描述
     */
    private String productDescription;

    /**
     * 退货单价
     */
    private BigDecimal productPrice;

    /**
     * 退货数量
     */
    private Integer productQuantity;

    /**
     * 门店ID
     */
    private Long storeId;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 退货总金额
     */
    private BigDecimal totalAmount;

    /**
     * 状态（0-未完成，1-已完成）
     */
    private Integer status;

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