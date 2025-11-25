package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 门店操作员设置为入库
     */
    @PostMapping("/stock-in/{id}")
    public BaseResponse<Void> stockIn(@PathVariable("id") Long PucchaseOrderId, HttpServletRequest request) {
        inventoryService.stockIn(PucchaseOrderId,request);
        return ResultUtils.success(null);
    }

    /**
     * 分页查询入库信息
     */
    @GetMapping("/list")
    public BaseResponse<Page<InventoryVO>> listInventories(InventoryQueryDTO queryDTO) {
        Page<InventoryVO> inventoryVOPage = inventoryService.listInventoriesWithQuantity(queryDTO);
        return ResultUtils.success(inventoryVOPage);
    }

}
