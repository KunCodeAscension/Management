package com.qzh.backend.model.dto.page;

import lombok.Data;
import java.util.List;

/**
 * 页面权限分配DTO（修改接口用）
 */
@Data
public class PageEditPermissionDTO {
    /**
     * 要关联的权限ID列表（空列表表示清空页面所有权限）
     */
    private List<Long> permissionIds;
}