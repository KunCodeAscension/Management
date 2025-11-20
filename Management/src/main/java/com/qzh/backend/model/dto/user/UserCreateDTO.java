package com.qzh.backend.model.dto.user;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

/**
 * 创建用户请求参数
 */
@Data
public class UserCreateDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "用户账号不能为空")
    @Size(min = 4, max = 20, message = "用户账号长度必须在4-20位之间")
    private String userAccount;

    @NotNull(message = "用户密码不能为空")
    @Size(min = 6, max = 20, message = "用户密码长度必须在6-20位之间")
    private String userPassword;

    @NotNull(message = "用户邮箱不能为空")
    private String email;

    @NotNull(message = "用户姓名不能为空")
    private String userName;

    @NotNull(message = "手机号不能为空")
    private String phone;

    private Integer status;
}