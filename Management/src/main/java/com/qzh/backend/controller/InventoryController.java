package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.dto.product.InventoryUpdateDTO;
import com.qzh.backend.model.dto.product.MultiWarehouseStockInDTO;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 分页查询入库信息
     */
    @GetMapping("/list")
    public BaseResponse<Page<InventoryVO>> listInventories(InventoryQueryDTO queryDTO) {
        Page<InventoryVO> inventoryVOPage = inventoryService.listInventoriesWithQuantity(queryDTO);
        return ResultUtils.success(inventoryVOPage);
    }

    /**
     * 根据库存ID获取库存信息
     */
    @GetMapping("/{id}")
    public BaseResponse<InventoryVO> getInventoryById(@PathVariable Long id) {
        InventoryVO inventoryVO = inventoryService.getInventoryVOById(id);
        return ResultUtils.success(inventoryVO);
    }

    /**
     * 根据库存ID编辑库存中商品信息
     */
    @PostMapping("/update")
    public BaseResponse<Void> updateInventory(@Valid @RequestBody InventoryUpdateDTO updateDTO) {
        inventoryService.updateInventory(updateDTO);
        return ResultUtils.success(null);
    }


    @PostMapping("/stock-in")
    public BaseResponse<Void> stockIn(@Valid @RequestBody MultiWarehouseStockInDTO stockInDTO, HttpServletRequest request) {
        inventoryService.stockInNew(stockInDTO, request);
        return ResultUtils.success(null);
    }

    @PostMapping("/sale-order")
    public BaseResponse<Void>  saleOrder(@RequestBody Long saleOrderId,HttpServletRequest request) {
        inventoryService.saleOrder(saleOrderId, request);
        return ResultUtils.success(null);
    }

    @PostMapping("/sale-return/confirm")
    public BaseResponse<Void> confirmSaleReturn(@RequestBody Long saleReturnId, HttpServletRequest request) {
        inventoryService.confirmSaleReturn(saleReturnId, request);
        return ResultUtils.success(null);
    }

}
