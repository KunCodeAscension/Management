package com.qzh.backend.model.dto.permission;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.Permission;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionQueryDto extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 开始时间（用于查询创建时间范围）
     */
    private Date startTime;

    /**
     * 结束时间（用于查询创建时间范围）
     */
    private Date endTime;

    public static QueryWrapper<Permission> getQueryWrapper(PermissionQueryDto dto) {
        QueryWrapper<Permission> queryWrapper = new QueryWrapper<>();
        if (dto == null) {
            return queryWrapper;
        }
        String name = dto.getName();
        String description = dto.getDescription();
        Date startTime = dto.getStartTime();
        Date endTime = dto.getEndTime();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        queryWrapper.like(ObjUtil.isNotNull(name), "name", name);
        queryWrapper.like(ObjUtil.isNotNull(description), "description", description);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}
