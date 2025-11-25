package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.AmountOrderQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.vo.AmountOrderDetailVO;
import jakarta.servlet.http.HttpServletRequest;

public interface AmountOrderService extends IService<AmountOrder> {

    /**
     * 根据门店ID分页查询金额单
     */
    Page<AmountOrder> listAmountOrdersByStoreId(AmountOrderQueryDTO queryDTO);

    void payOrder(Long id, HttpServletRequest request);

    void cancleOrder(Long id, HttpServletRequest request);

    AmountOrderDetailVO getAmountOrderDetail(Long id,HttpServletRequest request);

}