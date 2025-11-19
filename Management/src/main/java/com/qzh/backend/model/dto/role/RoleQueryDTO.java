package com.qzh.backend.model.dto.role;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class RoleQueryDTO extends PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String roleName;

    private String description;

    private Date startTime;

    private Date endTime;

    public static QueryWrapper<Role> getQueryWrapper(RoleQueryDTO roleQueryDTO) {
        QueryWrapper<Role> queryWrapper = new QueryWrapper<>();
        if (roleQueryDTO == null){
            return queryWrapper;
        }
        String roleName = roleQueryDTO.getRoleName();
        String description = roleQueryDTO.getDescription();
        Date startTime = roleQueryDTO.getStartTime();
        Date endTime = roleQueryDTO.getEndTime();
        queryWrapper.like(ObjUtil.isNotNull(roleName),"roleName",roleName);
        queryWrapper.like(ObjUtil.isNotNull(description),"description",description);
        queryWrapper.ge(ObjUtil.isNotEmpty(startTime), "createTime", startTime);
        queryWrapper.lt(ObjUtil.isNotEmpty(endTime), "createTime", endTime);
        return queryWrapper;
    }
}