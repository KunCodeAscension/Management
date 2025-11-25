package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.PurchaseOrderCreateDTO;
import com.qzh.backend.model.dto.product.PurchaseOrderQueryDTO;
import com.qzh.backend.model.entity.PurchaseOrder;
import com.qzh.backend.model.vo.PurchaseOrderListVO;
import jakarta.servlet.http.HttpServletRequest;

public interface PurchaseOrderService extends IService<PurchaseOrder> {

    Long createPurchaseOrder(PurchaseOrderCreateDTO createDTO, HttpServletRequest request);

    Page<PurchaseOrderListVO> listPurchaseOrdersWithAmount(PurchaseOrderQueryDTO queryDTO);

    PurchaseOrderListVO getPurchaseOrderById(Long id);

    void shipPurchaseOrder(Long orderId,HttpServletRequest request);
}