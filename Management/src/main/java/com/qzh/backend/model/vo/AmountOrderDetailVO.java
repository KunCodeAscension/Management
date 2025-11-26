package com.qzh.backend.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AmountOrderDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 门店ID 1
     */
    private Long storeId;

    /**
     * 门店名 1
     */
    private String storeName;

    /**
     * 商品ID 1
     */
    private Long productId;

    /**
     * 商品名称 1
     */
    private String productName;

    /**
     * 商品图片 1
     */
    private String productUrl;

    /**
     * 商品描述 1
     */
    private String productDescription;

    /**
     * 采购单价 1
     */
    private BigDecimal productPrice;

    /**
     * 采购数量 1
     */
    private Integer productQuantity;

    /**
     * 状态（0-待发货，1-已发货，2-已入库）
     */
    private Integer PurchaseOrderStatus;

    /**
     * 金额订单ID 1
     */
    private Long AmountOrderId;

    /**
     * 采购或者采退的订单ID
     */
    private Long OrderId;

    /**
     * 付款人ID 1
     */
    private Long payerId;

    /**
     * 金额 1
     */
    private BigDecimal amount;

    /**
     * 状态（0-待支付，1-已支付，2-已取消） 1
     */
    private Integer AmountOrderStatus;

    /**
     * 支付方式（alipay-支付宝） 1
     */
    private Integer payType;

    /**
     * 第三方支付流水号 1
     */
    private String tradeNo;
}
