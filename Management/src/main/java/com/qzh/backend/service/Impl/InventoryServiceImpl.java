package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.InventoryMapper;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.entity.Inventory;
import com.qzh.backend.model.entity.InventoryDetail;
import com.qzh.backend.model.entity.PurchaseOrder;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.enums.InventoryDetailTypeEnum;
import com.qzh.backend.model.enums.OrderTypeEnum;
import com.qzh.backend.model.enums.PurchaseOrderStatusEnum;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.InventoryDetailService;
import com.qzh.backend.service.InventoryService;
import com.qzh.backend.service.PurchaseOrderService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final PurchaseOrderService purchaseOrderService;

    private final GetLoginUserUtil getLoginUserUtil;

    private final InventoryDetailService inventoryDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(Long purchaseOrderId, HttpServletRequest request) {
        // 查询采购订单并校验状态
        PurchaseOrder purchaseOrder = purchaseOrderService.getById(purchaseOrderId);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "采购订单不存在");
        }
        if (!purchaseOrder.getStatus().equals(PurchaseOrderStatusEnum.SHIPPED.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有已发货的订单才能进行入库操作");
        }
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 更新采购订单状态为“已入库”
        purchaseOrder.setStatus(PurchaseOrderStatusEnum.STORED.getValue());
        boolean b = purchaseOrderService.updateById(purchaseOrder);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"采购信息更新失败");

        InventoryDetail detail = new InventoryDetail();
        detail.setProductId(purchaseOrder.getProductId());
        detail.setOrderId(purchaseOrderId);
        detail.setType(InventoryDetailTypeEnum.TAKEIN.getValue());    // 设置类型为入库
        detail.setOrderType(OrderTypeEnum.PURCHASE.getValue());     // 设置订单类型为采购
        detail.setProductQuantity(purchaseOrder.getProductQuantity());  // 设置数量
        detail.setCreateBy(loginUser.getId());
        boolean save = inventoryDetailService.save(detail);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"库存明细新增失败");

        // 更新库存表
        LambdaQueryWrapper<Inventory> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(Inventory::getProductId, purchaseOrder.getProductId())
                .eq(Inventory::getStoreId, purchaseOrder.getStoreId());
        Inventory inventory = this.getOne(queryWrapper);

        if (inventory == null) {
            inventory = new Inventory();
            inventory.setProductId(purchaseOrder.getProductId());
            inventory.setProductName(purchaseOrder.getProductName());
            inventory.setProductDescription(purchaseOrder.getProductDescription());
            inventory.setProductUrl(purchaseOrder.getProductUrl());
            // 出售价格默认比进货价多1
            inventory.setProductPrice(purchaseOrder.getProductPrice().add(new BigDecimal(1)));
            inventory.setStoreId(purchaseOrder.getStoreId());
            // 设置一个默认预警阈值
            inventory.setWarningThreshold(10);
            inventory.setCreateBy(purchaseOrder.getCreateBy());
            boolean a = this.save(inventory);
            ThrowUtils.throwIf(!a,ErrorCode.SYSTEM_ERROR,"库存新增信息失败");
        }
        // 如果库存记录已存在，则更新数量不做任何处理
    }

    @Override
    public Page<InventoryVO> listInventoriesWithQuantity(InventoryQueryDTO queryDTO) {
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        // 分页查询库存主表
        Page<Inventory> inventoryPage = this.page(
                new Page<>(current, size),
                InventoryQueryDTO.getQueryWrapper(queryDTO)
        );
        // 若库存主表为空，直接返回空分页
        if (CollectionUtils.isEmpty(inventoryPage.getRecords())) {
            return new Page<>(current, size, 0);
        }
        // 提取所有 productId + storeId 组合（唯一标识一个库存记录）
        List<Map<String, Long>> productStoreCombinations = inventoryPage.getRecords().stream()
                .map(inventory -> {
                    Map<String, Long> map = new HashMap<>();
                    map.put("productId", inventory.getProductId());
                    map.put("storeId", inventory.getStoreId());
                    return map;
                })
                .toList();
        // 批量查询库存明细表中这些商品的所有变动记录
        List<InventoryDetail> detailList = inventoryDetailService.list(
                Wrappers.lambdaQuery(InventoryDetail.class)
                        .in(detail -> productStoreCombinations.stream()
                                .anyMatch(comb -> comb.get("productId").equals(detail.getProductId())
                                        && comb.get("storeId").equals(detail.getOrderId()))
                        )
        );

        // productId + storeId 分组，计算每个组合的库存数量
        Map<String, Integer> quantityMap = calculateQuantityByProductStore(detailList);
        // 组装VO（填充库存数量）
        List<InventoryVO> voList = inventoryPage.getRecords().stream()
                .map(inventory -> {
                    InventoryVO vo = new InventoryVO(inventory);
                    // 构建 key：productId_storeId
                    String key = inventory.getProductId() + "_" + inventory.getStoreId();
                    // 填充数量
                    vo.setQuantity(quantityMap.getOrDefault(key, null));
                    return vo;
                })
                .collect(Collectors.toList());
        // 构建并返回VO分页对象
        Page<InventoryVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(inventoryPage.getTotal());
        voPage.setSize(inventoryPage.getSize());
        voPage.setCurrent(inventoryPage.getCurrent());
        voPage.setPages(inventoryPage.getPages());
        return voPage;
    }

    private Map<String, Integer> calculateQuantityByProductStore(List<InventoryDetail> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyMap();
        }
        // 分组计算：key = productId_storeId，value = 总数量
        return detailList.stream()
                .collect(Collectors.groupingBy(
                        // 分组key：productId + "_" + storeId（假设 orderId 存储门店ID）
                        detail -> detail.getProductId() + "_" + detail.getOrderId(),
                        Collectors.summingInt(detail -> {
                            Integer quantity = detail.getProductQuantity();
                            switch (detail.getOrderType()) {
                                case 0: // 采购：+数量
                                    return quantity;
                                case 1: // 采退：-数量
                                    return -quantity;
                                case 2: // 销售：-数量
                                    return -quantity;
                                case 3: // 销退：+数量
                                    return quantity;
                                default:
                                    return 0;
                            }
                        })
                ));
    }
}