package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.AmountOrderMapper;
import com.qzh.backend.mapper.InventoryDetailMapper;
import com.qzh.backend.mapper.PurchaseOrderMapper;
import com.qzh.backend.mapper.PurchaseReturnMapper;
import com.qzh.backend.model.dto.product.InventoryDetailQueryDTO;
import com.qzh.backend.model.dto.product.PurchaseReturnCreateDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.*;
import com.qzh.backend.service.PurchaseReturnService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PurchaseReturnServiceImpl extends ServiceImpl<PurchaseReturnMapper, PurchaseReturn> implements PurchaseReturnService {

    private final InventoryDetailMapper inventoryDetailMapper;

    private final PurchaseOrderMapper purchaseOrderMapper;

    private final GetLoginUserUtil getLoginUserUtil;

    private final AmountOrderMapper amountOrderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseReturn(PurchaseReturnCreateDTO createDTO, HttpServletRequest request) {
        Long purchaseOrderId = createDTO.getOrderId();
        Integer returnQuantity = createDTO.getReturnQuantity();
        // 根据采购订单ID查询所有相关的库存明细记录
        LambdaQueryWrapper<InventoryDetail> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(InventoryDetail::getOrderId, purchaseOrderId)
                .eq(InventoryDetail::getOrderType, OrderTypeEnum.PURCHASE.getValue())
                .eq(InventoryDetail::getWarehouseId, createDTO.getWarehouseId());
        InventoryDetail inventoryDetail = inventoryDetailMapper.selectOne(queryWrapper);
        if (inventoryDetail == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到与该采购订单关联的库存明细。");
        }
        if (returnQuantity <= 0 || returnQuantity > inventoryDetail.getProductQuantity()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采退数量不合法，超出可退范围。");
        }
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectById(createDTO.getOrderId());
        // 通过采购价退款
        BigDecimal returnAmount = purchaseOrder.getProductPrice().multiply(BigDecimal.valueOf(returnQuantity));
        PurchaseReturn returnOrder = new PurchaseReturn();
        returnOrder.setProductId(purchaseOrder.getProductId());
        returnOrder.setPurchaseId(createDTO.getOrderId());
        returnOrder.setWarehouseId(createDTO.getWarehouseId());
        returnOrder.setProductName(purchaseOrder.getProductName());
        returnOrder.setProductDescription(purchaseOrder.getProductDescription());
        returnOrder.setProductUrl(purchaseOrder.getProductUrl());
        returnOrder.setProductPrice(purchaseOrder.getProductPrice());
        returnOrder.setProductQuantity(returnQuantity);
        returnOrder.setStoreId(purchaseOrder.getStoreId());
        returnOrder.setSupplierId(purchaseOrder.getSupplierId());
        returnOrder.setTotalAmount(returnAmount);
        returnOrder.setStatus(PurchaseReturnStatusEnum.UNFINISHED.getValue()); // 采退订单设置为未完成
        boolean save = this.save(returnOrder);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"采退表信息保存失败");

        AmountOrder amountOrder = new AmountOrder();
        amountOrder.setOrderId(returnOrder.getId());
        amountOrder.setType(OrderTypeEnum.PURCHASE_RETURN.getValue());
        // 设置供应商为付款人  当前操作人为收款人
        User loginUser = getLoginUserUtil.getLoginUser(request);
        amountOrder.setPayerId(purchaseOrder.getSupplierId());
        amountOrder.setPayeeId(loginUser.getId());
        amountOrder.setAmount(returnAmount);
        amountOrder.setStoreId(purchaseOrder.getStoreId());
        amountOrder.setPayType(PayTypeEnum.ALIPAY.getValue()); // 目前仅支持支付宝
        amountOrder.setStatus(PayStatusEnum.PENDING_PAYMENT.getValue()); // 待支付
        amountOrder.setCreateBy(loginUser.getId());
        boolean saveAmountOrder = amountOrderMapper.insert(amountOrder) > 0;
        if (!saveAmountOrder) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建金额订单失败");
        }
        return returnOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPurchaseReturn(Long returnId, HttpServletRequest request) {
        // 查询采退订单详情
        PurchaseReturn purchaseReturn = this.getById(returnId);
        if (purchaseReturn == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "采退订单不存在");
        }
        if (purchaseReturn.getStatus() != PurchaseReturnStatusEnum.UNFINISHED.getValue()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该采退订单已确认或已取消，无需重复操作");
        }

        // 校验金额订单支付状态（供应商是否已支付）
        AmountOrder amountOrder = amountOrderMapper.selectOne(
                Wrappers.lambdaQuery(AmountOrder.class)
                        .eq(AmountOrder::getOrderId, returnId)
                        .eq(AmountOrder::getType, OrderTypeEnum.PURCHASE_RETURN.getValue()) // 采退金额订单类型
        );
        if (amountOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "采退金额订单不存在");
        }
        if (amountOrder.getStatus() != PayStatusEnum.PAID.getValue()) { // 假设PAID=1表示已支付
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "供应商尚未支付采退金额，无法确认退货");
        }
        // 登录用户
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 先查询
        InventoryDetail getInenventoryDetail = inventoryDetailMapper.selectOne(
                Wrappers.lambdaQuery(InventoryDetail.class)
                        .eq(InventoryDetail::getOrderId, purchaseReturn.getPurchaseId())
                        .eq(InventoryDetail::getWarehouseId, purchaseReturn.getWarehouseId())
        );
        // 生成库存明细（标注采退类型）
        InventoryDetail inventoryDetail = new InventoryDetail();
        inventoryDetail.setProductId(purchaseReturn.getProductId());
        inventoryDetail.setOrderId(returnId); // 关联采退订单ID
        inventoryDetail.setType(InventoryDetailTypeEnum.TAKEOUT.getValue()); // 出库（采退商品出库）
        inventoryDetail.setOrderType(OrderTypeEnum.PURCHASE_RETURN.getValue()); // 采退类型（需在枚举中添加）
        inventoryDetail.setProductQuantity(purchaseReturn.getProductQuantity()); // 采退数量
        inventoryDetail.setWarehouseId(getInenventoryDetail.getWarehouseId()); // 退货仓库
        inventoryDetail.setCreateBy(loginUser.getId()); // 操作人（店长ID）
        boolean save = inventoryDetailMapper.insert(inventoryDetail) > 0;
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"库存明细表更新失败");
        // 更新采退订单状态为已确认
        purchaseReturn.setStatus(PurchaseReturnStatusEnum.COMPLETED.getValue());
        boolean b = this.updateById(purchaseReturn);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"采退表状态更新失败");
    }
}