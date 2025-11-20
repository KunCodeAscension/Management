package com.qzh.backend.model.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.qzh.backend.model.entity.PageInfo;
import com.qzh.backend.model.entity.Permission;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class PermissionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String description;

    private Date createTime;

    private Date updateTime;

    private Long createBy;

    private List<PageInfo> pages;

    public static PermissionVO toPermissionVO(Permission permission) {
        if (permission == null) {
            return null;
        }
        return BeanUtil.copyProperties(permission, PermissionVO.class);
    }

    public static List<PermissionVO> toPermissionVOList(List<Permission> permissions) {
        if (CollectionUtil.isEmpty(permissions)) {
            return null;
        }
        return BeanUtil.copyToList(permissions, PermissionVO.class);
    }
}

