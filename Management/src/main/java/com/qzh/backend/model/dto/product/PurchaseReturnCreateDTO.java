package com.qzh.backend.model.dto.product;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 简化的采退创建请求DTO
 */
@Data
public class PurchaseReturnCreateDTO {
    @NotNull(message = "产品ID不能为空")
    private Long ProductId;

    @NotNull(message = "采退数量不能为空")
    @Min(value = 1, message = "采退数量必须大于0")
    private Integer returnQuantity;

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;
}