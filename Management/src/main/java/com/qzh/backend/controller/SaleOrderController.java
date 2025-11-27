package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.SaleOrderCreateDTO;
import com.qzh.backend.model.dto.product.SaleOrderQueryDTO;
import com.qzh.backend.model.entity.SaleOrder;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/sale/order")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    @PostMapping("/create")
    public BaseResponse<Long> createSaleOrder(@Valid @RequestBody SaleOrderCreateDTO createDTO, HttpServletRequest request) {
        Long saleOrderId = saleOrderService.createSaleOrder(createDTO,request);
        return ResultUtils.success(saleOrderId);
    }

    @GetMapping("/my")
    public BaseResponse<Page<SaleOrder>> listMySaleOrders(SaleOrderQueryDTO queryDTO, HttpServletRequest request) {
        Page<SaleOrder> orderPage = saleOrderService.listMyOrders(queryDTO,request);
        return ResultUtils.success(orderPage);
    }

    @GetMapping("{id}")
    public BaseResponse<SaleOrder> getSaleOrderById(@PathVariable Long id) {
        SaleOrder saleOrder = saleOrderService.getById(id);
        ThrowUtils.throwIf(saleOrder == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(saleOrder);
    }

    @GetMapping("/store")
    public BaseResponse<Page<SaleOrder>> listStoreSaleOrders(SaleOrderQueryDTO queryDTO) {
        Page<SaleOrder> page = saleOrderService.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), SaleOrderQueryDTO.getQueryWrapper(queryDTO));
        return ResultUtils.success(page);
    }

    @PostMapping("/confirm/{id}")
    public BaseResponse<Void> confirmOrderArrival(@PathVariable Long id, HttpServletRequest request) {
        saleOrderService.confirmOrderArrival(id, request);
        return ResultUtils.success(null);
    }

}