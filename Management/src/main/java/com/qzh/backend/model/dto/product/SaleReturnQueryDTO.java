package com.qzh.backend.model.dto.product;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.SaleReturn;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class SaleReturnQueryDTO extends PageRequest implements Serializable {

    private Integer status;

    private Long userId;

    private Long productId;

    private String productName;

    private Long saleOrderId;

    private Date startTime;

    private Date endTime;

    public static QueryWrapper<SaleReturn> getQueryWrapper(SaleReturnQueryDTO dto) {
        QueryWrapper<SaleReturn> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        Integer status = dto.getStatus();
        Long userId = dto.getUserId();
        Long productId = dto.getProductId();
        String productName = dto.getProductName();
        Long saleOrderId = dto.getSaleOrderId();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        
        queryWrapper.eq(ObjectUtil.isNotNull(status), "status", status);
        queryWrapper.eq(ObjectUtil.isNotNull(userId), "userId", userId);
        queryWrapper.eq(ObjectUtil.isNotNull(productId), "productId", productId);
        queryWrapper.eq(ObjectUtil.isNotNull(saleOrderId), "saleOrderId", saleOrderId);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        queryWrapper.like(StrUtil.isNotBlank(productName), "productName", productName);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        
        return queryWrapper;
    }

}