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
import com.qzh.backend.model.dto.product.InventoryUpdateDTO;
import com.qzh.backend.model.dto.product.MultiWarehouseStockInDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.*;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.*;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private final PurchaseOrderService purchaseOrderService;

    private final GetLoginUserUtil getLoginUserUtil;

    private final InventoryDetailService inventoryDetailService;

    private final WarehouseService warehouseService;

    private final SaleOrderService saleOrderService;

    private final AmountOrderService amountOrderService;

    private final SaleReturnService saleReturnService;

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
        List<Inventory> inventoryList = inventoryPage.getRecords();
        List<String> productWarehouseKeys = inventoryList.stream()
                .map(inv -> String.format("%d_%d", inv.getProductId(), inv.getWarehouseId()))
                .distinct()
                .toList();
        // 查询库存明细：按「商品ID+仓库ID」过滤
        List<InventoryDetail> detailList = inventoryDetailService.list(
                Wrappers.query(InventoryDetail.class)
                        // 核心：用 SQL CONCAT 函数拼接组合键，匹配 productWarehouseKeys
                        .in("CONCAT(productId, '_', warehouseId)", productWarehouseKeys)
        );
        Map<String, Integer> quantityMap = calculateQuantityByProductStore(detailList);
        List<InventoryVO> voList = inventoryList.stream()
                .map(inventory -> {
                    InventoryVO vo = new InventoryVO(inventory);
                    String key = String.format("%d_%d", inventory.getProductId(), inventory.getWarehouseId());
                    vo.setQuantity(quantityMap.getOrDefault(key, 0));
                    return vo;
                })
                .toList();
        Page<InventoryVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(inventoryPage.getTotal());
        voPage.setSize(inventoryPage.getSize());
        voPage.setCurrent(inventoryPage.getCurrent());
        voPage.setPages(inventoryPage.getPages());
        return voPage;
    }

    @Override
    public InventoryVO getInventoryVOById(Long id) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        // 根据ID查询库存主表记录
        Inventory inventory = this.getById(id);
        if (inventory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "库存记录不存在");
        }
        Long productId = inventory.getProductId();
        Long warehouseId = inventory.getWarehouseId(); // 关键：获取该库存记录对应的仓库ID
        LambdaQueryWrapper<InventoryDetail> detailQueryWrapper = Wrappers.lambdaQuery();
        detailQueryWrapper.eq(InventoryDetail::getProductId, productId)
                .eq(InventoryDetail::getWarehouseId, warehouseId); // 增加仓库ID筛选
        List<InventoryDetail> detailList = inventoryDetailService.list(detailQueryWrapper);
        // 计算库存数量
        Map<String, Integer> quantityMap = calculateQuantityByProductStore(detailList);
        // 组装VO对象：使用商品ID+仓库ID构建key，获取精准数量
        InventoryVO vo = new InventoryVO(inventory);
        String key = String.format("%d_%d", productId, warehouseId); // 二维度key
        vo.setQuantity(quantityMap.getOrDefault(key, 0)); // 无数据时默认为0
        return vo;
    }

    @Override
    public void updateInventory(InventoryUpdateDTO updateDTO) {
        Inventory inventory = this.getById(updateDTO.getId());
        if (inventory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "库存记录不存在");
        }
        inventory.setProductName(updateDTO.getProductName());
        inventory.setProductDescription(updateDTO.getProductDescription());
        inventory.setProductUrl(updateDTO.getProductUrl());
        inventory.setProductPrice(updateDTO.getProductPrice());
        inventory.setWarningThreshold(updateDTO.getWarningThreshold());
        boolean updateSuccess = this.updateById(inventory);
        if (!updateSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新库存信息失败");
        }
    }

    private Map<String, Integer> calculateQuantityByProductStore(List<InventoryDetail> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyMap();
        }
        return detailList.stream()
                .collect(Collectors.groupingBy(
                        // 分组key：商品ID_仓库ID
                        detail -> String.format("%d_%d",
                                detail.getProductId(), detail.getWarehouseId()),
                        Collectors.summingInt(detail -> {
                            Integer quantity = detail.getProductQuantity();
                            return switch (detail.getOrderType()) {
                                case 0 -> quantity;    // 采购：+数量
                                case 1 -> -quantity;   // 采退：-数量
                                case 2 -> -quantity;   // 销售：-数量
                                case 3 -> quantity;    // 销退：+数量
                                case 4 -> -quantity;   // 调拨转出：-数量（从源仓库减少）
                                case 5 -> quantity;    // 调拨转入：+数量（向目标仓库增加）
                                default -> 0;
                            };
                        })
                ));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockInNew(MultiWarehouseStockInDTO stockInDTO, HttpServletRequest request) {
        Long purchaseOrderId = stockInDTO.getPurchaseOrderId();
        List<MultiWarehouseStockInDTO.WarehouseStockInItem> items = stockInDTO.getItems();
        // 查询采购订单并校验状态
        PurchaseOrder purchaseOrder = purchaseOrderService.getById(purchaseOrderId);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "采购订单不存在");
        }
        if (!purchaseOrder.getStatus().equals(PurchaseOrderStatusEnum.SHIPPED.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "只有已发货的订单才能进行入库操作");
        }
        // 校验入库总数量是否与采购订单数量一致
        int totalStockInQuantity = items.stream()
                .mapToInt(MultiWarehouseStockInDTO.WarehouseStockInItem::getQuantity)
                .sum();
        if (totalStockInQuantity != purchaseOrder.getProductQuantity()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "各仓库入库数量之和必须等于采购订单总数量");
        }
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 更新采购订单状态为“已入库”
        purchaseOrder.setStatus(PurchaseOrderStatusEnum.STORED.getValue());
        boolean isOrderUpdated = purchaseOrderService.updateById(purchaseOrder);
        ThrowUtils.throwIf(!isOrderUpdated, ErrorCode.SYSTEM_ERROR, "更新采购订单状态失败");
        // 遍历入库明细，为每个仓库执行入库操作
        for (MultiWarehouseStockInDTO.WarehouseStockInItem item : items) {
            Long warehouseId = item.getWarehouseId();
            Integer quantity = item.getQuantity();
            // 校验仓库是否存在
            if (warehouseService.getById(warehouseId) == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "仓库不存在，ID: " + warehouseId);
            }
            // 生成库存明细记录
            InventoryDetail detail = new InventoryDetail();
            detail.setProductId(purchaseOrder.getProductId());
            detail.setOrderId(purchaseOrderId); // 注意：你的表结构中orderId是VARCHAR
            detail.setType(InventoryDetailTypeEnum.TAKEIN.getValue());    // 入库
            detail.setOrderType(OrderTypeEnum.PURCHASE.getValue());     // 采购订单
            detail.setProductQuantity(quantity);
            detail.setWarehouseId(warehouseId); // 设置仓库ID
            detail.setCreateBy(loginUser.getId());
            boolean isDetailSaved = inventoryDetailService.save(detail);
            ThrowUtils.throwIf(!isDetailSaved, ErrorCode.SYSTEM_ERROR, "创建库存明细失败");
            // 更新或创建库存记录
            LambdaQueryWrapper<Inventory> queryWrapper = Wrappers.lambdaQuery();
            queryWrapper.eq(Inventory::getProductId, purchaseOrder.getProductId())
                    .eq(Inventory::getStoreId, purchaseOrder.getStoreId()) // 保留原有的门店维度
                    .eq(Inventory::getWarehouseId, warehouseId); // 增加仓库维度
            Inventory inventory = this.getOne(queryWrapper);
            if (inventory == null) {
                // 如果库存记录不存在，则创建
                inventory = new Inventory();
                inventory.setProductId(purchaseOrder.getProductId());
                inventory.setProductName(purchaseOrder.getProductName());
                inventory.setProductDescription(purchaseOrder.getProductDescription());
                inventory.setProductUrl(purchaseOrder.getProductUrl());
                // 出售价格默认比进货价多1
                inventory.setProductPrice(purchaseOrder.getProductPrice().add(new BigDecimal(1)));
                inventory.setStoreId(purchaseOrder.getStoreId());
                inventory.setWarehouseId(warehouseId); // 设置仓库ID
                inventory.setWarningThreshold(10); // 默认预警阈值
                inventory.setCreateBy(loginUser.getId());
                boolean isInventorySaved = this.save(inventory);
                ThrowUtils.throwIf(!isInventorySaved, ErrorCode.SYSTEM_ERROR, "创建库存记录失败");
            }
            // 库存记录存在不做任何处理
        }
    }

    @Override
    @Transactional
    public void saleOrder(Long saleOrderId, HttpServletRequest request) {
        SaleOrder saleOrder = saleOrderService.getById(saleOrderId);
        if (saleOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "销售订单不存在");
        }
        if (!saleOrder.getStatus().equals(SaleOrderStatusEnum.PENDING.getValue())) { // 假设0=待发货，1=已发货，2=已完成
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单当前状态不支持发货操作");
        }
        AmountOrder amountOrder = amountOrderService.getOne(
                new LambdaQueryWrapper<AmountOrder>()
                        .eq(AmountOrder::getOrderId, saleOrderId)
                        .eq(AmountOrder::getType, OrderTypeEnum.SALE.getValue()) // 2=销售订单类型
        );
        if (amountOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "关联的金额订单不存在");
        }
        if (!amountOrder.getStatus().equals(PayStatusEnum.PAID.getValue())) { // 假设1=已支付，0=待支付
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户尚未支付，无法发货");
        }
        saleOrder.setStatus(SaleOrderStatusEnum.SHIPPED.getValue()); // 已发货
        boolean updateSuccess = saleOrderService.updateById(saleOrder);
        if (!updateSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新订单状态失败");
        }
        // 生成库存明细（标记为销售出库）
        InventoryDetail inventoryDetail = new InventoryDetail();
        inventoryDetail.setProductId(saleOrder.getProductId());
        inventoryDetail.setProductQuantity(saleOrder.getProductQuantity());
        inventoryDetail.setOrderId(saleOrderId);
        inventoryDetail.setOrderType(OrderTypeEnum.SALE.getValue());
        inventoryDetail.setType(InventoryDetailTypeEnum.TAKEOUT.getValue());
        inventoryDetail.setWarehouseId(saleOrder.getWarehouseId());
        inventoryDetail.setCreateBy(getLoginUserUtil.getLoginUser(request).getId()); // 操作人ID
        boolean save = inventoryDetailService.save(inventoryDetail);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "库存明细表新增失败");
        }
    }

    @Override
    public void confirmSaleReturn(Long saleReturnId, HttpServletRequest request) {
        // 查询销退订单详情
        SaleReturn saleReturn = saleReturnService.getById(saleReturnId);
        if (saleReturn == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "销退订单不存在");
        }
        if (!saleReturn.getStatus().equals(SaleReturnStatusEnum.UNFINISHED.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该销退订单已完成，无需重复操作");
        }
        saleReturn.setStatus(SaleReturnStatusEnum.COMPLETED.getValue());
        boolean updateSuccess = saleReturnService.updateById(saleReturn);
        if (!updateSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新销退订单状态失败");
        }
        // 生成库存明细（标记为销退入库）
        InventoryDetail inventoryDetail = new InventoryDetail();
        inventoryDetail.setProductId(saleReturn.getProductId());
        inventoryDetail.setProductQuantity(saleReturn.getProductQuantity());
        inventoryDetail.setOrderId(saleReturnId);
        inventoryDetail.setOrderType(OrderTypeEnum.SALE_RETURN.getValue()); // 3-销退类型
        inventoryDetail.setType(InventoryDetailTypeEnum.TAKEOUT.getValue()); // 0-入库（库存增加）
        inventoryDetail.setWarehouseId(saleReturn.getWarehouseId()); // 退货入库仓库
        inventoryDetail.setCreateBy(getLoginUserUtil.getLoginUser(request).getId()); // 操作人ID（商家）
        boolean save = inventoryDetailService.save(inventoryDetail);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "库存明细表新增失败");
        }
    }
}