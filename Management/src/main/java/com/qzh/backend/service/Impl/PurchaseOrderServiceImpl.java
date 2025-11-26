package com.qzh.backend.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.PurchaseOrderMapper;
import com.qzh.backend.model.dto.product.PurchaseOrderCreateDTO;
import com.qzh.backend.model.dto.product.PurchaseOrderQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.entity.Product;
import com.qzh.backend.model.entity.PurchaseOrder;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.enums.*;
import com.qzh.backend.model.vo.PurchaseOrderListVO;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.service.PurchaseOrderService;
import com.qzh.backend.service.ProductService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl extends ServiceImpl<PurchaseOrderMapper, PurchaseOrder> implements PurchaseOrderService {

    private final ProductService productService;

    private final AmountOrderService amountOrderService;

    private final AppGlobalConfig appGlobalConfig;

    private final GetLoginUserUtil getLoginUserUtil;

    /**
     * 创建采购订单，并生成对应的金额订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseOrder(PurchaseOrderCreateDTO createDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(createDTO == null,ErrorCode.PARAMS_ERROR);
        Integer quantity = createDTO.getQuantity();
        ThrowUtils.throwIf(quantity<=0,ErrorCode.SYSTEM_ERROR,"采购数量不能小于等于0");
        Product product = productService.getById(createDTO.getProductId());
        // 判断商品是否存在或者商品是否下架
        if (product == null || product.getStatus().equals(ProductStatusEnum.TAKEDOWN.getValue())) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "商品不存在: " + createDTO.getProductId());
        }
        // 采购价格
        BigDecimal price = product.getPrice();
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(createDTO.getQuantity()));
        // 获取登录用户
        User loginUser = getLoginUserUtil.getLoginUser(request);
        //创建采购订单
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setStoreId(appGlobalConfig.getCurrentStoreId());
        purchaseOrder.setSupplierId(product.getSupplierId());
        purchaseOrder.setProductId(product.getId());
        purchaseOrder.setProductName(product.getName());
        purchaseOrder.setProductUrl(product.getUrl());
        purchaseOrder.setProductDescription(product.getDescription());
        purchaseOrder.setProductPrice(price);
        purchaseOrder.setProductQuantity(createDTO.getQuantity());
        purchaseOrder.setTotalAmount(totalAmount);
        purchaseOrder.setStatus(PurchaseOrderStatusEnum.PENDING.getValue()); // 0-待发货
        purchaseOrder.setType(PurchaseOrderTypeEnum.MANUAL.getValue());  // 0-手动发起
        // 购买人
        purchaseOrder.setCreateBy(loginUser.getId());
        boolean saved = this.save(purchaseOrder);
        ThrowUtils.throwIf(!saved,ErrorCode.SYSTEM_ERROR,"采购订单创建失败");
        // 创建金额订单（付款单）
        AmountOrder amountOrder = new AmountOrder();
        amountOrder.setOrderId(purchaseOrder.getId());
        amountOrder.setType(OrderTypeEnum.PURCHASE.getValue()); // 0-采购
        amountOrder.setPayerId(loginUser.getId()); // 付款人设置为当前发起购买的用户
        amountOrder.setPayeeId(product.getSupplierId());  // 收款人设置为产品供应商
        amountOrder.setAmount(totalAmount);
        amountOrder.setStoreId(appGlobalConfig.getCurrentStoreId());  // 设置门店ID
        amountOrder.setStatus(PayStatusEnum.PENDING_PAYMENT.getValue()); // 0-待支付
        amountOrder.setPayType(PayTypeEnum.ALIPAY.getValue()); // 目前仅支持支付宝沙箱
        amountOrder.setCreateBy(loginUser.getId());
        boolean amountOrderSaved = amountOrderService.save(amountOrder);
        ThrowUtils.throwIf(!amountOrderSaved,ErrorCode.SYSTEM_ERROR,"采购订单创建失败");
        // 返回付款单ID
        return purchaseOrder.getId();
    }

    @Override
    public Page<PurchaseOrderListVO> listPurchaseOrdersWithAmount(PurchaseOrderQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null,ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size <= 0 || size > 20,ErrorCode.PARAMS_ERROR);
        // 分页查询采购订单列表
        Page<PurchaseOrder> purchaseOrderPage = this.page(
                new Page<>(current,size),
                PurchaseOrderQueryDTO.getQueryWrapper(queryDTO)
        );
        // 若采购订单为空，直接返回空分页
        if (CollUtil.isEmpty(purchaseOrderPage.getRecords())) {
            return new Page<>(current, size, 0);
        }
        // 收集所有采购订单的ID，用于关联查询金额订单
        List<Long> purchaseOrderIds = purchaseOrderPage.getRecords().stream()
                .map(PurchaseOrder::getId)
                .collect(Collectors.toList());
        // 批量查询关联的金额订单
        LambdaQueryWrapper<AmountOrder> amountOrderWrapper = Wrappers.lambdaQuery(AmountOrder.class)
                .in(AmountOrder::getOrderId, purchaseOrderIds);
        List<AmountOrder> amountOrderList = amountOrderService.list(amountOrderWrapper);
        // 构建金额订单的映射：key为 orderId (采购订单ID的字符串)，value为 AmountOrder 对象
        Map<Long, AmountOrder> amountOrderMap = amountOrderList.stream()
                .collect(Collectors.toMap(
                        AmountOrder::getOrderId,
                        Function.identity(), // 直接使用对象本身作为值
                        (existing, replacement) -> existing
                ));
        // 组装 PurchaseOrderListVO
        List<PurchaseOrderListVO> voList = purchaseOrderPage.getRecords().stream()
                .map(purchaseOrder -> {
                    PurchaseOrderListVO vo = new PurchaseOrderListVO();
                    vo.setPurchaseOrderId(purchaseOrder.getId());
                    vo.setStoreId(purchaseOrder.getStoreId());
                    vo.setSupplierId(purchaseOrder.getSupplierId());
                    vo.setProductId(purchaseOrder.getProductId());
                    vo.setProductName(purchaseOrder.getProductName());
                    vo.setProductUrl(purchaseOrder.getProductUrl());
                    vo.setProductDescription(purchaseOrder.getProductDescription());
                    vo.setProductPrice(purchaseOrder.getProductPrice());
                    vo.setProductQuantity(purchaseOrder.getProductQuantity());
                    vo.setPurchaseOrderStatus(purchaseOrder.getStatus());
                    vo.setPurchaseOrderType(purchaseOrder.getType());
                    vo.setCreateTime(purchaseOrder.getCreateTime());
                    vo.setUpdateTime(purchaseOrder.getUpdateTime());
                    vo.setCreateBy(purchaseOrder.getCreateBy());
                    AmountOrder amountOrder = amountOrderMap.get(purchaseOrder.getId());
                    if (amountOrder != null) {
                        vo.setAmountOrderId(amountOrder.getId());
                        // 注意：这里的 orderId 是金额订单自己的业务编号，不是采购订单ID
                        vo.setOrderId(amountOrder.getOrderId());
                        vo.setAmountOrderType(amountOrder.getType());
                        vo.setPayerId(amountOrder.getPayerId());
                        vo.setPayeeId(amountOrder.getPayeeId());
                        vo.setAmount(amountOrder.getAmount());
                        vo.setAmountOrderStatus(amountOrder.getStatus());
                        vo.setPayType(amountOrder.getPayType());
                        vo.setTradeNo(amountOrder.getTradeNo());
                    } else {
                        log.warn("未找到与采购订单 ID:{} 关联的金额订单",purchaseOrder.getId());
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        Page<PurchaseOrderListVO> voPage = new Page<>();
        voPage.setRecords(voList);
        voPage.setTotal(purchaseOrderPage.getTotal());
        voPage.setSize(purchaseOrderPage.getSize());
        voPage.setCurrent(purchaseOrderPage.getCurrent());
        voPage.setPages(purchaseOrderPage.getPages());
        return voPage;
    }


    @Override
    public PurchaseOrderListVO getPurchaseOrderById(Long id) {
        ThrowUtils.throwIf(id==null || id <= 0,ErrorCode.PARAMS_ERROR);
        // 根据ID查询采购订单
        PurchaseOrder purchaseOrder = this.getById(id);
        if (purchaseOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "采购订单不存在");
        }
        // 查询关联的金额订单
        LambdaQueryWrapper<AmountOrder> amountOrderWrapper = Wrappers.lambdaQuery(AmountOrder.class)
                .eq(AmountOrder::getOrderId, purchaseOrder.getId());
        AmountOrder amountOrder = amountOrderService.getOne(amountOrderWrapper);
        // 组装成 PurchaseOrderListVO 并返回
        PurchaseOrderListVO vo = new PurchaseOrderListVO();
        // 填充采购订单字段
        vo.setPurchaseOrderId(purchaseOrder.getId());
        vo.setStoreId(purchaseOrder.getStoreId());
        vo.setSupplierId(purchaseOrder.getSupplierId());
        vo.setProductId(purchaseOrder.getProductId());
        vo.setProductName(purchaseOrder.getProductName());
        vo.setProductUrl(purchaseOrder.getProductUrl());
        vo.setProductDescription(purchaseOrder.getProductDescription());
        vo.setProductPrice(purchaseOrder.getProductPrice());
        vo.setProductQuantity(purchaseOrder.getProductQuantity());
        vo.setPurchaseOrderStatus(purchaseOrder.getStatus());
        vo.setPurchaseOrderType(purchaseOrder.getType());
        vo.setCreateTime(purchaseOrder.getCreateTime());
        vo.setUpdateTime(purchaseOrder.getUpdateTime());
        vo.setCreateBy(purchaseOrder.getCreateBy());
        // 填充金额订单字段
        if (amountOrder != null) {
            vo.setAmountOrderId(amountOrder.getId());
            vo.setOrderId(amountOrder.getOrderId());
            vo.setAmountOrderType(amountOrder.getType());
            vo.setPayerId(amountOrder.getPayerId());
            vo.setPayeeId(amountOrder.getPayeeId());
            vo.setAmount(amountOrder.getAmount());
            vo.setAmountOrderStatus(amountOrder.getStatus());
            vo.setPayType(amountOrder.getPayType());
            vo.setTradeNo(amountOrder.getTradeNo());
        }
        return vo;
    }

    @Override
    public void shipPurchaseOrder(Long orderId,HttpServletRequest request) {
        // 根据 amountOrderId 查询金额单，确认其存在
        LambdaQueryWrapper<AmountOrder> amountOrderQueryWrapper = Wrappers.lambdaQuery(AmountOrder.class)
                .eq(AmountOrder::getOrderId, orderId);
        AmountOrder amountOrder = amountOrderService.getOne(amountOrderQueryWrapper);
        if (amountOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到编号为 " + orderId + " 的金额单");
        }
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 如果该订单为登录用户 那么payee收款人为供应商
        if(!amountOrder.getPayeeId().equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该订单不属于你，禁止操作");
        }
        // 查询金额单状态 如果是已支付允许发货
        if(!amountOrder.getStatus().equals(PayStatusEnum.PAID.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单未被门店支付，禁止操作");
        }
        // 采购单
        PurchaseOrder purchaseOrder = this.getById(orderId);
        // 设置状态为已发货
        purchaseOrder.setStatus(PurchaseOrderStatusEnum.SHIPPED.getValue());
        boolean b = this.updateById(purchaseOrder);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"状态更新失败");
    }
}