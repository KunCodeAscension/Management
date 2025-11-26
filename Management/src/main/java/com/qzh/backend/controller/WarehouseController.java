package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.warehouse.WarehouseAddDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseQueryDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseUpdateDTO;
import com.qzh.backend.model.entity.Warehouse;
import com.qzh.backend.service.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 仓库管理 Controller
 */
@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * 新增仓库
     */
    @PostMapping("/add")
    public BaseResponse<Long> addWarehouse(@Valid @RequestBody WarehouseAddDTO addDTO, HttpServletRequest request) {
        Long id = warehouseService.addWarehouse(addDTO, request);
        return ResultUtils.success(id);
    }

    /**
     * 根据 ID 查询仓库
     */
    @GetMapping("/get/{id}")
    public BaseResponse<Warehouse> getWarehouseById(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        return ResultUtils.success(warehouse);
    }

    /**
     * 更新仓库信息
     */
    @PutMapping("/update")
    public BaseResponse<Boolean> updateWarehouse(@Valid @RequestBody WarehouseUpdateDTO updateDTO) {
        boolean success = warehouseService.updateWarehouse(updateDTO);
        return ResultUtils.success(success);
    }

    /**
     * 分页查询仓库列表
     */
    @GetMapping("/page")
    public BaseResponse<Page<Warehouse>> getWarehousePage(@Valid WarehouseQueryDTO queryDTO) {
        Page<Warehouse> resultPage = warehouseService.getWarehousePage(queryDTO);
        return ResultUtils.success(resultPage);
    }
}