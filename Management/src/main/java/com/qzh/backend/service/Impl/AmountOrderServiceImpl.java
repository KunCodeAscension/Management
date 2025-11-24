package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.AmountOrderMapper;
import com.qzh.backend.model.dto.product.AmountOrderQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.utils.ThrowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmountOrderServiceImpl extends ServiceImpl<AmountOrderMapper, AmountOrder> implements AmountOrderService {

    private final AppGlobalConfig  appGlobalConfig;

    @Override
    public Page<AmountOrder> listAmountOrdersByStoreId(AmountOrderQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size > 20,ErrorCode.PARAMS_ERROR);
        Page<AmountOrder> page = new Page<>(current, size);
        queryDTO.setStoreId(appGlobalConfig.getCurrentStoreId());
        return this.page(page, AmountOrderQueryDTO.getQueryWrapper(queryDTO));
    }
}