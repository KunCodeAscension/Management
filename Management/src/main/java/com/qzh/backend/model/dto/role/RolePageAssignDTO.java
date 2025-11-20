package com.qzh.backend.model.dto.role;

import lombok.Data;
import java.util.List;

/**
 * 角色-页面分配DTO
 */
@Data
public class RolePageAssignDTO {
    /**
     * 要分配给角色的页面ID列表（空列表表示清空角色所有页面关联）
     */
    private List<Long> pageIds;
}