package com.qzh.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.SaleReturnCreateDTO;
import com.qzh.backend.model.dto.product.SaleReturnQueryDTO;
import com.qzh.backend.model.entity.SaleReturn;
import jakarta.servlet.http.HttpServletRequest;

public interface SaleReturnService extends IService<SaleReturn> {
    Long createSaleReturn(SaleReturnCreateDTO dto, HttpServletRequest request);

    Page<SaleReturn> listMySaleReturns(SaleReturnQueryDTO queryDTO,HttpServletRequest request);
}