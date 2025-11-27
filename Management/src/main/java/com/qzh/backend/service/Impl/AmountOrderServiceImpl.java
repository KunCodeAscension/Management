package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.AmountOrderMapper;
import com.qzh.backend.model.dto.product.AmountOrderQueryDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.PayStatusEnum;
import com.qzh.backend.model.vo.AmountOrderDetailVO;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.service.PurchaseOrderService;
import com.qzh.backend.service.PurchaseReturnService;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmountOrderServiceImpl extends ServiceImpl<AmountOrderMapper, AmountOrder> implements AmountOrderService {

    private final AppGlobalConfig  appGlobalConfig;

    private final GetLoginUserUtil getLoginUserUtil;

    @Resource
    @Lazy
    private PurchaseOrderService purchaseOrderService;

    private final PurchaseReturnService purchaseReturnService;

    @Resource
    @Lazy
    private SaleOrderService saleOrderService;

    @Resource
    @Lazy
    private SaleReturnServiceImpl saleReturnService;

    @Override
    public Page<AmountOrder> listAmountOrdersByStoreId(AmountOrderQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size > 20,ErrorCode.PARAMS_ERROR);
        Page<AmountOrder> page = new Page<>(current, size);
        queryDTO.setStoreId(appGlobalConfig.getCurrentStoreId());
        return this.page(page, AmountOrderQueryDTO.getQueryWrapper(queryDTO));
    }

    @Override
    public void payOrder(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AmountOrder amountOrder = this.getById(id);
        ThrowUtils.throwIf(amountOrder == null, ErrorCode.NOT_FOUND_ERROR,"订单不存在");
        Long payerId = amountOrder.getPayerId();
        User loginUser = getLoginUserUtil.getLoginUser(request);
        if(!payerId.equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该订单不由你支付");
        }
        // 设置订单状态为已支付
        amountOrder.setStatus(PayStatusEnum.PAID.getValue());
        boolean b = this.updateById(amountOrder);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR,"订单支付失败");
    }

    @Override
    public void cancleOrder(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AmountOrder amountOrder = this.getById(id);
        ThrowUtils.throwIf(amountOrder == null, ErrorCode.NOT_FOUND_ERROR,"订单不存在");
        Long payerId = amountOrder.getPayerId();
        User loginUser = getLoginUserUtil.getLoginUser(request);
        if(!payerId.equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该订单不由你支付");
        }
        // 设置订单状态为已支付
        amountOrder.setStatus(PayStatusEnum.CANCELLED.getValue());
        boolean b = this.updateById(amountOrder);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR,"订单取消失败");
    }

    @Override
    public AmountOrderDetailVO getAmountOrderDetail(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0,ErrorCode.PARAMS_ERROR);
        AmountOrder amountOrder = this.getById(id);
        ThrowUtils.throwIf(amountOrder == null, ErrorCode.NOT_FOUND_ERROR,"订单不存在");
        User loginUser = getLoginUserUtil.getLoginUser(request);
        if(!loginUser.getId().equals(amountOrder.getPayerId()) && !loginUser.getId().equals(amountOrder.getPayeeId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"该订单不属于你，无法查看");
        }
        AmountOrderDetailVO amountOrderDetailVO = new AmountOrderDetailVO();
        amountOrderDetailVO.setAmount(amountOrder.getAmount());
        amountOrderDetailVO.setAmountOrderStatus(amountOrder.getStatus());
        amountOrderDetailVO.setAmountOrderId(amountOrder.getId());
        amountOrderDetailVO.setPayType(amountOrder.getPayType());
        amountOrderDetailVO.setPayerId(amountOrder.getPayerId());
        amountOrderDetailVO.setStoreId(appGlobalConfig.getCurrentStoreId());
        amountOrderDetailVO.setStoreName(appGlobalConfig.getCurrentStoreName());
        amountOrderDetailVO.setTradeNo(amountOrder.getTradeNo());
        Long orderId = amountOrder.getOrderId();
        switch (amountOrder.getType()) {
            case 0:
                PurchaseOrder purchaseOrder = purchaseOrderService.getById(orderId);
                amountOrderDetailVO.setProductId(purchaseOrder.getProductId());
                amountOrderDetailVO.setProductName(purchaseOrder.getProductName());
                amountOrderDetailVO.setProductDescription(purchaseOrder.getProductDescription());
                amountOrderDetailVO.setProductPrice(purchaseOrder.getProductPrice());
                amountOrderDetailVO.setProductQuantity(purchaseOrder.getProductQuantity());
                amountOrderDetailVO.setProductUrl(purchaseOrder.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(purchaseOrder.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            case 1:
                PurchaseReturn purchaseReturn = purchaseReturnService.getById(orderId);
                amountOrderDetailVO.setProductId(purchaseReturn.getProductId());
                amountOrderDetailVO.setProductName(purchaseReturn.getProductName());
                amountOrderDetailVO.setProductDescription(purchaseReturn.getProductDescription());
                amountOrderDetailVO.setProductPrice(purchaseReturn.getProductPrice());
                amountOrderDetailVO.setProductQuantity(purchaseReturn.getProductQuantity());
                amountOrderDetailVO.setProductUrl(purchaseReturn.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(purchaseReturn.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            case 2:
                SaleOrder saleOrder = saleOrderService.getById(orderId);
                amountOrderDetailVO.setProductId(saleOrder.getProductId());
                amountOrderDetailVO.setProductName(saleOrder.getProductName());
                amountOrderDetailVO.setProductDescription(saleOrder.getProductDescription());
                amountOrderDetailVO.setProductPrice(saleOrder.getProductPrice());
                amountOrderDetailVO.setProductQuantity(saleOrder.getProductQuantity());
                amountOrderDetailVO.setProductUrl(saleOrder.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(saleOrder.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            case 3:
                SaleReturn saleReturn = saleReturnService.getById(orderId);
                amountOrderDetailVO.setProductId(saleReturn.getProductId());
                amountOrderDetailVO.setProductName(saleReturn.getProductName());
                amountOrderDetailVO.setProductDescription(saleReturn.getProductDescription());
                amountOrderDetailVO.setProductPrice(saleReturn.getProductPrice());
                amountOrderDetailVO.setProductQuantity(saleReturn.getProductQuantity());
                amountOrderDetailVO.setProductUrl(saleReturn.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(saleReturn.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            default:
                ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "未知的金额单类型：" + amountOrder.getType());
        }
        return amountOrderDetailVO;
    }
}