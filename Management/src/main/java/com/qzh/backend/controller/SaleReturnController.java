package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.SaleReturnCreateDTO;
import com.qzh.backend.model.dto.product.SaleReturnQueryDTO;
import com.qzh.backend.model.entity.SaleReturn;
import com.qzh.backend.service.SaleReturnService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sale/return")
@RequiredArgsConstructor
public class SaleReturnController {

    private final SaleReturnService saleReturnService;

    @PostMapping("/create")
    public BaseResponse<Long> createSaleReturn(@RequestBody SaleReturnCreateDTO dto, HttpServletRequest request) {
        Long returnOrderId = saleReturnService.createSaleReturn(dto, request);
        return ResultUtils.success(returnOrderId);
    }

    @GetMapping("/my")
    public BaseResponse<Page<SaleReturn>> listMySaleReturns(SaleReturnQueryDTO queryDTO,HttpServletRequest request) {
        Page<SaleReturn> returnPage = saleReturnService.listMySaleReturns(queryDTO,request);
        return ResultUtils.success(returnPage);
    }

    @GetMapping("/store")
    public BaseResponse<Page<SaleReturn>> listStoreSaleOrders(SaleReturnQueryDTO queryDTO) {
        Page<SaleReturn> page = saleReturnService.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), SaleReturnQueryDTO.getQueryWrapper(queryDTO));
        return ResultUtils.success(page);
    }

    @GetMapping("{id}")
    public BaseResponse<SaleReturn> getSaleOrderById(@PathVariable Long id) {
        SaleReturn saleReturn = saleReturnService.getById(id);
        ThrowUtils.throwIf(saleReturn == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(saleReturn);
    }

}