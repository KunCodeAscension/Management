package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.SaleReturnMapper;
import com.qzh.backend.model.dto.product.SaleOrderQueryDTO;
import com.qzh.backend.model.dto.product.SaleReturnCreateDTO;
import com.qzh.backend.model.dto.product.SaleReturnQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.entity.SaleOrder;
import com.qzh.backend.model.entity.SaleReturn;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.enums.OrderTypeEnum;
import com.qzh.backend.model.enums.PayStatusEnum;
import com.qzh.backend.model.enums.PayTypeEnum;
import com.qzh.backend.model.enums.SaleReturnStatusEnum;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.service.SaleReturnService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SaleReturnServiceImpl extends ServiceImpl<SaleReturnMapper, SaleReturn> implements SaleReturnService {

    private final SaleReturnMapper saleReturnMapper;
    private final SaleOrderService saleOrderService;
    private final AmountOrderService amountOrderService;
    private final GetLoginUserUtil getLoginUserUtil;
    private final AppGlobalConfig appGlobalConfig; // 假设包含店长ID配置

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSaleReturn(SaleReturnCreateDTO dto, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 查询销售订单详情
        SaleOrder saleOrder = saleOrderService.getById(dto.getSaleOrderId());
        if (saleOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "销售订单不存在");
        }
        // 校验订单归属（只能退自己的订单）
        if (!saleOrder.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作他人订单");
        }
        // 计算退货金额（销售单价 × 退货数量）
        BigDecimal returnAmount = saleOrder.getProductPrice().multiply(BigDecimal.valueOf(saleOrder.getProductQuantity()));
        // 创建销退订单
        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setSaleOrderId(dto.getSaleOrderId());
        saleReturn.setStoreId(saleOrder.getStoreId());
        saleReturn.setUserId(loginUser.getId());
        saleReturn.setProductId(saleOrder.getProductId());
        saleReturn.setProductName(saleOrder.getProductName());
        saleReturn.setProductUrl(saleOrder.getProductUrl());
        saleReturn.setProductDescription(saleOrder.getProductDescription());
        saleReturn.setProductPrice(saleOrder.getProductPrice());
        saleReturn.setProductQuantity(saleOrder.getProductQuantity());
        saleReturn.setTotalAmount(returnAmount);
        saleReturn.setStatus(SaleReturnStatusEnum.UNFINISHED.getValue()); // 0-未完成
        saleReturn.setWarehouseId(saleOrder.getWarehouseId()); // 退货入库仓库
        saleReturn.setReason(dto.getReason());
        saleReturn.setCreateBy(loginUser.getId());
        boolean saveReturn = saleReturnMapper.insert(saleReturn) > 0;
        if (!saveReturn) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建销退订单失败");
        }
        // 创建金额订单
        AmountOrder amountOrder = new AmountOrder();
        amountOrder.setOrderId(saleReturn.getId());
        amountOrder.setType(OrderTypeEnum.SALE_RETURN.getValue()); // 3-销退订单类型
        amountOrder.setPayerId(appGlobalConfig.getManagerId()); // 付款人为店长
        amountOrder.setPayeeId(loginUser.getId()); // 收款人为用户
        amountOrder.setAmount(returnAmount);
        amountOrder.setStoreId(saleOrder.getStoreId());
        amountOrder.setPayType(PayTypeEnum.ALIPAY.getValue());
        amountOrder.setStatus(PayStatusEnum.PENDING_PAYMENT.getValue());
        amountOrder.setCreateBy(loginUser.getId());
        boolean saveAmount = amountOrderService.save(amountOrder);
        if (!saveAmount) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建金额订单失败");
        }
        return saleReturn.getId();
    }

    @Override
    public Page<SaleReturn> listMySaleReturns(SaleReturnQueryDTO dto, HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        int current = dto.getCurrent();
        int size = dto.getSize();
        ThrowUtils.throwIf(size <= 0 || size > 20, ErrorCode.PARAMS_ERROR);
        Page<SaleReturn> saleOrderPage = new Page<>(current, size);
        User loginUser = getLoginUserUtil.getLoginUser(request);
        dto.setUserId(loginUser.getId());
        return this.page(saleOrderPage, SaleReturnQueryDTO.getQueryWrapper(dto));
    }
}