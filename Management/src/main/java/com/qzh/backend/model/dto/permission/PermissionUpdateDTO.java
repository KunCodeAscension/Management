package com.qzh.backend.model.dto.permission;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

@Data
public class PermissionUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "权限名称不能为空")
    private String name; // 权限名称

    private String description; // 权限描述（非必填）
}

