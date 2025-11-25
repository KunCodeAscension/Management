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
import com.qzh.backend.model.dto.product.PurchaseReturnCreateDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.OrderTypeEnum;
import com.qzh.backend.model.enums.PayStatusEnum;
import com.qzh.backend.model.enums.PayTypeEnum;
import com.qzh.backend.model.enums.PurchaseReturnStatusEnum;
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
                .eq(InventoryDetail::getOrderType, OrderTypeEnum.PURCHASE.getValue());
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
}