package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.SaleOrderMapper;
import com.qzh.backend.model.dto.product.SaleOrderCreateDTO;
import com.qzh.backend.model.dto.product.SaleOrderQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.entity.SaleOrder;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.enums.OrderTypeEnum;
import com.qzh.backend.model.enums.PayStatusEnum;
import com.qzh.backend.model.enums.PayTypeEnum;
import com.qzh.backend.model.enums.SaleOrderStatusEnum;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.service.InventoryService;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SaleOrderServiceImpl extends ServiceImpl<SaleOrderMapper, SaleOrder> implements SaleOrderService {

    private final InventoryService inventoryService;

    private final GetLoginUserUtil getLoginUserUtil;

    private final AppGlobalConfig appGlobalConfig;

    private final AmountOrderService amountOrderService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSaleOrder(SaleOrderCreateDTO createDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(createDTO.getBuyQuantity() <= 0,ErrorCode.PARAMS_ERROR);
        InventoryVO inventory = inventoryService.getInventoryVOById(createDTO.getInventoryId());
        if (inventory == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"库存信息不存在");
        }
        // 校验库存数量是否充足
        if (inventory.getQuantity() < createDTO.getBuyQuantity()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "库存不足，当前库存：" + inventory.getQuantity() );
        }
        BigDecimal totalAmount = inventory.getProductPrice().multiply(BigDecimal.valueOf(createDTO.getBuyQuantity()));
        // 获取登录用户
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 创建销售订单
        SaleOrder saleOrder = new SaleOrder();
        saleOrder.setStoreId(inventory.getStoreId()); // 门店ID从库存中获取
        saleOrder.setUserId(loginUser.getId()); // 购买用户ID
        saleOrder.setProductId(inventory.getProductId()); // 商品ID
        saleOrder.setProductName(inventory.getProductName()); // 商品名称
        saleOrder.setProductUrl(inventory.getProductUrl()); // 商品图片
        saleOrder.setProductDescription(inventory.getProductDescription()); // 商品描述
        saleOrder.setProductPrice(inventory.getProductPrice()); // 销售单价
        saleOrder.setProductQuantity(createDTO.getBuyQuantity()); // 购买数量
        saleOrder.setTotalAmount(totalAmount); // 订单总金额
        saleOrder.setWarehouseId(inventory.getWarehouseId()); // 发货仓库ID
        saleOrder.setStatus(SaleOrderStatusEnum.PENDING.getValue()); // 0-待发货
        saleOrder.setCreateBy(loginUser.getId()); // 创建人ID（用户ID）
        boolean save = this.save(saleOrder);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建销售订单失败");
        }

        // 5. 创建金额订单（记录交易金额）
        AmountOrder amountOrder = new AmountOrder();
        amountOrder.setOrderId(saleOrder.getId()); // 关联销售订单ID
        amountOrder.setType(OrderTypeEnum.SALE.getValue()); // 订单类型：销售订单
        amountOrder.setPayerId(loginUser.getId()); // 付款人ID（用户ID）
        amountOrder.setPayeeId(appGlobalConfig.getManagerId()); // 收款人ID（店长ID）
        amountOrder.setAmount(totalAmount); // 交易金额
        amountOrder.setStoreId(inventory.getStoreId()); // 门店ID
        amountOrder.setPayType(PayTypeEnum.ALIPAY.getValue()); // 支付方式，目前仅支持支付宝
        amountOrder.setStatus(PayStatusEnum.PENDING_PAYMENT.getValue()); // 0-待支付
        amountOrder.setCreateBy(loginUser.getId()); // 创建人ID
        boolean b = amountOrderService.save(amountOrder);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建金额单失败");
        }
        return saleOrder.getId();
    }

    @Override
    public Page<SaleOrder> listMyOrders(SaleOrderQueryDTO dto, HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null, ErrorCode.PARAMS_ERROR);
        int current = dto.getCurrent();
        int size = dto.getSize();
        ThrowUtils.throwIf(size <= 0 || size > 20, ErrorCode.PARAMS_ERROR);
        Page<SaleOrder> saleOrderPage = new Page<>(current, size);
        User loginUser = getLoginUserUtil.getLoginUser(request);
        dto.setUserId(loginUser.getId());
        return this.page(saleOrderPage, SaleOrderQueryDTO.getQueryWrapper(dto));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrderArrival(Long orderId, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 查询订单详情并校验
        SaleOrder saleOrder = this.getById(orderId);
        if (saleOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "订单不存在");
        }
        // 校验订单归属（只能确认自己的订单）
        if (!saleOrder.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作他人订单");
        }
        // 校验订单当前状态（仅已发货状态可确认到货）
        if (!saleOrder.getStatus().equals(SaleOrderStatusEnum.SHIPPED.getValue())) { // 假设1=已发货，2=已完成
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "订单当前状态不支持确认到货");
        }
        // 更新订单状态为已完成
        saleOrder.setStatus(SaleOrderStatusEnum.OVERY.getValue()); // 已完成
        boolean updateSuccess = this.updateById(saleOrder);
        if (!updateSuccess) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "确认到货失败");
        }
    }
}