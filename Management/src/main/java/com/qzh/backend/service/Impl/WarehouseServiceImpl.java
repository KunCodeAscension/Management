package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.WarehouseMapper;
import com.qzh.backend.model.dto.warehouse.WarehouseAddDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseQueryDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseUpdateDTO;
import com.qzh.backend.model.entity.Warehouse;
import com.qzh.backend.service.WarehouseService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private final GetLoginUserUtil getLoginUserUtil;

    @Override
    public Long addWarehouse(WarehouseAddDTO addDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(addDTO == null,ErrorCode.PARAMS_ERROR);
        // 1. 转换 DTO 为实体
        Warehouse warehouse = new Warehouse();
        warehouse.setName(addDTO.getName());
        warehouse.setAddress(addDTO.getAddress());
        warehouse.setDescription(addDTO.getDescription());
        warehouse.setCreateBy(getLoginUserUtil.getLoginUser(request).getId());
        // 2. 保存到数据库
        boolean saveSuccess = this.save(warehouse);
        if (!saveSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "新增仓库失败");
        }
        return warehouse.getId();
    }

    @Override
    public Warehouse getWarehouseById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0,ErrorCode.PARAMS_ERROR);
        return this.getById(id);
    }

    @Override
    public boolean updateWarehouse(WarehouseUpdateDTO updateDTO) {
        if (updateDTO.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "仓库 ID 不能为空");
        }
        Warehouse existingWarehouse = this.getById(updateDTO.getId());
        if (existingWarehouse == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "仓库不存在");
        }
        existingWarehouse.setName(updateDTO.getName());
        existingWarehouse.setAddress(updateDTO.getAddress());
        existingWarehouse.setDescription(updateDTO.getDescription());
        return this.updateById(existingWarehouse);
    }

    @Override
    public Page<Warehouse> getWarehousePage(WarehouseQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size <= 0 || size > 20,ErrorCode.PARAMS_ERROR);
        Page<Warehouse> page = new Page<>(current, size);
        QueryWrapper<Warehouse> queryWrapper = WarehouseQueryDTO.getQueryWrapper(queryDTO);
        return this.page(page, queryWrapper);
    }
}
