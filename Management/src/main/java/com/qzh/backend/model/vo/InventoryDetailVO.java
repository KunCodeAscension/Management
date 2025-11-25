package com.qzh.backend.model.vo;

import com.qzh.backend.model.entity.InventoryDetail;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存明细VO，包含从Inventory表关联的商品信息
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InventoryDetailVO extends InventoryDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品描述
     */
    private String productDescription;

    /**
     * 商品图片URL
     */
    private String productUrl;

    /**
     * 商品售价
     */
    private BigDecimal productPrice;
}