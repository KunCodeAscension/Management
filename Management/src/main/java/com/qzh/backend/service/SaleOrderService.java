package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.SaleOrderCreateDTO;
import com.qzh.backend.model.dto.product.SaleOrderQueryDTO;
import com.qzh.backend.model.entity.SaleOrder;
import jakarta.servlet.http.HttpServletRequest;

public interface SaleOrderService extends IService<SaleOrder> {

    Long createSaleOrder(SaleOrderCreateDTO createDTO, HttpServletRequest request);

    Page<SaleOrder> listMyOrders(SaleOrderQueryDTO dto, HttpServletRequest request);

    void confirmOrderArrival(Long orderId, HttpServletRequest request);
}