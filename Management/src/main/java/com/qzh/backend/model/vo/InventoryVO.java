package com.qzh.backend.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.qzh.backend.model.entity.Inventory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 库存VO（含实时计算的库存数量）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InventoryVO extends Inventory implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public InventoryVO(Inventory inventory) {
        // 继承父类字段
        this.setId(inventory.getId());
        this.setProductId(inventory.getProductId());
        this.setProductName(inventory.getProductName());
        this.setProductDescription(inventory.getProductDescription());
        this.setProductUrl(inventory.getProductUrl());
        this.setProductPrice(inventory.getProductPrice());
        this.setStoreId(inventory.getStoreId());
        this.setWarehouseId(inventory.getWarehouseId());
        this.setWarningThreshold(inventory.getWarningThreshold());
        this.setCreateBy(inventory.getCreateBy());
        this.setCreateTime(inventory.getCreateTime());
        this.setUpdateTime(inventory.getUpdateTime());
    }

    private Integer quantity;

}