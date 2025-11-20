package com.qzh.backend.model.dto.role;

import lombok.Data;

import java.util.List;

@Data
public class RoleAddPermissionDTO {
    /**
     * 要分配的权限ID列表（必填）
     */
    private List<Long> permissionIds;
}