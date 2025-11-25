package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.dto.product.InventoryUpdateDTO;
import com.qzh.backend.model.entity.Inventory;
import com.qzh.backend.model.vo.InventoryVO;
import jakarta.servlet.http.HttpServletRequest;

public interface InventoryService extends IService<Inventory> {

    /**
     * 根据采购订单进行入库操作
     * @param purchaseOrderId 采购订单ID
     */
    void stockIn(Long purchaseOrderId, HttpServletRequest request);

    Page<InventoryVO> listInventoriesWithQuantity(InventoryQueryDTO queryDTO);

    InventoryVO getInventoryVOById(Long id);

    void updateInventory(InventoryUpdateDTO updateDTO);
}