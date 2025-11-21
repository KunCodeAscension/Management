package com.qzh.backend.model.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class UserLoginDTO {

    @NotNull(message = "账号不能为空")
    @Size(min = 4, max = 20, message = "用户账号长度必须在4-20位之间")
    private String userAccount;

    @NotNull(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "用户密码长度必须在6-20位之间")
    private String userPassword;

}
