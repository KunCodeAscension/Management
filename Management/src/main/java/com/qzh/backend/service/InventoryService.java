package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.dto.product.InventoryUpdateDTO;
import com.qzh.backend.model.dto.product.MultiWarehouseStockInDTO;
import com.qzh.backend.model.entity.Inventory;
import com.qzh.backend.model.vo.InventoryVO;
import jakarta.servlet.http.HttpServletRequest;

public interface InventoryService extends IService<Inventory> {

    Page<InventoryVO> listInventoriesWithQuantity(InventoryQueryDTO queryDTO);

    InventoryVO getInventoryVOById(Long id);

    void updateInventory(InventoryUpdateDTO updateDTO);

    void stockInNew(MultiWarehouseStockInDTO stockInDTO, HttpServletRequest request);
}