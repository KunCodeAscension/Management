package com.qzh.backend.model.dto.product;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.Inventory;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 库存查询DTO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InventoryQueryDTO extends PageRequest implements Serializable {
    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（模糊查询）
     */
    private String productName;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    public static QueryWrapper<Inventory> getQueryWrapper(InventoryQueryDTO dto) {
        QueryWrapper<Inventory> queryWrapper = new QueryWrapper<>();
        if (dto ==  null) {
            return queryWrapper;
        }
        Long productId = dto.getProductId();
        String productName = dto.getProductName();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.eq(ObjUtil.isNotEmpty(productId),"productId", productId);
        queryWrapper.like(ObjUtil.isNotEmpty(productName),"productName", productName);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}