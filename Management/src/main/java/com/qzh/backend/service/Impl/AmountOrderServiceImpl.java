package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.mapper.AmountOrderMapper;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.service.AmountOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmountOrderServiceImpl extends ServiceImpl<AmountOrderMapper, AmountOrder> implements AmountOrderService {


}