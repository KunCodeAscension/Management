package com.qzh.backend.controller;

import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.PurchaseReturnCreateDTO;
import com.qzh.backend.service.PurchaseReturnService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 采退订单Controller
 */
@RestController
@RequestMapping("/purchase/return")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @PostMapping
    public BaseResponse<Long> createPurchaseReturn(@Valid @RequestBody PurchaseReturnCreateDTO createDTO, HttpServletRequest request) {
        Long returnOrderId = purchaseReturnService.createPurchaseReturn(createDTO,request);
        return ResultUtils.success(returnOrderId);
    }
}