package com.qzh.backend.model.dto.warehouse;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.model.dto.page.PageQueryDTO;
import com.qzh.backend.model.entity.Warehouse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class WarehouseQueryDTO extends PageQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 仓库名称 (模糊查询)
     */
    private String name;

    /**
     * 仓库地址 (模糊查询)
     */
    private String address;

    public static QueryWrapper<Warehouse> getQueryWrapper(WarehouseQueryDTO dto) {
        QueryWrapper<Warehouse> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        String name = dto.getName();
        String address = dto.getAddress();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(address), "address", address);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

}
