package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.warehouse.WarehouseAddDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseQueryDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseUpdateDTO;
import com.qzh.backend.model.entity.Warehouse;
import jakarta.servlet.http.HttpServletRequest;

public interface WarehouseService extends IService<Warehouse> {
    /**
     * 新增仓库
     * @param addDTO 新增仓库信息
     * @param request HttpServletRequest
     * @return 新增的仓库对象
     */
    Long addWarehouse(WarehouseAddDTO addDTO, HttpServletRequest request);

    /**
     * 根据 ID 查询仓库
     * @param id 仓库 ID
     * @return 仓库对象
     */
    Warehouse getWarehouseById(Long id);

    /**
     * 更新仓库信息
     * @param updateDTO 更新仓库信息
     * @return 是否更新成功
     */
    boolean updateWarehouse(WarehouseUpdateDTO updateDTO);

    /**
     * 分页查询仓库列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<Warehouse> getWarehousePage(WarehouseQueryDTO queryDTO);
}
