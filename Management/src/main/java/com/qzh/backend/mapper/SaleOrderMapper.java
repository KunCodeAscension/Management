package com.qzh.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qzh.backend.model.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SaleOrderMapper extends BaseMapper<SaleOrder> {
}