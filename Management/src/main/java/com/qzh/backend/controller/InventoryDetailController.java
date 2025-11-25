package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.InventoryDetailQueryDTO;
import com.qzh.backend.model.vo.InventoryDetailVO;
import com.qzh.backend.service.InventoryDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory/detail")
@RequiredArgsConstructor
public class InventoryDetailController {

    private final InventoryDetailService inventoryDetailService;

    /**
     * 分页查询库存明细
     */
    @GetMapping("/list")
    public BaseResponse<Page<InventoryDetailVO>> listInventoryDetails(InventoryDetailQueryDTO queryDTO) {
        Page<InventoryDetailVO> page = inventoryDetailService.listInventoryDetailsVO(queryDTO);
        return ResultUtils.success(page);
    }

    @GetMapping("/{id}")
    public BaseResponse<InventoryDetailVO> getInventoryDetailById(@PathVariable Long id) {
        InventoryDetailVO detailVO = inventoryDetailService.getInventoryDetailVOById(id);
        return ResultUtils.success(detailVO);
    }
}