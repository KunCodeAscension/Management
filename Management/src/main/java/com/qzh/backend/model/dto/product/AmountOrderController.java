package com.qzh.backend.model.dto.product;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.utils.GetLoginUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 金额单Controller
 */
@RestController
@RequestMapping("/api/amount/order")
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
}