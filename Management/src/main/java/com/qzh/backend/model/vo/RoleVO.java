package com.qzh.backend.model.vo;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.entity.Role;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class RoleVO implements Serializable {

    private Long id;


    private String roleName;


    private String description;


    private List<Permission> permissions;


    private Date createTime;


    private Date updateTime;


    private Long createBy;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    public static RoleVO toRoleVO(Role role) {
        if (role == null) {
            return null;
        }
        return BeanUtil.copyProperties(role, RoleVO.class);
    }

    public static List<RoleVO> toRoleVOList(List<Role> roles) {
        if (CollectionUtil.isEmpty(roles)) {
            return null;
        }
        return BeanUtil.copyToList(roles, RoleVO.class);
    }

}