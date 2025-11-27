package com.qzh.backend.schedule;

import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.*;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.service.InventoryDetailService;
import com.qzh.backend.service.ProductService;
import com.qzh.backend.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoReplenishmentService {

    private final AppGlobalConfig appGlobalConfig;

    private final ProductService productService;

    private final PurchaseOrderService purchaseOrderService;

    private final AmountOrderService amountOrderService;

    private final InventoryDetailService inventoryDetailService;

    /**
     * 创建采购订单（库存不足且无调拨来源时）
     */
    @Transactional
    public void createReplenishOrders(Inventory inventory, int replenishQuantity) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setStoreId(appGlobalConfig.getCurrentStoreId());
        // 查询商品信息
        Product product = productService.getById(inventory.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                    String.format("商品ID: %d 不存在，无法创建采购订单", inventory.getProductId()));
        }
        purchaseOrder.setSupplierId(product.getSupplierId());
        purchaseOrder.setProductId(product.getId());
        purchaseOrder.setProductName(product.getName());
        purchaseOrder.setProductUrl(product.getUrl());
        purchaseOrder.setProductDescription(product.getDescription());
        purchaseOrder.setProductPrice(product.getPrice());
        purchaseOrder.setProductQuantity(replenishQuantity);
        purchaseOrder.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(replenishQuantity)));
        purchaseOrder.setStatus(PurchaseOrderStatusEnum.PENDING.getValue()); // 0 - 待发货
        purchaseOrder.setType(PurchaseOrderTypeEnum.THRESHOLD.getValue());  // 1 - 阈值触发
        purchaseOrder.setCreateBy(appGlobalConfig.getManagerId()); // 店长ID
        boolean purchaseSaved = purchaseOrderService.save(purchaseOrder);
        if (!purchaseSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建采购订单失败");
        }
        // 创建对应的金额订单
        AmountOrder amountOrder = getAmountOrder(purchaseOrder, product);
        boolean amountSaved = amountOrderService.save(amountOrder);
        if (!amountSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建金额订单失败");
        }
        log.info("成功为商品 [{}]（仓库ID: {}）创建采购订单，采购数量: {}",
                product.getName(), inventory.getWarehouseId(), replenishQuantity);
    }

    private AmountOrder getAmountOrder(PurchaseOrder purchaseOrder, Product product) {
        AmountOrder amountOrder = new AmountOrder();
        amountOrder.setOrderId(purchaseOrder.getId()); // 关联采购订单ID
        amountOrder.setType(OrderTypeEnum.PURCHASE.getValue()); // 0 - 采购
        amountOrder.setPayerId(appGlobalConfig.getManagerId());
        amountOrder.setPayeeId(product.getSupplierId());
        amountOrder.setAmount(purchaseOrder.getTotalAmount());
        amountOrder.setStatus(PayStatusEnum.PENDING_PAYMENT.getValue());
        amountOrder.setCreateBy(appGlobalConfig.getManagerId());
        return amountOrder;
    }


    /**
     * 执行跨仓库调拨操作（生成调拨转出/转入的库存明细）
     */
    @Transactional
    public Long executeTransfer(Long productId, String productName, Long sourceWarehouseId,
                                Long targetWarehouseId, int transferQty) {
        Long operatorId = appGlobalConfig.getManagerId(); // 操作人：店长ID
        // 生成调拨订单ID
        Long transferOrderId = generateIntegerUniqueId();
        // 生成源仓库的「调拨转出」明细（库存减少）
        InventoryDetail outDetail = new InventoryDetail();
        outDetail.setProductId(productId);
        outDetail.setOrderId(transferOrderId);
        outDetail.setType(InventoryDetailTypeEnum.TAKEOUT.getValue()); // 0-出库
        outDetail.setOrderType(OrderTypeEnum.TRANSFER_OUT.getValue()); // 4-调拨转出
        outDetail.setProductQuantity(transferQty);
        outDetail.setWarehouseId(sourceWarehouseId); // 源仓库ID
        outDetail.setCreateBy(operatorId);
        boolean outSaved = inventoryDetailService.save(outDetail);
        if (!outSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    String.format("仓库ID: %d 商品: %s 调拨转出明细创建失败", sourceWarehouseId, productName));
        }

        // 3. 生成目标仓库的「调拨转入」明细（库存增加）
        InventoryDetail inDetail = new InventoryDetail();
        inDetail.setProductId(productId);
        inDetail.setOrderId(transferOrderId);
        inDetail.setType(InventoryDetailTypeEnum.TAKEIN.getValue()); // 1-入库
        inDetail.setOrderType(OrderTypeEnum.TRANSFER_IN.getValue()); // 5-调拨转入
        inDetail.setProductQuantity(transferQty);
        inDetail.setWarehouseId(targetWarehouseId); // 目标仓库ID
        inDetail.setCreateBy(operatorId);
        boolean inSaved = inventoryDetailService.save(inDetail);
        if (!inSaved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    String.format("仓库ID: %d 商品: %s 调拨转入明细创建失败", targetWarehouseId, productName));
        }
        return transferOrderId;
    }


    private Long generateIntegerUniqueId() {
        // 生成UUID并去除横线，转为字节数组
        byte[] uuidBytes = UUID.randomUUID().toString().replace("-", "").getBytes();
        // 计算哈希值（避免正负值，& 0x7FFFFFFF 确保结果为非负整数）
        int intHashCode = Arrays.hashCode(uuidBytes) & 0x7FFFFFFF;
        // 确保结果为非负Long：通过 & 0x7FFFFFFFL（L表示长整型）消除符号位
        long longHashCode = intHashCode & 0x7FFFFFFFL;
        //  极端情况：若哈希值为0，返回非0默认值（避免订单ID为0，符合业务习惯）
        return longHashCode == 0 ? 100000L : longHashCode;
    }

}
