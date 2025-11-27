package com.qzh.backend.model.dto.product;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class SaleOrderCreateDTO {
    @NotNull(message = "库存ID不能为空")
    private Long inventoryId; // 库存ID

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    private Integer buyQuantity; // 购买数量
}