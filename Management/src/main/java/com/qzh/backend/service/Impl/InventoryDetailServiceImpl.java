package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
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

        // 提取所有的 productId，用于批量查询 Inventory 表
        List<Long> productIds = detailPage.getRecords().stream()
                .map(InventoryDetail::getProductId)
                .distinct()
                .collect(Collectors.toList());
        List<Inventory> inventoryList = inventoryMapper.selectBatchIds(productIds);
        Map<Long, Inventory> inventoryMap = inventoryList.stream()
                .collect(Collectors.toMap(Inventory::getProductId, inventory -> inventory));
        // 将 InventoryDetail 列表转换为 InventoryDetailVO 列表
        List<InventoryDetailVO> voList = detailPage.getRecords().stream()
                .map(detail -> {
                    InventoryDetailVO vo = new InventoryDetailVO();
                    BeanUtils.copyProperties(detail, vo);
                    // 根据 productId 从 Map 中获取对应的 Inventory 对象
                    Inventory inventory = inventoryMap.get(detail.getProductId());
                    // 如果找到了对应的商品信息，则设置到 VO 中
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
        // 根据 productId 查询商品信息
        Inventory inventory = inventoryMapper.selectById(detail.getProductId());
        if (inventory != null) {
            vo.setProductName(inventory.getProductName());
            vo.setProductDescription(inventory.getProductDescription());
            vo.setProductUrl(inventory.getProductUrl());
            vo.setProductPrice(inventory.getProductPrice());
        }
        return vo;
    }

}