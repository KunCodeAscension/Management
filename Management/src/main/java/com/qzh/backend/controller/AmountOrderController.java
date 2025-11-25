package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.AmountOrderQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.vo.AmountOrderDetailVO;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.utils.GetLoginUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 金额单Controller
 */
@RestController
@RequestMapping("/amount/order")
@RequiredArgsConstructor
public class AmountOrderController {

    private final AmountOrderService amountOrderService;

    private final GetLoginUserUtil getLoginUserUtil;

    /**
     * 分页查询门店下的所有金额单
     */
    @GetMapping("/list")
    public BaseResponse<Page<AmountOrder>> listAmountOrders(AmountOrderQueryDTO queryDTO) {
        Page<AmountOrder> amountOrderPage = amountOrderService.listAmountOrdersByStoreId(queryDTO);
        return ResultUtils.success(amountOrderPage);
    }

    /**
     * 分页查询自己为付款人的金额单
     */
    @GetMapping("/list/payer")
    public BaseResponse<Page<AmountOrder>> listAmountOrdersByPayer(AmountOrderQueryDTO queryDTO, HttpServletRequest request) {
        User loginUser = getLoginUserUtil.getLoginUser(request);
        queryDTO.setPayerId(loginUser.getId());
        Page<AmountOrder> amountOrderPage = amountOrderService.listAmountOrdersByStoreId(queryDTO);
        return ResultUtils.success(amountOrderPage);
    }

    /**
     * 分页查询自己为收款人的金额单
     */
    @GetMapping("/list/payee")
    public BaseResponse<Page<AmountOrder>> listAmountOrdersByPayee(AmountOrderQueryDTO queryDTO, HttpServletRequest request) {
        User loginUser = getLoginUserUtil.getLoginUser(request);
        queryDTO.setPayeeId(loginUser.getId());
        Page<AmountOrder> amountOrderPage = amountOrderService.listAmountOrdersByStoreId(queryDTO);
        return ResultUtils.success(amountOrderPage);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{id}")
    public BaseResponse<AmountOrderDetailVO> getAmountOrderById(@PathVariable Long id,HttpServletRequest request) {
        AmountOrderDetailVO amountOrderDetail = amountOrderService.getAmountOrderDetail(id, request);
        return ResultUtils.success(amountOrderDetail);
    }

    /**
     * 支付订单 TODO 接入支付宝沙箱
     */
    @PostMapping("/payorder/{id}")
    public BaseResponse<Void> payOrder(@PathVariable Long id,HttpServletRequest request) {
        amountOrderService.payOrder(id,request);
        return ResultUtils.success(null);
    }

    /**
     * 支付订单 TODO 接入支付宝沙箱
     */
    @PostMapping("/cancelorder/{id}")
    public BaseResponse<Void> cancelorder(@PathVariable Long id,HttpServletRequest request) {
        amountOrderService.cancleOrder(id,request);
        return ResultUtils.success(null);
    }
}