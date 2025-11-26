package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.InventoryDetailMapper;
import com.qzh.backend.mapper.InventoryMapper;
import com.qzh.backend.model.dto.product.InventoryDetailQueryDTO;
import com.qzh.backend.model.entity.Inventory;
import com.qzh.backend.model.entity.InventoryDetail;
import com.qzh.backend.model.vo.InventoryDetailVO;
import com.qzh.backend.service.InventoryDetailService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryDetailServiceImpl extends ServiceImpl<InventoryDetailMapper, InventoryDetail> implements InventoryDetailService {

    private final InventoryMapper inventoryMapper;

    @Override
    public Page<InventoryDetailVO> listInventoryDetailsVO(InventoryDetailQueryDTO queryDTO) {
        // 构建分页对象
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        Page<InventoryDetail> page = new Page<>(current, size);
        Page<InventoryDetail> detailPage = this.page(page, InventoryDetailQueryDTO.getQueryWrapper(queryDTO));
        // 如果没有数据，直接返回空的 VO 分页对象
        if (CollectionUtils.isEmpty(detailPage.getRecords())) {
            return new Page<>(detailPage.getCurrent(), detailPage.getSize(), detailPage.getTotal());
        }
        List<String> productWarehouseKeys = detailPage.getRecords().stream()
                .map(detail -> String.format("%d_%d", detail.getProductId(), detail.getWarehouseId()))
                .distinct()
                .toList();
        QueryWrapper<Inventory> inventoryQueryWrapper = Wrappers.query(Inventory.class);
        inventoryQueryWrapper.in("CONCAT(productId, '_', warehouseId)", productWarehouseKeys);
        List<Inventory> inventoryList = inventoryMapper.selectList(inventoryQueryWrapper);
        Map<String, Inventory> inventoryMap = inventoryList.stream()
                .collect(Collectors.toMap(
                        inv -> String.format("%d_%d", inv.getProductId(), inv.getWarehouseId()),
                        inv -> inv,
                        (oldVal, newVal) -> oldVal
                ));
        List<InventoryDetailVO> voList = detailPage.getRecords().stream()
                .map(detail -> {
                    InventoryDetailVO vo = new InventoryDetailVO();
                    BeanUtils.copyProperties(detail, vo);
                    // 补充仓库ID
                    vo.setWarehouseId(detail.getWarehouseId());
                    // 构建「商品ID+仓库ID」key，精准匹配库存信息
                    String key = String.format("%d_%d", detail.getProductId(), detail.getWarehouseId());
                    Inventory inventory = inventoryMap.get(key);
                    // 填充该商品在对应仓库的信息
                    if (inventory != null) {
                        vo.setProductName(inventory.getProductName());
                        vo.setProductDescription(inventory.getProductDescription());
                        vo.setProductUrl(inventory.getProductUrl());
                        vo.setProductPrice(inventory.getProductPrice());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        // 构建并返回 VO 分页对象
        Page<InventoryDetailVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(detailPage.getTotal());
        voPage.setSize(detailPage.getSize());
        voPage.setCurrent(detailPage.getCurrent());
        voPage.setPages(detailPage.getPages());
        return voPage;
    }

    @Override
    public InventoryDetailVO getInventoryDetailVOById(Long id) {
        // 根据ID查询原始的库存明细记录
        InventoryDetail detail = this.getById(id);
        if (detail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "库存明细记录不存在");
        }
        return convertToVO(detail);
    }

    /**
     * 将单个 InventoryDetail 转换为 InventoryDetailVO
     */
    private InventoryDetailVO convertToVO(InventoryDetail detail) {
        InventoryDetailVO vo = new InventoryDetailVO();
        BeanUtils.copyProperties(detail, vo);
        // 补充仓库ID
        vo.setWarehouseId(detail.getWarehouseId());
        LambdaQueryWrapper<Inventory> inventoryQueryWrapper = Wrappers.lambdaQuery();
        inventoryQueryWrapper.eq(Inventory::getProductId, detail.getProductId())
                .eq(Inventory::getWarehouseId, detail.getWarehouseId());
        Inventory inventory = inventoryMapper.selectOne(inventoryQueryWrapper);
        if (inventory != null) {
            vo.setProductName(inventory.getProductName());
            vo.setProductDescription(inventory.getProductDescription());
            vo.setProductUrl(inventory.getProductUrl());
            vo.setProductPrice(inventory.getProductPrice());
        }
        return vo;
    }

}