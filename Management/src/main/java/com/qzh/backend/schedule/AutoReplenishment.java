package com.qzh.backend.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.*;
import com.qzh.backend.service.*;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 定时任务自动补货（支持跨仓库调拨优先，不足再采购）
 * 核心逻辑：库存低于阈值时，填补到阈值即可
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class AutoReplenishment {

    private final InventoryService inventoryService;

    private final InventoryDetailService inventoryDetailService;

    private final AutoReplenishmentService autoReplenishmentService;

    private final TransferLogService transferLogService;

    /**
     * 定时任务自动补货低于预警值的商品 每五分钟
     */
    @Scheduled(cron = "0 0/10 * * * *")
    public void task() {
        log.info("开始执行自动补货任务（支持跨仓库调拨）...");
        // 查询所有仓库的库存记录（按商品ID+仓库ID分组）
        List<Inventory> allInventories = inventoryService.list(Wrappers.lambdaQuery());
        if (CollectionUtils.isEmpty(allInventories)) {
            log.info("库存主表中没有记录，任务结束。");
            return;
        }
        // 按商品ID分组，获取每个商品在各仓库的库存信息
        Map<Long, List<Inventory>> productWarehouseMap = allInventories.stream()
                .collect(Collectors.groupingBy(Inventory::getProductId));
        // 计算所有商品在各仓库的实时库存（商品ID_仓库ID -> 实时库存）
        Map<String, Integer> realTimeQuantityMap = calculateRealTimeInventoryByProductWarehouse(allInventories);
        // 遍历每个商品，处理库存不足的仓库
        for (Map.Entry<Long, List<Inventory>> entry : productWarehouseMap.entrySet()) {
            Long productId = entry.getKey();
            List<Inventory> productInventories = entry.getValue();
            // 筛选当前商品库存低于阈值的仓库（待补货仓库）
            List<Inventory> lowStockInventories = productInventories.stream()
                    .filter(inventory -> {
                        String key = buildProductWarehouseKey(productId, inventory.getWarehouseId());
                        Integer realTimeQty = realTimeQuantityMap.getOrDefault(key, 0);
                        return realTimeQty < inventory.getWarningThreshold(); // 低于阈值才需要补（不含等于）
                    })
                    .toList();
            if (CollectionUtils.isEmpty(lowStockInventories)) {
                log.info("商品ID: {} 所有仓库库存均达标，无需补货", productId);
                continue;
            }
            // 处理每个待补货仓库
            for (Inventory lowStockInv : lowStockInventories) {
                try {
                    AutoReplenishment autoReplenishment = (AutoReplenishment) AopContext.currentProxy();
                    autoReplenishment.processLowStockWarehouse(lowStockInv, productInventories, realTimeQuantityMap);
                } catch (Exception e) {
                    log.error("处理仓库ID: {} 商品ID: {} 补货时发生异常: {}",
                            lowStockInv.getWarehouseId(), productId, e.getMessage(), e);
                }
            }
        }

        log.info("自动补货任务（支持跨仓库调拨）执行完毕。");
    }

    /**
     * 处理单个仓库的库存不足问题（优先调拨，再采购）
     */
    @Transactional(rollbackFor = Exception.class)
    public void processLowStockWarehouse(Inventory lowStockInv, List<Inventory> productInventories,
                                         Map<String, Integer> realTimeQuantityMap) {
        Long productId = lowStockInv.getProductId();
        Long targetWarehouseId = lowStockInv.getWarehouseId();
        String productName = lowStockInv.getProductName();
        int warningThreshold = lowStockInv.getWarningThreshold();
        int targetQuantity = warningThreshold;
        // 获取当前仓库的实时库存
        String targetKey = buildProductWarehouseKey(productId, targetWarehouseId);
        int currentQty = realTimeQuantityMap.getOrDefault(targetKey, 0);
        int neededQty = targetQuantity - currentQty;
        if (neededQty <= 0) {
            log.warn("仓库ID: {} 商品: {} 当前库存: {} 已达标（阈值: {}），无需填补",
                    targetWarehouseId, productName, currentQty, warningThreshold);
            return;
        }
        log.info("仓库ID: {} 商品: {} 库存不足，当前库存: {}，阈值: {}，需填补: {}（目标: {}）",
                targetWarehouseId, productName, currentQty, warningThreshold, neededQty, targetQuantity);
        // 查询其他仓库的可调拨库存（排除当前仓库，且库存>自身阈值）
        List<TransferSource> transferSources = getTransferableSources(productInventories, productId, targetWarehouseId, realTimeQuantityMap);
        if (CollectionUtils.isEmpty(transferSources)) {
            log.info("商品: {} 无可用调拨库存，直接创建采购订单", productName);
            // 无调拨库存，直接采购所需数量
            autoReplenishmentService.createReplenishOrders(lowStockInv, neededQty);
            return;
        }
        // 从其他仓库调拨库存填补缺口
        int transferredQty = 0; // 已调拨数量
        for (TransferSource source : transferSources) {
            if (transferredQty >= neededQty) {
                break; // 缺口已填补完成
            }
            // 本次可调拨数量（取剩余缺口和源仓库可调拨量的最小值）
            int transferableQty = source.getTransferableQty();
            int actualTransferQty = Math.min(transferableQty, neededQty - transferredQty);
            // 执行调拨操作
            Long orderId = autoReplenishmentService.executeTransfer(productId, productName, source.getWarehouseId(), targetWarehouseId, actualTransferQty);
            TransferLog transferLog = new TransferLog();
            transferLog.setProductId(productId);
            transferLog.setTransferOrderId(orderId);
            transferLog.setTransferQuantity(actualTransferQty);
            transferLog.setSourceWarehouseId(source.getWarehouseId());
            transferLog.setTransferOrderId(targetWarehouseId);
            transferLog.setRemark("定时任务自动调拨");
            boolean save = transferLogService.save(transferLog);
            ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR,"调拨日志记录异常");
            log.info("调拨成功：从仓库ID: {} 调拨商品: {} 数量: {} 到仓库ID: {}", source.getWarehouseId(), productName, actualTransferQty, targetWarehouseId);
            transferredQty += actualTransferQty;
        }
        // 调拨后仍有缺口，创建采购订单补充剩余数量
        int remainingNeededQty = neededQty - transferredQty;
        if (remainingNeededQty > 0) {
            log.info("商品: {} 调拨后仍需补充: {}（当前已补: {}），创建采购订单",
                    productName, remainingNeededQty, transferredQty);
            autoReplenishmentService.createReplenishOrders(lowStockInv, remainingNeededQty);
        } else {
            log.info("商品: {} 已通过调拨填补完成（共补: {}），无需采购", productName, transferredQty);
        }
    }

    /**
     * 查询可调拨库存的来源仓库
     */
    public List<TransferSource> getTransferableSources(List<Inventory> productInventories, Long productId,
                                                       Long targetWarehouseId, Map<String, Integer> realTimeQuantityMap) {
        return productInventories.stream()
                // 排除目标仓库（不能自己调拨给自己）
                .filter(inv -> !inv.getWarehouseId().equals(targetWarehouseId))
                .map(inv -> {
                    String sourceKey = buildProductWarehouseKey(productId, inv.getWarehouseId());
                    int realTimeQty = realTimeQuantityMap.getOrDefault(sourceKey, 0);
                    int warningThreshold = inv.getWarningThreshold();
                    // 可调拨量 = 实时库存 - 自身阈值（确保调拨后源仓库库存不低于阈值）
                    int transferableQty = realTimeQty - warningThreshold;
                    return new TransferSource(inv.getWarehouseId(), realTimeQty, warningThreshold, transferableQty);
                })
                // 过滤掉无调拨量的仓库（transferableQty > 0）
                .filter(source -> source.getTransferableQty() > 0)
                // 按可调拨量降序排序（优先从调拨量大的仓库获取）
                .sorted((s1, s2) -> Integer.compare(s2.getTransferableQty(), s1.getTransferableQty()))
                .collect(Collectors.toList());
    }

    /**
     * 按「商品ID+仓库ID」计算实时库存
     */
    public Map<String, Integer> calculateRealTimeInventoryByProductWarehouse(List<Inventory> inventories) {
        if (CollectionUtils.isEmpty(inventories)) {
            return Collections.emptyMap();
        }
        // 提取所有「商品ID+仓库ID」组合，精准查询库存明细
        List<String> productWarehouseKeys = inventories.stream()
                .map(inv -> buildProductWarehouseKey(inv.getProductId(), inv.getWarehouseId()))
                .distinct()
                .collect(Collectors.toList());
        // 查询相关的库存明细
        QueryWrapper<InventoryDetail> queryWrapper = Wrappers.query(InventoryDetail.class);
        queryWrapper.in("CONCAT(productId, '_', warehouseId)", productWarehouseKeys);
        List<InventoryDetail> detailList = inventoryDetailService.list(queryWrapper);
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyMap();
        }
        // 按「商品ID+仓库ID」分组计算实时库存
        return detailList.stream()
                .collect(Collectors.groupingBy(
                        detail -> buildProductWarehouseKey(detail.getProductId(), detail.getWarehouseId()),
                        Collectors.summingInt(detail -> switch (detail.getOrderType()) {
                            case 0 -> detail.getProductQuantity();    // 采购：+
                            case 1 -> -detail.getProductQuantity();   // 采退：-
                            case 2 -> -detail.getProductQuantity();   // 销售：-
                            case 3 -> detail.getProductQuantity();    // 销退：+
                            case 4 -> -detail.getProductQuantity();   // 调拨转出：-
                            case 5 -> detail.getProductQuantity();    // 调拨转入：+
                            default -> 0;
                        })
                ));
    }

    /**
     * 构建「商品ID_仓库ID」组合键
     */
    private String buildProductWarehouseKey(Long productId, Long warehouseId) {
        return String.format("%d_%d", productId, warehouseId);
    }

    /**
     * 调拨来源仓库DTO
     */
    @lombok.Data
    public static class TransferSource {
        private final Long warehouseId; // 源仓库ID
        private final int realTimeQuantity; // 源仓库实时库存
        private final int warningThreshold; // 源仓库预警阈值
        private final int transferableQty; // 可调拨数量（实时库存 - 阈值）
    }
}