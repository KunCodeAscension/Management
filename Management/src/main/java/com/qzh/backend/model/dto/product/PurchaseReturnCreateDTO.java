package com.qzh.backend.model.dto.product;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 简化的采退创建请求DTO
 */
@Data
public class PurchaseReturnCreateDTO {

    /**
     * 采购订单ID (对应库存明细表中的 orderId)
     */
    @NotNull(message = "采购订单ID不能为空")
    private Long orderId;

    /**
     * 采退数量
     */
    @NotNull(message = "采退数量不能为空")
    @Min(value = 1, message = "采退数量必须大于0")
    private Integer returnQuantity;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;
}