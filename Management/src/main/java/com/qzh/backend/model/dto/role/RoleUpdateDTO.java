package com.qzh.backend.model.dto.role;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "角色名称不能为空")
    private String roleName; // 角色名称

    private String description; // 角色描述（非必填）
}