package com.qzh.backend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 采购订单响应VO（含关联的金额订单数据）
 */
@Data
public class PurchaseOrderListVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 采购订单ID
     */
    private Long PurchaseOrderId;

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
     * 状态（0-待发货，1-已发货，2-已入库）
     */
    private Integer PurchaseOrderStatus;

    /**
     * 类型（0-手动发起，1-阈值触发）
     */
    private Integer PurchaseOrderType;

    /**
     * 金额订单ID
     */
    private Long AmountOrderId;

    /**
     * 订单编号
     */
    private Long orderId;

    /**
     * 类型（0-采购，1-采退，2-销售，3-销退）
     */
    private Integer AmountOrderType;

    /**
     * 付款人ID（门店ID）
     */
    private Long payerId;

    /**
     * 收款人ID（供应商ID）
     */
    private Long payeeId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 状态（0-待支付，1-已支付，2-已取消）
     */
    private Integer AmountOrderStatus;

    /**
     * 支付方式（alipay-支付宝）
     */
    private Integer payType;

    /**
     * 第三方支付流水号
     */
    private String tradeNo;

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