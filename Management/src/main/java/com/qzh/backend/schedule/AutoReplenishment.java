package com.qzh.backend.schedule;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.PayStatusEnum;
import com.qzh.backend.model.enums.OrderTypeEnum;
import com.qzh.backend.model.enums.PurchaseOrderStatusEnum;
import com.qzh.backend.model.enums.PurchaseOrderTypeEnum;
import com.qzh.backend.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定时任务自动补货
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AutoReplenishment {

    private final InventoryService inventoryService;

    private final InventoryDetailService inventoryDetailService;

    private final ProductService productService;

    private final PurchaseOrderService purchaseOrderService;

    private final AppGlobalConfig appGlobalConfig;

    private final AmountOrderService amountOrderService;

    /**
     * 定时任务自动补货低于预警值的商品 每五分钟
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void task() {
        log.info("开始执行自动补货任务...");
        List<Inventory> allInventories = inventoryService.list(Wrappers.lambdaQuery());
        if (CollectionUtils.isEmpty(allInventories)) {
            log.info("库存主表中没有记录，任务结束。");
            return;
        }
        Map<String, Integer> realTimeQuantityMap = calculateRealTimeInventory(allInventories);
        // 筛选出库存数量低于阈值的商品
        List<Inventory> lowStockInventories = allInventories.stream()
                .filter(inventory -> {
                    String productIdKey = String.valueOf(inventory.getProductId());
                    Integer realTimeQuantity = realTimeQuantityMap.getOrDefault(productIdKey, 0);
                    // 当实时库存 <= 预警阈值时，需要补货
                    return realTimeQuantity <= inventory.getWarningThreshold();
                })
                .toList();

        if (lowStockInventories.isEmpty()) {
            log.info("没有发现库存低于预警值的商品，任务结束。");
            return;
        }
        for (Inventory inventory : lowStockInventories) {
            try {
                String productIdKey = String.valueOf(inventory.getProductId());
                int currentQuantity = realTimeQuantityMap.getOrDefault(productIdKey, 0);
                int targetQuantity = inventory.getWarningThreshold() + 10;
                int replenishQuantity = targetQuantity - currentQuantity;
                if (replenishQuantity <= 0) {
                    log.warn("商品 [{}] 计算出的补货量不合法: {}", inventory.getProductName(), replenishQuantity);
                    continue;
                }
                createReplenishOrders(inventory, replenishQuantity);
            } catch (Exception e) {
                log.error("为商品 [{}] 创建补货订单时发生异常: {}", inventory.getProductName(), e.getMessage(), e);
            }
        }

        log.info("自动补货任务执行完毕。");
    }

    private Map<String, Integer> calculateRealTimeInventory(List<Inventory> inventories) {
        if (CollectionUtils.isEmpty(inventories)) {
            return Collections.emptyMap();
        }
        List<Long> productIds = inventories.stream()
                .map(Inventory::getProductId)
                .distinct()
                .collect(Collectors.toList());
        List<InventoryDetail> allDetails = inventoryDetailService.list(
                Wrappers.lambdaQuery(InventoryDetail.class)
                        .in(InventoryDetail::getProductId, productIds)
        );
        if (CollectionUtils.isEmpty(allDetails)) {
            return Collections.emptyMap();
        }
        return allDetails.stream()
                .collect(Collectors.groupingBy(
                        detail -> String.valueOf(detail.getProductId()),
                        Collectors.summingInt(detail -> {
                            switch (detail.getOrderType()) {
                                case 0: return detail.getProductQuantity();   // 采购
                                case 1: return -detail.getProductQuantity();  // 采退
                                case 2: return -detail.getProductQuantity();  // 销售
                                case 3: return detail.getProductQuantity();   // 销退
                                default: return 0;
                            }
                        })
                ));
    }

    @Transactional
    public void createReplenishOrders(Inventory inventory, int replenishQuantity) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setStoreId(appGlobalConfig.getCurrentStoreId());
        // 查询商品信息
        Product product = productService.getById(inventory.getProductId());
        purchaseOrder.setSupplierId(product.getSupplierId());
        purchaseOrder.setProductId(product.getId());
        purchaseOrder.setProductName(product.getName());
        purchaseOrder.setProductUrl(product.getUrl());
        purchaseOrder.setProductDescription(product.getDescription());
        purchaseOrder.setProductPrice(product.getPrice());
        purchaseOrder.setProductQuantity(replenishQuantity);
        purchaseOrder.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(replenishQuantity)));
        purchaseOrder.setStatus(PurchaseOrderStatusEnum.PENDING.getValue()); // 0 - 待发货
        purchaseOrder.setType(PurchaseOrderTypeEnum.THRESHOLD.getValue());  // 1 - 阈值出发
        purchaseOrder.setCreateBy(appGlobalConfig.getManagerId()); // 设置为店长ID

        boolean purchaseSaved = purchaseOrderService.save(purchaseOrder);
        if (!purchaseSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建采购订单失败");
        }
        // 创建对应的金额订单
        AmountOrder amountOrder = new AmountOrder();
        amountOrder.setOrderId(purchaseOrder.getId()); // 关联采购订单ID
        amountOrder.setType(OrderTypeEnum.PURCHASE.getValue()); // 0 - 采购
        amountOrder.setPayerId(appGlobalConfig.getManagerId());
        amountOrder.setPayeeId(product.getSupplierId());
        amountOrder.setAmount(purchaseOrder.getTotalAmount());
        amountOrder.setStatus(PayStatusEnum.PENDING_PAYMENT.getValue());
        amountOrder.setCreateBy(appGlobalConfig.getManagerId());
        boolean amountSaved = amountOrderService.save(amountOrder);
        if (!amountSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建金额订单失败");
        }
        log.info("成功为商品 [{}] 创建补货订单，采购数量: {}", inventory.getProductName(), replenishQuantity);
    }

}
