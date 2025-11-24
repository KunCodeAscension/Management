package com.qzh.backend.model.dto.product;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductQueryDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 商品状态
     */
    private Integer status;

    public static QueryWrapper<Product> getQueryWrapper(ProductQueryDTO dto) {
        QueryWrapper<Product> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        String name = dto.getName();
        String description = dto.getDescription();
        Long supplierId = dto.getSupplierId();
        Integer status = dto.getStatus();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.eq(ObjectUtil.isNotNull(supplierId),"supplierId", supplierId);
        queryWrapper.like(StrUtil.isNotEmpty(name),"name", name);
        queryWrapper.like(StrUtil.isNotEmpty(description),"description", description);
        queryWrapper.eq(ObjectUtil.isNotNull(status),"status", status);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }


}