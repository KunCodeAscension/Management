package com.qzh.backend.model.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

@Data
public class WarehouseAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 仓库名称
     */
    @NotBlank(message = "仓库名称不能为空")
    @Size(max = 100, message = "仓库名称长度不能超过100个字符")
    private String name;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    @Size(max = 255, message = "详细地址长度不能超过255个字符")
    private String address;

    /**
     * 仓库描述
     */
    @Size(max = 500, message = "仓库描述长度不能超过500个字符")
    private String description;

}
