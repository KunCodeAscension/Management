package com.qzh.backend.model.dto.user;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 更新用户请求参数
 */
@Data
public class UserUpdateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户姓名不能为空")
    private String userName;

    @NotNull(message = "手机号不能为空")
    private String phone;

    @NotNull(message = "状态不能为空")
    private Integer status;

    private List<Long> roleIds;
}