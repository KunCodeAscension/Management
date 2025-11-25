package com.qzh.backend.model.dto.product;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class InventoryUpdateDTO {

    /**
     * 库存ID (必须)
     */
    @NotNull(message = "库存ID不能为空")
    private Long id;

    /**
     * 商品名称
     */
    @NotNull(message = "商品名称不能为空")
    private String productName;

    /**
     * 商品描述
     */
    @NotNull(message = "商品描述不能为空")
    private String productDescription;

    /**
     * 商品图片URL
     */
    @NotNull(message = "商品图片不能为空")
    private String productUrl;

    /**
     * 出售单价
     */
    @NotNull(message = "商品出售单价")
    private BigDecimal productPrice;

    /**
     * 预警阈值
     */
    @NotNull(message = "预警值不能为空")
    private Integer warningThreshold;
}