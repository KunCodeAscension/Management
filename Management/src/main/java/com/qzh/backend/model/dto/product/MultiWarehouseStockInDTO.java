package com.qzh.backend.model.dto.product;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 多仓库入库请求DTO
 */
@Data
public class MultiWarehouseStockInDTO {

    /**
     * 采购订单ID
     */
    @NotNull(message = "采购订单ID不能为空")
    private Long purchaseOrderId;

    /**
     * 多仓库入库明细列表
     */
    @NotNull(message = "入库明细不能为空")
    private List<WarehouseStockInItem> items;

    /**
     * 单个仓库的入库信息
     */
    @Data
    public static class WarehouseStockInItem {
        /**
         * 仓库ID
         */
        @NotNull(message = "仓库ID不能为空")
        private Long warehouseId;

        /**
         * 入库数量
         */
        @NotNull(message = "入库数量不能为空")
        @Min(value = 1, message = "入库数量必须大于0")
        private Integer quantity;
    }
}