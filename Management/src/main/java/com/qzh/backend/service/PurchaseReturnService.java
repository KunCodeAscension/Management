package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.PurchaseReturnCreateDTO;
import com.qzh.backend.model.entity.PurchaseReturn;
import jakarta.servlet.http.HttpServletRequest;

public interface PurchaseReturnService extends IService<PurchaseReturn> {

    /**
     * @param createDTO 简化的创建采退订单请求DTO
     * @return 采退订单ID
     */
    Long createPurchaseReturn(PurchaseReturnCreateDTO createDTO, HttpServletRequest request);

    void confirmPurchaseReturn(Long returnId, HttpServletRequest request);
}